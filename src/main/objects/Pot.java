package main.objects;

import visual.RenderedSprite;

import java.util.Objects;

public class Pot extends Collidable {
    RenderedSprite sprite = null;
    boolean visible = false;
    //Item content = "";
    public Pot() {

    }
    void destroy() {

    }
    @Override
    public void Touched(Collidable c) {
        if (c.name.equals("ARROW")) {
            this.destroy();
        }
    }

}
