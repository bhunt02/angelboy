package main;

import visual.RenderedSprite;
import static main.GamePanel.centerScreen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class StageManager {
    Game game;
    static Hashtable<String,Stage> STAGES = new Hashtable<>();
    int world;
    int level;
    StageManager(int w, int l, Game game) {
        this.world = w;
        this.level = l;
        this.game = game;
        loadStage(true);
    }
    static Hashtable<Integer,String> tile_Codes = new Hashtable<>();
    static Hashtable<Integer,String> decor_Codes = new Hashtable<>();
    static Hashtable<Integer,String> poi_Codes = new Hashtable<>();
    static String[] rooms = new String[] {"POWERROOM","TREASUREROOM","NEST","MARKET","BLACKMARKET","CHALLENGEROOM","SPRINGROOM"};

    static String[] exitrooms = new String[] {"0-EXITROOM"};

    static {
        // TILES
        tile_Codes.put(0,"3_FORM_BRICK");
        tile_Codes.put(1,"BRICK");
        tile_Codes.put(2,"GROUND");
        tile_Codes.put(3,"PLATFORM");
        tile_Codes.put(4,"STONE");
        tile_Codes.put(5,"STUD_BRICK");
        tile_Codes.put(6,"WARPED");
        tile_Codes.put(8,"CLOUD");
        tile_Codes.put(9,"CLOUD_CORNER");
        tile_Codes.put(10,"ENEMY_POT");
        tile_Codes.put(11,"GLOSSY_PLATFORM");
        tile_Codes.put(12,"GLOSSY_PLATFORM_CORNER");
        tile_Codes.put(13,"ICE_CORNER");
        tile_Codes.put(14,"ICE_TOP");
        tile_Codes.put(15,"ICEY_PLATFORM");
        tile_Codes.put(16,"ICEY_PLATFORM_CORNER");
        tile_Codes.put(17,"LADDER");
        tile_Codes.put(18,"SPECIAL_BRICK");
        tile_Codes.put(19,"STUD_PLATFORM");
        tile_Codes.put(20,"STUDDED_BRICK");
        tile_Codes.put(21,"ICE_BOTTOM");
        tile_Codes.put(22,"TEMPLE_BRICK");
        tile_Codes.put(25,"TEMPLE_ROOF");
        tile_Codes.put(26,"TEMPLE_ROOF_INNER_CORNER");
        // FOREGROUND
        decor_Codes.put(3,"BUSH");
        decor_Codes.put(4,"COLUMN");
        decor_Codes.put(5,"DOOR_CLOSED");
        decor_Codes.put(6,"DOOR_OPEN");
        decor_Codes.put(7,"GRASS_COVER");
        decor_Codes.put(8,"HANGING_COLUMN_A");
        decor_Codes.put(9,"PILLAR");
        decor_Codes.put(10,"CLOUDTOP");
        decor_Codes.put(11,"CLOUDTOP_CORNER");
        decor_Codes.put(13,"TEMPLE_ROOF_CORNER");
        decor_Codes.put(15,"TOMBSTONE");
        decor_Codes.put(16,"LAVA");
        decor_Codes.put(17,"WATER_TOP");
        decor_Codes.put(18,"WATER");
        decor_Codes.put(19,"SPRING");
        decor_Codes.put(20,"CLOUD_LONG");
        decor_Codes.put(21,"CLOUD_MEDIUM");
        decor_Codes.put(22,"CLOUD_HUGE");
        decor_Codes.put(23,"TREE");
        decor_Codes.put(26,"COLUMN_CORINTHIAN_BASE");
        decor_Codes.put(27,"COLUMN_CORINTHIAN_TOP");
        decor_Codes.put(28,"COLUMN_IONIC_BASE");
        decor_Codes.put(29,"COLUMN_IONIC_TOP");
        decor_Codes.put(30,"COLUMN_MIDDLE");
        decor_Codes.put(31,"PILLAR_BOTTOM");
        decor_Codes.put(32,"PILLAR_BOTTOM_BROKEN_A");
        decor_Codes.put(33,"PILLAR_BOTTOM_BROKEN_B");
        decor_Codes.put(34,"PILLAR_BOTTOM_BROKEN_C");
        decor_Codes.put(35,"PILLAR_MIDDLE");
        decor_Codes.put(36,"PILLAR_MIDDLE_BROKEN_A");
        decor_Codes.put(37,"PILLAR_MIDDLE_BROKEN_B");
        decor_Codes.put(38,"PILLAR_MIDDLE_BROKEN_C");
        decor_Codes.put(39,"PILLAR_TOP");
        decor_Codes.put(40,"PILLAR_TOP_BROKEN_A");
        decor_Codes.put(41,"PILLAR_TOP_BROKEN_B");
        decor_Codes.put(42,"PILLAR_TOP_BROKEN_C");
        decor_Codes.put(99,"STATUE_UNDERWORLD");
        decor_Codes.put(100,"LIONGOYLE");
        // POINTS OF INTEREST
        poi_Codes.put(0,"SPAWNPOINT");
        poi_Codes.put(1,"EXITPOINT");
        poi_Codes.put(2,"RESPAWNPOINT");
        poi_Codes.put(3,"POWERROOM");
        poi_Codes.put(4,"TREASUREROOM");
        poi_Codes.put(5,"NEST");
        poi_Codes.put(6,"MARKET");
        poi_Codes.put(7,"BLACKMARKET");
        poi_Codes.put(8,"CHALLENGEROOM");
        poi_Codes.put(9,"SPRINGROOM");
    }
    String decodeTile(int n, Hashtable<Integer,String> codeHolder) {
        n = (n ^ 0xFF000000) >> 16;
        if (n == -1) {
            return "Void";
        } else {
            String tile = "Void";
            if (codeHolder.containsKey(n)) {
                tile = codeHolder.get(n);
            }
            return tile;
        }
    }
    static void loadStages(ArrayList<File> list) {
        for (int w = 0; w <= 6; w++) {
            for (int l = 1; l <= 5; l++) {
                String title = w + "-" + l;
                ArrayList<File> level = new ArrayList<>();
                for (File f : list) {
                    if (f.getName().contains(title)) {
                        level.add(f);
                    }
                }
                list.removeAll(level);
                processStage(level, title);
            }
        }
        for (String name : rooms) {
            ArrayList<File> room = new ArrayList<>();
            for (File f : list) {
                if (f.getName().contains(name)) {
                    room.add(f);
                }
            }
            processStage(room, name);
        }
        for (String name : exitrooms) {
            ArrayList<File> room = new ArrayList<>();
            for (File f : list) {
                if (f.getName().contains(name)) {
                    room.add(f);
                }
            }
            processStage(room, name);
        }
    }

    private static void processStage(ArrayList<File> level, String title) {
        try {
            File bg = null;
            File dt = null;
            BufferedImage tl = null;
            BufferedImage fg = null;
            BufferedImage po = null;
            for (File f : level) {
                String n = f.getName();
                if (n.contains("BACKGROUND.png")) {
                    bg = f;
                } else if (n.contains("_data.txt")) {
                    dt = f;
                } else if (n.contains("_TILES.png")) {
                    tl = ImageIO.read(f);
                } else if (n.contains("_FOREGROUND.png")) {
                    fg = ImageIO.read(f);
                } else if (n.contains("_POI.png")) {
                    po = ImageIO.read(f);
                }
            }
            if (dt != null) {
                Stage s = new Stage(title, dt, bg, tl, fg, po);
                STAGES.put(title, s);
            }
        } catch (IOException ignored) {}
    }
    RenderedSprite background = null;
    RenderedSprite roomBackground = null;
    static final double scale = 1.5;
    public static final int size = (int) (50 * scale);
    int[] spawnPoint = null;
    int[] roomEntrance = null;
    public Stage currentStage = null;
    public Stage currentRoom = null;

    public void loadStage(boolean init) {
        Player p = this.game.PLAYER;
        if (!init) {
            if (this.currentStage == null) return;
            if (p == null) return;

            int stageMode = this.currentStage.stageMode;

            if (stageMode == 0) {
                for (int y = 0; y < this.currentStage.stageHeight; y++) {
                    for (int x = 0; x < 16; x++) {
                        generateStageTile(this.currentStage, y, x, GamePanel.Tiles, GamePanel.Foreground_Tiles, GamePanel.POI_Tiles);
                    }
                }
            } else if (stageMode == 1) { // HORIZONTAL SCROLLER
                for (int y = 0; y < this.currentStage.stageHeight; y++) {
                    for (int x = 0; x < this.currentStage.stageWidth; x++) {
                        generateStageTile(this.currentStage, y, x, GamePanel.Tiles, GamePanel.Foreground_Tiles, GamePanel.POI_Tiles);
                    }
                }
            } else if (stageMode == 2) { // TEMPLE

            } else if (stageMode == 3) { // VERTICAL FLIGHT
                for (int y = 0; y < this.currentStage.stageHeight; y++) {
                    for (int x = 0; x < 16; x++) {
                        generateStageTile(this.currentStage, y, x, GamePanel.Tiles, GamePanel.Foreground_Tiles, GamePanel.POI_Tiles);
                    }
                }
            } else if (stageMode == -1) { // HORIZONTAL FLIGHT
                for (int y = 0; y < this.currentStage.stageHeight; y++) {
                    for (int x = 0; x < this.currentStage.stageWidth; x++) {
                        generateStageTile(this.currentStage, y, x, GamePanel.Tiles, GamePanel.Foreground_Tiles, GamePanel.POI_Tiles);
                    }
                }
            }
        } else {
            this.currentStage = STAGES.get(this.world+"-"+this.level);
            GamePanel.Tiles = new RenderedSprite[this.currentStage.stageHeight][this.currentStage.stageWidth];
            GamePanel.Foreground_Tiles = new RenderedSprite[this.currentStage.stageHeight][this.currentStage.stageWidth];
            GamePanel.POI_Tiles = new int[this.currentStage.stageHeight][this.currentStage.stageWidth];
            if (this.background == null && this.currentStage.background != null) {
                int tiles_Width = this.currentStage.stageWidth * size;
                double x_Scale = (tiles_Width * 1.0) / this.currentStage.background.getWidth();
                this.background = new RenderedSprite(
                        this.currentStage.background,
                        (int) ((this.currentStage.background.getWidth()/2)*x_Scale)+(2*size)+(2),
                        (-this.currentStage.background.getHeight()/2),
                        x_Scale,
                        -1,
                        null);
            }
            loadStage(false);
        }
    }
    public void loadRoom(String room) {
        Stage r = STAGES.get(room);
        this.currentRoom = r;
        boolean entrance = false;
        GamePanel.Room_Tiles = new RenderedSprite[16][16];
        GamePanel.Room_Foreground_Tiles = new RenderedSprite[16][16];
        GamePanel.Room_POI_Tiles = new int[16][16];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (!entrance) {
                    String poi_Type = decodeTile(r.poi.getRGB(x,r.stageHeight - (y + 1)), poi_Codes);
                    if (poi_Type.equals("RESPAWNPOINT")) {
                        entrance = true;
                        this.roomEntrance = new int[]{x, y-1};
                    }
                }
                generateStageTile(r, y, x, GamePanel.Room_Tiles, GamePanel.Room_Foreground_Tiles, GamePanel.Room_POI_Tiles);
            }
        }
        if (this.roomBackground == null && this.currentRoom.background != null) {
            Color tint = null;
            if (this.currentRoom.colorRules.containsKey("BACKGROUND")) {
                tint = this.currentRoom.colorRules.get("BACKGROUND");
            }
            this.roomBackground = (tint == null ? new RenderedSprite(
                    this.currentRoom.background,
                    centerScreen[0],
                    (centerScreen[1]-(this.currentRoom.background.getHeight())),
                    2.345,
                    -1,
                    null)
                    :
                    new RenderedSprite(
                    this.currentRoom.background,
                    centerScreen[0], (centerScreen[1]-(this.currentRoom.background.getHeight())),
                    2.345,
                    -1,
                    tint)
            );
        }
    }
    private void generateStageTile(Stage source, int y, int x, RenderedSprite[][] tiles, RenderedSprite[][] foreground, int[][] poi) {
        int nP = source.poi.getRGB(x,source.stageHeight - (y + 1));
        String poi_Type = decodeTile(nP, poi_Codes);
        nP = (nP ^ 0xFF000000) >> 16;
        poi[y][x] = nP;
        if (poi[y][x] < -1) poi[y][x] = -1;
        if (poi_Type.equals("SPAWNPOINT")) {
            this.spawnPoint = new int[]{x,y};
        }

        int nT = source.tiles.getRGB(x,source.stageHeight - (y + 1));
        int nF = source.foreground.getRGB(x,source.stageHeight - (y + 1));

        String type0 = decodeTile(nT, tile_Codes);
        String type1 = decodeTile(nF, decor_Codes);

        if (type1.equals("TREE")) {
            type1 = switch (currentStage.worldname) {
                case "Overworld", "Debug" -> "OVERWORLD_TREE";
                case "Underworld" -> "UNDERWORLD_TREE";
                case "Sky Kingdom", "Mountain" -> "SKYKINGDOM_TREE";
                default -> type1;
            };
        } else if (type1.equals("STATUE")) {
            type1 = switch (currentStage.worldname) {
                case "Overworld" -> "OVERWORLD_STATUE";
                case "Underworld" -> "UNDERWORLD_STATUE";
                case "Sky Kingdom", "Mountain" -> "SKYKINGDOM_STATUE";
                case "Palace" -> "PALACE_STATUE";
                default -> type1;
            };
        }

        if (type1.contains("CORNER")) {
            type1 = edgify(this.currentStage,source.stageHeight - (y + 1),x,type1);
        }
        if (type0.contains("CORNER")) {
            type0 = edgify(this.currentStage,source.stageHeight - (y + 1),x,type0);
        }

        addTile(source, x, y, type0, tiles);
        addTile(source, x, y, type1, foreground);
    }
    private String edgify(Stage source, int y, int x, String type) {
        String new_Type = type;
        boolean left_Tile = true;
        try {
            left_Tile = decodeTile(source.tiles.getRGB(x - 1, y), tile_Codes).equals("Void");
        } catch(ArrayIndexOutOfBoundsException ignored) {}
        boolean left_Foreground = true;
        try {
            left_Foreground = decodeTile(source.foreground.getRGB(x-1,y),decor_Codes).equals("Void");
        } catch(ArrayIndexOutOfBoundsException ignored) {}
        boolean right_Tile = true;
        try {
            right_Tile = decodeTile(source.tiles.getRGB(x + 1, y), tile_Codes).equals("Void");
        } catch(ArrayIndexOutOfBoundsException ignored) {}
        boolean right_Foreground = true;
        try {
            right_Foreground = decodeTile(source.foreground.getRGB(x+1,y),decor_Codes).equals("Void");
        } catch(ArrayIndexOutOfBoundsException ignored) {}

        switch (type) {
            case "CLOUDTOP_CORNER" -> {
                if (!left_Foreground) {
                    new_Type = type + "_M";
                }
            }
            case "TEMPLE_ROOF_CORNER" -> {
                if (!right_Tile || !right_Foreground) {
                    new_Type = type + "_M";
                }
            }
            case "TEMPLE_ROOF_INNER_CORNER" -> {
                if (left_Tile || !left_Foreground) {
                    new_Type = type + "_M";
                }
            }
            default -> {
                if (left_Tile) {
                    new_Type = type + "_M";
                }
            }
        }
        return new_Type;
    }
    private void addTile(Stage source, int x, int y, String type, RenderedSprite[][] destination) {
        if (!type.equals("Void") && !type.equals("")) {
            try {

                boolean mirrored = type.endsWith("_M");
                if (mirrored) {
                    type = type.substring(0,type.indexOf("_M"));
                }

                Color tint = null;
                Stage src;

                if (source.identity.contains("EXITROOM")) {
                    src = source;
                } else {
                    src = this.currentStage;
                }

                if (type.startsWith("PILLAR") || type.startsWith("COLUMN")) {
                    if (src.colorRules.containsKey(type.substring(0, 6))) {
                        tint = src.colorRules.get(type.substring(0, 6));
                    }
                } else if (type.contains("TEMPLE") && src.colorRules.containsKey("3_FORM_BRICK")) {
                    tint = src.colorRules.get("3_FORM_BRICK");
                } else if (src.colorRules.containsKey(type)) {
                    tint = src.colorRules.get(type);
                }

                if (destination[y][x] == null || (destination[y][x].name != null && !destination[y][x].name.equals(type))) {
                    RenderedSprite tile = (tint == null ? new RenderedSprite(type, mirrored, 0, 0, scale, 0) : new RenderedSprite(type, mirrored, 0, 0, scale, 0, tint));
                    GamePanel.toScreenPosition(y, x, tile);
                    destination[y][x] = tile;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
           destination[y][x] = null;
        }
    }
    public int[] advanceLevel(int w, int l) {
        if (!STAGES.containsKey(w+"-"+l)) {
            int[] temp = advanceLevel(w+1,1);
            w = temp[0];
            l = temp[1];
        }
        this.world = w;
        this.level = l;
        return new int[] {w,l};
    }
}

