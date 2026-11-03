import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * IT 2045C Lab 09 - Java Data Streams
 * Loads a text file, then uses a Stream with a lambda filter to show
 * only the lines containing a user-supplied search string.
 */
public class DataStreams extends JFrame
{
    private final JTextArea originalArea = new JTextArea();
    private final JTextArea filteredArea = new JTextArea();
    private final JTextField searchField = new JTextField(20);
    private final JButton loadButton = new JButton("Load File");
    private final JButton searchButton = new JButton("Search File");
    private final JButton quitButton = new JButton("Quit");
    private final JLabel statusLabel = new JLabel("Load a file to begin.");

    private Path loadedFile = null;

    public DataStreams()
    {
        super("Data Streams - File Search");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // Top: search string
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search string:"));
        topPanel.add(searchField);
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center: two parallel scrolling text areas
        originalArea.setEditable(false);
        filteredArea.setEditable(false);
        JScrollPane leftScroll = new JScrollPane(originalArea);
        leftScroll.setBorder(BorderFactory.createTitledBorder("Original File"));
        JScrollPane rightScroll = new JScrollPane(filteredArea);
        rightScroll.setBorder(BorderFactory.createTitledBorder("Filtered Lines"));
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        centerPanel.add(leftScroll);
        centerPanel.add(rightScroll);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom: command buttons
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        bottomPanel.add(loadButton);
        bottomPanel.add(searchButton);
        bottomPanel.add(quitButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Search is disabled until a file has been loaded
        searchButton.setEnabled(false);

        loadButton.addActionListener(e -> loadFile());
        searchButton.addActionListener(e -> searchFile());
        quitButton.addActionListener(e -> System.exit(0));

        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    /** Lets the user pick a file with JFileChooser and shows it in the left text area. */
    private void loadFile()
    {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
        {
            return;
        }

        Path path = Paths.get(chooser.getSelectedFile().getAbsolutePath());

        // Files.lines + Stream: read every line of the file
        try (Stream<String> lines = Files.lines(path))
        {
            String contents = lines.collect(Collectors.joining("\n"));
            originalArea.setText(contents);
            originalArea.setCaretPosition(0);
            filteredArea.setText("");
            loadedFile = path;
            searchButton.setEnabled(true);
            statusLabel.setText("Loaded: " + path.getFileName());
        }
        catch (IOException | java.io.UncheckedIOException ex)
        {
            JOptionPane.showMessageDialog(this,
                    "Could not read file:\n" + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Streams the loaded file and keeps only lines containing the search string. */
    private void searchFile()
    {
        final String searchString = searchField.getText().trim();
        if (searchString.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "Please enter a search string.",
                    "No Search String", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Stream<String> lines = Files.lines(loadedFile))
        {
            // The Stream + lambda filter required by the lab (case-insensitive match)
            String result = lines
                    .filter(line -> line.toLowerCase().contains(searchString.toLowerCase()))
                    .collect(Collectors.joining("\n"));

            filteredArea.setText(result);
            filteredArea.setCaretPosition(0);
            statusLabel.setText(result.isEmpty()
                    ? "No lines contain \"" + searchString + "\""
                    : "Filtered on \"" + searchString + "\"");
        }
        catch (IOException | java.io.UncheckedIOException ex)
        {
            JOptionPane.showMessageDialog(this,
                    "Could not read file:\n" + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new DataStreams().setVisible(true));
    }
}