import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Enemy {
    int x, y, hp, id;
    Enemy(int x, int y, int hp, int id) {
        this.x = x;
        this.y = y;
        this.hp = hp;
        this.id = id;
    }
    boolean isAlive() { return hp > 0; }
}

class Tower {
    int x, y, range, damage;
    Tower(int x, int y, int range, int damage) {
        this.x = x; this.y = y;
        this.range = range; this.damage = damage;
    }
    int dist2To(Enemy e) {
        int dx = x - e.x, dy = y - e.y;
        return dx*dx + dy*dy;
    }
}

public class TowerDefense {
    private final int width = 20;
    private final int height = 6;
    private int lives = 10;
    private int gold = 20;
    private int wave = 1;
    private final int maxWaves = 8;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Tower> towers = new ArrayList<>();
    private int nextEnemyId = 1;
    private final int baseX = width - 1;
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        new TowerDefense().start();
    }

    void start() {
        System.out.println("=== Console Tower Defense ===");
        System.out.println("Stop enemies from reaching the base (right side).");
        System.out.println("Commands:");
        System.out.println("  p x y  -> place a tower at (x,y) [cost 12 gold]");
        System.out.println("  s      -> skip placing");
        System.out.println("  q      -> quit");

        System.out.println("Press ENTER to start...");
        scanner.nextLine();

        while (lives > 0 && wave <= maxWaves) {
            System.out.println("\n--- Wave " + wave + " ---");
            spawnWaveEnemies(wave);
            runWave();
            if (lives <= 0) break;
            wave++;
            gold += 8;
            System.out.println("Wave cleared! +8 gold. Current gold: " + gold);
        }

        if (lives <= 0) {
            System.out.println("\nGame Over! You ran out of lives.");
        } else {
            System.out.println("\nVictory! You survived all waves!");
        }
    }

    void spawnWaveEnemies(int waveNum) {
        enemies.clear();
        int count = 4 + waveNum;
        int hpBase = 3 + waveNum/2;
        for (int i = 0; i < count; i++) {
            int row = ThreadLocalRandom.current().nextInt(0, height);
            enemies.add(new Enemy(0, row, hpBase + (i % 3), nextEnemyId++));
        }
    }

    void runWave() {
        int turn = 0;
        while (!enemies.isEmpty() && lives > 0) {
            turn++;
            System.out.printf("\nTurn %d | Lives: %d | Gold: %d | Towers: %d | Enemies: %d\n",
                    turn, lives, gold, towers.size(), countAliveEnemies());
            drawGrid();
            playerAction();
            towersFire();
            removeDeadEnemiesAndReward();
            moveEnemies();
            int escaped = removeAndCountEscaped();
            if (escaped > 0) {
                lives -= escaped;
                System.out.println(escaped + " enemies reached the base! Lives now " + lives);
            }
        }
    }

    int countAliveEnemies() {
        int c = 0;
        for (Enemy e : enemies) if (e.isAlive()) c++;
        return c;
    }

    void drawGrid() {
        char[][] grid = new char[height][width];
        for (char[] row : grid) Arrays.fill(row, '.');
        for (int y = 0; y < height; y++) grid[y][baseX] = 'B';

        for (Tower t : towers) if (inBounds(t.x, t.y)) grid[t.y][t.x] = 'T';

        int[][] enemyCount = new int[height][width];
        for (Enemy e : enemies) if (e.isAlive() && inBounds(e.x, e.y)) enemyCount[e.y][e.x]++;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (enemyCount[y][x] > 0) {
                    grid[y][x] = enemyCount[y][x] < 10 ? (char)('0' + enemyCount[y][x]) : 'E';
                }
            }
        }

        System.out.print("   ");
        for (int x = 0; x < width; x++) System.out.print(x % 10);
        System.out.println();

        for (int y = 0; y < height; y++) {
            System.out.printf("%2d ", y);
            for (int x = 0; x < width; x++) System.out.print(grid[y][x]);
            System.out.println();
        }
    }

    void playerAction() {
        System.out.print("Action (p x y / s / q): ");
        String cmd = scanner.next();
        if (cmd.equals("p")) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            if (!inBounds(x, y)) {
                System.out.println("Invalid coords.");
            } else if (x == baseX) {
                System.out.println("Cannot place on base.");
            } else if (gold < 12) {
                System.out.println("Not enough gold.");
            } else if (isTowerHere(x, y)) {
                System.out.println("Tower already there.");
            } else {
                towers.add(new Tower(x, y, 3, 3));
                gold -= 12;
                System.out.println("Placed tower at ("+x+","+y+"). Gold left: " + gold);
            }
        } else if (cmd.equals("s")) {
            System.out.println("Skipped placing.");
        } else if (cmd.equals("q")) {
            System.out.println("Quitting.");
            System.exit(0);
        } else {
            System.out.println("Unknown command.");
        }
    }

    boolean inBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    boolean isTowerHere(int x, int y) {
        for (Tower t : towers) if (t.x == x && t.y == y) return true;
        return false;
    }

    void towersFire() {
        for (Tower t : towers) {
            Enemy target = null;
            int bestDist2 = Integer.MAX_VALUE;
            for (Enemy e : enemies) {
                if (!e.isAlive()) continue;
                int d2 = t.dist2To(e);
                if (d2 <= t.range * t.range) {
                    if (d2 < bestDist2 || (d2 == bestDist2 && e.hp < (target != null ? target.hp : Integer.MAX_VALUE))) {
                        bestDist2 = d2;
                        target = e;
                    }
                }
            }
            if (target != null) {
                target.hp -= t.damage;
                System.out.printf("Tower (%d,%d) hits Enemy#%d for %d dmg (hp %d)\n",
                        t.x, t.y, target.id, t.damage, Math.max(0, target.hp));
            }
        }
    }

    void removeDeadEnemiesAndReward() {
        Iterator<Enemy> it = enemies.iterator();
        int reward = 0;
        while (it.hasNext()) {
            Enemy e = it.next();
            if (e.hp <= 0) {
                reward += 3;
                it.remove();
            }
        }
        if (reward > 0) {
            gold += reward;
            System.out.println("Killed enemies. +" + reward + " gold (total " + gold + ")");
        }
    }

    void moveEnemies() {
        for (Enemy e : enemies) if (e.x < baseX) e.x++;
    }

    int removeAndCountEscaped() {
        int escaped = 0;
        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy e = it.next();
            if (e.x >= baseX) {
                escaped++;
                it.remove();
            }
        }
        return escaped;
    }
}

