import javax.swing.*;
import java.awt.*;

public class Boot {

    //Boot-up sequence with terminal flair and GUI initialization yesserskiiiiiiiiii//

    // Reference to the GUI
    private GameGUI gameGUI;

    // Terminal Colors
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String PURPLE = "\u001B[35m";

   
public void launch() {
    printEpicHeader();

    loadingSequence("\t\t\t" + PURPLE + "CONNECTING TO S.H.I.E.L.D. DATABASE" + RESET, 3);
    loadingSequence("\t\t\t" + PURPLE + "BYPASSING HYDRA FIREWALLS" + RESET, 4);
    loadingSequence("\t\t\t" + PURPLE + "AUTHENTICATING AVENGERS INITIATIVE" + RESET, 2);

    System.out.println(GREEN + "\n\t\t\t[SUCCESS] Access Granted. Welcome, Director." + RESET);
    System.out.println("\t\t\t---------------------------------\n");

      // Create AND show the GUI in a single invokeLater — no race condition
      SwingUtilities.invokeLater(() -> {
          try {
              UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
              UIManager.put("Button.font", new Font("Impact", Font.PLAIN, 24));
          } catch (Exception e) {
              System.out.println(RED + "\t\t\t[ERROR] LookAndFeel Failure: " + e.getMessage() + RESET);
          }

          gameGUI = new GameGUI();
          gameGUI.setVisible(true);
          System.out.println(GREEN + "\t\t\t[INFO] Window Deployed to Desktop." + RESET);
      });

      startupComplete();
  }

    private void printEpicHeader() {
        System.out.println("\n\n\n\n\n\n");
        System.out.println(CYAN + "\t\t\t#################################################" + RESET);
        System.out.println(CYAN + "\t\t\t#                                               #" + RESET);
        System.out.println(CYAN + "\t\t\t#      M A R V E L    A S C E N S I O N         #" + RESET);
        System.out.println(CYAN + "\t\t\t#           --- by Group Unturned ---           #" + RESET);
        System.out.println(CYAN + "\t\t\t#                                               #" + RESET);
        System.out.println(CYAN + "\t\t\t#################################################" + RESET);
    }

    private void loadingSequence(String message, int dots) {
        System.out.print("> " + message);
        for (int i = 0; i < dots; i++) {
            try {
                Thread.sleep(400);
                System.out.print(".");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("\t\t\t[OK]");
    }

    private void initializeGUI() {
        System.out.println(GREEN + "\t\t\t[INFO] Initializing GUI Engine..." + RESET);
        
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                UIManager.put("Button.font", new Font("Impact", Font.PLAIN, 24)); 
            } catch (Exception e) {
                System.out.println(RED + "\t\t\t[ERROR] LookAndFeel Failure: " + e.getMessage() + RESET);
            }
            
            gameGUI = new GameGUI();
            System.out.println(GREEN + "\t\t\t[INFO] UI Components Rendered." + RESET);
        });
        
        try { Thread.sleep(800); } catch (InterruptedException e) {}
    }

    private void customizeGUI() {
        if (gameGUI != null) {
            SwingUtilities.invokeLater(() -> {
                System.out.println(GREEN + "\t\t\t[INFO] Applying Epic Theme Overlays..." + RESET);
            });
        }
    }

    private void showGUI() {
        if (gameGUI != null) {
            SwingUtilities.invokeLater(() -> {
                gameGUI.setVisible(true);
                System.out.println(GREEN + "\t\t\t[INFO] Window Deployed to Desktop." + RESET);
            });
        }
    }

    private void startupComplete() {
        System.out.println(GREEN + "\n\t\t\t=================================" + RESET);
        System.out.println(GREEN + "\t\t\t   MISSION START: HAVE FUN!!! " + RESET);
        System.out.println(GREEN + "\t\t\t=================================\n" + RESET);
    }
}
