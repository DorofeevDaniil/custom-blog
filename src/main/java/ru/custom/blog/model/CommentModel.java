package ru.custom.blog.model;

import java.util.Objects;

public class CommentModel {
    private Long id;
    private Long postId;
    private String text;

    public CommentModel(Long id, Long postId, String text) {
        this.id = id;
        this.postId = postId;
        this.text = text;
    }

    public CommentModel() {
    }

    public Long getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public Long getPostId() {
        return this.postId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CommentModel)) return false;

        CommentModel pm = (CommentModel) o;

        return Objects.equals(id, pm.id)
            && Objects.equals(postId, pm.postId)
            && Objects.equals(text, pm.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, postId, text);
    }
}
