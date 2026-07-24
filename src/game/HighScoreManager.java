package game;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {

    private static final String FILE_PATH = "highscore.dat";

    @SuppressWarnings("unchecked")
    public static List<ScoreEntry> loadLeaderboard() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ScoreEntry>) ois.readObject();
        } catch (Exception e) {
            System.out.println("[HighScoreManager]: Gagal membaca leaderboard -> " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static void saveScore(String name, int score) {
        if (name == null || name.trim().isEmpty()) {
            name = "Ninja";
        }

        List<ScoreEntry> leaderboard = loadLeaderboard();
        boolean exists = false;

        // 1. Cek apakah nama pemain sudah ada di leaderboard
        for (ScoreEntry entry : leaderboard) {
            if (entry.getName().equalsIgnoreCase(name)) {
                exists = true;
                // UPDATE: Hanya update jika skor baru LEBIH TINGGI dari skor lama
                if (score > entry.getScore()) {
                    entry.setScore(score);
                }
                break; // Hentikan loop karena player sudah ditemukan
            }
        }

        // 2. Jika nama pemain BELUM ADA, baru tambahkan ke list
        if (!exists) {
            leaderboard.add(new ScoreEntry(name, score));
        }

        // Urutkan seluruh daftar dari skor tertinggi ke terendah
        Collections.sort(leaderboard);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(leaderboard);
        } catch (IOException e) {
            System.out.println("[HighScoreManager]: Gagal menyimpan leaderboard -> " + e.getMessage());
        }
    }

    public static ScoreEntry getTopScore() {
        List<ScoreEntry> list = loadLeaderboard();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return new ScoreEntry("-", 0);
    }
}