package server.database;

import commons.EventWeakKey;
import commons.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, EventWeakKey> {
}
