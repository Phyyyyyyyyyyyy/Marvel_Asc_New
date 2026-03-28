import javax.swing.*;
import java.awt.*;

public class GauntletBattle extends BaseArena {

    private Player player;
    private Enemy enemy;
    private int playerScore = 0, aiScore = 0, currentRound = 1;
    private boolean playerTurn = true;

    public GauntletBattle(GameGUI frame, String heroName, String mapName) {
        super(frame, mapName);
        CharacterSelector selector = frame.getSelectorPanel();
        CharacterSelector.CharacterData heroData = selector.getHeroData(heroName);
        if (heroData == null) heroData = selector.getHeroData("Iron Man");

        this.player = new Player(heroData);
        this.enemy = Enemy.getRandomEnemy();

        setupUI(player.getName(), enemy.getName());
        setFighters(player, enemy);
        
        setSprite(true, "idle", "idle"); setSprite(false, "idle", "idle");
        startNewRound();
    }

    private void startNewRound() {
        player.resetForNewRound(); enemy.resetForNewRound();
        setSprite(true, "idle", "idle"); setSprite(false, "idle", "idle");
        
        roundLabel.setText("ROUND " + currentRound + " / 3");
        scoreLabel.setText(playerScore + " - " + aiScore);
        
        btnNext.setVisible(false); btnTryAgain.setVisible(false);
        playerTurn = true; isProcessing = false; matchOver = false;
        
        updateActionButtons(player);
        updateTurnIndicator("YOUR TURN", Color.GREEN);
        refreshBars();
        
        logMessage("\n══════════════════════════════════");
        logMessage("  ROUND " + currentRound + " - FIGHT!");
        logMessage("══════════════════════════════════\n");
    }

    @Override
    protected void onSkillButtonClicked(int index) {
        if (!playerTurn || isProcessing || matchOver) return;
        if (!player.canUse(index)) { logMessage("⚠ Not enough mana!"); return; }

        isProcessing = true; disableAllActions();
        updateTurnIndicator("", Color.WHITE);

        // INCREASED TIMER TO 1500ms TO FIX CUT-OFF ANIMATIONS
        playAnimationAndWait(true, ACTION_FOLDERS[index][0], ACTION_FOLDERS[index][1], 1500, () -> {
            int dmg = player.useAction(index); enemy.takeDamage(dmg);

            logMessage("[PLAYER — " + player.getName() + "]");
            logMessage("  ⚔ " + player.getActionName(index) + " → " + dmg + " damage!");
            logMessage("  " + enemy.getName() + " HP: " + enemy.getHp() + "/" + enemy.getMaxHp());

            showCombatText("-" + dmg, (int)(getWidth()*0.75), (int)(getHeight()*0.25), Color.RED);

            playAnimationAndWait(false, "damaged", "damaged", 600, () -> {
                player.tickCooldowns(); refreshBars();
                if (!enemy.isAlive()) endRound(true);
                else {
                    playerTurn = false; updateTurnIndicator("ENEMY TURN", Color.ORANGE);
                    Timer aiDelay = new Timer(400, e -> doAiTurn());
                    aiDelay.setRepeats(false); aiDelay.start();
                }
            });
        });
    }

    private void doAiTurn() {
        if (matchOver) return;
        int action = enemy.decideAction();

        // INCREASED TIMER TO 1500ms TO FIX CUT-OFF ANIMATIONS
        playAnimationAndWait(false, ACTION_FOLDERS[action][0], ACTION_FOLDERS[action][1], 1500, () -> {
            int dmg = enemy.useAction(action); player.takeDamage(dmg);

            logMessage("[ENEMY — " + enemy.getName() + "]");
            logMessage("  ⚔ " + enemy.getActionName(action) + " → " + dmg + " damage!");
            logMessage("  " + player.getName() + " HP: " + player.getHp() + "/" + player.getMaxHp());

            showCombatText("-" + dmg, (int)(getWidth()*0.20), (int)(getHeight()*0.45), Color.RED);

            playAnimationAndWait(true, "damaged", "damaged", 600, () -> {
                enemy.tickCooldowns(); refreshBars();
                if (!player.isAlive()) endRound(false);
                else {
                    playerTurn = true; isProcessing = false;
                    updateActionButtons(player); updateTurnIndicator("YOUR TURN", Color.GREEN);
                }
            });
        });
    }

    private void endRound(boolean playerWon) {
        if (playerWon) playerScore++; else aiScore++;
        scoreLabel.setText(playerScore + " - " + aiScore);
        
        if (playerScore >= 2 || aiScore >= 2) {
            matchOver = true;
            boolean wonMatch = playerScore >= 2;
            matchEndMessage = wonMatch ? "VICTORY" : "DEFEAT";
            matchEndColor = wonMatch ? Color.GREEN : Color.RED;
            startMatchEndFadeOut(!wonMatch);
            
            LeaderboardManager.recordGauntletResult(player.getName(), playerScore, aiScore, wonMatch);
            
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
            LeaderboardManager.recordGauntletResult(player.getName(), playerScore, aiScore, false);
        }
    }

    @Override protected void onRetryClicked() {
        playerScore = 0; aiScore = 0; currentRound = 1;
        leftOpacity = 1.0f; rightOpacity = 1.0f;
        btnNext.setText("CONTINUE ▶"); startNewRound(); repaint();
    }
}
