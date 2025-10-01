package battleship;

import java.util.*;

public class Main {

    private static final int SIZE = 10;

    public static void main(String[] args) {
        // 1) Print empty field
        printEmptyField();

        // 2) Read two coordinates
        System.out.println("Enter the coordinates of the ship:");
        Scanner sc = new Scanner(System.in);
        if (!sc.hasNextLine()) {
            System.out.println("Error!");
            return;
        }
        String line = sc.nextLine().trim();
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Error!");
            return;
        }

        // 3) Parse both coordinates
        Optional<Point> aOpt = parseCoordinate(parts[0]);
        Optional<Point> bOpt = parseCoordinate(parts[1]);
        if (aOpt.isEmpty() || bOpt.isEmpty()) {
            System.out.println("Error!");
            return;
        }
        Point a = aOpt.get();
        Point b = bOpt.get();

        // 4) Validate alignment (same row OR same column)
        boolean sameRow = a.row == b.row;
        boolean sameCol = a.col == b.col;
        if (!(sameRow ^ sameCol)) { // not exactly one of them
            System.out.println("Error!");
            return;
        }

        // 5) Build ordered list of parts from A->B (inclusive)
        List<String> coords = new ArrayList<>();
        if (sameRow) {
            int start = Math.min(a.col, b.col);
            int end = Math.max(a.col, b.col);
            for (int c = start; c <= end; c++) {
                coords.add(toCoord(a.row, c));
            }
        } else { // sameCol
            int start = Math.min(a.row, b.row);
            int end = Math.max(a.row, b.row);
            for (int r = start; r <= end; r++) {
                coords.add(toCoord(r, a.col));
            }
        }

        // 6) Output length and parts
        System.out.println("Length: " + coords.size());
        System.out.println("Parts: " + String.join(" ", coords));
    }

    /* ---------- Helpers ---------- */

    private static void printEmptyField() {
        // header
        System.out.print("  ");
        for (int c = 1; c <= SIZE; c++) {
            System.out.print(c);
            if (c < SIZE) System.out.print(" ");
        }
        System.out.println();

        // rows
        for (int r = 0; r < SIZE; r++) {
            char rowLetter = (char) ('A' + r);
            System.out.print(rowLetter + " ");
            for (int c = 0; c < SIZE; c++) {
                System.out.print("~");
                if (c < SIZE - 1) System.out.print(" ");
            }
            System.out.println();
        }
    }

    private static Optional<Point> parseCoordinate(String s) {
        // Expect format Letter + Number(1..10), e.g., A1, J10
        if (s == null || s.isEmpty()) return Optional.empty();

        // First char must be A..J (case-insensitive)
        char ch = Character.toUpperCase(s.charAt(0));
        if (ch < 'A' || ch > 'J') return Optional.empty();
        int row = ch - 'A';

        // The rest must be 1..10
        String numPart = s.substring(1);
        if (numPart.isEmpty() || !numPart.matches("\\d{1,2}")) return Optional.empty();

        int num;
        try {
            num = Integer.parseInt(numPart);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        if (num < 1 || num > 10) return Optional.empty();

        // Convert to 0-based column
        int col = num - 1;
        return Optional.of(new Point(row, col));
    }

    private static String toCoord(int row, int col) {
        return "" + (char) ('A' + row) + (col + 1);
    }

    private static class Point {
        final int row; // 0..9
        final int col; // 0..9
        Point(int r, int c) { this.row = r; this.col = c; }
    }
}
