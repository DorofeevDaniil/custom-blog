package ru.custom.blog.integration.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfiguration.class, WebConfiguration.class})
@WebAppConfiguration
@ActiveProfiles("integration")
@TestPropertySource(locations = "classpath:test-application.properties")
class AddPostControllerIntegrationTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    private static final ArrayList<PostModel> IDLE_POSTS = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");


        IDLE_POSTS.clear();

        populatePosts();
        populateComments();
    }

    @AfterEach
    void tearDown() throws IOException {
        File imageDir = new File(webApplicationContext.getServletContext().getRealPath("/images"));
        if (imageDir.exists()) {
            Files.walk(imageDir.toPath())
                .map(Path::toFile)
                .sorted((a, b) -> -a.compareTo(b)) // сначала удаляем файлы, потом директории
                .forEach(File::delete);
        }
    }

    @Test
    void addPost_shouldAddPost() throws Exception {
        mockMvc.perform(get("/posts/add"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("add-post"))
            .andReturn();
    }

    @Test
    void redirectToPosts_shouldRedirectToPosts() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/1/edit"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("add-post"))
            .andExpect(model().attributeExists("post"))
            .andReturn();

        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        List<String> titles = doc.select("textarea").eachText();

        assertFalse(titles.isEmpty());
        assertEquals(IDLE_POSTS.get(0).getTitle(), titles.get(0));
    }

    private void populatePosts() {
        for(int i = 0; i < JdbcNativePostRepositoryTest.IDLE_TITLES.size(); i++) {
            KeyHolder keyHolder = new GeneratedKeyHolder();

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
            }, keyHolder);

            post.setId(keyHolder.getKey().longValue());
            IDLE_POSTS.add(post);
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
