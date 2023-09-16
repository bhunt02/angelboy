package visual;

import main.GamePanel;
import main.Main;
import main.Player;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class LoadScreen extends JPanel {

    RenderedSprite[] sprite_Set = new RenderedSprite[7];
    int anim_Index = 1;
    int counter = 0;
    public LoadScreen() {
        this.setVisible(false);
    }

    /**
     * Proxy method to trigger repaint operations, used by the GraphicsRenderer that owns this LoadScreen
     */
    public void triggerPaint() {
        repaint();
    }
    @Override
    public void paintComponent(Graphics g) {
        g.setColor(Color.black);
        g.fillRect(0,0, Main.screen_Width,Main.screen_Height);
        if (Player.PLAYER_SPRITES.containsKey("WALK_"+anim_Index+"_M")) {
            RenderedSprite s = Player.PLAYER_SPRITES.get("WALK_" + anim_Index + "_M").clone();
            s.x = GamePanel.centerScreen[0];
            s.y = GamePanel.centerScreen[1];
            if (s.sprite != null) {
                int w = s.sprite.getWidth();
                int h = s.sprite.getHeight();
                int x = s.x - (w / 2);
                int y = s.y - (h / 2);
                g.drawImage(s.sprite, x, y, w, h, this);

                if (counter > 16) {
                    ++anim_Index;
                    if (anim_Index > 7) anim_Index = 0;
                    counter = 0;
                } else {
                    ++counter;
                }
            }
            g.setFont(GamePanel.fonts.get(24));
            g.setColor(Color.white);
            g.drawString("LOADING...",GamePanel.centerScreen[0]-96,GamePanel.centerScreen[1]+96);
        }

        if (counter > 16) {
            ++anim_Index;
            if (anim_Index > 7) anim_Index = 1;
            counter = 0;
        } else {
            ++counter;
        }
    }
}
