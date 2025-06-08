package ru.custom.blog.unit.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import ru.custom.blog.repository.PostRepository;
import ru.custom.blog.service.CommentService;
import ru.custom.blog.service.ImageService;
import ru.custom.blog.service.PostService;

@Configuration
@Profile("unit")
public class PostServiceUnitTestConfig {
    @Bean
    public PostRepository mockPostRepository() {
        return Mockito.mock(PostRepository.class);
    }

    @Bean
    public CommentService mockCommentService() {
        return Mockito.mock(CommentService.class);
    }

    @Bean
    public ImageService mockImageService() {
        return Mockito.mock(ImageService.class);
    }

    @Bean
    public PostService postService() {
        return new PostService(mockPostRepository(), mockCommentService(), mockImageService());
    }
}
