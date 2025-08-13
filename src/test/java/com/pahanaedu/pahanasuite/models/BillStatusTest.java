package com.pahanaedu.pahanasuite.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Failing-first tests for BillStatus.
 * Intent: enum exists and exposes the agreed finite states.
 */
class BillStatusTest {

    @Test
    void enum_hasExpectedValues() {
        // Expected minimal states for the assignment
        assertDoesNotThrow(() -> BillStatus.valueOf("ISSUED"));
        assertDoesNotThrow(() -> BillStatus.valueOf("PAID"));
        assertDoesNotThrow(() -> BillStatus.valueOf("CANCELLED"));

        // Basic sanity: total number of states is exactly 3 (keeps the model tight)
        assertEquals(3, BillStatus.values().length,
                "BillStatus should expose exactly ISSUED, PAID, CANCELLED for MVP");
    }
}
