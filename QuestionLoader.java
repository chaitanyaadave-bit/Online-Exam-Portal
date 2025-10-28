package com.exam.util;

import com.exam.model.Question;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionLoader {
    
    public static List<Question> loadQuestions(String filename, int count) throws Exception {
        List<Question> allQuestions = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String questionText = line.trim();
                String[] options = new String[4];
                
                for (int i = 0; i < 4; i++) {
                    options[i] = reader.readLine().trim();
                }
                
                int correctAnswer = Integer.parseInt(reader.readLine().trim());
                allQuestions.add(new Question(questionText, options, correctAnswer));
                
                reader.readLine(); // Skip blank line
            }
        }
        
        if (allQuestions.isEmpty()) {
            throw new Exception("No questions found in file");
        }
        
        Collections.shuffle(allQuestions);
        return allQuestions.subList(0, Math.min(count, allQuestions.size()));
    }
}