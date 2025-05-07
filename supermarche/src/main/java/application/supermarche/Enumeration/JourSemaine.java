package application.supermarche.Enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.DayOfWeek;
import java.util.Arrays;


public enum JourSemaine {
    LUNDI(DayOfWeek.MONDAY),
    MARDI(DayOfWeek.TUESDAY),
    MERCREDI(DayOfWeek.WEDNESDAY),
    JEUDI(DayOfWeek.THURSDAY),
    VENDREDI(DayOfWeek.FRIDAY),
    SAMEDI(DayOfWeek.SATURDAY),
    DIMANCHE(DayOfWeek.SUNDAY);

    private final DayOfWeek dayOfWeek;

    JourSemaine(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public DayOfWeek toDayOfWeek() {
        return dayOfWeek;
    }

    @JsonValue
    public String getValue() {
        return this.name();
    }

    public static JourSemaine fromDayOfWeek(DayOfWeek dayOfWeek) {
        return Arrays.stream(values())
                .filter(js -> js.dayOfWeek == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Jour invalide"));
    }

    @JsonCreator
    public static JourSemaine fromString(String value) {
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Jour invalide: " + value);
        }
    }
}
