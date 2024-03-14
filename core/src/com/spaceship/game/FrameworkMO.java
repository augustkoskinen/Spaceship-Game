package com.spaceship.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Random;

public class FrameworkMO {
    //draws sprites with depth layering
    public static void DrawWithLayering(SpriteBatch spritebatch, ArrayList<TextureSet> list) {
        ArrayList<TextureSet> textlist = new ArrayList<TextureSet>();
        for (TextureSet textureSet : list) {
            double tempdepth = textureSet.depth;
            int ind = textlist.size();
            while (ind > 0 && tempdepth < textlist.get(ind - 1).depth) {
                ind--;
            }
            ind = Math.max(0, ind);
            textlist.add(ind, textureSet);
        }
        spritebatch.begin();
        for (int i = 0; i < list.size(); i++) {
            if (textlist.get(i).texture != null)
                spritebatch.draw(textlist.get(i).texture, (float) (textlist.get(i).x), (float) (textlist.get(i).y), textlist.get(i).texture.getRegionWidth() / 2f, textlist.get(i).texture.getRegionHeight() / 2f, textlist.get(i).texture.getRegionWidth(), textlist.get(i).texture.getRegionHeight(), 1, 1, (float) Math.toDegrees(textlist.get(i).rotation));
        }
        spritebatch.end();
    }

    //class for sprites w/ a hitbox
    public static class SpriteObjectSqr {
        Texture texture;
        Rectangle collision = new Rectangle();
        double x = 0;
        double y = 0;
        double depth = 0;
        double coloffx = 0;
        double coloffy = 0;

        public SpriteObjectSqr(String texturepath, double xpos, double ypos, double colwidth, double colheight, double coladdx, double coladdy, ArrayList collist) {
            if (texturepath.isEmpty()) {
                texture = new Texture(Gdx.files.internal("empty.png"));
            } else {
                texture = new Texture(texturepath);
            }
            x = xpos;
            y = ypos;
            this.depth = ypos;
            collision.x = (float) xpos;
            collision.y = (float) ypos;
            if (collist!=null) {
                collision.height = (float) colheight;
                collision.width = (float) colwidth;
                collision.x += coladdx;
                collision.y += coladdy;
                coloffx = coladdx;
                coloffy = coladdy;
                collist.add(collision);
            }
        }

        public void setPosition(double xpos, double ypos) {
            collision.setPosition((float) (xpos + coloffx), (float) (ypos + coloffy));
            x = xpos;
            y = ypos;
        }

        public Vector3 getPosition() {
            return new Vector3((float) x, (float) y, 0);
        }

        public void addPosition(Vector3 movevect) {
            collision.setPosition((float) (x + movevect.x + coloffx), (float) (y + movevect.y + coloffy));
            x = x + movevect.x;
            y = y + movevect.y;
        }

        public void changeTexture(String texturepath) {
            texture = new Texture(Gdx.files.internal(texturepath));
        }
    }

    public static class SpriteObjectCirc {
        Texture texture;
        Circle collision = new Circle();
        double x = 0;
        double y = 0;
        double radius = 0;
        double depth = 0;

        public SpriteObjectCirc(String texturepath, double xpos, double ypos, double colrad, ArrayList collist) {
            if (texturepath.isEmpty()) {
                texture = new Texture(Gdx.files.internal("empty.png"));
            } else {
                texture = new Texture(texturepath);
            }
            this.depth = ypos;

            collision.x = (float) xpos;
            collision.y = (float) ypos;
            x = xpos;
            y = ypos;
            radius = colrad;

            if (collist!=null) {
                collision.radius = (float)colrad;
                collist.add(collision);
            }
        }

        public void setPosition(double xpos, double ypos) {
            collision.setPosition((float) (xpos ), (float) (ypos));
            x = xpos;
            y = ypos;
        }

        public Vector3 getPosition() {
            return new Vector3((float) x, (float) y, 0);
        }

        public void addPosition(Vector3 movevect) {
            collision.setPosition((float) (x + movevect.x ), (float) (y + movevect.y));
            x = x + movevect.x+radius;
            y = y + movevect.y+radius;
        }

        public void changeTexture(String texturepath) {
            texture = new Texture(Gdx.files.internal(texturepath));
        }
    }

    //class for animations and managing them
    public static class AnimationSet {
        Animation<TextureRegion> animation;
        Texture sheet;
        double time;
        boolean repeat = true;
        String textpath;
        int rows;
        int cols;
        double framereg;

        public AnimationSet(String textpath, int cols, int rows, double framereg) {
            sheet = new Texture(Gdx.files.internal(textpath));
            this.textpath = textpath;
            this.rows = rows;
            this.cols = cols;
            this.framereg = framereg;

            TextureRegion[][] tmp = TextureRegion.split(sheet,
                    sheet.getWidth() / cols,
                    sheet.getHeight() / rows
            );

            TextureRegion[] walkFrames = new TextureRegion[cols * rows];
            int index = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    walkFrames[index++] = tmp[i][j];
                }
            }

            animation = new Animation<TextureRegion>((float) (framereg), walkFrames);

            time = 0f;
        }

        public AnimationSet(String textpath, int cols, int rows, double framereg, boolean irepeat) {
            sheet = new Texture(Gdx.files.internal(textpath));
            this.textpath = textpath;
            this.rows = rows;
            this.cols = cols;
            this.framereg = framereg;
            repeat = irepeat;

            TextureRegion[][] tmp = TextureRegion.split(sheet,
                    sheet.getWidth() / cols,
                    sheet.getHeight() / rows
            );

            TextureRegion[] walkFrames = new TextureRegion[cols * rows];
            int index = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    walkFrames[index++] = tmp[i][j];
                }
            }

            animation = new Animation<TextureRegion>((float) (framereg), walkFrames);

            time = 0f;
        }

        public TextureRegion updateTime(double speed) {
            time += Gdx.graphics.getDeltaTime() * speed;
            return animation.getKeyFrame((float) time, repeat);
        }

        public TextureRegion incrementTime() {
            time += 0.1f;
            return animation.getKeyFrame((float) time, repeat);
        }

        public TextureRegion getAnim() {
            return animation.getKeyFrame((float) time, repeat);
        }
    }

    //class for a set of textures, position, rotation, and depth
    public static class TextureSet {
        TextureRegion texture;
        AnimationSet animationtexture;
        boolean isanimated = false;
        double x;
        double y;
        double rotation;
        double depth;

        public TextureSet(TextureRegion text, double xpos, double ypos, double depth) {
            texture = text;
            x = xpos;
            y = ypos;
            rotation = 0;
            this.depth = depth;
        }

        public TextureSet(TextureRegion text, double xpos, double ypos, double depth, double rot) {
            texture = text;
            x = xpos;
            y = ypos;
            rotation = rot;
            this.depth = depth;
        }

        public TextureSet(String text, double xpos, double ypos, double depth, int cols, int rows, double framereg) {
            texture = new TextureRegion(new Texture(text));
            x = xpos;
            y = ypos;
            animationtexture = new AnimationSet(text, cols, rows, framereg);
            isanimated = true;
            rotation = 0;
            this.depth = depth;
        }

        public TextureSet(TextureSet text, double xpos, double ypos) {
            texture = text.texture;
            depth = text.depth;
            x = xpos;
            y = ypos;
            rotation = 0;
        }
    }
}