package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.search.Find;
import searchengine.services.Indexing;
import searchengine.services.StatisticsService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SitesList sitesList;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private IndexRepository indexRepository;

    public ApiController(StatisticsService statisticsService, SitesList sitesList) {
        this.statisticsService = statisticsService;
        this.sitesList = sitesList;

    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        initial();
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing() {
        initial();
       return Indexing.startIndexing(sitesList);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity stopIndexing() {
        return Indexing.stopIndexing();
    }

    @PostMapping ("/indexPage")
    public ResponseEntity indexPage(@RequestParam String url) throws IOException {
        initial();
        return Indexing.pageIndexing(url);
    }

    @GetMapping ("/search")
    public ResponseEntity search(@RequestParam String query) throws IOException {
        initialSearch();
        Find.search(query);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    public void initial() {
        Indexing.setSiteRepository(siteRepository);
        Indexing.setPageRepository(pageRepository);
        Indexing.setLemmaRepository(lemmaRepository);
        Indexing.setIndexRepository(indexRepository);
    }

    public void initialSearch() {
        Find.setLemmaRepository(lemmaRepository);
        Find.setIndexRepository(indexRepository);
    }
}
