package application.supermarche.Controllers;

import application.supermarche.DTO.PackageUtilisateur.*;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Enumeration.RoleUtilisateur;
import application.supermarche.Exceptions.ApiException;
import application.supermarche.Repository.UtilisateurRepository;
import application.supermarche.Securite.JwtService;
import application.supermarche.Services.PackageUtilisateur.ActivationUtilisateurService;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@Slf4j
@RestController
@RequestMapping("user")
public class UtilisateurController {

    private static final Logger logger = LoggerFactory.getLogger(UtilisateurController.class);
    private final UtilisateurService utilisateurService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ActivationUtilisateurService activationService;
    private final UtilisateurRepository utilisateurRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UtilisateurController(UtilisateurService utilisateurService,
                                 AuthenticationManager authenticationManager,
                                 JwtService jwtService,
                                 ActivationUtilisateurService activationService, UtilisateurRepository utilisateurRepository, BCryptPasswordEncoder passwordEncoder) {
        this.utilisateurService = utilisateurService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.activationService = activationService;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Connexion

    @PostMapping(path = "connexion", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> connexion(@RequestBody AuthenticationDTO authDTO) {
        try {
            log.info("Connexion tentative: {}", authDTO.username());

            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authDTO.username(), authDTO.password())
            );

            Utilisateur user = (Utilisateur) auth.getPrincipal();
            Map<String, String> tokens = jwtService.generate(authDTO.username());

            Map<String, Object> response = new HashMap<>(tokens);
            response.put("role", user.getRole().name());
            response.put("userId", user.getId());
            response.put("email", user.getEmail());

            log.info("Connexion réussie: {}", authDTO.username());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Identifiants invalides: {}", authDTO.username());
            throw new ApiException("Identifiants incorrects", HttpStatus.UNAUTHORIZED);
        } catch (DisabledException e) {
            log.warn("Compte désactivé: {}", authDTO.username());
            throw new ApiException("Compte désactivé", HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Erreur connexion: {}", e.getMessage());
            throw new ApiException("Erreur d'authentification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ajouter personnel

    @PostMapping("/gerant/inscription")
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurDTO inscrireUtilisateur(
            @Valid @RequestBody InscriptionUtilisateurDTO dto) {

        // Validation du rôle
        try {
            RoleUtilisateur.valueOf(dto.role());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle invalide");
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(dto.nom());
        utilisateur.setEmail(dto.email());
        utilisateur.setMotDePasse(passwordEncoder.encode(dto.motDePasse()));
        utilisateur.setRole(RoleUtilisateur.valueOf(dto.role()));
        utilisateur.setActif(true);

        Utilisateur saved = utilisateurRepository.save(utilisateur);

        return new UtilisateurDTO(
                saved.getId(),
                saved.getNom(),
                saved.getEmail(),
                saved.getRole().name()
        );
    }

    // Activer ou desactiver personnel

    @PostMapping("gerant/{id}/activation")
    public ResponseEntity<Void> changerActivation(
            @PathVariable Long id,
            @RequestParam boolean actif) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Changement activation ID: {} par {}", id, email);

            Utilisateur modificateur = utilisateurRepository.findByEmail(email)
                    .orElseThrow(() -> new ApiException("Non autorisé", HttpStatus.FORBIDDEN));

            activationService.changerStatutActivation(id, modificateur, actif);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Erreur activation: {}", e.getMessage());
            throw new ApiException("Erreur de modification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // liste du personnel

    @GetMapping("/gerant/liste")
    public List<UtilisateurAvecEtatDTO> getAllUtilisateurs() {
        return utilisateurRepository.findBySupprimeFalse().stream()
                .map(UtilisateurAvecEtatDTO::new)
                .collect(Collectors.toList());
    }

    // Information sur un personnel

    @GetMapping("gerant/information/{id}")
    public UtilisateurDTO getUtilisateurById(@PathVariable Long id) {
        return utilisateurService.getUtilisateurById(id);
    }

    // Modifier info de un personnel

    @PutMapping("gerant/modifier/{id}")
    public UtilisateurDTO updateUtilisateur(
            @PathVariable Long id,
            @RequestBody UpdateUtilisateurDTO updateDTO) {
        return utilisateurService.updateUtilisateur(id, updateDTO);
    }

    // Supprimer un personnel

    @DeleteMapping("gerant/delete/{id}")
    public ResponseEntity<Void> softDeleteUtilisateur(@PathVariable Long id) {
        utilisateurService.softDeleteUtilisateur(id);
        return ResponseEntity.noContent().build();
    }

    // deconnexion de l'utilisateur

    @PostMapping(path = "deconnexion", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Map<String, String>> deconnexion(
            @RequestHeader("Authorization") String authHeader) {

        String jwt = authHeader.substring(7); // Supprime "Bearer "
        String username = jwtService.extractUsername(jwt);

        jwtService.deconnexion(username);

        return ResponseEntity.ok(Map.of(
                "message", "Déconnexion réussie pour " + username,
                "timestamp", Instant.now().toString()
        ));
    }

    // Refresh Token

    @PostMapping(path = "refresh-token", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody Map<String, String> refreshTokenRequest) {
        Map<String, String> tokens = this.jwtService.refreshToken(refreshTokenRequest);
        return ResponseEntity.ok(tokens);
    }
}
