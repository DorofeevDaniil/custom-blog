package ru.custom.blog.integration.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.custom.blog.integration.configuration.DataSourceConfiguration;
import ru.custom.blog.integration.configuration.WebConfiguration;

import java.io.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
class ImageControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    private static final String IMAGE_NAME = "/images/test-image.jpg";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void showPosts_shouldReturnHtmlWithPosts() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
            "image",
            "test-image.png",
            "image/png",
            new ClassPathResource(IMAGE_NAME).getInputStream()
        );

        mockMvc.perform(multipart("/posts")
            .file(imageFile)
            .param("title", "new title")
            .param("tags", "some newTag")
            .param("text", "some test text"));

        mockMvc.perform(get("/images/1")) // путь зависит от контроллера
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
            .andExpect(content().bytes(getImageBytes(IMAGE_NAME)));
    }

    private byte[] getImageBytes(String path) throws IOException {
        try (InputStream is = new ClassPathResource(IMAGE_NAME).getInputStream()) {
            assertNotNull(is, "Файл " + path + " не найден в resources");
            return is.readAllBytes();
        }
    }
}
