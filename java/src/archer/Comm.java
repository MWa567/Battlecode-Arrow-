package archer;

import battlecode.common.*;

public class Comm {
    public static boolean isSymmetryConfirmed = false;
    public static boolean[] isSymEliminated = new boolean[3]; // 0 = rotational, 1 = vertical, 2 = horizontal

    // Constants for symmetry types
    public static final int SYM_ROTATIONAL = 0;
    public static final int SYM_VERTICAL = 1;
    public static final int SYM_HORIZONTAL = 2;

    // Eliminate a specific symmetry if it has been confirmed invalid
    public static void eliminateSym(int sym) {
        if (!isSymEliminated[sym]) {
            isSymEliminated[sym] = true;
            System.out.println("Symmetry " + sym + " has been eliminated.");
        }
    }

    // Reset symmetry state (if needed, you can call this to start fresh)
    public static void resetSymmetryState() {
        isSymmetryConfirmed = false;
        for (int i = 0; i < isSymEliminated.length; i++) {
            isSymEliminated[i] = false;
        }
    }
}
