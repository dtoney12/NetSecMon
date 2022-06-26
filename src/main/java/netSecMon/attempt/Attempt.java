package netSecMon.attempt;

import javax.persistence.*;
import java.time.LocalDate;

@Table
@Entity
public class Attempt {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private long id;
    private String url;
    private LocalDate timestamp;
    private String status;

    public Attempt(Long id, String url, LocalDate timestamp, String status) {
        this.id = id;
        this.url = url;
        this.timestamp = timestamp;
        this.status = status;
    }

    public Attempt() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "Attempt{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}
