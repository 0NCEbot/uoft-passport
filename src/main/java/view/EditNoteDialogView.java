package view;

import interface_adapter.editnote.EditNoteController;
import interface_adapter.editnote.EditNoteViewModel;
import interface_adapter.editnote.EditNoteState;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class EditNoteDialogView extends JDialog implements PropertyChangeListener {
    private final EditNoteViewModel viewModel;
    private final EditNoteController controller;
    private final String noteId;
    private final String landmarkName;

    private JTextArea contentArea;
    private JLabel errorLabel;
    private JLabel charCountLabel;

    private static final int MAX_CHARS = 500;

    public EditNoteDialogView(Frame parent,
                          EditNoteViewModel viewModel,
                          EditNoteController controller,
                          String noteId,
                          String currentContent,
                          String landmarkName) {
        super(parent, "Edit Note", true);
        this.viewModel = viewModel;
        this.controller = controller;
        this.noteId = noteId;
        this.landmarkName = landmarkName;

        viewModel.addPropertyChangeListener(this);

        setupUI(currentContent);
        pack();
        setLocationRelativeTo(parent);
    }

    private void setupUI(String currentContent) {
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(500, 400));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Edit Note");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));

        JLabel landmarkLabel = new JLabel("Landmark: " + landmarkName);
        landmarkLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        landmarkLabel.setForeground(Color.GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(landmarkLabel, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Content Panel
        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        contentPanel.setBackground(Color.WHITE);

        JLabel contentLabel = new JLabel("Note Content:");
        contentLabel.setFont(new Font("Arial", Font.BOLD, 14));

        contentArea = new JTextArea(currentContent, 10, 40);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setFont(new Font("Arial", Font.PLAIN, 14));
        contentArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Character counter
        charCountLabel = new JLabel(currentContent.length() + "/" + MAX_CHARS);
        charCountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        charCountLabel.setForeground(Color.GRAY);

        contentArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
        });

        JScrollPane scrollPane = new JScrollPane(contentArea);

        contentPanel.add(contentLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        contentPanel.add(charCountLabel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);

        // Error Panel
        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.addActionListener(e -> dispose());

        JButton saveButton = new JButton("Save Changes");
        saveButton.setFont(new Font("Arial", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 102, 204));
        saveButton.setForeground(Color.BLACK);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> handleSave());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(errorLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateCharCount() {
        int length = contentArea.getText().length();
        charCountLabel.setText(length + "/" + MAX_CHARS);

        if (length > MAX_CHARS) {
            charCountLabel.setForeground(Color.RED);
        } else {
            charCountLabel.setForeground(Color.GRAY);
        }
    }

    private void handleSave() {
        String newContent = contentArea.getText();
        controller.execute(noteId, newContent);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            EditNoteState state = viewModel.getState();

            if (state.getError() != null) {
                errorLabel.setText(state.getError());
            } else if (state.getNoteId().equals(noteId)) {
                // Success!
                JOptionPane.showMessageDialog(
                        this,
                        "Note updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
                dispose();
            }
        }
    }
}