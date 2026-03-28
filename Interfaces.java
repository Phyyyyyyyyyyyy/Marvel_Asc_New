/**
 * Shared interfaces for Marvel Ascension — reusable contracts across game components.
 */
public interface Interfaces {

    /** Screens that build their own UI layout (CharacterSelector, Maps, GameModes). */
    interface GameScreen {
        void setupLayout();
    }

    /** Main frame/navigator that controls panel switching and shared state. */
    interface GameNavigator {
        void navigateTo(String panelName);
        String getSelectedHeroName();
        void setSelectedHero(String name);
        void setSelectedHero2(String name);
    }

    /** Combat entities (Enemy) — health, damage, healing. */
    interface Combatant {
        boolean isAlive();
        void takeDamage(int damage);
        void heal(int amount);
        int getHp();
        int getMaxHp();
        int getAttack();
    }
}
