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

    // UI Components
    private JTextField startLocationField;
    private JTextField destinationField;
    private JTextField intermediateStopsField;

    private JLabel usernameLabel;
    private JLabel totalDistanceLabel;
    private JLabel totalDurationLabel;
    private JLabel errorLabel;
    private JList<String> stepsList;
    private DefaultListModel<String> stepsListModel;
    private JLabel manualModeIndicator;
    private JLabel mapImageLabel;

    private JButton planButton;
    private JButton completeStepButton;
    private JButton checkInButton;

    public PlanRouteView(PlanRouteViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;
        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ==========================
        // 1. INITIALIZE COMPONENTS
        // ==========================

        // --- Top/Bottom Bars ---
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

        // --- Inputs (Left Side) ---
        JLabel titleLabel = new JLabel("Plan Your Route");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel startLabel = new JLabel("Start Landmark:");
        startLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        startLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        startLocationField = new JTextField(25);
        startLocationField.setMaximumSize(new Dimension(300, 30));
        startLocationField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel destLabel = new JLabel("End Landmark:");
        destLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        destLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        destinationField = new JTextField(25);
        destinationField.setMaximumSize(new Dimension(300, 30));
        destinationField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel intermediateLabel = new JLabel("<html>Intermediate Landmarks:<br>(comma-separated, optional)</html>");
        intermediateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        intermediateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        intermediateStopsField = new JTextField(25);
        intermediateStopsField.setMaximumSize(new Dimension(300, 30));
        intermediateStopsField.setAlignmentX(Component.LEFT_ALIGNMENT);

        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

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
                String[] stops = intermediate.isEmpty() ? new String[0] : intermediate.split(",\\s*");
                controller.planRoute(start, dest, stops);
            }
        });

        // --- Steps List (Moved to Left) ---
        JLabel stepsLabel = new JLabel("Steps:");
        stepsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        stepsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        stepsListModel = new DefaultListModel<>();
        stepsList = new JList<>(stepsListModel);
        stepsList.setFont(new Font("Arial", Font.PLAIN, 12));
        stepsList.setVisibleRowCount(6); // Controls height based on rows

        // Custom Renderer
        stepsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                PlanRouteState state = viewModel.getState();
                if (state != null && index < state.getSteps().size()) {
                    PlanRouteState.StepVM step = state.getSteps().get(index);
                    if (step.completed) {
                        label.setForeground(Color.GRAY);
                        label.setFont(label.getFont().deriveFont(Font.ITALIC));
                    } else if (index == state.getCurrentStepIndex()) {
                        label.setForeground(new Color(0, 102, 204));
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                    }
                }
                return label;
            }
        });

        JScrollPane stepsScroll = new JScrollPane(stepsList);
        stepsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Increased height slightly so it fits well under the inputs
        stepsScroll.setPreferredSize(new Dimension(300, 200));
        stepsScroll.setMaximumSize(new Dimension(300, 200));

        // --- Action Buttons (Moved to Left) ---
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionButtonsPanel.setOpaque(false);
        actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        completeStepButton = new JButton("Complete Step");
        completeStepButton.setFont(new Font("Arial", Font.PLAIN, 14));
        completeStepButton.setEnabled(false);
        completeStepButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        completeStepButton.addActionListener(e -> { if (controller != null) controller.completeStep(); });

        checkInButton = new JButton("Check In");
        checkInButton.setFont(new Font("Arial", Font.PLAIN, 14));
        checkInButton.setEnabled(false);
        checkInButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkInButton.setBackground(new Color(0, 180, 0));
        checkInButton.setForeground(Color.WHITE);
        checkInButton.setOpaque(true);
        checkInButton.setBorderPainted(false);
        checkInButton.addActionListener(e -> {
            if (controller != null) {
                String landmarkName = controller.checkInAtLandmark();
                if (landmarkName != null) {
                    JOptionPane.showMessageDialog(this, "Checked in at " + landmarkName + "!",
                            "Check-In Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        actionButtonsPanel.add(completeStepButton);
        actionButtonsPanel.add(Box.createHorizontalStrut(10)); // Space between buttons
        actionButtonsPanel.add(checkInButton);

        // --- Stats & Map (Right Side) ---
        JLabel routeLabel = new JLabel("Route Details");
        routeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        routeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        manualModeIndicator = new JLabel("");
        manualModeIndicator.setForeground(new Color(255, 140, 0));
        manualModeIndicator.setFont(new Font("Arial", Font.BOLD, 12));
        manualModeIndicator.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalDistanceLabel = new JLabel("Total Distance: —");
        totalDistanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalDistanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalDurationLabel = new JLabel("Total Duration: —");
        totalDurationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalDurationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mapImageLabel = new JLabel();
        mapImageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mapImageLabel.setPreferredSize(new Dimension(400, 400));
        mapImageLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));


        // ==========================
        // 2. BUILD LAYOUT
        // ==========================

        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setBackground(Color.WHITE);

        // --- Construct LEFT Panel ---
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createVerticalStrut(20));
        leftPanel.add(startLabel);
        leftPanel.add(startLocationField);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(destLabel);
        leftPanel.add(destinationField);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(intermediateLabel);
        leftPanel.add(intermediateStopsField);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(errorLabel);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(planButton);

        // NEW: Adding Steps and Buttons to Left Panel
        leftPanel.add(Box.createVerticalStrut(20)); // Separator
        leftPanel.add(stepsLabel);
        leftPanel.add(Box.createVerticalStrut(5));
        leftPanel.add(stepsScroll);
        leftPanel.add(Box.createVerticalStrut(10));
        leftPanel.add(actionButtonsPanel);

        // --- Construct RIGHT Panel ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 40));

        rightPanel.add(routeLabel);
        rightPanel.add(Box.createVerticalStrut(10));
        rightPanel.add(manualModeIndicator);
        rightPanel.add(totalDistanceLabel);
        rightPanel.add(totalDurationLabel);
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(mapImageLabel);

        // Add panels to center
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        // Add everything to Main View
        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    public void setPlanRouteController(PlanRouteController controller) {
        this.controller = controller;
    }

    public String getViewName() { return viewName; }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"state".equals(evt.getPropertyName())) return;

        PlanRouteState state = (PlanRouteState) evt.getNewValue();

        totalDistanceLabel.setText("Total Distance: " + state.getTotalDistance());
        totalDurationLabel.setText("Total Duration: " + state.getTotalDuration());

        manualModeIndicator.setText(state.isManualMode() ? "⚠ Self-guided mode (API unavailable)" : "");

        // Update map image
        byte[] mapBytes = state.getMapImageBytes();
        if (mapBytes != null && mapBytes.length > 0) {
            ImageIcon mapIcon = new ImageIcon(mapBytes);
            mapImageLabel.setIcon(mapIcon);
            mapImageLabel.setText("");
        } else {
            mapImageLabel.setIcon(null);
            mapImageLabel.setText("Map unavailable");
        }

        stepsListModel.clear();
        if (state.getSteps() != null) {
            for (PlanRouteState.StepVM step : state.getSteps()) {
                String line = step.instruction;
                if (!step.isLandmark && step.distance != null && !step.distance.isEmpty()) {
                    line += " (" + step.distance;
                    if (step.duration != null && !step.duration.isEmpty()) line += ", " + step.duration;
                    line += ")";
                }
                stepsListModel.addElement(line);
            }
        }

        PlanRouteState.StepVM currentStep = state.getCurrentStep();
        if (state.isRouteCompleted()) {
            completeStepButton.setEnabled(false);
            checkInButton.setEnabled(false);
        } else if (currentStep != null) {
            completeStepButton.setEnabled(!currentStep.isLandmark);
            checkInButton.setEnabled(currentStep.isLandmark);
            checkInButton.setBackground(currentStep.isLandmark ? new Color(0, 180, 0) : new Color(200, 200, 200));
            checkInButton.setForeground(currentStep.isLandmark ? Color.WHITE : new Color(140, 140, 140));
        }

        if (!state.isRouteCompleted() && state.getCurrentStepIndex() >= 0) {
            stepsList.ensureIndexIsVisible(state.getCurrentStepIndex());
        }

        errorLabel.setText(state.getErrorMessage() != null ? state.getErrorMessage() : "");
        stepsList.repaint();
    }
}