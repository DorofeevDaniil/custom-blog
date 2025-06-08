package ru.custom.blog.unit.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.*;
import ru.custom.blog.repository.CommentRepository;
import ru.custom.blog.service.CommentService;

@Configuration
@Profile("unit")
public class CommentsServiceUnitTestConfig {
    @Bean
    public CommentRepository mockCommentRepository() {
        return Mockito.mock(CommentRepository.class);
    }

    @Bean
    public CommentService commentService() {
        return new CommentService(mockCommentRepository());
    }
}
