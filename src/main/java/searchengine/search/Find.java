package searchengine.search;

import searchengine.services.LemmaFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Find {

    private static Map<String, Integer> lemmas = new HashMap<>();
    private static ArrayList<LemmaMap> lemmaMap = new ArrayList<>();
    public static void search(String text) throws IOException {
                            System.out.println(text);
        getLemmas(text);
        Collections.sort(lemmaMap);
        for (LemmaMap lemmaMap1 : lemmaMap) {
            System.out.println(lemmaMap1.getLemma() + " = " + lemmaMap1.getFrequency());
        }
    }

    public static void getLemmas(String text) throws IOException {
        LemmaFinder lemmaFinder = LemmaFinder.getInstance();
        Map<String, Integer> mapLemmas = lemmaFinder.collectLemmas(text);
        for (Map.Entry<String, Integer> lemma : mapLemmas.entrySet()) {
            if (lemma.getValue() < 5) {
                LemmaMap lemmaTemp = new LemmaMap();
                lemmaTemp.setLemma(lemma.getKey());
                lemmaTemp.setFrequency(lemma.getValue());
                lemmaMap.add(lemmaTemp);
            }
        }
    }

}
