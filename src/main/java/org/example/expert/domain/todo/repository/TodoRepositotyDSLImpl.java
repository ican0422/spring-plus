package org.example.expert.domain.todo.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.todo.entity.QTodo.todo;
import static org.example.expert.domain.user.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class TodoRepositotyDSLImpl implements TodoRepositoryDSL {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Todo> searchTasksInTodoList(
            String title,
            LocalDate startDate,
            LocalDate endDate,
            String nickname,
            Pageable pageable
    ) {
        List<Todo> results = jpaQueryFactory
                .selectFrom(todo)
                .leftJoin(todo.user, user)
                .where(eqTitle(title),
                        eqCreatedAt(startDate, endDate),
                        eqNickname(nickname))
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 조회 값이 null인 경우 0을 반환
        long total = Optional.ofNullable(
            jpaQueryFactory
                    .select(todo.count())
                    .from(todo)
                    .leftJoin(todo.user, user)
                    .where(eqTitle(title),
                            eqCreatedAt(startDate, endDate),
                            eqNickname(nickname))
                    .fetchOne()
        ).orElse(0L);
        return new PageImpl<>(results, pageable, total);
    }

    private BooleanExpression eqTitle(String title) {
        if (StringUtils.isEmpty(title)) {
            return null;
        }
        return todo.title.containsIgnoreCase(title);
    }

    private BooleanExpression eqCreatedAt(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return todo.createdAt.between(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        } else if (startDate != null) {
            return todo.createdAt.goe(startDate.atStartOfDay());
        } else if (endDate != null) {
            return todo.createdAt.loe(endDate.atTime(LocalTime.MAX));
        }
        return null;
    }

    private BooleanExpression eqNickname(String nickname) {
        if (StringUtils.isEmpty(nickname)) {
            return null;
        }
        return todo.user.nickname.containsIgnoreCase(nickname);
    }
}
