package commons;

import jakarta.persistence.*;

@Entity
public class BankAccount {

    @Id
    @Column(name="bankID")
    private long bankId;

    @OneToOne
    @PrimaryKeyJoinColumn(name="bankID", referencedColumnName="participantId")
    private Participant beneficiary;

    private String accountNumber;
}
