package main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Scanner;
public class Stage {
    String identity;
    String worldname;
    BufferedImage tiles;
    BufferedImage foreground;
    BufferedImage poi;
    BufferedImage background = null;
    int stageHeight;
    int stageWidth;
    public int stageMode = 0;
    /*
     * {0} = VERTICAL SCROLLER
     * {1} = HORIZONTAL SCROLLER
     * {2} = TEMPLE
     * {-1} = FLIGHT HORIZONTAL
     * {3} = FLIGHT VERTICAL
     */
    Hashtable<String, Color> colorRules = new Hashtable<>();

    private void readData(File data) {
        try {
            Scanner s = new Scanner(data);
            boolean colorInput = false;
            while (s.hasNextLine()) {
                String ln = s.nextLine();
                if (ln.startsWith("name")) {
                    this.worldname = ln.substring(5);
                } else if (ln.contains("stageMode")) {
                    this.stageMode = Integer.parseInt(ln.substring(9));
                } else if (ln.contains("COLORRULES")) {
                    colorInput = true;
                } else if (colorInput && ln.length() == 0) {
                    colorInput = false;
                } else if (colorInput) {
                    Scanner s0 = new Scanner(ln);
                    if (s0.hasNext()) {
                        String type = s0.next();
                        int[] rgb = new int[3];
                        for (int i = 0; i < 3; i++) {
                            rgb[i] = Integer.parseInt(s0.next());
                        }
                        this.colorRules.put(type, new Color(rgb[0], rgb[1], rgb[2]));
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    Stage(String identity, File data, File background, BufferedImage tl, BufferedImage fg, BufferedImage po) {
        this.identity = identity;
        this.tiles = tl;
        this.foreground = fg;
        this.poi = po;
        this.stageWidth = tiles.getWidth();
        this.stageHeight = tiles.getHeight();
        readData(data);
        if (background != null) {
            try {
                this.background = ImageIO.read(background);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
