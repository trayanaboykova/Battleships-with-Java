package battleship;

import java.util.*;

public class Main {

    /* ------------ configuration ------------ */

    static final int SIZE = 10;
    static final char FOG  = '~';
    static final char SHIP = 'O';
    static final char HIT  = 'X';
    static final char MISS = 'M';

    static final ShipSpec[] SHIPS_TO_PLACE = {
            new ShipSpec("Aircraft Carrier", 5),
            new ShipSpec("Battleship", 4),
            new ShipSpec("Submarine", 3),
            new ShipSpec("Cruiser", 3),
            new ShipSpec("Destroyer", 2)
    };

    /* --------------- entry point --------------- */

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Create players
        Player p1 = new Player("Player 1");
        Player p2 = new Player("Player 2");

        // Player 1 placement
        System.out.println(p1.name + ", place your ships on the game field\n");
        printField(p1.real);
        placeAllShips(sc, p1);
        promptPassTurn(sc);

        // Player 2 placement
        System.out.println(p2.name + ", place your ships to the game field\n");
        printField(p2.real);
        placeAllShips(sc, p2);
        promptPassTurn(sc);

        // Game loop
        Player current = p1;
        Player opponent = p2;

        while (true) {
            // Show opponent fog on top and my real at bottom
            printField(opponent.fog);
            System.out.println("---------------------");
            printField(current.real);
            System.out.println();
            System.out.println(current.name + ", it's your turn:\n");

            Point shot = readShot(sc);
            // Resolve shot on opponent's boards
            char before = opponent.real[shot.row][shot.col];

            if (before == SHIP) {
                opponent.real[shot.row][shot.col] = HIT;
                opponent.fog[shot.row][shot.col]  = HIT;

                Ship hitShip = findShipByCell(opponent.fleet, shot);
                boolean sunkNow = hitShip != null && isSunk(opponent.real, hitShip);

                if (allShipsSunk(opponent.real, opponent.fleet)) {
                    System.out.println("You sank the last ship. You won. Congratulations!");
                    break;
                } else if (sunkNow) {
                    System.out.println("You sank a ship!");
                    promptPassTurn(sc);
                } else {
                    System.out.println("You hit a ship!");
                    promptPassTurn(sc);
                }
            } else {
                // MISS, HIT, MISS again: still show miss or hit messages per rules
                if (before == HIT) {
                    // Re-hit same cell = still "hit"
                    opponent.fog[shot.row][shot.col] = HIT;
                    System.out.println("You hit a ship!");
                } else {
                    // before == FOG or before == MISS
                    opponent.real[shot.row][shot.col] = MISS;
                    opponent.fog[shot.row][shot.col]  = MISS;
                    System.out.println("You missed!");
                }
                promptPassTurn(sc);
            }

            // swap players
            Player tmp = current;
            current = opponent;
            opponent = tmp;
        }
    }

    /* ---------------- placement pipeline ---------------- */

    static void placeAllShips(Scanner sc, Player p) {
        for (ShipSpec spec : SHIPS_TO_PLACE) {
            System.out.printf("%nEnter the coordinates of the %s (%d cells):%n%n",
                    spec.name, spec.length);

            while (true) {
                String line = sc.nextLine().trim();
                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    System.out.printf("Error! Wrong ship location! Try again:%n%n");
                    continue;
                }

                Point a = parse(parts[0]);
                Point b = parse(parts[1]);

                if (a == null || b == null || !isStraight(a, b)) {
                    System.out.printf("Error! Wrong ship location! Try again:%n%n");
                    continue;
                }

                int len = segmentLength(a, b);
                if (len != spec.length) {
                    System.out.printf("Error! Wrong length of the %s! Try again:%n%n", spec.name);
                    continue;
                }

                if (!canPlace(p.real, a, b)) {
                    System.out.printf("Error! You placed it too close to another one. Try again:%n%n");
                    continue;
                }

                Ship placed = new Ship(spec.name, spec.length);
                placed.cells = enumerateCells(a, b);
                place(p.real, placed);
                p.fleet.add(placed);

                System.out.println();
                printField(p.real);
                break;
            }
        }
    }

    /* ---------------- UI helpers ---------------- */

    static void promptPassTurn(Scanner sc) {
        System.out.println("\nPress Enter and pass the move to another player");
        sc.nextLine(); // wait for Enter
        System.out.println();
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

    static Point readShot(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            Point p = parse(s);
            if (p != null) return p;
            System.out.println("\nError! You entered wrong coordinates! Try again:\n");
        }
    }

    /* ---------------- parsing & validation ---------------- */

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

        for (int r = r1; r <= r2; r++) {
            for (int c = c1; c <= c2; c++) {
                // only test cells that lie on the ship's straight line
                if (a.row != b.row && c != a.col) continue; // vertical ship
                if (a.col != b.col && r != a.row) continue; // horizontal ship

                // Check 8-neighborhood for any existing ship
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

    static class Player {
        final String name;
        final char[][] real; // true board
        final char[][] fog;  // opponent's view of this board
        final List<Ship> fleet = new ArrayList<>();
        Player(String name) {
            this.name = name;
            this.real = createField();
            this.fog  = createField();
        }
    }

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

    static class ShipSpec {
        final String name;
        final int length;
        ShipSpec(String name, int length) { this.name = name; this.length = length; }
    }

    static char[][] createField() {
        char[][] f = new char[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) Arrays.fill(f[r], FOG);
        return f;
    }
}
