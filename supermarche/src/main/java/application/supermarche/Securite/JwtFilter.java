package application.supermarche.Securite;

import application.supermarche.Entites.PackageJwt.Jwt;
import application.supermarche.Repository.JwtRepository;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtilisateurService utilisateurService;
    private final JwtRepository jwtRepository;

    public JwtFilter(JwtService jwtService, UtilisateurService utilisateurService, JwtRepository jwtRepository) {
        this.jwtService = jwtService;
        this.utilisateurService = utilisateurService;
        this.jwtRepository = jwtRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String token;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = authHeader.substring(7);
        username = jwtService.extractUsername(token);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<Jwt> tokenInDB = jwtRepository.findByValeur(token);

            // Token présent ET actif ET non expiré
            if (tokenInDB.isPresent()) {
                Jwt jwt = tokenInDB.get();
                boolean isTokenValid = !jwtService.isTokenExpired(token) && !jwt.isExpire() && !jwt.isDesactive();

                if (isTokenValid && jwt.getUtilisateur().getEmail().equals(username)) {
                    UserDetails userDetails = utilisateurService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
