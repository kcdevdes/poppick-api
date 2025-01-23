package com.kcdevdes.poppick.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;


@RestController
public class HomeController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/")
    public String checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            // DB 연결 확인 성공 시
            return "DB Connection OK";
        } catch (Exception e) {
            // DB 연결 실패 시
            return "DB Connection Failed: " + e.getMessage();
        }
    }
}
