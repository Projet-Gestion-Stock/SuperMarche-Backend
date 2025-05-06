package application.supermarche.Entites.PackageVente;

import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Utilisateur utilisateur;

    private double montantTotal;
    private double montantDonne;

    private double monnaieRendue;

    private String methodePaiement;

    private LocalDateTime dateVente = LocalDateTime.now();

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL)
    private List<VenteProduit> venteProduits = new ArrayList<>();

    @Column(unique = true) // Garantit l'unicité en base
    private String numeroRecu;

    public void setProduits(List<VenteProduit> venteProduits) {
    }

    public void setMontantRendue(double v) {
        this.monnaieRendue = v;
    }
}
