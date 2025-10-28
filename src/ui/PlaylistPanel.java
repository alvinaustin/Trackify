package ui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;

public class PlaylistPanel extends JPanel {
    private String username;
    private JList<String> playlistList;
    private DefaultListModel<String> playlistModel;
    private ArrayList<Integer> playlistIds;
    private JTextField playlistNameField, descriptionField;
    private JButton addButton, deleteButton;
    private Color spotifyGreen = new Color(30, 215, 96);

    public PlaylistPanel(String username) {
        this.username = username;

        setLayout(new BorderLayout());
        setBackground(new Color(18, 18, 18));

        // Top Label 
        JLabel title = new JLabel("🎧 Your Playlists", SwingConstants.CENTER);
        title.setForeground(spotifyGreen);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        // Playlist List 
        playlistModel = new DefaultListModel<>();
        playlistList = new JList<>(playlistModel);
        playlistList.setBackground(new Color(24, 24, 24));
        playlistList.setForeground(Color.WHITE);
        playlistList.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(playlistList);
        scrollPane.setBorder(BorderFactory.createLineBorder(spotifyGreen, 1));
        add(scrollPane, BorderLayout.CENTER);

        //  Bottom Panel (Add Playlist) 
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(24, 24, 24));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        bottomPanel.add(new JLabel("Name:"));
        playlistNameField = new JTextField(10);
        bottomPanel.add(playlistNameField);

        bottomPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField(10);
        bottomPanel.add(descriptionField);

        addButton = new JButton("Add Playlist");
        addButton.setBackground(Color.BLACK);
        addButton.setForeground(spotifyGreen);
        addButton.setBorder(BorderFactory.createLineBorder(spotifyGreen, 1));
        bottomPanel.add(addButton);

        deleteButton = new JButton("Delete Playlist");
        deleteButton.setBackground(Color.BLACK);
        deleteButton.setForeground(Color.RED);
        deleteButton.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        bottomPanel.add(deleteButton);

        add(bottomPanel, BorderLayout.SOUTH);

        //  Load existing playlists 
        loadPlaylists();

        // Add new playlist 
        addButton.addActionListener(e -> addPlaylist());

        //  Delete playlist
        deleteButton.addActionListener(e -> deletePlaylist());

        //  Open songs on double-click 
        playlistList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click to open playlist
                    int selectedIndex = playlistList.getSelectedIndex();
                    if (selectedIndex != -1) {
                        int playlistId = playlistIds.get(selectedIndex);
                        JFrame songFrame = new JFrame("Songs in Playlist");
                        songFrame.setSize(600, 400);
                        songFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        songFrame.add(new SongPanel(username, playlistId)); // ✅ Updated constructor
                        songFrame.setVisible(true);
                    }
                }
            }
        });
    }

    // Load all playlists from DB 
    private void loadPlaylists() {
        playlistModel.clear();
        playlistIds = new ArrayList<>();
        try (Connection conn = db.DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT id, name FROM playlists WHERE username = ?");
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                playlistIds.add(rs.getInt("id"));
                playlistModel.addElement(rs.getString("name"));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading playlists: " + e.getMessage());
        }
    }

    // Add Playlist 
    private void addPlaylist() {
        String name = playlistNameField.getText().trim();
        String desc = descriptionField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a playlist name!");
            return;
        }

        try (Connection conn = db.DBConnection.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("INSERT INTO playlists (username, name, description) VALUES (?, ?, ?)");
            stmt.setString(1, username);
            stmt.setString(2, name);
            stmt.setString(3, desc);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Playlist added!");
            loadPlaylists();
            playlistNameField.setText("");
            descriptionField.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding playlist: " + e.getMessage());
        }
    }

    // Delete Playlist 
    private void deletePlaylist() {
        int selectedIndex = playlistList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a playlist to delete.");
            return;
        }

        int playlistId = playlistIds.get(selectedIndex);

        int confirm = JOptionPane.showConfirmDialog(this, "Delete this playlist and all its songs?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = db.DBConnection.getConnection()) {
            PreparedStatement delSongs = conn.prepareStatement("DELETE FROM songs WHERE playlist_id = ?");
            delSongs.setInt(1, playlistId);
            delSongs.executeUpdate();

            PreparedStatement delPlaylist = conn.prepareStatement("DELETE FROM playlists WHERE id = ?");
            delPlaylist.setInt(1, playlistId);
            delPlaylist.executeUpdate();

            JOptionPane.showMessageDialog(this, "🗑 Playlist deleted!");
            loadPlaylists();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error deleting playlist: " + e.getMessage());
        }
    }
}
