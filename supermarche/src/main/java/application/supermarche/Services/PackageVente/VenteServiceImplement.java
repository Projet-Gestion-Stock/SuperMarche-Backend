package application.supermarche.Services.PackageVente;

import application.supermarche.DTO.PackageProduit.ProduitVenduDTO;
import application.supermarche.DTO.PackageUtilisateur.UtilisateurDTO;
import application.supermarche.DTO.PackageVente.VenteProduitResponseDTO;
import application.supermarche.DTO.PackageVente.VenteRequestDTO;
import application.supermarche.DTO.PackageVente.VenteResponseDTO;
import application.supermarche.Entites.PackageProduit.Produit;
import application.supermarche.Entites.PackageStock.Stock;
import application.supermarche.Entites.PackageUtilisateur.Utilisateur;
import application.supermarche.Entites.PackageVente.Vente;
import application.supermarche.Entites.PackageVente.VenteProduit;
import application.supermarche.Entites.SupermarcheInfo.SupermarcheInfo;
import application.supermarche.Exceptions.RessourceNotFoundException;
import application.supermarche.Mapper.VenteMapper;
import application.supermarche.Repository.ProduitRepository;
import application.supermarche.Repository.StockRepository;
import application.supermarche.Repository.VenteProduitRepository;
import application.supermarche.Repository.VenteRepository;
import application.supermarche.Services.PackageStock.StockService;
import application.supermarche.Services.SupermarcheInfo.SupermarcheInfoService;
import jakarta.transaction.Transactional;
import org.apache.pdfbox.pdmodel.font.*;
import org.springframework.stereotype.Service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.text.Normalizer;

import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.*;

@Service
public class VenteServiceImplement implements VenteService {

    private final VenteRepository venteRepository;
    private final VenteProduitRepository venteProduitRepository;
    private final ProduitRepository produitRepository;
    private final StockRepository stockRepository;
    private final StockService stockService;
    private final SupermarcheInfoService supermarcheInfoService; // Injecté ici
    private final VenteMapper venteMapper;


    public VenteServiceImplement(VenteRepository venteRepository,
                                 VenteProduitRepository venteProduitRepository,
                                 ProduitRepository produitRepository,
                                 StockRepository stockRepository,
                                 StockService stockService, SupermarcheInfoService supermarcheInfoService, VenteMapper venteMapper) {
        this.venteRepository = venteRepository;
        this.venteProduitRepository = venteProduitRepository;
        this.produitRepository = produitRepository;
        this.stockRepository = stockRepository;
        this.stockService = stockService;
        this.supermarcheInfoService = supermarcheInfoService;
        this.venteMapper = venteMapper;
    }

    @Override
    @Transactional
    public Vente enregistrerVente(VenteRequestDTO venteRequest, Utilisateur utilisateur) {
        // 1. Validation initiale
        if (venteRequest.produits() == null || venteRequest.produits().isEmpty()) {
            throw new IllegalArgumentException("La vente doit contenir au moins un produit");
        }

        // 2. Initialisation de la vente
        Vente vente = new Vente();
        vente.setUtilisateur(utilisateur);
        vente.setDateVente(LocalDateTime.now());
        vente.setNumeroRecu(genererNumeroRecu()); // Utilisation de votre méthode actuelle
        vente.setMethodePaiement(venteRequest.methodePaiement());

        // 3. Traitement des produits
        List<VenteProduit> produitsVendus = new ArrayList<>();
        double montantTotal = 0;

        for (ProduitVenduDTO p : venteRequest.produits()) {
            Produit produit = produitRepository.findById(p.produitId())
                    .orElseThrow(() -> new RessourceNotFoundException("Produit introuvable avec ID : " + p.produitId()));

            Stock stock = stockRepository.findByProduitId(p.produitId())
                    .orElseThrow(() -> new RessourceNotFoundException("Stock introuvable pour le produit ID : " + p.produitId()));

            if (stock.getQuantite() < p.quantite()) {
                throw new IllegalArgumentException(
                        String.format("Stock insuffisant pour %s. Disponible: %d, Demandé: %d",
                                produit.getProduit(),
                                stock.getQuantite(),
                                p.quantite())
                );
            }

            double sousTotal = produit.getPrix() * p.quantite();
            montantTotal += sousTotal;

            VenteProduit vp = new VenteProduit();
            vp.setProduit(produit);
            vp.setVente(vente);
            vp.setQuantiteVendue(p.quantite());
            vp.setPrixUnitaire(produit.getPrix());

            produitsVendus.add(vp);
        }

        // 4. Validation du paiement
        if (venteRequest.montantDonne() < montantTotal) {
            throw new IllegalArgumentException(
                    String.format("Montant insuffisant. Total: %.2f, Donné: %.2f, Manquant: %.2f",
                            montantTotal,
                            venteRequest.montantDonne(),
                            montantTotal - venteRequest.montantDonne())
            );
        }

        // 5. Finalisation de la vente
        vente.setMontantTotal(montantTotal);
        vente.setMontantDonne(venteRequest.montantDonne());
        vente.setMonnaieRendue(venteRequest.montantDonne() - montantTotal);

        // 6. Sauvegarde en cascade
        Vente venteSauvegardee = venteRepository.save(vente);

        // 7. Sauvegarde des produits vendus et mise à jour des stocks
        produitsVendus.forEach(vp -> {
            vp.setVente(venteSauvegardee);
            venteProduitRepository.save(vp);

            // Version corrigée avec gestion d'Optional
            Stock stock = stockRepository.findByProduitId(vp.getProduit().getId())
                    .orElseThrow(() -> new RessourceNotFoundException(
                            "Stock introuvable pour le produit ID: " + vp.getProduit().getId()));

            int nouvelleQuantite = stock.getQuantite() - vp.getQuantiteVendue();
            if (nouvelleQuantite < 0) {
                throw new IllegalStateException(
                        "Quantité en stock insuffisante après vente pour le produit: "
                                + vp.getProduit().getProduit());
            }

            stock.setQuantite(nouvelleQuantite);
            stockRepository.save(stock);
        });
       // return venteSauvegardee;

        return venteRepository.findById(venteSauvegardee.getId())
                .orElseThrow(() -> new RessourceNotFoundException("Vente non trouvée après création"));
    }

    @Override
    public List<VenteResponseDTO> listerVentes() {
        return venteRepository.findAll().stream()
                .map(vente -> {
                    List<VenteProduitResponseDTO> produits = Optional.ofNullable(vente.getVenteProduits())
                            .orElse(Collections.emptyList())
                            .stream()
                            .map(vp -> new VenteProduitResponseDTO(
                                    vp.getProduit().getId(),
                                    vp.getProduit().getProduit(), // Assurez-vous que c'est le bon champ
                                    vp.getQuantiteVendue(),
                                    vp.getPrixUnitaire()
                            ))
                            .toList();

                    return new VenteResponseDTO(
                            vente.getId(),
                            new UtilisateurDTO(
                                    vente.getUtilisateur().getId(),
                                    vente.getUtilisateur().getNom(),
                                    vente.getUtilisateur().getEmail(),
                                    vente.getUtilisateur().getRole().name()
                            ),
                            vente.getMontantTotal(),
                            vente.getMontantDonne(),
                            vente.getMonnaieRendue(),
                            vente.getMethodePaiement(),
                            vente.getDateVente(),
                            vente.getNumeroRecu(),
                            produits
                    );
                })
                .toList();
    }

    @Override
    public VenteResponseDTO recupererVente(Long id) {
        Vente vente = venteRepository.findById(id)
                .orElseThrow(() -> new RessourceNotFoundException("Vente avec l'ID " + id + " non trouvée"));

        List<VenteProduitResponseDTO> produits = Optional.ofNullable(vente.getVenteProduits())
                .orElse(Collections.emptyList())
                .stream()
                .map(vp -> new VenteProduitResponseDTO(
                        vp.getProduit().getId(),
                        vp.getProduit().getProduit(),
                        vp.getQuantiteVendue(),
                        vp.getPrixUnitaire()
                ))
                .toList();

        return new VenteResponseDTO(
                vente.getId(),
                new UtilisateurDTO(
                        vente.getUtilisateur().getId(),
                        vente.getUtilisateur().getNom(),
                        vente.getUtilisateur().getEmail(),
                        vente.getUtilisateur().getRole().name()
                ),
                vente.getMontantTotal(),
                vente.getMontantDonne(),
                vente.getMonnaieRendue(),
                vente.getMethodePaiement(),
                vente.getDateVente(),
                vente.getNumeroRecu(),
                produits
        );
    }


    // methode de generation de numero reçu aléatoire

    private String genererNumeroRecu() {
        String numero;
        do {
            String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder sb = new StringBuilder("#");
            Random random = new Random();
            for (int i = 0; i < 8; i++) {
                sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
            }
            numero = sb.toString();
        } while (venteRepository.existsByNumeroRecu(numero)); // Vérifie en base
        return numero;
    }

    @Override
    public String genererRecu(Long id) {
        // Récupération des données
        VenteResponseDTO venteResponse = recupererVente(id);
        List<VenteProduit> produitsVendus = venteProduitRepository.findByVenteId(id);

        // Formatage de la date
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // Construction du reçu
        StringBuilder recu = new StringBuilder();

        // En-tête
        recu.append("========================================\n");
        recu.append(String.format("%-20s %s\n", "Reçu n°:", venteResponse.numeroRecu()));
        recu.append(String.format("%-20s %s\n", "Date d'impression:",
                LocalDateTime.now().format(dateFormatter)));
        recu.append(String.format("%-20s %s\n", "Date de vente:",
                venteResponse.dateVente().format(dateFormatter)));
        recu.append("========================================\n\n");

        // Détails des produits
        recu.append("DÉTAIL DES PRODUITS\n");
        recu.append("----------------------------------------\n");
        recu.append(String.format("%-20s %-8s %-12s %-12s\n",
                "Produit", "Qté", "P.U (FCFA)", "Total (FCFA)"));
        recu.append("----------------------------------------\n");

        for (VenteProduit vp : produitsVendus) {
            recu.append(String.format("%-20s %-8d %-12.2f %-12.2f\n",
                    vp.getProduit().getProduit(),
                    vp.getQuantiteVendue(),
                    vp.getPrixUnitaire(),
                    vp.getPrixUnitaire() * vp.getQuantiteVendue()));
        }

        // Paiement
        recu.append("\nDÉTAIL DU PAIEMENT\n");
        recu.append("----------------------------------------\n");
        recu.append(String.format("%-20s %,.2f FCFA\n", "Montant total:", venteResponse.montantTotal()));
        recu.append(String.format("%-20s %,.2f FCFA\n", "Montant donné:", venteResponse.montantDonne()));
        recu.append(String.format("%-20s %,.2f FCFA\n", "Monnaie rendue:", venteResponse.monnaieRendue()));
        recu.append(String.format("%-20s %s\n", "Méthode de paiement:", venteResponse.methodePaiement()));
        recu.append("----------------------------------------\n");

        // Pied de page
        recu.append("\nServi par: ").append(venteResponse.utilisateur().nom()).append("\n");
        recu.append("Merci pour votre confiance et à bientôt !\n");
        recu.append("========================================");

        return recu.toString();
    }

    // methode generation de  recu pdf

    public byte[] genererRecuPDF(Long id) throws IOException {
        if (id == null) throw new IllegalArgumentException("L'ID de vente ne peut pas être null");

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            VenteResponseDTO venteResponse = recupererVente(id);
            List<VenteProduit> produits = venteProduitRepository.findByVenteId(id);
            SupermarcheInfo info = supermarcheInfoService.getInfo();

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                float margin = 50;
                float yPosition = 770;

                // === 1. Logo ===
                if (info.getLogoUrl() != null && !info.getLogoUrl().isEmpty()) {
                    try (InputStream in = new URL(info.getLogoUrl()).openStream()) {
                        byte[] imageBytes = in.readAllBytes();
                        PDImageXObject logo = PDImageXObject.createFromByteArray(document, imageBytes, "logo");
                        float imageWidth = 170;
                        float imageHeight = 170 * logo.getHeight() / logo.getWidth();
                        content.drawImage(logo, (PDRectangle.A4.getWidth() - imageWidth) / 2, yPosition - imageHeight, imageWidth, imageHeight);
                        yPosition -= (imageHeight + 20);
                    } catch (Exception e) {
                        System.err.println("Erreur de chargement du logo : " + e.getMessage());
                    }
                }

                yPosition -= 15;

                // === 2. Titre principal ===
                writeCenteredText(content, cleanText(info.getNom()), page.getMediaBox().getWidth(), yPosition, fontBold, 16, Color.BLACK);
                yPosition -= 25;

                // === 3. Numéro de commande et date ===
                writeCenteredText(content, cleanText("Commande " + venteResponse.numeroRecu()), page.getMediaBox().getWidth(), yPosition, fontRegular, 12, Color.BLACK);
                yPosition -= 20;

                String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm:ss"));
                writeCenteredText(content, cleanText(dateStr), page.getMediaBox().getWidth(), yPosition, fontRegular, 10, Color.GRAY);
                yPosition -= 30;

                // === 4. Tableau ===
                float[] columnWidths = {250, 80, 100, 100};
                String[] headers = {"Article", "Qté", "Prix", "Total"};
                drawTable(content, margin, yPosition, headers, columnWidths, fontBold);
                yPosition -= 20;

                for (VenteProduit vp : produits) {
                    String[] row = {
                            cleanText(vp.getProduit().getProduit()),
                            cleanText(String.valueOf(vp.getQuantiteVendue())),
                            cleanText(formatMoney(vp.getPrixUnitaire())),
                            cleanText(formatMoney(vp.getPrixUnitaire() * vp.getQuantiteVendue()))
                    };
                    yPosition -= 15;
                    drawTableRow(content, margin, yPosition, row, columnWidths, fontRegular,12);
                    yPosition -= 25;
                }
                yPosition -= 18;

                // === 6. Total ===
                content.setNonStrokingColor(Color.BLACK); // Assure que la couleur est noire
                drawTableRow(content, margin, yPosition,
                        new String[]{"Total", "", "", cleanText(formatMoney(venteResponse.montantTotal()))},
                        columnWidths, fontBold,15);
                yPosition -= 40;

                // === 7. Détails de paiement ===
                writeLeftText(content, "DETAIL DU PAIEMENT", margin, yPosition, fontBold, 12, Color.BLACK);
                yPosition -= 20;

                drawLine(content, margin, yPosition, page.getMediaBox().getWidth() - 2 * margin, 0.5f, Color.BLACK);
                yPosition -= 20;

                writeLeftText(content, cleanText(String.format("Montant total: %,.2f FCFA", venteResponse.montantTotal())), margin, yPosition, fontRegular, 10, Color.BLACK);
                yPosition -= 15;

                writeLeftText(content, cleanText(String.format("Montant donne: %,.2f FCFA", venteResponse.montantDonne())), margin, yPosition, fontRegular, 10, Color.BLACK);
                yPosition -= 15;

                writeLeftText(content, cleanText(String.format("Monnaie rendue: %,.2f FCFA", venteResponse.monnaieRendue())), margin, yPosition, fontRegular, 10, Color.BLACK);
                yPosition -= 15;

                writeLeftText(content, cleanText(String.format("Methode de paiement: %s", venteResponse.methodePaiement())), margin, yPosition, fontRegular, 10, Color.BLACK);
                yPosition -= 15;

                drawLine(content, margin, yPosition, page.getMediaBox().getWidth() - 2 * margin, 0.5f, Color.BLACK);
                yPosition -= 30;

                // === 8. Pied de page ===
                writeCenteredText(content, cleanText("Servi par: " + venteResponse.utilisateur().nom()), page.getMediaBox().getWidth(), yPosition, fontRegular, 10, Color.BLACK);
                yPosition -= 15;

                writeCenteredText(content, cleanText("Merci pour votre confiance et a bientot !"), page.getMediaBox().getWidth(), yPosition, fontRegular, 10, Color.BLACK);
                yPosition -= 15;

                drawLine(content, margin, yPosition, page.getMediaBox().getWidth() - 2 * margin, 0.5f, Color.BLACK);
                yPosition -= 20;

                writeCenteredText(content, cleanText(info.getLocalisation()), page.getMediaBox().getWidth(), yPosition, fontRegular, 10, Color.BLACK);
                yPosition -= 15;

                writeCenteredText(content, cleanText(info.getTelephone()), page.getMediaBox().getWidth(), yPosition, fontRegular, 10, Color.BLACK);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IOException("Erreur de generation du PDF: " + e.getMessage(), e);
        }
    }


    // Méthode pour écrire du texte aligné à gauche

    private void writeLeftText(PDPageContentStream content, String text, float x, float y, PDFont font, float fontSize, Color color) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.setNonStrokingColor(color);
        content.newLineAtOffset(x, y);
        content.showText(cleanText(text));
        content.endText();
    }

    // Méthode de nettoyage des textes

    private String cleanText(String input) {
        if (input == null) return "";
        return Normalizer.normalize(input, Normalizer.Form.NFKD)
                .replace('\u202F', ' ')    // espace fine insécable
                .replace('\u00A0', ' ')    // espace insécable classique
                .replace('\u2019', '\'')   // apostrophe typographique
                .replaceAll("[^\\p{ASCII}]", ""); // supprime tout caractère non ASCII
    }

    // Méthode sécurisée pour écrire du texte

    private void writeSafeText(PDPageContentStream content, String text,
                               float x, float y, PDType1Font font,
                               float fontSize, Color color) throws IOException {
        content.beginText();
        try {
            content.setFont(font, fontSize);
            content.setNonStrokingColor(color);
            content.newLineAtOffset(x, y);
            content.showText(text);
        } finally {
            content.endText();
        }
    }

    // Méthodes pour le tableau
    private void drawTable(PDPageContentStream content, float x, float y,
                           String[] headers, float[] columnWidths, PDType1Font font) throws IOException {
        float currentX = x;
        for (int i = 0; i < headers.length; i++) {
            writeSafeText(content, headers[i], currentX, y, font, 10, Color.BLACK);
            currentX += columnWidths[i];
        }
        drawLine(content, x, y - 5, calculateTotalWidth(columnWidths), 1, Color.LIGHT_GRAY);
    }

    private void drawTableRow(PDPageContentStream content, float x, float y,
                              String[] values, float[] columnWidths,
                              PDType1Font font, float fontSize) throws IOException {
        float currentX = x;
        content.setFont(font, fontSize);
        for (int i = 0; i < values.length; i++) {
            writeSafeText(content, values[i], currentX, y, font, fontSize, Color.BLACK);
            currentX += columnWidths[i];
        }
    }

    private String formatMoney(double amount) {
        String money = String.format(java.util.Locale.US, "%,.0f FCFA", amount); // force US locale
        return cleanText(money); // nettoie les caractères spéciaux (notamment U+202F)
    }


    private float calculateTotalWidth(float[] columnWidths) {
        float total = 0;
        for (float width : columnWidths) {
            total += width;
        }
        return total;
    }

    private void drawLine(PDPageContentStream content, float x, float y,
                          float length, float thickness, Color color) throws IOException {
        content.setStrokingColor(color);
        content.setLineWidth(thickness);
        content.moveTo(x, y);
        content.lineTo(x + length, y);
        content.stroke();
    }

    private void writeCenteredText(PDPageContentStream content, String text,
                                   float pageWidth, float y, PDType1Font font,
                                   float fontSize, Color color) throws IOException {
        content.beginText();
        try {
            content.setFont(font, fontSize);
            content.setNonStrokingColor(color);
            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
            float x = (pageWidth - textWidth) / 2;
            content.newLineAtOffset(x, y);
            content.showText(cleanText(text));
        } finally {
            content.endText();
        }
    }
}