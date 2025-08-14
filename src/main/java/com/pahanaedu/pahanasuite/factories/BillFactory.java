package com.pahanaedu.pahanasuite.factories;

import com.pahanaedu.pahanasuite.models.Bill;
import com.pahanaedu.pahanasuite.models.BillStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Creates Bill with a predictable billNo format:
 * INV-YYYYMMDD-HHMMSS-#### (#### is an in-JVM counter).
 */
public final class BillFactory {
    private BillFactory() {}

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    public static Bill create(int customerId) {
        Bill bill = new Bill();
        bill.setCustomerId(customerId);
        bill.setStatus(BillStatus.ISSUED);
        bill.setIssuedAt(LocalDateTime.now());
        bill.setBillNo(nextNumber());
        // Money fields are already 0.00 by default in Bill
        return bill;
    }

    private static String nextNumber() {
        String ts = LocalDateTime.now().format(DATE);
        int n = COUNTER.updateAndGet(i -> (i >= 9999) ? 1 : i + 1);
        return "INV-" + ts + "-" + String.format("%04d", n);
        // e.g., INV-20250814-203015-0001
    }
}
