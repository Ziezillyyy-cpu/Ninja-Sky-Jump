package game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Kelas HighScoreManager.java
 * Bertanggung jawab untuk menyimpan dan memuat skor tertinggi (High Score)
 * beserta nama pemain pemegangnya ke/dari file lokal "highscore.dat",
 * sehingga High Score tetap tersimpan walau aplikasi ditutup.
 */
public class HighScoreManager {

    private static final String FILE_PATH = "highscore.dat";

    /** Struktur sederhana untuk menampung data skor tertinggi */
    public static class HighScoreData {
        public final int score;
        public final String name;

        public HighScoreData(int score, String name) {
            this.score = score;
            this.name = name;
        }
    }

    public static HighScoreData load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new HighScoreData(0, "-");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String scoreLine = reader.readLine();
            String nameLine = reader.readLine();

            int score = 0;
            if (scoreLine != null && !scoreLine.trim().isEmpty()) {
                score = Integer.parseInt(scoreLine.trim());
            }
            String name = (nameLine != null && !nameLine.trim().isEmpty()) ? nameLine.trim() : "-";

            return new HighScoreData(score, name);
        } catch (IOException | NumberFormatException e) {
            System.out.println("[HighScoreManager]: Gagal membaca high score -> " + e.getMessage());
            return new HighScoreData(0, "-");
        }
    }

    public static void save(HighScoreData data) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.valueOf(data.score));
            writer.newLine();
            writer.write(data.name);
        } catch (IOException e) {
            System.out.println("[HighScoreManager]: Gagal menyimpan high score -> " + e.getMessage());
        }
    }
}
