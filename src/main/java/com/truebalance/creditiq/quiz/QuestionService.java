package com.truebalance.creditiq.quiz;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.truebalance.creditiq.quiz.dto.QuestionDto;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class QuestionService {

    private List<Question> questions;

    @PostConstruct
    void loadQuestions() throws IOException {
        var mapper = new ObjectMapper();
        var resource = new ClassPathResource("questions.json");
        questions = mapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

    public List<Question> getAllWithAnswers() {
        return questions;
    }

    public List<QuestionDto> getAllForClient() {
        return questions.stream()
                .map(q -> new QuestionDto(q.id(), q.text(), q.options()))
                .toList();
    }

    public int score(List<com.truebalance.creditiq.quiz.dto.AnswerDto> answers) {
        int correct = 0;
        for (var answer : answers) {
            for (var question : questions) {
                if (question.id().equals(answer.questionId())
                        && question.correctIndex() == answer.selectedIndex()) {
                    correct++;
                    break;
                }
            }
        }
        return correct;
    }
}
