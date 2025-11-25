package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.login.LoginController;
import interface_adapter.login.LoginState;
import interface_adapter.login.LoginViewModel;

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
 * The View for when the user is logging into the program.
 */
public class LoginView extends JPanel implements ActionListener, PropertyChangeListener {

    private final String viewName = "log in";
    private final LoginViewModel loginViewModel;
    private final ViewManagerModel viewManagerModel;

    private final JTextField usernameInputField = new JTextField(15);
    private final JLabel usernameErrorField = new JLabel();

    private final JPasswordField passwordInputField = new JPasswordField(15);
    private final JLabel passwordErrorField = new JLabel();

    private final JButton logIn;
    private final JButton signUp;
    private LoginController loginController = null;

    public LoginView(LoginViewModel loginViewModel, ViewManagerModel viewManagerModel) {

        this.loginViewModel = loginViewModel;
        this.loginViewModel.addPropertyChangeListener(this);
        this.viewManagerModel = viewManagerModel;

        // Whole panel background + layout
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Center content panel (everything in the white “card”)
        final JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(new EmptyBorder(60, 0, 60, 0)); // top/bottom padding

        // Title & subtitle like the mockup
        final JLabel welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 40));

        final JLabel subtitleLabel = new JLabel("Ready to explore?");
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 25));

        // Add spacing **under** subtitle
        subtitleLabel.setBorder(new EmptyBorder(15, 0, 40, 0));  // increased spacing


        // Username & password rows
        final LabelTextPanel usernameInfo = new LabelTextPanel(
                new JLabel("Username"), usernameInputField);
        final LabelTextPanel passwordInfo = new LabelTextPanel(
                new JLabel("Password"), passwordInputField);

        // Make input rows narrower and centered
        usernameInfo.setMaximumSize(new Dimension(300, 50));
        passwordInfo.setMaximumSize(new Dimension(300, 50));
        usernameInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordInfo.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Error label (under username)
        usernameErrorField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameErrorField.setHorizontalAlignment(SwingConstants.CENTER);
        usernameErrorField.setForeground(Color.RED);
        usernameErrorField.setPreferredSize(new Dimension(300, 20));
        usernameErrorField.setMinimumSize(new Dimension(300, 20));
        usernameErrorField.setMaximumSize(new Dimension(300, 20));
        usernameErrorField.setText(" ");

        // Buttons panel – Login and Sign Up side by side
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttons.setOpaque(false);

        logIn = new JButton("Login");
        signUp = new JButton("Sign Up");

        Dimension buttonSize = new Dimension(140, 40);
        logIn.setPreferredSize(buttonSize);
        signUp.setPreferredSize(buttonSize);
        logIn.setOpaque(true);
        logIn.setBackground(new Color(230, 240, 255));       // light blue
        logIn.setForeground(new Color(20, 60, 160));         // deep blue text
        logIn.setFocusPainted(false);
        logIn.setBorder(BorderFactory.createLineBorder(new Color(20, 60, 160), 2));

        signUp.setOpaque(true);
        signUp.setBackground(new Color(255, 255, 255));      // white
        signUp.setForeground(new Color(20, 60, 160));        // deep blue
        signUp.setFocusPainted(false);
        signUp.setBorder(BorderFactory.createLineBorder(new Color(20, 60, 160), 2));

        buttons.add(logIn);
        buttons.add(signUp);

        // Add listeners
        logIn.addActionListener(
                evt -> {
                    if (evt.getSource().equals(logIn)) {
                        final LoginState currentState = loginViewModel.getState();

                        loginController.execute(
                                currentState.getUsername(),
                                currentState.getPassword()
                        );
                    }
                }
        );

        signUp.addActionListener(
                evt -> {
                    if (evt.getSource().equals(signUp)) {
                        loginViewModel.setState(new LoginState());
                        loginViewModel.firePropertyChange();

                        this.viewManagerModel.setState("sign up");
                        this.viewManagerModel.firePropertyChange();
                    }
                }
        );

        usernameInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final LoginState currentState = loginViewModel.getState();
                currentState.setUsername(usernameInputField.getText());
                loginViewModel.setState(currentState);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentListenerHelper();
            }
        });

        passwordInputField.getDocument().addDocumentListener(new DocumentListener() {

            private void documentListenerHelper() {
                final LoginState currentState = loginViewModel.getState();
                currentState.setPassword(new String(passwordInputField.getPassword()));
                loginViewModel.setState(currentState);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentListenerHelper();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentListenerHelper();
            }
        });

        // Build the vertical layout inside the centered panel
        centerPanel.add(welcomeLabel);
        centerPanel.add(Box.createVerticalStrut(5));  // space between Welcome and subtitle
        centerPanel.add(subtitleLabel);

        centerPanel.add(Box.createVerticalStrut(25));  // space before Username
        centerPanel.add(usernameInfo);
        centerPanel.add(usernameErrorField);

        centerPanel.add(Box.createVerticalStrut(5));  // space before Password
        centerPanel.add(passwordInfo);

        centerPanel.add(Box.createVerticalStrut(60));  // space before Login/Sign Up buttons
        centerPanel.add(buttons);


        // Put center panel in the middle of the view
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * React to a button click that results in evt.
     * @param evt the ActionEvent to react to
     */
    public void actionPerformed(ActionEvent evt) {
        System.out.println("Click " + evt.getActionCommand());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final LoginState state = (LoginState) evt.getNewValue();
        setFields(state);
        usernameErrorField.setText(state.getLoginError());
    }

    private void setFields(LoginState state) {

        usernameInputField.setText(state.getUsername());
        passwordInputField.setText(state.getUsername());
    }

    public String getViewName() {
        return viewName;
    }

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }
}
