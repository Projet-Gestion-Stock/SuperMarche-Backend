package application.supermarche.Services.PackageProduit;

import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Repository.ProduitRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExpirationProduitService {

    private final ProduitRepository produitRepository;

    public ExpirationProduitService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Tous les jours à minuit
    @Transactional
    public void traiterProduitsPerimes() {
        List<Produit> produitsPerimes = produitRepository
                .findByDateExpirationBefore(LocalDate.now());

        if (produitsPerimes.isEmpty()) return;

        produitsPerimes.forEach(produit -> {
            produit.setPerime(true);
            produitRepository.save(produit);
        });

        String noms = produitsPerimes.stream()
                .map(Produit::getProduit)
                .collect(Collectors.joining(", "));
        log.info("Produits périmés détectés et marqués : {}", noms);
    }


}
