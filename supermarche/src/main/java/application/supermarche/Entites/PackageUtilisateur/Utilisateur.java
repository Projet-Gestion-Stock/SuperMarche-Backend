package application.supermarche.Entites.PackageUtilisateur;

import application.supermarche.Enumeration.RoleUtilisateur;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "utilisateurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilisateur_id")
    private Long id;

    @Column(name = "nom_utilisateur", nullable = false)
    private String nom;

    @Column(name = "email_utilisateur", nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe_utilisateur", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_utilisateur", nullable = false)
    private RoleUtilisateur role;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "date_creation_utilisateur", updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public RoleUtilisateur getRole() {
        return role;
    }

    public void setRole(RoleUtilisateur role) {
        this.role = role;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    @Override
    public String getPassword() {
        return this.motDePasse; // Retourne le vrai mot de passe encodé
    }

    @Override
    public String getUsername() {
        return this.email; // Retourne l'email comme identifiant
    }

    @Override
    public boolean isEnabled() {
        return this.actif; // C'est cette méthode que Spring Security utilise pour vérifier l'état
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Ou implémentez une logique de verrouillage si nécessaire
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> role.name()); // Convertir l'enum en rôle utilisable
    }

}

