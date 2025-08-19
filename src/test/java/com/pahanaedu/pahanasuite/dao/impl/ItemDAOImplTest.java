package com.pahanaedu.pahanasuite.dao.impl;

import com.pahanaedu.pahanasuite.dao.DBConnectionFactory;
import com.pahanaedu.pahanasuite.models.Item;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemDAOImplTest {
    private Connection conn;
    private MockedStatic<DBConnectionFactory> dbMock;

    @BeforeEach
    void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE items (id INT AUTO_INCREMENT PRIMARY KEY, sku VARCHAR(64), name VARCHAR(255), category VARCHAR(64), description TEXT, unit_price DECIMAL(10,2), stock_qty INT, attributes TEXT, created_at TIMESTAMP, updated_at TIMESTAMP)");
            st.execute("INSERT INTO items (sku,name,category,description,unit_price,stock_qty,attributes) VALUES" +
                    "('A','ItemA','OTHER','',10,2,'{}')," +
                    "('B','ItemB','OTHER','',10,6,'{}')," +
                    "('C','ItemC','OTHER','',10,1,'{}')," +
                    "('D','ItemD','OTHER','',10,3,'{}')");
        }
        dbMock = org.mockito.Mockito.mockStatic(DBConnectionFactory.class);
        dbMock.when(DBConnectionFactory::getConnection).thenReturn(conn);
    }

    @AfterEach
    void cleanup() throws Exception {
        dbMock.close();
        conn.close();
    }

    @Test
    void findLowStockReturnsOrderedList() {
        ItemDAOImpl dao = new ItemDAOImpl();
        List<Item> items = dao.findLowStock(5, 3);
        assertEquals(3, items.size());
        assertEquals("ItemC", items.get(0).getName());
        assertEquals("ItemA", items.get(1).getName());
        assertEquals("ItemD", items.get(2).getName());
    }
}
