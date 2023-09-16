package main;

import main.objects.Arrow;
import visual.RenderedSprite;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import static main.GamePanel.centerScreen;
import static main.GamePanel.stage_Offset;
import static main.StageManager.*;
import static main.technical.Edge.*;

public class Player extends Avatar {
    // PLAYER STATS
    public int max_health = 12;
    public int health = max_health;

    public int current_Hearts;
    public int current_Score = 0;

    // PLAYER CONDITIONALS
    private boolean canJump = false;
    private boolean canMove = false;
    public boolean moving;
    private boolean shooting;
    private boolean falling;
    private boolean jumping;
    private boolean flying;
    private boolean fluttering;
    private boolean climbing;
    private boolean crouching;
    private boolean squished;
    private boolean crouchDebris;
    private boolean facingUp;
    private boolean inCutscene = true;
    private boolean reachedMidpoint = false;
    public boolean spawned = false;
    // STATIC VARIABLES
    private static final double scale = 4.0;

    // ANIMATION
    public static Hashtable<String, RenderedSprite> PLAYER_SPRITES;
    static void loadSprites(ArrayList<String> l) throws IOException {
        PLAYER_SPRITES = new Hashtable<>();
        for (String name : l) {
            RenderedSprite RS = new RenderedSprite(name, false, 0, 0, scale, 0);
            RenderedSprite RS_M = new RenderedSprite(name, true, 0, 0, scale, 0);
            PLAYER_SPRITES.put(name, RS);
            PLAYER_SPRITES.put(name + "_M", RS_M);
        }
    }

    // ANIMATION CYCLES
    boolean reverse = false;
    int runCycle = 8;
    int climbCycle = 1;
    int flutterCycle = 1;

    // CONSTRUCTORS
    Player(int x, int y, Game game) {
        super(x,y,game);
        this.lastSprite = PLAYER_SPRITES.get("IDLE");
        this.currentSprite = this.lastSprite;
        this.height = this.currentSprite.sprite.getHeight();
        this.width = this.currentSprite.sprite.getWidth();
    }

    // PLAYER FUNCTIONALITY
    private final double flightSpeed = 0.25;
    private final double runSpeed = 2.0;
    private final double jumpStrength = 7;

    private int reload_Time = 60;
    private int reload = 0;
    public boolean acceptUserInput = true;
    public boolean jumpHeld = false;
    public boolean upHeld = false;
    public boolean downHeld = false;
    public boolean leftHeld = false;
    public boolean rightHeld = false;
    void Jump() {
        if (this.flying || this.inCutscene) return;

        if (this.canJump && !this.crouching && !this.facingUp) {
            this.jumping = true;
            this.jumpHeld = true;
            this.canJump = false;
            if (this.velocity[1] >= 0) {
                this.velocity[1] = -(jumpStrength);
            }
        }
    }

    void Move(int dir) {
        if (this.inCutscene) return;
        if (flying) {
            if (dir == -1) {
                leftHeld = true;
                rightHeld = false;
            } else if (dir == 1) {
                rightHeld = true;
                leftHeld = false;
            } else {
                rightHeld = false;
                leftHeld = false;
            }
            return;
        }
        if (this.canMove) {
            this.moving = true;
            this.velocity[0] = (dir * runSpeed);
        }
    }

    void Crouch(boolean b) {
        if (this.inCutscene) return;

        this.downHeld = b;
        if (this.flying) return;
        if (!this.jumping && !this.falling && !this.fluttering && !this.facingUp) {
            if (!squished) {
                this.crouching = b;
                this.crouchDebris = false;
            } else if (b) {
                this.crouching = true;
                this.crouchDebris = false;
            } else {
                this.crouchDebris = true;
            }
        } else this.crouching = false;
    }

    void faceUp(boolean b) {
        if (this.inCutscene) return;
        this.upHeld = b;
        if (this.flying) return;
        if (!this.jumping && !this.falling && !this.fluttering && !this.crouching) {
            this.facingUp = b;
        } else this.facingUp = false;
    }

    void Shoot() {
        if (this.inCutscene || this.crouching || reload > 0) return;

        reload += reload_Time;
        this.shooting = true;

        try {
            Arrow a = null;
            if (this.facingUp && roof == null) {
                a = new Arrow((int) this.x, (int) this.y-64,"ARROW_UP",false,this.GAME);
                a.velocity[1] = -10;
            } else if ((direction_Facing == -1 && left == null) || (direction_Facing == 1 && right == null)) {
                boolean m = false;
                if (direction_Facing < 0) m = true;
                a = new Arrow((int) this.x+(direction_Facing*64), (int) this.y,"ARROW",m,this.GAME);
                a.velocity[0] = (direction_Facing*10);
            }
            if (a!=null) {
                a.update();
                GamePanel.Projectiles.add(a);
            }
        } catch(IOException ignored) {}
    }

    // PLAYER UPDATES
    double lastX = this.x;
    double lastY = this.y;
    @Override
    public void update() {
        if (this.GAME.sm != null) {
            if (reload > 0) --reload;
            if (this.health <= 0) {
                this.dead = true;
                return;
            }
            this.x += this.velocity[0];
            this.y += this.velocity[1];
            if (!this.moving && !this.inCutscene && !this.flying) {
                int friction = 2; // LOW = MORE, HIGH = LESS
                if (this.floor != null) {
                    if (this.floor.name.toUpperCase().contains("ICE")) {
                        friction = 4;
                    }
                }
                if (this.velocity[0] < 0) {
                    this.velocity[0] += (this.velocity[0] / friction);
                } else if (this.velocity[0] > 0) {
                    this.velocity[0] -= (this.velocity[0] / friction);
                }

                if (Math.abs(this.velocity[0]) < 1) {
                    this.velocity[0] = 0;
                }
            }

            if (this.x != this.lastX || this.y != this.lastY || this.currentSprite != this.lastSprite) {
                if (this.spawned) {
                    Collisions();
                }
            }

            this.animate();

            lastX = this.x;
            lastY = this.y;
        }
    }
    static ArrayList<String> JumpThrough = new ArrayList<>();

    static {
        JumpThrough.add("PLATFORM");
        JumpThrough.add("CLOUD");
        JumpThrough.add("CLOUD_CORNER");
        JumpThrough.add("STUD_PLATFORM");
    }

    private RenderedSprite floor = null;
    private RenderedSprite roof = null;
    private RenderedSprite left = null;
    private RenderedSprite right = null;
    private RenderedSprite lower_left = null;
    private RenderedSprite lower_right = null;
    private RenderedSprite upper_left = null;
    private RenderedSprite upper_right = null;
    private boolean fellThrough = false;
    private RenderedSprite floorDebris = null;
    private void Collisions() {

        boolean inRoom = (GAME.sm.currentRoom != null);

        if (GAME.sm.roomEntrance != null & inRoom) {
            int[] coords = toPlayerCoords(this.GAME.sm.roomEntrance[0], this.GAME.sm.roomEntrance[1]);
            int dir;
            if (!GAME.sm.currentRoom.identity.contains("EXITROOM")) {
                this.direction_Facing = -1;
                dir = -1;
                this.velocity[0] = 0;
            } else {
                this.direction_Facing = 1;
                dir = 1;
            }
            this.x = coords[0]+(dir*(this.width/8.0));
            this.y = coords[1];
            this.canJump = true;
            this.canMove = true;
            GAME.sm.roomEntrance = null;
        }

        if (respawnCoords != null && !inRoom) {
            this.x = respawnCoords[0];
            this.y = respawnCoords[1];
            this.canJump = true;
            this.canMove = true;
            respawnCoords = null;
        }

        RenderedSprite[][] source0 = (!inRoom ? GamePanel.Tiles : GamePanel.Room_Tiles);
        int[][] source1 = (!inRoom ? GamePanel.POI_Tiles : GamePanel.Room_POI_Tiles);

        if (GAME.sm.currentStage.stageMode == 0 || GAME.sm.currentStage.stageMode == 3) {
            double left_Edge = (centerScreen[0] - (8 * size)) + (size / 2.0) - (0.1 * size);
            double right_Edge = (centerScreen[0] - (8 * size)) + (size / 2.0) + (15 * size) + (0.1 * size);
            double bottom_Edge = (GAME.GAME_SCREEN.getHeight()-stage_Offset+(size));

            if (this.y > bottom_Edge && !inRoom) {
                this.health = 0;
            }

            if (this.x > right_Edge) {
                double[] tileCoords = toTileCoords(((centerScreen[0] - (8 * size)) + (size / 2.0)), this.y);
                conformToStage(left_Edge, right_Edge, tileCoords,0);
            } else if (this.x < left_Edge) {
                double[] tileCoords = toTileCoords(((centerScreen[0] - (8 * size)) + (size / 2.0) + (15 * size)), this.y);
                conformToStage(right_Edge, left_Edge, tileCoords,0);
            }
        } else if (GAME.sm.currentStage.stageMode == -1 || GAME.sm.currentStage.stageMode == 1) {

            double bottom_Edge = (GAME.GAME_SCREEN.getHeight());
            double top_Edge = (0);

            if (!inRoom) {
                double edge = (centerScreen[0] - (8 * size)) + (size / 2.0) + (GamePanel.stage_Offset);
                if (this.x < edge) {
                    conformToStage(edge, edge, null, 1);
                }
            }

            if (this.y > bottom_Edge) {
                if (GAME.sm.currentStage.stageMode == 1) {
                    this.health = 0;
                }
                conformToStage(bottom_Edge,bottom_Edge,null,2);
            } else if (this.y < top_Edge) {
                conformToStage(top_Edge,top_Edge,null,2);
            }
        }

        int[] critPoints = getCriticalPoints();
        floor = getTile(critPoints[1], critPoints[0], source0);
        roof = getTile(critPoints[1] + 2, critPoints[0], source0);
        left = getTile(critPoints[1] + 1, critPoints[0] - 1, source0);
        right = getTile(critPoints[1] + 1, critPoints[0] + 1, source0);
        lower_left = getTile(critPoints[1], critPoints[0] - 1, source0);
        lower_right = getTile(critPoints[1], critPoints[0] + 1, source0);
        upper_left = getTile(critPoints[1] + 2, critPoints[0] - 1, source0);
        upper_right = getTile(critPoints[1] + 2, critPoints[0] + 1, source0);

        int door_Left = -1;
        int door_Right = -1;
        int poi_Direct;
        if (this.velocity[1] == 0) {
            door_Left = getPOITile(critPoints[1] + 1, critPoints[0] - 1, source1);
            door_Right = getPOITile(critPoints[1] + 1, critPoints[0] + 1, source1);
        }
        poi_Direct = getPOITile(critPoints[1] + 1, critPoints[0], source1);

        //debugPoints(critPoints, 1);

        boolean midair = false;
        int p_Y = (int) this.y;

        if (poi_Direct > 1) {
            playerDoorInteraction(poi_Direct,critPoints[1]+1,critPoints[0],inRoom);
        }

        squished = false;
        if (floor != null && roof != null && (Math.abs(roof.y - floor.y) <= 150 && !JumpThrough.contains(roof.name) && !JumpThrough.contains(floor.name))) {
            this.squished = true;
        } else {
            if (this.crouchDebris) {
                this.crouching = false;
            }
        }

        if (roof != null) {
            if (!JumpThrough.contains(roof.name) && !this.crouching) {
                if (getEdge(this.currentSprite,UP) < getEdge(roof,DOWN)) {
                    this.velocity[1] = 0;
                    if (this.flying) {
                        this.velocity[1] = 2;
                    }
                    this.y = getEdge(roof,DOWN) + (1+this.height/2.0);
                }
            }
        }

        if (floor != null) {
            if (getEdge(this.currentSprite,DOWN) > getEdge(floor,UP)) {
                if (!JumpThrough.contains(floor.name)) {
                    this.velocity[1] = 0;
                    if (this.flying) {
                        this.velocity[1] = -2;
                    }
                    this.y = 1 + getEdge(floor,UP) - (this.height/2.0);
                } else {
                    if (!this.jumping && floor != floorDebris && (getEdge(this.currentSprite,DOWN) < getEdge(floor,DOWN)-(size/2.0))) {
                        this.velocity[1] = 0;
                        this.y = 1 + getEdge(floor,UP) - (this.height/2.0);
                    } else {
                        midair = true;
                    }
                }
            } else {
                if (JumpThrough.contains(floor.name)) {
                    if (this.crouching) {
                        fellThrough = true;
                        floorDebris = floor;
                        midair = true;
                    } else if (fellThrough) {
                        if (floor == floorDebris) {
                            midair = true;
                        } else {
                            fellThrough = false;
                            floorDebris = null;
                        }
                    }
                }
            }
        } else {
            midair = true;
            fellThrough = false;
            floorDebris = null;
        }

        if (left != null) {
            if (getEdge(this.currentSprite,LEFT) < getEdge(left,RIGHT)) {
                if (door_Left > 0) {
                    playerDoorInteraction(door_Left, critPoints[1] + 1, critPoints[0] - 1, inRoom);
                }
                if (!JumpThrough.contains(left.name)) {
                    if (this.flying) {
                        this.velocity[0] = 3;
                    }
                    this.x = 1 + getEdge(left,RIGHT) + (this.width/2.0);
                }
            }
        }

        /*if (upper_left != null) {
            //  && ((upper_left.y + (this.height / 2)) > p_Y)
            if (getEdge(this.currentSprite,UP) < getEdge(upper_left,DOWN)
                && getEdge(this.currentSprite,LEFT) < getEdge(upper_left,RIGHT)
               )
            {
                if (!JumpThrough.contains(upper_left.name) && !this.crouching) {
                    this.x = 1 + getEdge(upper_left,RIGHT) + (this.width/2.0);
                }
            }
        }*/

        if (lower_left != null) {
            if (!JumpThrough.contains(lower_left.name)) {
                //if (this.x < (lower_left.x + lower_left.width) && this.x > (lower_left.x)) {
                if (getEdge(this.currentSprite,LEFT) < getEdge(lower_left,RIGHT)
                    && getEdge(this.currentSprite,RIGHT) > getEdge(lower_left,LEFT)
                    && getEdge(this.currentSprite,DOWN) < getEdge(lower_left,UP)
                    && getEdge(this.currentSprite,UP) > getEdge(lower_left,DOWN)
                   )
                {
                    this.x = 1 + getEdge(lower_left,RIGHT) + (this.width/2.0);
                }
            }
        }

        if (right != null && this.x > (right.x - right.width)) {
            if (door_Right > 0) {
                playerDoorInteraction(door_Right, critPoints[1] + 1, critPoints[0] + 1, inRoom);
            }
            if (!JumpThrough.contains(right.name)) {
                if (this.flying) {
                    this.velocity[0] = -3;
                }
                this.x = (right.x - right.width);
            }
        }

        if (upper_right != null && ((upper_right.y + (this.height / 2)) > p_Y)) {
            if (this.x > (upper_right.x - upper_right.width)) {
                if (!JumpThrough.contains(upper_right.name) && !this.crouching) {
                    this.x = (upper_right.x - upper_right.width);
                }
            }
        }

        if (lower_right != null) {
            if (!JumpThrough.contains(lower_right.name)) {
                if (this.x > (lower_right.x - lower_right.width) && this.x < (lower_right.x)) {
                    int p_Y_lower = (int) (this.y + (this.height / 2));
                    if ((lower_right.y + lower_right.height) > p_Y_lower && (lower_right.y < p_Y_lower)) {
                        this.x = (lower_right.x - lower_right.width);
                    }
                }
            }
        }

        if (this.flying) {
            if (this.downHeld) {
                this.velocity[1] += flightSpeed;
                if (this.velocity[1] > 4) {
                    this.velocity[1] = 4;
                }
            } else if (this.upHeld) {
                this.velocity[1] -= flightSpeed;
                if (this.velocity[1] < -4) {
                    this.velocity[1] = -4;
                }
            }
            if (this.rightHeld) {
                this.velocity[0] += flightSpeed;
                if (this.velocity[0] > 3) {
                    this.velocity[0] = 3;
                }
            } else if (this.leftHeld) {
                this.velocity[0] -= flightSpeed;
                if (this.velocity[0] < -3) {
                    this.velocity[0] = -3;
                }
            }
        }

        if (midair) {
            double div = 1;
            if (this.fluttering) div = 10;
            if (this.flying) div = 100;
            this.velocity[1] += (Game.GRAVITY / div);
            this.canJump = false;
        } else {
            this.canJump = true;
            this.fluttering = false;
            this.jumpHeld = false;
        }
    }

    int[] respawnCoords = null;

    private void playerDoorInteraction(int doorType, int y, int x, boolean inRoom) {
        if (doorType > 2) {
            if (this.inCutscene) {
                if (doorType == 4 && !reachedMidpoint) {
                    reachedMidpoint = true;
                    this.velocity[0] = 0;
                    this.moving = false;
                    this.GAME.GAME_SCREEN.displayScore();
                }
                return;
            }
            if (this.GAME.sm.currentStage.stageMode == -1 || this.GAME.sm.currentStage.stageMode == 3) {
                if (doorType == 3) {
                    this.flying = true;
                } else if (doorType == 9) {
                    this.flying = false;
                }
            } else if (this.GAME.sm.currentStage.stageMode != 2) {
                if (respawnCoords == null) {
                    respawnCoords = new int[]{(int) this.x, (int) this.y};
                }

                int[][] poi_Source = (inRoom ? GamePanel.Room_POI_Tiles : GamePanel.POI_Tiles);
                poi_Source[y-1][x] = -1;
                poi_Source[y][x] = -1;
                this.canMove = false;
                this.canJump = false;
                this.GAME.sm.loadRoom(rooms[doorType - 3]);
                this.velocity[0] = 0;

                try {
                    if (!inRoom) {
                        Color tint = null;
                        if (this.GAME.sm.currentStage.colorRules.containsKey("DOOR_CLOSED")) {
                            tint = this.GAME.sm.currentStage.colorRules.get("DOOR_CLOSED");
                        }
                        RenderedSprite tile = (tint == null ? new RenderedSprite("DOOR_CLOSED", false, 0, 0, StageManager.scale, 0) : new RenderedSprite("DOOR_CLOSED", false, 0, 0, StageManager.scale, 0, tint));
                        GamePanel.toScreenPosition(y, x, tile);
                        GamePanel.Foreground_Tiles[y][x] = tile;
                    }
                } catch (Exception ignored) {}
            } else {
                // temple stuff
            }
        } else if (doorType == 1) {
            if (inRoom) {
                if (this.inCutscene) {
                    if (spawned) {
                        despawn();
                        this.GAME.GAME_LOOP.initiateStageExit();
                    }
                } else {
                    this.GAME.sm.currentRoom = null;
                }
            } else {
                exitStage();
            }
        }
    }
    @Override
    protected void spawn(int x, int y) {
        this.canJump = true;
        this.canMove = true;
        this.inCutscene = false;
        this.GAME.CUTSCENE = false;
        this.current_Hearts = this.GAME.FILE.hearts;
        int[] converted = toPlayerCoords(x,y);
        this.x = converted[0];
        this.y = converted[1];
        this.health = this.max_health;
        this.dead = false;
        this.spawned = true;
    }
    protected void despawn() {
        this.x = -9999;
        this.y = -9999;
        respawnCoords = null;
        reachedMidpoint = false;
        this.spawned = false;
        this.velocity[0] = 0;
        this.velocity[1] = 0;
        this.health = this.max_health;
        this.dead = false;
        this.GAME.PAUSED = true;
    }
    private void exitStage() {
        this.canMove = false;
        this.canJump = false;
        int w = Character.getNumericValue(this.GAME.sm.currentStage.identity.charAt(0));
        //this.GAME.sm.loadRoom(this.GAME.sm.currentStage.identity+"-EXITROOM");
        this.GAME.sm.loadRoom(w+"-EXITROOM");
        this.inCutscene = true;
        this.GAME.CUTSCENE = true;
        this.velocity[0] = 2;
        this.moving = true;
    }
    private static void debugPoints(int[] critPoints, int roofOffset) {
        GamePanel.PlayerTiles.clear();

        Color[] colors = new Color[]{new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255), new Color(255, 255, 0), new Color(0, 255, 255), new Color(255, 0, 255), new Color(255, 128, 0), new Color(128, 0, 255)};

        for (int i = 0; i < 8; i++) {
            int[] pos = new int[]{critPoints[0], critPoints[1]};
            switch (i) {
                case 1 ->
                    pos[1] = critPoints[1] + roofOffset;
                case 2 -> {
                    pos[1] = critPoints[1] + 1;
                    pos[0] = critPoints[0] - 1;
                }
                case 3 -> {
                    pos[1] = critPoints[1] + 1;
                    pos[0] = critPoints[0] + 1;
                }
                case 4 ->
                    pos[0] = critPoints[0] - 1;
                case 5 ->
                    pos[0] = critPoints[0] + 1;
                case 6 -> {
                    pos[1] = critPoints[1] + 2;
                    pos[0] = critPoints[0] - 1;
                }
                case 7 -> {
                    pos[1] = critPoints[1] + 2;
                    pos[0] = critPoints[0] + 1;
                }
            }
            try {
                RenderedSprite ps = new RenderedSprite("STONE", false, 0, 0, 1, 1, colors[i]);
                GamePanel.toScreenPosition(pos[1], pos[0], ps);
                GamePanel.PlayerTiles.add(ps);
            } catch (IOException e) {
                return;
            }
        }
    }
    public int[] getCriticalPoints() {
        double[] playerCoords = toTileCoords(this.x, this.y);
        return new int[]{(int) Math.round(playerCoords[0]), (int) Math.floor(playerCoords[1]) // torso level
        };
    }
    public void animate() {

        if (this.velocity[0] < 0) {
            this.direction_Facing = -1;
        } else if (this.velocity[0] > 0) {
            this.direction_Facing = 1;
        } else {
            this.moving = false;
        }

        if (this.velocity[1] < 0) {
            this.falling = false;
        } else if (this.velocity[1] > 0) {
            this.jumping = false;
            this.falling = true;
            this.fluttering = this.jumpHeld;
        } else {
            this.fluttering = false;
            this.falling = false;
        }

        String spriteName = "IDLE";

        if (this.jumping) {
            spriteName = "JUMP_1";
        } else if (this.falling) {
            if (this.fluttering) {
                spriteName = "JUMP_" + (int) (Math.ceil(flutterCycle / 2.0));
            } else spriteName = "JUMP_5";
        } else if (this.moving) {
            spriteName = "WALK_" + (int) (Math.ceil(runCycle / 8.0));
        }

        if (this.flying) {
            if (Math.abs(this.velocity[1]) > 0) {
                spriteName = "JUMP_" + (int) (Math.ceil(flutterCycle / 2.0));
            } else {
                spriteName = "JUMP_5";
            }
        }

        if (this.shooting) {
            spriteName = spriteName + "_S";
            if (this.reload < this.reload_Time/2) {
                this.shooting = false;
            }
        } else if (this.reload > 0) {
           // spriteName = spriteName + "_R";
        }

        if (direction_Facing == 1) {
            spriteName = spriteName + "_M";
        }

        if (!jumping && !falling) {
            if (this.crouching) {
                spriteName = "DUCK";
            } else if (this.facingUp) {
                spriteName = "UP_S";
            }
        }

        PLAYER_SPRITES.forEach((k, s) -> {
            s.x = (int) this.x;
            s.y = (int) this.y;
        });
        this.lastSprite = this.currentSprite;
        this.currentSprite = PLAYER_SPRITES.get(spriteName);

        if (this.moving) {
            ++this.runCycle;
            if (this.runCycle > 56) this.runCycle = 8;
        } else this.runCycle = 3;

        if (this.climbing) {
            ++this.climbCycle;
            if (this.climbCycle > 2) this.climbCycle = 1;
        } else this.climbCycle = 1;

        if (this.fluttering || this.flying) {
            if (this.reverse) {
                --this.flutterCycle;
            } else {
                ++flutterCycle;
            }
            if (flutterCycle > 10) {
                reverse = true;
                flutterCycle = 10;
            } else if (flutterCycle < 2) {
                reverse = false;
                flutterCycle = 2;
            }
        } else {
            this.reverse = false;
            this.flutterCycle = 1;
        }
    }
    public void damage(int strength) {
        this.health -= strength;
    }
}
