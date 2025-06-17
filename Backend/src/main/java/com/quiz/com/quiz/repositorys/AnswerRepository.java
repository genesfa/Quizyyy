package com.quiz.com.quiz.repositorys;

import com.quiz.com.quiz.entitys.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import com.quiz.com.quiz.entitys.Question;
import java.util.List;
import com.quiz.com.quiz.entitys.Team;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByQuestion(Question question);
    Answer findByTeamAndQuestion(Team team, Question question); // Find an answer by team and question
}