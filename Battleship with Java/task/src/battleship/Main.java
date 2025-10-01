package battleship;

import java.util.*;

public class Main {

    /* ------------ configuration ------------ */

    static final int SIZE = 10;
    static final char FOG  = '~';
    static final char SHIP = 'O';
    static final char HIT  = 'X';
    static final char MISS = 'M';

    static final Ship[] SHIPS_TO_PLACE = {
            new Ship("Aircraft Carrier", 5),
            new Ship("Battleship", 4),
            new Ship("Submarine", 3),
            new Ship("Cruiser", 3),
            new Ship("Destroyer", 2)
    };

    /* --------------- entry point --------------- */

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Boards
        char[][] real = createField(); // ships + X/M
        char[][] fog  = createField(); // ~ + X/M (never shows O)

        // Keep the placed ships with their coordinates for sink detection
        List<Ship> fleet = new ArrayList<>();

        // 1) Show empty field and place ships
        printField(real);
        for (Ship plan : SHIPS_TO_PLACE) {
            System.out.printf("%nEnter the coordinates of the %s (%d cells):%n%n",
                    plan.name, plan.length);
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
                if (len != plan.length) {
                    System.out.printf("Error! Wrong length of the %s! Try again:%n%n", plan.name);
                    continue;
                }

                // no overlap / no touching
                if (!canPlace(real, a, b)) {
                    System.out.printf("Error! You placed it too close to another one. Try again:%n%n");
                    continue;
                }

                // Build the actual ship with all its cells, place on board, remember it
                Ship placed = new Ship(plan.name, plan.length);
                placed.cells = enumerateCells(a, b);
                place(real, placed);
                fleet.add(placed);

                System.out.println();
                printField(real);
                break;
            }
        }

        // 2) Game loop with fog of war
        System.out.println("\nThe game starts!\n");
        printField(fog);
        System.out.println("\nTake a shot!\n");

        while (true) {
            String s = sc.nextLine().trim();
            Point shot = parse(s);
            if (shot == null) {
                // (Stage’s example wording)
                System.out.println("\nError! You entered wrong coordinates! Try again:\n");
                continue;
            }

            char before = real[shot.row][shot.col]; // what was there
            boolean wasShipCell = (before == SHIP);
            boolean wasFogCell  = (before == FOG);

            // Mark results on both boards; repeated shots behave the same message-wise
            if (wasShipCell) {
                real[shot.row][shot.col] = HIT;
                fog [shot.row][shot.col] = HIT;
            } else if (wasFogCell) {
                real[shot.row][shot.col] = MISS;
                fog [shot.row][shot.col] = MISS;
            } else {
                // Already X or M; mirror to fog anyway to keep boards consistent
                fog[shot.row][shot.col] = real[shot.row][shot.col];
            }

            // Show current (fog) view
            System.out.println();
            printField(fog);
            System.out.println();

            // Decide message
            if (before == SHIP) {
                // New hit: check if this specific ship is now sunk
                Ship hitShip = findShipByCell(fleet, shot);
                if (hitShip != null && isSunk(real, hitShip)) {
                    // Remove from remaining? Not necessary; we’ll check victory via all-ship scan
                    if (allShipsSunk(real, fleet)) {
                        System.out.println("You sank the last ship. You won. Congratulations!");
                        break;
                    } else {
                        System.out.println("You sank a ship! Specify a new target:");
                    }
                } else {
                    // Either not fully sunk yet, or it was a repeated hit cell (handled below)
                    System.out.println("You hit a ship! Try again:");
                }
            } else if (before == HIT) {
                // Re-shot an already hit cell → still “hit” message
                System.out.println("You hit a ship! Try again:");
            } else if (before == MISS) {
                // Re-shot an already missed cell → still “missed” message
                System.out.println("You missed. Try again:");
            } else {
                // Fresh miss
                System.out.println("You missed. Try again:");
            }
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

    static List<Point> enumerateCells(Point a, Point b) {
        List<Point> list = new ArrayList<>();
        if (a.row == b.row) { // horizontal
            int c1 = Math.min(a.col, b.col);
            int c2 = Math.max(a.col, b.col);
            for (int c = c1; c <= c2; c++) list.add(new Point(a.row, c));
        } else { // vertical
            int r1 = Math.min(a.row, b.row);
            int r2 = Math.max(a.row, b.row);
            for (int r = r1; r <= r2; r++) list.add(new Point(r, a.col));
        }
        return list;
    }

    static void place(char[][] f, Ship ship) {
        for (Point p : ship.cells) f[p.row][p.col] = SHIP;
    }

    /* ---------------- sinking logic ---------------- */

    static Ship findShipByCell(List<Ship> fleet, Point p) {
        for (Ship s : fleet) {
            for (Point cell : s.cells) {
                if (cell.row == p.row && cell.col == p.col) return s;
            }
        }
        return null;
    }

    static boolean isSunk(char[][] real, Ship ship) {
        for (Point p : ship.cells) {
            if (real[p.row][p.col] != HIT) return false;
        }
        return true;
    }

    static boolean allShipsSunk(char[][] real, List<Ship> fleet) {
        for (Ship s : fleet) {
            if (!isSunk(real, s)) return false;
        }
        return true;
    }

    /* ---------------- data types ---------------- */

    static class Point {
        final int row, col;
        Point(int r, int c) { this.row = r; this.col = c; }
    }

    static class Ship {
        final String name;
        final int length;
        List<Point> cells = new ArrayList<>();
        Ship(String name, int length) { this.name = name; this.length = length; }
    }
}
