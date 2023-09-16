package visual;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.awt.image.*;
import java.util.Objects;

public class RenderedSprite extends Sprite implements Comparable<RenderedSprite>, Cloneable {
    // SCREEN POSITION
    public int x;
    public int y;

    // SPRITE
    public BufferedImage sprite;
    public BufferedImage[] animations;
    public int drawingOrder;
    public Color tint = new Color(255,255,255,0);
    public double scaleWeight = 1.0;
    public void tint() {
        int w = this.sprite.getWidth();
        int h = this.sprite.getHeight();
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Color pixelColor = new Color(this.sprite.getRGB(x, y), true);
                int r = (pixelColor.getRed() + this.tint.getRed()) / 2;
                int g = (pixelColor.getGreen() + this.tint.getGreen()) / 2;
                int b = (pixelColor.getBlue() + this.tint.getBlue()) / 2;
                int a = pixelColor.getAlpha();
                int rgba = (a << 24) | (r << 16) | (g << 8) | b;
                sprite.setRGB(x, y, rgba);
            }
        }
    }
    public void scale(double scale) {
        this.scaleWeight = scale;
        int w = (int) (this.width*scale);
        int h = (int) (this.height*scale);
        double x_Ratio = (w * 1.0)/this.width;
        double y_Ratio = (h * 1.0)/this.height;
        AffineTransform at = AffineTransform.getScaleInstance(x_Ratio, y_Ratio);
        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage newSprite = new BufferedImage(w,h, BufferedImage.TYPE_INT_ARGB);
        ato.filter(this.sprite, newSprite);
        this.sprite = newSprite;
        this.width = w;
        this.height = h;
    }
    public void generateMirrored() {
        BufferedImage mir = new BufferedImage(this.width,this.height,BufferedImage.TYPE_INT_ARGB);
        for(int y = 0; y < this.height; y++){
            for(int lx = 0, rx = this.width - 1; lx < this.width; lx++, rx--){
                int l = this.sprite.getRGB(lx, y);
                int r = this.sprite.getRGB(rx, y);
                mir.setRGB(lx, y, r);
                mir.setRGB(rx, y, l);
            }
        }
        this.sprite = mir;
    }
    public RenderedSprite(String name, boolean mirrored, int x, int y, double s, int o, Color c) throws IOException {
        super(name);
        if (List.containsKey(name)) {
            Sprite parent = List.get(name);
            this.address = parent.address;
            this.name = parent.name;
            this.sprite = ImageIO.read(new File(this.address));
            this.width = this.sprite.getWidth();
            this.height = this.sprite.getHeight();
            this.drawingOrder = o;
            this.x = x;
            this.y = y;
            this.tint = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0);
            if (mirrored) {
                this.generateMirrored();
            }
            this.scale(s);
            this.tint();
        }
    }
    public RenderedSprite(String name, boolean mirrored, int x, int y, double s, int o) throws IOException {
        super(name);
        if (List.containsKey(name)) {
            Sprite parent = List.get(name);
            this.address = parent.address;
            this.name = parent.name;
            if (name.equals("LAVA") || name.equals("WATER") || name.equals("WATER_TOP") || name.equals("SPRING")) {
                animations = new BufferedImage[8];
                for (int i = 1; i <= 8; i++) {
                    if (List.containsKey(name+"_"+i)) {
                        animations[i-1] = ImageIO.read(new File(List.get(name+"_"+i).address));
                    }
                }
            }
            this.sprite = ImageIO.read(new File(this.address));
            this.width = this.sprite.getWidth();
            this.height = this.sprite.getHeight();
            this.drawingOrder = o;
            this.x = x;
            this.y = y;
            if (mirrored) {
                this.generateMirrored();
            }
            this.scale(s);
        }
    }

    public RenderedSprite(BufferedImage img,int x, int y,double s, int o, Color c) {
        super(0);
        this.sprite = img;
        this.address = "";
        this.name = "Background";
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.drawingOrder = o;
        this.x = x;
        this.y = y;
        this.scale(s);
        if (c != null) {
            this.tint = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0);
            this.tint();
        }
    }

    public boolean equals(RenderedSprite other) {
        if (other.x == this.x && other.y == this.y && this.name.equals(other.name) && this.address.equals(other.address)) {
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(RenderedSprite s) {
        return (this.drawingOrder-s.drawingOrder);
    }

    @Override
    public RenderedSprite clone() {
        return (RenderedSprite) super.clone();
    }
}
