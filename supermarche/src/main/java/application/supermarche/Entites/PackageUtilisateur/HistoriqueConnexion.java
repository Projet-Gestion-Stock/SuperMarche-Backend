package application.supermarche.Entites.PackageUtilisateur;


import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "historique_connexion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueConnexion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email")
    private String email;

    @Column(name = "action")
    private String action; // "CONNEXION", "REFRESH", "DECONNEXION", etc.

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "timestamp")
    private Date timestamp;

    @ManyToOne
    private Utilisateur utilisateur;
}
