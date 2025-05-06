package application.supermarche.DTO.PackageVente;

import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;

import java.time.LocalDateTime;
import java.util.List;

public record VenteDTO(
        Long id,
        String numeroRecu,
        LocalDateTime dateVente,
        double montantTotal,
        String methodePaiement,
        UtilisateurDTO utilisateur,
        List<VenteProduitDTO> produits
) {}