package com.quiz.com.quiz.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.com.quiz.entitys.Question;
import com.quiz.com.quiz.repositorys.QuestionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader {

    private final QuestionRepository questionRepository;

    public DataLoader(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public void initializeData() {
        loadQuestions(); // Ensure questions are loaded first
        List<Question> questions = questionRepository.findAll(); // Load all questions from the database
        System.out.println("Total questions in the database: " + questions.size());
        for (Question question : questions) {
            System.out.println("Question in DB: " + question.getName());
        }
    }


    public void loadQuestions() {
        System.out.println("Loading questions...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("question.json");
            if (inputStream == null) {
                System.err.println("Resource 'question.json' not found in the classpath.");
                return;
            }
            List<Question> questions = mapper.readValue(inputStream, new TypeReference<List<Question>>() {});
            for (Question question : questions) {
                questionRepository.findByName(question.getName())
                        .orElseGet(() -> {
                            System.out.println("Saving question: " + question.getName());
                            return questionRepository.save(question);
                        });
            }

            // Load all questions from the database to verify
            List<Question> savedQuestions = questionRepository.findAll();
            System.out.println("Total questions in the database: " + savedQuestions.size());
            for (Question savedQuestion : savedQuestions) {
                System.out.println("Question in DB: " + savedQuestion.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
