package application.supermarche.Entites.PackageJwt;

import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "jwt")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Jwt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "valeur")
    private String valeur;

    @Column(name = "desactive")
    private boolean desactive;

    @Column(name = "expire")
    private boolean expire;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private RefreshToken refreshToken;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE})
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

}
