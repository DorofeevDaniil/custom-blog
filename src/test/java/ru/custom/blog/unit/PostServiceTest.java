package ru.custom.blog.unit;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.repository.PostRepository;
import ru.custom.blog.service.CommentService;
import ru.custom.blog.service.ImageService;
import ru.custom.blog.service.PostService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = PostService.class)
class PostServiceTest {
    @MockitoBean(reset = MockReset.BEFORE)
    private PostRepository postRepository;

    @MockitoBean(reset = MockReset.BEFORE)
    private ImageService imageService;

    @MockitoBean(reset = MockReset.BEFORE)
    private CommentService commentService;

    @Autowired
    private PostService postService;

    private static final String IMAGE_NAME = "/tmp/images/test.jpg";

    private static final Long FIRST_ID = 1L;
    private static final Long SECOND_ID = 2L;

    @Test
    void getTotalPostsCount_success() {
        // Проверка вызова метода
        when(postRepository.getTotalCount()).thenReturn(FIRST_ID);

        // Выполнение метода
        postService.getTotalPostsCount();

        // Проверка вызовов
        verify(postRepository, times(1)).getTotalCount();
    }

    @Test
    void savePost_success() {
        PostModel mockPostModel = new PostModel();
        MultipartFile mockFile = createMultipart();
        String basePath = "/tmp";
        String expectedPath = IMAGE_NAME;

        // Проверка вызова метода
        when(postRepository.save(any(PostModel.class))).thenReturn(FIRST_ID);
        when(imageService.saveImage(mockFile, basePath)).thenReturn(expectedPath);

        // Выполнение метода
        Long result = postService.savePost(mockPostModel, mockFile, basePath);

        // Проверка вызовов
        assertEquals(FIRST_ID, result);
        verify(postRepository, times(1)).save(any(PostModel.class));
        verify(imageService, times(1)).saveImage(mockFile, basePath);
    }

    @Test
    void editPost_success() {
        PostModel mockPostModel = new PostModel();
        mockPostModel.setId(FIRST_ID);

        MultipartFile mockFile = createMultipart();
        String basePath = "/tmp";
        String expectedPath = IMAGE_NAME;

        // Проверка вызова метода
        doNothing().when(postRepository).update(any(PostModel.class));
        when(postRepository.findImageById(mockPostModel.getId())).thenReturn(Optional.of(expectedPath));
        when(imageService.saveImage(mockFile, basePath)).thenReturn(expectedPath);
        doNothing().when(imageService).removeImage(expectedPath);

        // Выполнение метода
        postService.editPost(mockPostModel, mockFile, basePath);

        // Проверка вызовов
        verify(postRepository, times(1)).findImageById(anyLong());
        verify(postRepository, times(1)).update(any(PostModel.class));
        verify(imageService, times(1)).removeImage(expectedPath);
        verify(imageService, times(1)).saveImage(mockFile, basePath);
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
        when(postRepository.findPage(10, 0)).thenReturn(List.of(firstMockPost, secondMockPost));
        when(commentService.getByPostId(firstMockPost.getId())).thenReturn(new ArrayList<>());
        when(commentService.getByPostId(secondMockPost.getId())).thenReturn(List.of(mockResponseComment));

        // Выполнение метода
        List<PostModel> posts = postService.getPage(1, 10);

        secondMockPost.setComments(List.of(mockResponseComment));

        // Проверка вызовов
        verify(postRepository, times(1)).findPage(10, 0);
        verify(commentService, times(2)).getByPostId(anyLong());

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
        when(postRepository.findPageByTag("tag", 10, 0)).thenReturn(List.of(firstMockPost, secondMockPost));
        when(commentService.getByPostId(firstMockPost.getId())).thenReturn(new ArrayList<>());
        when(commentService.getByPostId(secondMockPost.getId())).thenReturn(List.of(mockResponseComment));

        // Выполнение метода
        List<PostModel> posts = postService.getPageByTag("tag", 10);

        secondMockPost.setComments(List.of(mockResponseComment));

        // Проверка вызовов
        verify(postRepository, times(1)).findPageByTag(anyString(), anyInt(), anyInt());
        verify(commentService, times(2)).getByPostId(anyLong());

        assertEquals(2, posts.size());
        assertTrue(posts.contains(firstMockPost));
        assertTrue(posts.contains(secondMockPost));
    }

    @Test
    void getPost_successWhenPostExists() {
        PostModel mockPost = new PostModel();
        mockPost.setId(FIRST_ID);

        // Проверка вызова метода
        when(postRepository.findPostById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        when(commentService.getByPostId(mockPost.getId())).thenReturn(new ArrayList<>());

        // Выполнение метода
        PostModel post = postService.getPost(mockPost.getId());

        // Проверка вызовов
        verify(postRepository, times(1)).findPostById(anyLong());
        verify(commentService, times(1)).getByPostId(anyLong());
        assertEquals(mockPost, post);
    }

    @Test
    void getPost_emptyWhenPostDoesNotExists() {
        PostModel mockPost = new PostModel();

        // Проверка вызова метода
        when(postRepository.findPostById(FIRST_ID)).thenReturn(Optional.empty());
        when(commentService.getByPostId(mockPost.getId())).thenReturn(new ArrayList<>());

        // Выполнение метода
        PostModel post = postService.getPost(FIRST_ID);

        // Проверка вызовов
        verify(postRepository, times(1)).findPostById(anyLong());
        verify(commentService, times(1)).getByPostId(null);
        assertEquals(mockPost, post);
    }

    @Test
    void updateLikesCount_increment() {
        // Проверка вызова метода
        doNothing().when(postRepository).incrementLikesCount(FIRST_ID);

        // Выполнение метода
        postService.updateLikesCount(FIRST_ID, true);

        // Проверка вызовов
        verify(postRepository, times(1)).incrementLikesCount(anyLong());
    }

    @Test
    void updateLikesCount_decrement() {
        // Проверка вызова метода
        doNothing().when(postRepository).decrementLikesCount(FIRST_ID);

        // Выполнение метода
        postService.updateLikesCount(FIRST_ID, false);

        // Проверка вызовов
        verify(postRepository, times(1)).decrementLikesCount(anyLong());
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
