package ru.custom.blog.unit.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import ru.custom.blog.repository.PostRepository;
import ru.custom.blog.service.ImageService;

@Configuration
@Profile("unit")
public class ImageServiceUnitTestConfig {
    @Bean
    public PostRepository mockPostRepository() {
        return Mockito.mock(PostRepository.class);
    }

    @Bean
    public ImageService mockImageService() {
        return new ImageService(mockPostRepository());
    }
}
