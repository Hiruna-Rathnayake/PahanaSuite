package com.pahanaedu.pahanasuite.dao;

import com.pahanaedu.pahanasuite.models.Item;

import java.util.List;

public interface ItemDAO {
    Item findById(int id);
    Item findBySku(String sku);
    List<Item> search(String q, String category, int limit, int offset); // q matches name/sku
    Item createItem(Item item);     // returns with id (and timestamps if you map them)
    boolean updateItem(Item item);
    boolean deleteItem(int id);

    /** Add or remove stock in one atomic statement; refuses to go below 0. */
    boolean adjustStock(int id, int delta);

    /** Counts items whose stock is below the given threshold. */
    int countLowStock(int threshold);
}
