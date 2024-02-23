package commons;
import jakarta.persistence.*;

import java.util.*;


@Entity
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id; //ID of participant should be determined systematically
    @OneToMany(cascade = CascadeType.PERSIST)
    private Set<Expense> expenseList;
    private String name;

    /**
     * Use this constructed if the participant already has expenses
     * @param name name of participant
     * @param expenseList expenses the participant has already acquired
     */
    public Participant(String name, Set<Expense> expenseList) {
        this.name = name;
        this.expenseList = expenseList;
    }

    /**
     * constructor
     */
    public Participant(){
    }

    /**\
     * Use this constructed if the participant is known to
     * not have any expenses
     * @param name name of participant
     */
    public Participant(String name) {
        this.name = name;
        this.expenseList = new HashSet<>();
    }

    /**
     * getter for ID
     * @return the ID of the participant
     */
    public long getID() {
        return id;
    }

    /**
     * setter for id
     * @param id id to be changed to
     */
    public void setID(long id){
        this.id = id;
    }

    /**
     * getter for name
     * @return name of the participant
     */
    public String getName() {
        return name;
    }

    /**
     * setter for name
     * @param name name to replace the previous name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter for expenses
     * @return the expenses of the participant
     */
    public Set<Expense> getExpenseList() {
        return expenseList;
    }

    /**
     *
     * @param expenseList setter for the expenses
     */
    public void setExpenseList(Set<Expense> expenseList) {
        this.expenseList = expenseList;
    }


    /**
     * adds an expense to the expenseList
     * @param expense the expense to be added to the expenseList
     * @return false if expense is null, or it is already in expenseList.
     * Otherwise, it adds the expense to the expenseList and returns true.
     */
    public boolean addExpense(Expense expense) {
        if(expense == null || expenseList.contains(expense)) return false;
        expenseList.add(expense);
        return true;
    }

    /**
     *
     * @param o object to be compared to
     * @return true iff all the parameters are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Participant that = (Participant) o;

        if (id != that.id) return false;
        if (!Objects.equals(name, that.name)) return false;
        return Objects.equals(expenseList, that.expenseList);
    }

    /**
     * hashCode generator for class Participant
     * @return a unique hashcode for the object
     */
    @Override
    public int hashCode() {
        long result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (expenseList != null ? expenseList.hashCode() : 0);
        return (int) result;
    }
}

