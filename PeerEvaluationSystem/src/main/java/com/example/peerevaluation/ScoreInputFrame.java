package com.example.peerevaluation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.IOException;

//[Driver: hz24472]
//[Navigator: zz23120]
public class ScoreInputFrame extends JFrame {
    private HashMap<String, HashMap<String, double[]>> scoresMap = new HashMap<>();


    private String[] criteria = {
            "Participated in group discussions or meetings",
            "Helped keep the group focused on the task",
            "Contributed useful ideas",
            "Quantity of work done",
            "Quality of work done"
    };

    public ScoreInputFrame(HashMap<String, String> studentMap) {
        setTitle("Input Scores");
        setSize(1820, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] studentNames = studentMap.values().toArray(new String[0]);
        ArrayList<Object[]> dataList = new ArrayList<>();

        for (String fromStudent : studentNames) {
            for (String toStudent : studentNames) {
                if (!fromStudent.equals(toStudent)) {
                    Object[] row = new Object[7];
                    row[0] = fromStudent;
                    row[1] = toStudent;
                    for (int i = 2; i < row.length; i++) {
                        row[i] = "";
                    }
                    dataList.add(row);
                }
            }
        }

        Object[][] data = dataList.toArray(new Object[0][]);
        String[] columnNames = {"From Student", "To Student", criteria[0], criteria[1], criteria[2], criteria[3], criteria[4]};

        JTable table = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 1;
            }
        };

        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        for (int i = 2; i < columnNames.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(260);
        }
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(1820, 900));

        JButton submitButton = new JButton("Submit Scores");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int row = 0; row < data.length; row++) {
                    String fromStudent = (String) data[row][0];
                    String toStudent = (String) data[row][1];
                    double[] scores = new double[5];

                    for (int i = 0; i < criteria.length; i++) {
                        Object value = table.getValueAt(row, i + 2);
                        try {
                            scores[i] = Double.parseDouble(value.toString());
                            if (scores[i] < 0 || scores[i] > 5) {
                                JOptionPane.showMessageDialog(null, "Score at row " + (row + 1) + ", column " + (i + 3) + " must be between 0 and 5.");
                                return;
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(null, "Invalid input at row " + (row + 1) + ", column " + (i + 3));
                            return;
                        }
                    }

                    scoresMap.computeIfAbsent(fromStudent, k -> new HashMap<>()).put(toStudent, scores);
                }
                JOptionPane.showMessageDialog(null, "Scores submitted successfully!");
                GetScoresMap();
            }
        });

        JButton calculateAverageButton = new JButton("Calculate Grand Average Score");
        calculateAverageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scoresMap.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please submit scores first!");
                    return;
                }

                HashMap<String, Double> averages = calculateGrandAverageScores();

                StringBuilder result = new StringBuilder("Grand Average Scores:\n");
                for (String student : averages.keySet()) {
                    result.append(student)
                            .append(": ")
                            .append(String.format("%.2f", averages.get(student)))
                            .append("\n");
                }

                JOptionPane.showMessageDialog(null, result.toString(),
                        "Grand Average Scores", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton calculatePeerMarkButton = new JButton("Calculate Peer Mark");
        calculatePeerMarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scoresMap.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please submit scores first!");
                    return;
                }

                String multiplierInput = JOptionPane.showInputDialog(
                        null,
                        "Please enter a multiplier:",
                        "Enter Multiplier",
                        JOptionPane.QUESTION_MESSAGE
                );

                if (multiplierInput == null || multiplierInput.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Multiplier input cancelled or invalid.");
                    return;
                }

                try {
                    double multiplier = Double.parseDouble(multiplierInput);
                    HashMap<String, Double> peerMarks = calculatePeerMarks(multiplier);

                    StringBuilder result = new StringBuilder("Peer Marks:\n");
                    for (String student : peerMarks.keySet()) {
                        result.append(student)
                                .append(": ")
                                .append(String.format("%.2f", peerMarks.get(student)))
                                .append("\n");
                    }

                    JOptionPane.showMessageDialog(null, result.toString(),
                            "Peer Marks", JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid multiplier. Please enter a numeric value.");
                }
            }
        });

        // [Driver: kr21130]
        // [Navigator: ka22205]
        JButton checkScoresButton = new JButton("Check Zero-Sum Scoring");
        checkScoresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (scoresMap.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Please submit scores first!");
                    return;
                }
                checkGrandAverageScores();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);
        buttonPanel.add(calculateAverageButton);
        buttonPanel.add(calculatePeerMarkButton);
        buttonPanel.add(checkScoresButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);
        add(panel);
        setVisible(true);
    }

    public HashMap<String, Double> calculateGrandAverageScores() {
        HashMap<String, Double> studentAverageScores = new HashMap<>();
        HashMap<String, Integer> scoreCounts = new HashMap<>();

        for (String fromStudent : scoresMap.keySet()) {
            HashMap<String, double[]> toMap = scoresMap.get(fromStudent);
            for (String toStudent : toMap.keySet()) {
                double[] scores = toMap.get(toStudent);
                double totalScore = 0;

                for (double score : scores) {
                    totalScore += score;
                }

                studentAverageScores.put(toStudent,
                        studentAverageScores.getOrDefault(toStudent, 0.0) + totalScore);
                scoreCounts.put(toStudent,
                        scoreCounts.getOrDefault(toStudent, 0) + scores.length);
            }
        }

        for (String student : studentAverageScores.keySet()) {
            studentAverageScores.put(student,
                    studentAverageScores.get(student) / scoreCounts.get(student));
        }

        return studentAverageScores;
    }

    public HashMap<String, Double> calculatePeerMarks(double multiplier) {
        HashMap<String, Double> grandAverages = calculateGrandAverageScores();
        HashMap<String, Double> peerMarks = new HashMap<>();

        for (String student : grandAverages.keySet()) {
            peerMarks.put(student, grandAverages.get(student) * multiplier);
        }

        return peerMarks;
    }


     // Checks if students' grand average scores comply with zero-sum scoring requirements
     // Zero-sum rule is violated only when scores exceed 3.5
     //[Driver: kr21130]
     //[Navigator: hw24209]

    private void checkGrandAverageScores() {
        HashMap<String, Double> grandAverages = calculateGrandAverageScores();

        StringBuilder scoresDisplay = new StringBuilder("Student Grand Averages:\n\n");
        boolean hasZeroSumViolation = false;
        boolean hasLowScores = false;
        double totalScore = 0;
        ArrayList<String> zeroSumViolations = new ArrayList<>();
        ArrayList<String> lowScores = new ArrayList<>();

        for (String student : grandAverages.keySet()) {
            double average = grandAverages.get(student);
            totalScore += average;

            scoresDisplay.append(student)
                    .append(": ")
                    .append(String.format("%.2f", average));

            if (average > 3.5) {
                scoresDisplay.append(" ⚠️"); // Warning indicator for zero-sum violation
                hasZeroSumViolation = true;
                zeroSumViolations.add(student);
            } else if (average < 3.0) {
                scoresDisplay.append(" ℹ️"); // Information indicator for low score
                hasLowScores = true;
                lowScores.add(student);
            }
            scoresDisplay.append("\n");
        }

        scoresDisplay.append("\nTotal sum of scores: ")
                .append(String.format("%.2f", totalScore))
                .append("\n");
//[Driver: kr21130]
        //[Navigator: hz24472]
        if (hasZeroSumViolation) {
            scoresDisplay.append("\nZERO-SUM SCORING METHOD VIOLATION!\n")
                    .append("The following students have scores above the maximum ")
                    .append("allowed value of 3.5:\n");

            for (String student : zeroSumViolations) {
                scoresDisplay.append("- ")
                        .append(student)
                        .append(": ")
                        .append(String.format("%.2f", grandAverages.get(student)))
                        .append("\n");
            }

            scoresDisplay.append("\nThis violates the zero-sum scoring method requirements. ")
                    .append("Please adjust these scores to be at most 3.5 before proceeding.");
        }
//[Driver: kr21130]
        //[Navigator: ka22205]
        if (hasLowScores) {
            scoresDisplay.append("\nNOTE: Some students have scores below 3.0:\n");

            for (String student : lowScores) {
                scoresDisplay.append("- ")
                        .append(student)
                        .append(": ")
                        .append(String.format("%.2f", grandAverages.get(student)))
                        .append("\n");
            }

            scoresDisplay.append("\nWhile these low scores don't violate the zero-sum method, ")
                    .append("you may want to review them for fairness.");
        }

        JOptionPane.showMessageDialog(null,
                scoresDisplay.toString(),
                "Zero-Sum Scoring Method Check",
                hasZeroSumViolation ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

    public HashMap<String, HashMap<String, double[]>> getScoresMap() {
        return scoresMap;
    }

    public void GetScoresMap() {
        String filePath = "PeerEvaluationSystem/data/scores.xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Scores");

        int rowNum = 0;

        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("From Student");
        headerRow.createCell(1).setCellValue("To Student");
        for (int i = 0; i < 5; i++) {
            headerRow.createCell(i + 2).setCellValue("Score " + (i + 1));
        }

        for (String fromStudent : scoresMap.keySet()) {
            HashMap<String, double[]> toMap = scoresMap.get(fromStudent);
            for (String toStudent : toMap.keySet()) {
                double[] scores = toMap.get(toStudent);
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(fromStudent);
                row.createCell(1).setCellValue(toStudent);

                for (int i = 0; i < scores.length; i++) {
                    row.createCell(2 + i).setCellValue(scores[i]);
                }
            }
        }

        for (int i = 0; i <= 6; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            workbook.close();
            JOptionPane.showMessageDialog(null, "Scores saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving scores to Excel file.");
        }
    }
}
