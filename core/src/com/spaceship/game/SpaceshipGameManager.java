package com.spaceship.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

public class SpaceshipGameManager extends Game {
	SpriteBatch batch;
	public static boolean pause;
	public String launcher;
	public static final int TILE_WIDTH = 16;
	public static final int WORLD_WIDTH = 46;
	public static final int WORLD_HEIGHT = 26;
	public static final double MULT_AMOUNT = 55;
	public static double WIND_WIDTH = 1024;
	public static double WIND_HEIGHT = 576;
	public static double CHANGE_RATIO = 1;
	public static double SLOWSPEED = 1;
	public static final double GRAVITY = 0.25f*MULT_AMOUNT;
	public static final double WALK_SPEED = 2*MULT_AMOUNT;
	public static final double JUMP_SPEED = 5.2f*MULT_AMOUNT;
	public static Player mainplayer;
	static ArrayList<PoofCloud> PoofCloudList = new ArrayList<>();
	static ArrayList<Planet> PlanetList = new ArrayList<>();
	static ArrayList CollisionList = new ArrayList<>();
	public static boolean shake = false;
	public static double shaketime = 0.7f;
	public static Vector3 loadjiggle = new Vector3();
	public SpaceshipGameManager(String launcher){ this.launcher = launcher; }

	@Override
	public void create () {
		batch = new SpriteBatch();
		mainplayer = new Player(new Vector3(256,800,0),new Vector3(8,8,0),0);
		new Planet(new Vector3(0,0,0), new Vector3(8,8,0),0);
		this.setScreen(new GameScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

	public static void addShake(double add) {
		shake = true;
		shaketime = add;
	}

	public void updateShake() {
		if(shake)
			loadjiggle = new Vector3((float)((shaketime*2)*((int)(Math.random()*2)==0 ? 1f : -1f)),(float)((shaketime*2)*((int)(Math.random()*2)==0 ? 1f : -1f)),0);

		if(shaketime>0)
			shaketime-=Gdx.graphics.getDeltaTime();
		else {
			shaketime = .7f;
			shake = false;
			loadjiggle = new Vector3();
		}
	}




	//===========>
	//OBJECTS
	//===========>


	public static class Player {
		public FrameworkMO.SpriteObjectSqr sprite;
		public FrameworkMO.AnimationSet animrw;
		public FrameworkMO.AnimationSet animlw;
		public Vector3 movevect;
		public double horzmove;
		public int movedir = 1;
		public double lastdir = 1;
		public int jumpcount = 5;
		public int jumpdir = 0;
		public double moverot = 0;
		public int jumpdiradd = 0;
		public int controltype;
		public int skintype = 1;
		public int prevskintype = 1;
		public Controller controller;
		public boolean chosechar = false;
		public boolean firebutton = false;
		public boolean jumpbutton = false;
		public boolean firebuttonrelease = false;
		public boolean jumpbuttonrelease = false;
		public boolean pressright = false;
		public boolean pressleft = false;
		public boolean releaseright = false;
		public boolean releaseleft = false;
		public double deadcount = 0;
		public double x = 0;
		public double y = 0;
		public Vector3 centerpos;

		public Player(Vector3 pos, Vector3 centerpos, int type) {
			movevect = new Vector3();
			controltype = 0;
			sprite = new FrameworkMO.SpriteObjectSqr("p1body.png",pos.x,pos.y,16,16,0,0,CollisionList);

			this.centerpos = centerpos;

			x=pos.x+centerpos.x;
			y=pos.y+centerpos.y;

			//if (Controllers.getControllers().size == 0) controltype = 0;
			if (controltype == 1) {
				controller = Controllers.getControllers().get(0);
			}
		}

		public Player(Vector3 pos, Vector3 centerpos, int type, Controller controller) {
			movevect = new Vector3();
			controltype = type;
			sprite = new FrameworkMO.SpriteObjectSqr("p1body.png",pos.x,pos.y,16,16,0,0,CollisionList);

			this.centerpos = centerpos;

			x=pos.x+centerpos.x;
			y=pos.y+centerpos.y;

			if (Controllers.getControllers().size == 0) controltype = 0;
			if (controltype == 1) {
				this.controller = controller;
			}
		}

		public TextureRegion updatePlayerPos() {
			TextureRegion playertext = null;
			int rightmove = (controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.D) : (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d > .25) ? 1 : 0;
			int leftmove = (controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.A) : (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d < -.25) ? 1 : 0;
			int netmove = (rightmove - leftmove);
			Rectangle playercol = MovementMath.DuplicateRect(sprite.collision);
			playertext = new TextureRegion(new Texture("p" + skintype + "body.png"));

			if (!pause) {
				//SLOWSPEED = 1;
				if ((controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) : controller.getButton(controller.getMapping().buttonL1))) {
					//SLOWSPEED = 0.25f;
				}

				if (netmove != 0) {
					double movespeed = WALK_SPEED; //(controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? RUN_SPEED :
					horzmove = netmove * (movespeed) + movevect.x;
					horzmove = Math.max(Math.min(movevect.x + horzmove, movespeed), -movespeed) - movevect.x;
					movedir = rightmove - leftmove;
				}

				//animlw.framereg = (controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;
				//animrw.framereg = (controltype == 0 ? Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) : controller.getButton(controller.getMapping().buttonR1)) ? 0.1f : 0.2f;

				if (movevect.y < 10 * MULT_AMOUNT)
					movevect.y -= GRAVITY * MULT_AMOUNT * Gdx.graphics.getDeltaTime() * SLOWSPEED;

				boolean grounded = true;//MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(0, -1, 0), new Vector3(15, 15, 0));
				boolean wallsliding = false;//((MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(1, 0, 0), new Vector3(15, 15, 0)) && rightmove == 1) || (MovementMath.CheckCollisions(WORLD_MAP, playercol, 0, new Vector3(-1, 0, 0), new Vector3(15, 15, 0)) && leftmove == 1));
				if (grounded) jumpcount = 5;

				if ((controltype == 0 ? Gdx.input.isKeyJustPressed(Input.Keys.F) : jumpbutton) && jumpcount > 0) {
					int degree = MovementMath.toDegrees(controller);
					if (degree != -1) {
						double movex = 0;
						double movey = 0;
						int turnfactor = 0;
						switch (degree) {
							case 0: {
								movey = JUMP_SPEED;
								turnfactor = -8;
								break;
							}
							case 45: {
								movex = -JUMP_SPEED;
								movey = JUMP_SPEED;
								turnfactor = -8;
								break;
							}
							case 90: {
								movex = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 135: {
								movex = -JUMP_SPEED;
								movey = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 180: {
								movey = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 225: {
								movex = JUMP_SPEED;
								movey = -JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 270: {
								movex = JUMP_SPEED;
								turnfactor = -36;
								break;
							}
							case 315: {
								movex = JUMP_SPEED;
								movey = JUMP_SPEED;
								turnfactor = -8;
								break;
							}
						}
						PoofCloudList.add(new PoofCloud(degree, new Vector3((float)(sprite.x), (float)(sprite.y), 0)));
						jumpcount--;
						if (jumpdiradd == 0) {
							jumpdiradd = movedir * turnfactor;
							if (movedir < 0)
								jumpdir = -360;
							else
								jumpdir = 360;
						}
						if (movevect.y < 0) movevect.y = 0;

						if (!wallsliding) {
							movevect.x += movex;
							movevect.y += movey;
						} else {
							movevect.x = (float)(-movedir * 4.5f * MULT_AMOUNT);
							movevect.y += (float)(3 * MULT_AMOUNT);
						}
						movevect.y = (float)(Math.min(movevect.y, 5 * MULT_AMOUNT));

						lastdir = degree;
					}
				} else {
					if (wallsliding) movevect.y = (float)(-1 * MULT_AMOUNT);
				}

				if (MovementMath.toDegrees(controller) != -1) lastdir = MovementMath.toDegrees(controller);

				if ((controltype == 0 ? Gdx.input.isKeyJustPressed(Input.Keys.G) : firebutton)) {
					//BulletList.add(new Bullet(lastdir + 180, new Vector3(sprite.x + 4, sprite.y + 4, 0), "p" + skintype + "bullet.png", this));
				}

				playercol = MovementMath.DuplicateRect(sprite.collision);

				if (-1!=MovementMath.CheckCollisions(playercol, CollisionList, new Vector3((float)((movevect.x + horzmove) * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0, 0), new Vector3(15, 15, 0))) {
					double sign = Math.abs(movevect.x + horzmove) / (movevect.x + horzmove);
					while (-1==MovementMath.CheckCollisions(playercol, CollisionList, new Vector3((float)(sign), 0, 0), new Vector3(15, 15, 0))) {
						sprite.addPosition(new Vector3((float)(sign), 0, 0));
						playercol = MovementMath.DuplicateRect(sprite.collision);
					}
					movevect.x = 0;
					horzmove = 0;
				}
				sprite.addPosition(new Vector3((float)((movevect.x + horzmove) * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0, 0));

				playercol = MovementMath.DuplicateRect(sprite.collision);
				if (-1!=MovementMath.CheckCollisions(playercol, CollisionList, new Vector3(0, (float)((movevect.y) * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0), new Vector3(15, 15, 0))) {
					double sign = Math.abs(movevect.y) / movevect.y;
					while (-1==MovementMath.CheckCollisions(playercol, CollisionList, new Vector3(0, (float)(sign), 0), new Vector3(15, 15, 0))) {
						sprite.addPosition(new Vector3(0, (float)(sign), 0));
						playercol = MovementMath.DuplicateRect(sprite.collision);
					}
					movevect.y = 0;
				}
				sprite.addPosition(new Vector3(0, (float)((movevect.y) * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0));

				moverot -= ((movevect.x + horzmove) * Gdx.graphics.getDeltaTime() * SLOWSPEED) * 5;
				movevect.x *= .88f;
				horzmove *= (grounded ? .5f : .88f);

				x=sprite.x+centerpos.x;
				y=sprite.y+centerpos.y;
			}

			sprite.depth = y;
			if (jumpdir != 0) {
				jumpdir += jumpdiradd;
				if (jumpdir == 0) {
					jumpdiradd = 0;
				}
			}

			return playertext;
		}

		public void setPosition(Vector3 newpos){
			sprite.setPosition(newpos.x,newpos.y);
			x = newpos.x+centerpos.x;
			y = newpos.y+centerpos.y;
		}
		public void addPosition(Vector3 addpos){
			sprite.addPosition(addpos);
			x = sprite.x+centerpos.x;
			y = sprite.y+centerpos.y;
		}
		public Vector3 getPosition(){
			return new Vector3((float) x,(float) y,0);
		}

		public FrameworkMO.TextureSet getEyeText() {
			Vector3 addpos = MovementMath.lengthDir((double) Math.toRadians(lastdir + 90), 1.8f);

			double xpos = sprite.collision.x + addpos.x;
			double ypos = sprite.collision.y - 0.5f + addpos.y;

			return new FrameworkMO.TextureSet(new TextureRegion(new Texture("eyes.png")), xpos, ypos, 10000, (double) Math.toRadians(lastdir + 90));
		}

		public void updateControls() {
			if (controltype == 1) {
				firebutton = false;
				jumpbutton = false;

				if (firebuttonrelease && controller.getButton(controller.getMapping().buttonA) && !firebutton)
					firebutton = true;
				firebuttonrelease = !controller.getButton(controller.getMapping().buttonA);

				if (jumpbuttonrelease && controller.getButton(controller.getMapping().buttonB) && !jumpbutton)
					jumpbutton = true;
				jumpbuttonrelease = !controller.getButton(controller.getMapping().buttonB);


				pressright = false;
				boolean rdown = (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d > .45;

				if (rdown && releaseright) {
					pressright = true;
				}

				if (!rdown) releaseright = true;
				else releaseright = false;

				pressleft = false;
				boolean ldown = (double) Math.round((controller.getAxis(controller.getMapping().axisLeftX)) * 100d) / 100d < -.45;

				if (ldown && releaseleft) {
					pressleft = true;
				}

				if (!ldown) releaseleft = true;
				else releaseleft = false;
			}
		}
	}

	public static class Planet {
		public FrameworkMO.SpriteObjectCirc sprite;
		public Vector3 movevect;
		public double x = 0;
		public double y = 0;
		public Vector3 centerpos;
		public int type = 0;

		public Planet(Vector3 pos, Vector3 centerpos, int type) {
			movevect = new Vector3();
			type = (int)(Math.random()*3);
			sprite = new FrameworkMO.SpriteObjectCirc("planet1.png",pos.x,pos.y,512,CollisionList);

			this.centerpos = centerpos;
			x=pos.x+centerpos.x;
			y=pos.y+centerpos.y;

			PlanetList.add(this);
		}

		public TextureRegion updatePos() {
			sprite.depth = sprite.y;
			return new TextureRegion(sprite.texture);
		}

		public void setPosition(Vector3 newpos){
			sprite.setPosition(newpos.x,newpos.y);
			x = newpos.x+centerpos.x;
			y = newpos.y+centerpos.y;
		}
		public void addPosition(Vector3 addpos){
			sprite.addPosition(addpos);
			x = sprite.x+centerpos.x;
			y = sprite.y+centerpos.y;
		}
		public Vector3 getPosition(){
			return new Vector3((float) x,(float) y,0);
		}
	}

	public static class PoofCloud {
		private double life = 0.7f;
		private int type = 0;
		private double time = 0;
		private double dir;
		private double speed = 1;
		private FrameworkMO.AnimationSet animation;
		private Vector3 pos;

		public PoofCloud(double dir, Vector3 pos){
			animation = new FrameworkMO.AnimationSet("poof.png",7,1,0.1f);
			this.pos = pos;
			this.dir = dir;
		}
		public PoofCloud(double dir, Vector3 pos,int type){
			this.type = type;
			if(type==1) {
				animation = new FrameworkMO.AnimationSet("poofcloud.png",8,1,0.1f,false);
				this.pos = pos;
				this.dir = 0;
				this.life = 0.8f;
			} else {
				animation = new FrameworkMO.AnimationSet("poof.png",7,1,0.1f);
				this.pos = pos;
				this.dir = dir;
			}
		}
		public FrameworkMO.TextureSet updateTime(){
			if(type==0) {
				Vector3 addvect = MovementMath.lengthDir((double) Math.toRadians(dir - 90), speed*Gdx.graphics.getDeltaTime()*MULT_AMOUNT);
				if(!pause)
					pos = new Vector3(pos.x + addvect.x, pos.y + addvect.y, 0);
			}

			if(!pause) {
				time += Gdx.graphics.getDeltaTime();
				speed *= .9f;
			}

			if(time>=life) {
				PoofCloudList.remove(this);
				return null;
			}

			if(!pause)
				return new FrameworkMO.TextureSet(animation.updateTime(1),pos.x,pos.y,100000,(double)Math.toRadians(dir));
			else
				return new FrameworkMO.TextureSet(animation.getAnim(),pos.x,pos.y,100000,(double)Math.toRadians(dir));
		}
	}
}
