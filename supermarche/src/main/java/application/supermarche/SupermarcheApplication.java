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

import application.supermarche.Repository.SupermarcheInfoRepository;
import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Enumeration.JourSemaine;
import java.util.EnumMap;
import java.util.Map;

@EnableScheduling
@SpringBootApplication
public class SupermarcheApplication {

	public static void main(String[] args) {

		SpringApplication.run(SupermarcheApplication.class, args);

	}

	@Bean
	CommandLineRunner initDatabase(UtilisateurRepository utilisateurRepository,
								   PasswordEncoder passwordEncoder,
								   SupermarcheInfoRepository supermarcheInfoRepository) {
		return args -> {
			// Création de l'utilisateur admin
			String adminEmail = "admin@supermarche.com";
			if (utilisateurRepository.findByEmail(adminEmail).isEmpty()) {
				Utilisateur admin = new Utilisateur();
				admin.setNom("Administrateur");
				admin.setEmail(adminEmail);
				admin.setMotDePasse(passwordEncoder.encode("123456"));
				admin.setRole(RoleUtilisateur.ADMIN);
				admin.setActif(true);
				admin.setSupprime(false);

				utilisateurRepository.save(admin);
				System.out.println("Utilisateur admin créé avec succès");
			}

			// Initialisation des informations du supermarché
			if (supermarcheInfoRepository.count() == 0) {
				SupermarcheInfo supermarcheInfo = new SupermarcheInfo();
				supermarcheInfo.setNom("Mon Supermarché");
				supermarcheInfo.setLogoUrl("https://res.cloudinary.com/dycdqyspf/image/upload/v1747385723/supermarche/logos/logo_1747385682044.png");
				supermarcheInfo.setLocalisation("123 Rue Principale, Ville");
				supermarcheInfo.setTelephone("+1234567890");
				supermarcheInfo.setDescription("Le meilleur supermarché de la ville");
				supermarcheInfo.setEmail("contact@supermarche.com");

				// Initialisation des horaires
				Map<JourSemaine, String> horaires = new EnumMap<>(JourSemaine.class);
				horaires.put(JourSemaine.LUNDI, "08:00-20:00");
				horaires.put(JourSemaine.MARDI, "08:00-20:00");
				horaires.put(JourSemaine.MERCREDI, "08:00-20:00");
				horaires.put(JourSemaine.JEUDI, "08:00-20:00");
				horaires.put(JourSemaine.VENDREDI, "08:00-21:00");
				horaires.put(JourSemaine.SAMEDI, "08:00-21:00");
				horaires.put(JourSemaine.DIMANCHE, "09:00-13:00");

				supermarcheInfo.setHorairesOuverture(horaires);

				supermarcheInfoRepository.save(supermarcheInfo);
				System.out.println("Informations du supermarché initialisées avec succès");
			}
		};
	}
}
