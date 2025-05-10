
package application.supermarche.Services.PackageStatistique;

import application.supermarche.Enumeration.ErrorCode;
import application.supermarche.Entites.PackageVente.Vente;
import application.supermarche.Entites.PackageVente.VenteProduit;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Repository.VenteProduitRepository;
import application.supermarche.Repository.VenteRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StatistiquesServiceImplement implements StatistiquesService {

    private final VenteRepository venteRepository;
    private final VenteProduitRepository venteProduitRepository;

    public StatistiquesServiceImplement(VenteRepository venteRepository,
                                        VenteProduitRepository venteProduitRepository) {
        this.venteRepository = venteRepository;
        this.venteProduitRepository = venteProduitRepository;
    }

    @Override
    public Map<String, Object> statistiquesVentes() {
        try {
            log.debug("Calcul des statistiques de ventes");
            List<Vente> ventes = venteRepository.findAll();

            if (ventes.isEmpty()) {
                log.info("Aucune vente trouvée pour le calcul des statistiques");
                return Map.of(
                        "chiffreAffaires", 0.0,
                        "nombreVentes", 0
                );
            }

            double chiffreAffaires = ventes.stream()
                    .mapToDouble(Vente::getMontantTotal)
                    .sum();

            Map<String, Object> stats = new HashMap<>();
            stats.put("chiffreAffaires", arrondirMontant(chiffreAffaires));
            stats.put("nombreVentes", ventes.size());

            log.info("Statistiques calculées - CA: {}, Nb ventes: {}",
                    stats.get("chiffreAffaires"), stats.get("nombreVentes"));

            return stats;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des statistiques de ventes: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors du calcul des statistiques",
                    ErrorCode.STATS_CALCULATION_ERROR);
        }
    }


    public Map<String, Object> produitsPlusVendus(int limit) {
        try {
            log.debug("Recherche des produits les plus vendus");
            List<VenteProduit> lignes = venteProduitRepository.findAll();

            if (lignes.isEmpty()) {
                log.info("Aucune ligne de vente trouvée pour les statistiques produits");
                return Map.of("produitsPlusVendus", Collections.emptyList());
            }

            Map<String, Integer> produitsVendus = new HashMap<>();

            for (VenteProduit vp : lignes) {
                String nomProduit = vp.getProduit().getProduit();
                produitsVendus.merge(nomProduit, vp.getQuantiteVendue(), Integer::sum);
            }

            // Solution 1: Utilisation explicite de LinkedHashMap
            List<Map<String, Object>> topProduits = produitsVendus.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(limit)
                    .map(entry -> {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("produit", entry.getKey());
                        map.put("quantite", entry.getValue());
                        return map;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("produitsPlusVendus", topProduits);

            log.info("Top produits calculé - {} produits trouvés", topProduits.size());
            return stats;

        } catch (Exception e) {
            log.error("Erreur lors du calcul des produits plus vendus: {}", e.getMessage());
            throw new BusinessException(
                    "Erreur lors du calcul des produits plus vendus",
                    ErrorCode.PRODUCT_STATS_ERROR);
        }
    }

    private double arrondirMontant(double montant) {
        return Math.round(montant * 100.0) / 100.0;
    }
}
