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
        MapSiteRecursive.setLinksPool(new ArrayList<>());

        for (SiteConf siteConf : sitesList.getSites()) {
            Integer id = deleteSite(siteConf.getName());
            Site site = addSite(siteConf);

            try {
                ArrayList<String> listUrl = MapCreate.create(site, pageRepository);

                for (String path : listUrl) {
                    Page page = new Page();
                    page.setSite(site);
                    page.setPath(path);
                    pageRepository.save(page);
                }
            } catch (Exception ex) {
                site.setStatusTime(LocalDateTime.now());
                site.setStatus(Status.FAILED);
                site.setLastError("Произошла ошибка: " + ex.getMessage());
                siteRepository.save(site);
                continue;
            }
            site.setStatus(Status.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }
        return null;
    }

    private Site addSite (SiteConf siteConf) {
        Site site = new Site();
        site.setName(siteConf.getName());
        site.setUrl(siteConf.getUrl());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        Site newSite = siteRepository.save(site);
        return newSite;
    }

    private Integer deleteSite (String name) {
        Iterable<Site> siteIterable = siteRepository.findAll();
        Integer id = null;
        for (Site site : siteIterable) {
            if (site.getName().equals(name)) {
                id = site.getId();
            }
        }
        if (id != null) {
            siteRepository.deleteById(id);
        }
        return id;
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }
}
