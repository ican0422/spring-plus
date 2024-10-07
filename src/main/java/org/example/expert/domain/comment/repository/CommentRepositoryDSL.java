package org.example.expert.domain.comment.repository;

import org.example.expert.domain.comment.entity.Comment;

import java.util.List;

public interface CommentRepositoryDSL {

    List<Comment> findByTodoIdWithUser(Long todoId);
//    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.todo.id = :todoId")
//    List<Comment> findByTodoIdWithUser(@Param("todoId") Long todoId);
}
