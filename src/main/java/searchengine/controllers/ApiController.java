package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping ("/indexPage{url}")
    public ResponseEntity indexPage(@PathVariable String url) throws IOException {
        initial();
        return Indexing.pageIndexing(url);
    }

    @GetMapping ("/search{query}")
    public ResponseEntity search(@PathVariable String query) throws IOException {
                            System.out.println("Поиск: " + query);
        Find.search(query);
        return null;
    }

    public void initial() {
        Indexing.setSiteRepository(siteRepository);
        Indexing.setPageRepository(pageRepository);
        Indexing.setLemmaRepository(lemmaRepository);
        Indexing.setIndexRepository(indexRepository);
    }
}
