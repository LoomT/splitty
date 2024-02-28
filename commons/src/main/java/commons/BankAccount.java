package commons;

import jakarta.persistence.*;

@Entity
public class BankAccount {

    @Id
    private long bankId;

    private String beneficiary;

    private String accountNumber;

    /**
     *
     * @param beneficiary beneficiary of the account
     * @param accountNumber accountNumber
     */
    public BankAccount(String beneficiary, String accountNumber) {
        this.beneficiary = beneficiary;
        this.accountNumber = accountNumber; //should be hashed
    }

    /**
     * Constructor with no variables
     */
    public BankAccount(){}

    /**
     * getter for bankID
     * @return bankID
     */
    public long getBankId() {
        return bankId;
    }

    /**
     * Setter for bankID
     * @param bankId bankID to replace the old one
     */
    public void setBankId(long bankId) {
        this.bankId = bankId;
    }

    /**
     * Getter for beneficiary
     * @return beneficiary of account
     */
    public String getBeneficiary() {
        return beneficiary;
    }

    /**
     * Setter for beneficiary
     * @param beneficiary beneficiary to replace the old one
     */
    public void setBeneficiary(String beneficiary) {
        this.beneficiary = beneficiary;
    }

    /**
     * getter for account number
     * @return accountNumber
     */
    public String getAccountNumber() {
        return accountNumber;
    }

    /**
     *
     * @param accountNumber accountNumber to replace the old one
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    /**
     *
     * @param o object to be compared to
     * @return true iff all the fields are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BankAccount that = (BankAccount) o;

        if (bankId != that.bankId) return false;
        if (!beneficiary.equals(that.beneficiary)) return false;
        return accountNumber.equals(that.accountNumber);
    }

    /**
     *
     * @return hashcode of object
     */
    @Override
    public int hashCode() {
        int result = (int) (bankId ^ (bankId >>> 32));
        result = 31 * result + beneficiary.hashCode();
        result = 31 * result + accountNumber.hashCode();
        return result;
    }

    /**
     *
     * @return human-readable string of object
     */
    @Override
    public String toString() {
        return "BankAccount{" +
                "bankId=" + bankId +
                ", beneficiary=" + beneficiary +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}
