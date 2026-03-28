import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Random;

public abstract class BaseArena extends JPanel {

    protected final GameGUI mainFrame;
    protected Image battlefieldImage = null;
    protected Image leftSprite = null, rightSprite = null;

    protected Combatant leftFighter, rightFighter;

    protected int shakeX = 0, shakeY = 0;
    protected boolean isProcessing = false;
    protected String floatingText = "";
    protected int floatX = 0, floatY = 0, floatOpacity = 0;
    protected Color floatColor = Color.WHITE;
    protected Timer animationTimer = null;

    protected float leftOpacity = 1.0f, rightOpacity = 1.0f;
    protected Timer fadeTimer = null;
    protected boolean isFadingOut = false;
    protected boolean matchOver = false;
    protected String matchEndMessage = "";
    protected Color matchEndColor = Color.WHITE;

    protected JPanel hud;
    protected JLabel roundLabel, scoreLabel, turnIndicator;
    protected JProgressBar hpBarL, manaBarL, hpBarR, manaBarR;
    protected JLabel hpLblL, manaLblL, hpLblR, manaLblR;
    protected JTextArea battleLog;
    protected JButton btnBasic, btnSkill1, btnSkill2, btnUlt, btnForfeit;
    protected JButton btnNext, btnTryAgain;

    // ACTION_FOLDERS shifted: ultimate is now index 3
    protected static final String[][] ACTION_FOLDERS = {
        {"basic",   "basic"}, {"skill1",  "first"}, {"skill2",  "second"},
        {"ultimate","ult"}
    };

    public BaseArena(GameGUI frame, String mapName) {
        this.mainFrame = frame;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 720));
        loadBattlefield(mapName);
    }

    protected abstract void onSkillButtonClicked(int index);
    protected abstract void onNextClicked();
    protected abstract void onForfeitClicked();
    protected abstract void onRetryClicked();

    protected void setupUI(String nameL, String nameR) {
        if (hud != null) this.remove(hud);

        hud = new JPanel(new BorderLayout());
        hud.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(20, 40, 0, 40));
        top.add(createStatPanel(nameR, false), BorderLayout.WEST);

        JPanel centerInfo = new JPanel(new GridLayout(2, 1));
        centerInfo.setOpaque(false);
        roundLabel = makeLabel("ROUND 1", 28, new Color(255, 215, 0));
        scoreLabel = makeLabel("SCORE", 20, Color.WHITE);
        centerInfo.add(roundLabel);
        centerInfo.add(scoreLabel);
        top.add(centerInfo, BorderLayout.CENTER);

        battleLog = new JTextArea();
        battleLog.setEditable(false);
        battleLog.setBackground(new Color(0, 0, 0, 180));
        battleLog.setForeground(new Color(0, 255, 255));
        battleLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        battleLog.setLineWrap(true);
        battleLog.setWrapStyleWord(true);
        
        JScrollPane scroll = new JScrollPane(battleLog);
        scroll.setPreferredSize(new Dimension(280, 120));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 1));
        top.add(scroll, BorderLayout.EAST);
        hud.add(top, BorderLayout.NORTH);

        JPanel bot = new JPanel(new GridBagLayout());
        bot.setOpaque(false);
        bot.setBorder(BorderFactory.createEmptyBorder(0, 40, 25, 40));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 1; gbc.weightx = 1.0;
        turnIndicator = makeLabel("READY", 32, Color.YELLOW);
        bot.add(turnIndicator, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        bot.add(createStatPanel(nameL, true), gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 0, 0, 0);

        JPanel actions = new JPanel(new GridLayout(1, 5, 10, 0));
        actions.setOpaque(false);

        btnBasic  = createActionBtn(0, new Color(60, 60, 60));
        btnSkill1 = createActionBtn(1, new Color(30, 70, 140));
        btnSkill2 = createActionBtn(2, new Color(30, 70, 140));
        btnUlt    = createActionBtn(3, new Color(140, 30, 30));

        btnForfeit = new JButton("<html><center>FORFEIT<br><font size='2'>[END MATCH]</font></center></html>");
        btnForfeit.setBackground(new Color(120, 30, 30));
        btnForfeit.setForeground(Color.WHITE);
        btnForfeit.setFocusPainted(false);
        btnForfeit.setFont(new Font("Arial", Font.BOLD, 12));
        btnForfeit.addActionListener(e -> onForfeitClicked());

        actions.add(btnBasic); actions.add(btnSkill1); actions.add(btnSkill2);
        actions.add(btnUlt); actions.add(btnForfeit);
        bot.add(actions, gbc);

        btnNext = new JButton("CONTINUE ▶");
        btnNext.setFont(new Font("Impact", Font.PLAIN, 20));
        btnNext.setBackground(new Color(0, 100, 0));
        btnNext.setForeground(Color.WHITE);
        btnNext.setFocusPainted(false);
        btnNext.setVisible(false);
        btnNext.addActionListener(e -> onNextClicked());
        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 0, 0);
        bot.add(btnNext, gbc);

        hud.add(bot, BorderLayout.SOUTH);
        add(hud);

        if (btnTryAgain != null) this.remove(btnTryAgain);
        btnTryAgain = new JButton("RETRY MISSION");
        btnTryAgain.setFont(new Font("Impact", Font.PLAIN, 24));
        btnTryAgain.setBackground(new Color(0, 100, 0));
        btnTryAgain.setForeground(Color.WHITE);
        btnTryAgain.setFocusPainted(false);
        btnTryAgain.setVisible(false);
        btnTryAgain.addActionListener(e -> onRetryClicked());
        add(btnTryAgain);
    }

    private JButton createActionBtn(int index, Color bg) {
        JButton b = new JButton("");
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setFont(new Font("Arial", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(120, 70));
        b.addActionListener(e -> onSkillButtonClicked(index));
        return b;
    }

    protected void setFighters(Combatant left, Combatant right) {
        this.leftFighter = left; this.rightFighter = right;
        hpBarL.setMaximum(left.getMaxHp()); manaBarL.setMaximum(left.getMaxMana());
        hpBarR.setMaximum(right.getMaxHp()); manaBarR.setMaximum(right.getMaxMana());
        refreshBars();
    }

    protected void refreshBars() {
        if (leftFighter != null) {
            hpBarL.setValue(leftFighter.getHp()); hpLblL.setText("HP: " + leftFighter.getHp() + "/" + leftFighter.getMaxHp());
            manaBarL.setValue(leftFighter.getMana()); manaLblL.setText("MP: " + leftFighter.getMana() + "/" + leftFighter.getMaxMana());
        }
        if (rightFighter != null) {
            hpBarR.setValue(rightFighter.getHp()); hpLblR.setText("HP: " + rightFighter.getHp() + "/" + rightFighter.getMaxHp());
            manaBarR.setValue(rightFighter.getMana()); manaLblR.setText("MP: " + rightFighter.getMana() + "/" + rightFighter.getMaxMana());
        }
    }

    protected void updateActionButtons(Combatant f) {
        if (f == null) return;
        btnBasic.setText("<html><center>BASIC<br><font size='2'>[MP: 0 | DMG: " + f.getActionDamage(0) + "]</font></center></html>");
        btnSkill1.setText("<html><center>" + f.getSkill1Name() + "<br><font size='2'>[MP: " + f.getActionCost(1) + " | DMG: " + f.getActionDamage(1) + "]</font></center></html>");
        btnSkill2.setText("<html><center>" + f.getSkill2Name() + "<br><font size='2'>[MP: " + f.getActionCost(2) + " | DMG: " + f.getActionDamage(2) + "]</font></center></html>");
        btnUlt.setText("<html><center>" + f.getUltimateName() + "<br><font size='2'>[MP: " + f.getActionCost(3) + " | DMG: " + f.getActionDamage(3) + "]</font></center></html>");
        
        btnBasic.setEnabled(true);
        btnSkill1.setEnabled(f.canUse(1));
        btnSkill2.setEnabled(f.canUse(2));
        btnUlt.setEnabled(f.canUse(3));
    }

    protected void disableAllActions() {
        btnBasic.setEnabled(false); btnSkill1.setEnabled(false);
        btnSkill2.setEnabled(false); btnUlt.setEnabled(false);
    }

    protected void updateTurnIndicator(String text, Color c) {
        turnIndicator.setText(text);
        turnIndicator.setForeground(c);
    }

    protected void logMessage(String msg) {
        battleLog.append(msg + "\n");
        battleLog.setCaretPosition(battleLog.getDocument().getLength());
    }

    protected void playAnimationAndWait(boolean isLeft, String folder, String action, int duration, Runnable callback) {
        if (folder.equals("damaged")) triggerShake(15, 350);
        setSprite(isLeft, folder, action);

        if (animationTimer != null && animationTimer.isRunning()) animationTimer.stop();
        animationTimer = new Timer(duration, e -> {
            if (isLeft ? leftFighter.isAlive() : rightFighter.isAlive()) setSprite(isLeft, "idle", "idle");
            if (callback != null) callback.run();
        });
        animationTimer.setRepeats(false); animationTimer.start();
    }

    private String mapHeroNameToFile(String name) {
        switch (name) {
            case "ironman":        return "ironman";
            case "captainamerica": return "captainamerica";
            case "spiderman":      return "spider-man";
            case "blackwidow":     return "blackwidow";
            case "thefalcon":      return "falcon";
            case "antman":         return "antman";
            case "janclark":       return "clark";
            case "reuben":         return "reuben";
            case "justine":        return "justine";
            case "thanos":         return "thanos";
            case "thor":           return "thor";
            case "hulk":           return "hulk";
            default:               return name;
        }
    }

    protected void setSprite(boolean isLeft, String folder, String action) {
        if (leftFighter == null || rightFighter == null) return;
        
        String rawName = (isLeft ? leftFighter.getName() : rightFighter.getName()).replace("-", "").replace(" ", "").toLowerCase();
        String mappedName = mapHeroNameToFile(rawName);
        String side = isLeft ? "NE" : "SW";

        // Try the exact path first (with side suffix)
        String path = "gifs/" + folder + "/" + mappedName + action + side + ".gif";
        File f = new File(path);
        
        // If not found, try without side suffix
        if (!f.exists()) {
            path = "gifs/" + folder + "/" + mappedName + action + ".gif";
            f = new File(path);
        }
        
        // For basic attacks, try using skill1 animation as fallback if basic folder doesn't have the animation
        if (!f.exists() && folder.equals("basic")) {
            path = "gifs/skill1/" + mappedName + "first" + side + ".gif";
            f = new File(path);
            if (!f.exists()) {
                path = "gifs/skill1/" + mappedName + "first.gif";
                f = new File(path);
            }
        }
        
        // For idle animations, try using the default image or just skip
        if (!f.exists() && folder.equals("idle")) {
            // Try to load a static image from picture folder as fallback
            path = "picture/" + mappedName + ".png";
            f = new File(path);
            if (!f.exists()) {
                path = "picture/" + mappedName + ".jpg";
                f = new File(path);
            }
        }
        
        // For damaged animations, try to load a generic damaged effect or skip
        if (!f.exists() && folder.equals("damaged")) {
            // Try to load the same character's idle animation as fallback
            path = "gifs/idle/" + mappedName + "idle" + side + ".gif";
            f = new File(path);
            if (!f.exists()) {
                path = "gifs/idle/" + mappedName + "idle.gif";
                f = new File(path);
            }
            // If still not found, we'll just not show an animation (keep current sprite)
            if (!f.exists()) {
                System.out.println("Missing damaged animation for: " + mappedName);
                repaint();
                return;
            }
        }

        if (f.exists()) {
            ImageIcon icon = new ImageIcon(f.getPath());
            icon.getImage().flush(); 
            if (isLeft) leftSprite = icon.getImage(); 
            else rightSprite = icon.getImage();
        } else {
            if (isLeft) leftSprite = null; 
            else rightSprite = null;
            System.out.println("Missing GIF: " + f.getPath());
        }
        repaint();
    }

    protected void showCombatText(String text, int x, int y, Color color) {
        this.floatingText = text; this.floatX = x; this.floatY = y;
        this.floatColor = color; this.floatOpacity = 255;
        Timer t = new Timer(25, e -> {
            floatY -= 4; floatOpacity -= 8;
            if (floatOpacity <= 0) { floatOpacity = 0; ((Timer) e.getSource()).stop(); }
            repaint();
        });
        t.start();
    }

    protected void triggerShake(int intensity, int duration) {
        long start = System.currentTimeMillis();
        Random rand = new Random();
        Timer t = new Timer(20, e -> {
            if (System.currentTimeMillis() - start > duration) { shakeX = 0; shakeY = 0; ((Timer) e.getSource()).stop(); }
            else { shakeX = rand.nextInt(intensity * 2) - intensity; shakeY = rand.nextInt(intensity * 2) - intensity; }
            repaint();
        });
        t.start();
    }

    protected void startMatchEndFadeOut(boolean leftLost) {
        final long startTime = System.currentTimeMillis();
        isFadingOut = true;
        if (fadeTimer != null && fadeTimer.isRunning()) fadeTimer.stop();
        fadeTimer = new Timer(16, e -> {
            float progress = Math.min(1.0f, (System.currentTimeMillis() - startTime) / 800f);
            if (leftLost) leftOpacity = Math.max(0, 1.0f - progress);
            else rightOpacity = Math.max(0, 1.0f - progress);
            if (progress >= 1.0f) { fadeTimer.stop(); isFadingOut = false; }
            repaint();
        });
        fadeTimer.start();
    }

    @Override
    public void doLayout() {
        super.doLayout();
        if (hud != null) hud.setBounds(0, 0, getWidth(), getHeight());
        if (btnTryAgain != null) btnTryAgain.setBounds((getWidth() - 250) / 2, (getHeight() - 65) / 2, 250, 65);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (battlefieldImage != null) g2.drawImage(battlefieldImage, 0, 0, getWidth(), getHeight(), this);

        int pSize = (int)(getWidth() * 0.375), eSize = (int)(getWidth() * 0.25);

        if (rightSprite != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, rightOpacity));
            g2.drawImage(rightSprite, (int)(getWidth()*0.65) + shakeX, (int)(getHeight()*0.15) + shakeY, eSize, eSize, this);
        }
        if (leftSprite != null) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, leftOpacity));
            g2.drawImage(leftSprite, (int)(getWidth()*0.05) + shakeX, (int)(getHeight()*0.38) + shakeY, pSize, pSize, this);
        }
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        if (floatOpacity > 0) {
            g2.setFont(new Font("Impact", Font.BOLD, 52));
            g2.setColor(new Color(0, 0, 0, floatOpacity));
            g2.drawString(floatingText, floatX + 3, floatY + 3);
            g2.setColor(new Color(floatColor.getRed(), floatColor.getGreen(), floatColor.getBlue(), floatOpacity));
            g2.drawString(floatingText, floatX, floatY);
        }

        if (matchOver && !isFadingOut) {
            g2.setColor(new Color(0, 0, 0, 200)); g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setFont(new Font("Impact", Font.ITALIC, 110)); g2.setColor(matchEndColor);
            g2.drawString(matchEndMessage, (getWidth() - g2.getFontMetrics().stringWidth(matchEndMessage))/2, getHeight()/2);
        }
    }

    private JPanel createStatPanel(String name, boolean isLeft) {
        JPanel box = new JPanel(); box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setBackground(new Color(0, 0, 0, 200));
        box.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 215, 0), 2), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        box.setPreferredSize(new Dimension(260, 140));

        JLabel n = new JLabel(name.toUpperCase()); n.setFont(new Font("Impact", Font.PLAIN, 18));
        n.setForeground(new Color(255, 215, 0)); n.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar hp = new JProgressBar(); hp.setForeground(new Color(40, 180, 100)); hp.setBackground(new Color(80, 40, 40)); hp.setPreferredSize(new Dimension(220, 18));
        JLabel hpLabel = new JLabel("HP: 100/100"); hpLabel.setFont(new Font("Arial", Font.BOLD, 12)); hpLabel.setForeground(Color.WHITE); hpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar mn = new JProgressBar(); mn.setForeground(new Color(40, 120, 200)); mn.setBackground(new Color(40, 40, 80)); mn.setPreferredSize(new Dimension(220, 18));
        JLabel manaLabel = new JLabel("MP: 0/100"); manaLabel.setFont(new Font("Arial", Font.BOLD, 12)); manaLabel.setForeground(new Color(100, 200, 255)); manaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (isLeft) { hpBarL = hp; manaBarL = mn; hpLblL = hpLabel; manaLblL = manaLabel; } 
        else { hpBarR = hp; manaBarR = mn; hpLblR = hpLabel; manaLblR = manaLabel; }

        box.add(n); box.add(Box.createRigidArea(new Dimension(0, 5))); box.add(hpLabel); box.add(hp);
        box.add(Box.createRigidArea(new Dimension(0, 5))); box.add(manaLabel); box.add(mn);
        return box;
    }

    private JLabel makeLabel(String t, int s, Color c) {
        JLabel l = new JLabel(t); l.setFont(new Font("Impact", Font.PLAIN, s)); l.setForeground(c);
        l.setHorizontalAlignment(SwingConstants.CENTER); return l;
    }

    protected void loadBattlefield(String mapName) {
        String[][] mapData = {
            { "Asgard",          "asgardgamebg"        },
            { "Avengers Tower",  "avengerstowercover"  }, 
            { "Avengers HQ",     "avengerstowerinside" },
            { "City Court",      "citubballcourt"      },
            { "Jollibee Arena",  "jollibeeinside"      },
            { "Nyan Realm",      "nyanmap"             },
            { "Random Stage",    "randompicture"       },
            { "Sokovia",         "sokoviagamemap"      },
            { "Titan",           "titangame"           },
            { "Wakanda",         "wakandacover"        }, 
            { "Wakanda Inside",  "wakandainside"       }
        };

        String fileKey = "asgardgamebg"; 
        
        for (String[] entry : mapData) {
            if (entry[0].equalsIgnoreCase(mapName)) {
                fileKey = entry[1];
                break;
            }
        }

        File f = new File("maps/" + fileKey + ".png");
        if(f.exists()) {
            battlefieldImage = new ImageIcon(f.getPath()).getImage();
        } else {
            File jpg = new File("maps/" + fileKey + ".jpg");
            if(jpg.exists()) battlefieldImage = new ImageIcon(jpg.getPath()).getImage();
            else battlefieldImage = new ImageIcon("maps/asgardgamebg.png").getImage();
        }
    }
}
