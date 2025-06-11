package ru.custom.blog.integration.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import ru.custom.blog.model.PostModel;
import java.io.IOException;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

class PostsControllerIntegrationTest extends BaseControllerTest {
    private static final String IMAGE_NAME = "/images/test-image.jpg";

    @Override
    protected void additionalSetup() {
        IDLE_COMMENTS.clear();
        IDLE_POSTS.clear();

        populatePosts();
        populateComments();
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
}
