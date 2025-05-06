package application.supermarche.DTO.PackageVente;

import application.supermarche.DTO.PackageProduit.ProduitVenduDTO;

import java.util.List;

public record VenteRequestDTO(
        double montantDonne,
        List<ProduitVenduDTO> produits,
        String methodePaiement
) {}

