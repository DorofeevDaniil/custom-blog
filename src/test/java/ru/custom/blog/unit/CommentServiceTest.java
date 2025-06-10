package ru.custom.blog.unit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockReset;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.repository.CommentRepository;
import ru.custom.blog.service.CommentService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;


@SpringBootTest(classes = CommentService.class)
class CommentServiceTest {
    @MockitoBean(reset = MockReset.BEFORE)
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    private static final Long FIRST_ID = 1L;

    @Test
    void deleteAllPostComments_success() {
        // Проверка вызова метода
        doNothing().when(commentRepository).deleteByPostId(anyLong());

        // Выполнение метода
        commentService.deleteAllPostComments(FIRST_ID);

        // Проверка вызовов
        verify(commentRepository, times(1)).deleteByPostId(anyLong());
    }

    @Test
    void removeComment_success() {
        // Проверка вызова метода
        doNothing().when(commentRepository).deleteById(anyLong());

        // Выполнение метода
        commentService.removeComment(FIRST_ID);

        // Проверка вызовов
        verify(commentRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void saveComment_success() {
        CommentModel comment = new CommentModel();

        // Проверка вызова метода
        when(commentRepository.save(comment)).thenReturn(FIRST_ID);

        // Выполнение метода
        commentService.saveComment(comment);

        // Проверка вызовов
        verify(commentRepository, times(1)).save(any(CommentModel.class));
    }

    @Test
    void editComment_success() {
        CommentModel comment = new CommentModel();

        // Проверка вызова метода
        doNothing().when(commentRepository).update(comment);

        // Выполнение метода
        commentService.editComment(comment);

        // Проверка вызовов
        verify(commentRepository, times(1)).update(any(CommentModel.class));
    }

    @Test
    void getByPostId_success() {
        // Проверка вызова метода
        when(commentRepository.findByPostId(FIRST_ID)).thenReturn(anyList());

        // Выполнение метода
        commentService.getByPostId(FIRST_ID);

        // Проверка вызовов
        verify(commentRepository, times(1)).findByPostId(anyLong());
    }
}
