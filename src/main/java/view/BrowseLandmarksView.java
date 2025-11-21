package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.browselandmarks.BrowseLandmarksController;
import interface_adapter.browselandmarks.BrowseLandmarksState;
import interface_adapter.browselandmarks.BrowseLandmarksViewModel;
import interface_adapter.selectedplace.SelectedPlaceController;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

    public BrowseLandmarksView(BrowseLandmarksViewModel viewModel,
                               BrowseLandmarksController controller,
                               SelectedPlaceController selectedPlaceController,
                               ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.controller = controller;
        this.viewManagerModel = viewManagerModel;
        this.selectedPlaceController = selectedPlaceController;

        this.viewModel.addPropertyChangeListener(this);

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
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));

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

        listModel.clear();
        for (BrowseLandmarksState.LandmarkVM vm : state.getLandmarks()) {
            listModel.addElement(vm.name);
        }

        mapPanel.setLandmarks(state.getLandmarks());

        String username = viewModel.getState().getUsername();
        if (username != null && !username.isEmpty()) {
            usernameLabel.setText(username);
        }
    }

    public String getViewName() {
        return viewName;
    }
}
