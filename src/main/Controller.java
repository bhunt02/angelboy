package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
public class Controller implements KeyListener {

    Game GAME;
    Controller(Game game) {
        this.GAME = game;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (GAME != null) {
            GAME.keyPressed(e.getKeyCode());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (GAME != null) {
            GAME.keyReleased(e.getKeyCode());
        }
    }
}
