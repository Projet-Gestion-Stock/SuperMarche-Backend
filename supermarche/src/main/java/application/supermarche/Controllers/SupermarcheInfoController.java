package application.supermarche.Controllers;

import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Services.SupermarcheInfo.SupermarcheInfoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("supermarche")
public class SupermarcheInfoController {

    private final SupermarcheInfoService service;

    public SupermarcheInfoController(SupermarcheInfoService service) {
        this.service = service;
    }

    @GetMapping
    public SupermarcheInfo getInfo() {
        return service.getInfo();
    }

    @PostMapping
    public SupermarcheInfo updateInfo(@RequestBody SupermarcheInfo info) {
        return service.updateInfo(info);
    }
}
