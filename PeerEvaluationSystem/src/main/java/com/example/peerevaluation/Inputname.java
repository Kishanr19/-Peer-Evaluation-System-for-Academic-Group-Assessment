package com.example.peerevaluation;
//[Driver: cl24929]
//[Navigator: hw24209]
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;

// Main class
public class Inputname {


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new LoginFrame();
            }
        });
    }
}
//[Driver: cl24929]
//[Navigator: hw24209]
// Login interface
class LoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;
    private static String filePath = "Group_Project\\PeerEvaluationSystem\\resources\\scores.xlsx";
    private HashMap<String, HashMap<String, double[]>> scoresMap = new HashMap<>();

    //[Driver: cl24929]
    //[Navigator: hw24209]
    public LoginFrame() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel userLabel = new JLabel("Username:");
        userField = new JTextField(15);

        JLabel passLabel = new JLabel("Password:");
        passField = new JPasswordField(15);

        loginButton = new JButton("Login");

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());

                if (username.equals("admin") && password.equals("admin")) {
                    new AdminFrame();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Incorrect username or password!");
                }
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);
        panel.add(new JLabel());
        panel.add(loginButton);

        add(panel);
        setVisible(true);
    }
    // Admin interface
    class AdminFrame extends JFrame {
        private JTextField nameField;
        private JTextField idField;
        private JButton addButton;
        private JButton proceedButton;

        private HashMap<String, String> studentMap = new HashMap<>();

        public AdminFrame() {
            setTitle("Admin - Add Students");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLocationRelativeTo(null);

            JLabel nameLabel = new JLabel("Student Name:");
            nameField = new JTextField(20);

            JLabel idLabel = new JLabel("Student ID:");
            idField = new JTextField(20);

            addButton = new JButton("Add Student");
            proceedButton = new JButton("Proceed to Score Input");

            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    addStudent();
                }
            });

            proceedButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (studentMap.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Please add at least one student before proceeding.");
                    } else {
                        new com.example.peerevaluation.ScoreInputFrame(studentMap); // 传递 studentMap 给独立类
                        dispose();
                    }
                }
            });

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(4, 2));
            panel.add(nameLabel);
            panel.add(nameField);
            panel.add(idLabel);
            panel.add(idField);
            panel.add(addButton);
            panel.add(proceedButton);

            add(panel);
            setVisible(true);
        }

        private void addStudent() {
            String name = nameField.getText().trim();
            String id = idField.getText().trim();

            if (name.isEmpty() || id.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter both name and ID.");
                return;
            }

            if (name.equals("12345")) {
                JOptionPane.showMessageDialog(null, "Format error: Invalid name!");
                return;
            }

            if (studentMap.containsKey(id)) {
                JOptionPane.showMessageDialog(null, "A student with this ID already exists!");
                return;
            }

            studentMap.put(id, name);

            JOptionPane.showMessageDialog(null, "Student added successfully!");

            // Clear the input fields for the next entry
            nameField.setText("");
            idField.setText("");
        }

        // Getter method for studentMap
        public HashMap<String, String> getStudentMap() {
            return studentMap;
        }
    }



    // (Driver:kr21130)
    //(Navigator:ka22205)
    // Method to collect student scores
    private void collectStudentScores() {
        HashMap<String, Integer> studentScores = new HashMap<>();
        int totalScore = 0;
        int studentCount = 0;

        while (true) {
            String studentName = JOptionPane.showInputDialog(null,
                    "Enter the student's name (or type 'done' to finish):");

            if (studentName == null || studentName.equalsIgnoreCase("done")) {
                break; // Exit if the user types 'done'
            }

            String scoreInput = JOptionPane.showInputDialog(null,
                    "Enter the score for " + studentName + ":");

            try {
                int score = Integer.parseInt(scoreInput);
                studentScores.put(studentName, score);
                totalScore += score;
                studentCount++;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid score input. Please enter a valid number.");
            }
        }
// (Driver:kr21130)
        //(Navigator:ka22205)
        // Calculate and display the grand score (average)
        double grandScore = (studentCount > 0) ? (double) totalScore / studentCount : 0;
        JOptionPane.showMessageDialog(null, "The grand score (average) is: " + grandScore);

        // Ask if the user wants to apply zero-sum scoring
        int applyZeroSum = JOptionPane.showConfirmDialog(null,
                "Do you want to apply zero-sum scoring?",
                "Zero-Sum Scoring", JOptionPane.YES_NO_OPTION);

        if (applyZeroSum == JOptionPane.YES_OPTION) {
            applyZeroSumScoring(studentScores, grandScore); // Apply zero-sum if chosen
        }

        peerMark(grandScore);
    }
    // (Driver:kr21130)
    //(Navigator:ka22205)

    // Method to apply zero-sum scoring
    private void applyZeroSumScoring(HashMap<String, Integer> studentScores, double grandScore) {
        // Zero-sum scoring ensures that the total score equals zero
        int totalScore = studentScores.values().stream().mapToInt(Integer::intValue).sum();
        int correction = -totalScore; // The correction to make the total sum zero

        // Apply the correction
        for (String student : studentScores.keySet()) {
            int originalScore = studentScores.get(student);
            studentScores.put(student, originalScore + correction / studentScores.size());
        }
        // Display the corrected scores
        StringBuilder scoresDisplay = new StringBuilder("Adjusted Scores (Zero-Sum):\n");
        for (String student : studentScores.keySet()) {
            scoresDisplay.append(student).append(": ").append(studentScores.get(student)).append("\n");
        }
        JOptionPane.showMessageDialog(null, scoresDisplay.toString());
// (Driver:kr21130)
        //(Navigator:ka22205)
        // Flag grand score if it's not between 3.0 and 3.5
        if (grandScore < 3.0 || grandScore > 3.5) {
            JOptionPane.showMessageDialog(null, "Warning: Grand score is out of range! (" + grandScore + ")");
        }

        normaliseScores(studentScores, grandScore);
        peerMark(grandScore);
    }
// [Driver:ka22205] [Navigator:kr21130]
    private void normaliseScores(HashMap<String, Integer> studentScores, double grandScore) {
        if (grandScore > 3.5) {
            JOptionPane.showMessageDialog(null, "Grand score exceeds 3.5, this will now be normalised to 3.5.");

            double adjustmentFactor = 3.5 / grandScore;

            for (String student : studentScores.keySet()) {
                int originalScore = studentScores.get(student);
                int normalizedScore = (int) Math.round(originalScore * adjustmentFactor);
                studentScores.put(student, normalizedScore);
            }

            StringBuilder scoresDisplay = new StringBuilder("Normalised Score:\n");
            for (String student : studentScores.keySet()) {
                scoresDisplay.append(student).append(": ").append(studentScores.get(student)).append("\n");
            }
            JOptionPane.showMessageDialog(null, scoresDisplay.toString());
        }
    }
    private void peerMark(double grandScore) {
        // Prompt user to enter a multiplier
        String multiplierInput = JOptionPane.showInputDialog(null,
                "Enter multiplier for the grand average score:");

        try {
            double multiplier = Double.parseDouble(multiplierInput);

            double peerMark = grandScore * multiplier;

            JOptionPane.showMessageDialog(null, "Your peer mark is: " + peerMark);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a valid multiplier.");
        }
    }

    public void getScoresMap() {
        for (String fromStudent : scoresMap.keySet()) {
            HashMap<String, double[]> toMap = scoresMap.get(fromStudent);
            System.out.println("From: " + fromStudent + "\n");
            for (String toStudent : toMap.keySet()) {
                double[] scores = toMap.get(toStudent);
                System.out.print("  To: " + toStudent + " Scores: ");
                for (double score : scores) {
                    System.out.print(score + " ");
                }
                System.out.println();
            }
        }
    }
}



//[Driver: cl24929]
//[Navigator: hw24209]
// Admin interface
class AdminFrame extends JFrame {
    private JTextField nameField;
    private JTextField idField;
    private JButton submitButton;

    private static HashMap<String, String> studentMap = new HashMap<>();

    public AdminFrame() {
        setTitle("Admin - Add Student");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel nameLabel = new JLabel("Student Name:");
        nameField = new JTextField(20);

        JLabel idLabel = new JLabel("Student ID:");
        idField = new JTextField(20);

        submitButton = new JButton("Submit");

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addStudent();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 2));
        panel.add(nameLabel);
        panel.add(nameField);
        panel.add(idLabel);
        panel.add(idField);
        panel.add(new JLabel());
        panel.add(submitButton);

        add(panel);
        setVisible(true);
    }

    private void addStudent() {
        String name = nameField.getText();
        String id = idField.getText();

        if (name.equals("12345")) {
            JOptionPane.showMessageDialog(null, "Format error: Invalid name!");
            return;
        }

        if (studentMap.containsKey(id) && studentMap.get(id).equals(name)) {
            JOptionPane.showMessageDialog(null, "The student name already exists!");
            return;
        }

        studentMap.put(id, name);

        JOptionPane.showMessageDialog(null, "Student added successfully!");

        JOptionPane.showMessageDialog(null, "Redirecting to the grade input function...");
        // Actual redirection code can be added here
    }
}
