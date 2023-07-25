package searchengine.mapsite;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveTask;

public class MapSiteRecursive extends RecursiveTask<ArrayList<String>> {
    private MapSite mapSite;
    private Site site;
    private static ArrayList<String> linksPool = new ArrayList();
    private static ArrayList<Page> pagePool = new ArrayList<>();
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private static List<MapSiteRecursive> taskManager = new ArrayList<>();

    public MapSiteRecursive(MapSite mapSite, Site site, PageRepository pageRepository) {
        this.mapSite = mapSite;
        this.site = site;
        this.pageRepository = pageRepository;
    }

    @Override
    protected ArrayList<String> compute() {

        ArrayList<String> listUrl = new ArrayList<>();

        if (!linksPool.contains(mapSite.getUrl())) {
            linksPool.add(mapSite.getUrl());

            ParseWeb parseWeb = new ParseWeb(linksPool, pageRepository);
            ConcurrentSkipListSet<String> links = parseWeb.getLinks(mapSite.getUrl());

            for (String link : links) {
                Iterable<Page> pageIterable = pageRepository.findAll();
                for (Page page : pageIterable) {
                    if (!linksPool.contains(link) && !page.getPath().contains(link)) {
                        linksPool.add(link);
                        mapSite.addChildren(new MapSite(link));
                    }

                }
            }
        }

        List<MapSiteRecursive> taskList = new ArrayList<>();

        for (MapSite child : mapSite.getSiteMapChildrens()) {
            MapSiteRecursive task = new MapSiteRecursive(child, site, pageRepository);
            task.fork();
            taskList.add(task);
            taskManager.add(task);
        }
        
        for (MapSiteRecursive task : taskList) {
            listUrl.addAll(task.join());
        }
        return listUrl;
    }

    public static ArrayList<String> getLinksPool() {
        return linksPool;
    }

    public static void setLinksPool(ArrayList<String> linksPool) {
        MapSiteRecursive.linksPool = linksPool;
    }

    public static void stop () {
        for (MapSiteRecursive task : taskManager) {
            task.cancel(true);
        }
    }
}