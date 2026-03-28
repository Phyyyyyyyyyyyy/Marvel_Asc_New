// ============================================================
//  ABSTRACTION — Abstract base class that defines the contract
//  all fighters must follow, hiding internal implementation.
// ============================================================
public abstract class Combatant {

    // ENCAPSULATION — all fields private, accessed via getters/setters
    private String name;
    private int    hp;
    private int    maxHp;
    private int    attack;
    private int    mana;
    private final int maxMana = 100;

    private String skill1Name, skill2Name, ultimateName;
    private int    sk1Cost, sk2Cost, ultCost;
    private int    sk1Damage, sk2Damage, ultDamage;

    protected CooldownManager cooldown;

    public Combatant(String name, int hp, int attack,
                     String skill1Name, String skill2Name, String ultimateName,
                     int sk1Cost, int sk2Cost, int ultCost,
                     int sk1Damage, int sk2Damage, int ultDamage) {
        this.name         = name;
        this.hp           = hp;
        this.maxHp        = hp;
        this.attack       = attack;
        this.mana         = 30; // start with some mana

        this.skill1Name   = skill1Name;
        this.skill2Name   = skill2Name;
        this.ultimateName = ultimateName;

        this.sk1Cost   = sk1Cost;
        this.sk2Cost   = sk2Cost;
        this.ultCost   = ultCost;

        this.sk1Damage = sk1Damage;
        this.sk2Damage = sk2Damage;
        this.ultDamage = ultDamage;
    }

    public abstract int decideAction();

    public void resetForNewRound() {
        this.hp   = this.maxHp;
        this.mana = 30;
        if (cooldown != null) cooldown.resetAll();
    }

    public void setCooldownManager(CooldownManager c) { cooldown = c; }
    public void tickCooldowns() { if (cooldown != null) cooldown.tickAll(); }

    public boolean isAlive() { return hp > 0; }

    public int takeDamage(int damage) {
        int actual = Math.max(1, damage);
        hp = Math.max(0, hp - actual);
        return actual;
    }

    public void restoreMana(int amount) {
        mana = Math.min(maxMana, mana + amount);
    }

    public int useAction(int actionIndex) {
        switch (actionIndex) {
            case 0:
                restoreMana(10);
                return attack;
            case 1:
                if (mana >= sk1Cost && (cooldown == null || cooldown.isReady(1)))
                    { mana -= sk1Cost; restoreMana(5); if (cooldown != null) cooldown.useSkill(1); return sk1Damage; }
                break;
            case 2:
                if (mana >= sk2Cost && (cooldown == null || cooldown.isReady(2)))
                    { mana -= sk2Cost; restoreMana(5); if (cooldown != null) cooldown.useSkill(2); return sk2Damage; }
                break;
            case 3: // Ultimate is now slot 3
                if (mana >= ultCost && (cooldown == null || cooldown.isReady(3)))
                    { mana -= ultCost; if (cooldown != null) cooldown.useSkill(3); return ultDamage; }
                break;
        }
        restoreMana(10);
        return attack;
    }

    public boolean canUse(int actionIndex) {
        if (cooldown != null && !cooldown.isReady(actionIndex)) return false;
        switch (actionIndex) {
            case 1: return mana >= sk1Cost;
            case 2: return mana >= sk2Cost;
            case 3: return mana >= ultCost;
            default: return true;
        }
    }

    public String getActionName(int actionIndex) {
        switch (actionIndex) {
            case 0:  return "Basic Attack";
            case 1:  return skill1Name;
            case 2:  return skill2Name;
            case 3:  return ultimateName;
            default: return "Unknown";
        }
    }

    public int getActionDamage(int actionIndex) {
        switch (actionIndex) {
            case 0:  return attack;
            case 1:  return sk1Damage;
            case 2:  return sk2Damage;
            case 3:  return ultDamage;
            default: return 0;
        }
    }

    public int getActionCost(int actionIndex) {
        switch (actionIndex) {
            case 1:  return sk1Cost;
            case 2:  return sk2Cost;
            case 3:  return ultCost;
            default: return 0;
        }
    }

    // Getters — ENCAPSULATION
    public String getName()         { return name; }
    public int    getHp()           { return hp; }
    public int    getMaxHp()        { return maxHp; }
    public int    getAttack()       { return attack; }
    public int    getMana()         { return mana; }
    public int    getMaxMana()      { return maxMana; }
    public String getSkill1Name()   { return skill1Name; }
    public String getSkill2Name()   { return skill2Name; }
    public String getUltimateName() { return ultimateName; }
    public int    getSk1Cost()      { return sk1Cost; }
    public int    getSk2Cost()      { return sk2Cost; }
    public int    getUltCost()      { return ultCost; }
}
