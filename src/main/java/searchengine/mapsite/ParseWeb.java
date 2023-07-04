package searchengine.mapsite;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.model.Page;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.Thread.sleep;

public class ParseWeb {
    private static ConcurrentSkipListSet<String> links;
    public static ArrayList<String> linksFiltred;
    public static PageRepository pageRepository;

    public ParseWeb(ArrayList<String> linksFiltred, PageRepository pageRepository) {
        //this.linksFiltred = linksFiltred;
        this.pageRepository = pageRepository;
    }

    public static ConcurrentSkipListSet<String> getLinks(String url) {
                                System.out.println("Запущен парсинг страницы: " + url);
        links = new ConcurrentSkipListSet<>();
        try {
            sleep(200);
            Connection connection = Jsoup.connect(url)
                    .ignoreHttpErrors(true)
                    .followRedirects(true);
            Document document = connection.get();
            Elements elements = document.select("body").select("a");
            for (Element element : elements) {
                String link = element.absUrl("href");
                Iterable<Page> pageIterable = pageRepository.findAll();
                for (Page page : pageIterable) {
                    if (isLink(link) && !isFile(link) && !page.getPath().contains(link) && !links.contains(link)) {
                        links.add(link);
                    }

                }
            }
        } catch (InterruptedException e) {
            System.out.println(e + " - " + url);
        } catch (SocketTimeoutException e) {
            System.out.println(e + " - " + url);
        } catch (IOException e) {
            System.out.println(e + " - " + url);
        }
        return links;
    }

    private static boolean isLink(String link) {
        String regex = "/";
        return link.contains(regex);
    }

    private static boolean isFile(String link) {
        link.toLowerCase();
        return link.contains(".jpg")
                || link.contains(".jpeg")
                || link.contains(".png")
                || link.contains(".gif")
                || link.contains(".webp")
                || link.contains(".pdf")
                || link.contains(".eps")
                || link.contains(".xlsx")
                || link.contains(".doc")
                || link.contains(".pptx")
                || link.contains(".docx")
                || link.contains("?_ga");
    }
}