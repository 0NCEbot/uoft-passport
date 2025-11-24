package view;

import interface_adapter.EventBus;
import interface_adapter.ViewManagerModel;
import interface_adapter.browselandmarks.BrowseLandmarksController;
import interface_adapter.browselandmarks.BrowseLandmarksState;
import interface_adapter.browselandmarks.BrowseLandmarksViewModel;
import interface_adapter.selectedplace.SelectedPlaceController;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BrowseLandmarksView extends JPanel implements PropertyChangeListener {

    private final String viewName = "browse landmarks";
    private final BrowseLandmarksViewModel viewModel;
    private final BrowseLandmarksController controller;
    private final ViewManagerModel viewManagerModel;

    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JList<String> landmarkList = new JList<>(listModel);
    private final StaticMapPanel mapPanel = new StaticMapPanel();
    private final SelectedPlaceController selectedPlaceController;

    private JLabel usernameLabel;

    // For filtering
    private List<BrowseLandmarksState.LandmarkVM> allLandmarks = new ArrayList<>();
    private JComboBox<String> typeFilterDropdown;
    private String currentVisitFilter = "All";  // "All", "Unvisited", "Visited"

    public BrowseLandmarksView(BrowseLandmarksViewModel viewModel,
                               BrowseLandmarksController controller,
                               SelectedPlaceController selectedPlaceController,
                               ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.selectedPlaceController = selectedPlaceController;

        this.viewModel.addPropertyChangeListener(this);

        // Subscribe to user login to load landmarks with correct visit counts
        EventBus.subscribe("userLoggedIn", payload -> {
            String username = (String) payload;
            BrowseLandmarksState state = viewModel.getState();
            state.setUsername(username);
            viewModel.setState(state);
            controller.loadLandmarks();
        });

        // Subscribe to visit modifications to refresh landmark visit counts
        EventBus.subscribe("visitModified", payload -> {
            controller.loadLandmarks();
        });

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ======= TOP BAR (username + logout) =======
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        userPanel.setOpaque(false);
        usernameLabel = new JLabel("Username1!"); // wire real username later if you want
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameLabel.setForeground(new Color(0, 102, 204));
        userPanel.add(usernameLabel);

        JLabel logoutLabel = new JLabel("Logout");
        logoutLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        logoutLabel.setForeground(new Color(0, 102, 204));
        logoutLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        topBar.add(userPanel, BorderLayout.WEST);
        topBar.add(logoutLabel, BorderLayout.EAST);

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

        // ======= CENTER AREA: left column + map =======
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setBackground(Color.WHITE);

        // ---------- LEFT COLUMN ----------
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 20));

        JLabel titleLabel = new JLabel("Landmarks");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // ======= FILTER PANEL =======
        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.Y_AXIS));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        filterPanel.setMaximumSize(new Dimension(320, 100));

        // Type filter dropdown
        JPanel typeFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        typeFilterRow.setBackground(Color.WHITE);
        JLabel typeLabel = new JLabel("Type: ");
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        typeFilterDropdown = new JComboBox<>(new String[]{"All Types"});
        typeFilterDropdown.setPreferredSize(new Dimension(180, 25));
        typeFilterDropdown.setFont(new Font("Arial", Font.PLAIN, 13));
        typeFilterRow.add(typeLabel);
        typeFilterRow.add(typeFilterDropdown);

        // Visit filter buttons
        JPanel visitFilterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        visitFilterRow.setBackground(Color.WHITE);
        JLabel visitLabel = new JLabel("Visits: ");
        visitLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        visitFilterRow.add(visitLabel);

        JButton allButton = createFilterButton("All", true);
        JButton unvisitedButton = createFilterButton("Unvisited", false);
        JButton visitedButton = createFilterButton("Visited", false);

        visitFilterRow.add(allButton);
        visitFilterRow.add(unvisitedButton);
        visitFilterRow.add(visitedButton);

        filterPanel.add(typeFilterRow);
        filterPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        filterPanel.add(visitFilterRow);

        leftPanel.add(filterPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // BIGGER LIST
        landmarkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        landmarkList.setFont(new Font("Arial", Font.PLAIN, 16));
        landmarkList.setFixedCellHeight(28);

        landmarkList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setFont(new Font("Arial", Font.PLAIN, 16));
                label.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 5));
                if (isSelected) {
                    label.setForeground(Color.WHITE);
                    label.setBackground(new Color(0, 102, 204));
                } else {
                    label.setForeground(new Color(0, 102, 204));
                    label.setBackground(Color.WHITE);
                }
                return label;
            }
        });

        JScrollPane listScroll = new JScrollPane(landmarkList);
        listScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        listScroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        // wider + taller than before
        listScroll.setPreferredSize(new Dimension(320, 550));

        leftPanel.add(listScroll);

        // ---------- RIGHT: MAP ----------
        JPanel mapContainer = new JPanel(new BorderLayout());
        mapContainer.setBackground(Color.WHITE);
        mapContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 40));
        mapContainer.add(mapPanel, BorderLayout.CENTER);

        centerPanel.add(leftPanel);
        centerPanel.add(mapContainer);

        // ======= ADD TO MAIN PANEL =======
        add(topBar, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);

        // ======= BEHAVIOUR =======
        landmarkList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String name = landmarkList.getSelectedValue();
                if (name != null) {
                    String username = viewModel.getState().getUsername();
                    selectedPlaceController.selectPlace(username, name);

                    SwingUtilities.invokeLater(() -> landmarkList.clearSelection());

                }
            }
        });

        mapPanel.setMarkerClickListener(name -> {
            System.out.println("Map clicked: " + name);
            landmarkList.setSelectedValue(name, true);
        });

        controller.loadLandmarks();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!"state".equals(evt.getPropertyName())) return;

        BrowseLandmarksState state = (BrowseLandmarksState) evt.getNewValue();

        // Store all landmarks for filtering
        allLandmarks = new ArrayList<>(state.getLandmarks());

        // Store current filter selections to preserve them
        String previousTypeSelection = (String) typeFilterDropdown.getSelectedItem();

        // Populate type dropdown with unique types
        List<String> uniqueTypes = allLandmarks.stream()
                .map(vm -> vm.type)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        typeFilterDropdown.removeAllItems();
        typeFilterDropdown.addItem("All Types");
        for (String type : uniqueTypes) {
            typeFilterDropdown.addItem(type);
        }

        // Restore previous type selection if it still exists
        if (previousTypeSelection != null) {
            for (int i = 0; i < typeFilterDropdown.getItemCount(); i++) {
                if (typeFilterDropdown.getItemAt(i).equals(previousTypeSelection)) {
                    typeFilterDropdown.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Add listener for type dropdown (only once)
        if (typeFilterDropdown.getActionListeners().length == 0) {
            typeFilterDropdown.addActionListener(e -> applyFilters());
        }

        // Apply filters to show landmarks
        applyFilters();

        String username = viewModel.getState().getUsername();
        if (username != null && !username.isEmpty()) {
            usernameLabel.setText(username);
        }
    }

    public String getViewName() {
        return viewName;
    }

    private JButton createFilterButton(String text, boolean isSelected) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(80, 25));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (isSelected) {
            button.setBackground(new Color(0, 102, 204));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(new Color(0, 102, 204));
            button.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 1));
        }

        // Add action listener to handle button clicks
        button.addActionListener(e -> {
            currentVisitFilter = text;
            // Update all visit filter buttons
            Container parent = button.getParent();
            for (Component comp : parent.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton) comp;
                    String btnText = btn.getText();
                    if (btnText.equals("All") || btnText.equals("Unvisited") || btnText.equals("Visited")) {
                        if (btnText.equals(text)) {
                            btn.setBackground(new Color(0, 102, 204));
                            btn.setForeground(Color.WHITE);
                            btn.setBorderPainted(false);
                        } else {
                            btn.setBackground(Color.WHITE);
                            btn.setForeground(new Color(0, 102, 204));
                            btn.setBorder(BorderFactory.createLineBorder(new Color(0, 102, 204), 1));
                        }
                    }
                }
            }
            applyFilters();
        });

        return button;
    }

    private void applyFilters() {
        String selectedType = (String) typeFilterDropdown.getSelectedItem();

        List<BrowseLandmarksState.LandmarkVM> filtered = allLandmarks.stream()
                .filter(landmark -> {
                    // Type filter
                    boolean typeMatch = "All Types".equals(selectedType) || landmark.type.equals(selectedType);

                    // Visit filter
                    boolean visitMatch = true;
                    if ("Unvisited".equals(currentVisitFilter)) {
                        visitMatch = landmark.visitCount == 0;
                    } else if ("Visited".equals(currentVisitFilter)) {
                        visitMatch = landmark.visitCount > 0;
                    }

                    return typeMatch && visitMatch;
                })
                .collect(Collectors.toList());

        // Update the list
        listModel.clear();
        for (BrowseLandmarksState.LandmarkVM vm : filtered) {
            listModel.addElement(vm.name);
        }

        // Update the map with filtered landmarks
        mapPanel.setLandmarks(filtered);
    }
}
