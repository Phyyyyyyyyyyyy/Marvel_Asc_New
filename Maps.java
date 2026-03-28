import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Maps extends JPanel {

    private final GameGUI mainFrame;
    private static final Random random = new Random();

    // MAP_DATA format: { "Display Name", "thumbnail file key" }
    private static final String[][] MAP_DATA = {
        { "Asgard",                         "asgardgamebg"        },
        { "Avengers Tower",                 "avengerstowercover"   },
        { "Avengers HQ",                    "avengerstowerinside"  },
        { "CIT-U Outside Basketball Court", "citubballcourt"       },
        { "Jollibee Arena",                 "jollibeeinside"       },
        { "Nyan Realm",                     "nyanmap"              },
        { "Sokovia",                        "sokoviagamemap"       },
        { "Titan",                          "titangame"            },
        { "T'Challa Throne Room",           "wakandainside"        },
    };

    private final List<String[]> actualMaps = new ArrayList<>();
    
    private String  selectedMapName = null;
    private String  selectedMapKey  = null;
    private JButton selectedButton  = null;
    private final List<JButton> allMapButtons = new ArrayList<>();

    private JLabel statusBar;
    private JLabel toastLabel;
    private Timer  toastTimer;
    
    // Animation components
    private JDialog animationDialog;
    private JLabel animationLabel;
    private Timer animationTimer;
    private int animationStep = 0;
    private String[] randomizingMaps;
    private boolean isAnimating = false;

    public Maps(GameGUI frame) {
        this.mainFrame = frame;
        
        // Initialize actual maps list
        for (String[] map : MAP_DATA) {
            actualMaps.add(map);
        }
        
        // Prepare randomizing maps for animation
        randomizingMaps = new String[actualMaps.size()];
        for (int i = 0; i < actualMaps.size(); i++) {
            randomizingMaps[i] = actualMaps.get(i)[0];
        }
        
        setupLayout();
    }

    public void resetSelection() {
        selectedMapName = null;
        selectedMapKey  = null;
        selectedButton  = null;
        for (JButton b : allMapButtons)
            b.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
        updateStatusBar();
    }

    public void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(20, 20, 20));

        // Header
        JLabel header = new JLabel("SELECT YOUR BATTLEFIELD", SwingConstants.CENTER);
        header.setFont(new Font("Verdana", Font.BOLD, 38));
        header.setForeground(new Color(255, 215, 0));
        header.setBorder(BorderFactory.createEmptyBorder(16, 0, 4, 0));
        add(header, BorderLayout.NORTH);

        // Hero grid inside a JLayeredPane so the toast floats on top
        JLayeredPane layered = new JLayeredPane();

        JPanel grid = new JPanel(new GridLayout(3, 4, 15, 15)) {
            public Dimension getPreferredSize() { return layered.getSize(); }
        };
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Add all actual maps
        for (String[] map : MAP_DATA) {
            grid.add(createMapButton(map[0], map[1]));
        }
        
        // Add Random Stage button (special)
        grid.add(createRandomStageButton());
        
        grid.setBounds(0, 0, 1, 1);
        layered.add(grid, JLayeredPane.DEFAULT_LAYER);

        toastLabel = new JLabel("", SwingConstants.CENTER);
        toastLabel.setFont(new Font("Impact", Font.PLAIN, 28));
        toastLabel.setForeground(Color.WHITE);
        toastLabel.setOpaque(true);
        toastLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 2),
            BorderFactory.createEmptyBorder(10, 24, 10, 24)
        ));
        toastLabel.setVisible(false);
        toastLabel.setBounds(0, 0, 1, 1);
        layered.add(toastLabel, JLayeredPane.POPUP_LAYER);

        layered.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                int w = layered.getWidth(), h = layered.getHeight();
                grid.setBounds(0, 0, w, h);
                toastLabel.setBounds(0, (h - 60) / 2, w, 60);
            }
        });

        add(layered, BorderLayout.CENTER);

        // South: status bar + footer
        JPanel south = new JPanel(new BorderLayout(4, 4));
        south.setOpaque(false);

        statusBar = new JLabel("", SwingConstants.CENTER);
        statusBar.setFont(new Font("Arial", Font.BOLD, 14));
        statusBar.setForeground(Color.WHITE);
        statusBar.setOpaque(true);
        statusBar.setBackground(new Color(25, 25, 25));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(255, 215, 0)),
            BorderFactory.createEmptyBorder(5, 0, 5, 0)
        ));
        updateStatusBar();
        south.add(statusBar, BorderLayout.NORTH);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 10));
        footer.setOpaque(false);

        JButton backBtn    = new JButton("BACK TO MENU");
        JButton confirmBtn = new JButton("CONFIRM STAGE");
        confirmBtn.setFont(new Font("Impact", Font.PLAIN, 20));
        confirmBtn.setForeground(new Color(255, 215, 0));
        confirmBtn.setBackground(new Color(40, 80, 40));
        confirmBtn.setFocusPainted(false);

        JTextField secretInput = new JTextField(8);
        JButton    unlockBtn   = new JButton("ACCESS SECRET FILES");

        backBtn.addActionListener(e -> {
            resetSelection();
            mainFrame.navigateTo("main");
        });

        confirmBtn.addActionListener(e -> confirmStage());

        unlockBtn.addActionListener(e -> {
            try {
                int code = Integer.parseInt(secretInput.getText().trim());
                if (code == 9999) {
                    showRandomizationAnimation();
                } else {
                    JOptionPane.showMessageDialog(this, "Access Denied: Invalid Code");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter numeric access code");
            }
        });

        footer.add(backBtn);
        JLabel codeLabel = new JLabel("SECURE CODE:");
        codeLabel.setForeground(Color.WHITE);
        footer.add(codeLabel);
        footer.add(secretInput);
        footer.add(unlockBtn);
        footer.add(confirmBtn);
        south.add(footer, BorderLayout.SOUTH);
        add(south, BorderLayout.SOUTH);
    }
    
    /**
     * Show randomization animation with spinning map names
     */
    private void showRandomizationAnimation() {
        if (isAnimating) return;
        isAnimating = true;
        
        // Create a modal dialog for the animation
        animationDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Randomizing...", true);
        animationDialog.setUndecorated(true);
        animationDialog.setSize(500, 350);
        animationDialog.setLocationRelativeTo(this);
        animationDialog.setBackground(new Color(0, 0, 0, 0));
        
        // Create main panel with gradient background
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(30, 30, 50), 
                    getWidth(), getHeight(), new Color(10, 10, 30));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw decorative border
                g2d.setColor(new Color(255, 215, 0, 100));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
            }
        };
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        panel.setOpaque(false);
        
        // Title with animation effect
        JLabel titleLabel = new JLabel("RANDOMIZING STAGE", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Impact", Font.PLAIN, 32));
        titleLabel.setForeground(new Color(255, 215, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Center panel for the spinning animation
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        
        // Map name display (will cycle through maps)
        animationLabel = new JLabel("", SwingConstants.CENTER);
        animationLabel.setFont(new Font("Impact", Font.PLAIN, 28));
        animationLabel.setForeground(Color.WHITE);
        animationLabel.setBackground(new Color(0, 0, 0, 150));
        animationLabel.setOpaque(true);
        animationLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
            BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));
        
        // Loading spinner text
        JLabel spinnerLabel = new JLabel("- - -", SwingConstants.CENTER);
        spinnerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        spinnerLabel.setForeground(new Color(100, 200, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);
        centerPanel.add(animationLabel, gbc);
        gbc.gridy = 1;
        centerPanel.add(spinnerLabel, gbc);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Footer with instruction
        JLabel instructionLabel = new JLabel("Randomizing... please wait", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        instructionLabel.setForeground(new Color(150, 150, 200));
        panel.add(instructionLabel, BorderLayout.SOUTH);
        
        animationDialog.add(panel);
        
        // Start animation timer
        animationStep = 0;
        animationTimer = new Timer(80, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cycle through map names
                int index = animationStep % randomizingMaps.length;
                String currentMap = randomizingMaps[index];
                animationLabel.setText(currentMap);
                
                // Add visual effect - pulse effect
                if (animationStep % 6 == 0) {
                    animationLabel.setForeground(new Color(255, 215, 0));
                    Timer pulseTimer = new Timer(40, new ActionListener() {
                        int pulseStep = 0;
                        @Override
                        public void actionPerformed(ActionEvent evt) {
                            if (pulseStep < 3) {
                                animationLabel.setForeground(new Color(1.0f, 0.8f + (pulseStep * 0.07f), 0.2f));
                                pulseStep++;
                            } else {
                                animationLabel.setForeground(Color.WHITE);
                                ((Timer)evt.getSource()).stop();
                            }
                        }
                    });
                    pulseTimer.setRepeats(true);
                    pulseTimer.start();
                }
                
                // Update spinner animation
                String[] spinnerFrames = {"-", "- -", "- - -", "- - - -"};
                spinnerLabel.setText(spinnerFrames[animationStep % spinnerFrames.length]);
                
                animationStep++;
            }
        });
        
        // Stop animation after 2 seconds and select random map
        Timer stopTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationTimer.stop();
                
                // Select random map
                int randomIndex = random.nextInt(actualMaps.size());
                String[] randomMap = actualMaps.get(randomIndex);
                String finalMapName = randomMap[0];
                String finalMapKey = randomMap[1];
                
                // Show final result with celebration effect
                animationLabel.setText(">> " + finalMapName + " <<");
                animationLabel.setForeground(new Color(255, 215, 0));
                animationLabel.setFont(new Font("Impact", Font.PLAIN, 32));
                spinnerLabel.setText("LOCKED IN");
                spinnerLabel.setForeground(new Color(100, 255, 100));
                
                // Close dialog after short delay
                Timer closeTimer = new Timer(800, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        animationDialog.dispose();
                        isAnimating = false;
                        
                        // Set the selected map
                        selectedMapName = finalMapName;
                        selectedMapKey = finalMapKey;
                        
                        // Update UI
                        if (selectedButton != null) {
                            selectedButton.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
                        }
                        selectedButton = null;
                        selectedMapName = finalMapName;
                        selectedMapKey = finalMapKey;
                        
                        updateStatusBar();
                        showToast("RANDOMIZED: " + finalMapName.toUpperCase() + "!", new Color(100, 50, 150));
                        
                        // Confirm to proceed
                        int confirm = JOptionPane.showConfirmDialog(Maps.this,
                            "Random Stage selected: " + finalMapName + "\nProceed to battle?",
                            "Stage Selected", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        
                        if (confirm == JOptionPane.YES_OPTION) {
                            confirmStageWithMap(finalMapName);
                        }
                    }
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
        stopTimer.setRepeats(false);
        
        animationTimer.start();
        stopTimer.start();
        
        animationDialog.setVisible(true);
    }
    
    private JButton createRandomStageButton() {
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw gradient background
                GradientPaint gp = new GradientPaint(0, 0, new Color(100, 50, 150), 
                    getWidth(), getHeight(), new Color(150, 50, 200));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Draw question mark
                g2d.setColor(new Color(255, 215, 0));
                g2d.setFont(new Font("Impact", Font.BOLD, 80));
                FontMetrics fm = g2d.getFontMetrics();
                String questionMark = "?";
                int x = (getWidth() - fm.stringWidth(questionMark)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(questionMark, x, y);
                
                // Draw sparkle effects around the question mark
                g2d.setColor(new Color(255, 255, 100, 150));
                for (int i = 0; i < 8; i++) {
                    double angle = System.currentTimeMillis() / 500.0 + i;
                    int sx = x + fm.stringWidth(questionMark) / 2 + (int)(Math.sin(angle) * 25);
                    int sy = y - fm.getHeight() / 2 + (int)(Math.cos(angle * 1.5) * 20);
                    g2d.fillOval(sx, sy, 4, 4);
                }
                
                // Dark strip at the bottom for the name label
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(0, getHeight() - 30, getWidth(), 30);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 13));
                FontMetrics fm2 = g2d.getFontMetrics();
                String text = "RANDOM STAGE";
                int tw = fm2.stringWidth(text);
                g2d.drawString(text, (getWidth() - tw) / 2, getHeight() - 9);
            }
        };
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != selectedButton)
                    btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != selectedButton)
                    btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
            }
        });
        
        btn.addActionListener(e -> {
            if (selectedButton != null)
                selectedButton.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
            selectedButton = btn;
            selectedMapName = "Random Stage";
            selectedMapKey = null;
            btn.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3));
            updateStatusBar();
            showToast("RANDOM STAGE SELECTED! Click confirm for a surprise!", new Color(150, 0, 150));
        });
        
        allMapButtons.add(btn);
        return btn;
    }

    private JButton createMapButton(String displayName, String fileKey) {
        final Image img = loadFromDisk(fileKey);

        final String initials = java.util.Arrays.stream(displayName.split(" "))
            .map(w -> String.valueOf(w.charAt(0)).toUpperCase())
            .collect(java.util.stream.Collectors.joining());

        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                if (img != null) {
                    g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g2d.setColor(new Color(30, 30, 45));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    g2d.setColor(new Color(255, 215, 0));
                    Font f = new Font("Impact", Font.PLAIN, Math.min(getWidth(), getHeight()) / 3);
                    g2d.setFont(f);
                    FontMetrics fm = g2d.getFontMetrics();
                    g2d.drawString(initials, (getWidth() - fm.stringWidth(initials)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                }
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRect(0, getHeight() - 28, getWidth(), 28);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                FontMetrics fm = g2d.getFontMetrics();
                int tw = fm.stringWidth(displayName.toUpperCase());
                g2d.drawString(displayName.toUpperCase(), (getWidth() - tw) / 2, getHeight() - 9);
            }
        };
        btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (btn != selectedButton)
                    btn.setBorder(BorderFactory.createLineBorder(Color.YELLOW, 2));
            }
            public void mouseExited(MouseEvent e) {
                if (btn != selectedButton)
                    btn.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
            }
        });

        btn.addActionListener(e -> {
            if (selectedButton != null)
                selectedButton.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 80), 2));
            selectedButton = btn;
            selectedMapName = displayName;
            selectedMapKey = fileKey;
            btn.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 3));
            updateStatusBar();
            showToast("STAGE LOCKED - " + displayName.toUpperCase() + "!", new Color(20, 100, 20));
        });

        allMapButtons.add(btn);
        return btn;
    }

    private void confirmStage() {
        if (selectedMapName == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a stage first!", "No Stage Selected",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Handle Random Stage - show animation
        if ("Random Stage".equals(selectedMapName)) {
            showRandomizationAnimation();
            return;
        }
        
        confirmStageWithMap(selectedMapName);
    }
    
    private void confirmStageWithMap(String mapName) {
        String mode = mainFrame.getCurrentMode();
        String heroName = mainFrame.getSelectedHeroName();
        
        if ("PVP".equals(mode)) {
            mainFrame.startPvpBattle(mapName);
        } else if ("AI".equals(mode)) {
            mainFrame.startGauntletBattle(heroName, mapName);
        } else {
            JOptionPane.showMessageDialog(this, "ASCENSION mode coming soon!");
        }
    }

    private void updateStatusBar() {
        if (selectedMapName != null) {
            if ("Random Stage".equals(selectedMapName)) {
                statusBar.setText("<html><center>Stage: <font color='#FFD700'><b>RANDOM (Surprise!)</b></font></center></html>");
            } else {
                statusBar.setText("<html><center>Stage: <font color='#FFD700'><b>"
                    + selectedMapName + "</b></font></center></html>");
            }
        } else {
            statusBar.setText("<html><center><font color='#888888'>No stage selected</font></center></html>");
        }
    }

    private void showToast(String message, Color bg) {
        toastLabel.setText(message);
        toastLabel.setBackground(bg);
        toastLabel.setVisible(true);
        toastLabel.getParent().repaint();
        if (toastTimer != null && toastTimer.isRunning()) toastTimer.stop();
        toastTimer = new Timer(1800, e -> toastLabel.setVisible(false));
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    private Image loadFromDisk(String fileKey) {
        java.io.File png = new java.io.File("maps/" + fileKey + ".png");
        if (png.exists()) return new ImageIcon(png.getPath()).getImage();
        java.io.File jpg = new java.io.File("maps/" + fileKey + ".jpg");
        if (jpg.exists()) return new ImageIcon(jpg.getPath()).getImage();
        return null;
    }

    public String getSelectedMapKey() { return selectedMapKey; }
}
