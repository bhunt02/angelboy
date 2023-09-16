package main.technical;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class SaveFile {
    File save = null;
    public String identity = "";

    public int index;
    public int hearts = 0;
    public int power_Level = 0;
    public int score = 0;
    public int excellence = 0;
    public int world = 0;
    public int level = 1;
    public SaveFile(String identity, int hearts, int power_Level, int score, int excellence, int world, int level, int index) {
        try {
            this.save = new File("src/savefiles/" + index+identity + "_save.txt");
            boolean isNew = save.createNewFile();
            if (isNew) {
                this.index = index;
                this.identity = identity;
                this.hearts = hearts;
                this.power_Level = power_Level;
                this.score = score;
                this.excellence = excellence;
                this.world = world;
                this.level = level;
                recordFile(index);
            } else {
                Object[] data = readSave(this.save);
                if (data != null) {
                    this.index = Integer.parseInt(""+this.save.getName().charAt(0));
                    this.identity = (String) data[0];
                    this.hearts = (int) data[1];
                    this.power_Level = (int) data[2];
                    this.score = (int) data[3];
                    this.excellence = (int) data[4];
                    this.world = (int) data[5];
                    this.level = (int) data[6];
                } else {
                    this.index = index;
                    this.identity = identity;
                    this.hearts = hearts;
                    this.power_Level = power_Level;
                    this.score = score;
                    this.excellence = excellence;
                    this.world = world;
                    this.level = level;
                }
            }
        } catch (IOException ignored) {}
    }
    public SaveFile(String identity) {
        save = new File("src/savefiles/"+identity+"_save.txt");
        Object[] data = readSave(save);
        if (data != null) {
            this.index = Integer.parseInt(""+save.getName().charAt(0));
            this.identity = (String) (data[0]);
            this.hearts = (int) data[1];
            this.power_Level = (int) data[2];
            this.score = (int) data[3];
            this.excellence = (int) data[4];
            this.world = (int) data[5];
            this.level = (int) data[6];
        }
    }
    public static Object[] readSave(File file) {
        try {
            Scanner pR = new Scanner(file);
            ArrayList<Object> t = new ArrayList<>();
            while (pR.hasNextLine()) {
                String ln = pR.nextLine();
                String d = ln.substring(ln.indexOf(":")+1);
                if (ln.startsWith("IDENTITY:")) {
                    t.add(d.substring(1));
                } else {
                    t.add(Integer.parseInt(d));
                }
            }
            pR.close();
            return t.toArray();
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public Object[] compileData() {
        ArrayList<Object> object = new ArrayList<>();
        object.add(this.identity);
        object.add(this.hearts);
        object.add(this.power_Level);
        object.add(this.score);
        object.add(this.excellence);
        object.add(this.world);
        object.add(this.level);
        return object.toArray();
    }
    public void recordFile(int index) {
        Object[] data = compileData();
        try {
            try (PrintWriter pS = new PrintWriter(save)) {
                for (int i = 0; i < data.length; i++) {
                    String prefix = switch (i) {
                        case 1 -> "HEARTS:";
                        case 2 -> "POWER:";
                        case 3 -> "SCORE:";
                        case 4 -> "EXCELLENCE:";
                        case 5 -> "WORLD:";
                        case 6 -> "LEVEL:";
                        default -> "IDENTITY:"+index;
                    };
                    pS.println(prefix + data[i]);
                }
            }
        } catch (FileNotFoundException ignored) {}
    }
    public boolean eraseFile() {
        return this.save.delete();
    }
}
