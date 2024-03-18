package com.spaceship.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class GameScreen implements Screen {
    //320, 160 spawn
    private OrthographicCamera cam;
    public double camrot = 0;
    public double prevrot = 0;
    private SpaceshipGameManager manager;
    private SpriteBatch batch;
    private SpaceshipGameManager.Player player;
    public GameScreen (SpaceshipGameManager manager) {
        this.manager = manager;
        player = manager.mainplayer;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // cam setup
        cam = new OrthographicCamera();
        cam.setToOrtho(false, (manager.WORLD_WIDTH*manager.TILE_WIDTH), (manager.WORLD_HEIGHT*manager.TILE_WIDTH));
        cam.position.set(new Vector3((float)(player.x), (float)(player.y), 0));
        camrot = MovementMath.getCameraAngle(cam);
    }

    @Override
    public void render(float delta) {
        //clears screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        double camdis = MovementMath.pointDis(cam.position, new Vector3((float) (player.x), (float) (player.y), 0));
        double camdir = MovementMath.pointDir(cam.position, new Vector3((float) (player.x), (float) (player.y), 0));
        Vector3 campos = MovementMath.lengthDir(camdir, camdis);
        cam.position.set(cam.position.x + campos.x * .05f, cam.position.y + campos.y * .05f, 0);
        cam.zoom = 0.75f;

        if(prevrot<-90&&Math.toDegrees(player.gpulldir)>90){
            camrot = (camrot+360+Math.toDegrees(player.gpulldir))*0.5;
        } else if ((prevrot>90&&Math.toDegrees(player.gpulldir)<-90)) {
            camrot = (camrot-360+Math.toDegrees(player.gpulldir))*0.5;
        } else {
            camrot = (camrot+Math.toDegrees(player.gpulldir))*0.5;
        }
        prevrot = Math.toDegrees(player.gpulldir);

        MovementMath.setCamPos(cam, camrot);

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();

        for(SpaceshipGameManager.Planet planet : manager.PlanetList) {
            for(int i = 0; i<planet.TreeList.size();i++) {
                Texture text = planet.TreeList.get(i).texture;
                boolean alive = planet.TreeList.get(i).checkDestroyable(player,manager.ParticleList,manager.PlanetList);
                if(alive) {
                    batch.draw(new TextureRegion(text),(float)(planet.TreeList.get(i).x),(float)(planet.TreeList.get(i).y),0,0,text.getWidth(),text.getHeight(),1,1,(float)(planet.TreeList.get(i).rotation));
                } else {
                    planet.TreeList.remove(i);
                    i--;
                }
            }

            FrameworkMO.TextureSet text = new FrameworkMO.TextureSet(planet.updatePos(),planet.sprite.x,planet.sprite.y,planet.sprite.depth);
            batch.draw(
                    text.texture,
                    (float)(text.x),
                    (float)(text.y),
                    planet.sprite.texture.getWidth(),
                    planet.sprite.texture.getHeight(),
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
                    //System.out.println(textset.x);
                    batch.draw(
                        textset.texture,
                        (float) (textset.x),
                        (float) (textset.y),
                        textset.texture.getRegionWidth(),
                        textset.texture.getRegionHeight(),
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
        batch.draw(playertext,(float)(player.sprite.x-player.sprite.radius), (float)(player.sprite.y-player.sprite.radius),playertext.getRegionWidth()/2,playertext.getRegionHeight()/2,playertext.getRegionWidth(),playertext.getRegionHeight(),1,1,(float) player.moverot);
        FrameworkMO.TextureSet eyetext = player.getEyeText();
        batch.draw(eyetext.texture,(float)(eyetext.x-player.sprite.radius),(float)(eyetext.y-player.sprite.radius),eyetext.texture.getRegionWidth()/2,eyetext.texture.getRegionHeight()/2,eyetext.texture.getRegionWidth(),eyetext.texture.getRegionHeight(),1,1,(float)Math.toDegrees(eyetext.rotation));

        for(int i = 0; i<manager.PoofCloudList.size();i++) {
            FrameworkMO.TextureSet text = manager.PoofCloudList.get(i).updateTime();
            if(text!=null) {
                batch.draw(text.texture,(float)(text.x),(float)(text.y),text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float)Math.toDegrees(text.rotation));
            } else
                i--;
        }

        batch.end();
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
