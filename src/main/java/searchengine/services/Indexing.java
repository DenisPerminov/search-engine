package searchengine.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import searchengine.config.SiteConf;
import searchengine.config.SitesList;
import searchengine.mapsite.MapCreate;
import searchengine.mapsite.MapSiteRecursive;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Indexing {

    public static ResponseEntity startIndexing(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList) {
        MapSiteRecursive.setLinksPool(new ArrayList<>());

        for (SiteConf siteConf : sitesList.getSites()) {
            Integer id = deleteSite(siteConf.getName(), siteRepository);
            Site site = addSite(siteConf, siteRepository);

            site.setStatus(Status.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);

            try {
                ArrayList<String> listUrl = MapCreate.create(site, pageRepository);
                for (String ulrString : listUrl) {
                    System.out.println(ulrString);
                }

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

        }
        return  ResponseEntity.status(HttpStatus.OK).body(null);
    }

    private static Site addSite(SiteConf siteConf, SiteRepository siteRepository) {
        Site site = new Site();
        site.setName(siteConf.getName());
        site.setUrl(siteConf.getUrl());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        Site newSite = siteRepository.save(site);
        return newSite;
    }

    private static Integer deleteSite(String name, SiteRepository siteRepository) {
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
}
