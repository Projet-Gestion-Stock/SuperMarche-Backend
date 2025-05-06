package application.supermarche.Services.PackageProduit;

import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Repository.ProduitRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpirationProduitService {
    private final ProduitRepository produitRepository;

    public ExpirationProduitService(ProduitRepository produitRepository) {
        this.produitRepository = produitRepository;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Tous les jours à minuit
    @Transactional
    public void desactiverProduitsPerimes() {
        List<Produit> produitsPerimes = produitRepository
                .findByDateExpirationBeforeAndActifTrue(LocalDate.now());

        produitsPerimes.forEach(produit -> {
            produit.desactiver();
            produitRepository.save(produit);
        });
    }
}
