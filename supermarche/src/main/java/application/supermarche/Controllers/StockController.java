package application.supermarche.Controllers;


import application.supermarche.DTO.PackageStock.StockAlerteDTO;
import application.supermarche.DTO.PackageStock.StockResponseDTO;
import application.supermarche.DTO.PackageStock.StockUpdateRequestDTO;
import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Exceptions.ApiException;
import application.supermarche.Exceptions.BusinessException;
import application.supermarche.Exceptions.ResourceNotFoundException;
import application.supermarche.Mapper.StockMapper;
import application.supermarche.Services.PackageStock.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("stock")
public class StockController {

    private final StockService stockService;
    private final StockMapper stockMapper;

    public StockController(StockService stockService, StockMapper stockMapper) {
        this.stockService = stockService;
        this.stockMapper = stockMapper;
    }

    @PutMapping("gerant/mettreAJourStock/{produitId}")
    public ResponseEntity<StockResponseDTO> mettreAJourStock(
            @PathVariable Long produitId,
            @RequestBody StockUpdateRequestDTO request) {

        try {
            log.info("Mise à jour stock demandée - Produit ID: {}, Quantité: {}", produitId, request.quantiteAjoutee());

            Stock stock = stockService.mettreAJourStock(produitId, request.quantiteAjoutee());

            StockResponseDTO response = new StockResponseDTO(
                    stock.getId(),
                    stockMapper.toProduitStockDTO(stock.getProduit()),
                    stock.getQuantite(),
                    stock.getSeuilAlerte()
            );

            log.info("Mise à jour stock réussie - Produit ID: {}, Nouvelle quantité: {}", produitId, stock.getQuantite());
            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            log.warn("Stock non trouvé - Produit ID: {}", produitId);
            throw new ApiException(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BusinessException e) {
            log.warn("Erreur métier lors de la mise à jour: {}", e.getMessage());
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erreur technique lors de la mise à jour du stock: {}", e.getMessage());
            throw new ApiException("Erreur technique lors de la mise à jour du stock", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("gerant/alerte")
    public ResponseEntity<List<StockAlerteDTO>> getAlertesStock() {
        try {
            log.info("Demande de liste des stocks en alerte");

            List<Stock> stocks = stockService.listerStocksFaibles();
            List<StockAlerteDTO> dtos = stocks.stream()
                    .map(stockMapper::toStockAlerteDTO)
                    .toList();

            log.info("Retour de {} produits en alerte de stock", dtos.size());
            return ResponseEntity.ok(dtos);

        } catch (BusinessException e) {
            log.warn("Erreur métier lors de la récupération des alertes: {}", e.getMessage());
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erreur technique lors de la récupération des alertes: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("gerant/statistiquesStock")
    public ResponseEntity<Map<String, Object>> statistiquesStock() {
        try {
            log.info("Demande de statistiques de stock");

            Map<String, Object> stats = stockService.statistiquesStock();

            log.info("Statistiques générées avec succès");
            return new ResponseEntity<>(stats, HttpStatus.OK);

        } catch (BusinessException e) {
            log.warn("Erreur métier lors du calcul des stats: {}", e.getMessage());
            throw new ApiException(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Erreur technique lors du calcul des stats: {}", e.getMessage());
            throw new ApiException("Erreur technique", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
