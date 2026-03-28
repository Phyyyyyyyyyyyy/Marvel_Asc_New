import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LeaderboardManager — S.H.I.E.L.D. Combat Database
 * Features: Automatic alignment, CSV-style persistence, and Dev-Reset.
 */
public class LeaderboardManager {

    private static final String FILE_NAME = "leaderboard.txt";
    private static final DateTimeFormatter TIMESTAMP_FMT =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"));

    // UI Formatting Constants for perfect grid alignment
    private static final int COL_WIDTH_NAME = 25;
    private static final int COL_WIDTH_WINS = 6;
    private static final int COL_WIDTH_LOSS = 8;

    /**
     * Records Gauntlet match results.
     */
    public static void recordGauntletResult(String heroName, int playerScore, int aiScore, boolean playerWon) {
        String timestamp = TIMESTAMP_FMT.format(Instant.now());
        String line = String.join(";",
                timestamp,
                "GAUNTLET",
                escape(heroName),
                playerWon ? "WIN" : "LOSS",
                String.valueOf(playerScore),
                String.valueOf(aiScore)
        );
        appendLine(line);
    }

    /**
     * Records PvP match results.
     */
    public static void recordPvpResult(String playerName, String heroName, int roundsWon, int roundsLost) {
        String timestamp = TIMESTAMP_FMT.format(Instant.now());
        String label = (playerName == null || playerName.trim().isEmpty())
                ? heroName
                : playerName.trim() + " (" + heroName + ")";

        final String result = (roundsWon > roundsLost) ? "WIN" : (roundsWon < roundsLost ? "LOSS" : "DRAW");

        String line = String.join(";",
                timestamp,
                "PVP",
                escape(label),
                result,
                String.valueOf(roundsWon),
                String.valueOf(roundsLost)
        );
        appendLine(line);
    }

    /**
     * Wipes the leaderboard file. (The Developer "Backdoor" Method)
     */
    public static void resetLeaderboard() {
        try {
            Files.deleteIfExists(Paths.get(FILE_NAME));
        } catch (IOException e) {
            System.err.println("Failed to purge records: " + e.getMessage());
        }
    }

    /**
     * Generates a perfectly aligned string for display in a Monospaced JTextArea.
     */
    public static String buildLeaderboardText() {
        Path path = Paths.get(FILE_NAME);
        if (!Files.exists(path)) {
            return "ACCESS DENIED: No combat records found.\n\n"
                 + "Complete a mission to initialize database.";
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "DATABASE ERROR: " + e.getMessage();
        }

        if (lines.isEmpty()) return "DATABASE EMPTY: No records found.";

        // Aggregate Data
        Map<Key, Stats> map = new HashMap<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(";");
            if (parts.length < 6) continue;

            Key key = new Key(parts[1], unescape(parts[2]));
            Stats stats = map.computeIfAbsent(key, k -> new Stats());
            if ("WIN".equalsIgnoreCase(parts[3])) stats.wins++;
            else if ("LOSS".equalsIgnoreCase(parts[3])) stats.losses++;
        }

        // Group by Mode
        Map<String, List<Map.Entry<Key, Stats>>> byMode = new HashMap<>();
        for (Map.Entry<Key, Stats> e : map.entrySet()) {
            byMode.computeIfAbsent(e.getKey().mode, k -> new ArrayList<>()).add(e);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("S.H.I.E.L.D. COMBAT RECORDS - LEVEL 7 ACCESS\n");
        sb.append("==================================================\n\n");

        List<String> modes = new ArrayList<>(byMode.keySet());
        modes.sort((a, b) -> a.equals("GAUNTLET") ? -1 : (b.equals("GAUNTLET") ? 1 : a.compareToIgnoreCase(b)));

        for (int mi = 0; mi < modes.size(); mi++) {
            String mode = modes.get(mi);
            List<Map.Entry<Key, Stats>> list = byMode.get(mode);
            list.sort(Comparator.comparing((Map.Entry<Key, Stats> e) -> e.getValue().wins).reversed()
                                .thenComparing(e -> e.getKey().hero));

            if (mi > 0) sb.append("\n");
            sb.append(mode).append(" MODE DETECTED\n");
            sb.append("--------------------------------------------------\n");
            
            // Fixed Column Logic
            sb.append(String.format("%-" + COL_WIDTH_NAME + "s %" + COL_WIDTH_WINS + "s %" + COL_WIDTH_LOSS + "s%n", 
                                    "HERO / PLAYER", "WINS", "LOSSES"));
            sb.append(String.format("%-" + COL_WIDTH_NAME + "s %" + COL_WIDTH_WINS + "s %" + COL_WIDTH_LOSS + "s%n", 
                                    "-------------------------", "------", "-------"));

            for (Map.Entry<Key, Stats> e : list) {
                String dName = e.getKey().hero;
                if (dName.length() > COL_WIDTH_NAME) {
                    dName = dName.substring(0, COL_WIDTH_NAME - 3) + "...";
                }
                sb.append(String.format("%-" + COL_WIDTH_NAME + "s %" + COL_WIDTH_WINS + "d %" + COL_WIDTH_LOSS + "d%n",
                        dName, e.getValue().wins, e.getValue().losses));
            }
        }

        sb.append("\n[END OF DATA - RECORDED BY ARTIFICIAL INTELLIGENCE]\n");
        return sb.toString();
    }

    private static void appendLine(String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escape(String s) { return s.replace(";", "\\;"); }
    private static String unescape(String s) { return s.replace("\\;", ";"); }

    private static class Key {
        final String mode, hero;
        Key(String mode, String hero) { this.mode = mode; this.hero = hero; }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Key)) return false;
            Key other = (Key) o;
            return mode.equals(other.mode) && hero.equals(other.hero);
        }
        @Override
        public int hashCode() { return 31 * mode.hashCode() + hero.hashCode(); }
    }

    private static class Stats { int wins, losses; }
}
