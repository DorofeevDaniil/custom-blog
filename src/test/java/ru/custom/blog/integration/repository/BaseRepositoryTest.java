package ru.custom.blog.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.repository.CommentRepository;
import ru.custom.blog.repository.PostRepository;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
abstract class BaseRepositoryTest {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected CommentRepository commentRepository;

    protected static final ArrayList<PostModel> IDLE_POSTS = new ArrayList<>();
    protected static final ArrayList<CommentModel> IDLE_COMMENTS = new ArrayList<>();

    protected static final ArrayList<String> IDLE_TITLES = new ArrayList<>(Arrays.asList("title1", "title2", "title3"));
    protected static final ArrayList<String> IDLE_TEXT = new ArrayList<>(Arrays.asList("example string 1", "example string 2", "example string 3"));
    protected static final ArrayList<String> IDLE_IMAGE_PATH = new ArrayList<>(Arrays.asList("path1", "path2", "path3"));
    protected static final ArrayList<String> IDLE_TTAGS = new ArrayList<>(Arrays.asList("simple1 tag1", "simple2 tag2", "simple3 tag3"));

    protected static final Map<Long, String> IDLE_COMMENTS_DATA = Map.of(
        1L, "post 1 comment 1",
        2L, "post 2 comment 1",
        3L, "post 3 comment 1"
    );

    @BeforeEach
    void setUp() {
        // Очистка базы данных
        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");

        additionalSetup();
    }

    protected void additionalSetup() {
        // По умолчанию ничего не делает
    }

    protected void populatePosts() {
        for(int i = 0; i < IDLE_TITLES.size(); i++) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            PostModel post = new PostModel();
            post.setTitle(IDLE_TITLES.get(i));
            post.setText(IDLE_TEXT.get(i));
            post.setImagePath(IDLE_IMAGE_PATH.get(i));
            post.setLikesCount(1);
            post.setTags(Arrays.stream(IDLE_TTAGS.get(i).split(" ")).toList());

            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO posts(title, text, image_path, likes_count, tags) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

                statement.setString(1, post.getTitle());
                statement.setBlob(2, new ByteArrayInputStream(post.getText().getBytes(StandardCharsets.UTF_8)));
                statement.setString(3, post.getImagePath());
                statement.setInt(4, post.getLikesCount());
                statement.setString(5, post.getTagsAsText());


                return statement;
            }, keyHolder);

            post.setId(keyHolder.getKey().longValue());
            IDLE_POSTS.add(post);
        }
    }

    protected void populateComments() {
        for(Map.Entry<Long, String> entry : IDLE_COMMENTS_DATA.entrySet()) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            CommentModel comment = new CommentModel();
            comment.setPostId(entry.getKey());
            comment.setText(entry.getValue());

            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO comments(post_id, text) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);

                statement.setLong(1, comment.getPostId());
                statement.setString(2, comment.getText());


                return statement;
            }, keyHolder);

            comment.setId(keyHolder.getKey().longValue());
            IDLE_COMMENTS.add(comment);
        }
    }
}
