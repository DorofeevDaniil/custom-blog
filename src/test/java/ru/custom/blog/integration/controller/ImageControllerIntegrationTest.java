package ru.custom.blog.integration.controller;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;

import java.io.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

class ImageControllerIntegrationTest extends BaseControllerTest {

    private static final String IMAGE_NAME = "/images/test-image.jpg";

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
