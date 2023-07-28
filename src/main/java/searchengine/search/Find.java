package searchengine.search;

import searchengine.model.Indexes;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.services.LemmaFinder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Find {

    private static LemmaRepository lemmaRepository;
    private static IndexRepository indexRepository;
    private static Float maxAbsRelevance = 0.0F;



    public static ArrayList<SearchData> search(String text) throws IOException {
        ArrayList<Lemma> lemmas = new ArrayList<>();
        ArrayList<SearchData> dataList = new ArrayList<>();

        lemmas = getLemmas(text);
        if (lemmas.isEmpty()) {
            System.out.println("Уточните запрос");
            return null;
        }
        Collections.sort(lemmas);
        Lemma firstLemma = lemmas.get(0);
        lemmas.remove(0);
        Iterable<Indexes> indexIterable = indexRepository.findAll();
        ArrayList<Indexes> indexesArrayList = new ArrayList<>();
        for (Indexes indexes : indexIterable) {
            indexesArrayList.add(indexes);
        }

        ArrayList<Indexes> tempIndexList = new ArrayList<>();
        tempIndexList = searchPages(firstLemma, indexesArrayList);
        ArrayList<Page> pages = new ArrayList<>();
        for (Indexes indexes : tempIndexList) {
            pages.add(indexes.getPage());
        }

        for (Lemma lemma : lemmas) {

            tempIndexList.clear();
            tempIndexList = searchIndex(lemma, pages);
            indexesArrayList.clear();
            indexesArrayList.addAll(tempIndexList);
            pages.clear();
            for (Indexes indexes : tempIndexList) {
                pages.add(indexes.getPage());
            }
        }

        if (indexesArrayList.isEmpty()) {
            System.out.println("Совпадений не найдено");
            return null;
        } else {
            System.out.println("Найдены следующие страницы: ");
            lemmas.add(firstLemma);
            TreeMap<Float, Page> pageMap = calculateAbsRelevance(pages, lemmas);
            Map<Page, Float> pageRelevanceMap = calculateRelevance(pageMap);

            dataList = getResult(pageRelevanceMap);
        }
        return dataList;
    }

    public static ArrayList<Lemma> getLemmas(String text) throws IOException {
        ArrayList<Lemma> lemmaArrayList = new ArrayList<>();
        LemmaFinder lemmaFinder = LemmaFinder.getInstance();
        Map<String, Integer> mapLemmas = lemmaFinder.collectLemmas(text);
        for (Map.Entry<String, Integer> lemmaMap : mapLemmas.entrySet()) {
            Lemma lemma = searchLemmas(lemmaMap.getKey());
            if (lemma != null) {
                lemmaArrayList.add(lemma);
            }
        }
        return lemmaArrayList;
    }

    public static Lemma searchLemmas(String lemmaSearched) {
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        for (Lemma lemma : lemmaIterable) {
            if (lemma.getLemma().equals(lemmaSearched) && lemma.getFrequency() < 40) {
                return lemma;
            }
        }
        return null;
    }

    public static ArrayList<Indexes> searchPages(Lemma lemma, ArrayList<Indexes> indexesArrayList) {
        ArrayList<Indexes> newIndexList = new ArrayList<>();
        for (Indexes indexes : indexesArrayList) {
            if (indexes.getLemma().getId() == lemma.getId()) {
                newIndexList.add(indexes);
            }
        }
        return newIndexList;
    }

    public static ArrayList<Indexes> searchIndex(Lemma lemma, ArrayList<Page> pages) {
        ArrayList<Indexes> newIndexList = new ArrayList<>();
        Iterable<Indexes> indexesIterable = indexRepository.findAll();
        for (Page page : pages) {
            for (Indexes indexes2 : indexesIterable) {
                if ((indexes2.getPage().getId() == page.getId()) &&
                        (indexes2.getLemma().getId() == lemma.getId())) {
                    newIndexList.add(indexes2);
                }
            }
        }
        return newIndexList;
    }

    public static TreeMap<Float, Page> calculateAbsRelevance(ArrayList<Page> pages, ArrayList<Lemma> lemmaList) {
        TreeMap<Float, Page> pageMap = new TreeMap<>();
        Iterable<Indexes> indexesIterable = indexRepository.findAll();
        ArrayList<Indexes> indexesList = new ArrayList<>();
        for (Indexes indexes : indexesIterable) {
            indexesList.add(indexes);
        }
        for (Page page : pages) {
            Float absRelevance = 0.0F;
            for (Lemma lemma : lemmaList) {
                for (Indexes indexes : indexesList) {
                    if ((indexes.getLemma().getId() == lemma.getId()) &&
                            (indexes.getPage().getId() == page.getId())) {
                        absRelevance += indexes.getRank();
                    }
                }
            }
            pageMap.put(absRelevance, page);
            if (absRelevance > maxAbsRelevance) {
                maxAbsRelevance = absRelevance;
            }

        }
        return pageMap;
    }

    public static Map<Page, Float> calculateRelevance(TreeMap<Float, Page> pageMap) {
        Map<Page, Float> pageRelevanceMap = new HashMap<>();

        for (Map.Entry<Float, Page> entry : pageMap.entrySet()) {
            Float relevance = entry.getKey() / maxAbsRelevance;
            pageRelevanceMap.put(entry.getValue(), relevance);
        }
        Map<Page, Float> sortedMap = pageRelevanceMap.entrySet()
                .stream()
                .sorted(Collections
                        .reverseOrder(Map.Entry.comparingByValue())).collect(Collectors
                        .toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        pageRelevanceMap.clear();
        return sortedMap;
    }

    public static ArrayList<SearchData> getResult(Map<Page, Float> pageMap) {
        ArrayList<SearchData> dataList = new ArrayList<>();
        for (Map.Entry<Page, Float> entry : pageMap.entrySet()) {
            SearchData searchData = new SearchData();
            searchData.setUri(entry.getKey().getPath());
            searchData.setTitle("Title");
            searchData.setSnippet("text");
            searchData.setRelevance(entry.getValue());
            dataList.add(searchData);
        }
        return dataList;
    }
    public static LemmaRepository getLemmaRepository() {
        return lemmaRepository;
    }

    public static void setLemmaRepository(LemmaRepository lemmaRepository) {
        Find.lemmaRepository = lemmaRepository;
    }

    public static IndexRepository getIndexRepository() {
        return indexRepository;
    }

    public static void setIndexRepository(IndexRepository indexRepository) {
        Find.indexRepository = indexRepository;
    }
}
