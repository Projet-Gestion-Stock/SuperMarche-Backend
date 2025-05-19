package application.supermarche;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.Bean;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import application.supermarche.Enumeration.RoleUtilisateur;

@EnableScheduling
@SpringBootApplication
public class SupermarcheApplication {

	public static void main(String[] args) {

		SpringApplication.run(SupermarcheApplication.class, args);

	}

	@Bean
	CommandLineRunner initDatabase(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			// Vérifier si l'utilisateur admin existe déjà (par email)
			String adminEmail = "admin@supermarche.com";
			if (utilisateurRepository.findByEmail(adminEmail).isEmpty()) {
				Utilisateur admin = new Utilisateur();
				admin.setNom("Administrateur");
				admin.setEmail(adminEmail);
				admin.setMotDePasse(passwordEncoder.encode("Admin123!")); // Mot de passe sécurisé et hashé
				admin.setRole(RoleUtilisateur.ADMIN);
				admin.setActif(true);
				admin.setSupprime(false);

				utilisateurRepository.save(admin);
				System.out.println("Utilisateur admin créé avec succès");
			}
		};
	}
}
