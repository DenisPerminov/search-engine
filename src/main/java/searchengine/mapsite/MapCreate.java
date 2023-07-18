package searchengine.mapsite;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;

public class MapCreate {

    public static ArrayList<String> create(Site site, PageRepository pageRepository) {
                        System.out.println("Создание карты сайта запущено");
        MapSite mapSite = new MapSite(site.getUrl());

        try {
            Page firstPage = new Page();
            firstPage.setSite(site);
            firstPage.setPath(mapSite.getUrl());
            Connection connection = Jsoup.connect(mapSite.getUrl());
            Document doc = connection.get();
            firstPage.setContent(doc.outerHtml());
            firstPage.setCode(connection.response().statusCode());
            pageRepository.save(firstPage);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        MapSiteRecursive task = new MapSiteRecursive(mapSite, site, pageRepository);
        new ForkJoinPool().invoke(task);

        ArrayList<String> urlList = createSiteMapString(mapSite);

        return urlList;
    }

    public static ArrayList<String> createSiteMapString(MapSite mapSite) {
        ArrayList<String> listUrl = new ArrayList<>();
        listUrl.add(mapSite.getUrl());
        mapSite.getSiteMapChildrens().forEach(child -> listUrl.addAll(createSiteMapString(child)));
        //String tab = String.join("", Collections.nCopies(indent, "\t"));
        //StringBuilder result = new StringBuilder(tab + mapSite.getUrl());
        //mapSite.getSiteMapChildrens().forEach(child -> result.append("\n")
          //      .append(createSiteMapString(child, indent + 1)));
        return listUrl;
    }
}