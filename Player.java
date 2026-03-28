public class Player extends Combatant {

    public Player(CharacterSelector.CharacterData data) {
        super(
            data.getName(), 
            data.getHp(), 
            data.getAttack(),
            data.getSkill1(), 
            data.getSkill2(), 
            data.getUltimate(),
            20, 25, 50,                      // Mana costs
            data.getAttack() + 5,            // Skill 1 damage
            data.getAttack() + 10,           // Skill 2 damage
            data.getAttack() * 2             // Ultimate damage
        );
        this.setCooldownManager(new CooldownManager());
    }

    @Override
    public int decideAction() {
        return 0; // Players are controlled by UI buttons, not AI logic
    }
}
