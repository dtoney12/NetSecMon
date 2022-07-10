package netSecMon.attempt;

import netSecMon.Status;

import javax.persistence.*;

@Table
@Entity
public class Attempt {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private long id;
    private String url;
    private String timestamp;
    private String status;

    public Attempt(Long id, String url, String timestamp, Status status) {
        this.id = id;
        this.url = url;
        this.timestamp = timestamp;
        this.status = status.name();
    }
    public Attempt(String url, String timestamp, Status status) {
        this.url = url;
        this.timestamp = timestamp;
        this.status = status.name();
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Status getStatus() {
        return Status.valueOf(status);
    }

    public void setStatus(Status status) {
        this.status = status.name();
    }


    @Override
    public String toString() {
        return "\n" + url +
                " | " + timestamp +
                " | " + status;
    }
}
