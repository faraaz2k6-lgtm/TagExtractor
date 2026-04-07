import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class TagExtractor extends JFrame {

    private JLabel textFileLabel;
    private JLabel stopFileLabel;
    private JTextArea outputArea;

    private File textFile;
    private File stopWordsFile;

    private Set<String> stopWords;
    private Map<String, Integer> wordFrequency;

    public TagExtractor() {
        setTitle("Tag / Keyword Extractor");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        stopWords = new TreeSet<>();
        wordFrequency = new TreeMap<>();

        createGUI();
    }

    private void createGUI() {
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        textFileLabel = new JLabel("Selected Text File: None");
        stopFileLabel = new JLabel("Selected Stop Words File: None");

        topPanel.add(textFileLabel);
        topPanel.add(stopFileLabel);
        add(topPanel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton chooseTextButton = new JButton("Choose Text File");
        JButton chooseStopButton = new JButton("Choose Stop Words File");
        JButton extractButton = new JButton("Extract Tags");
        JButton saveButton = new JButton("Save Output");

        chooseTextButton.addActionListener(this::chooseTextFile);
        chooseStopButton.addActionListener(this::chooseStopWordsFile);
        extractButton.addActionListener(this::extractTags);
        saveButton.addActionListener(this::saveOutput);

        buttonPanel.add(chooseTextButton);
        buttonPanel.add(chooseStopButton);
        buttonPanel.add(extractButton);
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void chooseTextFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            textFile = chooser.getSelectedFile();
            textFileLabel.setText("Selected Text File: " + textFile.getName());
        }
    }

    private void chooseStopWordsFile(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            stopWordsFile = chooser.getSelectedFile();
            stopFileLabel.setText("Selected Stop Words File: " + stopWordsFile.getName());
        }
    }

    private void extractTags(ActionEvent e) {
        if (textFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose a text file first.");
            return;
        }

        if (stopWordsFile == null) {
            JOptionPane.showMessageDialog(this, "Please choose a stop words file first.");
            return;
        }

        stopWords.clear();
        wordFrequency.clear();
        outputArea.setText("");

        loadStopWords();
        processTextFile();
        displayResults();

        JOptionPane.showMessageDialog(this, "Tag extraction completed successfully.");
    }

    private void loadStopWords() {
        try (Scanner fileScanner = new Scanner(stopWordsFile)) {
            while (fileScanner.hasNextLine()) {
                String word = fileScanner.nextLine().trim().toLowerCase();

                if (!word.isEmpty()) {
                    stopWords.add(word);
                }
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error reading stop words file.");
        }
    }

    private void processTextFile() {
        try (Scanner fileScanner = new Scanner(textFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().toLowerCase();

                // Split on anything that is NOT a letter
                String[] words = line.split("[^a-z]+");

                for (String word : words) {
                    // Skip empty words and stop words
                    if (!word.isEmpty() && !stopWords.contains(word)) {
                        if (wordFrequency.containsKey(word)) {
                            wordFrequency.put(word, wordFrequency.get(word) + 1);
                        } else {
                            wordFrequency.put(word, 1);
                        }
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this, "Error reading text file.");
        }
    }

    private void displayResults() {
        StringBuilder sb = new StringBuilder();

        sb.append("Extracted Tags and Frequencies\n");
        sb.append("----------------------------------------\n");

        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            sb.append(entry.getKey())
                    .append(" : ")
                    .append(entry.getValue())
                    .append("\n");
        }

        outputArea.setText(sb.toString());
    }

    private void saveOutput(ActionEvent e) {
        if (outputArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to save. Please extract tags first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File outputFile = chooser.getSelectedFile();

            try (PrintWriter writer = new PrintWriter(outputFile)) {
                writer.print(outputArea.getText());
                JOptionPane.showMessageDialog(this, "Output saved successfully.");
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(this, "Error saving output file.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractor app = new TagExtractor();
            app.setVisible(true);
        });
    }
}