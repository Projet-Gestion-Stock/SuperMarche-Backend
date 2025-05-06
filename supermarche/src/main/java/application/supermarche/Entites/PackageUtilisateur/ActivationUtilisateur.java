package application.supermarche.Entites.PackageUtilisateur;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activation_utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivationUtilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activationId;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "modifie_par_id", nullable = false)
    private Utilisateur modifiePar;

    @Column(name = "statut_avant", nullable = false)
    private String statutAvant;

    @Column(name = "statut_apres", nullable = false)
    private String statutApres;

    @Column(name = "date_modification", updatable = false)
    private LocalDateTime dateModification = LocalDateTime.now();
}
