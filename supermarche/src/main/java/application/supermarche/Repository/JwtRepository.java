package application.supermarche.Repository;

import application.supermarche.Entites.PackageJwt.Jwt;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.stream.Stream;

public interface JwtRepository extends CrudRepository<Jwt, Integer> {
    Optional<Jwt> findByValeur(String valeur);

    @Query("FROM Jwt j WHERE j.desactive = :desactive AND j.expire = :expire AND j.utilisateur.email = :email")
    Optional<Jwt> findUtilisateurValidToken(String email, boolean desactive, boolean expire);

    @Query("FROM Jwt j WHERE j.utilisateur.email = :email")
    Stream<Jwt> findUtilisateur(String email);

    @Query("FROM Jwt j WHERE j.refreshToken.valeur = :valeur")
    Optional<Jwt> findByRefreshToken(String valeur);

    void deleteAllByExpireAndDesactive(boolean expire, boolean desactive);

}
