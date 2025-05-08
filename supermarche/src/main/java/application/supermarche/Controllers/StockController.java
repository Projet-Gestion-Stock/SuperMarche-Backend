package application.supermarche.Controllers;


import application.supermarche.DTO.PackageStock.StockAlerteDTO;
import application.supermarche.DTO.PackageStock.StockResponseDTO;
import application.supermarche.DTO.PackageStock.StockUpdateRequestDTO;
import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Mapper.StockMapper;
import application.supermarche.Services.PackageStock.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("stock")
public class StockController {


    private final StockService stockService;
    private final StockMapper stockMapper;

    public StockController(StockService stockService, StockMapper stockMapper) {
        this.stockService = stockService;
        this.stockMapper = stockMapper;
    }

    // Mettre a jour le stock d'un produit

    @PutMapping("gerant/mettreAJourStock/{produitId}")
    public ResponseEntity<StockResponseDTO> mettreAJourStock(
            @PathVariable Long produitId,
            @RequestBody StockUpdateRequestDTO request) {

        Stock stock = stockService.mettreAJourStock(produitId, request.quantiteAjoutee());

        return ResponseEntity.ok(
                new StockResponseDTO(
                        stock.getId(),
                        stockMapper.toProduitStockDTO(stock.getProduit()),
                        stock.getQuantite(),
                        stock.getSeuilAlerte()
                )
        );
    }

    // Afficher les produit qui ont un stock faible

    @GetMapping(path = "gerant/alerte",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StockAlerteDTO>> getAlertesStock() {
        List<Stock> stocks = stockService.listerStocksFaibles();
        List<StockAlerteDTO> dtos = stocks.stream()
                .map(stockMapper::toStockAlerteDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // Afficher les statistique

    @GetMapping(path = "gerant/statistiquesStock",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> statistiquesStock() {
        Map<String, Object> stats = stockService.statistiquesStock();
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}