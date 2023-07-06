package searchengine.model;


import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(columnDefinition = "enum('INDEXING','INDEXED', 'FAILED')")
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "status_time", columnDefinition = "DATETIME")
    private LocalDateTime statusTime;
    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;
    @Column(columnDefinition = "VARCHAR(255)")
    private String url;
    @Column(columnDefinition = "VARCHAR(255)")
    private String name;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "site_id")
    private List<Page> pages;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(LocalDateTime statusTime) {
        this.statusTime = statusTime;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
