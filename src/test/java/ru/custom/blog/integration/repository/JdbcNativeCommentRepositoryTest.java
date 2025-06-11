package ru.custom.blog.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class JdbcNativeCommentRepositoryTest extends BaseRepositoryTest {

    public static final Map<Long, String> IDLE_COMMENTS_DATA = Map.of(
        1L, "post 1 comment 1",
        2L, "post 2 comment 1",
        3L, "post 3 comment 1"
    );

    private static final ArrayList<CommentModel> IDLE_COMMENTS = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Очистка базы данных
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");

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

    private void populateComments() {
        for(Map.Entry<Long, String> entry : IDLE_COMMENTS_DATA.entrySet()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            CommentModel comment = new CommentModel();
            comment.setPostId(entry.getKey());
            comment.setText(entry.getValue());

            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    "insert into comments(post_id, text) values (?, ?)", Statement.RETURN_GENERATED_KEYS);

                statement.setLong(1, comment.getPostId());
                statement.setString(2, comment.getText());


                return statement;
            }, keyHolder);

            comment.setId(keyHolder.getKey().longValue());
            IDLE_COMMENTS.add(comment);
        }
    }

    private void populatePosts() {
        for(int i = 0; i < JdbcNativePostRepositoryTest.IDLE_TITLES.size(); i++) {
            PostModel post = new PostModel();
            post.setTitle(JdbcNativePostRepositoryTest.IDLE_TITLES.get(i));
            post.setText(JdbcNativePostRepositoryTest.IDLE_TEXT.get(i));
            post.setImagePath(JdbcNativePostRepositoryTest.IDLE_IMAGE_PATH.get(i));
            post.setLikesCount(1);
            post.setTags(Arrays.stream(JdbcNativePostRepositoryTest.IDLE_TTAGS.get(i).split(" ")).toList());

            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    "insert into posts(title, text, image_path, likes_count, tags) values (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, post.getTitle());
                statement.setBlob(2, new ByteArrayInputStream(post.getText().getBytes(StandardCharsets.UTF_8)));
                statement.setString(3, post.getImagePath());
                statement.setInt(4, post.getLikesCount());
                statement.setString(5, post.getTagsAsText());


                return statement;
            });
        }
    }
}
