package application.supermarche.Mapper;

import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import org.springframework.stereotype.Component;


@Component
public class UtilisateurMapper {

    public UtilisateurDTO toDTO(Utilisateur utilisateur) {
        return new UtilisateurDTO(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getRole().name()
        );
    }

    public Utilisateur toEntity(UtilisateurDTO dto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(dto.id());  // Accès aux champs avec dto.id(), pas dto.getId()
        utilisateur.setNom(dto.nom());
        utilisateur.setEmail(dto.email());
        return utilisateur;
    }
}
