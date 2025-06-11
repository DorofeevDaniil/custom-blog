package ru.custom.blog.integration.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
abstract class BaseControllerTest {
    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected MockMvc mockMvc;
}
