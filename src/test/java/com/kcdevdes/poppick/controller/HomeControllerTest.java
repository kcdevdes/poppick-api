package com.kcdevdes.poppick.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.BDDMockito.given;

@WebMvcTest(controllers = HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DataSource dataSource;

    @Test
    @DisplayName("Check if DB connection is OK")
    void checkDatabaseConnection() throws Exception {
        // Mock the DataSource to return a valid connection
        Connection mockConnection = org.mockito.Mockito.mock(Connection.class);
        given(dataSource.getConnection()).willReturn(mockConnection);

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("DB Connection OK"));
    }

    @Test
    @DisplayName("Check if DB connection is failed")
    void checkDatabaseConnectionFailure() throws Exception {
        // Mock the DataSource to throw an SQLException
        given(dataSource.getConnection()).willThrow(new SQLException("Unable to connect"));

        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string("DB Connection Failed: Unable to connect"));
    }
}