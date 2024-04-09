package server.database;

import commons.EventWeakKey;
import commons.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, EventWeakKey> {
}
