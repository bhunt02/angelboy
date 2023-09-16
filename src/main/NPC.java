package main;

import main.objects.Collidable;
import visual.RenderedSprite;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import static main.GamePanel.centerScreen;
import static main.StageManager.*;

public class NPC extends Avatar implements Cloneable {
    // NPC STATS
    public String name = "";
    public int health = 10;

    public int strength = 1;

    // NPC CONDITIONALS
    boolean canFaceUp;
    boolean canCrouch;

    // STATIC VARIABLES
    public static final double scale = 4.0;

    // ANIMATION
    public static Hashtable<String, RenderedSprite> NPC_SPRITES;
    static void loadSprites(ArrayList<String> l) throws IOException {
        NPC_SPRITES = new Hashtable<>();
        for (String n : l) {
            RenderedSprite RS = new RenderedSprite(n, false, 0, 0, scale, 0);
            RenderedSprite RS_M = new RenderedSprite(n, true, 0, 0, scale, 0);
            NPC_SPRITES.put(n, RS);
            NPC_SPRITES.put(n + "_M", RS_M);
        }
    }

    // ANIMATION CYCLES
    boolean reverse = false;
    int runCycle = 8;
    int climbCycle = 1;
    int flyCycle = 1;

    // CONSTRUCTORS
    NPC(int x, int y, Game game, String name) {
        super(x,y,game);
        this.name = name;
        this.height = NPC_SPRITES.get(this.name+"_IDLE").sprite.getHeight();
        this.width = NPC_SPRITES.get(this.name+"_IDLE").sprite.getWidth();
    }

    // NPC FUNCTIONALITY
    void Jump() {
        if (this.canJump && !this.crouching && !this.facingUp) {
            this.jumping = true;
            this.canJump = false;
            if (this.velocity[1] >= 0) {
                this.velocity[1] = -(this.jump_Strength);
            }
        }
    }
    void Move(int dir) {
        this.moving = true;
        this.velocity[0] = (dir * 2);
    }

    void Crouch(boolean b) {
        if (this.canCrouch) {
            if (!this.jumping && !this.falling && !this.flying && !this.facingUp) {
                this.crouching = true;
            } else this.crouching = false;
        }
    }
    void faceUp(boolean b) {
        if (this.canFaceUp) {
            if (!this.jumping && !this.falling && !this.flying && !this.crouching) {
                this.facingUp = b;
            } else this.facingUp = false;
        }
    }
    @Override
    public void update() {
        if (this.GAME.sm != null) {
            this.x += this.velocity[0];
            this.y += this.velocity[1];
            if (!this.moving) {
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
                Collisions();
            }

            this.animate();
            this.currentSprite.x = (int) this.x;
            this.currentSprite.y = (int) this.y;
            lastX = this.x;
            lastY = this.y;
        }
    }
    static ArrayList<String> JumpThrough = new ArrayList<>();

    static {
        JumpThrough.add("PLATFORM");
    }

    public RenderedSprite floor = null;
    public RenderedSprite roof = null;
    public RenderedSprite left = null;
    public RenderedSprite right = null;
    public RenderedSprite lower_left = null;
    public RenderedSprite lower_right = null;
    public RenderedSprite upper_left = null;
    public RenderedSprite upper_right = null;

    public void Collisions() {

        boolean inRoom = (GAME.sm.currentRoom != null);

        RenderedSprite[][] source0 = (!inRoom ? GamePanel.Tiles : GamePanel.Room_Tiles);

        if (GAME.sm.currentStage.stageMode == 0 || GAME.sm.currentStage.stageMode == 3) {
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
        floor = getTile(critPoints[1], critPoints[0], source0);
        roof = getTile(critPoints[1] + 2, critPoints[0], source0);
        left = getTile(critPoints[1] + 1, critPoints[0] - 1, source0);
        right = getTile(critPoints[1] + 1, critPoints[0] + 1, source0);
        lower_left = getTile(critPoints[1], critPoints[0] - 1, source0);
        lower_right = getTile(critPoints[1], critPoints[0] + 1, source0);
        upper_left = getTile(critPoints[1] + 2, critPoints[0] - 1, source0);
        upper_right = getTile(critPoints[1] + 2, critPoints[0] + 1, source0);

        boolean midair = false;
        int p_Y = (int) this.y;

        if (roof != null) {
            if (!JumpThrough.contains(roof.name) && !this.crouching) {
                if ((roof.y + (size / 2) + (this.height / 2)) > p_Y) {
                    this.velocity[1] = 0;
                    this.y = (roof.y + (size / 2.0) + (this.height / 2.0));
                }
            }
        }

        if (floor != null) {
            if ((floor.y - (this.height / 2) - (size / 2)) < p_Y) {
                if (!JumpThrough.contains(floor.name)) {
                    this.velocity[1] = 0;
                    this.y = (floor.y - (this.height / 2.0) - (size / 2.0));
                } else {
                    if (!this.jumping && !this.crouching) {
                        this.velocity[1] = 0;
                        this.y = (floor.y - (this.height / 2.0) - (size / 2.0));
                    } else {
                        midair = true;
                    }
                }
            }
        } else {
            midair = true;
        }

        if (left != null && this.x < (left.x + left.width)) {
            if (!JumpThrough.contains(left.name)) {
                this.x = (left.x + left.width);
            }
        }

        if (upper_left != null && ((upper_left.y + (this.height / 2)) > p_Y)) {
            if (this.x < (upper_left.x + upper_left.width)) {
                if (!JumpThrough.contains(upper_left.name) && !this.crouching) {
                    this.x = (upper_left.x + upper_left.width);
                }
            }
        }

        if (lower_left != null) {
            if (!JumpThrough.contains(lower_left.name)) {
                if (this.x < (lower_left.x + lower_left.width) && this.x > (lower_left.x)) {
                    int p_Y_lower = (int) (this.y + (this.height / 2));
                    if ((lower_left.y + lower_left.height) > p_Y_lower && (lower_left.y < p_Y_lower)) {
                        if (this.velocity[0] < 0) {
                            this.velocity[0] = 0;
                        }
                        this.x = (lower_left.x + lower_left.width);
                    }
                }
            }
        }

        if (right != null && this.x > (right.x - right.width)) {
            if (!JumpThrough.contains(right.name)) {
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
                        if (this.velocity[0] > 0) {
                            this.velocity[0] = 0;
                        }
                        this.x = (lower_right.x - lower_right.width);
                    }
                }
            }
        }

        if (midair) {
            if (!this.flying) {
                this.velocity[1] += (Game.GRAVITY);
            }
        }
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
        } else {
            this.flying = false;
            this.falling = false;
        }

        String spriteName = "IDLE";

        if (this.jumping) {
            spriteName = "JUMP_1";
        } else if (this.falling) {
            if (this.flying) {
                spriteName = "JUMP_" + (int) (Math.ceil(flyCycle / 2.0));
            } else spriteName = "JUMP_5";
        } else if (this.moving) {
            spriteName = "WALK_" + (int) (Math.ceil(runCycle / 8.0));
        }

        if (direction_Facing == 1) {
            spriteName = spriteName + "_M";
        }

        if (!jumping && !falling) {
            if (this.crouching) {
                spriteName = this.name+"DUCK";
            } else if (this.facingUp) {
                spriteName = this.name+"UP_S";
            }
        }

        NPC_SPRITES.forEach((k, s) -> {
            s.x = (int) this.x;
            s.y = (int) this.y;
        });

        spriteName = this.name+"_"+spriteName;

        this.lastSprite = this.currentSprite;
        this.currentSprite = NPC_SPRITES.get(spriteName);

        if (this.moving) {
            ++this.runCycle;
            if (this.runCycle > 56) this.runCycle = 8;
        } else this.runCycle = 3;

        if (this.flying) {
            if (this.reverse) {
                --this.flyCycle;
            } else {
                ++flyCycle;
            }
            if (flyCycle > 10) {
                reverse = true;
                flyCycle = 10;
            } else if (flyCycle < 2) {
                reverse = false;
                flyCycle = 2;
            }
        } else {
            this.reverse = false;
            this.flyCycle = 1;
        }
    }

    @Override
    public NPC clone() {
        try {
            return (NPC) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void Touched(Collidable c) {
        if (c.name.contains("PLAYER")) {
            ((Player) c).damage(this.strength);
        }
    }
    public void damage(int strength) {
        this.health -= strength;
    }
}
