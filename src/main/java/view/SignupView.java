package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.signup.SignupController;
import interface_adapter.signup.SignupState;
import interface_adapter.signup.SignupViewModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * The View for the Signup screen.
 */
public class SignupView extends JPanel implements ActionListener, PropertyChangeListener {

    private final String viewName = "sign up";
    private final SignupViewModel signupViewModel;
    private final ViewManagerModel viewManagerModel;

    private final JTextField usernameInputField = new JTextField(15);
    private final JPasswordField passwordInputField = new JPasswordField(15);
    private final JPasswordField confirmPasswordInputField = new JPasswordField(15);

    private final JLabel usernameErrorField = new JLabel();
    private final JLabel passwordErrorField = new JLabel();
    private final JLabel successField = new JLabel();

    private final JButton signUpButton;
    private final JButton loginButton;

    private SignupController signupController;

    public SignupView(SignupViewModel signupViewModel, ViewManagerModel viewManagerModel) {
        this.signupViewModel = signupViewModel;
        this.signupViewModel.addPropertyChangeListener(this);
        this.viewManagerModel = viewManagerModel;

        // Overall layout (same as LoginView)
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        final JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(60, 0, 60, 0));

        // Title & subtitle
        final JLabel titleLabel = new JLabel("Create your account");
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 35));

        final JLabel subtitleLabel = new JLabel("Join us to start exploring");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitleLabel.setBorder(new EmptyBorder(15, 0, 40, 0));

        // Input rows (custom panels instead of LabelTextPanel)
        final JPanel usernameRow = createInputRow("Username", usernameInputField);
        final JPanel passwordRow = createInputRow("Password", passwordInputField);
        final JPanel confirmPasswordRow = createInputRow("Confirm Password", confirmPasswordInputField);

        // Error / success labels
        Dimension msgSize = new Dimension(300, 20);

        usernameErrorField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameErrorField.setHorizontalAlignment(SwingConstants.CENTER);
        usernameErrorField.setForeground(Color.RED);
        usernameErrorField.setPreferredSize(msgSize);
        usernameErrorField.setMinimumSize(msgSize);
        usernameErrorField.setMaximumSize(msgSize);
        usernameErrorField.setText(" ");

        passwordErrorField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordErrorField.setHorizontalAlignment(SwingConstants.CENTER);
        passwordErrorField.setForeground(Color.RED);
        passwordErrorField.setPreferredSize(msgSize);
        passwordErrorField.setMinimumSize(msgSize);
        passwordErrorField.setMaximumSize(msgSize);
        passwordErrorField.setText(" ");

        successField.setAlignmentX(Component.CENTER_ALIGNMENT);
        successField.setHorizontalAlignment(SwingConstants.CENTER);
        successField.setForeground(Color.GREEN);
        successField.setPreferredSize(msgSize);
        successField.setMinimumSize(msgSize);
        successField.setMaximumSize(msgSize);
        successField.setText(" ");

        // Buttons (same style as LoginView)
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttons.setOpaque(false);

        signUpButton = new JButton("Sign Up");
        loginButton = new JButton("Login");

        Dimension buttonSize = new Dimension(140, 40);
        signUpButton.setPreferredSize(buttonSize);
        loginButton.setPreferredSize(buttonSize);

        signUpButton.setOpaque(true);
        signUpButton.setBackground(new Color(230, 240, 255)); // light blue
        signUpButton.setForeground(new Color(20, 60, 160));   // deep blue
        signUpButton.setFocusPainted(false);
        signUpButton.setBorder(BorderFactory.createLineBorder(new Color(20, 60, 160), 2));

        loginButton.setOpaque(true);
        loginButton.setBackground(Color.WHITE);
        loginButton.setForeground(new Color(20, 60, 160));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createLineBorder(new Color(20, 60, 160), 2));

        buttons.add(signUpButton);
        buttons.add(loginButton);

        // Button actions
        signUpButton.addActionListener(
                evt -> {
                    if (evt.getSource().equals(signUpButton)) {
                        final SignupState currentState = signupViewModel.getState();
                        signupController.execute(
                                currentState.getUsername(),
                                currentState.getPassword(),
                                currentState.getConfirmPassword()
                        );
                    }
                }
        );

        loginButton.addActionListener(
                evt -> {
                    if (evt.getSource().equals(loginButton)) {
                        signupViewModel.setState(new SignupState());
                        signupViewModel.firePropertyChange();

                        viewManagerModel.setState("log in");
                        viewManagerModel.firePropertyChange();
                    }
                }
        );

        // Listeners to sync fields with state
        usernameInputField.getDocument().addDocumentListener(new DocumentListener() {
            private void helper() {
                final SignupState currentState = signupViewModel.getState();
                currentState.setUsername(usernameInputField.getText());
                signupViewModel.setState(currentState);
            }
            public void insertUpdate(DocumentEvent e) { helper(); }
            public void removeUpdate(DocumentEvent e) { helper(); }
            public void changedUpdate(DocumentEvent e) { helper(); }
        });

        passwordInputField.getDocument().addDocumentListener(new DocumentListener() {
            private void helper() {
                final SignupState currentState = signupViewModel.getState();
                currentState.setPassword(new String(passwordInputField.getPassword()));
                signupViewModel.setState(currentState);
            }
            public void insertUpdate(DocumentEvent e) { helper(); }
            public void removeUpdate(DocumentEvent e) { helper(); }
            public void changedUpdate(DocumentEvent e) { helper(); }
        });

        confirmPasswordInputField.getDocument().addDocumentListener(new DocumentListener() {
            private void helper() {
                final SignupState currentState = signupViewModel.getState();
                currentState.setConfirmPassword(new String(confirmPasswordInputField.getPassword()));
                signupViewModel.setState(currentState);
            }
            public void insertUpdate(DocumentEvent e) { helper(); }
            public void removeUpdate(DocumentEvent e) { helper(); }
            public void changedUpdate(DocumentEvent e) { helper(); }
        });

        // Build vertical layout
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(subtitleLabel);

        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(usernameRow);
        centerPanel.add(usernameErrorField);

        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(passwordRow);
        centerPanel.add(passwordErrorField);

        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(confirmPasswordRow);
        centerPanel.add(successField);

        centerPanel.add(Box.createVerticalStrut(40));
        centerPanel.add(buttons);

        add(centerPanel, BorderLayout.CENTER);
    }

    /** Helper to create a "label + field" row similar to LoginView. */
    private JPanel createInputRow(String labelText, JComponent input) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(new Color(245, 245, 245));

        Dimension rowSize = new Dimension(350, 40);
        row.setPreferredSize(rowSize);
        row.setMinimumSize(rowSize);
        row.setMaximumSize(rowSize);

        JLabel label = new JLabel(labelText);

        // 🔥 FIX: make all labels same width
        Dimension labelSize = new Dimension(140, 30);
        label.setPreferredSize(labelSize);
        label.setMinimumSize(labelSize);
        label.setMaximumSize(labelSize);

        label.setBorder(new EmptyBorder(0, 20, 0, 0));
        row.add(label);

        // Input field size (short height)
        Dimension fieldSize = new Dimension(180, 24);
        input.setPreferredSize(fieldSize);
        input.setMinimumSize(fieldSize);
        input.setMaximumSize(fieldSize);

        row.add(Box.createHorizontalStrut(10));
        row.add(input);

        return row;
    }


    @Override
    public void actionPerformed(ActionEvent evt) {
        System.out.println("Click " + evt.getActionCommand());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final SignupState state = (SignupState) evt.getNewValue();
        setFields(state);

        usernameErrorField.setText(state.getUsernameError());
        passwordErrorField.setText(state.getPasswordError());
        successField.setText(state.getSuccessMessage());
    }

    private void setFields(SignupState state) {
        usernameInputField.setText(state.getUsername());
        passwordInputField.setText(state.getPassword());
        confirmPasswordInputField.setText(state.getConfirmPassword());
    }

    public String getViewName() {
        return viewName;
    }

    public void setSignupController(SignupController signupController) {
        this.signupController = signupController;
    }
}
