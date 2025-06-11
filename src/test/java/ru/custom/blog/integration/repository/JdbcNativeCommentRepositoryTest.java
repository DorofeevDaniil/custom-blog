package ru.custom.blog.integration.repository;

import org.junit.jupiter.api.Test;
import ru.custom.blog.model.CommentModel;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JdbcNativeCommentRepositoryTest extends BaseRepositoryTest {

    @Override
    protected void additionalSetup() {
        IDLE_COMMENTS.clear();

        populatePosts();
        populateComments();
    }

    @Test
    void findByPostId_shouldReturnComment() {
        List<CommentModel> comments = commentRepository.findByPostId(IDLE_COMMENTS.get(0).getPostId());

        assertEquals(1, comments.size());
        assertEquals(IDLE_COMMENTS.get(0), comments.get(0));
    }

    @Test
    void save_shouldSaveComment() {
        CommentModel comment = populateTestComment();
        int idleCount = commentRepository.findByPostId(comment.getPostId()).size();

        Long id = commentRepository.save(comment);
        comment.setId(id);

        List<CommentModel> comments = commentRepository.findByPostId(comment.getPostId());

        assertNotNull(comments);
        assertEquals(idleCount + 1, comments.size());
        assertTrue(comments.contains(comment));
    }

    @Test
    void update_shouldUpdateComment() {
        CommentModel comment = commentRepository.findByPostId(1L).get(0);

        commentRepository.update(comment);

        List<CommentModel> comments = commentRepository.findByPostId(comment.getPostId());

        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(comment, comments.get(0));
    }

    @Test
    void deleteById_shouldDeleteComment() {
        List<CommentModel> idleComments = commentRepository.findByPostId(1L);
        assertFalse(idleComments.isEmpty());

        commentRepository.deleteById(idleComments.get(0).getId());

        List<CommentModel> comments = commentRepository.findByPostId(idleComments.get(0).getPostId());

        assertTrue(comments.isEmpty());
    }

    @Test
    void deleteByPostId_shouldDeleteComment()  {
        List<CommentModel> idleComments = commentRepository.findByPostId(1L);
        assertFalse(idleComments.isEmpty());

        commentRepository.deleteByPostId(idleComments.get(0).getPostId());

        List<CommentModel> comments = commentRepository.findByPostId(idleComments.get(0).getPostId());

        assertTrue(comments.isEmpty());
    }

    private CommentModel populateTestComment() {
        CommentModel model = new CommentModel();

        model.setPostId(1L);
        model.setText("post 1 comment 2");

        return model;
    }
}
