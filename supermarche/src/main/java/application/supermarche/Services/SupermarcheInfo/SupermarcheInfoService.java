package application.supermarche.Services.SupermarcheInfo;

import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Repository.SupermarcheInfoRepository;
import org.springframework.stereotype.Service;

@Service
public class SupermarcheInfoService {

    private final SupermarcheInfoRepository repository;

    public SupermarcheInfoService(SupermarcheInfoRepository repository) {
        this.repository = repository;
    }

    public SupermarcheInfo getInfo() {
        return repository.findById(1L).orElseThrow(() -> new RuntimeException("Info manquante"));
    }

    public SupermarcheInfo updateInfo(SupermarcheInfo info) {
        info.setId(1L); // ID fixe si on ne veut qu'un seul enregistrement
        return repository.save(info);
    }
}
