package ru.custom.blog.initializer;

import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.service.PostService;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ImageStartupInitializer {

    private final PostService postService;
    private final ServletContext servletContext;

    private static final List<String> INIT_FILES = List.of("post-add-comment-html.jpg", "post-add-edit-html.jpg", "post-add-html.jpg");
    private static final String INIT_TEXT = "Этот текст так прекрасен только потому, что сгенерирован при старте для изображения ";

    private static final Logger logger = LoggerFactory.getLogger(ImageStartupInitializer.class);

    public ImageStartupInitializer(PostService postService, ServletContext servletContext) {
        this.postService = postService;
        this.servletContext = servletContext;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void populatePosts(ContextRefreshedEvent event) {
        try {
            String basePath = servletContext.getRealPath("/");
            Path imageDir = Paths.get(basePath, "images");

            if (Files.notExists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            for (String filename : INIT_FILES) {
                Path targetPath = imageDir.resolve(filename);
                if (Files.notExists(targetPath)) {
                    try (InputStream in = new ClassPathResource("init_images/" + filename).getInputStream()) {
                        Files.copy(in, targetPath);
                    }
                }

                PostModel post = new PostModel();
                post.setTitle("Init: " + filename);
                post.setText("Text for " + INIT_TEXT + filename);
                post.setImagePath(targetPath.toString());
                post.setLikesCount(0);
                post.setTags(List.of("init", "auto"));

                postService.savePost(post);
            }

        } catch (Exception e) {
            logger.error("Failed to initialize image data", e);
        }
    }
}