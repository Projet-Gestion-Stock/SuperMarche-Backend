package application.supermarche.Mapper;

import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;
import application.supermarche.DTO.PackageVente.VenteProduitResponseDTO;
import application.supermarche.DTO.PackageVente.VenteResponseDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Entites.PackageVente.Vente;
import application.supermarche.Entites.PackageVente.VenteProduit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class VenteMapper {

    public VenteResponseDTO toResponseDto(Vente vente) {
        return new VenteResponseDTO(
                vente.getId(),
                toUtilisateurDto(vente.getUtilisateur()),
                vente.getMontantTotal(),
                vente.getMontantDonne(),
                vente.getMonnaieRendue(),
                vente.getMethodePaiement(),
                vente.getDateVente(),
                vente.getNumeroRecu(),
                Optional.ofNullable(vente.getVenteProduits())
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(this::toProduitResponseDto)
                        .toList()
        );
    }

    private UtilisateurDTO toUtilisateurDto(Utilisateur utilisateur) {
        return new UtilisateurDTO(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getEmail(),
                utilisateur.getRole().name()
        );
    }

    private VenteProduitResponseDTO toProduitResponseDto(VenteProduit venteProduit) {
        return new VenteProduitResponseDTO(
                venteProduit.getProduit().getId(),
                venteProduit.getProduit().getProduit(), // Adaptez selon le nom réel du champ
                venteProduit.getQuantiteVendue(),
                venteProduit.getPrixUnitaire()
        );
    }
}
