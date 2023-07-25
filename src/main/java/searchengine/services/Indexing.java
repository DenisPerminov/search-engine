package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import searchengine.config.SiteConf;
import searchengine.config.SitesList;
import searchengine.mapsite.MapCreate;
import searchengine.mapsite.MapSiteRecursive;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class Indexing {

    private static SiteRepository siteRepository;
    private static PageRepository pageRepository;
    private static LemmaRepository lemmaRepository;
    private static IndexRepository indexRepository;

    public static ResponseEntity startIndexing(SitesList sitesList) {

        String currentPath = "";

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
                    currentPath = path;

                    Connection connection = null;
                    try {
                        connection = Jsoup.connect(path);
                        Document doc = connection.get();
                        page.setContent(doc.outerHtml());
                        page.setCode(connection.response().statusCode());
                        pageRepository.save(page);
                    } catch (Exception exception) {
                        page.setCode(connection.response().statusCode());
                        pageRepository.save(page);
                        continue;
                    }

                    pageAddIndex(page);
                }

            } catch (Exception ex) {
                site.setStatusTime(LocalDateTime.now());
                site.setStatus(Status.FAILED);
                site.setLastError(ex.getMessage() + " - " + currentPath);
                siteRepository.save(site);
                continue;
            }

            site.setStatus(Status.INDEXED);
            site.setStatusTime(LocalDateTime.now());
            siteRepository.save(site);
        }

        return  ResponseEntity.status(HttpStatus.OK).body(null);
    }

    private static Site addSite(SiteConf siteConf) {
        Site site = new Site();
        site.setName(siteConf.getName());
        site.setUrl(siteConf.getUrl());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        Site newSite = siteRepository.save(site);
        return newSite;
    }

    private static Integer deleteSite(String name) {
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

    public static ResponseEntity stopIndexing () {
        MapSiteRecursive.stop();
        return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(null);
    }

    public static ResponseEntity pageIndexing(String url) throws IOException {


        Iterable<Page> pageIterable = pageRepository.findAll();
        for (Page page : pageIterable) {
            if (page.getPath().equals(url)) {
                pageAddIndex(page);
                return ResponseEntity.status(HttpStatus.OK).body(null);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    public static void pageAddIndex(Page page) throws IOException {
        Page newPage = new Page();
        newPage.setSite(page.getSite());
        newPage.setPath(page.getPath());
        Connection connection = Jsoup.connect(page.getPath());
        Document doc = connection.get();
        newPage.setContent(doc.outerHtml());
        newPage.setCode(connection.response().statusCode());
        pageRepository.save(newPage);

        LemmaFinder lemmaFinder = LemmaFinder.getInstance();
        Map<String, Integer> lemmas;
        lemmas = lemmaFinder.collectLemmas(newPage.getContent());

        for (Map.Entry<String, Integer> lemma : lemmas.entrySet()) {
            Lemma newLemma = addLemmas(lemma.getKey(), lemma.getValue(), newPage);
            addIndex(lemma.getKey(), lemma.getValue(), newLemma, page);
        }
    }

    private static Lemma addLemmas(String word, Integer count, Page page) {

        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();

        for (Lemma lemma : lemmaIterable) {
            if (lemma.getLemma().equals(word)) {
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaRepository.save(lemma);
                return lemma;
            }
        }
        Lemma newLemma = new Lemma();
        newLemma.setLemma(word);
        newLemma.setFrequency(1);
        newLemma.setSite(page.getSite());
        lemmaRepository.save(newLemma);
        return newLemma;
    }

    private static void addIndex(String word, Integer count, Lemma lemma, Page page) {
        Iterable<Indexes> indexIterable = indexRepository.findAll();

        for (Indexes indexes : indexIterable) {
            if (indexes.getLemma().getLemma().equals(word) && indexes.getPage().getPath().equals(page.getPath())) {
                indexes.setRank(count);
                indexRepository.save(indexes);
                return;
            }
        }
        Indexes newIndex = new Indexes();
        newIndex.setPage(page);
        newIndex.setLemma(lemma);
        newIndex.setRank(count);
        indexRepository.save(newIndex);
    }

    public static SiteRepository getSiteRepository() {
        return siteRepository;
    }

    public static void setSiteRepository(SiteRepository siteRepository) {
        Indexing.siteRepository = siteRepository;
    }

    public static PageRepository getPageRepository() {
        return pageRepository;
    }

    public static void setPageRepository(PageRepository pageRepository) {
        Indexing.pageRepository = pageRepository;
    }

    public static LemmaRepository getLemmaRepository() {
        return lemmaRepository;
    }

    public static void setLemmaRepository(LemmaRepository lemmaRepository) {
        Indexing.lemmaRepository = lemmaRepository;
    }

    public static IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public static void setIndexRepository(IndexRepository indexRepository) {
        Indexing.indexRepository = indexRepository;
    }
}
