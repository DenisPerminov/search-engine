package searchengine.mapsite;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class MapSite {
    private String url;
    private CopyOnWriteArrayList<MapSite> mapSiteChildren;

    public MapSite(String url) {
        mapSiteChildren = new CopyOnWriteArrayList<>();
        this.url = url;
    }

    public void addChildren(MapSite children) {
        mapSiteChildren.add(children);
    }

    public CopyOnWriteArrayList<MapSite> getSiteMapChildrens() {
        return mapSiteChildren;
    }

    public String getUrl() {
        return url;
    }
}