package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.planroute.PlanRouteController;
import interface_adapter.planroute.PlanRouteState;
import interface_adapter.planroute.PlanRouteViewModel;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class PlanRouteView extends JPanel implements PropertyChangeListener {

    private final String viewName = "plan a route";
    private final PlanRouteViewModel viewModel;
    private final ViewManagerModel viewManagerModel;
    private PlanRouteController controller;

    // Input fields
    private JTextField startLocationField;
    private JTextField destinationField;
    private JTextField intermediateStopsField;

    // Display components
    private JLabel usernameLabel;
    private JLabel totalDistanceLabel;
    private JLabel totalDurationLabel;
    private JLabel errorLabel;
    private JList<String> stepsList;
    private DefaultListModel<String> stepsListModel;
    private JLabel manualModeIndicator;

    // Buttons
    private JButton planButton;
    private JButton startRouteButton;

    public PlanRouteView(PlanRouteViewModel viewModel,
                         ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // === TOP BAR ===
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        userPanel.setOpaque(false);
        usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(0, 102, 204));
        userPanel.add(usernameLabel);

        topBar.add(userPanel, BorderLayout.WEST);

        // === BACK BUTTON ===
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

        // === CENTER: left input + right results ===
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setBackground(Color.WHITE);

        // LEFT: Input form
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 20));

        JLabel titleLabel = new JLabel("Plan Your Route");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Start location
        JLabel startLabel = new JLabel("Start Location:");
        startLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        startLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        startLocationField = new JTextField(25);
        startLocationField.setMaximumSize(new Dimension(300, 30));
        startLocationField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Destination
        JLabel destLabel = new JLabel("Destination:");
        destLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        destLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        destinationField = new JTextField(25);
        destinationField.setMaximumSize(new Dimension(300, 30));
        destinationField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Intermediate stops
        JLabel intermediateLabel = new JLabel("Intermediate Stops (comma-separated, optional):");
        intermediateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        intermediateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        intermediateStopsField = new JTextField(25);
        intermediateStopsField.setMaximumSize(new Dimension(300, 30));
        intermediateStopsField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Error label
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Plan button
        planButton = new JButton("Plan Route");
        planButton.setFont(new Font("Arial", Font.PLAIN, 16));
        planButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        planButton.setMaximumSize(new Dimension(200, 40));
        planButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        planButton.addActionListener(e -> {
            if (controller != null) {
                String start = startLocationField.getText().trim();
                String dest = destinationField.getText().trim();
                String intermediate = intermediateStopsField.getText().trim();
                String[] stops = intermediate.isEmpty() ? new String[0]
                        : intermediate.split(",\\s*");

                controller.planRoute(start, dest, stops);
            }
        });

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(30));
        leftPanel.add(startLabel);
        leftPanel.add(startLocationField);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(destLabel);
        leftPanel.add(destinationField);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(intermediateLabel);
        leftPanel.add(intermediateStopsField);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(errorLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(planButton);

        // RIGHT: Results
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 40));

        JLabel routeLabel = new JLabel("Route Details");
        routeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        routeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Summary
        manualModeIndicator = new JLabel("");
        manualModeIndicator.setForeground(new Color(255, 140, 0)); // orange
        manualModeIndicator.setFont(new Font("Arial", Font.BOLD, 12));
        manualModeIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalDistanceLabel = new JLabel("Total Distance: —");
        totalDistanceLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        totalDistanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalDurationLabel = new JLabel("Total Duration: —");
        totalDurationLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        totalDurationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Steps list
        JLabel stepsLabel = new JLabel("Steps:");
        stepsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stepsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        stepsListModel = new DefaultListModel<>();
        stepsList = new JList<>(stepsListModel);
        stepsList.setFont(new Font("Arial", Font.PLAIN, 12));
        stepsList.setVisibleRowCount(8);
        JScrollPane stepsScroll = new JScrollPane(stepsList);
        stepsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepsScroll.setPreferredSize(new Dimension(350, 200));

        // Start route button
        startRouteButton = new JButton("Start Route");
        startRouteButton.setFont(new Font("Arial", Font.PLAIN, 14));
        startRouteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        startRouteButton.setMaximumSize(new Dimension(150, 35));
        startRouteButton.setEnabled(false);
        startRouteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rightPanel.add(routeLabel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(manualModeIndicator);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(totalDistanceLabel);
        rightPanel.add(totalDurationLabel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(stepsLabel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(stepsScroll);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(startRouteButton);

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void setPlanRouteController(PlanRouteController controller) {
        this.controller = controller;
    }

    public String getViewName() {
        return viewName;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"state".equals(evt.getPropertyName())) return;

        PlanRouteState state = (PlanRouteState) evt.getNewValue();

        // Update display
        totalDistanceLabel.setText("Total Distance: " + state.getTotalDistance());
        totalDurationLabel.setText("Total Duration: " + state.getTotalDuration());

        if (state.isManualMode()) {
            manualModeIndicator.setText("⚠ Self-guided mode (API unavailable)");
        } else {
            manualModeIndicator.setText("");
        }

        // Update steps list
        stepsListModel.clear();
        for (PlanRouteState.StepVM step : state.getSteps()) {
            String line = "• " + step.instruction +
                    " (" + step.distance + ", " + step.duration + ")";
            if (step.landmarkNearby != null && !step.landmarkNearby.isEmpty()) {
                line += " [" + step.landmarkNearby + " nearby]";
            }
            stepsListModel.addElement(line);
        }

        // Enable start button if we have steps
        startRouteButton.setEnabled(!state.getSteps().isEmpty());

        // Show errors
        if (state.getErrorMessage() != null && !state.getErrorMessage().isEmpty()) {
            errorLabel.setText(state.getErrorMessage());
        } else {
            errorLabel.setText("");
        }
    }
}