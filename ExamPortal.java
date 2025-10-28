package com.exam.ui;

import com.exam.model.Question;
import com.exam.util.QuestionLoader;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public class ExamPortal extends JFrame {
    private List<Question> questions;
    private String studentName, rollNumber;
    private int currentQ = 0, timeLeft = 300;
    private int[] answers;
    
    private JLabel qLabel, timerLabel, qNumLabel;
    private JRadioButton[] opts = new JRadioButton[4];
    private ButtonGroup group = new ButtonGroup();
    private JButton prevBtn, nextBtn, submitBtn;
    private Timer timer;
    
    public ExamPortal() {
        setTitle("Online Exam Portal");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        try {
            questions = QuestionLoader.loadQuestions("exam_questions.txt", 5);
            answers = new int[questions.size()];
            Arrays.fill(answers, -1);
            showLogin();
        } catch (Exception e) {
            error("Error loading questions: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void showLogin() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(10, 10, 10, 10);
        
        JLabel title = new JLabel("Online Exam Portal");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        g.gridwidth = 2;
        p.add(title, g);
        
        JTextField nameF = new JTextField(20);
        JTextField rollF = new JTextField(20);
        
        g.gridwidth = 1;
        g.gridy = 1;
        p.add(new JLabel("Name:"), g);
        g.gridx = 1;
        p.add(nameF, g);
        
        g.gridx = 0; g.gridy = 2;
        p.add(new JLabel("Roll Number:"), g);
        g.gridx = 1;
        p.add(rollF, g);
        
        JButton start = new JButton("Start Exam");
        start.addActionListener(e -> {
            String n = nameF.getText().trim(), r = rollF.getText().trim();
            if (n.isEmpty() || r.isEmpty()) { error("Enter both fields"); return; }
            if (!n.matches(".*[a-zA-Z].*")) { error("Name must contain letters"); return; }
            if (!r.matches("\\d+")) { error("Roll must be numeric"); return; }
            studentName = n; rollNumber = r;
            showExam();
        });
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        p.add(start, g);
        
        setContentPane(p);
        setVisible(true);
    }
    
    private void showExam() {
        JPanel main = new JPanel(new BorderLayout());
        
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(51, 102, 153));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        qNumLabel = new JLabel();
        qNumLabel.setForeground(Color.WHITE);
        timerLabel = new JLabel();
        timerLabel.setForeground(Color.WHITE);
        top.add(qNumLabel, BorderLayout.WEST);
        top.add(timerLabel, BorderLayout.EAST);
        
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        qLabel = new JLabel();
        qLabel.setFont(new Font("Arial", Font.BOLD, 16));
        center.add(qLabel);
        center.add(Box.createVerticalStrut(20));
        
        for (int i = 0; i < 4; i++) {
            opts[i] = new JRadioButton();
            group.add(opts[i]);
            int idx = i;
            opts[i].addActionListener(e -> answers[currentQ] = idx);
            center.add(opts[i]);
        }
        
        JPanel bottom = new JPanel();
        prevBtn = new JButton("Previous");
        nextBtn = new JButton("Next");
        submitBtn = new JButton("Submit Exam");
        
        prevBtn.addActionListener(e -> { if (currentQ > 0) { currentQ--; display(); }});
        nextBtn.addActionListener(e -> { if (currentQ < questions.size() - 1) { currentQ++; display(); }});
        submitBtn.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Submit exam?", "Confirm", 
                JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION) {
                timer.stop();
                showResults();
            }
        });
        
        bottom.add(prevBtn);
        bottom.add(nextBtn);
        bottom.add(submitBtn);
        
        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        main.add(bottom, BorderLayout.SOUTH);
        
        setContentPane(main);
        display();
        
        timer = new Timer(1000, e -> {
            timeLeft--;
            timerLabel.setText(String.format("Time: %02d:%02d", timeLeft / 60, timeLeft % 60));
            if (timeLeft <= 0) { timer.stop(); error("Time's up!"); showResults(); }
        });
        timer.start();
        revalidate();
    }
    
    private void display() {
        Question q = questions.get(currentQ);
        qNumLabel.setText("Question " + (currentQ + 1) + " of " + questions.size());
        qLabel.setText(q.getText());
        group.clearSelection();
        for (int i = 0; i < 4; i++) opts[i].setText((char)('A' + i) + ". " + q.getOptions()[i]);
        if (answers[currentQ] >= 0) opts[answers[currentQ]].setSelected(true);
        prevBtn.setEnabled(currentQ > 0);
        nextBtn.setEnabled(currentQ < questions.size() - 1);
    }
    
    private void showResults() {
        int score = 0;
        for (int i = 0; i < questions.size(); i++) 
            if (questions.get(i).isCorrect(answers[i])) score++;
        
        double pct = (score * 100.0) / questions.size();
        String grade = pct >= 90 ? "A+" : pct >= 80 ? "A" : pct >= 70 ? "B+" : 
                       pct >= 60 ? "B" : pct >= 50 ? "C" : "F";
        
        JPanel p = new JPanel(new BorderLayout());
        
        JPanel header = new JPanel();
        header.setBackground(new Color(51, 102, 153));
        JLabel h = new JLabel("Exam Results");
        h.setFont(new Font("Arial", Font.BOLD, 28));
        h.setForeground(Color.WHITE);
        header.add(h);
        
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));
        
        addLabel(center, studentName + " - " + rollNumber, 18);
        center.add(Box.createVerticalStrut(20));
        addLabel(center, "Score: " + score + "/" + questions.size(), 36);
        center.add(Box.createVerticalStrut(10));
        addLabel(center, String.format("%.0f%% - Grade: %s", pct, grade), 20);
        
        JPanel btns = new JPanel();
        JButton details = new JButton("View Details");
        JButton again = new JButton("New Exam");
        
        details.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Detailed Results:\n\n");
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);
                String ua = answers[i] >= 0 ? (char)('A' + answers[i]) + ". " + q.getOptions()[answers[i]] : "Not answered";
                String ca = (char)('A' + q.getCorrectAnswer()) + ". " + q.getOptions()[q.getCorrectAnswer()];
                sb.append(String.format("Q%d: %s\nYour: %s\nCorrect: %s\n%s\n\n",
                    i + 1, q.getText(), ua, ca, q.isCorrect(answers[i]) ? "Correct" : "Incorrect"));
            }
            JTextArea ta = new JTextArea(sb.toString());
            ta.setEditable(false);
            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(550, 350));
            JOptionPane.showMessageDialog(this, sp, "Details", JOptionPane.PLAIN_MESSAGE);
        });
        
        again.addActionListener(e -> { dispose(); new ExamPortal(); });
        
        btns.add(details);
        btns.add(again);
        
        p.add(header, BorderLayout.NORTH);
        p.add(center, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        
        setContentPane(p);
        revalidate();
    }
    
    private void addLabel(JPanel p, String text, int size) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Arial", Font.BOLD, size));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(l);
    }
    
    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ExamPortal());
    }
}