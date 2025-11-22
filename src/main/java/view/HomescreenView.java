package view;

import interface_adapter.ViewManagerModel;
import interface_adapter.homescreen.HomescreenViewModel;
import interface_adapter.homescreen.HomescreenController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.image.BufferedImage;
import java.awt.geom.RoundRectangle2D;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class HomescreenView extends JPanel implements PropertyChangeListener {
    private final String viewName = "homescreen";
    private final HomescreenViewModel viewModel;
    private final ViewManagerModel viewManagerModel;
    private HomescreenController controller;

    private JLabel usernameLabel;

    public HomescreenView(HomescreenViewModel viewModel, ViewManagerModel viewManagerModel) {
        this.viewModel = viewModel;
        this.viewManagerModel = viewManagerModel;

        this.viewModel.addPropertyChangeListener(this);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);


        //(top panel)- username area
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.setPreferredSize(new Dimension(getWidth(), 50));
        usernameLabel = new JLabel("placeholder_username");
        usernameLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        topPanel.add(usernameLabel);

        //(bottom panel)- back button area
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setPreferredSize(new Dimension(getWidth(), 50));
        JButton backButton = new JButton("back");
        backButton.setFont(new Font("Arial", Font.PLAIN, 14));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setForeground(Color.BLUE);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        bottomPanel.add(backButton);
        backButton.addActionListener(e -> {
            viewManagerModel.setState("log in");
            viewManagerModel.firePropertyChange();
        });


        //(centre panel)- main content area (split left/right)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));
        centerPanel.setBackground(Color.WHITE);

        //(left panel)- title and buttons
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 20));

        //title section
        JLabel titleLabel = new JLabel("Welcome!");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Ready to explore?");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        leftPanel.add(titleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(subtitleLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        //buttons- add section listeners here
        JButton browseLandmarksButton = createStyledButton("Browse Landmarks");
        browseLandmarksButton.addActionListener(e -> {
            if (controller != null) {
                controller.browseLandmarks();
            }
        });

        JButton planRouteButton = createStyledButton("Plan A Route");
        planRouteButton.addActionListener(e -> {
            if (controller != null) {
                controller.planRoute();
            }
        });

        JButton myProgressButton = createStyledButton("My Progress");
        myProgressButton.addActionListener(e -> {
            if (controller != null) {
                controller.viewProgress();
            }
        });



        leftPanel.add(browseLandmarksButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(planRouteButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanel.add(myProgressButton);




        //(right panel)- image/map
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 50)); // Add padding

        //load and scale the image
        ImageIcon originalIcon = new ImageIcon("src/main/resources/placeholder_img2.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);

        // Create rounded image
        BufferedImage roundedImage = createRoundedImage(scaledImage, 30); // 30 = corner radius
        ImageIcon scaledIcon = new ImageIcon(roundedImage);

        JLabel imageLabel = new JLabel();
        imageLabel.setIcon(scaledIcon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(imageLabel, BorderLayout.CENTER);

        //add left and right to center panel
        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);

        //add all panels to main layout
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setHomescreenController(HomescreenController controller) {
        this.controller = controller;
    }



    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 220), 2));
        button.setBackground(Color.WHITE);
        button.setForeground(new Color(30, 80, 180));
        button.setPreferredSize(new Dimension(350, 70));
        button.setMaximumSize(new Dimension(350, 70));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        //add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBorder(BorderFactory.createLineBorder(new Color(70, 130, 220), 2));
            }
        });

        return button;
    }

    //using awt to create the rounded edges
    private BufferedImage createRoundedImage(Image image, int cornerRadius) {
        // Force the image to load completely
        ImageIcon temp = new ImageIcon(image);
        int width = temp.getIconWidth();
        int height = temp.getIconHeight();

        // Safety check
        if (width <= 0 || height <= 0) {
            // Return a default image or handle error
            BufferedImage defaultImage = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
            return defaultImage;
        }

        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = roundedImage.createGraphics();

        // Enable anti-aliasing for smooth edges
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Create rounded rectangle clip
        g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));

        // Draw the image
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return roundedImage;
    }

    // ADD THIS METHOD
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("state")) {
            String username = viewModel.getState().getUsername();
            if (username != null && !username.isEmpty()) {
                usernameLabel.setText(username);
            }
        }
    }

    public String getViewName() {
        return viewName;
    }

}



