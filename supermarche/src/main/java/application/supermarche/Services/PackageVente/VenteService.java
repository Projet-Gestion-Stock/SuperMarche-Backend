package application.supermarche.Services.PackageVente;

import application.supermarche.DTO.PackageVente.VenteRequestDTO;
import application.supermarche.DTO.PackageVente.VenteResponseDTO;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Entites.PackageVente.Vente;

import java.io.IOException;
import java.util.List;

public interface VenteService {

    Vente enregistrerVente(VenteRequestDTO venteRequest, Utilisateur utilisateur);

    List<VenteResponseDTO> listerVentes(); // Type de retour List<VenteDto>

    VenteResponseDTO recupererVente(Long id);

    String genererRecu(Long id);

    byte[] genererRecuPDF(Long id) throws IOException;


}
