package com.quiz.com.quiz.repositorys;

import com.quiz.com.quiz.entitys.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Team findBySessionId(String sessionId); // Add this method
}