package client.scenes;

import commons.Participant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenDebtsPageCtrlTest {

    @Test
    void initializePage() {
    }

    @Test
    void minCashFlow() {
    }

    @Test
    void getMax() {
        Map<Participant, BigDecimal> map = new HashMap<>();
        Participant bob = new Participant("Bob");
        Participant tom = new Participant("Tom");
        Participant kate = new Participant("Kate");
        map.put(bob, new BigDecimal("100.00"));
        map.put(tom, new BigDecimal("-20.00"));
        map.put(kate, new BigDecimal("-80.00"));
        assertEquals(bob, OpenDebtsPageCtrl.getMax(map));
    }

    @Test
    void getMin() {
        Map<Participant, BigDecimal> map = new HashMap<>();
        Participant bob = new Participant("Bob");
        Participant tom = new Participant("Tom");
        Participant kate = new Participant("Kate");
        map.put(bob, new BigDecimal("100.00"));
        map.put(tom, new BigDecimal("-20.00"));
        map.put(kate, new BigDecimal("-80.00"));
        assertEquals(kate, OpenDebtsPageCtrl.getMin(map));
    }
}