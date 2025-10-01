package battleship;

import java.util.*;

public class Main {

    /* ------------ configuration ------------ */

    static final int SIZE = 10;
    static final char FOG  = '~';
    static final char SHIP = 'O';
    static final char HIT  = 'X';
    static final char MISS = 'M';

    static final Ship[] SHIPS = {
            new Ship("Aircraft Carrier", 5),
            new Ship("Battleship", 4),
            new Ship("Submarine", 3),
            new Ship("Cruiser", 3),
            new Ship("Destroyer", 2)
    };

    /* --------------- entry point --------------- */

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // 1) Create and show empty field
        char[][] field = createField();
        printField(field);

        // 2) Place all ships with validation
        for (Ship ship : SHIPS) {
            System.out.printf("%nEnter the coordinates of the %s (%d cells):%n%n",
                    ship.name, ship.length);
            while (true) {
                String line = sc.nextLine().trim();
                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    System.out.printf("Error! Wrong ship location! Try again:%n%n");
                    continue;
                }

                Point a = parse(parts[0]);
                Point b = parse(parts[1]);

                // basic location checks
                if (a == null || b == null || !isStraight(a, b)) {
                    System.out.printf("Error! Wrong ship location! Try again:%n%n");
                    continue;
                }

                // length must match the ship
                int len = segmentLength(a, b);
                if (len != ship.length) {
                    System.out.printf("Error! Wrong length of the %s! Try again:%n%n", ship.name);
                    continue;
                }

                // no overlap / no touching
                if (!canPlace(field, a, b)) {
                    System.out.printf("Error! You placed it too close to another one. Try again:%n%n");
                    continue;
                }

                // place and print
                place(field, a, b);
                System.out.println();
                printField(field);
                break;
            }
        }

        // 3) Stage 3: one shot, then stop
        System.out.println("\nThe game starts!\n");
        printField(field);

        System.out.println("\nTake a shot!\n");
        Point shot;
        while (true) {
            String s = sc.nextLine().trim();
            shot = parse(s);
            if (shot == null) {
                System.out.println("\nError! You entered the wrong coordinates! Try again:\n");
                continue;
            }
            break;
        }

        // 4) Apply shot result
        char cell = field[shot.row][shot.col];
        if (cell == SHIP) {
            field[shot.row][shot.col] = HIT;
        } else if (cell == FOG) {
            field[shot.row][shot.col] = MISS;
        } // If already X or M (not expected here), leave as is.

        System.out.println();
        printField(field);
        System.out.println();
        if (field[shot.row][shot.col] == HIT) {
            System.out.println("You hit a ship!");
        } else {
            System.out.println("You missed!");
        }
    }

    /* ---------------- field helpers ---------------- */

    static char[][] createField() {
        char[][] f = new char[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) Arrays.fill(f[r], FOG);
        return f;
    }

    static void printField(char[][] f) {
        // header
        System.out.print("  ");
        for (int c = 1; c <= SIZE; c++) {
            System.out.print(c);
            if (c < SIZE) System.out.print(" ");
        }
        System.out.println();
        // rows
        for (int r = 0; r < SIZE; r++) {
            System.out.print((char) ('A' + r) + " ");
            for (int c = 0; c < SIZE; c++) {
                System.out.print(f[r][c]);
                if (c < SIZE - 1) System.out.print(" ");
            }
            System.out.println();
        }
    }

    /* ---------------- placement & validation ---------------- */

    static Point parse(String s) {
        if (s == null || s.length() < 2) return null;
        char rowCh = Character.toUpperCase(s.charAt(0));
        if (rowCh < 'A' || rowCh > 'J') return null;
        String num = s.substring(1);
        if (!num.matches("\\d{1,2}")) return null;
        int col = Integer.parseInt(num);
        if (col < 1 || col > 10) return null;
        return new Point(rowCh - 'A', col - 1);
    }

    static boolean isStraight(Point a, Point b) {
        return a.row == b.row || a.col == b.col;
    }

    static int segmentLength(Point a, Point b) {
        return (a.row == b.row)
                ? Math.abs(a.col - b.col) + 1
                : Math.abs(a.row - b.row) + 1;
    }

    static boolean canPlace(char[][] f, Point a, Point b) {
        int r1 = Math.min(a.row, b.row);
        int r2 = Math.max(a.row, b.row);
        int c1 = Math.min(a.col, b.col);
        int c2 = Math.max(a.col, b.col);

        // Check overlap and adjacency (8-neighborhood) around each intended cell
        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                // only test cells that lie on the ship's straight line
                if (a.row != b.row && c != a.col) continue; // vertical ship
                if (a.col != b.col && r != a.row) continue; // horizontal ship

                // Check 8 neighbors for any existing ship
                for (int dr = -1; dr <= 1; dr++) {
                    for (int dc = -1; dc <= 1; dc++) {
                        int nr = r + dr, nc = c + dc;
                        if (inBounds(nr, nc) && f[nr][nc] == SHIP) return false;
                    }
                }
            }
        }
        return true;
    }

    static boolean inBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    static void place(char[][] f, Point a, Point b) {
        if (a.row == b.row) { // horizontal
            int c1 = Math.min(a.col, b.col);
            int c2 = Math.max(a.col, b.col);
            for (int c = c1; c <= c2; c++) f[a.row][c] = SHIP;
        } else { // vertical
            int r1 = Math.min(a.row, b.row);
            int r2 = Math.max(a.row, b.row);
            for (int r = r1; r <= r2; r++) f[r][a.col] = SHIP;
        }
    }

    /* ---------------- data types ---------------- */

    static class Point {
        final int row, col;
        Point(int r, int c) { this.row = r; this.col = c; }
    }

    static class Ship {
        final String name;
        final int length;
        Ship(String name, int length) { this.name = name; this.length = length; }
    }
}