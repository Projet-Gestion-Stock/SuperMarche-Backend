package application.supermarche.Securite;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class ConfigurationSecuriteApplication {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtFilter jwtFilter;

    public ConfigurationSecuriteApplication(BCryptPasswordEncoder bCryptPasswordEncoder, JwtFilter jwtFilter) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtFilter = jwtFilter;
    }

    // Configuration des droits d'accès

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return
                httpSecurity
                        .csrf(AbstractHttpConfigurer::disable)
                        .authorizeHttpRequests(
                                authorize ->
                                        authorize
                                                .requestMatchers("/user/connexion").permitAll()
                                                .requestMatchers("/user/refresh-token").permitAll()
                                                .requestMatchers("/user/deconnexion").permitAll()
                                                .requestMatchers("/user/gerant/**").hasAnyRole("GERANT", "ADMIN")
                                                .requestMatchers("/images/gerant/**").hasAnyRole("GERANT", "ADMIN")
                                                .requestMatchers("/produits/gerant/**").hasAnyRole("GERANT", "ADMIN")
                                                .requestMatchers("/stock/gerant/**").hasAnyRole("GERANT", "ADMIN")
                                                .requestMatchers("/statistiques/admin/**").hasRole("ADMIN")
                                                .requestMatchers("/statistiques/gerant/**").hasAnyRole("GERANT", "ADMIN")
                                                .requestMatchers("/ventes/staff/**").hasAnyRole("STAFF", "GERANT", "ADMIN")
                                                .requestMatchers("/supermarche/staff/**").hasAnyRole("STAFF", "GERANT", "ADMIN")
                                                .requestMatchers("/supermarche/admin/**").hasRole("ADMIN")

                        )
                        .sessionManagement(httpSecuritySessionManagementConfigurer ->
                                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        )
                        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                        .build();
    }


    // gestion de l'authentification des utilisateurs

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return  daoAuthenticationProvider;
    }
}
