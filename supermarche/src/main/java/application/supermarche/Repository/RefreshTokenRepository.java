package application.supermarche.Repository;

import application.supermarche.Entites.PackageJwt.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Integer> {

    List<RefreshToken> findByExpireTrueOrExpirationBefore(Date date);

    void deleteAll(Iterable<? extends RefreshToken> refreshTokens);
}
