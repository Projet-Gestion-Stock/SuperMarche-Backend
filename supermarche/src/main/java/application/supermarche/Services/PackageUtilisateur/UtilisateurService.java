package application.supermarche.Services.PackageUtilisateur;

import application.supermarche.DTO.PackageUtilisateur.InscriptionUtilisateurDTO;
import application.supermarche.DTO.PackageUtilisateur.UpdateUtilisateurDTO;
import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Enumeration.RoleUtilisateur;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Mapper.UtilisateurMapper;
import application.supermarche.Repository.JwtRepository;
import application.supermarche.Repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import application.supermarche.Enumeration.ErrorCode;


@Slf4j
@Service
public class UtilisateurService implements UserDetailsService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final JwtRepository jwtRepository;

    public UtilisateurService(BCryptPasswordEncoder passwordEncoder, UtilisateurRepository utilisateurRepository, UtilisateurMapper utilisateurMapper, JwtRepository jwtRepository) {
        this.passwordEncoder = passwordEncoder;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurMapper = utilisateurMapper;
        this.jwtRepository = jwtRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            log.debug("Recherche utilisateur: {}", username);
            Utilisateur utilisateur = utilisateurRepository.findByEmail(username)
                    .orElseThrow(() -> {
                        log.warn("Utilisateur non trouvé: {}", username);
                        return new UsernameNotFoundException("Identifiants incorrects");
                    });

            if (!utilisateur.isActif()) {
                log.warn("Tentative de connexion compte inactif: {}", username);
                throw new DisabledException("Compte désactivé");
            }

            // Vérification que le compte n'est pas supprimé (soft delete)
            if (utilisateur.isSupprime()) {
                log.warn("Tentative de connexion compte supprimé: {}", username);
                throw new DisabledException("Compte supprimé");
            }

            return utilisateur;
        } catch (Exception e) {
            log.error("Erreur technique loadUserByUsername: {}", e.getMessage());
            throw e;
        }
    }

    // Ajouter personnel

    public Utilisateur createUtilisateur(InscriptionUtilisateurDTO dto) {
        Utilisateur user = new Utilisateur();
        user.setNom(dto.nom());
        user.setEmail(dto.email());
        user.setRole(RoleUtilisateur.valueOf(dto.role()));
        user.setMotDePasse(passwordEncoder.encode(dto.motDePasse()));
        user.setActif(true); // Par défaut actif à la création
        return utilisateurRepository.save(user);
    }

    // Information sur un personnel

    public UtilisateurDTO getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .map(utilisateurMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé ID: {}", id);
                    return new ResourceNotFoundException("Utilisateur introuvable", ErrorCode.USER_NOT_FOUND);
                });
    }

    // Modifier info de un personnel

    @Transactional
    public UtilisateurDTO updateUtilisateur(Long id, UpdateUtilisateurDTO updateDTO) {
        try {
            log.info("Mise à jour utilisateur ID: {}", id);

            Utilisateur utilisateur = utilisateurRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable", ErrorCode.USER_NOT_FOUND));

            // Validation email
            if (!utilisateur.getEmail().equals(updateDTO.email()) &&
                    utilisateurRepository.existsByEmail(updateDTO.email())) {
                throw new BusinessException("Email déjà utilisé", ErrorCode.EMAIL_ALREADY_USED);
            }

            // Mise à jour des champs
            utilisateur.setNom(updateDTO.nom());
            utilisateur.setEmail(updateDTO.email());

            if (updateDTO.role() != null) {
                try {
                    utilisateur.setRole(RoleUtilisateur.valueOf(updateDTO.role()));
                } catch (IllegalArgumentException e) {
                    throw new BusinessException("Rôle invalide", ErrorCode.INVALID_ROLE);
                }
            }

            return utilisateurMapper.toDTO(utilisateurRepository.save(utilisateur));

        } catch (DataIntegrityViolationException e) {
            log.error("Erreur base de données: {}", e.getMessage());
            throw new BusinessException("Erreur de mise à jour", ErrorCode.DATABASE_ERROR);
        }
    }

    // liste du personnel

    public List<UtilisateurDTO> getAllUtilisateurs() {
        return utilisateurRepository.findBySupprimeFalse().stream()
                .map(utilisateurMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Marquer utilisateur comme supprimé

    public void softDeleteUtilisateur(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'id : " + id));

        if (utilisateur.isSupprime()) {
            throw new IllegalStateException("L'utilisateur est déjà supprimé");
        }

        utilisateur.setSupprime(true);
        utilisateurRepository.save(utilisateur);
    }

    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }

    // Ajoutez cette méthode pour récupérer l'utilisateur complet
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }
}