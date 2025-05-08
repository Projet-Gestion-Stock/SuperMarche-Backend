package application.supermarche.Enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public enum RoleUtilisateur {
    ADMIN("ADMIN"),  // Notez le préfixe ROLE_
    GERANT("GERANT"),
    STAFF("STAFF");

    private final String authority;

    RoleUtilisateur(String authority) {
        this.authority = authority;
    }

    @JsonCreator
    public static RoleUtilisateur fromString(String value) {
        for (RoleUtilisateur role : RoleUtilisateur.values()) {
            if (role.authority.equalsIgnoreCase(value) ||
                    role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Aucun rôle correspondant à : " + value);
    }
}
