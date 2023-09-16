package main;

import visual.Sprite;

import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.io.*;
public class Main {
    public static int screen_Width = (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getWidth();
    public static int screen_Height = (int) GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight()-(int)(Toolkit.getDefaultToolkit().getScreenSize().getHeight()-GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getHeight());
    public static final String TITLE = "Angel Boy DEMO";
    static void loadAssets() {
        try {
            File spritesDirectory = new File("src/sprites/");
            ArrayList<File> visualsList = Utilities.getList(spritesDirectory);

            File stagesDirectory = new File("src/stages/");
            ArrayList<File> stagesList = Utilities.getList(stagesDirectory);

            File savesDirectory = new File("src/savefiles/");
            ArrayList<File> savesList = Utilities.getList(savesDirectory);

            Game.loadFiles(savesList);

            ArrayList<String> l = new ArrayList<>();
            for (File visual : visualsList) {
                String name = visual.getName();
                name = name.substring(0, name.length() - 4);
                String path = visual.getCanonicalPath();
                path = path.substring(path.indexOf("src"));
                new Sprite(path, name);
                if (path.contains("player")) {
                    l.add(name);
                }
            }
            Player.loadSprites(l);
            StageManager.loadStages(stagesList);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    static public void request() {
        boolean success = run();
        if (!success) {
            System.err.println("Error encountered while instantiating game instance.");
        }
    }
    static private boolean run() {
        JFrame window = new JFrame(Main.TITLE);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setIconImage(Toolkit.getDefaultToolkit().getImage(Sprite.List.get("ic_window").address));

        Game game = new Game(window);
        final Thread GAME_THREAD = new Thread(game);
        GAME_THREAD.start();

        return true;
    }
    public static void main(String[] args) {
        loadAssets();

        request();
    }
}
