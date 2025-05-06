package application.supermarche.Controllers;

import application.supermarche.DTO.PackageUtilisateur.AuthenticationDTO;
import application.supermarche.DTO.PackageUtilisateur.UpdateUtilisateurDTO;
import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Repository.UtilisateurRepository;
import application.supermarche.Securite.JwtService;
import application.supermarche.Services.PackageUtilisateur.ActivationUtilisateurService;
import application.supermarche.Services.PackageUtilisateur.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("user")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ActivationUtilisateurService activationService;
    private final UtilisateurRepository utilisateurRepository;

    public UtilisateurController(UtilisateurService utilisateurService,
                                 AuthenticationManager authenticationManager,
                                 JwtService jwtService,
                                 ActivationUtilisateurService activationService, UtilisateurRepository utilisateurRepository) {
        this.utilisateurService = utilisateurService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.activationService = activationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    // connexion de l'utilisateur

    @PostMapping(path = "connexion",consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> connexion(@RequestBody AuthenticationDTO authenticationDTO) {
        final Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationDTO.username(), authenticationDTO.password())
        );
        if (authenticate.isAuthenticated()){
            return this.jwtService.generate(authenticationDTO.username());
        }
        return null;
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


    // creer un utilisateur

    @PostMapping(path = "inscription",consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UtilisateurDTO createUtilisateur(@RequestBody Utilisateur utilisateur) {
        return utilisateurService.createUtilisateur(utilisateur);
    }

    // Activer un utilisateur

    @PostMapping("{id}/activation")
    public ResponseEntity<Void> changerActivation(
            @PathVariable Long id,
            @RequestParam boolean actif) {

        // Récupération de l'email depuis le contexte
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // Récupération de l'utilisateur complet
        Utilisateur modifiePar = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        activationService.changerStatutActivation(id, modifiePar, actif);
        return ResponseEntity.ok().build();
    }

    // Liste des utilisateurs

    @GetMapping(path = "liste",produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public List<UtilisateurDTO> getAllUtilisateurs() {
        return utilisateurService.getAllUtilisateurs();
    }

    // Afficher un utilisateur

    @GetMapping(path = "information/{id}",produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UtilisateurDTO getUtilisateurById(@PathVariable Long id) {
        return utilisateurService.getUtilisateurById(id);
    }


    // mettre a jour un utilisateur

    @PutMapping(path = "modifier/{id}", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public UtilisateurDTO updateUtilisateur(
            @PathVariable Long id,
            @RequestBody UpdateUtilisateurDTO updateDTO) {
        return utilisateurService.updateUtilisateur(id, updateDTO);
    }

}
