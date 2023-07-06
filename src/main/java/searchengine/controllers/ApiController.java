package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.SiteConf;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.mapsite.MapCreate;
import searchengine.mapsite.MapSiteRecursive;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.services.Indexing;
import searchengine.services.StatisticsService;

import java.time.LocalDateTime;
import java.util.ArrayList;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final SitesList sitesList;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;

    public ApiController(StatisticsService statisticsService, SitesList sitesList) {
        this.statisticsService = statisticsService;
        this.sitesList = sitesList;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity startIndexing () {
       return Indexing.siteIndexing(siteRepository, pageRepository, sitesList);
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }
}
