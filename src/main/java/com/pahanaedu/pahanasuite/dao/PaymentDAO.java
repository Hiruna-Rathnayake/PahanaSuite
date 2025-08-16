package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.Payment;
import java.math.BigDecimal;
import java.util.List;

public interface PaymentDAO {
    Payment create(Payment p);
    List<Payment> findByBillId(int billId);
    BigDecimal sumByBillId(int billId);
}
