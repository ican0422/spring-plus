package org.example.expert.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.comment.entity.Comment;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.example.expert.domain.comment.entity.QComment.comment;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryDSL {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<Comment> findByTodoIdWithUser(Long todoId) {
        return jpaQueryFactory.selectFrom(comment)
                .join(comment.user)
                .fetchJoin()
                .where(comment.todo.id.eq(todoId))
                .stream().toList();
    }
}
