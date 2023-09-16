package main.objects;

import main.Game;
import main.NPC;
import visual.RenderedSprite;
import main.GamePanel;
import java.io.IOException;
import java.util.Iterator;

import static main.GamePanel.centerScreen;
import static main.GamePanel.stage_Offset;
import static main.StageManager.size;

public class Arrow extends Collidable {
    public Arrow(int x, int y, String type, boolean mirrored, Game game) throws IOException {
        super(x, y, new RenderedSprite(type, mirrored, x, y, 1.0, 1), game);
        this.name = "Arrow";
    }
    @Override
    public void update() {
        super.update();

        if (game.sm.currentStage.stageMode == 0 || game.sm.currentStage.stageMode == 3) {
            double left_Edge = (centerScreen[0] - (8 * size)) + (size / 2.0);
            double right_Edge = (centerScreen[0] - (8 * size)) + (size / 2.0) + (15 * size);

            if (this.x > right_Edge) {
                double[] tileCoords = toTileCoords(((centerScreen[0] - (8 * size)) + (size / 2.0)), this.y);
                conformToStage(left_Edge, right_Edge, tileCoords,0);
            } else if (this.x < left_Edge) {
                double[] tileCoords = toTileCoords(((centerScreen[0] - (8 * size)) + (size / 2.0) + (15 * size)), this.y);
                conformToStage(right_Edge, left_Edge, tileCoords,0);
            }
        }

        int[] critPoints = getCriticalPoints();
        RenderedSprite center = getTile(critPoints[1], critPoints[0], GamePanel.Tiles);
        RenderedSprite left = getTile(critPoints[1], critPoints[0] - 1, GamePanel.Tiles);
        RenderedSprite right = getTile(critPoints[1], critPoints[0] + 1, GamePanel.Tiles);
        RenderedSprite up = getTile(critPoints[1]+1, critPoints[0], GamePanel.Tiles);
        RenderedSprite down = getTile(critPoints[1]-1, critPoints[0], GamePanel.Tiles);

        if (center != null) {
            if (this.x < (center.x+size) && this.x > (center.x-size) && this.y < (center.y+size) && this.y > (center.y)) {
                Touched(null);
            }
        }
        if (left != null && (this.x < (left.x+size))) {
            Touched(null);
        }
        if (right != null && (this.x > (right.x-size))) {
            Touched(null);
        }
        if (this.sprite.name.equals("ARROW_UP")) {
            if (up != null && (this.y < (up.y+size))) {
                Touched(null);
            }
            if (down != null && (this.y > (down.y))) {
                Touched(null);
            }
        }
    }

    @Override
    public void Touched(Collidable c) {
        GamePanel.Projectiles.remove(this);
        if (c == null) return;
        if (c instanceof NPC && GamePanel.Enemies.contains((NPC) c)) {
            ((NPC) c).damage(this.game.FILE.power_Level);
        } else if (c instanceof Pot) {
            ((Pot) c).destroy();
        }
    }
}
