package ru.custom.blog.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.custom.blog.integration.configuration.DataSourceConfiguration;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.repository.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, JdbcNativeCommentRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class JdbcNativeCommentRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;

    public static final Map<Long, String> IDLE_COMMENTS_DATA = Map.of(
        1L, "post 1 comment 1",
        2L, "post 2 comment 1",
        3L, "post 3 comment 1"
    );
    public static final ArrayList<CommentModel> IDLE_COMMENTS = new ArrayList<>();

    public static final ArrayList<String> IDLE_TITLES = new ArrayList<>(Arrays.asList("title1", "title2", "title3"));
    public static final ArrayList<String> IDLE_TEXT = new ArrayList<>(Arrays.asList("example string 1", "example string 2", "example string 3"));
    public static final ArrayList<String> IDLE_IMAGE_PATH = new ArrayList<>(Arrays.asList("path1", "path2", "path3"));
    public static final ArrayList<String> IDLE_TTAGS = new ArrayList<>(Arrays.asList("simple1 tag1", "simple2 tag2", "simple3 tag3"));

    @BeforeEach
    void setUp() {
        // Очистка базы данных
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");
        IDLE_COMMENTS.clear();

        for(int i = 0; i < IDLE_TITLES.size(); i++) {
            PostModel post = new PostModel();
            post.setTitle(IDLE_TITLES.get(i));
            post.setText(IDLE_TEXT.get(i));
            post.setImagePath(IDLE_IMAGE_PATH.get(i));
            post.setLikesCount(1);
            post.setTags(Arrays.stream(IDLE_TTAGS.get(i).split(" ")).toList());

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
