package commons;

import jakarta.persistence.*;

@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private double amount;
    private String description;
    private String name;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Participant burrower;
    @OneToOne(cascade = CascadeType.PERSIST)
    private Participant lender;

    /**
     * constructor
     */
    public Expense(){}

}
