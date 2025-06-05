package ru.custom.blog.integration.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import ru.custom.blog.integration.configuration.DataSourceConfiguration;
import ru.custom.blog.model.PostModel;
import ru.custom.blog.repository.JdbcNativePostRepository;
import ru.custom.blog.repository.PostRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, JdbcNativePostRepository.class})
@TestPropertySource(locations = "classpath:test-application.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JdbcNativePostRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostRepository postRepository;

    public static final ArrayList<String> IDLE_TITLES = new ArrayList<>(Arrays.asList("title1", "title2", "title3"));
    public static final ArrayList<String> IDLE_TEXT = new ArrayList<>(Arrays.asList("example string 1", "example string 2", "example string 3"));
    public static final ArrayList<String> IDLE_IMAGE_PATH = new ArrayList<>(Arrays.asList("path1", "path2", "path3"));
    public static final ArrayList<String> IDLE_TTAGS = new ArrayList<>(Arrays.asList("simple1 tag1", "simple2 tag2", "simple3 tag3"));
    private static final ArrayList<PostModel> IDLE_POSTS = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Очистка базы данных
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");

        IDLE_POSTS.clear();

        populatePosts();
    }

    @Test
    void getTotalCount_shouldReturnPostsCount() {
        long postsCount = postRepository.getTotalCount();

        assertEquals(3, postsCount);
    }

    @Test
    void savePostById_shouldReturnPost() {
        PostModel post = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);

        assertNotNull(post);
        assertEquals(IDLE_POSTS.get(0), post);
    }

    @Test
    void save_shouldAddPost() {
        PostModel model = populateTestPost();
        long count = postRepository.getTotalCount();

        Long postId = postRepository.save(model);
        model.setId(postId);

        long updatedCount = postRepository.getTotalCount();
        PostModel savedPost = postRepository.findPostById(model.getId()).orElseGet(PostModel::new);

        assertEquals(count + 1, updatedCount);
        assertNotNull(savedPost);
        assertEquals(model, savedPost);
    }

    @Test
    void findPage_shouldReturnIdlePosts_reverseOrder() {
        List<PostModel> posts = postRepository.findPage(10, 0);

        posts.sort((o1, o2) -> Math.toIntExact(o1.getId() - o2.getId()));

        assertNotNull(posts);
        assertFalse(posts.isEmpty());

        for (int i = 0; i < IDLE_POSTS.size(); i++) {
            assertEquals(IDLE_POSTS.get(i), posts.get(i));
            assertEquals(IDLE_POSTS.get(i), posts.get(i));
            assertEquals(IDLE_POSTS.get(i), posts.get(i));
        }
    }

    @Test
    void findPageByTag_shouldReturnPostByTag() {
        List<PostModel> posts = postRepository.findPageByTag(IDLE_POSTS.get(0).getTags().get(0), 10, 0);

        assertNotNull(posts);
        assertFalse(posts.isEmpty());
        assertEquals(1, posts.size());
        assertEquals(IDLE_POSTS.get(0), posts.get(0));
    }

    @Test
    void update_shouldUpdatePost() {
        PostModel newPost = populateTestPost();
        newPost.setId(1L);

        postRepository.update(newPost);

        PostModel resultPost = postRepository.findPostById(newPost.getId()).orElseGet(PostModel::new);

        assertNotNull(resultPost.getId());
        assertEquals(newPost, resultPost);
    }

    @Test
    void findImageById_shouldReturnImage() {
        Optional<String> imagePath = postRepository.findImageById(IDLE_POSTS.get(0).getId());

        assertTrue(imagePath.isPresent());
        assertEquals(IDLE_POSTS.get(0).getImagePath(), imagePath.get());
    }

    @Test
    void incrementLikesCount_shouldAddLike() {
        PostModel idlePost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer idleLikesCount = idlePost.getLikesCount();

        postRepository.incrementLikesCount(IDLE_POSTS.get(0).getId());

        PostModel updatedPost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer resultLikesCount = updatedPost.getLikesCount();

        assertNotNull(resultLikesCount);
        assertEquals(idleLikesCount + 1, resultLikesCount);
    }

    @Test
    void decrementLikesCount_shouldAddLike() {
        PostModel idlePost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer idleLikesCount = idlePost.getLikesCount();

        postRepository.decrementLikesCount(IDLE_POSTS.get(0).getId());

        PostModel updatedPost = postRepository.findPostById(IDLE_POSTS.get(0).getId()).orElseGet(PostModel::new);
        Integer resultLikesCount = updatedPost.getLikesCount();

        assertNotNull(resultLikesCount);
        assertEquals(idleLikesCount - 1, resultLikesCount);
    }

    @Test
    void deleteById_shouldDeletePost() {
        long count = postRepository.getTotalCount();

        postRepository.deleteById(IDLE_POSTS.get(0).getId());

        long updatedCount = postRepository.getTotalCount();

        Optional<PostModel> deletedPost = postRepository.findPostById(IDLE_POSTS.get(0).getId());

        assertFalse(deletedPost.isPresent());
        assertEquals(count - 1, updatedCount);
    }

    private PostModel populateTestPost() {
        PostModel model = new PostModel();

        model.setTitle("test4");
        model.setText("example string 4");
        model.setImagePath("path4");
        model.setLikesCount(1);
        model.setTags(List.of("some4","tag4"));

        return model;
    }

    private void populatePosts() {
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
                    "insert into posts(title, text, image_path, likes_count, tags) values (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);

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
}
