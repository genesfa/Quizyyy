package com.quiz.com.quiz.repositorys;

import com.quiz.com.quiz.entitys.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    // You can add custom query methods here if needed
    // For example, to find questions by type:
    // List<Question> findByQuestionType(QuestionType questionType);

    Optional<Question> findByName(String name);
}
