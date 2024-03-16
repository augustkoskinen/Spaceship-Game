package com.spaceship.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

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
	public static final double WALK_SPEED = 1*MULT_AMOUNT;
	public static final double JUMP_SPEED = 8f*MULT_AMOUNT;
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
		mainplayer = new Player(new Vector3(512,512,0),new Vector3(8,8,0));
		new Planet(new Vector3(0,0,0),0);
		new Planet(new Vector3(448,448,0),0);
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
		//basic vars
		public FrameworkMO.SpriteObjectCirc sprite;
		public double x = 0;
		public double y = 0;
		public Vector3 centerpos;
		public int skintype = 1;

		//move
		public Vector3 netvect = new Vector3();
		public Vector3 walkvect = new Vector3();
		public Vector3 jumpvect = new Vector3();
		public Vector3 gpullvect = new Vector3();
		public int movedir = 1;
		public double moverot = 0;
		public double lastdir = 1;
		public double gpulldir;

		//jump
		public int jumpcount = 1;
		public int jumpdir = 0;
		public int jumpdiradd = 0;

		public Player(Vector3 pos, Vector3 centerpos) {
			sprite = new FrameworkMO.SpriteObjectCirc("p1body.png", pos.x, pos.y, 8, CollisionList);

			this.centerpos = centerpos;

			x = pos.x + centerpos.x;
			y = pos.y + centerpos.y;
		}

		public TextureRegion updatePlayerPos() {
			//setting up vars
			TextureRegion playertext;
			int rightmove = Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0;
			int leftmove = Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0;
			int netmove = (rightmove-leftmove);
			Circle playercol = MovementMath.DuplicateCirc(sprite.collision);
			playertext = new TextureRegion(new Texture("p" + skintype + "body.png"));

			if (!pause) {
				gpulldir = MovementMath.pointDir(sprite.getPosition(),SpaceMath.getClosestPlanet(sprite.getPosition(),PlanetList).getPosition());

				double multamount = Gdx.graphics.getDeltaTime() * SLOWSPEED;
				Vector3 gpulltemp = SpaceMath.getNetGravity(sprite.getPosition(), PlanetList);
				gpullvect = MovementMath.addVect(gpullvect, new Vector3((float)(gpulltemp.x * multamount),(float)(gpulltemp.y * multamount),0));

				if (netmove != 0) {
					double movespeed = netmove * WALK_SPEED;
					walkvect = new Vector3(walkvect.x+(float)(Math.cos(gpulldir+Math.toRadians(90))*movespeed),walkvect.y+(float)(Math.sin(gpulldir+Math.toRadians(90))*movespeed),0);
					movedir = netmove;
				}

				boolean grounded = MovementMath.CheckCollisions(playercol, CollisionList, MovementMath.lengthDir(gpulldir,1), 7.5f)!=-1;

				if (grounded) { jumpcount = 1; gpullvect = new Vector3();}

				if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && jumpcount > 0) {

					PoofCloudList.add(new PoofCloud(Math.toDegrees(gpulldir)+90, new Vector3((float) (sprite.x-8), (float) (sprite.y-8), 0)));

					jumpcount--;
					if (jumpdiradd == 0) {
						jumpdiradd = movedir * 1;
						if (movedir < 0)
							jumpdir = -360;
						else
							jumpdir = 360;
					}

					Vector3 addupvect = MovementMath.lengthDir(gpulldir+Math.toRadians(180),JUMP_SPEED);
					jumpvect.x = addupvect.x;
					jumpvect.y = addupvect.y;
				}

				if (MovementMath.toDegrees() != -1) lastdir = MovementMath.toDegrees();

				/*
				float curupdir = 45;
				if (-1 == MovementMath.CheckCollisions(playercol, CollisionList, new Vector3((float) (netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), (float) (Math.sin(Math.toRadians(curupdir)) * netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0), 7.5f)) {
					while (-1 == MovementMath.CheckCollisions(playercol, CollisionList, new Vector3((float) (netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), (float) (Math.sin(Math.toRadians(curupdir))* netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0), 7.5f)) {
						curupdir--;
						if(Math.sin(curupdir)<=0) break;
					}
					curupdir++;
					if(curupdir>0) {
						//MovementMath.addVect(netvect, MovementMath.lengthDir(curupdir, netvect.x));
						//sprite.addPosition(0,MovementMath.lengthDir(Math.toRadians(curupdir), netvect.x).y * Gdx.graphics.getDeltaTime() * SLOWSPEED);
					}
				} else {
				*/

				netvect = MovementMath.addVect(walkvect,jumpvect,gpullvect);

				playercol = MovementMath.DuplicateCirc(sprite.collision);
				if (-1 != MovementMath.CheckCollisions(playercol, CollisionList, new Vector3((float) (netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0, 0), 7.5f)) {
					double sign = Math.abs(netvect.x) / (netvect.x);
					while (-1 == MovementMath.CheckCollisions(playercol, CollisionList, new Vector3((float) (sign), 0, 0), 7.5f)) {
						sprite.addPosition(new Vector3((float) (sign), 0, 0));
						playercol = MovementMath.DuplicateCirc(sprite.collision);
					}
					netvect.x = 0;
					walkvect.x = 0;
					jumpvect.x = 0;
					gpullvect.x = 0;
				}

				sprite.addPosition(new Vector3((float) (netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0, 0));

				playercol = MovementMath.DuplicateCirc(sprite.collision);
				if (-1 != MovementMath.CheckCollisions(playercol, CollisionList, new Vector3(0, (float) (netvect.y * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0), 7.5f)) {
					double sign = Math.abs(netvect.y) / (netvect.y);
					while (-1 == MovementMath.CheckCollisions(playercol, CollisionList, new Vector3(0, (float) (sign), 0), 7.5f)) {
						sprite.addPosition(new Vector3(0, (float) (sign), 0));
						playercol = MovementMath.DuplicateCirc(sprite.collision);
					}
					netvect.y = 0;
					walkvect.y = 0;
					jumpvect.y = 0;
					gpullvect.y = 0;
				}
				sprite.addPosition(new Vector3(0, (float) (netvect.y * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0));

				moverot -= (walkvect.len()*movedir * Gdx.graphics.getDeltaTime() * SLOWSPEED) * 5;

				walkvect.x *= .5f;
				walkvect.y *= .5f;
				jumpvect.x *= .92f;
				jumpvect.y *= .92f;

				x = sprite.x;
				y = sprite.y;
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

		public void setPosition(Vector3 newpos) {
			sprite.setPosition(newpos.x, newpos.y);
			x = newpos.x;
			y = newpos.y;
		}

		public void addPosition(Vector3 addpos) {
			sprite.addPosition(addpos);
			x = sprite.x;
			y = sprite.y;
		}

		public Vector3 getPosition() {
			return new Vector3((float) x, (float) y, 0);
		}

		public FrameworkMO.TextureSet getEyeText() {
			Vector3 addpos = MovementMath.lengthDir((double) gpulldir+Math.toRadians(movedir==-1?-90:90), 1.8f);

			double xpos = sprite.collision.x + addpos.x;
			double ypos = sprite.collision.y - 0.5f + addpos.y;

			return new FrameworkMO.TextureSet(new TextureRegion(new Texture("eyes.png")), xpos, ypos, 10000, (double) gpulldir+Math.toRadians(movedir==-1?-90:90));
		}
	}

	public static class Planet {
		public FrameworkMO.SpriteObjectCirc sprite;
		public Vector3 movevect;
		public double x = 0;
		public double y = 0;
		public double radius = 0;
		public int type = 0;
		public double mass = 0;

		public Planet(Vector3 pos, int type) {
			movevect = new Vector3();
			type = (int)(Math.random()*3);
			radius = 256;
			this.mass = 1000000;
			sprite = new FrameworkMO.SpriteObjectCirc("planet1.png",pos.x,pos.y,256, CollisionList);

			x=sprite.x+radius;
			y=sprite.y+radius;

			PlanetList.add(this);
		}

		public TextureRegion updatePos() {
			sprite.depth = sprite.y;
			return new TextureRegion(sprite.texture);
		}
		public Vector3 getPosition(){
			return new Vector3((float) (x),(float) (y),0);
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
