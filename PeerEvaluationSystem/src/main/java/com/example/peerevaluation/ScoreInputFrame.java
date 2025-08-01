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
                            JOptionPane.showMessageDialog(null, "Invalid input at row " + (row + 1)+", column " + (i + 3));
                            return;
                        }
                    }

                    scoresMap.computeIfAbsent(fromStudent, k -> new HashMap<>()).put(toStudent, scores);
                }
                JOptionPane.showMessageDialog(null, "Scores submitted successfully!");
                GetScoresMap();
            }
        });

        //[Driver: ka22205]
        //[Navigator: ep20200]
        JButton calculateAverageButton = new JButton("Calculate Grand Average Score");
        calculateAverageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                        studentAverageScores.put(toStudent, studentAverageScores.getOrDefault(toStudent, 0.0) + totalScore);
                        scoreCounts.put(toStudent, scoreCounts.getOrDefault(toStudent, 0) + scores.length);
                    }
                }

                StringBuilder result = new StringBuilder("Grand Average Scores:\n");
                for (String student : studentAverageScores.keySet()) {
                    double average = studentAverageScores.get(student) / scoreCounts.get(student);
                    result.append(student).append(": ").append(String.format("%.2f", average)).append("\n");
                }

                JOptionPane.showMessageDialog(null, result.toString(), "Grand Average Scores", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        //[Driver: ka22205]
        //[Navigator: ep20200]
        JButton calculatePeerMarkButton = new JButton("Calculate Peer Mark");
        calculatePeerMarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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

                        studentAverageScores.put(toStudent, studentAverageScores.getOrDefault(toStudent, 0.0) + totalScore);
                        scoreCounts.put(toStudent, scoreCounts.getOrDefault(toStudent, 0) + scores.length);
                    }
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

                    StringBuilder result = new StringBuilder("Peer Marks:\n");
                    for (String student : studentAverageScores.keySet()) {
                        double average = studentAverageScores.get(student) / scoreCounts.get(student);
                        double peerMark = average * multiplier;
                        result.append(student).append(": ").append(String.format("%.2f", peerMark)).append("\n");
                    }

                    JOptionPane.showMessageDialog(null, result.toString(), "Peer Marks", JOptionPane.INFORMATION_MESSAGE);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid multiplier. Please enter a numeric value.");
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);
        buttonPanel.add(calculateAverageButton);
        buttonPanel.add(calculatePeerMarkButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    //[Driver: ka22205]
    //[Navigator: ep20200]
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

                studentAverageScores.put(toStudent, studentAverageScores.getOrDefault(toStudent, 0.0) + totalScore);
                scoreCounts.put(toStudent, scoreCounts.getOrDefault(toStudent, 0) + scores.length);
            }
        }

        for (String student : studentAverageScores.keySet()) {
            studentAverageScores.put(student, studentAverageScores.get(student) / scoreCounts.get(student));
        }

        return studentAverageScores;
    }

    //[Driver: ka22205]
    //[Navigator: ep20200]
    public HashMap<String, Double> calculatePeerMarks(double multiplier) {
        HashMap<String, Double> grandAverages = calculateGrandAverageScores();
        HashMap<String, Double> peerMarks = new HashMap<>();

        for (String student : grandAverages.keySet()) {
            peerMarks.put(student, grandAverages.get(student) * multiplier);
        }

        return peerMarks;
    }
    

    public HashMap<String, HashMap<String, double[]>> getScoresMap() {
        return scoresMap;
    }

    //[Driver: hw24209]
    //[Navigator: cl24929]
    public void GetScoresMap() {
        String filePath = "PeerEvaluationSystem/data/scores.xlsx";
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Scores");
    
        int rowNum = 0;
    
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.createCell(0).setCellValue("From Student");
        headerRow.createCell(1).setCellValue("To Student");
        headerRow.createCell(2).setCellValue("Score 1");
        headerRow.createCell(3).setCellValue("Score 2");
        headerRow.createCell(4).setCellValue("Score 3");
        headerRow.createCell(5).setCellValue("Score 4");
        headerRow.createCell(6).setCellValue("Score 5");
    
         for (String fromStudent : scoresMap.keySet()) {
             HashMap<String, double[]> toMap = scoresMap.get(fromStudent);
             for (String toStudent : toMap.keySet()) {
                 double[] scores = toMap.get(toStudent);
                 Row row = sheet.createRow(rowNum++);
    
                 Cell cellFrom = row.createCell(0);
                 cellFrom.setCellValue(fromStudent);
    
                 Cell cellTo = row.createCell(1);
                 cellTo.setCellValue(toStudent);
    
                 for (int i = 0; i < scores.length; i++) {
                     Cell scoreCell = row.createCell(2 + i);
                     scoreCell.setCellValue(scores[i]);
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
