package com.example.peerevaluation;

import java.util.HashMap;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.awt.Component;

public class ScoreInputFrameTest {
    private HashMap<String, String> studentMap;
    private ScoreInputFrame frame;

    @Before
    public void setUp() {
        studentMap = new HashMap<>();
        studentMap.put("001", "Hanqi");
        studentMap.put("002", "Zhou");
        frame = new ScoreInputFrame(studentMap);
    }

    // Existing initialization tests
    @Test
    public void testScoresMapInitialization() {
        assertNotNull(frame.getScoresMap());
        assertTrue(frame.getScoresMap().isEmpty());
    }

    @Test
    public void testTableInitialization() {
        JTable table = getTableFromFrame();
        assertNotNull(table);
        assertEquals("Hanqi", table.getValueAt(0, 0));
        assertEquals("Zhou", table.getValueAt(0, 1));
    }

    // New zero sum button tests
    //(Driver : kr21130)
    //(Navigator : ka22205)
    @Test
    public void testZeroSumButtonExists() {
        JPanel panel = (JPanel) frame.getContentPane().getComponent(0);
        JButton zeroSumButton = findZeroSumButton(panel);
        assertNotNull("Zero Sum button should exist in the frame", zeroSumButton);
        assertTrue("Zero Sum button should be enabled", zeroSumButton.isEnabled());
        assertEquals("Button should have correct text", "Check Zero Sum", zeroSumButton.getText());
    }

    //(Driver : kr21130)
    //(Navigator : hz24472)
    @Test
    public void testZeroSumButtonClick() {
        JPanel panel = (JPanel) frame.getContentPane().getComponent(0);
        JButton zeroSumButton = findZeroSumButton(panel);
        assertNotNull("Zero Sum button should exist", zeroSumButton);

        // Simulate button click
        simulateZeroSumButton(zeroSumButton);

        // The button click should not cause any exceptions
        assertTrue("Frame should remain visible after button click", frame.isVisible());
    }

    // Existing test methods
    @Test
    public void testValidScoreInput() {
        JTable table = getTableFromFrame();

        table.setValueAt("4.123", 0, 2);
        table.setValueAt("3.345", 0, 3);
        table.setValueAt("5.0", 0, 4);
        table.setValueAt("2.123", 0, 5);
        table.setValueAt("4.124355", 0, 6);

        simulateSubmitButton();

        HashMap<String, double[]> scoresForBob = frame.getScoresMap().get("Hanqi");
        assertNotNull(scoresForBob);
        assertArrayEquals(new double[]{4.123, 3.345, 5.0, 2.123, 4.124355}, scoresForBob.get("Zhou"), 0.000001);
    }

    @Test
    public void testInvalidScoreInput() throws Exception {
        JTable table = getTableFromFrame();

        table.setValueAt("invalid", 0, 2);
        table.setValueAt("3.345", 0, 3);
        table.setValueAt("5.0", 0, 4);
        table.setValueAt("2.123", 0, 5);
        table.setValueAt("4.124355", 0, 6);

        simulateSubmitButton();

        assertTrue(frame.getScoresMap().isEmpty());
    }

    @Test
    public void testScoreOutOfRange() throws Exception {
        JTable table = getTableFromFrame();

        table.setValueAt("6.0", 0, 2);
        table.setValueAt("3.345", 0, 3);
        table.setValueAt("5.0", 0, 4);
        table.setValueAt("2.123", 0, 5);
        table.setValueAt("4.124355", 0, 6);

        simulateSubmitButton();

        assertTrue(frame.getScoresMap().isEmpty());
    }

    // Existing calculation tests
    @Test
    public void testCalculateGrandAverageScores() {
        frame.getScoresMap().put("Alice", new HashMap<>());
        frame.getScoresMap().get("Alice").put("Bob", new double[]{90, 80});
        frame.getScoresMap().get("Alice").put("Charlie", new double[]{70});

        frame.getScoresMap().put("Bob", new HashMap<>());
        frame.getScoresMap().get("Bob").put("Alice", new double[]{85});
        frame.getScoresMap().get("Bob").put("Charlie", new double[]{75, 95});

        HashMap<String, Double> grandAverages = frame.calculateGrandAverageScores();

        System.out.println("Grand Averages: " + grandAverages);

        assertEquals(85.0, grandAverages.get("Alice"), 0.01);
        assertEquals(85.0, grandAverages.get("Bob"), 0.01);
        assertEquals(80.0, grandAverages.get("Charlie"), 0.01);
    }

    @Test
    public void testCalculatePeerMarks() {
        frame.getScoresMap().put("Alice", new HashMap<>());
        frame.getScoresMap().get("Alice").put("Bob", new double[]{90, 80});
        frame.getScoresMap().get("Alice").put("Charlie", new double[]{70});

        frame.getScoresMap().put("Bob", new HashMap<>());
        frame.getScoresMap().get("Bob").put("Alice", new double[]{85});
        frame.getScoresMap().get("Bob").put("Charlie", new double[]{75, 95});

        double multiplier = 2.0;

        HashMap<String, Double> peerMarks = frame.calculatePeerMarks(multiplier);

        System.out.println("Peer Marks: " + peerMarks);

        assertEquals(170.0, peerMarks.get("Alice"), 0.01);
        assertEquals(170.0, peerMarks.get("Bob"), 0.01);
        assertEquals(160.0, peerMarks.get("Charlie"), 0.01);
    }

    // Helper methods
    //(Driver : kr21130)
    //(Navigator : hz24472)
    private JTable getTableFromFrame() {
        JPanel panel = (JPanel) frame.getContentPane().getComponent(0);
        JScrollPane scrollPane = (JScrollPane) panel.getComponent(0);
        return (JTable) scrollPane.getViewport().getView();
    }
    //(Driver : kr21130)
    //(Navigator : hz24472)
    private void simulateSubmitButton() {
        JPanel panel = (JPanel) frame.getContentPane().getComponent(0);
        JButton submitButton = (JButton) panel.getComponent(1);
        for (ActionListener listener : submitButton.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(submitButton, ActionEvent.ACTION_PERFORMED, null));
        }
    }
    //(Driver : kr21130)
    //(Navigator : cl24929)
    private JButton findZeroSumButton(JPanel panel) {
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if ("Check Zero Sum".equals(button.getText())) {
                    return button;
                }
            }
        }
        return null;
    }

    private void simulateZeroSumButton(JButton button) {
        assertNotNull("Zero Sum button should not be null", button);
        for (ActionListener listener : button.getActionListeners()) {
            listener.actionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, "checkZeroSum"));
        }
    }
}