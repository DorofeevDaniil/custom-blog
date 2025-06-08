package ru.custom.blog.unit.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.custom.blog.repository.PostRepository;
import ru.custom.blog.service.ImageService;
import ru.custom.blog.unit.configuration.ImageServiceUnitTestConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Optional;

import static org.mockito.Mockito.reset;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("unit")
@ContextConfiguration(classes = ImageServiceUnitTestConfig.class)
class ImageServiceTest {
    @Autowired
    private PostRepository mockPostRepository;

    @Autowired
    private ImageService mockImageService;

    private static final Long FIRST_ID = 1L;

    private Path tempFile;
    private Path tempDir;

    @BeforeEach
    void resetMocks() throws IOException {
        reset(mockPostRepository);

        tempDir = Files.createTempDirectory("image-test-dir");
        tempFile = Files.createTempFile("test", ".jpg");
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);

        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        }
    }

    @Test
    void getImage_imageNotFoundInDatabase() {
        when(mockPostRepository.findImageById(FIRST_ID)).thenReturn(Optional.empty());

        ResponseEntity<Resource> response = mockImageService.getImage(FIRST_ID);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getImage_imageFileDoesNotExist() {
        when(mockPostRepository.findImageById(FIRST_ID)).thenReturn(Optional.of("/non/existing/path/image.jpg"));

        ResponseEntity<Resource> response = mockImageService.getImage(FIRST_ID);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void getImage_imageIsReturnedSuccessfully() throws IOException {
        Path imageFile = tempDir.resolve("test.jpg");
        Files.write(imageFile, "test content".getBytes());

        when(mockPostRepository.findImageById(FIRST_ID)).thenReturn(Optional.of(imageFile.toString()));

        ResponseEntity<Resource> response = mockImageService.getImage(FIRST_ID);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("inline; filename=\"test.jpg\"", response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
    }

    @Test
    void saveImage_shouldSaveFileToDiskAndReturnPath() throws IOException {
        byte[] content = "test image content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );

        String savedPath = mockImageService.saveImage(multipartFile, tempDir.toString());

        File savedFile = new File(savedPath);
        assertTrue(savedFile.exists());
        assertEquals("test.jpg", savedFile.getName());
        assertEquals(content.length, Files.readAllBytes(savedFile.toPath()).length);
    }

    @Test
    void saveImage_shouldNotThrowExceptionIfWriteFails() {
        MockMultipartFile badFile = new MockMultipartFile(
            "file",
            "bad.jpg",
            "image/jpeg",
            new byte[0]
        );

        File nonWritableDir = new File(tempDir.toFile(), "unwritable");
        nonWritableDir.mkdirs();
        nonWritableDir.setWritable(false);

        assertDoesNotThrow(() ->
            mockImageService.saveImage(badFile, nonWritableDir.getAbsolutePath())
        );
    }

    @Test
    void removeImage_shouldDeleteFileSuccessfully() {
        assertTrue(Files.exists(tempFile));

        mockImageService.removeImage(tempFile.toString());

        assertFalse(Files.exists(tempFile));
    }

    @Test
    void removeImage_shouldNotThrowIfFileDoesNotExist() throws IOException {
        Files.delete(tempFile);

        assertDoesNotThrow(() -> mockImageService.removeImage(tempFile.toString()));
    }
}
