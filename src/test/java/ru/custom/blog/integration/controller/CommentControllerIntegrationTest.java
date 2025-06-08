package ru.custom.blog.integration.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.custom.blog.integration.configuration.DataSourceConfiguration;
import ru.custom.blog.integration.configuration.WebConfiguration;
import ru.custom.blog.integration.repository.JdbcNativeCommentRepositoryTest;
import ru.custom.blog.integration.repository.JdbcNativePostRepositoryTest;
import ru.custom.blog.model.CommentModel;
import ru.custom.blog.model.PostModel;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:test-application.properties")
class CommentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");

        populatePosts();
        populateComments();
    }

    @AfterEach
    void tearDown() throws IOException {
        File imageDir = new File(webApplicationContext.getServletContext().getRealPath("/images"));
        if (imageDir.exists()) {
            Files.walk(imageDir.toPath())
                .map(Path::toFile)
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(File::delete);
        }
    }

    @Test
    void handleAddComment_shouldAddComment() throws Exception {
        mockMvc.perform(post("/posts/1/comments")
                .param("text", "some test text"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void handleDeleteComment_shouldDeletePost() throws Exception {
        mockMvc.perform(post("/posts/1/comments/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/1"));
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

    private void populateComments() {
        for(Map.Entry<Long, String> entry : JdbcNativeCommentRepositoryTest.IDLE_COMMENTS_DATA.entrySet()) {

            CommentModel comment = new CommentModel();
            comment.setPostId(entry.getKey());
            comment.setText(entry.getValue());

            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    "insert into comments(post_id, text) values (?, ?)", Statement.RETURN_GENERATED_KEYS);

                statement.setLong(1, comment.getPostId());
                statement.setString(2, comment.getText());


                return statement;
            });
        }
    }
}
