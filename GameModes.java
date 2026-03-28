import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameModes extends JPanel {
    private final GameGUI mainFrame;

    public GameModes(GameGUI frame) {
        this.mainFrame = frame;
        setLayout(new BorderLayout());
        setBackground(new Color(10, 10, 15));

        JLabel header = new JLabel("SELECT OPERATION", SwingConstants.CENTER);
        header.setFont(new Font("Impact", Font.PLAIN, 50));
        header.setForeground(new Color(255, 0, 0));
        header.setBorder(new EmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        JPanel cardPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        cardPanel.setOpaque(false);
        cardPanel.setBorder(new EmptyBorder(50, 50, 50, 50));

        cardPanel.add(createModeBtn("STRIKE (PVP)",       "Local 1v1",    Color.RED,             "PVP"));
        cardPanel.add(createModeBtn("GAUNTLET (AI)",      "Single Fight", Color.CYAN,            "AI"));
        cardPanel.add(createModeBtn("ASCENSION (LADDER)", "Boss Rush",    new Color(128,0,255),  "LADDER"));

        add(cardPanel, BorderLayout.CENTER);

        JButton back = new JButton("ABORT SELECTION");
        back.setFont(new Font("Arial", Font.BOLD, 18));
        back.addActionListener(e -> mainFrame.navigateTo("main"));
        add(back, BorderLayout.SOUTH);
    }

    private JButton createModeBtn(String title, String sub, Color c, String mode) {
        JButton b = new JButton("<html><center><font size='6'>" + title
                + "</font><br><font size='4'>" + sub + "</font></center></html>");
        b.setFont(new Font("SansSerif", Font.BOLD, 20));
        b.setBackground(new Color(30, 30, 40));
        b.setForeground(c);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createLineBorder(c, 3));
        b.addActionListener(e -> {
            mainFrame.setCurrentMode(mode);
            mainFrame.getSelectorPanel().resetSelections();
            mainFrame.navigateTo("selector");
        });
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(new Color(50, 50, 60)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(new Color(30, 30, 40)); }
        });
        return b;
    }
}
