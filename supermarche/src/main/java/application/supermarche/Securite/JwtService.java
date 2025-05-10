package application.supermarche.Securite;

import application.supermarche.Entites.PackageJwt.Jwt;
import application.supermarche.Entites.PackageJwt.RefreshToken;
import application.supermarche.Entites.PackageUtilisateur.HistoriqueConnexion;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Exceptions.InvalidTokenException;
import application.supermarche.Exceptions.JwtException;
import application.supermarche.Exceptions.TokenExpiredException;
import application.supermarche.Repository.HistoriqueConnexionRepository;
import application.supermarche.Repository.JwtRepository;
import application.supermarche.Repository.RefreshTokenRepository;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
public class JwtService {

    public static final String BEARER = "Bearer";
    public static final String REFRESH = "refresh";

    @Value("${Jwt.secret.key}")
    private String secretKey;

    private final UtilisateurService utilisateurService;
    private final JwtRepository jwtRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final HistoriqueConnexionRepository historiqueConnexionRepository;

    public JwtService(UtilisateurService utilisateurService, JwtRepository jwtRepository, RefreshTokenRepository refreshTokenRepository, HistoriqueConnexionRepository historiqueConnexionRepository) {
        this.utilisateurService = utilisateurService;
        this.jwtRepository = jwtRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.historiqueConnexionRepository = historiqueConnexionRepository;
    }

    // mettre le token jwt a l'etat desactive

    private void desactiveToken(Utilisateur utilisateur){
        List<Jwt> jwtList = this.jwtRepository.findUtilisateur(utilisateur.getEmail()).peek(
                jwt -> {
                    jwt.setDesactive(true);
                    jwt.setExpire(true);
                }
        ).collect(Collectors.toList());

        this.jwtRepository.saveAll(jwtList);
    }

    // suppresion authomatique des token desactivé

    @Scheduled(cron = "0 10 0 * * *") // s'execute tout les jour à 00h10 : 0 10 0 * * *
    public void removeUseLesJwt(){
        log.info("Suppression des tokens à {}", Instant.now());
        this.jwtRepository.deleteAllByExpireAndDesactive(true,true);
    }

    // suppresion authomatique des refresh token desactivé

    @Scheduled(cron = "0 10 0 * * *") // s'execute tout les jour à 00h10 : 0 10 0 * * *
    public void nettoyerTokensExpires() {
        Date now = new Date();
        List<RefreshToken> expires = refreshTokenRepository.findByExpireTrueOrExpirationBefore(now);
        refreshTokenRepository.deleteAll(expires);
    }

    // Enregistrer l'historique de connexion

    public void enregistrerHistorique(String email, String action) {
        Utilisateur utilisateur = (Utilisateur) utilisateurService.loadUserByUsername(email);

        HistoriqueConnexion historique = HistoriqueConnexion.builder()
                .email(email)
                .action(action)
                .timestamp(new Date())
                .utilisateur(utilisateur)
                .build();

        historiqueConnexionRepository.save(historique);
    }

    public String extractUsername(String token) {
        try {
            return getClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.error("Erreur lors de l'extraction du username: {}", e.getMessage());
            throw new InvalidTokenException("Token invalide");
        }
    }

    // Récupère l’email (dans claims personnalisés)
    public String extractEmail(String token) {
        return getClaim(token, claims -> claims.get("email", String.class));
    }

    // Récupère le rôle
    public String extractRole(String token) {
        return getClaim(token, claims -> claims.get("role", String.class));
    }

    // Vérifie si le token est expiré
    public boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    // Extraction générique d’un claim
    private <T> T getClaim(String token, Function<Claims, T> function) {
        Claims claims = getAllClaims(token);
        return function.apply(claims);
    }

    // Extraction de tous les claims
    private Claims getAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expiré");
            throw new TokenExpiredException("Token expiré");
        } catch (Exception e) {
            log.error("Token invalide: {}", e.getMessage());
            throw new InvalidTokenException("Token invalide");
        }
    }

    public Map<String, String> generate(String username) {
        Utilisateur utilisateur = (Utilisateur) this.utilisateurService.loadUserByUsername(username);

        // désactive tous les anciens tokens de cet utilisateur
        this.desactiveToken(utilisateur);

        Map<String, String> jwtMap = new HashMap<>(this.generateJwt(utilisateur));

        RefreshToken refreshToken = RefreshToken.builder()
                .valeur(UUID.randomUUID().toString())
                .expire(false)
                .creation(new Date())
                .expiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 jours plus tard
                .build();

        Jwt jwt = Jwt.builder()
                .valeur(jwtMap.get(BEARER))
                .desactive(false)
                .expire(false)
                .refreshToken(refreshToken)
                .utilisateur(utilisateur)
                .build();

        this.jwtRepository.save(jwt); // ça sauvegarde aussi le refreshToken à cause du cascade

        // Enregistre l'action Connexion
        this.enregistrerHistorique(utilisateur.getEmail(), "CONNEXION");

        jwtMap.put("refresh", refreshToken.getValeur());

        return jwtMap;
    }


    private Map<String, String> generateJwt(Utilisateur utilisateur) {

        // Claims personnalisés
        Map<String, Object> claims = Map.of(
                "id", utilisateur.getId(),
                "role", utilisateur.getRole().name() // Utilisez name() au lieu de getAuthority()
        );

        // Date actuelle
        Date nowDate = new Date();

        // Expiration chaque 30 minutes à partir du lancement

        LocalDateTime expirationDateTime = LocalDateTime.now().plusMinutes(30);
        Date expirationDate = Date.from(expirationDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // Génération du token
        String jwt = Jwts.builder()
                .setClaims(claims)
                .setSubject(utilisateur.getEmail())
                .setIssuedAt(nowDate)
                .setExpiration(expirationDate)
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();

        // Retourner le token dans une Map
        return Map.of(BEARER, jwt);
    }

    @PostConstruct
    public void validateKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            if (keyBytes.length != 32) {
                log.error("Clé JWT de taille incorrecte: {} bytes", keyBytes.length);
                throw new IllegalStateException("La clé JWT doit faire exactement 32 bytes (256 bits)");
            }
            log.info("Configuration JWT valide");
        } catch (IllegalArgumentException e) {
            log.error("Clé JWT Base64 invalide");
            throw new JwtException("Configuration JWT invalide", e); // Maintenant cela fonctionnera
        }
    }

    private Key getKey() {
        try {
            // Vérification que la clé est bien en base64
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            if (keyBytes.length < 32) {
                throw new IllegalArgumentException("La clé doit faire au moins 256 bits (32 bytes)");
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            log.error("Erreur de configuration de la clé JWT: {}", e.getMessage());
            throw new RuntimeException("Configuration JWT invalide", e);
        }
    }

    public void deconnexion(String username) {
        Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(username);

        jwtRepository.findUtilisateurValidToken(
                        utilisateur.getEmail(),
                        false,
                        false
                )
                .ifPresent(token -> {
                    token.setExpire(true);
                    token.setDesactive(true);
                    jwtRepository.save(token);

                    // Enregistre l'action Deconnexion
                    enregistrerHistorique(utilisateur.getEmail(), "DECONNEXION");
                });
    }

    public Map<String, String> refreshToken(Map<String, String> refreshTokenRequest) {
        String refresh = refreshTokenRequest.get(REFRESH);

        Jwt jwt = this.jwtRepository.findByRefreshToken(refresh)
                .orElseThrow(() -> {
                    log.warn("Refresh token introuvable");
                    return new InvalidTokenException("Refresh token invalide");
                });

        RefreshToken refreshToken = jwt.getRefreshToken();

        if (refreshToken.isExpire() || refreshToken.getExpiration().before(new Date())) {
            log.warn("Tentative d'utilisation de refresh token expiré");
            throw new TokenExpiredException("Refresh token expiré");
        }

        try {
            this.enregistrerHistorique(jwt.getUtilisateur().getEmail(), "REFRESH");
            this.desactiveToken(jwt.getUtilisateur());
            return this.generate(jwt.getUtilisateur().getEmail());
        } catch (Exception e) {
            log.error("Erreur lors du refresh token: {}", e.getMessage());
            throw new JwtException("Erreur lors du renouvellement du token");
        }
    }

}
