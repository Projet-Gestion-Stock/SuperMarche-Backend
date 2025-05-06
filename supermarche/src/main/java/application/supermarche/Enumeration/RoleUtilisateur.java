package application.supermarche.Enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum RoleUtilisateur {
    ADMIN("admin"),
    GERANT("gerant"),
    STAFF("staff");

    private final String valeur;

    RoleUtilisateur(String valeur) {
        this.valeur = valeur;
    }

    @JsonCreator
    public static RoleUtilisateur fromString(String valeur) {
        for (RoleUtilisateur role : RoleUtilisateur.values()) {
            if (role.valeur.equalsIgnoreCase(valeur)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Aucun rôle correspondant à : " + valeur);
    }

    @Override
    public String toString() {
        return this.valeur;
    }
}
