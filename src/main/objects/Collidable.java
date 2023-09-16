package main.objects;

import main.Avatar;
import main.Game;
import main.GamePanel;
import main.NPC;
import main.technical.Edge;
import visual.RenderedSprite;

import static main.GamePanel.centerScreen;
import static main.StageManager.size;
import static main.technical.Edge.*;

public abstract class Collidable {
    public int x;
    public int y;
    public int[] velocity = new int[]{0,0};
    public String name = null;
    Game game = null;
    public RenderedSprite sprite;
    public Collidable() {this(0,0,null,null);}
    public Collidable(int x, int y, RenderedSprite sprite, Game game) {
        this.x = x;
        this.y = y;
        if (sprite != null && sprite.name != null) {
            this.name = sprite.name;
        }
        this.game = game;
        this.sprite = sprite;
    }

    public void update() {
        if (this.game == null || this.sprite == null || this.name == null) return;
        this.x += this.velocity[0];
        this.y += this.velocity[1];
        this.sprite.x = this.x;
        this.sprite.y = this.y;

        isColliding(game.PLAYER);
        for (NPC n : GamePanel.Enemies) {
            isColliding(n);
        }
    }
    protected RenderedSprite getTile(int y, int x, RenderedSprite[][] source) {
        try {
            return source[y][x];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
    public double[] Bounds(RenderedSprite sprite) {
        if (sprite == null) return null;
        if (sprite.sprite == null) return null;

        return new double[] {
            sprite.x - sprite.sprite.getWidth()/2.0, // LEFT
            sprite.x + sprite.sprite.getWidth()/2.0, // RIGHT
            sprite.y - sprite.sprite.getHeight()/2.0, // UP
            sprite.y + sprite.sprite.getHeight()/2.0 // DOWN
        };
    }
    public double getEdge(RenderedSprite sprite, Edge e) {
        if (sprite == null || sprite.sprite == null) return 0;
        double n = sprite.x;
        double s = sprite.sprite.getWidth();

        if (e == UP || e == DOWN) {
            n = sprite.y;
            s = sprite.sprite.getHeight();
        }
        if (e == UP || e == LEFT) {
            s *= -1;
        }
        return (n+(s/2.0));
    }
    void isColliding(Collidable other) {
        if (this.game == null) return;
        if (this.sprite == null || this.name == null || other.sprite == null || other.name == null) return;

        double[] bounds0 = this.Bounds(this.sprite);
        double[] bounds1 = other.Bounds(other.sprite);

        if (bounds0 == null || bounds1 == null) return;

        if (bounds0[0] < bounds1[1] && bounds0[1] > bounds1[0] && bounds0[2] > bounds1[3] && bounds0[3] < bounds1[2]) {
            this.Touched(other);
        }
    }
    protected void conformToStage(double edge_0, double edge_1, double[] tileCoords, int mode) {
        RenderedSprite tile = null;

        if (tileCoords != null && mode == 0) {
            tile = GamePanel.Tiles[(int) tileCoords[1]][(int) tileCoords[0]];
        }

        if (tile == null) {
            this.x = (int) edge_0;
        } else {
            if (this.name.equals("Arrow")) this.Touched(null);
            else this.x = (int) edge_1;
        }
    }
    public double[] toTileCoords(double x, double y) {
        int extra = (this.sprite.height / 2) - (size / 2);
        return new double[]{((x - centerScreen[0]) / size) + 15.0 / 2, Math.abs(((y - this.game.GAME_SCREEN.getHeight() + extra) / size) + (1.0 / 6))};
    }

    public int[] getCriticalPoints() {
        double[] coords = toTileCoords(this.x, this.y);
        return new int[]{(int) Math.round(coords[0]), (int) Math.floor(coords[1]) // torso level
        };
    }
    public abstract void Touched(Collidable c);
}
