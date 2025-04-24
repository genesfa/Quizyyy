package com.quiz.com.quiz.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quiz.com.quiz.entitys.Question;
import com.quiz.com.quiz.repositorys.QuestionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader {

    private final QuestionRepository questionRepository;

    public DataLoader(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @PostConstruct
    public void loadQuestions() {
        System.out.println("Loading questions...");
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = getClass().getResourceAsStream("./question.json");
            List<Question> questions = mapper.readValue(inputStream, new TypeReference<List<Question>>() {});
            for (Question question : questions) {
                questionRepository.findByName(question.getName())
                        .orElseGet(() -> questionRepository.save(question));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
