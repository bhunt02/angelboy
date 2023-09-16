package visual;
import java.awt.*;
import java.io.*;
import java.util.Hashtable;
public class Sprite extends Component implements Cloneable {
    // LIST OF SPRITES
    public static Hashtable<String,Sprite> List = new Hashtable<>();
    // SPRITE INFORMATION
    public String address;
    public String name;

    // DRAWING RULES
    public int width;
    public int height;
    public int drawingOrder = 0; // -1 = BACKGROUND, 0 = MID-GROUND, 1 = FOREGROUND
    public Sprite(int custom) {}
    public Sprite(String name) {}
    public Sprite(String address, String name) throws IOException {
        this.address = address;
        this.name = name;
        List.put(this.name,this);
    }

    @Override
    public Sprite clone() {
        try {
            return (Sprite) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}

