package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    BankAccount ba;

    @BeforeEach
    public void setUp(){
        ba = new BankAccount("test", "1234abc");
    }

    @Test
    void setBankId() {
        ba.setBankId(1);
        assertEquals(1, ba.getBankId());
    }

    @Test
    void getBeneficiary() {
        assertEquals("test", ba.getBeneficiary());
    }

    @Test
    void setBeneficiary() {
        assertEquals("test", ba.getBeneficiary());
        ba.setBeneficiary("test2");
        assertEquals("test2", ba.getBeneficiary());
    }

    @Test
    void getAccountNumber() {
        assertEquals("1234abc", ba.getAccountNumber());
    }

    @Test
    void setAccountNumber() {
        assertEquals("1234abc", ba.getAccountNumber());
        ba.setAccountNumber("1234");
        assertEquals("1234", ba.getAccountNumber());
    }

    @Test
    void testEqualsTrue() {
        BankAccount test = new BankAccount("test", "1234abc");
        assertEquals(test, ba);
    }

    @Test
    void testEqualsFalse() {
        BankAccount test = new BankAccount("test2", "1234abc");
        assertNotEquals(test, ba);
    }

    @Test
    void testHashCode() {
        BankAccount test = new BankAccount("test", "1234abc");
        assertEquals(test.hashCode(), ba.hashCode());
    }
}