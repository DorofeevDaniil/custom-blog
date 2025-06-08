package ru.custom.blog.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.repository.PostRepository;
import ru.custom.blog.service.CommentService;
import ru.custom.blog.service.ImageService;
import ru.custom.blog.service.PostService;
import ru.custom.blog.unit.configuration.PostServiceUnitTestConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("unit")
@ContextConfiguration(classes = {PostServiceUnitTestConfig.class})
class PostServiceTest {
    @Autowired
    private PostRepository mockPostRepository;

    @Autowired
    private ImageService mockImageService;

    @Autowired
    private CommentService mockCommentService;

    @Autowired
    private PostService postService;

    private static final String IMAGE_NAME = "/tmp/images/test.jpg";

    private static final Long FIRST_ID = 1L;
    private static final Long SECOND_ID = 2L;

    @BeforeEach
    void resetMocks() {
        reset(mockPostRepository);
        reset(mockImageService);
        reset(mockCommentService);
    }

    @Test
    void getTotalPostsCount_success() {
        // Проверка вызова метода
        when(mockPostRepository.getTotalCount()).thenReturn(FIRST_ID);

        // Выполнение метода
        postService.getTotalPostsCount();

        // Проверка вызовов
        verify(mockPostRepository, times(1)).getTotalCount();
    }

    @Test
    void savePost_success() {
        PostModel mockPostModel = new PostModel();
        MultipartFile mockFile = createMultipart();
        String basePath = "/tmp";
        String expectedPath = IMAGE_NAME;

        // Проверка вызова метода
        when(mockPostRepository.save(any(PostModel.class))).thenReturn(FIRST_ID);
        when(mockImageService.saveImage(mockFile, basePath)).thenReturn(expectedPath);

        // Выполнение метода
        Long result = postService.savePost(mockPostModel, mockFile, basePath);

        // Проверка вызовов
        assertEquals(FIRST_ID, result);
        verify(mockPostRepository, times(1)).save(any(PostModel.class));
        verify(mockImageService, times(1)).saveImage(mockFile, basePath);
    }

    @Test
    void editPost_success() {
        PostModel mockPostModel = new PostModel();
        mockPostModel.setId(FIRST_ID);

        MultipartFile mockFile = createMultipart();
        String basePath = "/tmp";
        String expectedPath = IMAGE_NAME;

        // Проверка вызова метода
        doNothing().when(mockPostRepository).update(any(PostModel.class));
        when(mockPostRepository.findImageById(mockPostModel.getId())).thenReturn(Optional.of(expectedPath));
        when(mockImageService.saveImage(mockFile, basePath)).thenReturn(expectedPath);
        doNothing().when(mockImageService).removeImage(expectedPath);

        // Выполнение метода
        postService.editPost(mockPostModel, mockFile, basePath);

        // Проверка вызовов
        verify(mockPostRepository, times(1)).findImageById(anyLong());
        verify(mockPostRepository, times(1)).update(any(PostModel.class));
        verify(mockImageService, times(1)).removeImage(expectedPath);
        verify(mockImageService, times(1)).saveImage(mockFile, basePath);
    }

    @Test
    void getPage_success() {
        PostModel firstMockPost = new PostModel();
        firstMockPost.setId(FIRST_ID);
        firstMockPost.setComments(new ArrayList<>());

        PostModel secondMockPost = new PostModel();
        secondMockPost.setId(SECOND_ID);
        secondMockPost.setComments(new ArrayList<>());

        String mockText = "test text";

        CommentModel mockResponseComment = new CommentModel();
        mockResponseComment.setId(FIRST_ID);
        mockResponseComment.setPostId(secondMockPost.getId());
        mockResponseComment.setText(mockText);

        // Проверка вызова метода
        when(mockPostRepository.findPage(10, 0)).thenReturn(List.of(firstMockPost, secondMockPost));
        when(mockCommentService.getByPostId(firstMockPost.getId())).thenReturn(new ArrayList<>());
        when(mockCommentService.getByPostId(secondMockPost.getId())).thenReturn(List.of(mockResponseComment));

        // Выполнение метода
        List<PostModel> posts = postService.getPage(1, 10);

        secondMockPost.setComments(List.of(mockResponseComment));

        // Проверка вызовов
        verify(mockPostRepository, times(1)).findPage(10, 0);
        verify(mockCommentService, times(2)).getByPostId(anyLong());

        assertEquals(2, posts.size());
        assertTrue(posts.contains(firstMockPost));
        assertTrue(posts.contains(secondMockPost));
    }

    @Test
    void getPageByTag_success() {
        PostModel firstMockPost = new PostModel();
        firstMockPost.setId(FIRST_ID);
        firstMockPost.setComments(new ArrayList<>());

        PostModel secondMockPost = new PostModel();
        secondMockPost.setId(SECOND_ID);
        secondMockPost.setComments(new ArrayList<>());

        String mockText = "test text";

        CommentModel mockResponseComment = new CommentModel();
        mockResponseComment.setId(FIRST_ID);
        mockResponseComment.setPostId(secondMockPost.getId());
        mockResponseComment.setText(mockText);

        // Проверка вызова метода
        when(mockPostRepository.findPageByTag("tag", 10, 0)).thenReturn(List.of(firstMockPost, secondMockPost));
        when(mockCommentService.getByPostId(firstMockPost.getId())).thenReturn(new ArrayList<>());
        when(mockCommentService.getByPostId(secondMockPost.getId())).thenReturn(List.of(mockResponseComment));

        // Выполнение метода
        List<PostModel> posts = postService.getPageByTag("tag", 10);

        secondMockPost.setComments(List.of(mockResponseComment));

        // Проверка вызовов
        verify(mockPostRepository, times(1)).findPageByTag(anyString(), anyInt(), anyInt());
        verify(mockCommentService, times(2)).getByPostId(anyLong());

        assertEquals(2, posts.size());
        assertTrue(posts.contains(firstMockPost));
        assertTrue(posts.contains(secondMockPost));
    }

    @Test
    void getPost_successWhenPostExists() {
        PostModel mockPost = new PostModel();
        mockPost.setId(FIRST_ID);

        // Проверка вызова метода
        when(mockPostRepository.findPostById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        when(mockCommentService.getByPostId(mockPost.getId())).thenReturn(new ArrayList<>());

        // Выполнение метода
        PostModel post = postService.getPost(mockPost.getId());

        // Проверка вызовов
        verify(mockPostRepository, times(1)).findPostById(anyLong());
        verify(mockCommentService, times(1)).getByPostId(anyLong());
        assertEquals(mockPost, post);
    }

    @Test
    void getPost_emptyWhenPostDoesNotExists() {
        PostModel mockPost = new PostModel();

        // Проверка вызова метода
        when(mockPostRepository.findPostById(FIRST_ID)).thenReturn(Optional.empty());
        when(mockCommentService.getByPostId(mockPost.getId())).thenReturn(new ArrayList<>());

        // Выполнение метода
        PostModel post = postService.getPost(FIRST_ID);

        // Проверка вызовов
        verify(mockPostRepository, times(1)).findPostById(anyLong());
        verify(mockCommentService, times(1)).getByPostId(null);
        assertEquals(mockPost, post);
    }

    @Test
    void updateLikesCount_increment() {
        // Проверка вызова метода
        doNothing().when(mockPostRepository).incrementLikesCount(FIRST_ID);

        // Выполнение метода
        postService.updateLikesCount(FIRST_ID, true);

        // Проверка вызовов
        verify(mockPostRepository, times(1)).incrementLikesCount(anyLong());
    }

    @Test
    void updateLikesCount_decrement() {
        // Проверка вызова метода
        doNothing().when(mockPostRepository).decrementLikesCount(FIRST_ID);

        // Выполнение метода
        postService.updateLikesCount(FIRST_ID, false);

        // Проверка вызовов
        verify(mockPostRepository, times(1)).decrementLikesCount(anyLong());
    }

    private MockMultipartFile createMultipart() {
        return new MockMultipartFile(
            "image",
            "test-image.png",
            "image/png",
            "test content".getBytes()
        );
    }
}
