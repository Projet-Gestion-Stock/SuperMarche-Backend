package application.supermarche.Securite;

import application.supermarche.Entites.PackageJwt.Jwt;
import application.supermarche.Exceptions.InvalidTokenException;
import application.supermarche.Exceptions.TokenExpiredException;
import application.supermarche.Repository.JwtRepository;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;


@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;
    private final JwtRepository jwtRepository;

    public JwtFilter(JwtService jwtService, UtilisateurService utilisateurService,
                     JwtRepository jwtRepository) {
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
        this.jwtRepository = jwtRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            final String authHeader = request.getHeader("Authorization");

            // 1. Vérification basique du header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            // 2. Extraction et validation du token
            final String token = authHeader.substring(7);
            final String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                validateAndAuthenticateToken(token, username);
            }

        } catch (TokenExpiredException e) {
            log.warn("Token expiré: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token expiré");
            return;
        } catch (InvalidTokenException e) {
            log.warn("Token invalide: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token invalide");
            return;
        } catch (DisabledException e) {
            log.warn("Compte désactivé: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "Compte désactivé");
            return;
        } catch (Exception e) {
            log.error("Erreur d'authentification JWT: {}", e.getMessage());
            sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Erreur d'authentification");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void validateAndAuthenticateToken(String token, String username) {
        Optional<Jwt> tokenInDB = jwtRepository.findByValeur(token);

        if (tokenInDB.isEmpty()) {
            log.warn("Token non trouvé en base pour l'utilisateur: {}", username);
            throw new InvalidTokenException("Token non valide");
        }

        Jwt jwt = tokenInDB.get();
        boolean isTokenValid = !jwtService.isTokenExpired(token) &&
                !jwt.isExpire() &&
                !jwt.isDesactive();

        if (!isTokenValid) {
            log.warn("Token invalide (expiré/revoké) pour l'utilisateur: {}", username);
            throw new InvalidTokenException("Token invalide");
        }

        if (!jwt.getUtilisateur().getEmail().equals(username)) {
            log.warn("Incohérence token/utilisateur: {}", username);
            throw new InvalidTokenException("Incohérence token/utilisateur");
        }

        UserDetails userDetails = utilisateurService.loadUserByUsername(username);

        // Convertir les rôles au format Spring Security
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities().stream()
                .map(a -> new SimpleGrantedAuthority("ROLE_" + a.getAuthority()))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.debug("Utilisateur authentifié: {}", username);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setContentType("application/json");
        response.setStatus(status.value());
        response.getWriter().write(
                String.format("{\"status\": %d, \"message\": \"%s\"}",
                        status.value(), message)
        );
    }
}
