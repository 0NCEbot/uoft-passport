package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.addnotes.AddNotesController;
import interface_adapter.addnotes.AddNotesState;
import interface_adapter.addnotes.AddNotesViewModel;
// ADD THESE IMPORTS (note: no underscore):
import interface_adapter.editnote.EditNoteController;
import interface_adapter.editnote.EditNoteViewModel;
import interface_adapter.deletenote.DeleteNoteController;
import interface_adapter.deletenote.DeleteNoteViewModel;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class AddNotesView extends JPanel implements PropertyChangeListener {

    private final String viewName = "notes";
    private final AddNotesViewModel viewModel;
    private final ViewManagerModel viewManagerModel;

    private AddNotesController controller;

    // ADD THESE FIELDS:
    private EditNoteController editNoteController;
    private EditNoteViewModel editNoteViewModel;
    private DeleteNoteController deleteNoteController;
    private DeleteNoteViewModel deleteNoteViewModel;

    private JLabel usernameLabel;
    private JLabel landmarkNameLabel;
    private JTextArea descriptionArea;      // left-side input
    private JTextArea descriptionRight;     // right-side landmark description
    private JLabel addressLabel;
    private JLabel hoursLabel;
    private JLabel imageLabel;
    private JLabel messageLabel;

    // CHANGED: Replace JList with JPanel
    private JPanel notesDisplayPanel;  // CHANGED from DefaultListModel and JList
    private JLabel noNotesLabel;

    // ====== GOOGLE PLACES CONFIG (same as SelectedPlaceView) ======
    private static final String PLACES_API_KEY = "AIzaSyCk9bPskLw7eUI-_Y9G6tW8eDAE-iXI8Ms";
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private final OkHttpClient httpClient = new OkHttpClient();
    private SwingWorker<ImageIcon, Void> currentPhotoWorker;

    // UPDATED CONSTRUCTOR - Add the edit/delete controllers
    public AddNotesView(AddNotesViewModel viewModel,
                        ViewManagerModel viewManagerModel,
                        EditNoteViewModel editNoteViewModel,
                        EditNoteController editNoteController,
                        DeleteNoteViewModel deleteNoteViewModel,
                        DeleteNoteController deleteNoteController) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        this.editNoteViewModel = editNoteViewModel;
        this.editNoteController = editNoteController;
        this.deleteNoteViewModel = deleteNoteViewModel;
        this.deleteNoteController = deleteNoteController;

        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== TOP BAR =====
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        userPanel.setOpaque(false);
        usernameLabel = new JLabel("Username1!");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(0, 102, 204));
        userPanel.add(usernameLabel);

        JLabel logoutLabel = new JLabel("Logout");
        logoutLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutLabel.setForeground(new Color(0, 102, 204));
        logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        topBar.add(userPanel, BorderLayout.WEST);
        topBar.add(logoutLabel, BorderLayout.EAST);

        // ===== BOTTOM BAR (back) =====
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton backButton = new JButton("back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setForeground(new Color(0, 102, 204));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> {
            AddNotesState current = viewModel.getState();
            AddNotesState cleared = new AddNotesState();

            cleared.setUsername(current.getUsername());
            cleared.setLandmarkName(current.getLandmarkName());
            cleared.setLandmarkDescription(current.getLandmarkDescription());
            cleared.setAddress(current.getAddress());
            cleared.setOpenHours(current.getOpenHours());
            cleared.setContent("");
            cleared.setErrorMessage(null);
            cleared.setSuccessMessage(null);
            cleared.setNotes(current.getNotes());

            viewModel.setState(cleared);
            viewModel.firePropertyChange();

            descriptionArea.setText("");
            messageLabel.setText("");

            viewManagerModel.setState("selected place");
            viewManagerModel.firePropertyChange();
        });

        bottomBar.add(backButton);

        // ===== CENTER LAYOUT =====
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setBackground(Color.WHITE);

        // -------- LEFT SIDE --------
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 20));

        JLabel notesLabel = new JLabel("Notes");
        notesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        notesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        noNotesLabel = new JLabel("No notes yet!");
        noNotesLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        noNotesLabel.setForeground(Color.GRAY);
        noNotesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // CHANGED: Create panel instead of JList
        notesDisplayPanel = new JPanel();
        notesDisplayPanel.setLayout(new BoxLayout(notesDisplayPanel, BoxLayout.Y_AXIS));
        notesDisplayPanel.setBackground(Color.WHITE);

        JScrollPane notesScroll = new JScrollPane(notesDisplayPanel);
        notesScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        notesScroll.setPreferredSize(new Dimension(500, 150));
        notesScroll.getVerticalScrollBar().setUnitIncrement(16);

        // typing area
        descriptionArea = new JTextArea(5, 21);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton addNoteButton = new JButton("Add Note");
        addNoteButton.setFont(new Font("Arial", Font.PLAIN, 16));
        addNoteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addNoteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addNoteButton.addActionListener(e -> {
            if (controller != null) {
                controller.addNote(descriptionArea.getText());
            }
        });

        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(notesLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(noNotesLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(notesScroll);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(descriptionScroll);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(addNoteButton);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(messageLabel);

        // -------- RIGHT SIDE --------
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 40));

        imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(350, 220));
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        setPlaceholderImage();

        landmarkNameLabel = new JLabel("Landmark Name");
        landmarkNameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        landmarkNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        descriptionRight = new JTextArea();
        descriptionRight.setEditable(false);
        descriptionRight.setLineWrap(true);
        descriptionRight.setWrapStyleWord(true);
        descriptionRight.setFont(new Font("Arial", Font.PLAIN, 14));
        descriptionRight.setOpaque(false);
        descriptionRight.setAlignmentX(Component.LEFT_ALIGNMENT);

        addressLabel = new JLabel();
        addressLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        addressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        hoursLabel = new JLabel();
        hoursLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        hoursLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        rightPanel.add(imageLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(landmarkNameLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(descriptionRight);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        rightPanel.add(addressLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        rightPanel.add(hoursLabel);

        centerPanel.add(leftPanel);
        JScrollPane scroll = new JScrollPane(
                rightPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        );
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 20));

        centerPanel.add(scroll);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void setNotesController(AddNotesController controller) {
        this.controller = controller;
    }

    public String getViewName() {
        return viewName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"state".equals(evt.getPropertyName())) return;

        AddNotesState state = (AddNotesState) evt.getNewValue();

        usernameLabel.setText(state.getUsername());
        landmarkNameLabel.setText(state.getLandmarkName());
        descriptionRight.setText(state.getLandmarkDescription());
        addressLabel.setText(state.getAddress());

        String openHours = state.getOpenHours();
        if (openHours == null || openHours.isBlank() || "No hours available".equals(openHours)) {
            hoursLabel.setText("Hours: No hours available");
        } else {
            String htmlHours =
                    "<html>Hours:<br>" + openHours.replace("\n", "<br>") + "</html>";
            hoursLabel.setText(htmlHours);
        }

        // CHANGED: Update notes display with Edit/Delete buttons
        notesDisplayPanel.removeAll();
        if (state.getNotes() != null && !state.getNotes().isEmpty()) {
            for (AddNotesState.NoteVM n : state.getNotes()) {
                JPanel notePanel = createNotePanel(n);
                notesDisplayPanel.add(notePanel);
            }
            noNotesLabel.setVisible(false);
        } else {
            noNotesLabel.setVisible(true);
        }
        notesDisplayPanel.revalidate();
        notesDisplayPanel.repaint();

        if (state.getErrorMessage() != null) {
            messageLabel.setForeground(Color.RED);
            messageLabel.setText(state.getErrorMessage());
        } else if (state.getSuccessMessage() != null) {
            messageLabel.setForeground(new Color(0, 128, 0));
            messageLabel.setText(state.getSuccessMessage());
            descriptionArea.setText("");
        } else {
            messageLabel.setText("");
        }

        loadPhotoForLandmarkAsync(state.getLandmarkName());
    }

    // NEW METHOD: Create note panel with Edit/Delete buttons
    private JPanel createNotePanel(AddNotesState.NoteVM noteVM) {
        System.out.println("DEBUG Display: noteId='" + noteVM.noteId + "' createdAt='" + noteVM.createdAt + "' content='" + noteVM.content + "'");
        JPanel notePanel = new JPanel(new BorderLayout(10, 5));
        notePanel.setBackground(Color.WHITE);
        notePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 5, 8, 5)
        ));
        notePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        // Note text
        JLabel noteLabel = new JLabel(noteVM.createdAt + " - " + noteVM.content);
        noteLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);

        JButton editButton = new JButton("Edit");
        editButton.setFont(new Font("Arial", Font.PLAIN, 11));
        editButton.setForeground(new Color(0, 102, 204));
        editButton.setBorderPainted(true);
        editButton.setContentAreaFilled(false);
        editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        editButton.addActionListener(e -> handleEditNote(noteVM.noteId, noteVM.content));

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 11));
        deleteButton.setForeground(new Color(220, 53, 69));
        deleteButton.setBorderPainted(true);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.addActionListener(e -> handleDeleteNote(noteVM.noteId));

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        notePanel.add(noteLabel, BorderLayout.CENTER);
        notePanel.add(buttonPanel, BorderLayout.EAST);

        return notePanel;
    }

    // NEW METHOD: Handle edit
    private void handleEditNote(String noteId, String currentContent) {
        EditNoteDialogView dialog = new EditNoteDialogView(
                (Frame) SwingUtilities.getWindowAncestor(this),
                editNoteViewModel,
                editNoteController,
                noteId,
                currentContent,
                viewModel.getState().getLandmarkName()
        );
        dialog.setVisible(true);

        // Reload notes after edit
        if (controller != null) {
            controller.reloadNotes();
            // Clear the error message
            messageLabel.setText("");
        }
    }

    // NEW METHOD: Handle delete
    private void handleDeleteNote(String noteId) {
        int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this note?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            deleteNoteController.execute(noteId);

            // Reload notes after delete
            if (controller != null) {
                controller.reloadNotes();
                // Clear the error message
                messageLabel.setText("");
            }
        }
    }

    /* ================== PHOTO LOADING ================== */

    private void setPlaceholderImage() {
        ImageIcon img = new ImageIcon("src/main/resources/placeholder_landmark.jpg");
        Image scaled = img.getImage().getScaledInstance(350, 220, Image.SCALE_SMOOTH);
        imageLabel.setIcon(new ImageIcon(scaled));
    }

    private void loadPhotoForLandmarkAsync(String landmarkName) {
        if (landmarkName == null || landmarkName.isBlank()) {
            setPlaceholderImage();
            return;
        }

        if (currentPhotoWorker != null && !currentPhotoWorker.isDone()) {
            currentPhotoWorker.cancel(true);
        }

        currentPhotoWorker = new SwingWorker<>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                try {
                    String photoName = findPhotoNameForPlace(landmarkName);
                    if (photoName == null) {
                        return null;
                    }
                    return fetchPhotoIcon(photoName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    ImageIcon icon = get();
                    if (icon != null) {
                        imageLabel.setIcon(icon);
                    } else {
                        setPlaceholderImage();
                    }
                    revalidate();
                    repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    setPlaceholderImage();
                }
            }
        };

        currentPhotoWorker.execute();
    }

    private String findPhotoNameForPlace(String textQuery) throws Exception {
        String url = "https://places.googleapis.com/v1/places:searchText";

        JSONObject bodyJson = new JSONObject();
        bodyJson.put("textQuery", textQuery);
        bodyJson.put("maxResultCount", 1);

        RequestBody body = RequestBody.create(bodyJson.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Goog-Api-Key", PLACES_API_KEY)
                .addHeader("X-Goog-FieldMask", "places.photos")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            String resp = response.body().string();
            JSONObject json = new JSONObject(resp);
            JSONArray places = json.optJSONArray("places");
            if (places == null || places.length() == 0) {
                return null;
            }
            JSONObject place = places.getJSONObject(0);
            JSONArray photos = place.optJSONArray("photos");
            if (photos == null || photos.length() == 0) {
                return null;
            }
            JSONObject photo = photos.getJSONObject(0);
            return photo.optString("name", null);
        }
    }

    private ImageIcon fetchPhotoIcon(String photoName) throws Exception {
        String url = "https://places.googleapis.com/v1/"
                + photoName
                + "/media?maxHeightPx=300&maxWidthPx=600&key="
                + PLACES_API_KEY;

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            byte[] bytes = response.body().bytes();
            ImageIcon icon = new ImageIcon(bytes);
            Image scaled = icon.getImage().getScaledInstance(350, 220, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        }
    }
}
