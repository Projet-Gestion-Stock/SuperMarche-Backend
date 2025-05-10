package application.supermarche.Services.PackageUtilisateur;

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

            return utilisateur;
        } catch (Exception e) {
            log.error("Erreur technique loadUserByUsername: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public UtilisateurDTO createUtilisateur(Utilisateur utilisateur) {
        try {
            log.info("Création utilisateur: {}", utilisateur.getEmail());

            if (utilisateurRepository.existsByEmail(utilisateur.getEmail())) {
                throw new BusinessException("Email déjà utilisé", ErrorCode.EMAIL_ALREADY_USED);
            }

            utilisateur.setMotDePasse(passwordEncoder.encode(utilisateur.getMotDePasse()));
            utilisateur.setActif(true);

            Utilisateur savedUser = utilisateurRepository.save(utilisateur);
            log.info("Utilisateur créé ID: {}", savedUser.getId());

            return utilisateurMapper.toDTO(savedUser);

        } catch (DataIntegrityViolationException e) {
            log.error("Erreur base de données: {}", e.getMessage());
            throw new BusinessException("Erreur de création utilisateur", ErrorCode.DATABASE_ERROR);
        }
    }

    public UtilisateurDTO getUtilisateurById(Long id) {
        return utilisateurRepository.findById(id)
                .map(utilisateurMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Utilisateur non trouvé ID: {}", id);
                    return new ResourceNotFoundException("Utilisateur introuvable", ErrorCode.USER_NOT_FOUND);
                });
    }

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

    public List<UtilisateurDTO> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(utilisateurMapper::toDTO)
                .collect(Collectors.toList());
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