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
    }

    @Override
    public void render(float delta) {
        //clears screen
        ScreenUtils.clear(0, 0, 0, 1, true);

        double camdis = MovementMath.pointDis(cam.position, new Vector3((float) (player.x), (float) (player.y), 0));
        double camdir = MovementMath.pointDir(cam.position, new Vector3((float) (player.x), (float) (player.y), 0));
        Vector3 campos = MovementMath.lengthDir(camdir, camdis);
        cam.position.set(cam.position.x + campos.x * .05f, cam.position.y + campos.y * .05f, 0);
        cam.zoom = 0.4f;

        cam.update();
        batch.setProjectionMatrix(cam.combined);

        batch.begin();
        player.updateControls();
        TextureRegion playertext = player.updatePlayerPos();
        batch.draw(playertext,(float)(player.sprite.x), (float)(player.sprite.y),playertext.getRegionWidth()/2,playertext.getRegionHeight()/2,playertext.getRegionWidth(),playertext.getRegionHeight(),1,1,(float) player.moverot);
        FrameworkMO.TextureSet eyetext = player.getEyeText();
        batch.draw(eyetext.texture,(float)(eyetext.x),(float)(eyetext.y),eyetext.texture.getRegionWidth()/2,eyetext.texture.getRegionHeight()/2,eyetext.texture.getRegionWidth(),eyetext.texture.getRegionHeight(),1,1,(float)Math.toDegrees(eyetext.rotation));

        for(int i = 0; i<manager.PlanetList.size();i++) {
            FrameworkMO.TextureSet text = new FrameworkMO.TextureSet(manager.PlanetList.get(i).updatePos(),manager.PlanetList.get(i).x,manager.PlanetList.get(i).y,manager.PlanetList.get(i).sprite.depth);
            batch.draw(text.texture,(float)(text.x),(float)(text.y),text.texture.getRegionWidth()/2,text.texture.getRegionHeight()/2,text.texture.getRegionWidth(),text.texture.getRegionHeight(),1,1,(float)Math.toDegrees(text.rotation));
        }

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
