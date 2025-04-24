package com.quiz.com.quiz.entitys;

import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.persistence.Access;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Question {


    @Id
    private Long id;

    private QuestionType questionType;

    @Column(unique = true)
    private String name;

}
