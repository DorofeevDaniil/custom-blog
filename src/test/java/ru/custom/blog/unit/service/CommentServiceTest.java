package ru.custom.blog.unit.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.repository.CommentRepository;
import ru.custom.blog.service.CommentService;
import ru.custom.blog.unit.configuration.CommentsServiceUnitTestConfig;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@ExtendWith(SpringExtension.class)
@ActiveProfiles("unit")
@ContextConfiguration(classes = CommentsServiceUnitTestConfig.class)
class CommentServiceTest {
    @Autowired
    private CommentRepository mockCommentRepository;

    @Autowired
    private CommentService commentService;

    private static final Long FIRST_ID = 1L;

    @BeforeEach
    void resetMocks() {
        reset(mockCommentRepository);
    }

    @Test
    void deleteAllPostComments_success() {
        // Проверка вызова метода
        doNothing().when(mockCommentRepository).deleteByPostId(anyLong());

        // Выполнение метода
        commentService.deleteAllPostComments(FIRST_ID);

        // Проверка вызовов
        verify(mockCommentRepository, times(1)).deleteByPostId(anyLong());
    }

    @Test
    void removeComment_success() {
        // Проверка вызова метода
        doNothing().when(mockCommentRepository).deleteById(anyLong());

        // Выполнение метода
        commentService.removeComment(FIRST_ID);

        // Проверка вызовов
        verify(mockCommentRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void saveComment_success() {
        CommentModel comment = new CommentModel();

        // Проверка вызова метода
        when(mockCommentRepository.save(comment)).thenReturn(FIRST_ID);

        // Выполнение метода
        commentService.saveComment(comment);

        // Проверка вызовов
        verify(mockCommentRepository, times(1)).save(any(CommentModel.class));
    }

    @Test
    void editComment_success() {
        CommentModel comment = new CommentModel();

        // Проверка вызова метода
        doNothing().when(mockCommentRepository).update(comment);

        // Выполнение метода
        commentService.editComment(comment);

        // Проверка вызовов
        verify(mockCommentRepository, times(1)).update(any(CommentModel.class));
    }

    @Test
    void getByPostId_success() {
        // Проверка вызова метода
        when(mockCommentRepository.findByPostId(FIRST_ID)).thenReturn(anyList());

        // Выполнение метода
        commentService.getByPostId(FIRST_ID);

        // Проверка вызовов
        verify(mockCommentRepository, times(1)).findByPostId(anyLong());
    }
}
