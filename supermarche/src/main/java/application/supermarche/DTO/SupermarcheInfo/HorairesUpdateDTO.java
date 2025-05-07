package application.supermarche.DTO.SupermarcheInfo;

import application.supermarche.Enumeration.JourSemaine;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record HorairesUpdateDTO(
        @NotNull
        Map<JourSemaine, String> horaires
) {
    public boolean isValid() {
        if (horaires == null) return false;

        return horaires.entrySet().stream().allMatch(entry -> {
            if (entry.getValue() == null) return true; // Permet de définir un jour comme fermé

            String[] parties = entry.getValue().split("-");
            if (parties.length != 2) return false;

            try {
                java.time.LocalTime.parse(parties[0]);
                java.time.LocalTime.parse(parties[1]);
                return true;
            } catch (java.time.format.DateTimeParseException e) {
                return false;
            }
        });
    }
}

