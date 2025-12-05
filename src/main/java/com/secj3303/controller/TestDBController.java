package com.secj3303.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestDBController {

    private final DataSource ds;

    @Autowired
    public TestDBController(DataSource dataSource) {
        this.ds = dataSource;
    }

    @RequestMapping("/testdb")
    public String test() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        String sql = "SELECT * FROM person";

        try {
            conn = ds.getConnection();
            stmt = conn.createStatement();
            rs =  stmt.executeQuery(sql);

            System.out.println("execute query SELECT * FROM person");

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "testdb";
    }
}
