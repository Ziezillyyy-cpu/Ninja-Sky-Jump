package game;

/**
 * STRUKTUR DATA 4 (Tree & Tree Traversal Pre-Order): Kelas DifficultyTree.java
 * Menyusun simpul-simpul DifficultyNode dalam bentuk Binary Tree
 * dan mengimplementasikan Pre-Order Traversal untuk pencarian tingkat kesulitan secara dinamis.
 */
public class DifficultyTree {
    private DifficultyNode root;

    public DifficultyTree() {
        // Menginisialisasi pohon biner tingkat kesulitan dengan rentang kelipatan 50:
        //
        //               [ MEDIUM ] (Skor 50 - 99)
        //              /          \
        //   [ EASY ] (Skor 0 - 49)  [ HARD ] (Skor 100+)

        root = new DifficultyNode("MEDIUM", 50, 99, 85, 110, 0.35);
        root.left = new DifficultyNode("EASY", 0, 49, 60, 85, 0.0);
        root.right = new DifficultyNode("HARD", 100, Integer.MAX_VALUE, 105, 135, 0.60);
    }

    /**
     * Mencari tingkat kesulitan saat ini berdasarkan skor menggunakan Pre-Order Traversal.
     * Alur Pre-Order: Node -> Anak Kiri -> Anak Kanan.
     * Pencarian mengembalikan DifficultyNode yang cocok, serta mencetak alur traversal untuk edukasi.
     */
    public DifficultyNode searchDifficulty(int score) {
        StringBuilder traversalLog = new StringBuilder("Pencarian Pre-Order: ");
        DifficultyNode result = preOrderSearch(root, score, traversalLog);
        System.out.println(traversalLog.toString() + " -> Terpilih: " + (result != null ? result.getLevelName() : "Tidak Ditemukan"));
        return result;
    }

    /**
     * Fungsi rekursif pembantu untuk melakukan pre-order traversal.
     */
    private DifficultyNode preOrderSearch(DifficultyNode node, int score, StringBuilder log) {
        if (node == null) {
            return null;
        }

        // 1. Kunjungi Node saat ini (Proses Data / Cetak Ke Log)
        log.append("[").append(node.getLevelName()).append("] ");
        if (score >= node.getMinScore() && score <= node.getMaxScore()) {
            return node; // Ditemukan!
        }

        // 2. Telusuri Anak Kiri
        DifficultyNode leftResult = preOrderSearch(node.left, score, log);
        if (leftResult != null) {
            return leftResult;
        }

        // 3. Telusuri Anak Kanan
        return preOrderSearch(node.right, score, log);
    }

    public DifficultyNode getRoot() {
        return root;
    }
}
