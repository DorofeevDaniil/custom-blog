package ru.custom.blog.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.custom.blog.repository.PostRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {
    private final PostRepository postRepository;

    public ImageService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public ResponseEntity<Resource> getImage(Long id) {
        String imagePath = postRepository.findImageById(id);

        if (imagePath == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(imagePath);
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
}
