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

    /**
     *
     * @param id id of expense
     * @param amount amount owed
     * @param description description of the expense
     * @param name name of the expense
     * @param burrower person burrowed from for the expense
     * @param lender person that lent the expense
     */
    public Expense(long id, double amount, String description, String name,
                   Participant burrower, Participant lender) {
        this.id = id;
        this.amount = amount;
        this.description = description;
        this.name = name;
        this.burrower = burrower;
        this.lender = lender;
    }
}
