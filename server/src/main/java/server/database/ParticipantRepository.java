package server.database;

import commons.EventWeakKey;
import commons.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipantRepository extends JpaRepository<Participant, EventWeakKey> {
    // Custom methods for participant repository can be added here if needed
}
