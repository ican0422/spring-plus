package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.StringJoiner;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    @Query("SELECT t FROM Todo t LEFT JOIN FETCH t.user u ORDER BY t.modifiedAt DESC")
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    // 날씨
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE t.weather = :weather " +
            "ORDER BY t.modifiedAt DESC ")
    Page<Todo> findAllByWeatherOrderByModifiedAtDesc(
            Pageable pageable,
            @Param("weather") String weather
    );

    // 날씨 and 수정기간 (시작 날짜만)
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE t.weather = :weather AND DATE(t.modifiedAt) >= :startDate " +
            "ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByWeatherAndModifiedAtStartingWith(
            Pageable pageable,
            @Param("weather") String weather,
            @Param("startDate") LocalDate startDate
    );

    // 날씨 and 수정기간 (엔드 날짜만)
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE t.weather = :weather AND DATE(t.modifiedAt) <= :endDate " +
            "ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByWeatherAndModifiedAtLessThanEqual(
            Pageable pageable,
            @Param("weather") String weather,
            @Param("endDate") LocalDate endDate
    );

    // 날씨 and 수정기간 (시작, 엔드 포함)
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE t.weather = :weather AND DATE(t.modifiedAt) BETWEEN :startDate AND :endDate " +
            "ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByWeatherAndModifiedAtBetween(
            Pageable pageable,
            @Param("weather") String weather,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 수정기간 (시작 날짜만)
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE DATE(t.modifiedAt) >= :startDate " +
            "ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByModifiedAtStartingWith(
            Pageable pageable,
            @Param("startDate") LocalDate startDate
    );

    // 수정기간 (엔드 날짜만)
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE DATE(t.modifiedAt) <= :endDate " +
            "ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByModifiedAtLessThanEqual(
            Pageable pageable,
            @Param("endDate") LocalDate endDate
    );

    // 수정기간 (시작, 엔드)
    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN FETCH t.user u " +
            "WHERE DATE(t.modifiedAt) BETWEEN :startDate AND :endDate " +
            "ORDER BY t.modifiedAt DESC ")
    Page<Todo> findByModifiedAtBetween(
            Pageable pageable,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM Todo t " +
            "LEFT JOIN t.user " +
            "WHERE t.id = :todoId")
    Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId);
}
