package org.example.expert.domain.todo.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.dto.response.TodoSearchResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.todo.repository.TodoRepositoryDSL;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoRepositoryDSL todoRepositoryDSL;
    private final WeatherClient weatherClient;

    @Transactional
    public TodoSaveResponse saveTodo(AuthUser authUser, TodoSaveRequest todoSaveRequest) {
        User user = User.fromAuthUser(authUser);

        String weather = weatherClient.getTodayWeather();

        Todo newTodo = new Todo(
                todoSaveRequest.getTitle(),
                todoSaveRequest.getContents(),
                weather,
                user
        );
        Todo savedTodo = todoRepository.save(newTodo);

        return new TodoSaveResponse(
                savedTodo.getId(),
                savedTodo.getTitle(),
                savedTodo.getContents(),
                weather,
                new UserResponse(user.getId(), user.getEmail())
        );
    }

    public Page<TodoResponse> getTodos(int page, int size, String weather, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Todo> todos = conditionalData(pageable, weather, startDate, endDate);

        return todos.map(todo -> new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(todo.getUser().getId(), todo.getUser().getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        ));
    }

    public Page<TodoSearchResponse> searchTodos(String title, LocalDate startDate, LocalDate endDate, String nickname, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Todo> todos = todoRepositoryDSL.searchTasksInTodoList(title, startDate, endDate, nickname, pageable);

        return todos.map(todo -> new TodoSearchResponse(
                todo.getTitle(),
                (long) todo.getManagers().size(),
                (long) todo.getComments().size()
        ));
    }

    public TodoResponse getTodo(long todoId) {
        Todo todo = todoRepository.findByIdWithUser(todoId)
                .orElseThrow(() -> new InvalidRequestException("Todo not found"));

        User user = todo.getUser();

        return new TodoResponse(
                todo.getId(),
                todo.getTitle(),
                todo.getContents(),
                todo.getWeather(),
                new UserResponse(user.getId(), user.getEmail()),
                todo.getCreatedAt(),
                todo.getModifiedAt()
        );
    }

    // 조건 별 데이터 가져오기
    private Page<Todo> conditionalData(Pageable pageable, String weather, LocalDate startDate, LocalDate endDate) {
        // 날씨 포함
        if (weather != null) {
            // 날씨가 포함된 수정 기간 검색 (시작 날짜만 포함)
            if (startDate != null && endDate == null) {
                return todoRepository.findByWeatherAndModifiedAtStartingWith(pageable, weather, startDate);
            }

            // 날씨가 포함된 수정 기간 검색 (엔드 날짜만 포함)
            if (startDate == null && endDate != null) {
                return todoRepository.findByWeatherAndModifiedAtLessThanEqual(pageable, weather, endDate);
            }

            // 날씨가 포함된 수정 기간 검색 (시작, 엔드 날짜 포함)
            if (startDate != null && endDate != null) {
                return todoRepository.findByWeatherAndModifiedAtBetween(pageable, weather, startDate, endDate);
            }

            return todoRepository.findAllByWeatherOrderByModifiedAtDesc(pageable, weather);
        }

        // 날씨 미포함
        // 수정 기간으로 검색 (시작 날짜만 포함)
        if (startDate != null && endDate == null) {
            return todoRepository.findByModifiedAtStartingWith(pageable, startDate);
        }
        // 수정 기간으로 검색 (엔드 날짜만 포함)
        if (startDate == null && endDate != null) {
            return todoRepository.findByModifiedAtLessThanEqual(pageable, endDate);
        }
        // 수정기간으로 검색 (시작, 엔드 날짜 포함)
        if (startDate != null && endDate != null) {
            return todoRepository.findByModifiedAtBetween(pageable, startDate, endDate);
        }

        // 전부 포함 안될때
        return todoRepository.findAllByOrderByModifiedAtDesc(pageable);
    }
}
