package application.supermarche.Entites.PackageProduit;

import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "produit")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String produit; // nom du produit

    private double prix; // prix unitaire

    private String categorie; // ingrédient, etc. (peut être null)

    private LocalDate dateAjout = LocalDate.now();// date d'ajout

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate dateExpiration; // date d'expiration (peut être null)

    private String unite; // Kg, bidon, sachet, etc.

    private String fournisseur; // nom du fournisseur (peut être null)

    private String description; // description (peut être null)

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean actif = true;

    // Méthode de désactivation

    public void desactiver() {
        this.actif = false;
        this.dateExpiration = LocalDate.now(); // Optionnel : marquer comme expiré
    }

    public void setStock(Stock stock) {
    }
    @OneToOne(mappedBy = "produit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Stock stock;

    // Méthodes pour accéder aux infos du stock
    public int getQuantiteDisponible() {
        return this.stock != null ? this.stock.getQuantite() : 0;
    }

    public int getSeuilAlerte() {
        return this.stock != null ? this.stock.getSeuilAlerte() : 0;
    }

    // Méthodes d'état d'expiration
    public boolean estPerime() {
        return dateExpiration != null && dateExpiration.isBefore(LocalDate.now());
    }

    public boolean estPresquePerime(int joursAvantAlerte) {
        return dateExpiration != null
                && !estPerime()
                && dateExpiration.isBefore(LocalDate.now().plusDays(joursAvantAlerte));
    }

    public String getStatutExpiration(int joursAlerte) {
        if (estPerime()) return "PÉRIMÉ";
        if (estPresquePerime(joursAlerte)) return "PRESQUE_PÉRIMÉ";
        return "BON_ÉTAT";
    }
}
