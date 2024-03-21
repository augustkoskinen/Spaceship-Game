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
        public Texture texture;
        public Rectangle collision = new Rectangle();
        public double x = 0;
        public double y = 0;
        public double depth = 0;
        public double coloffx = 0;
        public double coloffy = 0;
        public double rotation = 0;

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
            collision.height = (float) colheight;
            collision.width = (float) colwidth;
            collision.x += coladdx;
            collision.y += coladdy;
            if (collist!=null) {
                coloffx = coladdx;
                coloffy = coladdy;
                collist.add(collision);
            }
        }

        public SpriteObjectSqr(String texturepath, double xpos, double ypos, double colwidth, double colheight, double coladdx, double coladdy, double rot, ArrayList collist) {
            if (texturepath.isEmpty()) {
                texture = new Texture(Gdx.files.internal("empty.png"));
            } else {
                texture = new Texture(texturepath);
            }
            x = xpos;
            y = ypos;
            this.depth = ypos;
            rotation = rot;
            collision.x = (float) xpos;
            collision.y = (float) ypos;
            collision.height = (float) colheight;
            collision.width = (float) colwidth;
            collision.x += coladdx;
            collision.y += coladdy;
            if (collist!=null) {
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

    //class for sprites w/ a hitbox
    public static class DestroyableObject extends SpriteObjectSqr {
        public int health = 0;
        public DestroyableObject(String texturepath, double xpos, double ypos, double colwidth, double colheight, double coladdx, double coladdy, double rot, ArrayList collist) {
            super(texturepath, xpos, ypos, colwidth, colheight, coladdx, coladdy, rot, collist);
            health = 5;
        }
        public boolean checkDestroyable(SpaceshipGameManager.Player player, ArrayList<ParticleSet> ParticleList, ArrayList<SpaceshipGameManager.Planet> PlanetList, boolean paused) {
            if(MovementMath.overlaps(player.sprite.collision,collision,rotation,Math.toDegrees(player.gpulldir)+180+45)&& player.justclicked && !paused) {
                player.justclicked = false;
                health--;
                Vector3 placepos = MovementMath.lengthDir(Math.toRadians(rotation+63.4349488229), 35.77708764);
                ParticleList.add(new FrameworkMO.ParticleSet(0,MovementMath.addVect(getPosition(),new Vector3(placepos.x,placepos.y,0)),PlanetList));
            }

            return (health>0);
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
            x = xpos+radius;
            y = ypos+radius;
            radius = colrad;

            if (collist!=null) {
                collision.radius = (float)colrad;
                collist.add(collision);
            }
        }

        public void setPosition(double xpos, double ypos) {
            collision.setPosition((float) (xpos), (float) (ypos));
            x = xpos+radius;
            y = ypos+radius;
        }

        public Vector3 getPosition() {
            return new Vector3((float)(x), (float)(y), 0);
        }

        public void addPosition(Vector3 movevect) {
            collision.setPosition(collision.x + movevect.x, collision.y + movevect.y);
            x += movevect.x;
            y += movevect.y;
        }
        public void addPosition(double x, double y) {
            collision.setPosition(collision.x + (float) x, collision.y + (float) y);
            this.x += x;
            this.y += y;
        }

        public void changeTexture(String texturepath) {
            texture = new Texture(Gdx.files.internal(texturepath));
        }
    }

    public static class ParticleSet {
        ArrayList<Particle> ParticleList = new ArrayList<>();
        public ParticleSet(int type, Vector3 pos, ArrayList<SpaceshipGameManager.Planet> planetlist) {
            switch (type) {
                case 0 : {
                    int pcount = (int)(Math.random()*5)+30;
                    for (int i = 0; i< pcount; i++) {
                        ParticleList.add(new Particle(type, pos, planetlist));
                    }
                    break;
                }
            }
        }
        public ArrayList<TextureSet> getParticles() {
            ArrayList<TextureSet> textlist = new ArrayList<>();
            for(int i = 0; i < ParticleList.size(); i++) {
                if(ParticleList.get(i).checkPos()) {
                    textlist.add(new TextureSet(ParticleList.get(i).sprite.getAnim(),ParticleList.get(i).position.x,ParticleList.get(i).position.y,0,ParticleList.get(i).gravitypull+90));
                } else {
                    ParticleList.remove(i);
                    i--;
                }
            }
            if(ParticleList.isEmpty())
                return null;
            return textlist;
        }
    }
    public static class Particle {
        public AnimationSet sprite = null;
        double maxlife = 0;
        double life = 0;
        int movesign = 1;
        Vector3 position = new Vector3();
        Vector3 velocity = new Vector3();
        double gravitypull = 0;
        public Particle(int type, Vector3 pos, ArrayList<SpaceshipGameManager.Planet> planetlist) {
            switch (type) {
                case 0 : {
                    int pcount = (int)(Math.random()*10);
                    for (int i = 0; i< pcount; i++) {
                        sprite = new AnimationSet("treeparticles.png",5,1,0.1f);
                        sprite.time = ((int)(Math.random()*4)+1)*0.1f;
                        life = Math.random()*.3;
                        position = pos;
                        gravitypull = Math.toDegrees(MovementMath.pointDir(pos,SpaceMath.getClosestPlanet(pos,planetlist).getPosition()));
                        velocity = MovementMath.addVect(MovementMath.lengthDir(Math.toRadians(gravitypull+movesign*90),(Math.random()*1.8)+0.2f),MovementMath.lengthDir(Math.toRadians(gravitypull+180),(Math.random()*80)+0.5f));
                        movesign = (int)(Math.random()*2) == 0 ? -1 : 1;
                        maxlife = 0.7;
                    }
                    break;
                }
            }
        }
        public boolean checkPos() {
            life += Gdx.graphics.getDeltaTime();
            if(maxlife<=life) {
                return false;
            } else {
                position = MovementMath.addVect(new Vector3(velocity.x*Gdx.graphics.getDeltaTime(),velocity.y*Gdx.graphics.getDeltaTime(),0),position);
                Vector3 addside = MovementMath.lengthDir(Math.toRadians(gravitypull+movesign*-1*90),1);
                Vector3 addup = MovementMath.lengthDir(Math.toRadians(gravitypull),3);
                velocity = MovementMath.addVect(velocity,addside,addup);
            }
            return true;
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

        public TextureSet(TextureRegion text, double xpos, double ypos) {
            texture = text;
            x = xpos;
            y = ypos;
            rotation = 0;
            this.depth = 0;
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