package searchengine.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(indexes = @Index(columnList = "path", unique = true))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Site site;
    @Column(name = "path", columnDefinition = "TEXT", nullable = false)
    private String path;
    private int code;
    @Column(columnDefinition = "mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String content;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "page_id")
    private List<Indexes> indexes;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

