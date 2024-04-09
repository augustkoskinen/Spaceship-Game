package com.spaceship.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;

public class FrameworkMO {
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

        public SpriteObjectSqr(String texturepath, double xpos, double ypos, double colwidth, double colheight, double coladdx, double coladdy, double rot, ArrayList collist) {
            if (texturepath.isEmpty()) {
                texture = new Texture(Gdx.files.internal("empty.png"));
            } else {
                texture = new Texture(texturepath);
            }
            x = xpos;
            y = ypos;
            this.depth = ypos;
            collision.height = (float) colheight;
            collision.width = (float) colwidth;
            collision.x = (float)(xpos + coladdx);
            collision.y = (float)(ypos + coladdy);

            if (collist!=null) {
                coloffx = coladdx;
                coloffy = coladdy;
                collist.add(collision);
            }

            rotate(rot);
        }

        public void setPosition(double xpos, double ypos) {
            collision.setPosition((float) (xpos + coloffx), (float) (ypos + coloffy));
            x = xpos;
            y = ypos;
        }

        public void addPosition(Vector3 movevect) {
            x = x + movevect.x;
            y = y + movevect.y;
            collision.setPosition((float) (x + coloffx), (float) (y + coloffy));
        }

        public Vector3 getPosition() {
            return new Vector3((float) x, (float) y, 0);
        }

        public void rotate(double r) {
            rotation = r;
        }
    }

    //class for sprites w/ a hitbox
    public static class DestroyableObject extends SpriteObjectSqr {
        public int health = 0;
        public DestroyableObject(String texturepath, double xpos, double ypos, double colwidth, double colheight, double coladdx, double coladdy, double rot, ArrayList collist) {
            super(texturepath, xpos, ypos, colwidth, colheight, coladdx, coladdy, rot, collist);
            health = 5;
        }
        public boolean checkDestroyable(SpaceshipGameManager.Player player, SpaceshipGameManager manager) {
            if(MovementMath.pointDis(player.getPosition(),getPosition())<64 && MovementMath.overlaps(player.sprite.collision, collision, rotation) && player.justclicked && !manager.pause) {
                player.justclicked = false;
                health--;
                manager.ParticleList.add(new FrameworkMO.ParticleSet(0,getPosition(),manager.PlanetList));
                SpaceshipGameManager.Item additem = new SpaceshipGameManager.Item(0);
                additem.drop(MovementMath.addVect(getPosition(), MovementMath.lengthDir(Math.random()*360,8)));
            }

            return (health>0);
        }
    }

    public static class SpriteObjectCirc {
        Texture texture;
        Circle collision = new Circle();
        double x = 0;
        double y = 0;
        double depth = 0;
        double rotation = 0;

        public SpriteObjectCirc(String texturepath, double xpos, double ypos, double colrad, ArrayList collist) {
            if (texturepath.isEmpty()) {
                texture = new Texture(Gdx.files.internal("empty.png"));
            } else {
                texture = new Texture(texturepath);
            }
            this.depth = ypos;

            collision.x = (float)(xpos);
            collision.y = (float)(ypos);
            collision.radius = (float)colrad;
            x = xpos;
            y = ypos;

            if (collist!=null) {
                collist.add(collision);
            }
        }

        public void setPosition(double xpos, double ypos) {
            collision.setPosition((float) (xpos), (float) (ypos));
            x = xpos;
            y = ypos;
        }

        public void addPosition(Vector3 movevect) {
            x += movevect.x;
            y += movevect.y;
            collision.setPosition((float)(x), (float)(y));
        }

        public Vector3 getPosition() {
            return new Vector3((float)(x), (float)(y), 0);
        }
    }

    public static class ParticleSet {
        ArrayList<Particle> ParticleList = new ArrayList<>();
        public ParticleSet(int type, Vector3 pos, ArrayList<SpaceshipGameManager.Planet> planetlist) {
            switch (type) {
                case 0 : {
                    int pcount = (int)(Math.random()*5)+30;
                    for (int i = 0; i< pcount; i++) {
                        ParticleList.add(new Particle(type, MovementMath.addVect(pos, MovementMath.lengthDir(Math.random()*360,2)), planetlist));
                    }
                    break;
                }
            }
        }
        public ParticleSet(int type, Vector3 pos, ArrayList<SpaceshipGameManager.Planet> planetlist, double rotation) {
            switch (type) {
                case 0 : {
                    int pcount = (int)(Math.random()*5)+30;
                    for (int i = 0; i< pcount; i++) {
                        ParticleList.add(new Particle(type, MovementMath.addVect(pos, MovementMath.lengthDir(Math.random()*360,2)), planetlist));
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
        public void addParticle(int type, Vector3 pos, ArrayList<SpaceshipGameManager.Planet> planetlist, double rotation) {
            switch (type) {
                case 0 : {
                    ParticleList.add(new Particle(type, pos, planetlist, rotation));
                    break;
                }
                case 1 : {
                    ParticleList.add(new Particle(type, MovementMath.addVect(pos,new Vector3((float)(Math.random()*10 - 5),(float)(Math.random()*10 - 5),0)), planetlist, rotation));
                    break;
                }
            }
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
        int type = -1;
        public Particle(int type, Vector3 pos, ArrayList<SpaceshipGameManager.Planet> planetlist) {
            this.type = type;
            switch (type) {
                case 0 : {
                    sprite = new AnimationSet("treeparticles.png",5,1,0.1f);
                    sprite.time = ((int)(Math.random()*4)+1)*0.1f;
                    life = Math.random()*.3;
                    position = pos;
                    gravitypull = MovementMath.pointDir(pos,SpaceMath.getClosestPlanet(pos,planetlist).getPosition());
                    velocity = MovementMath.addVect(MovementMath.lengthDir(gravitypull+movesign*90,(Math.random()*1.8)+0.2f),MovementMath.lengthDir(gravitypull+180,(Math.random()*80)+0.5f));
                    movesign = (int)(Math.random()*2) == 0 ? -1 : 1;
                    maxlife = 0.7;
                    break;
                }
            }
        }
        public Particle(int type, Vector3 pos, ArrayList<SpaceshipGameManager.Planet> planetlist, double rotation) {
            this.type = type;
            switch (type) {
                case 0 : {
                    sprite = new AnimationSet("treeparticles.png",5,1,0.1f);
                    sprite.time = ((int)(Math.random()*4)+1)*0.1f;
                    life = Math.random()*.3;
                    position = pos;
                    gravitypull = MovementMath.pointDir(pos,SpaceMath.getClosestPlanet(pos,planetlist).getPosition());
                    velocity = MovementMath.addVect(MovementMath.lengthDir(gravitypull+movesign*90,(Math.random()*1.8)+0.2f),MovementMath.lengthDir(gravitypull+180,(Math.random()*80)+0.5f));
                    movesign = (int)(Math.random()*2) == 0 ? -1 : 1;
                    maxlife = 0.7;
                    break;
                }
                case 1 : {
                    sprite = new AnimationSet("flame.png",5,1,0.1f);
                    life = Math.random()*.5+.75;
                    position = pos;
                    movesign = (int)(Math.random()*2) == 0 ? -1 : 1;
                    velocity = MovementMath.lengthDir(rotation+(90*movesign),(int)(Math.random()*30)+10);
                    maxlife = 2;
                    break;
                }
            }
        }
        public boolean checkPos() {
            switch (type) {
                case 0 : {
                    life += Gdx.graphics.getDeltaTime();
                    if (maxlife <= life) {
                        return false;
                    } else {
                        position = MovementMath.addVect(new Vector3(velocity.x * Gdx.graphics.getDeltaTime(), velocity.y * Gdx.graphics.getDeltaTime(), 0), position);
                        Vector3 addside = MovementMath.lengthDir(gravitypull + movesign * -1 * 90, 1);
                        Vector3 addup = MovementMath.lengthDir(gravitypull, 3);
                        velocity = MovementMath.addVect(velocity, addside, addup);
                    }
                }
                case 1 : {
                    sprite.updateTime(life);

                    if (sprite.time >= sprite.framereg*sprite.cols*sprite.rows) {
                        return false;
                    } else {
                        position = MovementMath.addVect(new Vector3(velocity.x * Gdx.graphics.getDeltaTime(), velocity.y * Gdx.graphics.getDeltaTime(), 0), position);
                    }
                    velocity.x *=.975f;
                    velocity.y *=.975f;
                }
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