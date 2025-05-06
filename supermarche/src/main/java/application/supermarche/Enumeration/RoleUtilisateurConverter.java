package application.supermarche.Enumeration;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = true)
public class RoleUtilisateurConverter implements AttributeConverter<RoleUtilisateur, String> {

    @Override
    public String convertToDatabaseColumn(RoleUtilisateur role) {
        return (role == null) ? null : role.toString();
    }

    @Override
    public RoleUtilisateur convertToEntityAttribute(String valeur) {
        return RoleUtilisateur.fromString(valeur);
    }
}
