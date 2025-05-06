package application.supermarche.Entites.SupermarcheInfo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SupermarcheInfo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupermarcheInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom; // Nom du supermarché

    private String logoUrl; // URL vers l’image (Cloudinary)

    private String localisation;

    private String telephone;

    // Getters et Setters
}
