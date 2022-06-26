package netSecMon.attempt;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConnectionAttemptRepository extends JpaRepository<Attempt, Long> {

}
