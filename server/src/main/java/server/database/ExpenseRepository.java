package server.database;

import commons.Expense;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    // Custom methods for expense repository can be added here if needed
}
