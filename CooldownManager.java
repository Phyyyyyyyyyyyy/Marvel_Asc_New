import java.util.HashMap;
import java.util.Map;

/**
 * CooldownManager — tracks per-skill cooldowns for a combatant.
 *
 * Each skill slot (1–4) has a configurable max cooldown (in turns).
 * After a skill is used, it enters cooldown and cannot be used again
 * until the required number of turns have passed.
 *
 * Slot 0 (Basic Attack) never has a cooldown.
 *
 * Usage:
 *   CooldownManager cd = new CooldownManager();
 *   cd.setMaxCooldown(1, 2);  // Skill 1 has a 2-turn cooldown
 *   cd.useSkill(1);           // Mark skill 1 as used
 *   cd.isReady(1);            // false — still on cooldown
 *   cd.tickAll();             // Advance all cooldown timers by 1 turn
 *   cd.isReady(1);            // false (1 turn left)
 *   cd.tickAll();
 *   cd.isReady(1);            // true — ready again
 */
public class CooldownManager {

    // Default cooldown lengths (in turns) for each skill slot
    private static final int DEFAULT_SK1_CD = 2;
    private static final int DEFAULT_SK2_CD = 2;
    private static final int DEFAULT_SK3_CD = 3;
    private static final int DEFAULT_ULT_CD = 4;

    // Maps skill index → max cooldown turns
    private final Map<Integer, Integer> maxCooldown = new HashMap<>();

    // Maps skill index → remaining cooldown turns (0 = ready)
    private final Map<Integer, Integer> remaining   = new HashMap<>();

    // ── Constructors ───────────────────────────────────────────────────────

    /** Creates a manager with the default cooldown values. */
    public CooldownManager() {
        maxCooldown.put(1, DEFAULT_SK1_CD);
        maxCooldown.put(2, DEFAULT_SK2_CD);
        maxCooldown.put(3, DEFAULT_SK3_CD);
        maxCooldown.put(4, DEFAULT_ULT_CD);

        // All skills start ready
        for (int slot : maxCooldown.keySet())
            remaining.put(slot, 0);
    }

    /**
     * Creates a manager with custom cooldown values per slot.
     * @param sk1 cooldown for skill 1
     * @param sk2 cooldown for skill 2
     * @param sk3 cooldown for skill 3
     * @param ult cooldown for ultimate
     */
    public CooldownManager(int sk1, int sk2, int sk3, int ult) {
        maxCooldown.put(1, sk1);
        maxCooldown.put(2, sk2);
        maxCooldown.put(3, sk3);
        maxCooldown.put(4, ult);

        for (int slot : maxCooldown.keySet())
            remaining.put(slot, 0);
    }

    // ── Configuration ──────────────────────────────────────────────────────

    /**
     * Override the max cooldown for a specific slot.
     * @param slot  skill index (1–4)
     * @param turns number of turns the skill is locked after use
     */
    public void setMaxCooldown(int slot, int turns) {
        if (slot < 1 || slot > 4) return;
        maxCooldown.put(slot, Math.max(0, turns));
    }

    // ── Core API ───────────────────────────────────────────────────────────

    /**
     * Returns true if the skill is off cooldown and ready to use.
     * Slot 0 (Basic Attack) is always ready.
     */
    public boolean isReady(int slot) {
        if (slot == 0) return true;
        return remaining.getOrDefault(slot, 0) <= 0;
    }

    /**
     * Marks a skill as used and starts its cooldown.
     * Has no effect for slot 0 (Basic Attack) or unknown slots.
     */
    public void useSkill(int slot) {
        if (slot == 0 || !maxCooldown.containsKey(slot)) return;
        remaining.put(slot, maxCooldown.get(slot));
    }

    /**
     * Advances ALL cooldown timers by 1 turn.
     * Call this once at the END of the owning combatant's turn.
     */
    public void tickAll() {
        for (int slot : remaining.keySet()) {
            int r = remaining.get(slot);
            if (r > 0) remaining.put(slot, r - 1);
        }
    }

    /**
     * Returns the number of turns remaining on a cooldown.
     * 0 means the skill is ready. Slot 0 always returns 0.
     */
    public int getRemaining(int slot) {
        if (slot == 0) return 0;
        return remaining.getOrDefault(slot, 0);
    }

    /**
     * Resets all cooldowns to 0 (all skills become ready).
     * Call this at the start of each new round.
     */
    public void resetAll() {
        for (int slot : remaining.keySet())
            remaining.put(slot, 0);
    }

    /**
     * Returns a human-readable cooldown label for a skill button.
     * Examples: "READY", "CD: 2t"
     */
    public String getLabel(int slot) {
        if (slot == 0) return "";
        int r = getRemaining(slot);
        return r <= 0 ? "READY" : "CD: " + r + "t";
    }

    /**
     * Returns a compact summary of all cooldowns (useful for debugging).
     * Format: "SK1:0 SK2:2 SK3:1 ULT:4"
     */
    @Override
    public String toString() {
        return "SK1:" + getRemaining(1)
             + " SK2:" + getRemaining(2)
             + " SK3:" + getRemaining(3)
             + " ULT:" + getRemaining(4);
    }
}
