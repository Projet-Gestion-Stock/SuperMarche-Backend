package application.supermarche.Repository;

import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupermarcheInfoRepository extends JpaRepository<SupermarcheInfo, Long> {
}
