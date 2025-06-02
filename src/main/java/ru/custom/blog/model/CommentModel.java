package ru.custom.blog.model;

public class CommentModel {
    private Long id;
    private String text;

    public CommentModel(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Long getId() {
        return this.id;
    }

    public String getText() {
        return this.text;
    }

    public void settId(Long id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }
}
