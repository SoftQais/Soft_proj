package library0;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FineTest {

    @Test
    void getOutstandingAmount_returnsRemaining() {
        Fine fine = new Fine("F1", "U1", "L1", 30, 10);

        assertEquals(20, fine.getOutstandingAmount());
    }

    @Test
    void applyPayment_partialPayment() {
        Fine fine = new Fine("F1", "U1", "L1", 30, 0);

        int applied = fine.applyPayment(15);

        assertEquals(15, applied);
        assertEquals(15, fine.getPaidAmount());
        assertEquals(15, fine.getOutstandingAmount());
    }

    @Test
    void applyPayment_fullPaymentAndMore() {
        Fine fine = new Fine("F1", "U1", "L1", 30, 10);

        int applied = fine.applyPayment(50);

        // outstanding before pay = 20, so only 20 should be applied
        assertEquals(20, applied);
        assertEquals(30, fine.getPaidAmount());
        assertEquals(0, fine.getOutstandingAmount());
    }
}