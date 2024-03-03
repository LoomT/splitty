package server.database;

import commons.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    // Custom methods for participant repository can be added here if needed
}
