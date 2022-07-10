package netSecMon.attempt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.Optional;

@Service
public class AttemptService {
    private final AttemptRepository repository;

    @Autowired
    public AttemptService(AttemptRepository repository) {
        this.repository = repository;
    }
    public List<Attempt> getAttemptsByHostname(String hostname) {
        return repository.findByUrl(hostname);
    }
    public List<Attempt> getAttempts() {
        return repository.findAll();
    }

    public void addAttempt(Attempt attempt) {
        repository.save(attempt);
    }
}
