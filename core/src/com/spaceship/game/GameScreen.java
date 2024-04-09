package com.spaceship.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

import static com.spaceship.game.MovementMath.hyp;
import static com.spaceship.game.MovementMath.lengthDir;

public class GameScreen implements Screen {
    //320, 160 spawn
    private OrthographicCamera cam;
    public double camrot = 0;
    public double prevrot = 0;
    private SpaceshipGameManager manager;
    private SpriteBatch batch;
    private Texture tabtoboard;
    private Texture tabtoleave;
    private SpaceshipGameManager.Player player;
    public GameScreen (SpaceshipGameManager manager) {
        this.manager = manager;
        player = manager.mainplayer;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        tabtoboard = new Texture("boardaction.png");
        tabtoleave = new Texture("leaveaction.png");

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, 736, 416);
        cam.position.set(new Vector3((float)(player.x), (float)(player.y), 0));
        camrot = MovementMath.getCameraAngle(cam);
    }

    @Override
    public void render(float delta) {
        //clears screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        double camdis = MovementMath.pointDis(cam.position, player.loadedrocket!=null ? player.loadedrocket.sprite.getPosition() : player.getPosition());
        double camdir = MovementMath.pointDir(cam.position, player.loadedrocket!=null ? player.loadedrocket.sprite.getPosition() : player.getPosition());

        Vector3 campos = lengthDir(camdir, camdis);
        cam.position.set(cam.position.x + campos.x * .05f, cam.position.y + campos.y * .05f, 0);
        cam.zoom = 1.5f;//player.loadedrocket!=null ? 3 : 0.75f;

        if(prevrot<-90&&player.gpulldir>90){
            camrot = (camrot+360+player.gpulldir)*0.5;
        } else if ((prevrot>90&&player.gpulldir<-90)) {
            camrot = (camrot-360+player.gpulldir)*0.5;
        } else {
            camrot = (camrot+player.gpulldir)*0.5;
        }
        prevrot = player.gpulldir;

        MovementMath.setCamPos(cam, camrot);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();

        for(SpaceshipGameManager.Planet planet : manager.PlanetList) {
            for(int i = 0; i<planet.TreeList.size();i++) {
                Texture text = planet.TreeList.get(i).texture;
                boolean alive = planet.TreeList.get(i).checkDestroyable(player,manager);
                if(alive) {
                    Vector3 movevect = MovementMath.lengthDir(MovementMath.pointDir(new Vector3(text.getWidth()/2,text.getHeight()/2,0),new Vector3()),MovementMath.pointDis(new Vector3(text.getWidth()/2,text.getHeight()/2,0),new Vector3()));
                    batch.draw(new TextureRegion(text),(float)(planet.TreeList.get(i).x+movevect.x),(float)(planet.TreeList.get(i).y+movevect.y),text.getWidth()/2,text.getHeight()/2,text.getWidth(),text.getHeight(),1,1,(float)(planet.TreeList.get(i).rotation-90));
                } else {
                    planet.TreeList.remove(i);
                    i--;
                }
            }

            FrameworkMO.TextureSet textset = new FrameworkMO.TextureSet(planet.updatePos(),planet.sprite.x,planet.sprite.y,planet.sprite.depth);
            batch.draw(
                    textset.texture,
                    (float)(textset.x-planet.radius),
                    (float)(textset.y-planet.radius),
                    planet.sprite.texture.getWidth()/2,
                    planet.sprite.texture.getHeight()/2,
                    planet.sprite.texture.getWidth(),
                    planet.sprite.texture.getHeight(),
                    1,
                    1,
                    0
            );
        }

        for(int i = 0; i<manager.ParticleList.size();i++) {
            ArrayList<FrameworkMO.TextureSet> textlist = manager.ParticleList.get(i).getParticles();
            if(textlist==null) {
                manager.ParticleList.remove(i);
                i--;
            } else {
                for(FrameworkMO.TextureSet textset : textlist) {
                    batch.draw(
                        textset.texture,
                        (float) (textset.x),
                        (float) (textset.y),
                        textset.texture.getRegionWidth()/2,
                        textset.texture.getRegionHeight()/2,
                        textset.texture.getRegionWidth(),
                        textset.texture.getRegionHeight(),
                        1,
                        1,
                        (float)textset.rotation
                    );
                }
            }
        }

        TextureRegion playertext = player.updatePlayerPos();
        if(playertext!=null) {
            batch.draw(playertext, (float) (player.sprite.x - player.sprite.collision.radius), (float) (player.sprite.y - player.sprite.collision.radius), playertext.getRegionWidth() / 2, playertext.getRegionHeight() / 2, playertext.getRegionWidth(), playertext.getRegionHeight(), 1, 1, (float) player.moverot);
            FrameworkMO.TextureSet eyetext = player.getEyeText();
            batch.draw(eyetext.texture, (float) (eyetext.x - eyetext.texture.getRegionWidth() / 2), (float) (eyetext.y - eyetext.texture.getRegionWidth() / 2), eyetext.texture.getRegionWidth() / 2, eyetext.texture.getRegionHeight() / 2, eyetext.texture.getRegionWidth(), eyetext.texture.getRegionHeight(), 1, 1, (float) eyetext.rotation);
        }

        for(int i = 0; i<manager.ItemList.size();i++) {
            FrameworkMO.TextureSet textset =  manager.ItemList.get(i).updatePosition();
            boolean pickedup = false;
            if(MovementMath.overlaps(player.sprite.collision, manager.ItemList.get(i).collision)) {
                pickedup = manager.ItemList.get(i).gain();
            }
            if(textset!=null&&!pickedup) {
                batch.draw(textset.texture,(float)(textset.x-textset.texture.getRegionWidth()/2),(float)(textset.y-textset.texture.getRegionHeight()/2),textset.texture.getRegionWidth()/2,textset.texture.getRegionHeight()/2,textset.texture.getRegionWidth(),textset.texture.getRegionHeight(), .5f,.5f, (float) manager.ItemList.get(i).gvectdir+90);
            }
        }

        for(int i = 0; i<manager.PoofCloudList.size();i++) {
            FrameworkMO.TextureSet text = manager.PoofCloudList.get(i).updateTime();
            if(text!=null) {
                batch.draw(text.texture,(float)(text.x),(float)(text.y),text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float)Math.toDegrees(text.rotation));
            } else
                i--;
        }

        for(int i = 0; i<manager.RocketList.size();i++) {
            FrameworkMO.TextureSet text = manager.RocketList.get(i).updateSpeed();
            if(text!=null) {
                ArrayList<FrameworkMO.TextureSet> textlist = manager.RocketList.get(i).flamePS.getParticles();
                if(textlist!=null) {
                    for (FrameworkMO.TextureSet textset : textlist) {
                        batch.draw(
                                textset.texture,
                                (float) (textset.x),
                                (float) (textset.y),
                                textset.texture.getRegionWidth() / 2,
                                textset.texture.getRegionHeight() / 2,
                                textset.texture.getRegionWidth(),
                                textset.texture.getRegionHeight(),
                                1,
                                1,
                                (float) textset.rotation
                        );
                    }
                }
                Vector3 movevect = MovementMath.lengthDir(MovementMath.pointDir(new Vector3(text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,0),new Vector3()),MovementMath.pointDis(new Vector3(text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,0),new Vector3()));
                batch.draw(text.texture,(float)(text.x+movevect.x),(float)(text.y+movevect.y),text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float)text.rotation+90);
            } else
                i--;
        }

        batch.end();

        if(manager.canboardship) {
            manager.batch.begin();
            manager.batch.draw(tabtoboard,Gdx.graphics.getWidth()/2-64,Gdx.graphics.getHeight()/2-16-64);
            manager.batch.end();
        } else if(manager.canunboardship) {
            manager.batch.begin();
            manager.batch.draw(tabtoleave,Gdx.graphics.getWidth()/2-64,Gdx.graphics.getHeight()/2-16-64);
            manager.batch.end();
        }

        player.inventory.drawInventory(manager.batch);
    }

    //necessary overrides
    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
