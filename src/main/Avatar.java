package main;

import main.objects.Collidable;
import visual.RenderedSprite;
import static main.GamePanel.centerScreen;
import static main.StageManager.size;

public class Avatar extends Collidable {

    // CHARACTER CONDITIONALS
    protected boolean canJump;
    protected boolean canMove;
    protected boolean moving;
    protected boolean jumping;
    protected boolean flying;
    protected boolean crouching;
    protected boolean facingUp;
    protected boolean falling;
    protected boolean dead;
    // VELOCITY
    protected double[] velocity = new double[2];
    protected double speed = 2.0;
    protected double jump_Strength = -7.0;

    // POSITION
    public double x;
    public double y;

    double lastX = this.x;
    double lastY = this.y;

    // SIZE
    int height;
    int width;

    // ANIMATION
    int direction_Facing = 1;
    RenderedSprite currentSprite;
    RenderedSprite lastSprite;

    // GAME REFERENCE
    Game GAME;

    // CONSTRUCTORS
    Avatar(int x, int y, Game game) {
        this.x = x;
        this.y = y;
        this.GAME = game;
    }

    // METHODS
    protected int getPOITile(int y, int x, int[][] source) {
        try {
            return source[y][x];
        } catch (ArrayIndexOutOfBoundsException e) {
            return -1;
        }
    }
    protected void spawn(int x, int y) {
        int[] converted = toPlayerCoords(x,y);
        this.x = converted[0];
        this.y = converted[1];
    }

    @Override
    protected void conformToStage(double edge_0, double edge_1, double[] tileCoords, int mode) {
        RenderedSprite tileDown = null;
        RenderedSprite tileUp = null;

        if (tileCoords != null && mode == 0) {
            tileCoords[0] = Math.round(tileCoords[0]);
            tileCoords[1] = Math.round(tileCoords[1]);
            tileDown = GamePanel.Tiles[(int) tileCoords[1]][(int) tileCoords[0]];
            tileUp = GamePanel.Tiles[(int) tileCoords[1]+1][(int) tileCoords[0]];
        }

        if (mode == 0) {
            if (tileDown == null && tileUp == null) {
                this.x = edge_0;
            } else {
                this.x = edge_1;
            }
        } else if (mode == 1) {
            this.x = edge_1;
        } else if (mode == 2) {
            this.y = edge_1;
        }
    }
    public int[] toPlayerCoords(int x0, int y0) {
        int extra_Y = (this.height / 2) - (size / 2);
        return new int[]{
                (int) ((centerScreen[0] - (8 * size)) + (size / 2.0) + (x0 * size)),
                (GAME.GAME_SCREEN.getHeight() - ((size / 6) + (y0 * size)) - extra_Y)
        };
    }
    @Override
    public double[] toTileCoords(double x, double y) {
        int extra = (this.height / 2) - (size / 2);
        return new double[]{((x - centerScreen[0]) / size) + 15.0 / 2, Math.abs(((y - GAME.GAME_SCREEN.getHeight() + extra) / size) + (1.0 / 6))};
    }
    @Override
    public int[] getCriticalPoints() {
        double[] coords = toTileCoords(this.x, this.y);
        return new int[]{(int) Math.round(coords[0]), (int) Math.floor(coords[1]) // torso level
        };
    }

    @Override
    public void Touched(Collidable c) {}
}
