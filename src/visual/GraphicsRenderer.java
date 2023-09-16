package visual;

import main.*;

import javax.swing.*;
import java.awt.*;
import java.util.TimerTask;
import java.util.Timer;

public class GraphicsRenderer extends Thread implements Runnable {
    public JFrame window;
    public GamePanel gamepanel;
    public LoadScreen loadscreen;
    public Player player;
    public Game game;
    public static final Dimension GAME_SCREEN_SIZE = new Dimension(1504,907);
    public GraphicsRenderer(Game g, JFrame frame, Player p) {
        window = frame;
        game = g;
        player = p;
        loadscreen = new LoadScreen();
        loadscreen.setPreferredSize(GAME_SCREEN_SIZE);
        loadscreen.setMaximumSize(GAME_SCREEN_SIZE);
        loadscreen.setMinimumSize(GAME_SCREEN_SIZE);

        gamepanel = new GamePanel(game);
        gamepanel.setPreferredSize(GAME_SCREEN_SIZE);
        gamepanel.setMaximumSize(GAME_SCREEN_SIZE);
        gamepanel.setMinimumSize(GAME_SCREEN_SIZE);

        frame.setContentPane(gamepanel);
        frame.setGlassPane(loadscreen);

        game.addGameScreen(gamepanel);
        window.setBackground(Color.BLACK);
        window.setVisible(true);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.pack();
    }

    class UpdateGraphics extends TimerTask {
        Game game;
        UpdateGraphics(Game game) {
            this.game = game;
        }

        public void run() {
            if (game.LOADING) {
                loadscreen.setVisible(true);
                loadscreen.triggerPaint();
            } else {
                loadscreen.setVisible(false);
            }
            if (gamepanel.getWidth() != GAME_SCREEN_SIZE.getWidth() || gamepanel.getHeight() != GAME_SCREEN_SIZE.getHeight()) {
                gamepanel.setSize(GAME_SCREEN_SIZE);
                loadscreen.setSize(GAME_SCREEN_SIZE);
                int x = (int) (window.getWidth()-GAME_SCREEN_SIZE.getWidth())/4;
                int y = (int) (window.getHeight()-GAME_SCREEN_SIZE.getHeight())/4;
                gamepanel.setLocation(x,y);
                loadscreen.setLocation(x,y);
            }

            gamepanel.triggerPaint();
        }
    }

    public void run() {
        Timer updateGraphics = new Timer("UPDATE GRAPHICS");
        updateGraphics.scheduleAtFixedRate(new UpdateGraphics(this.game),0,17);
    }
}
