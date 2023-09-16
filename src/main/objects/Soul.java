package main.objects;

import main.Game;
import main.GamePanel;
import visual.RenderedSprite;

import java.io.IOException;

public class Soul extends Collidable {
    int x = 0;
    int y = 0;
    int amount;
    RenderedSprite sprite = null;
    public Soul(int x, int y, int amount, Game game) throws IOException {
        super(x, y, new RenderedSprite("SOUL_" + amount, false, x, y, 1.0, 1), game);
        this.amount = amount;
    }
    @Override
    public void Touched(Collidable c) {
        if (c.name.contains("PLAYER")) {
            game.PLAYER.current_Hearts += amount;
            amount = 0;
            GamePanel.Items.remove(this.sprite);
            this.sprite = null;
        }
    }

}
