package application.supermarche.Controllers;


import application.supermarche.Services.PackageStatistique.StatistiquesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("statistiques")
public class StatistiquesController {

    private final StatistiquesService statistiquesService;

    public StatistiquesController(StatistiquesService statistiquesService) {
        this.statistiquesService = statistiquesService;
    }

    @GetMapping(path = "admin/ventes",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> statistiquesVentes() {
        Map<String, Object> stats = statistiquesService.statistiquesVentes();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }

    @GetMapping(path = "gerant/produits",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> produitsPlusVendus() {
        Map<String, Object> stats = statistiquesService.produitsPlusVendus();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}