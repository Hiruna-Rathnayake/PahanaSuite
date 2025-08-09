package com.pahanaedu.pahanasuite.dao;

import java.sql.Connection;

public interface DBConnection {
    Connection getConnection() throws Exception;
}
