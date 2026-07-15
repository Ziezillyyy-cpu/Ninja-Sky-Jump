package game;

/**
 * STRUKTUR DATA 4 (Tree): Kelas DifficultyNode.java
 * Sebagai simpul (node) pada pohon DifficultyTree yang menyimpan konfigurasi kesulitan game.
 */
public class DifficultyNode {
    private String levelName;         // "EASY", "MEDIUM", "HARD"
    private int minScore;             // Batas minimum skor untuk masuk tingkat kesulitan ini
    private int maxScore;             // Batas maksimum skor (inklusif)
    private int minGapY;              // Jarak vertikal minimum antar platform
    private int maxGapY;              // Jarak vertikal maksimum antar platform
    private double breakableProb;     // Peluang platform "BREAKABLE" muncul (0.0 sampai 1.0)
    
    // Pointer anak kiri dan anak kanan (Struktur Binary Tree)
    public DifficultyNode left;
    public DifficultyNode right;

    public DifficultyNode(String levelName, int minScore, int maxScore, int minGapY, int maxGapY, double breakableProb) {
        this.levelName = levelName;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.minGapY = minGapY;
        this.maxGapY = maxGapY;
        this.breakableProb = breakableProb;
        this.left = null;
        this.right = null;
    }

    // Getters & Setters
    public String getLevelName() { return levelName; }
    public int getMinScore() { return minScore; }
    public int getMaxScore() { return maxScore; }
    public int getMinGapY() { return minGapY; }
    public int getMaxGapY() { return maxGapY; }
    public double getBreakableProb() { return breakableProb; }
}
