package ru.custom.blog.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.custom.blog.repository.PostRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class ImageService {
    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);
    private final PostRepository postRepository;

    public ImageService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public ResponseEntity<Resource> getImage(Long id) {
        Optional<String> imagePath = postRepository.findImageById(id);

        if (imagePath.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(imagePath.get());
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filePath.getFileName() + "\"")
                .body(resource);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public String saveImage(MultipartFile imageFile, String basePath) {
        String uploadDir =  Paths.get(basePath, "images").toString();

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        String filePath = uploadDir + File.separator + imageFile.getOriginalFilename();

        File destinationFile = new File(filePath);

        try {
            imageFile.transferTo(destinationFile);
        } catch (IOException e) {
            logger.error(String.format("Can't write file %s. Got error: %s", filePath, e.getMessage()));
        }

        return filePath;
    }

    public void removeImage(String imagePath) {
        try {
            Files.delete(Path.of(imagePath));
        } catch(IOException e) {
            logger.error(String.format("Can't remove file %s. Got error: %s", imagePath, e.getMessage()));
        }
    }
}
