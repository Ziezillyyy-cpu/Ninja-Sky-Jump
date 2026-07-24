package game;

import java.io.Serializable;

public class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
    private static final long serialVersionUID = 1L;
    private String name;
    private int score;

    public ScoreEntry(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    // --- TAMBAHAN METHOD SETTER ---
    public void setScore(int score) {
        this.score = score;
    }

    public void setName(String name) {
        this.name = name;
    }
    // -------------------------------

    // Urutkan skor dari yang tertinggi ke terendah (Descending)
    @Override
    public int compareTo(ScoreEntry o) {
        return Integer.compare(o.score, this.score);
    }
}