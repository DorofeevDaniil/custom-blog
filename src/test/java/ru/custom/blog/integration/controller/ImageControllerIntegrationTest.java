package ru.custom.blog.integration.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

class ImageControllerIntegrationTest extends BaseControllerTest {

    private static final String IMAGE_NAME = "/images/test-image.jpg";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
    }

    @AfterEach
    void tearDown() throws IOException {
        File imageDir = new File(webApplicationContext.getServletContext().getRealPath("/images"));
        if (imageDir.exists()) {
            Files.walk(imageDir.toPath())
                .map(Path::toFile)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(File::delete);
        }
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
