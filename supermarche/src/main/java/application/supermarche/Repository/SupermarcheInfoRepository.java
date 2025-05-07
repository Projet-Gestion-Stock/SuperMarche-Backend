package application.supermarche.Repository;

import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupermarcheInfoRepository extends JpaRepository<SupermarcheInfo, Long> {
}