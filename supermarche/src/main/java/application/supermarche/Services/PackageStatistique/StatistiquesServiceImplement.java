/*

package application.supermarche.Services.PackageStatistique;

import application.supermarche.Entites.PackageVente.Vente;
import application.supermarche.Repository.VenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatistiquesServiceImplement implements StatistiquesService{


    private final VenteRepository venteRepository;

    public StatistiquesServiceImplement(VenteRepository venteRepository) {
        this.venteRepository = venteRepository;
    }

    @Override
    public Map<String, Object> statistiquesVentes() {
        List<Vente> ventes = venteRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("chiffreAffaires", ventes.stream().mapToDouble(Vente::getMontantTotal).sum());
        stats.put("nombreVentes", ventes.size());
        return stats;
    }

    @Override
    public Map<String, Object> produitsPlusVendus() {
        List<Vente> ventes = venteRepository.findAll();
        Map<String, Integer> produitsVendus = new HashMap<>();
        ventes.forEach(vente -> vente.getProduit().forEach(produit ->
                produitsVendus.merge(produit.getProduit(), 1, Integer::sum)));

        List<Map.Entry<String, Integer>> topProduits = produitsVendus.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("produitsPlusVendus", topProduits);
        return stats;
    }
}
*/

package application.supermarche.Services.PackageStatistique;

import application.supermarche.Entites.PackageVente.Vente;
import application.supermarche.Entites.PackageVente.VenteProduit;
import application.supermarche.Repository.VenteProduitRepository;
import application.supermarche.Repository.VenteRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatistiquesServiceImplement implements StatistiquesService {

    private final VenteRepository venteRepository;
    private final VenteProduitRepository venteProduitRepository;

    public StatistiquesServiceImplement(VenteRepository venteRepository, VenteProduitRepository venteProduitRepository) {
        this.venteRepository = venteRepository;
        this.venteProduitRepository = venteProduitRepository;
    }

    @Override
    public Map<String, Object> statistiquesVentes() {
        List<Vente> ventes = venteRepository.findAll();
        Map<String, Object> stats = new HashMap<>();
        stats.put("chiffreAffaires", ventes.stream().mapToDouble(Vente::getMontantTotal).sum());
        stats.put("nombreVentes", ventes.size());
        return stats;
    }

    @Override
    public Map<String, Object> produitsPlusVendus() {
        List<VenteProduit> lignes = venteProduitRepository.findAll();

        Map<String, Integer> produitsVendus = new HashMap<>();

        for (VenteProduit vp : lignes) {
            String nomProduit = vp.getProduit().getProduit();
            produitsVendus.merge(nomProduit, vp.getQuantiteVendue(), Integer::sum);
        }

        List<Map.Entry<String, Integer>> topProduits = produitsVendus.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        Map<String, Object> stats = new HashMap<>();
        stats.put("produitsPlusVendus", topProduits);
        return stats;
    }
}
