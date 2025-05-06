package application.supermarche.Services.PackageUtilisateur;

import application.supermarche.DTO.PackageUtilisateur.UpdateUtilisateurDTO;
import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Enumeration.RoleUtilisateur;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Mapper.UtilisateurMapper;
import application.supermarche.Repository.JwtRepository;
import application.supermarche.Repository.UtilisateurRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.context.support.BeanDefinitionDsl;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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


    // connexion de l'utilisateur

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = this.utilisateurRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun Utilisateur ne correspond"));

        if (!utilisateur.isEnabled()) {
            throw new DisabledException("Le compte utilisateur est désactivé");
        }

        return utilisateur; // Retourne directement l'entité qui implémente UserDetails
    }

    // Ajoutez cette méthode pour récupérer l'utilisateur complet
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
    }


    // Creer un utilisateur

    @Transactional
    public UtilisateurDTO createUtilisateur(Utilisateur utilisateur) {
        String motDePasseCrypte = this.passwordEncoder.encode(utilisateur.getMotDePasse());
        utilisateur.setMotDePasse(motDePasseCrypte);
        utilisateur.setActif(true); // Par défaut, le compte est actif
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        return utilisateurMapper.toDTO(savedUtilisateur);
    }

    // Liste des Utilisateurs

    public List<UtilisateurDTO> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(utilisateurMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Afficher un utilisateur

    public UtilisateurDTO getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));
        return utilisateurMapper.toDTO(utilisateur);
    }

    // Mettre a jour un utilisateur

    @Transactional
    public UtilisateurDTO updateUtilisateur(Long id, UpdateUtilisateurDTO updateDTO) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        utilisateur.setNom(updateDTO.nom());
        utilisateur.setEmail(updateDTO.email());

        // Conversion explicite du String vers RoleUtilisateur
        if (updateDTO.role() != null && !updateDTO.role().isEmpty()) {
            try {
                utilisateur.setRole(RoleUtilisateur.valueOf(updateDTO.role()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Rôle invalide. Valeurs autorisées: " +
                        Arrays.toString(RoleUtilisateur.values()));
            }
        }

        return utilisateurMapper.toDTO(utilisateurRepository.save(utilisateur));
    }


    public Utilisateur findByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }


}
