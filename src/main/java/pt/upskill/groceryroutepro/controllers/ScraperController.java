package pt.upskill.groceryroutepro.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pt.upskill.groceryroutepro.models.ScraperParams;

import pt.upskill.groceryroutepro.services.ScraperService;


@RestController
@Component
@RequestMapping("/scraper")
public class ScraperController {

    @Autowired
    ScraperService scraperService;

    @PostMapping("/continente")
    public ResponseEntity<String> scrapeContinente(@RequestBody ScraperParams scraperParams) {
        try {
            this.scraperService.scrapeContinente(scraperParams.getUrl(), scraperParams.getCategory());
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/continente/all")
    public ResponseEntity<String> scrapeContinenteAll() {
        try {
            this.scraperService.scrapeContinenteAll();
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/auchan")
    public ResponseEntity<String> scrapeAuchan(@RequestBody ScraperParams scraperParams) {
        try {
            this.scraperService.scrapeAuchan(scraperParams.getUrl(), scraperParams.getCategory());
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/auchan/all")
    public ResponseEntity<String> scrapeAuchanAll() {
        try {
            this.scraperService.scrapeAuchanAll();
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/minipreco")
    public ResponseEntity<String> scrapeMinipreco(@RequestBody ScraperParams scraperParams) {
        try {
            this.scraperService.scrapeMinipreco(scraperParams.getUrl(), scraperParams.getCategory());
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/minipreco/all")
    public ResponseEntity<String> scrapeMiniprecoAll() {
        try {
            this.scraperService.scrapeMiniprecoAll();
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/pingo-doce")
    public ResponseEntity<String> scrapePingoDoce(@RequestBody ScraperParams scraperParams) {
        try {
            this.scraperService.scrapePingoDoce(scraperParams.getUrl(), scraperParams.getCategory());
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/pingo-doce/all")
    public ResponseEntity<String> scrapePingoDoceAll() {
        try {
            this.scraperService.scrapePingoDoceAll();
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/intermarche")
    public ResponseEntity<String> scrapeIntermarche(@RequestBody ScraperParams scraperParams) {
        try {
            this.scraperService.scrapeIntermarche(scraperParams.getUrl(), scraperParams.getCategory(), scraperParams.getRequestBody());
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/intermarche/all")
    public ResponseEntity<String> scrapeIntermarcheAll() {
        try {
            this.scraperService.scrapeIntermarcheAll();
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

    @PostMapping("/all-chains")
    public ResponseEntity<String> scrapeAllChains() {
        try {
            this.scraperService.scrapeIntermarcheAll();
            this.scraperService.scrapeContinenteAll();
            this.scraperService.scrapePingoDoceAll();
            this.scraperService.scrapeMiniprecoAll();
            this.scraperService.scrapeAuchanAll();
            return ResponseEntity.ok("Produtos guardados com sucesso");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro ao adicionar produtos: " + e.getMessage());
        }
    }

}
