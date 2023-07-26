package searchengine.search;

public class LemmaMap implements Comparable<LemmaMap>{

    private String lemma;
    private Integer frequency;

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    @Override
    public int compareTo(LemmaMap o) {
        return frequency.compareTo(o.frequency);
    }

}
