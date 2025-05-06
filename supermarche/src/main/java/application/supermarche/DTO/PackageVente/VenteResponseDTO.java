package application.supermarche.DTO.PackageVente;

import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;

import java.time.LocalDateTime;
import java.util.List;

public record VenteResponseDTO(
        Long id,
        UtilisateurDTO utilisateur,
        double montantTotal,
        double montantDonne,
        double monnaieRendue,
        String methodePaiement,
        LocalDateTime dateVente,
        String numeroRecu,
        List<VenteProduitResponseDTO> produits
) {}
