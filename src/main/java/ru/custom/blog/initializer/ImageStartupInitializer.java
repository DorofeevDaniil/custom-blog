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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class ImageStartupInitializer {

    private final PostService postService;
    private final ServletContext servletContext;

    private static final Map<String, String> INIT_FILES = Map.of(
        "spring", "Реализация Spring Framework API с нуля. Пошаговое руководство для начинающих. Часть 1",
        "zavod", "Лавандовый раф или стакан самогона: есть ли место на заводе хипстеру с макбуком",
        "worst-design", "Как сделать ужасный для пользователя интерфейс. Коллекция HTML/CSS лайфхаков" ,
        "integration-tests", "Как перестать беспокоиться и начать внедрять интеграционные тесты",
        "relativity", "Самое понятное объяснение Специальной теории относительности",
        "microservices", "Микросервисы в представлении среднего разработчика, и как всё на самом деле"
    );
    private static final Logger logger = LoggerFactory.getLogger(ImageStartupInitializer.class);

    public ImageStartupInitializer(PostService postService, ServletContext servletContext) {
        this.postService = postService;
        this.servletContext = servletContext;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void populatePosts(ContextRefreshedEvent event) {
        for (Map.Entry<String, String> fileEntry : INIT_FILES.entrySet()) {

            String targetImagePath = saveImage(fileEntry.getKey());

            String text = getFileText(fileEntry.getKey());

            PostModel post = new PostModel();
            post.setTitle(fileEntry.getValue());
            post.setText(text);
            post.setImagePath(targetImagePath);
            post.setLikesCount(0);
            post.setTags(List.of("init", "auto"));

            postService.savePost(post);
        }
    }

    private String getFileText(String textFileName) {
        StringBuilder builder = new StringBuilder();
        String resultFileName = textFileName + "-preview-demo.txt";

        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(
                new ClassPathResource("init_texts/" + resultFileName).getInputStream(),
                StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            logger.error(String.format("Failed to read text file %s. Got error: %s", resultFileName, e.getMessage()));
        }

        return builder.toString();
    }

    private String saveImage(String imageFileName) {
        String initImageFilePath = imageFileName + "-preview-demo.jpg";
        String basePath = servletContext.getRealPath("/");
        Path imageDir = Paths.get(basePath, "images");
        String resultImagePath = "";

        try {
            if (Files.notExists(imageDir)) {
                Files.createDirectories(imageDir);
            }

            Path targetImagePath = imageDir.resolve(initImageFilePath);
            resultImagePath = targetImagePath.toString();

            if (Files.notExists(targetImagePath)) {
                try (InputStream in = new ClassPathResource("init_images/" + initImageFilePath).getInputStream()) {
                    Files.copy(in, targetImagePath);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Failed to initialize image data. Got error: %s", e.getMessage()));
        }

        return resultImagePath;
    }
}