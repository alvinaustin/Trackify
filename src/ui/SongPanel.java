package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class SongPanel extends JPanel {

    private JTable songTable;
    private JTextField titleField, artistField, durationField, searchField;
    private JButton addButton, searchButton, refreshButton;
    private DefaultTableModel model;
    private int playlistId;
    private String username;
    private Color spotifyGreen = new Color(30, 215, 96);

    public SongPanel(String username, int playlistId) {
        this.username = username;
        this.playlistId = playlistId;

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        // ===== Top Panel (Search Bar) =====
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(24, 24, 24));
        topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setForeground(Color.WHITE);
        topPanel.add(searchLabel);

        searchField = new JTextField(20);
        topPanel.add(searchField);

        searchButton = new JButton("🔍 Search");
        searchButton.setBackground(Color.BLACK);
        searchButton.setForeground(spotifyGreen);
        searchButton.setBorder(BorderFactory.createLineBorder(spotifyGreen, 1));
        topPanel.add(searchButton);

        refreshButton = new JButton("⟳ Refresh");
        refreshButton.setBackground(Color.BLACK);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // ===== Center Panel (Table) =====
        model = new DefaultTableModel(new String[]{"Title", "Artist", "Duration"}, 0);
        songTable = new JTable(model);
        songTable.setBackground(new Color(25, 25, 25));
        songTable.setForeground(Color.WHITE);
        songTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        songTable.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(songTable);
        scrollPane.setBackground(new Color(25, 25, 25));
        add(scrollPane, BorderLayout.CENTER);

        // ===== Bottom Panel (Add Song Form) =====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(24, 24, 24));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        bottomPanel.add(new JLabel("Title:"));
        titleField = new JTextField(10);
        bottomPanel.add(titleField);

        bottomPanel.add(new JLabel("Artist:"));
        artistField = new JTextField(10);
        bottomPanel.add(artistField);

        bottomPanel.add(new JLabel("Duration:"));
        durationField = new JTextField(5);
        bottomPanel.add(durationField);

        addButton = new JButton("Add Song");
        addButton.setBackground(Color.BLACK);
        addButton.setForeground(spotifyGreen);
        addButton.setBorder(BorderFactory.createLineBorder(spotifyGreen, 1));
        bottomPanel.add(addButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // ===== Load Songs Initially =====
        loadSongs("");

        // ===== Button Actions =====
        addButton.addActionListener(e -> addSong());
        searchButton.addActionListener(e -> searchSongs());
        refreshButton.addActionListener(e -> loadSongs(""));
    }

    // ===== Load Songs (with optional filter) =====
    private void loadSongs(String filter) {
        model.setRowCount(0); // clear table
        try (Connection conn = db.DBConnection.getConnection()) {
            PreparedStatement stmt;
            if (filter == null || filter.trim().isEmpty()) {
                stmt = conn.prepareStatement("SELECT title, artist, duration FROM songs WHERE playlist_id = ?");
                stmt.setInt(1, playlistId);
            } else {
                stmt = conn.prepareStatement("SELECT title, artist, duration FROM songs WHERE playlist_id = ? AND (title LIKE ? OR artist LIKE ?)");
                stmt.setInt(1, playlistId);
                stmt.setString(2, "%" + filter + "%");
                stmt.setString(3, "%" + filter + "%");
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(rs.getString("title"));
                row.add(rs.getString("artist"));
                row.add(rs.getString("duration"));
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading songs: " + e.getMessage());
        }
    }

    // ===== Search Songs =====
    private void searchSongs() {
        String keyword = searchField.getText().trim();
        loadSongs(keyword);
    }

    // ===== Add New Song =====
    private void addSong() {
        String title = titleField.getText().trim();
        String artist = artistField.getText().trim();
        String duration = durationField.getText().trim();

        if (title.isEmpty() || artist.isEmpty() || duration.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        try (Connection conn = db.DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO songs (playlist_id, title, artist, duration) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, playlistId);
            stmt.setString(2, title);
            stmt.setString(3, artist);
            stmt.setString(4, duration);
            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "✅ Song added!");
            loadSongs("");
            titleField.setText("");
            artistField.setText("");
            durationField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding song: " + e.getMessage());
        }
    }
}
