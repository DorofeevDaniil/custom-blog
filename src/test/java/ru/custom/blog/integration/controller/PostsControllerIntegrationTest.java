package ru.custom.blog.integration.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
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
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

class PostsControllerIntegrationTest extends BaseControllerTest {

    private static final ArrayList<CommentModel> IDLE_COMMENTS = new ArrayList<>();
    private static final ArrayList<PostModel> IDLE_POSTS = new ArrayList<>();
    private static final String IMAGE_NAME = "/images/test-image.jpg";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM posts");

        jdbcTemplate.execute("ALTER TABLE posts ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");


        IDLE_COMMENTS.clear();
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
                .sorted((a, b) -> -a.compareTo(b))
                .forEach(File::delete);
        }
    }


    @Test
    void showPosts_shouldReturnHtmlWithPosts() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/html;charset=UTF-8"))
                    .andExpect(view().name("posts"))
                    .andExpect(model().attributeExists("paging"))
                    .andExpect(model().attributeExists("posts"))
                    .andReturn();

        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        Elements selectedOption = doc.select("select#pageSize option[selected]");
        List<String> titles = doc.select("h2").eachText();

        assertEquals("10", selectedOption.attr("value"));
        assertEquals(IDLE_POSTS.size(), titles.size());

        for (PostModel post : IDLE_POSTS) {
            assertTrue(titles.contains(post.getTitle()));
        }
    }

    @Test
    void showPosts_shouldReturnWithPageSize() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts")
                .param("pageNumber", "1")
                .param("pageSize", "5"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("posts"))
            .andExpect(model().attributeExists("paging"))
            .andExpect(model().attributeExists("posts"))
            .andReturn();

        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        Elements selectedOption = doc.select("select#pageSize option[selected]");
        List<String> titles = doc.select("h2").eachText();

        assertEquals("5", selectedOption.attr("value"));
        assertEquals(IDLE_POSTS.size(), titles.size());

        for (PostModel post : IDLE_POSTS) {
            assertTrue(titles.contains(post.getTitle()));
        }
    }

    @Test
    void showPosts_shouldSearchByTag() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts")
                .param("search", "tag2"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("posts"))
            .andExpect(model().attributeExists("paging"))
            .andExpect(model().attributeExists("posts"))
            .andReturn();

        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        Elements selectedOption = doc.select("select#pageSize option[selected]");
        List<String> titles = doc.select("h2").eachText();

        assertEquals("10", selectedOption.attr("value"));
        assertEquals(1, titles.size());
        assertEquals(IDLE_POSTS.get(1).getTitle(), titles.get(0));
    }

    @Test
    void redirectToPosts_shouldRedirectToPosts() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("posts"));
    }

    @Test
    void handleCreatePost_shouldAddPost() throws Exception {
        MockMultipartFile imageFile = createMultipart();

        mockMvc.perform(multipart("/posts")
                .file(imageFile)
                .param("title", "new title")
                .param("tags", "some newTag")
                .param("text", "some test text"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/" + (IDLE_POSTS.size() + 1)));
    }

    @Test
    void handleUpdatePost_shouldUpdatePost() throws Exception {
        MockMultipartFile imageFile = createMultipart();

        mockMvc.perform(multipart("/posts/1")
                .file(imageFile)
                .param("title", "new title")
                .param("tags", "some newTag")
                .param("text", "some test text"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void showPost_shouldReturnPost() throws Exception {
        MvcResult result = mockMvc.perform(get("/posts/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/html;charset=UTF-8"))
            .andExpect(view().name("post"))
            .andExpect(model().attributeExists("post"))
            .andReturn();

        String html = result.getResponse().getContentAsString();
        Document doc = Jsoup.parse(html);

        Elements comments = doc.select("span[id^=comment]");
        List<String> titles = doc.select("h2").eachText();

        assertFalse(titles.isEmpty());
        assertEquals(IDLE_POSTS.get(0).getTitle(), titles.get(0));
        assertEquals(1, comments.size());
        assertEquals(
            IDLE_COMMENTS
                .stream()
                .filter(c -> c.getPostId() == 1L)
                .findFirst().get().getText(),
            comments.get(0).text());
    }

    @Test
    void handleLikePost_shouldLikePost() throws Exception {
        mockMvc.perform(post("/posts/1/like")
                .param("like", "true"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void handleLikePost_shouldDislikePost() throws Exception {
        mockMvc.perform(post("/posts/1/like")
                .param("like", "false"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts/1"));
    }

    @Test
    void handleDeletePost_shouldDeletePost() throws Exception {
        mockMvc.perform(post("/posts/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/posts"));
    }

    private MockMultipartFile createMultipart() throws IOException {
        return new MockMultipartFile(
            "image",
            "test-image.png",
            "image/png",
            new ClassPathResource(IMAGE_NAME).getInputStream()
        );
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
                    "insert into posts(title, text, image_path, likes_count, tags) values (?, ?, ?, ?, ?)", new String[] {"id"});

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
            KeyHolder keyHolder = new GeneratedKeyHolder();

            CommentModel comment = new CommentModel();
            comment.setPostId(entry.getKey());
            comment.setText(entry.getValue());

            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(
                    "insert into comments(post_id, text) values (?, ?)", new String[] {"id"});

                statement.setLong(1, comment.getPostId());
                statement.setString(2, comment.getText());


                return statement;
            }, keyHolder);

            comment.setId(keyHolder.getKey().longValue());
            IDLE_COMMENTS.add(comment);
        }
    }
}
