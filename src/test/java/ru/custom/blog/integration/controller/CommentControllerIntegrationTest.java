package ru.custom.blog.integration.controller;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerIntegrationTest extends BaseControllerTest {

    @Override
    protected void additionalSetup() {
        populatePosts();
        populateComments();
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
}
