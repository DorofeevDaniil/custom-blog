package ru.custom.blog.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PostModel {
    private Long id;
    private String title;
    private String imagePath;
    private Integer likesCount;
    private List<CommentModel> comments;
    private List<String> tags;
    private String text;

    public PostModel(Long id, String title, String text, String imagePath, Integer likesCount, List<String> tags) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.imagePath = imagePath;
        this.likesCount = likesCount;
        this.tags = tags;
    }

    public PostModel(String title, Integer likesCount, List<String> tags, String text) {
        this.title = title;
        this.likesCount = likesCount;
        this.tags = tags;
        this.text = text;
    }

    public PostModel(Long id, String title, List<String> tags, String text) {
        this.id = id;
        this.title = title;
        this.tags = tags;
        this.text = text;
    }

    public PostModel() {
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getImagePath() {
        return this.imagePath;
    }

    public Integer getLikesCount() {
        return this.likesCount != null ? this.likesCount : 0;
    }

    public List<CommentModel> getComments() {
        return this.comments != null ? this.comments : new ArrayList<>();
    }

    public List<String> getTags() {
        return this.tags;
    }

    public String getTagsAsText() {
        return String.join(" ", this.tags);
    }

    public String getTextPreview() {
        if (this.text == null) return "";

        return this.text.length() > 350 ? convertUrlsToLinks(this.text).substring(0, 350) + "..." : convertUrlsToLinks(this.text);
    }

    public String getText() {
        return this.text;
    }

    public List<String> getTextParts() {
        return Arrays.asList(convertUrlsToLinks(this.text).split("\\R", -1));
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setLikesCount(Integer likesCount) {
        this.likesCount = likesCount;
    }

    public void setComments(List<CommentModel> comments) {
        this.comments = comments;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String convertUrlsToLinks(String text) {
        return text.replaceAll(
            "(https?://[^\\s\"']+)",
            "<a href=\"$1\" target=\"_blank\">$1</a>"
        );
    }
}
