package view;

import interface_adapter.EventBus;
import interface_adapter.ViewManagerModel;
import interface_adapter.myprogress.MyProgressController;
import interface_adapter.myprogress.MyProgressState;
import interface_adapter.myprogress.MyProgressViewModel;
import interface_adapter.logout.LogoutController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MyProgressView extends JPanel implements PropertyChangeListener {

    private final String viewName = "my progress";
    private final MyProgressViewModel viewModel;
    private LogoutController logoutController;

    // Top bar
    private final JLabel usernameLabel;

    // Stats labels
    private final JLabel completionLabel = new JLabel(" ");
    private final JLabel visitsTodayLabel = new JLabel(" ");
    private final JLabel visitsWeekLabel = new JLabel(" ");
    private final JLabel visitsMonthLabel = new JLabel(" ");
    private final JLabel totalVisitsLabel = new JLabel(" ");
    private final JLabel currentStreakLabel = new JLabel(" ");
    private final JLabel longestStreakLabel = new JLabel(" ");
    private final JLabel mostVisitedLabel = new JLabel(" ");

    private MyProgressController controller;

    public MyProgressView(MyProgressViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewModel.addPropertyChangeListener(this);

        // Subscribe to global event
        EventBus.subscribe("ToMyProgress", _ -> {
            if (controller == null) {
                return;
            }
            System.out.println("[MyProgressView] Navigated to My Progress");
            controller.execute();
            if (viewModel.getState().getUsername() == null) {
                throw new RuntimeException("Username is null");
            }
        });

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        // ======= TOP BAR (username + logout) =======
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        userPanel.setOpaque(false);
        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(0, 102, 204));
        userPanel.add(usernameLabel);

        JLabel logoutLabel = new JLabel("Logout");
        logoutLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutLabel.setForeground(new Color(0, 102, 204));
        logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (logoutController != null) {
                    String username = viewModel.getState().getUsername();
                    logoutController.execute(username);
                }
            }
        });

        topBar.add(userPanel, BorderLayout.WEST);
        topBar.add(logoutLabel, BorderLayout.EAST);

        // ======= CENTER CONTENT (stats) =======
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        // Title
        JLabel title = new JLabel("My Progress");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stats panel
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        statsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        statsPanel.add(createStatRow("Landmarks Explored:", completionLabel));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(createStatRow("Visits Today:", visitsTodayLabel));
        statsPanel.add(createStatRow("Visits This Week:", visitsWeekLabel));
        statsPanel.add(createStatRow("Visits This Month:", visitsMonthLabel));
        statsPanel.add(createStatRow("Total Visits:", totalVisitsLabel));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(createStatRow("Current Streak:", currentStreakLabel));
        statsPanel.add(createStatRow("Longest Streak:", longestStreakLabel));
        statsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        statsPanel.add(createStatRow("Most Visited:", mostVisitedLabel));

        // Visit History button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);

        JButton visitHistoryButton = new JButton("View Visit History");
        visitHistoryButton.setFont(new Font("Arial", Font.BOLD, 14));
        visitHistoryButton.setForeground(Color.WHITE);
        visitHistoryButton.setBackground(new Color(0, 102, 204));
        visitHistoryButton.setOpaque(true);
        visitHistoryButton.setBorderPainted(false);
        visitHistoryButton.setFocusPainted(false);
        visitHistoryButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        visitHistoryButton.addActionListener(e -> {
            EventBus.publish("ToViewHistory", viewModel.getState().getUsername());
            viewManagerModel.setState("view history");
            viewManagerModel.firePropertyChange();
        });

        buttonPanel.add(visitHistoryButton);

        centerPanel.add(title);
        centerPanel.add(statsPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(buttonPanel);

        // ======= BOTTOM BAR (back link) =======
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
            viewManagerModel.setState("homescreen");
            viewManagerModel.firePropertyChange();
        });

        bottomBar.add(backButton);

        // ======= ADD TO MAIN LAYOUT =======
        this.add(topBar, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createStatRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.setBackground(Color.WHITE);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("Arial", Font.PLAIN, 14));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 14));
        valueLabel.setForeground(new Color(0, 102, 204));

        row.add(labelComponent);
        row.add(valueLabel);
        return row;
    }

    public void setLogoutController(LogoutController controller) {
        this.logoutController = controller;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        MyProgressState state = (MyProgressState) evt.getNewValue();

        updateDisplay(state);
    }

    private void updateDisplay(MyProgressState state) {
        // Update username
        usernameLabel.setText(state.getUsername());

        // Update stats
        completionLabel.setText(state.getUniqueLandmarksVisited() + " / "
                + state.getUniqueLandmarksTotal()
                + " (" + state.getUniqueLandmarksCompletionPercentage() + "%)");
        visitsTodayLabel.setText(String.valueOf(state.getTotalVisitsToday()));
        visitsWeekLabel.setText(String.valueOf(state.getTotalVisitsPastWeek()));
        visitsMonthLabel.setText(String.valueOf(state.getTotalVisitsPastMonth()));
        totalVisitsLabel.setText(String.valueOf(state.getTotalVisits()));
        currentStreakLabel.setText(state.getCurrentVisitStreak() + " days");
        longestStreakLabel.setText(state.getLongestVisitStreak() + " days");

        if (state.getMostVisitedLandmarkName().isEmpty()) {
            mostVisitedLabel.setText("No visits yet");
        } else {
            mostVisitedLabel.setText(state.getMostVisitedLandmarkName()
                    + " (" + state.getMostVisitedLandmarkCount() + " visits)");
        }
    }

    public String getViewName() {
        return viewName;
    }

    public void setMyProgressController(MyProgressController controller) {
        this.controller = controller;
    }
}