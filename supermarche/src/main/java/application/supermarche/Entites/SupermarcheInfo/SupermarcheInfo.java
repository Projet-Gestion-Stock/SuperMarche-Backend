package application.supermarche.Entites.SupermarcheInfo;

import application.supermarche.Enumeration.JourSemaine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Cascade;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

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


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "supermarche_horaires",
            joinColumns = @JoinColumn(name = "supermarche_id"))
    @MapKeyColumn(name = "jour_semaine")
    @Column(name = "horaires")
    @MapKeyEnumerated(EnumType.STRING)
    private Map<JourSemaine, String> horairesOuverture;

    public LocalTime getHeureOuverture(DayOfWeek jour) {
        return getHeureOuverture(JourSemaine.fromDayOfWeek(jour));
    }

    public LocalTime getHeureOuverture(JourSemaine jour) {
        String horaires = horairesOuverture.get(jour);
        if (horaires == null || horaires.isEmpty()) return null;
        return java.time.LocalTime.parse(horaires.split("-")[0]);
    }

    public LocalTime getHeureFermeture(DayOfWeek jour) {
        return getHeureFermeture(JourSemaine.fromDayOfWeek(jour));
    }

    public LocalTime getHeureFermeture(JourSemaine jour) {
        String horaires = horairesOuverture.get(jour);
        if (horaires == null || horaires.isEmpty()) return null;
        return java.time.LocalTime.parse(horaires.split("-")[1]);
    }

}
