package org.example.expert.domain.todo.dto.response;

import lombok.Getter;

@Getter
public class TodoSearchResponse {

    private final String title;
    private final Long userCount;
    private final Long commentCount;

    public TodoSearchResponse(String title, Long userCount, Long commentCount) {
        this.title = title;
        this.userCount = userCount;
        this.commentCount = commentCount;
    }
}
