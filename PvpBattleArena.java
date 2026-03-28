import javax.swing.*;
import java.awt.*;

public class PvpBattleArena extends BaseArena {

    private Player p1, p2;
    private int p1Wins = 0, p2Wins = 0, currentRound = 1;
    private boolean p1Turn = true;
    
    private CharacterSelector.CharacterData savedD1, savedD2;
    private String savedMap; 

    public PvpBattleArena(GameGUI frame) {
        super(frame, "asgardgamebg"); 
    }

    public void startBattle(CharacterSelector.CharacterData d1, CharacterSelector.CharacterData d2, String map) {
        this.savedD1 = d1; 
        this.savedD2 = d2; 
        this.savedMap = map;
        
        this.p1 = new Player(d1); 
        this.p2 = new Player(d2);
        
        loadBattlefield(map); 

        setupUI(p1.getName(), p2.getName());
        setFighters(p1, p2);
        
        p1Wins = 0; p2Wins = 0; currentRound = 1;
        leftOpacity = 1.0f; rightOpacity = 1.0f;
        btnNext.setText("CONTINUE ▶");
        
        startNewRound();
    }

    private void startNewRound() {
        p1.resetForNewRound(); p2.resetForNewRound();
        
        setSprite(true, "idle", "idle"); 
        setSprite(false, "idle", "idle");
        
        roundLabel.setText("ROUND " + currentRound + " / 3");
        updateScoreStars();
        
        btnNext.setVisible(false); btnTryAgain.setVisible(false);
        p1Turn = true; isProcessing = false; matchOver = false;
        
        updateActionButtons(p1);
        updateTurnIndicator("PLAYER 1'S TURN", Color.YELLOW);
        refreshBars();
        
        logMessage("\n══════════════════════════════════");
        logMessage("  ROUND " + currentRound + " - FIGHT!");
        logMessage("══════════════════════════════════\n");
    }

    private void updateScoreStars() {
        String p1Stars = "★".repeat(p1Wins) + "☆".repeat(2 - Math.min(p1Wins, 2));
        String p2Stars = "★".repeat(p2Wins) + "☆".repeat(2 - Math.min(p2Wins, 2));
        scoreLabel.setText("P1: " + p1Stars + "  |  P2: " + p2Stars);
    }

    @Override
    protected void onSkillButtonClicked(int index) {
        if (isProcessing || matchOver) return;

        Combatant attacker = p1Turn ? p1 : p2;
        Combatant defender = p1Turn ? p2 : p1;
        if (!attacker.canUse(index)) return;

        isProcessing = true; disableAllActions();
        updateTurnIndicator((p1Turn ? "PLAYER 1" : "PLAYER 2") + " ATTACKING...", Color.WHITE);

        // INCREASED TIMER TO 1500ms TO FIX CUT-OFF ANIMATIONS
        playAnimationAndWait(p1Turn, ACTION_FOLDERS[index][0], ACTION_FOLDERS[index][1], 1500, () -> {
            int dmg = attacker.useAction(index); defender.takeDamage(dmg);

            String pLabel = p1Turn ? "P1" : "P2";
            logMessage("[" + pLabel + " — " + attacker.getName() + "]");
            logMessage("  ⚔ " + attacker.getActionName(index) + " → " + dmg + " damage!");
            logMessage("  " + defender.getName() + " HP: " + defender.getHp() + "/" + defender.getMaxHp());

            if (p1Turn) showCombatText("-" + dmg, (int)(getWidth()*0.75), (int)(getHeight()*0.25), Color.RED);
            else showCombatText("-" + dmg, (int)(getWidth()*0.20), (int)(getHeight()*0.45), Color.RED);

            playAnimationAndWait(!p1Turn, "damaged", "damaged", 600, () -> {
                attacker.tickCooldowns(); refreshBars();
                if (!defender.isAlive()) endRound(p1Turn);
                else {
                    p1Turn = !p1Turn; isProcessing = false;
                    updateActionButtons(p1Turn ? p1 : p2);
                    updateTurnIndicator((p1Turn ? "PLAYER 1'S" : "PLAYER 2'S") + " TURN", Color.YELLOW);
                }
            });
        });
    }

    private void endRound(boolean p1Won) {
        if (p1Won) p1Wins++; else p2Wins++;
        updateScoreStars();
        
        if (p1Wins >= 2 || p2Wins >= 2) {
            matchOver = true;
            matchEndMessage = p1Wins >= 2 ? "PLAYER 1 WINS" : "PLAYER 2 WINS";
            matchEndColor = Color.GREEN;
            startMatchEndFadeOut(p1Wins < 2);
            
            LeaderboardManager.recordPvpResult("Player 1", p1.getName(), p1Wins, p2Wins);
            LeaderboardManager.recordPvpResult("Player 2", p2.getName(), p2Wins, p1Wins);
            
            Timer showButtons = new Timer(900, e -> {
                btnTryAgain.setVisible(true);
                btnNext.setText("RETURN TO MENU"); btnNext.setVisible(true);
            });
            showButtons.setRepeats(false); showButtons.start();
        } else {
            btnNext.setVisible(true);
        }
    }

    @Override protected void onNextClicked() {
        if (btnNext.getText().contains("MENU")) mainFrame.navigateTo("main");
        else { currentRound++; startNewRound(); }
    }

@Override protected void onForfeitClicked() {
        if (matchOver) { mainFrame.navigateTo("main"); return; }
        if (JOptionPane.showConfirmDialog(this, "Forfeit match?") == JOptionPane.YES_OPTION) {
            matchOver = true; matchEndMessage = "FORFEITED"; matchEndColor = Color.RED;
            startMatchEndFadeOut(true); 
            
            // --- NEW: Show the Retry Button ---
            btnTryAgain.setVisible(true); 
            
            btnNext.setText("RETURN TO MENU"); 
            btnNext.setVisible(true);
        }
    }

    @Override protected void onRetryClicked() {
        startBattle(savedD1, savedD2, savedMap); 
        repaint();
    }
}
