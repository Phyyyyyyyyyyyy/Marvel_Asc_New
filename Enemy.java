import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy extends Combatant {

    public enum EnemyTier { EASY, MEDIUM, HARD, BOSS }

    private static final Random rand = new Random();
    private final EnemyTier tier;

    public Enemy(String name, int hp, int attack,
                 String skill1, String skill2, String ultimate,
                 int sk1Cost, int sk2Cost, int ultCost,
                 int sk1Dmg, int sk2Dmg, int ultDmg,
                 EnemyTier tier) {
        super(name, hp, attack, skill1, skill2, ultimate,
              sk1Cost, sk2Cost, ultCost, sk1Dmg, sk2Dmg, ultDmg);
        this.tier = tier;
        setCooldownManager(new CooldownManager());
    }

    @Override
    public int decideAction() {
        // Ultimate is now index 3
        if (canUse(3) && getHp() < getMaxHp() * 0.4) return 3;
        
        if (rand.nextInt(100) < 65) {
            List<Integer> available = new ArrayList<>();
            if (canUse(2)) available.add(2);
            if (canUse(1)) available.add(1);
            
            if (!available.isEmpty())
                return available.get(rand.nextInt(available.size()));
        }
        return 0; 
    }

    private static final List<Enemy> HERO_MIRROR_DATABASE = List.of(
        new Enemy("Iron Man", 90, 12, "Repulsor Blast", "Micro-Missiles", "Unibeam Overload",
            12, 15, 35, 12, 18, 40, EnemyTier.EASY),
        new Enemy("Captain America", 100, 10, "Shield Throw", "Vibranium Bash", "Avengers Assemble",
            12, 15, 35, 10, 16, 38, EnemyTier.EASY),
        new Enemy("Spider-Man", 85, 12, "Web Snare", "Spider-Sense Dodge", "Maximum Spider",
            12, 0, 35, 12, 0, 42, EnemyTier.EASY),
        new Enemy("Black Widow", 88, 14, "Widow's Bite", "Dual Pistols", "Lullaby Takedown",
            12, 15, 35, 14, 18, 40, EnemyTier.EASY),
        
        new Enemy("Iron Man", 110, 18, "Repulsor Blast", "Micro-Missiles", "Unibeam Overload",
            15, 20, 40, 18, 25, 55, EnemyTier.MEDIUM),
        new Enemy("Captain America", 130, 15, "Shield Throw", "Vibranium Bash", "Avengers Assemble",
            15, 20, 40, 15, 22, 50, EnemyTier.MEDIUM),
        new Enemy("Thor", 140, 20, "Hammer Toss", "Lightning Strike", "God Blast",
            15, 20, 45, 20, 28, 58, EnemyTier.MEDIUM),
        new Enemy("Spider-Man", 100, 14, "Web Snare", "Spider-Sense Dodge", "Maximum Spider",
            15, 0, 40, 14, 0, 48, EnemyTier.MEDIUM),
        
        new Enemy("Thor", 160, 24, "Hammer Toss", "Lightning Strike", "God Blast",
            18, 22, 48, 24, 30, 65, EnemyTier.HARD),
        new Enemy("Hulk", 190, 28, "Gamma Punch", "Thunderclap", "Worldbreaker Slam",
            18, 22, 50, 28, 32, 75, EnemyTier.HARD),
        new Enemy("The Falcon", 115, 18, "Wing Shield", "Redwing Strike", "Flight Form Alpha",
            15, 20, 45, 18, 24, 52, EnemyTier.HARD),
        new Enemy("Ant-Man", 110, 15, "Size Shift", "Ant Swarm", "Giant-Man Stomp",
            15, 20, 45, 15, 20, 50, EnemyTier.HARD),
        
        new Enemy("Thanos", 250, 35, "Titan Punch", "Power Stone", "The Snap",
            20, 28, 60, 35, 45, 100, EnemyTier.BOSS),
        new Enemy("Ultron Prime", 220, 32, "Laser Pulse", "Drone Swarm", "Extinction Protocol",
            20, 25, 55, 32, 38, 85, EnemyTier.BOSS),
        new Enemy("Dormammu", 200, 30, "Dark Dimension", "Flame Wave", "Dimension Collapse",
            18, 24, 50, 30, 35, 80, EnemyTier.BOSS),
        new Enemy("Loki", 180, 28, "Scepter Blast", "Illusion", "God of Mischief",
            18, 22, 48, 28, 30, 70, EnemyTier.BOSS)
    );

    public static Enemy getRandomEnemy() {
        return cloneMirror(HERO_MIRROR_DATABASE.get(rand.nextInt(HERO_MIRROR_DATABASE.size())));
    }

    public static Enemy getRandomEnemyByLevel(int level) {
        EnemyTier tier;
        switch (level) {
            case 1: tier = EnemyTier.EASY; break;
            case 2: tier = EnemyTier.MEDIUM; break;
            case 3: tier = EnemyTier.HARD; break;
            case 4: tier = EnemyTier.BOSS; break;
            default: return getRandomEnemy();
        }
        List<Enemy> filtered = new ArrayList<>();
        for (Enemy e : HERO_MIRROR_DATABASE) if (e.tier == tier) filtered.add(e);
        if (filtered.isEmpty()) return getRandomEnemy();
        return cloneMirror(filtered.get(rand.nextInt(filtered.size())));
    }

    public static Enemy getEnemyByName(String name) {
        for (Enemy e : HERO_MIRROR_DATABASE)
            if (e.getName().equalsIgnoreCase(name)) return cloneMirror(e);
        return getRandomEnemy();
    }

    private static Enemy cloneMirror(Enemy e) {
        return new Enemy(e.getName(), e.getMaxHp(), e.getAttack(),
            e.getSkill1Name(), e.getSkill2Name(), e.getUltimateName(),
            e.getSk1Cost(), e.getSk2Cost(), e.getUltCost(),
            e.getActionDamage(1), e.getActionDamage(2), e.getActionDamage(3),
            e.tier);
    }
}
