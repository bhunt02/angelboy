package main;
import main.objects.Collidable;
import main.technical.SaveFile;
import utilities.ConcurrentCollection;
import visual.RenderedSprite;
import visual.Sprite;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static main.StageManager.size;
import static visual.GraphicsRenderer.GAME_SCREEN_SIZE;

public class GamePanel extends JPanel {
    // RENDERING VARIABLES
    public static int[] centerScreen = new int[2];
    public double[] scale_Ratio = new double[] {1.0,1.0};

    /* NON-STAGE VISUALS
    *
    * Items: Sprites and visuals to be drawn at every render step outside of stage rendering conventions
    *       --> PriorityQueue is used to draw RenderedSprites according to their drawing order
    */
    public static ConcurrentCollection<RenderedSprite> Items = new ConcurrentCollection<>();

    /* MAIN STAGE TILES (SIZE IS VARIABLE ACCORDING TO STAGE)
     *
     * Tiles: Tiles with interactive physics that make up the stage at large
     * Foreground_Tiles: Tiles overlain in front of the player's sprite for immersion and decoration
     * POI_Tiles: Locations denoting 'points of interest' in the room; i.e. doors.
     * Interactable_Tiles: Locations denoting where to spawn visible interactable items that have no collision w/ player; i.e. monsters
     */

    public static RenderedSprite[][] Tiles = new RenderedSprite[16][16];
    public static RenderedSprite[][] Foreground_Tiles = new RenderedSprite[16][16];
    public static int[][] POI_Tiles = new int[16][16];
    public static int[][] Interactable_Tiles = new int[16][16];

    /* ROOM TILES (SIZE IS STATIC); ALL ROOMS ARE 16x16
    *
    * Room_Tiles: Tiles with interactive physics that make up the room at large
    * Room_Foreground_Tiles: Tiles overlain in front of the player's sprite for immersion and decoration
    * Room_POI_Tiles: Locations denoting 'points of interest' in the room; i.e. doors.
    * Room_Interactable_Tiles: Locations denoting where to spawn visible interactable items that have no collision w/ player in the room; i.e. monsters
    */

    public static RenderedSprite[][] Room_Tiles = new RenderedSprite[16][16];
    public static RenderedSprite[][] Room_Foreground_Tiles = new RenderedSprite[16][16];
    public static int[][] Room_POI_Tiles = new int[16][16];
    public static int[][] Room_Interactable_Tiles = new int[16][16];

    /* STAGE RENDERING VARIABLES
    *
    * stage_Offset: How far has the stage been advanced into; offsets the rendered visuals in the viewport proportional to progression
    * background_Offset: How far has the stage been advanced into; offsets the rendered background in the viewport proportional to progression
    * stage_Offset_LIMIT: How far can the rendered visuals be advanced into; prevents the stage from clipping out of the background
    * stage_Offset_MIN: How far should the rendered visuals be advanced into by default?
    * render_Low: The starting point for which tiles from the stage should be rendered
    * render_High: The end point for which tiles from the stage should be rendered
    * load_Buffer: When the stage loads for the first time, cover it up for aesthetic reasons
    * fluid_Index: When fluids are on the screen, which sprite should be selected? (animation)
    * fluid_Reverse: Reverse fluid animation after one cycle, and back again after another
    */

    public static int stage_Offset = 0;
    public static int background_Offset = 0;
    public static int stage_Offset_LIMIT = -1;
    public static int stage_Offset_MIN = -1;
    int render_Low = 0;
    int render_High = 16;
    int fluid_Index = 1;
    boolean fluid_Reverse = false;

    /* NON-TILE STAGE SPRITES
    *
    * COLLECTIBLES: Storage of sprites that represent collectibles i.e. currency that appear periodically
    * HEARTS: Storage of sprites that represent the player's health
    * fonts: Storage of the font used to type strings in the game in multiples of 12
    * displayScore: display score at the end of a stage
    */
    public static Hashtable<String,RenderedSprite> COLLECTIBLES = new Hashtable<>();
    public static Hashtable<String,RenderedSprite> HEARTS = new Hashtable<>();
    public static ConcurrentCollection<NPC> Enemies = new ConcurrentCollection<>();
    public static ConcurrentCollection<Collidable> Projectiles = new ConcurrentCollection<>();
    public static Hashtable<Integer, Font> fonts = new Hashtable<>();
    boolean displayScore = false;

    /* CUTSCENE-SPECIFIC VARIABLES
    *
    * typing: is the player inputting text?
    *
    * title_Display: is the title screen currently displaying
    * title_Start: where the logo should start at
    * title_background_Start: where the title background should start at
    * title_Current: current position of title text
    * title_background_Current: current position of title background
    *
    * fileselect_Display: is the file select currently displaying
    * pointer_y: where the cursor is pointing in file select
    * pointer_x: where the cursor is pointing in the file select (on portions with more than 1 option)
    */
    boolean typing = false;
    boolean title_Display = false;
    int title_Start = -250;
    int title_background_Start = -250+this.getHeight();
    double title_Current = title_Start;
    double title_background_Current = title_background_Start;
    boolean fileselect_Display = false;
    boolean createFile = false;
    boolean eraseFile = false;
    int pointer_y = 0;
    int pointer_x = 0;

    /* DEBUG ONLY
    *
    * PlayerTiles: Tiles specially aligned around the player to represent its collision zones
    */
    public static ConcurrentCollection<RenderedSprite> PlayerTiles = new ConcurrentCollection<>();

    /* IMPORTANT GAME OBJECTS
    *
    * game: the Game that this GamePanel is rendering items for
    * sm: the StageManager that is employed by the game, with important static variables to be used when calculating Rendering positions
     */
    public Game game = null;
    public StageManager sm = null;
    public GamePanel(Game game) {
        this.game = game;
        centerScreen[0] = (int) (GAME_SCREEN_SIZE.getWidth()/2);
        centerScreen[1] = (int) (GAME_SCREEN_SIZE.getHeight()/2);
        setIgnoreRepaint(true);
        setDoubleBuffered(true);
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        this.addKeyListener(new Controller(game));
        try {
            Font base = Font.createFont(Font.TRUETYPE_FONT, new File("src/sprites/Sprites/font/font.ttf"));
            for (int i = 1; i < 12; i++) {
                int s = 12*i;
                fonts.put(s,base.deriveFont(Font.PLAIN,s));
            }
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }
        Sprite.List.forEach((k,s) -> {
            if (k.contains("hrt")) {
                try {
                    RenderedSprite rs = new RenderedSprite(k, false, 0, 0, 0.5, 1);
                    int n = Integer.parseInt(k.substring(k.indexOf("_")+1));
                    COLLECTIBLES.put("H"+n, rs);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (k.contains("hea")) {
                try {
                    RenderedSprite rs = new RenderedSprite(k,false,0,0,1,1);
                    HEARTS.put(k,rs);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Proxy method to trigger repaint operations, used by the GraphicsRenderer that owns this GamePanel
     */
    public void triggerPaint() {
        repaint();
    }

    /**
     * Adds the current StageManager that the Game is using
     *
     * @param sm the StageManager that the Game is using
     */
    public void addStage(StageManager sm) {
        this.sm = sm;
        if (sm.currentStage.stageMode == 0 || sm.currentStage.stageMode == 3) {
            background_Offset = 999999;
        }
    }

    /**
     * Paints all elements of the Game onto the GamePanel
     *
     * @param g the specified Graphics context
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        PriorityQueue<RenderedSprite> PQ = null;

        if (!Items.isEmpty()) {
            PQ = new PriorityQueue<>(Items);
            drawFromQueue(g,PQ,-1);
            drawFromQueue(g,PQ,0);
        }

        if (game.IN_GAME && !game.PLAYER.dead) {
            drawMain(g);
        } else if (game.FILE_SELECT) {
            drawFileSelect(g, true);
        } else if (game.TITLE_SCREEN) {
            drawTitleScreen(g,true);
        } else if (game.PLAYER.dead) {
            drawDeathScreen(g, true);
        }

        if (PQ != null) {
            drawFromQueue(g,PQ,1);
        }

        if (!game.PLAYER.dead && deathScreen_Display) {
            drawDeathScreen(g, false);
        }
        if (!game.TITLE_SCREEN && title_Display) {
            drawTitleScreen(g,false);
        }
        if (!game.FILE_SELECT && fileselect_Display) {
            drawFileSelect(g,false);
        }
    }
    private void drawFromQueue(Graphics g, PriorityQueue<RenderedSprite> PQ, int order) {
        while (!PQ.isEmpty()) {
            if (!(PQ.peek() == null)) {
                if (PQ.peek().drawingOrder == order) {
                    RenderedSprite s = PQ.poll();
                    if (s.sprite == null) continue;
                    int w = s.sprite.getWidth();
                    int h = s.sprite.getHeight();
                    int x = s.x - (w / 2);
                    int y = s.y - (h / 2);
                    g.drawImage(s.sprite, x, y, w, h, this);
                } else {
                    break;
                }
            } else {
                PQ.poll();
            }
        }
    }

    private void drawFromConcurrentCollection(Graphics g, ConcurrentCollection<? extends Collidable> c, int stageMode) {
        for (Collidable e : c) {
            e.update();
            if (e.sprite != null) {
                drawSprite(g,e.sprite,true,stageMode);
            }
        }
    }

    RenderedSprite title = null;
    RenderedSprite title_Background = null;

    /**
     * Draws the title screen
     *
     * @param g: the specified graphics context
     * @param active: turn title screen on/off
     */
    void drawTitleScreen(Graphics g, boolean active) {
        if (active) {
            title_Display = true;
            g.setFont(fonts.get(36));
            g.setColor(Color.white);
            title_background_Current = title_background_Current + 4;
            if (title_background_Current > (this.getHeight() + 250)) {
                title_background_Current = this.getHeight() + 250;
                title_Current += 4;
            }
            if (title_Current >= centerScreen[1]) {
                title_Current = centerScreen[1];
                g.drawString("PRESS ANY KEY TO START",centerScreen[0]-370, centerScreen[1]+325);
            }
            if (title != null) {
                title.y = (int) title_Current;
            }
            if (title_Background != null) {
                title_Background.y = (int) title_background_Current;
            }
            try {
                if (title == null) {
                    title = new RenderedSprite("ts_t", false, centerScreen[0], (int) title_Current, 2.0, 1);
                    if (!Items.contains(title)) {
                        Items.add(title);
                    }
                }
                if (title_Background == null) {
                    title_Background = new RenderedSprite("ts_bg", false, centerScreen[0], (int) title_background_Current, 1.1, -1);
                    if (!Items.contains(title_Background)) {
                        Items.add(title_Background);
                    }
                }
            } catch (IOException ignored){}
        } else {
            if (title != null) {
                Items.remove(title);
                title = null;
            }
            if (title_Background != null) {
                Items.remove(title_Background);
                title_Background = null;
            }
            title_background_Current = title_background_Start;
            title_Current = title_Start;
            title_Display = false;
        }
    }

    /**
     * Moves the cursor within file select
     *
     * @param dir: direction to move the pointer (-1, 1)
     * @param axis: what axis is the pointer being moved (0 = vertical, 1 = horizontal)
     */
    public void moveFilePointer(int dir, int axis) {
        if (fileselect_Display) {
            if (axis == 0) {
                if (dir < 0 && !(pointer_x > 0)) {
                    pointer_y -= 1;
                } else if (dir > 0) {
                    pointer_y += 1;
                }
                if (pointer_y < 0) {
                    pointer_y = 0;
                } else if (pointer_y > 3) {
                    pointer_y = 3;
                }

                if ((createFile || eraseFile) && pointer_y > 2) {
                    pointer_y = 2;
                }
            } else if (axis == 1 && pointer_y > 2) {
                pointer_x += dir;
                if (pointer_x < 0) {
                    pointer_x = 0;
                } else if (pointer_x > 1) {
                    pointer_x = 1;
                }
            }
        }
    }

    char[] typedName = new char[10];
    {Arrays.fill(typedName,' ');}
    int typedIndex = 0;

    /**
     * takes user input as keys being typed into file select
     *
     * @param key: key that user typed
     */
    void typeFileName(int key) {
        if (key == KeyEvent.VK_BACK_SPACE && typedIndex >= 0) {
            typedIndex -= 1;
            typedName[typedIndex] = ' ';
            if (typedIndex < 0)
                typedIndex = 0;
        } else if (key == KeyEvent.VK_ESCAPE) {
            Arrays.fill(typedName,' ');
            typedIndex = 0;
            this.typing = false;
            createFile = false;
        } else if (key == KeyEvent.VK_ENTER) {
            this.typing = false;
            createFile = false;
            game.GAME_LOOP.createNewFile(pointer_y,new String(typedName));
            Arrays.fill(typedName,' ');
            typedIndex = 0;
        } else if (KeyEvent.getKeyText(key).length() == 1 && Character.isAlphabetic(KeyEvent.getKeyText(key).charAt(0))) {
            if (typedIndex < 10) {
                typedName[typedIndex] = KeyEvent.getKeyText(key).charAt(0);
                typedIndex += 1;
            }
        }
    }

    RenderedSprite cursor = null;
    RenderedSprite fileselect_Background = null;
    /**
     * Draws the file select
     *
     * @param g: the specified graphics context
     * @param active: turn file select on/off
     */
    void drawFileSelect(Graphics g, boolean active) {
        if (active) {
            fileselect_Display = true;
            int startX = (int) ((centerScreen[0] - (4.5 * size)) + (size / 2.0));
            int startY = (int) (GAME_SCREEN_SIZE.getHeight() - ((size / 6) + (6 * size)));

            g.setColor(new Color(0,0,0,128));
            g.fillRect(centerScreen[0]-350,centerScreen[1]-110, 700,300);
            try {
                if (fileselect_Background == null) {
                    fileselect_Background = new RenderedSprite("fsBACKGROUND", false, centerScreen[0], centerScreen[1], 0.65, -1);
                    if (!Items.contains(fileselect_Background)) {
                        Items.add(fileselect_Background);
                    }
                }
            } catch (IOException ignored) {}

            if (cursor == null) {
                try {
                    cursor = new RenderedSprite("mn_cursor",false,startX-24,startY,1.0,1);
                } catch (IOException e) {
                    return;
                }
            } else {
                if (pointer_y > 2) {
                    cursor.y = startY + 20 + (pointer_y*30);
                } else {
                    cursor.y = startY - 10 + (pointer_y * 30);
                }
                cursor.x = (startX-24) + (pointer_x*120);
                if (!Items.contains(cursor)) {
                    Items.add(cursor);
                }
            }

            g.setFont(fonts.get(24));
            g.setColor(Color.white);
            for (int i = 0; i < 3; i++) {
                if (Game.SAVES[i] != null) {
                    SaveFile sv = Game.SAVES[i];

                    StringBuilder hearts_Represent = new StringBuilder();
                    int numDigits = Integer.toString(sv.hearts).length();
                    int zeroAdd = 3-numDigits;
                    hearts_Represent.append("0".repeat(Math.max(0, zeroAdd)));
                    hearts_Represent.append(sv.hearts);

                    StringBuilder score_Represent = new StringBuilder();
                    numDigits = Integer.toString(sv.score).length();
                    zeroAdd = 7-numDigits;
                    score_Represent.append("0".repeat(Math.max(0, zeroAdd)));
                    score_Represent.append(sv.score);

                    if ((createFile || eraseFile) && i != pointer_y) {
                        g.setColor(Color.gray);
                    } else {
                        g.setColor(Color.white);
                    }
                    String formatted = String.format("%10s %s %s %s", sv.identity, hearts_Represent, sv.world+"-"+sv.level, score_Represent);
                    g.drawString(formatted,startX,startY+(i*30));
                } else {
                    if (this.typing && i == pointer_y) {
                        g.setColor(Color.white);
                        g.drawString(new String(typedName),startX,startY+(i*30));
                    }
                    if ((createFile || eraseFile) && i != pointer_y) {
                        g.setColor(Color.gray);
                    } else {
                        g.setColor(Color.white);
                    }
                    g.drawString("__________ ___ ___ _______",startX,startY+(i*30));
                }
            }
            if (createFile || eraseFile) {
                g.setColor(Color.white);
                g.drawString("ESC to STOP", startX, startY + 150);
                if (pointer_y <= 2 && Game.SAVES[pointer_y] != null) {
                    if (createFile) {
                        g.setColor(Color.gray);
                    } else {
                        g.setColor(Color.white);
                    }
                } else {
                    if (createFile) {
                        g.setColor(Color.white);
                    } else {
                        g.setColor(Color.gray);
                    }
                }
                if (createFile) {
                    if (!typing) {
                        g.drawString("ENTER to START TYPING", startX, startY + 120);
                    } else {
                        g.drawString("ENTER to COMPLETE", startX, startY + 120);
                    }
                }
                if (eraseFile) {
                    g.drawString("ENTER to ERASE FILE", startX, startY + 120);
                }
                g.setColor(Color.gray);
            } else {
                g.drawString("NEW", startX, startY + 120);
                g.drawString("ERASE", startX + 120, startY + 120);
            }
            g.drawString("SELECT FILE", startX + 160, startY-60);
            g.drawString("ESC - TITLE SCREEN", startX + 80, startY+180);
        } else {
            if (fileselect_Background!= null) {
                Items.remove(fileselect_Background);
                fileselect_Background = null;
            }
            fileselect_Display = false;
            Items.remove(cursor);
            cursor = null;
        }
    }

    /**
     * moves the cursor on the death screen
     *
     * @param dir which direction to move the cursor
     */
    public void moveDeathPointer(int dir) {
        if (deathScreen_Display) {
            death_pointer += dir;

            if (death_pointer < 0) {
                death_pointer = 0;
            } else if (death_pointer > 1) {
                death_pointer = 1;
            }
        }
    }

    RenderedSprite deathcursor = null;
    boolean deathScreen_Display = false;
    int death_pointer = 0;

    /**
     * draws the death screen
     *
     * @param g the specified Graphics context
     * @param active whether to draw the screen or not
     */
    void drawDeathScreen(Graphics g, boolean active) {
        if (active) {
            int startX = (centerScreen[0]);
            int startY = (int) (GAME_SCREEN_SIZE.getHeight() - ((size / 6) + (6 * size)));

            if (deathcursor == null) {
                try {
                    deathcursor = new RenderedSprite("POINTER",false,startX-24,startY + (death_pointer*30),1.0,1);
                } catch (IOException e) {
                    return;
                }
            } else {
                deathcursor.y = startY + 50 + (death_pointer*30);
                deathcursor.x = startX-120;
                if (!Items.contains(deathcursor)) {
                    Items.add(deathcursor);
                }
            }
            deathScreen_Display = true;
            g.setColor(Color.black);
            g.fillRect(0,0,this.getWidth(),this.getHeight());
            g.setFont(fonts.get(24));
            g.setColor(Color.white);
            g.drawString("I'M A GONER!",startX-96,startY);
            g.drawString("CONTINUE?",startX-96,startY+30);
            g.drawString("YES",startX-96,startY+60);
            g.drawString("NO",startX-96,startY+90);
        } else {
            deathScreen_Display = false;
            Items.remove(deathcursor);
            deathcursor = null;
        }
    }
    /**
     * Draws IN-GAME elements
     *
     * @param g the specified Graphics context
     */
    void drawMain(Graphics g) {
        int stageMode = 0;
        if (sm != null && sm.currentStage != null) {
            stageMode = sm.currentStage.stageMode;
        }

        if (sm != null && sm.currentRoom != null) {
            stageMode = -2;
        }

        if (game.PLAYER != null && sm != null && game.PLAYER.spawned) {
            if (Math.abs(stageMode) != 2) {
                if (stageMode == 0 || stageMode == 3) {
                    int inc = (int) Math.round((size * 10) - (game.PLAYER.y + ((sm.spawnPoint[1] - 1) * size)));
                    if (inc > stage_Offset) {
                        stage_Offset = inc;
                        render_Low = (stage_Offset / size) - 3;
                    }
                } else if (stageMode == -1 || stageMode == 1) {
                    if (stage_Offset_LIMIT == -1) {
                        stage_Offset_LIMIT = (int) ((centerScreen[0] - (8 * size)) + (size / 2.0) + ((this.game.sm.currentStage.stageWidth - 19) * (size)));
                    }
                    if (sm.spawnPoint != null && stage_Offset_MIN == -1) {
                        stage_Offset_MIN = (size * (sm.spawnPoint[0] - 2));
                        stage_Offset = stage_Offset_MIN;
                    }
                    int inc = (int) (Math.round(game.PLAYER.x)) - (size * 10);
                    if (inc > stage_Offset && inc < stage_Offset_LIMIT) {
                        stage_Offset = inc;
                        render_Low = Math.abs(stage_Offset / size);
                    }
                }
            } else {
                // temple
            }
        }

        if (render_Low < 0) render_Low = 0;

        if (stageMode == -1 || stageMode == 1) {
            render_High = render_Low + 19;
            if (render_High > Tiles[0].length) render_High = Tiles[0].length;
        } else if (stageMode != -2) {
            render_High = render_Low + 19;
            if (render_High > Tiles.length) render_High = Tiles.length;
        }

        if (game.PLAYER != null && game.PLAYER.spawned && sm != null && sm.currentStage != null && sm.background != null) {
            int l = (16 * size);
            if (Math.abs(stageMode) != 2) {
                RenderedSprite s = sm.background;
                if (stageMode == 0 || stageMode == 3) {
                    double ratio = ((s.sprite.getHeight() - l) / (sm.currentStage.stageHeight * size * 1.0));
                    int inc = (int) ((s.sprite.getHeight() - l) - (stage_Offset * ratio));

                    if (inc < background_Offset) {
                        background_Offset = inc;
                    }

                    if (background_Offset < 0) {
                        background_Offset = 0;
                    }

                    if (background_Offset > s.sprite.getHeight() - l) {
                        background_Offset = s.sprite.getHeight() - l;
                    }

                    g.drawImage(s.sprite.getSubimage(0, background_Offset, l, l),
                            (size * 2) + 2,
                            0,
                            null
                    );
                } else if (stageMode == 1 || stageMode == -1) {
                    g.drawImage(s.sprite.getSubimage(stage_Offset, 0, l, l),
                            (size * 2) + 2,
                            -size * 4,
                            null
                    );
                }
            } else {
                if (stageMode == -2 && sm.roomBackground != null) {
                    RenderedSprite s = sm.roomBackground;
                    g.drawImage(s.sprite.getSubimage(0, 0, l, l), (size * 2) + 2, -size * 4, null);
                }
                // how the temple / room backgrounds work
            }
        }

        if (stageMode != -2) {
            drawTiles(stageMode, Tiles, g);
        } else {
            drawTiles(stageMode, Room_Tiles, g);
        }

        drawFromConcurrentCollection(g,Projectiles,stageMode);
        if (game.PLAYER != null && game.PLAYER.currentSprite != null && game.PLAYER.spawned) {
            drawSprite(g, game.PLAYER.currentSprite, true, stageMode);
        }
        drawFromConcurrentCollection(g,Enemies,stageMode);

        if (stageMode != -2) {
            drawTiles(stageMode, Foreground_Tiles, g);
        } else {
            drawTiles(stageMode, Room_Foreground_Tiles, g);
        }

        for (int i = 0; i < PlayerTiles.size(); i++) {
            RenderedSprite tile = PlayerTiles.get(i);
            if (tile != null) {
                drawSprite(g, tile, true, stageMode);
            }
        }

        // HUD ELEMENTS
        if (!game.PAUSED) {
            drawHud(g);
        }

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, (int) ((centerScreen[0] - (8.5 * size)) + (size / 2.0)), this.getHeight());
        g.fillRect((int) ((centerScreen[0] + (7.45 * size)) + (size / 2.0)), 0, (int) ((centerScreen[0] - (8 * size)) + (size / 2.0)), this.getHeight());

        if (fluid_Reverse) --fluid_Index;
        else ++fluid_Index;

        if (fluid_Index > 128) {
            fluid_Index = 128;
            fluid_Reverse = true;
        } else if (fluid_Index < 1) {
            fluid_Index = 1;
            fluid_Reverse = false;
        }
    }

    /**
     *   Draws the tiles of the specified tileSet to the screen
     *   @param stageMode: What kind of stage is currently being played in?
     *   @param tileSet: What tileSet are we drawing from?
     *   @param g: the specified Graphics context used by the GamePanel
     */
    private void drawTiles(int stageMode, RenderedSprite[][] tileSet, Graphics g) {
        if (stageMode == 0 || stageMode == 3) { // VERTICAL LEVELS
            for (int y = render_Low; y < render_High; y++) {
                for (int x = 0; x < tileSet[y].length; x++) {
                    if (tileSet[y][x] != null) {
                        RenderedSprite tile = tileSet[y][x];
                        drawSprite(g, tile, true, stageMode);
                    }
                }
            }
        } else if (stageMode == -1 || stageMode == 1) { // HORIZONTAL LEVELS
            for (int y = 0; y < tileSet.length; y++) {
                for (int x = render_Low; x < render_High; x++) {
                    if (tileSet[y][x] != null) {
                        RenderedSprite tile = tileSet[y][x];
                        drawSprite(g, tile, true, stageMode);
                    }
                }
            }
        } else if (stageMode == 2) { // TEMPLE
            // figure out how to render temple rooms and put it here
        } else if (stageMode == -2 || stageMode == -3) {
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    if (tileSet[y][x] != null) {
                        RenderedSprite tile = tileSet[y][x];
                        drawSprite(g, tile, true, stageMode);
                    }
                }
            }
        }
    }

    /**
    *   animate sprites that are denoted as fluids for dynamic appearance
    *   @param s: the sprite in question to animate.
    *   @return - if it is a fluid, return the sprite version for to swap to
    *           - if it is not, return the normal sprite
    */
    private BufferedImage animateFluid(RenderedSprite s) {
        if (s!=null && s.name!=null) {
            if (s.name.contains("LAVA") || s.name.contains("WATER") || s.name.contains("SPRING") || s.name.contains("WATER_TOP")) {
                return s.animations[(int) Math.ceil(fluid_Index / 16.0) - 1];
            } else {
                return s.sprite;
            }
        }
        return null;
    }

    /**
     * converts x,y indices into screen coordinates, used by StageManager when loading tiles
     *
     * @param x the x index of the tile
     * @param y the y index of the tile
     * @param tile the tile to have its position converted
     */
    static void toScreenPosition(int y, int x, RenderedSprite tile) {
        int extra_Y = 0;
        int extra_X = 0;
        if (tile.height > size) extra_Y = (tile.height/2)-(size/2);
        if (tile.width > size) extra_X = (tile.width/2)-(size/2);
        tile.x = (int) ((centerScreen[0] - (8 * size)) + (size / 2.0) + (x * (size)) + extra_X);
        tile.y = (int) (GAME_SCREEN_SIZE.getHeight() - ((size / 6) + (y * size)) - extra_Y);
    }

    /**
     * draws Sprites to the screen
     *
     * @param g the specified Graphics context
     * @param s the sprite to be drawn
     * @param stageElement is the element a tile of the stage or not
     * @param stageMode what is the stageMode of the current stage that is being rendered
     */
    private void drawSprite(Graphics g, RenderedSprite s, boolean stageElement, int stageMode) {
        int w = s.width;
        int h = s.height;
        int x = s.x-(w/2);
        int y = s.y-(h/2);
        if (stageMode == 0 || stageMode == 3) {
            y = (stageElement ? stage_Offset + y : y);
        }
        if (stageMode == 1 || stageMode == -1) {
            x = (stageElement ? x - stage_Offset : x);
        }
        g.drawImage(animateFluid(s), x, y, w, h, this);
    }

    /**
     * draws Hud elements to the screen
     *
     * @param g the specified Graphics context
     */
    private void drawHud(Graphics g) {
        if (!this.game.CUTSCENE) {
            int startX = (int) ((centerScreen[0] - (8 * size)) + (size / 2.0));
            int startY = (int) (GAME_SCREEN_SIZE.getHeight() - ((size / 6) + (11.5 * size)));

            int x = startX;
            int y = startY;

            double hearts_n = game.PLAYER.health / 4.0;
            int full_hearts = (int) Math.floor(hearts_n);
            hearts_n -= full_hearts;

            int three_quarter_hearts = (hearts_n == 0.75 ? 1 : 0);
            int half_hearts = (hearts_n == 0.5 ? 1 : 0);
            int quarter_hearts = (hearts_n == 0.25 ? 1 : 0);

            int drawnHearts = 0;

            int[] result = generateHearts(g, x, y, drawnHearts, startX, full_hearts, "hea_100");
            x = result[0];
            y = result[1];
            drawnHearts = result[2];
            result = generateHearts(g, x, y, drawnHearts, startX, three_quarter_hearts, "hea_75");
            x = result[0];
            y = result[1];
            drawnHearts = result[2];
            result = generateHearts(g, x, y, drawnHearts, startX, half_hearts, "hea_50");
            x = result[0];
            y = result[1];
            drawnHearts = result[2];
            generateHearts(g, x, y, drawnHearts, startX, quarter_hearts, "hea_25");

            int offset = (int) Math.round(hearts_n + full_hearts);
            if (offset > 10) {
                offset = 10;
            }

            x = startX + (offset * 32);
            y = startY;

            RenderedSprite hIcon = COLLECTIBLES.get("H25").clone();
            hIcon.x = x;
            hIcon.y = y - 13;
            x += 20;
            g.setFont(fonts.get(24));
            g.setColor(Color.white);
            g.drawString("000", x, y-3);
            drawSprite(g, hIcon, false, 0);
        } else if (displayScore) {
            int x = centerScreen[0];
            int y = centerScreen[1];
            this.game.FILE.hearts = game.PLAYER.current_Hearts;
            game.PLAYER.current_Hearts = 0;
            if (game.PLAYER.current_Score > 0) {
                game.FILE.score += 1;
                game.PLAYER.current_Score -= 1;
                g.setFont(fonts.get(24));
                g.setColor(Color.white);
                String r0 = "EARNED "+game.PLAYER.current_Score;
                String r1 = "TOTAL "+game.FILE.score;
                g.drawString(r0, x-(12*r0.length()), y);
                g.drawString(r1, x-(12*r1.length()), y+24);
            } else {
                displayScore = false;
                game.PLAYER.velocity[0] = 2;
                game.PLAYER.moving = true;
            }
        }
    }

    /**
     * proxy method to trigger displaying the score at the end of levels
     */
    public void displayScore() {
        displayScore = true;
    }

    /**
     * draw hearts representing player health to the HUD
     *
     * @param g the specified Graphics Context
     * @param x the x coordinate to begin at
     * @param y the y coordinate to begin at
     * @param drawnHearts the amount of hearts drawn by previous invocations
     * @param startX the x coordinate that the hud began drawing at
     * @param n the number of hearts to draw
     * @param type the heart type to draw; e.g., 100%, 75%, 50%, 25%
     * @return an integer array that contains the x and y that were reached & the amount of hearts drawn by the method for later invocations
     */
    private int[] generateHearts(Graphics g, int x, int y, int drawnHearts, int startX, int n, String type) {
        for (int i = 0; i < n; i++) {
            RenderedSprite h = HEARTS.get(type).clone();
            h.x = x-12;
            h.y = y-14;
            drawSprite(g, h, false, 0);
            x += 32;
            ++drawnHearts;
            if (drawnHearts > 9) {
                x = startX;
                drawnHearts = 0;
                y += 32;
            }
        }
        return new int[] {x, y, drawnHearts};
    }

    /**
     * resets all variables modified during playtime
     */

    public void reset() {
        stage_Offset = 0;
        background_Offset = 0;
        stage_Offset_LIMIT = -1;
        stage_Offset_MIN = -1;
        render_Low = 0;
        render_High = 16;
        sm = null;
        Tiles = new RenderedSprite[16][16];
        Foreground_Tiles = new RenderedSprite[16][16];
        POI_Tiles = new int[16][16];
        Room_Tiles = new RenderedSprite[16][16];
        Room_Foreground_Tiles = new RenderedSprite[16][16];
        Room_POI_Tiles = new int[16][16];
    }
}
