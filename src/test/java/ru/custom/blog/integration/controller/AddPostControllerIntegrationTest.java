package ru.custom.blog.integration.controller;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import static org.junit.jupiter.api.Assertions.*;

class AddPostControllerIntegrationTest extends BaseControllerTest {
    @Override
    protected void additionalSetup() {
        IDLE_POSTS.clear();

        populatePosts();
        populateComments();
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
}
