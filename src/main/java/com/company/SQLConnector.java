package com.company;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLConnector {

    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(
                    "jdbc:sqlserver://cbspdatabase.database.windows.net:1433;" +
                            "database=sql_database;" +
                            "user=cbspadmin@cbspdatabase;" +
                            "password=Hello12344321;" +
                            "encrypt=true;trustServerCertificate=false;" +
                            "hostNameInCertificate=*.database.windows.net;" +
                            "loginTimeout=30");
        } catch (Exception e) {
            System.out.println(e);
        }
        return conn;
    }
}