package application.supermarche.Entites.PackageJwt;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "refresh-token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "valeur")
    private String valeur;

    @Column(name = "expire")
    private boolean expire;

    @Column(name = "date-creation")
    private Date creation;

    @Column(name = "date-expiration")
    private Date expiration;


}
