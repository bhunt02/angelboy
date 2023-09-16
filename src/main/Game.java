package main;

import visual.GraphicsRenderer;
import main.technical.SaveFile;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.Timer;

public class Game extends Thread implements Runnable {
    public boolean TITLE_SCREEN = true;
    public boolean FILE_SELECT = false;
    public boolean IN_GAME = false;
    public boolean PAUSED = true;
    public boolean CUTSCENE = false;
    public boolean LOADING = false;
    public boolean EXIT = false;
    public static final double GRAVITY = 0.098;
    public StageManager sm = null;
    public Player PLAYER;
    public static SaveFile[] SAVES = new SaveFile[3];
    public SaveFile FILE;
    public GraphicsRenderer RENDERER;
    public GamePanel GAME_SCREEN;
    public Game_Loop GAME_LOOP;
    public int world = 0;
    public int level = 1;
    public Game(JFrame window) {
        PLAYER = new Player(-Main.screen_Width,-Main.screen_Height,this);
        RENDERER = new GraphicsRenderer(this,window,PLAYER);
        final Thread GRAPHICS_THREAD = new Thread(RENDERER);
        GRAPHICS_THREAD.start();
    }
    public static void loadFiles(ArrayList<File> save_files) {
        try {
            for (int i = 0; i < 3; i++) {
                if (save_files.size() > i) {
                    File sv = save_files.get(i);
                    SaveFile s = new SaveFile(sv.getName().substring(0, sv.getName().indexOf("_save.txt")));
                    SAVES[i] = s;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void addGameScreen(GamePanel g) {
        this.GAME_SCREEN = g;
    }
    class Game_Loop extends TimerTask {
        Game g;
        boolean stageExit = false;
        boolean gameExit = false;
        boolean respawnPlayer = false;
        Game_Loop(Game g) {
            this.g = g;
            g.GAME_LOOP = this;
        }
        public void loadStage() {
            g.LOADING = true;
            g.GAME_SCREEN.reset();
            this.g.sm = new StageManager(world, level, this.g);
            g.GAME_SCREEN.addStage(sm);
            g.PLAYER.spawn(sm.spawnPoint[0],sm.spawnPoint[1]);
            g.PAUSED = false;
            g.LOADING = false;
        }
        public void Respawn() {
            this.respawnPlayer = false;
            g.sm.currentRoom = null;
            g.PLAYER.velocity[0] = 0;
            g.PLAYER.velocity[1] = 0;
            g.PLAYER.despawn();
            this.g.sm = null;
            this.loadStage();
        }
        public void processExit() {
            this.stageExit = false;
            int[] newWorld_Level = this.g.sm.advanceLevel(this.g.world, this.g.level + 1);
            this.g.sm = null;
            this.g.world = newWorld_Level[0];
            this.g.level = newWorld_Level[1];
            this.g.FILE.world = this.g.world;
            this.g.FILE.level = this.g.level;
            this.g.FILE.recordFile(this.g.FILE.index);
            this.loadStage();
        }
        public void ExitGame() {
            this.g.TITLE_SCREEN = true;
            this.g.IN_GAME = false;
            this.g.world = 0;
            this.g.level = 1;
            this.g.FILE = null;
            this.g.PLAYER.despawn();
            this.g.GAME_SCREEN.reset();
            this.g.sm = null;
            this.gameExit = false;
        }
        public void initiateStageExit() {
            this.stageExit = true;
        }
        public void initiateGameExit() {
            this.gameExit = true;
        }
        public void initiatePlayerRespawn() { this.respawnPlayer = true;}
        public void createNewFile(int number, String alias) {
            // CHANGE TO 1-1 LATER
            SAVES[number] = new SaveFile(alias,0,1,0,0,0,1,number);
            SAVES[number].recordFile(number);
        }
        @Override
        public void run() {
            if (IN_GAME) {
                if (this.stageExit) {
                    processExit();
                    return;
                } else if (this.respawnPlayer) {
                    Respawn();
                    return;
                } else if (this.gameExit) {
                    ExitGame();
                    return;
                }
                if (sm == null && !CUTSCENE) {
                    loadStage();
                }
                if (!PAUSED) {
                    PLAYER.update();
                } else {
                    // pause screen stuff
                }
            }
        }
    }
    @Override
    public void run() {
        Timer tick_rate = new Timer("GAME LOOP");
        tick_rate.scheduleAtFixedRate(new Game_Loop(this), 0, 17);
    }
    void keyPressed(int k) {
        if (IN_GAME) {
            if (PLAYER.acceptUserInput) {
                if (!PLAYER.dead) {
                    if (!PAUSED) {
                        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) {
                            PLAYER.faceUp(true);
                        } else if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) {
                            PLAYER.Crouch(true);
                        } else if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) {
                            PLAYER.Move(-1);
                        } else if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) {
                            PLAYER.Move(1);
                        } else if (k == KeyEvent.VK_E) {
                            PLAYER.Shoot();
                        } else if (k == KeyEvent.VK_SPACE) {
                            PLAYER.Jump();
                        } else if (k == KeyEvent.VK_ESCAPE) {
                            PAUSED = true;
                        }
                    } else {
                        if (k == KeyEvent.VK_ESCAPE && !CUTSCENE) {
                            PAUSED = false;
                        }
                    }
                } else {
                    if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) {
                        GAME_SCREEN.moveDeathPointer(-1);
                    } else if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) {
                        GAME_SCREEN.moveDeathPointer(1);
                    } else if (k == KeyEvent.VK_ENTER) {
                        if (GAME_SCREEN.death_pointer == 0) {
                            GAME_LOOP.initiatePlayerRespawn();
                        } else if (GAME_SCREEN.death_pointer == 1) {
                            GAME_LOOP.initiateGameExit();
                        }
                    }
                }
            }
        } else if (FILE_SELECT) {
            if (!GAME_SCREEN.typing) {
                if (k == KeyEvent.VK_ESCAPE) {
                    if (GAME_SCREEN.createFile || GAME_SCREEN.eraseFile) {
                        GAME_SCREEN.createFile = false;
                        GAME_SCREEN.eraseFile = false;
                        GAME_SCREEN.pointer_y = 0;
                        GAME_SCREEN.pointer_x = 0;
                    } else {
                        TITLE_SCREEN = true;
                        FILE_SELECT = false;
                    }
                } else if (k == KeyEvent.VK_ENTER) {
                    if (GAME_SCREEN.pointer_y < 3) { // SELECT FILE
                         if (SAVES[GAME_SCREEN.pointer_y] != null) {
                             if (GAME_SCREEN.eraseFile) {
                                 boolean success = SAVES[GAME_SCREEN.pointer_y].eraseFile();
                                 System.out.printf("Erased file %d successfully: %b\n", GAME_SCREEN.pointer_y + 1, success);
                                 SAVES[GAME_SCREEN.pointer_y] = null;
                                 GAME_SCREEN.eraseFile = false;
                             } else if (!GAME_SCREEN.createFile) {
                                 this.FILE = SAVES[GAME_SCREEN.pointer_y];
                                 this.PLAYER.max_health = this.FILE.power_Level*12;
                                 this.PLAYER.health = this.PLAYER.max_health;
                                 this.world = this.FILE.world;
                                 this.level = this.FILE.level;
                                 FILE_SELECT = false;
                                 IN_GAME = true;
                             } else {
                                 // play error noise
                             }
                         } else {
                             if (GAME_SCREEN.createFile) {
                                 GAME_SCREEN.typing = true;
                             }
                         }
                    } else if (GAME_SCREEN.pointer_y == 3) { // CREATE / DELETE FILE
                        if (GAME_SCREEN.pointer_x == 0) {
                            int target = -1;
                            for (int i = 0; i < 3; i++) {
                                if (SAVES[i] == null) {
                                    target = i;
                                    break;
                                }
                            }
                            if (target != -1) {
                                GAME_SCREEN.createFile = true;
                                GAME_SCREEN.pointer_y = target;
                            }
                        } else if (GAME_SCREEN.pointer_x == 1) {
                            int target = -1;
                            for (int i = 0; i < 3; i++) {
                                if (SAVES[i] != null) {
                                    target = i;
                                    break;
                                }
                            }
                            if (target != -1) {
                                GAME_SCREEN.eraseFile = true;
                                GAME_SCREEN.pointer_y = target;
                                GAME_SCREEN.pointer_x = 0;
                            }
                        }
                    }
                } else if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) {
                    GAME_SCREEN.moveFilePointer(-1, 0);
                } else if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) {
                    GAME_SCREEN.moveFilePointer(1, 0);
                } else if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) {
                    GAME_SCREEN.moveFilePointer(1, 1);
                } else if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) {
                    GAME_SCREEN.moveFilePointer(-1, 1);
                }
            } else {
                GAME_SCREEN.typeFileName(k);
            }
        } else if (TITLE_SCREEN) {
            TITLE_SCREEN = false;
            FILE_SELECT = true;
        }
    }
    void keyReleased(int k) {
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) {
            PLAYER.faceUp(false);
        } else if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) {
            PLAYER.Crouch(false);
        } else if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT || k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) {
            PLAYER.Move(0);
        } else if (k == KeyEvent.VK_SPACE) {
            if (PLAYER.jumpHeld) {
                PLAYER.jumpHeld = false;
            }
        }
    }
}
