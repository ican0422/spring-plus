package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TodoRepositoryDSL {
    Page<Todo> searchTasksInTodoList(String title,
                                     LocalDate startDate,
                                     LocalDate endDate,
                                     String nickname,
                                     Pageable pageable
    );
}
