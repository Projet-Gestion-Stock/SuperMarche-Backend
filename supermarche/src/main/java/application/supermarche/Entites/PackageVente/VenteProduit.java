package application.supermarche.Entites.PackageVente;

import application.supermarche.Entites.PackageProduit.Produit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vente_produit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private Vente vente;

    @ManyToOne
    private Produit produit;

    private int quantiteVendue;
    private double prixUnitaire; // Copié du produit au moment de la vente

}
