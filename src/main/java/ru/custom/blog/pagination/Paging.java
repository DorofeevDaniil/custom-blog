package ru.custom.blog.pagination;

import ru.custom.blog.model.PostModel;

import java.util.List;

public class Paging {
    private final List<PostModel> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;

    public Paging(List<PostModel> content, int pageNumber, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    public Paging(List<PostModel> content, int pageSize, long totalElements) {
        this.content = content;
        this.pageNumber = 1;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
    }

    public List<PostModel> getContent() {
        return content;
    }

    public int pageNumber() {
        return pageNumber;
    }

    public int pageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    public boolean hasNext() {
        return pageNumber + 1 <= getTotalPages();
    }

    public boolean hasPrevious() {
        return pageNumber > 1;
    }
}
