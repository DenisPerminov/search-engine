package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteConf;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SitesList sites;

    @Override
    public StatisticsResponse getStatistics() {

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<SiteConf> sitesList = sites.getSites();
        for(int i = 0; i < sitesList.size(); i++) {
            SiteConf siteConf = sitesList.get(i);
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(siteConf.getName());
            item.setUrl(siteConf.getUrl());

            int pages = 0;
            PageRepository pageRepository = Indexing.getPageRepository();
            Iterable<Page> pageIterable = pageRepository.findAll();
            for (Page page : pageIterable) {
                if (page.getSite().getName().equals(siteConf.getName())) {
                    pages++;
                }
            }
            int lemmas = 0;
            LemmaRepository lemmaRepository = Indexing.getLemmaRepository();
            Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
            for (Lemma lemma : lemmaIterable) {
                if (lemma.getSite().getName().equals(siteConf.getName())) {
                    lemmas++;
                }
            }
            item.setPages(pages);
            item.setLemmas(lemmas);
            SiteRepository siteRepository = Indexing.getSiteRepository();
            Iterable<Site> siteIterable = siteRepository.findAll();
            for (Site site : siteIterable) {
                if (site.getName().equals(siteConf.getName())) {
                    item.setStatus(site.getStatus().toString());
                    item.setError(site.getLastError());
                    item.setStatusTime(site.getStatusTime().atZone(ZoneId.systemDefault()).toEpochSecond() * 1000);
                }
            }

            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
