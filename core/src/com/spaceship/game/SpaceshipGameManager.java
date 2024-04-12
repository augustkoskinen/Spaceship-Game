package com.spaceship.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import sun.jvm.hotspot.gc.shared.Space;

import java.util.ArrayList;

public class SpaceshipGameManager extends Game {
	//needed vars
	SpriteBatch batch;
	public static boolean pause;
	public String launcher;
	public static Player mainplayer;
	public static boolean rocketcrafted = false;
	public static boolean canboardship;
	public static boolean canunboardship;

	//consts
	public static final double MULT_AMOUNT = 55;
	public static double SLOWSPEED = 1;
	public static final double WALK_SPEED = 1*MULT_AMOUNT;
	public static final double JUMP_SPEED = 8f*MULT_AMOUNT;

	//lists
	static ArrayList<PoofCloud> PoofCloudList = new ArrayList<>();
	static ArrayList<Planet> PlanetList = new ArrayList<>();
	static ArrayList<Rocket> RocketList = new ArrayList<>();
	static ArrayList<FrameworkMO.ParticleSet> ParticleList = new ArrayList<>();
	static ArrayList<Item> ItemList = new ArrayList<>();
	static ArrayList CollisionList = new ArrayList<>();
	//static ArrayList<FrameworkMO.SpriteObjectSqr> SprObjSqrList = new ArrayList<>();
	//static ArrayList<FrameworkMO.SpriteObjectCirc> SprObjCircList = new ArrayList<>();

	public SpaceshipGameManager(String launcher){ this.launcher = launcher; }

	@Override
	public void create () {
		batch = new SpriteBatch();

		//set up player
		mainplayer = new Player(new Vector3(512,1032,0));

		//add a planet
		new Planet(new Vector3(512,512,0),1);

		//set game screen
		this.setScreen(new GameScreen(this));
	}

	@Override
	public void render () { super.render(); }
	
	@Override
	public void dispose () {
		batch.dispose();
	}


	//===========>
	//OBJECTS
	//===========>

	//object for a player
	public static class Player {
		//basic vars
		public FrameworkMO.SpriteObjectCirc sprite;
		public double x = 0;
		public double y = 0;
		public int skintype = 1;
		public Inventory inventory;

		//move
		public Vector3 netvect = new Vector3();
		public Vector3 walkvect = new Vector3();
		public Vector3 jumpvect = new Vector3();
		public Vector3 gpullvect = new Vector3();
		public int movedir = 1;
		public double moverot = 0;
		public double lastdir = 1;
		public double gpulldir;
		public Rocket loadedrocket = null;

		//jump
		public int jumpcount = 1;
		public int jumpdir = 0;
		public int jumpdiradd = 0;

		//interaction vars
		public boolean justclicked = false;
		public boolean justtabbed = false;

		public Player(Vector3 pos) {
			//add an inventory
			inventory = new Inventory();

			//create body
			sprite = new FrameworkMO.SpriteObjectCirc("p1body.png", pos.x, pos.y, 8, CollisionList);

			x = pos.x;
			y = pos.y;
		}

		public TextureRegion updatePlayerPos() {
			//update input vars
			justclicked = Gdx.input.justTouched();
			justtabbed = Gdx.input.isKeyJustPressed(Input.Keys.TAB);

			//update inventory
			if(mainplayer.justtabbed&&(!canboardship&&!canunboardship|| inventory.inventoryopen)) {
				inventory.inventoryopen = !inventory.inventoryopen;
				mainplayer.justtabbed = false;
				pause = inventory.inventoryopen;
			}

			//setting up vars
			TextureRegion playertext;
			int rightmove = Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0;
			int leftmove = Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0;
			int netmove = (rightmove-leftmove);
			Circle playercol = MovementMath.DuplicateCirc(sprite.collision);
			playertext = new TextureRegion(new Texture("p" + skintype + "body.png"));

			if (!pause&&loadedrocket==null) {
				//update gravity vars
				gpulldir = MovementMath.pointDir(sprite.getPosition(),SpaceMath.getClosestPlanet(sprite.getPosition(),PlanetList).getPosition());
				double multamount = Gdx.graphics.getDeltaTime() * SLOWSPEED;
				Vector3 gpulltemp = SpaceMath.getNetGravity(sprite.getPosition(), PlanetList,1);
				gpullvect = MovementMath.addVect(gpullvect, new Vector3((float)(gpulltemp.x * multamount),(float)(gpulltemp.y * multamount),0));

				//update walk velocity
				if (netmove != 0) {
					double movespeed = netmove * WALK_SPEED;
					walkvect = new Vector3(walkvect.x+(MovementMath.lengthDir(gpulldir+90,movespeed).x),walkvect.y+(MovementMath.lengthDir(gpulldir+90,movespeed).y),0);
					movedir = netmove;
				}

				//check if player is grounded/allowed to jump
				boolean grounded = MovementMath.CheckCollisions(playercol, CollisionList, MovementMath.lengthDir(gpulldir,1), 7.5f)!=-1;
				if (grounded) {jumpcount = 1; gpullvect = new Vector3();}
				if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && jumpcount > 0) {
					PoofCloudList.add(new PoofCloud(gpulldir+90, new Vector3((float) (sprite.x-8), (float) (sprite.y-8), 0)));

					jumpcount--;
					if (jumpdiradd == 0) {
						jumpdiradd = movedir * 1;
						if (movedir < 0)
							jumpdir = -360;
						else
							jumpdir = 360;
					}

					Vector3 addupvect = MovementMath.lengthDir(gpulldir+180,JUMP_SPEED);
					jumpvect.x = addupvect.x;
					jumpvect.y = addupvect.y;
				}

				if (MovementMath.toDegrees() != -1) lastdir = MovementMath.toDegrees();

				//add vectors for movement
				netvect = MovementMath.addVect(walkvect,jumpvect,gpullvect);

				//move x
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

				//move y
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

				//rotation for movement
				moverot -= (walkvect.len()*movedir * Gdx.graphics.getDeltaTime() * SLOWSPEED) * 5;

				//natural decay
				walkvect.x *= .5f;
				walkvect.y *= .5f;
				jumpvect.x *= .92f;
				jumpvect.y *= .92f;
			}

			x = sprite.getPosition().x;
			y = sprite.getPosition().y;

			//update jump
			sprite.depth = y;
			if (jumpdir != 0) {
				jumpdir += jumpdiradd;
				if (jumpdir == 0) {
					jumpdiradd = 0;
				}
			}

			if(loadedrocket!=null)
				return null;

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

		//get the texture for the player's eyes
		public FrameworkMO.TextureSet getEyeText() {
			Vector3 addpos = MovementMath.lengthDir(gpulldir+(movedir==-1?-90:90), 1.8f);

			double xpos = x + addpos.x;
			double ypos = y - 0.5f + addpos.y;

			return new FrameworkMO.TextureSet(new TextureRegion(new Texture("eyes.png")), xpos, ypos, 10000, gpulldir+(movedir==-1?-90:90));
		}
	}

	//class for the inventory for a player
	public static class Inventory {
		//texture and sprites needed
		private Sprite inventoryspr;
		private Sprite rocketspr;
		private Sprite hotbarspr;
		private Texture hoverslot;
		private Texture takenslot;

		//number of resources for items
		private FrameworkMO.AnimationSet count;

		//arrays
		public Item[][] storage = new Item[3][6];
		public Item[][] accessories = new Item[3][2];
		public Item[][] hotbar = new Item[1][6];
		public ArrayList<Item> CraftList = new ArrayList<>();
		public int hotbarslot = 0;
		public Item dragItem = null;
		public Item hoverItem = null;
		public boolean inventoryopen = false;

		//crafting vars
		public int selected = 0;
		public double menuRot = 0;
		public final double FADE=1; //range 1.9 to 0.1
		public final double SCALE=1; //lower for bigger in front and smaller in back
		public final double CHANGE_SPEED = 5; //lower for faster
		public final double menu_x = 436;
		public final double menu_y = 360;
		public final double MENU_WIDTH = 256;
		public final double MENU_HEIGHT = 32;
		public Inventory() {
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));
			CraftList.add(new Item(1));

			//textures
			rocketspr = new Sprite(new Texture("rocket.png"));
			rocketspr.setScale(1.5f,1.5f);
			rocketspr.setPosition(736/4-6,416/4+176);

			inventoryspr = new Sprite(new Texture("inventory.png"));
			inventoryspr.setScale(2,2);
			inventoryspr.setPosition(736/4,416/4);

			hotbarspr = new Sprite(new Texture("hotbar.png"));
			hotbarspr.setScale(2,2);
			hotbarspr.setPosition(736/4,416/4);
			hoverslot = new Texture("hoverslot.png");
			takenslot = new Texture("takenslot.png");
			count = new FrameworkMO.AnimationSet("numbers.png",17,1,1);

			int ranadd = (int)(Math.random()*3)+2;
			for(int i = 0; i<ranadd;i++) {
				storage[(int)(Math.random()*3)][(int)(Math.random()*6)] = new Item(0);
			}
			ranadd = (int)(Math.random()*7);
			for(int i = 0; i<ranadd;i++) {
				hotbar[0][(int)(Math.random()*6)] = new Item(0);
			}
		}
		public void drawInventory(SpriteBatch batch) {
			batch.begin();
			if(inventoryopen) {
				//hotkeys
				int switchslot = -1;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) switchslot = 0;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) switchslot = 1;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) switchslot = 2;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) switchslot = 3;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) switchslot = 4;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) switchslot = 5;
				inventoryspr.draw(batch);

				//draw/manage accessory inventory
				boolean catchmouse = false;
				for (int i = 0; i < accessories.length; i++) {
					for (int j = 0; j < accessories[i].length; j++) {
						if(accessories[i][j]!=null) {
							batch.draw(new TextureRegion(takenslot),j * 52 + 152, i * 54 + 54, 0, 0, takenslot.getWidth(), takenslot.getHeight(), 2, 2, 0);
						}
						if(Gdx.input.getX()>=j * 52 + 152&&Gdx.input.getX()<=j * 52 + 152 + 32&&Gdx.graphics.getHeight() - Gdx.input.getY()>=i * 54 + 54&&Gdx.graphics.getHeight() - Gdx.input.getY()<=i * 54 + 54+32&&!catchmouse) {
							batch.draw(new TextureRegion(hoverslot), j * 52 + 152, i * 54 + 54, 0, 0, hoverslot.getWidth(), hoverslot.getHeight(), 2, 2, 0);
							catchmouse = true;
							hoverItem = accessories[i][j];

							if(switchslot!=-1) {
								accessories[i][j] = hotbar[0][switchslot];
								hotbar[0][switchslot] = hoverItem;
							}

							if(mainplayer.justclicked&&dragItem==null) {
								mainplayer.justclicked = false;

								dragItem = hoverItem;
								accessories[i][j] = null;
							} else if(mainplayer.justclicked) {
								mainplayer.justclicked = false;

								if(hoverItem==null) {
									accessories[i][j] = dragItem;
									dragItem = null;
								}  else if (hoverItem.stackable&&hoverItem.amount+dragItem.amount<=16) {
									hoverItem.amount += dragItem.amount;
									dragItem = null;
								} else if (hoverItem.stackable&&hoverItem.amount+dragItem.amount>16) {
									dragItem.amount = hoverItem.amount+dragItem.amount-16;
									hoverItem.amount = 16;
								} else {
									accessories[i][j] = dragItem;
									dragItem = hoverItem;
								}
							}
						}
						if(accessories[i][j]!=null) {
							batch.draw(new TextureRegion(accessories[i][j].text),j * 52 + 152, i * 54 + 54);
							count.time = accessories[i][j].amount;
							batch.draw(count.getAnim(),j * 52 + 152, i * 54 + 54, 0, 0, count.getAnim().getRegionWidth(), count.getAnim().getRegionHeight(), 2, 2, 0);
						}
					}
				}

				//draw/manage storage inventory
				for (int i = 0; i < storage.length; i++) {
					for (int j = 0; j < storage[i].length; j++) {
						if(storage[i][j]!=null) {
							batch.draw(new TextureRegion(takenslot),j * 52 + 298, i * 54 + 148, 0, 0, takenslot.getWidth(), takenslot.getHeight(), 2, 2, 0);
						}
						if(Gdx.input.getX()>=j * 52 + 298&&Gdx.input.getX()<=j * 52 + 298 + 32&&Gdx.graphics.getHeight() - Gdx.input.getY()>=i * 54 + 148&&Gdx.graphics.getHeight() - Gdx.input.getY()<=i * 54 + 148 + 32&&!catchmouse) {
							batch.draw(new TextureRegion(hoverslot), j * 52 + 298, i * 54 + 148,0,0,hoverslot.getWidth(),hoverslot.getHeight(),2,2,0);
							catchmouse = true;
							hoverItem = storage[i][j];

							if(switchslot!=-1) {
								storage[i][j] = hotbar[0][switchslot];
								hotbar[0][switchslot] = hoverItem;
							}

							if(mainplayer.justclicked&&dragItem==null) {
								mainplayer.justclicked = false;

								dragItem = hoverItem;
								storage[i][j] = null;
							} else if(mainplayer.justclicked) {
								mainplayer.justclicked = false;

								if(hoverItem==null) {
									storage[i][j] = dragItem;
									dragItem = null;
								} else if (hoverItem.stackable&&hoverItem.amount+dragItem.amount<=16) {
									hoverItem.amount += dragItem.amount;
									dragItem = null;
								} else if (hoverItem.stackable&&hoverItem.amount+dragItem.amount>16) {
									dragItem.amount = hoverItem.amount+dragItem.amount-16;
									hoverItem.amount = 16;
								} else {
									storage[i][j] = dragItem;
									dragItem = hoverItem;
								}
							}
						}
						if(storage[i][j]!=null) {
							batch.draw(new TextureRegion(storage[i][j].text),j * 52 + 298, i * 54 + 148);
							count.time = storage[i][j].amount;
							batch.draw(count.getAnim(),j * 52 + 298, i * 54 + 148, 0, 0, count.getAnim().getRegionWidth(), count.getAnim().getRegionHeight(), 2, 2, 0);
						}
					}
				}

				//draw/manage hotbar inventory
				for (int j = 0; j < hotbar[0].length; j++) {
					if(hotbar[0][j]!=null) {
						batch.draw(new TextureRegion(takenslot),j * 52 + 298, 54, 0, 0, takenslot.getWidth(), takenslot.getHeight(), 2, 2, 0);
					}
					if(Gdx.input.getX()>=j * 52 + 298&&Gdx.input.getX()<=j * 52 + 298 + 32&&Gdx.graphics.getHeight() - Gdx.input.getY()>=54&&Gdx.graphics.getHeight() - Gdx.input.getY()<=54 + 32&&!catchmouse) {
						batch.draw(new TextureRegion(hoverslot), j * 52 + 298, 54,0,0,hoverslot.getWidth(),hoverslot.getHeight(),2,2,0);
						catchmouse = true;
						hoverItem = hotbar[0][j];

						if(switchslot!=-1) {
							hotbar[0][j] = hotbar[0][switchslot];
							hotbar[0][switchslot] = hoverItem;
						}

						if(mainplayer.justclicked&&dragItem==null) {
							mainplayer.justclicked = false;

							dragItem = hoverItem;
							hotbar[0][j] = null;
						} else if(mainplayer.justclicked) {
							mainplayer.justclicked = false;

							if(hoverItem==null) {
								hotbar[0][j] = dragItem;
								dragItem = null;
							} else if (hoverItem.stackable&&hoverItem.amount+dragItem.amount<=16) {
								hoverItem.amount += dragItem.amount;
								dragItem = null;
							} else if (hoverItem.stackable&&hoverItem.amount+dragItem.amount>16) {
								dragItem.amount = hoverItem.amount+dragItem.amount-16;
								hoverItem.amount = 16;
							} else {
								hotbar[0][j] = dragItem;
								dragItem = hoverItem;
							}
						}
					}
					if(hotbar[0][j]!=null) {
						batch.draw(new TextureRegion(hotbar[0][j].text),j * 52 + 298, 54);
						count.time = hotbar[0][j].amount;
						batch.draw(count.getAnim(),j * 52 + 298, 54, 0, 0, count.getAnim().getRegionWidth(), count.getAnim().getRegionHeight(), 2, 2, 0);
					}
				}

				//crafting menu management
				if(Gdx.input.isKeyJustPressed(Input.Keys.A)) selected--;
				if(Gdx.input.isKeyJustPressed(Input.Keys.D)) selected++;
				if(selected>=CraftList.size()) {
					selected = 0;
				}
				if(selected<0) {
					selected = CraftList.size()-1;
				}

				//rotate
				menuRot -= MovementMath.angleDiff(menuRot, selected*(360f/CraftList.size()))/CHANGE_SPEED;

				ArrayList<Sprite> SlotSprites = new ArrayList<>();
				ArrayList<Sprite> ItemSprites = new ArrayList<>();

				Item hovercraft = null;
				//add crafting items for drawing gui
				for(int i = 0; i < CraftList.size(); i++) {
					float sc = (float)Math.abs((1+MovementMath.lengthDir((menuRot+90)+i*(360f/ CraftList.size()),MENU_HEIGHT/SCALE).y/(MENU_HEIGHT*SCALE)))*2;
					float x = (float)(menu_x+MovementMath.lengthDir((menuRot+90)+i*(360f/ CraftList.size()), MENU_WIDTH/2).x);  //x //+(32-(spr.getScaleY()*32)/2)
					float y = (float)(menu_y-MovementMath.lengthDir((menuRot+90)+i*(360f/ CraftList.size()),MENU_HEIGHT/2).y);  //x //+(32-(spr.getScaleY()*32)/2)
					float a = (float)(1+(MovementMath.lengthDir((menuRot+90)+i*(360f/ CraftList.size()),MENU_HEIGHT/FADE).y/(MENU_HEIGHT*FADE)))/(2*(float)FADE);
					boolean hover = false;
					boolean craftable = canCraft(CraftList.get(i),false);

					CraftList.get(i).sortPriority = (float)Math.abs((1+MovementMath.lengthDir((menuRot+90)+i*(360f/ CraftList.size()),MENU_HEIGHT/SCALE).y/(MENU_HEIGHT*SCALE)))*2;

					if((selected != 0 ? CraftList.size()-selected : 0)==i&&
						Gdx.input.getX()>x-24&&
						Gdx.graphics.getHeight()-Gdx.input.getY()>y-24&&
						Gdx.input.getX()<x+sc*16-24&&
						Gdx.graphics.getHeight()-Gdx.input.getY()<y+sc*16-24
					) {
						hovercraft = CraftList.get(i);
						hover = true;
						if(mainplayer.justclicked&&canCraft(CraftList.get(i),true)) {
							mainplayer.justclicked = false;
						}
					}

					Sprite sprslot = new Sprite(new Texture(hover ? "crafthover.png" : (craftable ? "craftslot.png" : "crafttaken.png")));
					sprslot.setScale(sc);
					sprslot.setPosition(x, y);
					sprslot.setAlpha(a);

					Sprite spritem = new Sprite(new Texture("item/"+CraftList.get(i).type+".png"));
					spritem.setScale(sc/2);
					spritem.setPosition(x-8, y-8);
					spritem.setAlpha(a);

					SlotSprites.add(sprslot);
					ItemSprites.add(spritem);
				}

				//draw crafting gui
				FrameworkMO.DrawMenuWithLayering(batch, SlotSprites, ItemSprites);

				if(hovercraft!=null) {
					batch.draw(new Texture("matmenu.png"),480,320,64,64);
					for (int i = 0; i < hovercraft.MaterialList.get(0).size(); i++) {
						batch.draw(new Texture("item/"+hovercraft.MaterialList.get(0).get(i)+".png"),484,364,16,16);
						count.time = hovercraft.MaterialList.get(1).get(i);
						batch.draw(count.getAnim(),494,368,16,16);
					}
				}

				if(dragItem!=null) {
					batch.draw(dragItem.text,Gdx.input.getX()-16,Gdx.graphics.getHeight() - Gdx.input.getY()-16);
					if(dragItem.stackable) {
						count.time = dragItem.amount;
						batch.draw(count.getAnim(), Gdx.input.getX() - 16, Gdx.graphics.getHeight() - Gdx.input.getY() - 16, 0, 0, count.getAnim().getRegionWidth(), count.getAnim().getRegionHeight(), 2, 2, 0);
					}
					if(mainplayer.justclicked) {
						dragItem.drop();
						mainplayer.justclicked = false;
						dragItem = null;
					}
				}
			} else {
				//hotkeys
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) hotbarslot = 0;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) hotbarslot = 1;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) hotbarslot = 2;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) hotbarslot = 3;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_5)) hotbarslot = 4;
				if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) hotbarslot = 5;
				hotbarspr.draw(batch);

				//draw hotbar when not in inventory
				for (int j = 0; j < hotbar[0].length; j++) {
					if(j==hotbarslot)
						batch.draw(new TextureRegion(hoverslot),j * 52 + 222, 52, 0, 0, hoverslot.getWidth(),hoverslot.getHeight(), 2, 2, 0);
					if(hotbar[0][j]!=null) {
						if(j!=hotbarslot)
							batch.draw(new TextureRegion(takenslot),j * 52 + 222, 52, 0, 0, takenslot.getWidth(), takenslot.getHeight(), 2, 2, 0);
						batch.draw(new TextureRegion(hotbar[0][j].text),j * 52 + 222, 52);
						count.time = hotbar[0][j].amount;
						batch.draw(count.getAnim(),j * 52 + 222, 52, 0, 0, count.getAnim().getRegionWidth(), count.getAnim().getRegionHeight(), 2, 2, 0);
					}
				}
			}
			batch.end();
		}

		//add item to inventory
		public Item addItem(Item item) {
			boolean foundspot = false;
			for (int j = 0; j < hotbar[0].length; j++) {
				if(hotbar[0][j]==null&&!foundspot) {
					hotbar[0][j] = item;
					foundspot = true;
				} else if(hotbar[0][j]!=null&&hotbar[0][j].type==item.type&&item.stackable&&item.amount+hotbar[0][j].amount<=16&&!foundspot) {
					hotbar[0][j].amount += item.amount;
					foundspot = true;
				} else if(hotbar[0][j]!=null&&hotbar[0][j].type==item.type&&item.stackable&&item.amount+hotbar[0][j].amount>16&&!foundspot) {
					item.amount = item.amount+hotbar[0][j].amount-16;
					hotbar[0][j].amount = 16;
				}
			}
			for (int i = 0; i < storage.length; i++) {
				for (int j = 0; j < storage[i].length; j++) {
					if(storage[i][j]==null&&!foundspot) {
						storage[i][j] = item;
						foundspot = true;
					} else if(storage[i][j]!=null&&storage[i][j].type==item.type&&item.stackable&&item.amount+storage[i][j].amount<=16&&!foundspot) {
						storage[i][j].amount += item.amount;
						foundspot = true;
					} else if(storage[i][j]!=null&&storage[i][j].type==item.type&&item.stackable&&item.amount+storage[i][j].amount>16&&!foundspot) {
						item.amount = item.amount+storage[i][j].amount-16;
						storage[i][j].amount = 16;
					}
				}
			}
			return (foundspot ? null : item);
		}

		//remove item from inventory
		public void removeItem(int type, int count) {
			int tempcount = count;
			for (int j = 0; j < hotbar[0].length; j++) {
				if(tempcount>0) {
					if (hotbar[0][j] != null && hotbar[0][j].type == type && hotbar[0][j].stackable && hotbar[0][j].amount - tempcount > 0) {
						hotbar[0][j].amount -= tempcount;
						tempcount = 0;
					} else if (hotbar[0][j] != null && hotbar[0][j].type == type && hotbar[0][j].stackable && hotbar[0][j].amount - tempcount <= 0) {
						hotbar[0][j].amount = 0;
						tempcount -= 16;
					} else if (hotbar[0][j] != null && hotbar[0][j].type == type && !hotbar[0][j].stackable) {
						hotbar[0][j] = null;
					}
				}
			}
			for (int i = 0; i < storage.length; i++) {
				for (int j = 0; j < storage[i].length; j++) {
					if(tempcount>0) {
						if (storage[i][j] != null && storage[i][j].type == type && storage[i][j].stackable && storage[i][j].amount - tempcount > 0) {
							storage[i][j].amount -= tempcount;
							tempcount = 0;
						} else if (storage[i][j] != null && storage[i][j].type == type && storage[i][j].stackable && storage[i][j].amount - tempcount <= 0) {
							storage[i][j].amount = 0;
							tempcount -= 16;
						} else if (storage[i][j] != null && storage[i][j].type == type && !storage[i][j].stackable) {
							storage[i][j] = null;
						}
					}
				}
			}
		}

		//check if an item is in the inventory
		public int checkItem(int type) {
			int count = 0;
			for (int j = 0; j < hotbar[0].length; j++) {
				if(hotbar[0][j]!=null&&type==hotbar[0][j].type) {
					count+=hotbar[0][j].amount;
				}
			}
			for (int i = 0; i < storage.length; i++) {
				for (int j = 0; j < storage[i].length; j++) {
					if(storage[i][j]!=null&&type==storage[i][j].type) {
						count+=storage[i][j].amount;
					}
				}
			}
			return (count);
		}

		//check if an item can be crafted
		public boolean canCraft(Item item, boolean craft) {
			if (!rocketcrafted&&item!=null) {
				boolean havemats = true;
				for (int i = 0; i < item.MaterialList.get(0).size(); i++) {
					if(checkItem(item.MaterialList.get(0).get(i))<item.MaterialList.get(1).get(i)) {
						havemats = false;
					}
				}

				if(craft&&havemats) {
					switch (item.type) {
						case 1 : {
							RocketList.add(new Rocket(mainplayer.getPosition(), mainplayer.gpulldir + 180));
							rocketcrafted = true;
							break;
						}
					}
					for (int i = 0; i < item.MaterialList.get(0).size(); i++) {
						removeItem(item.MaterialList.get(0).get(i), item.MaterialList.get(1).get(i));
					}
				}
				return havemats;
			}
			return false;
		}
	}

	//class for items in inventory, on the ground, and in the crafting menu
	public static class Item {
		public boolean ininventory = true;
		public double pickupable = 0;
		public Circle collision = new Circle(0,0,4);
		public Vector3 gvect = new Vector3();
		public double gvectdir = 0;

		//needed vars
		public int type;
		public Texture text;
		public boolean stackable;
		public int amount = 1;

		//lists
		public ArrayList<ArrayList<Integer>> MaterialList = new ArrayList<>(); //0 = item, 1 = num

		public double sortPriority = 0;
		public Item(int t) {
			type = t;
			MaterialList.add(new ArrayList<Integer>());
			MaterialList.add(new ArrayList<Integer>());

			switch (type) {
				case 0 : {
					stackable = true;
					break;
				}
				case 1 : {
					stackable = false;
					MaterialList.get(0).add(0);
					MaterialList.get(1).add(16);
					break;
				}
			}

			//if(stackable) amount = (int)(Math.random()*16)+1;
			text = new Texture("item/"+type+".png");
		}
		public FrameworkMO.TextureSet updatePosition() {
			//check gravity vectors
			gvect.add(SpaceMath.getNetGravity(new Vector3(collision.x,collision.y,0),PlanetList,.01f));
			Planet nearplanet = SpaceMath.getClosestPlanet(new Vector3(collision.x,collision.y,0),PlanetList);
			gvectdir = MovementMath.pointDir(new Vector3(collision.x,collision.y,0), nearplanet.getPosition());

			if(pickupable>=0) {
				pickupable-=Gdx.graphics.getDeltaTime();
			}

			//add x
			Circle col = MovementMath.DuplicateCirc(collision);
			if (MovementMath.overlaps(col, nearplanet.sprite.collision, new Vector3((float) (gvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0, 0))) {
				double sign = Math.abs(gvect.x) / (gvect.x);
				while (!MovementMath.overlaps(col, nearplanet.sprite.collision, new Vector3((float) (sign), 0, 0))) {
					collision.x += sign;
					col = MovementMath.DuplicateCirc(collision);
				}
				gvect.x = 0;
			}
			collision.x += gvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED;

			//add y
			col = MovementMath.DuplicateCirc(collision);
			if (MovementMath.overlaps(col, nearplanet.sprite.collision, new Vector3(0, (float) (gvect.y * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0))) {
				double sign = Math.abs(gvect.y) / (gvect.y);
				while (!MovementMath.overlaps(col, nearplanet.sprite.collision, new Vector3(0, (float) (sign), 0))) {
					collision.y += sign;
					col = MovementMath.DuplicateCirc(collision);
				}
				gvect.y = 0;
			}
			collision.y += gvect.y * Gdx.graphics.getDeltaTime() * SLOWSPEED;

			return new FrameworkMO.TextureSet(new TextureRegion(text),collision.x,collision.y);
		}

		//drop item form inventory
		public void drop() {
			ItemList.add(this);
			collision.setPosition((float)mainplayer.x,(float)mainplayer.y);
			pickupable = 2;
			ininventory = false;
		}

		//drop item form inventory at a pos
		public void drop(Vector3 pos) {
			ItemList.add(this);
			collision.setPosition((float)pos.x,(float)pos.y);
			pickupable = .5;
			ininventory = false;
		}

		//gain item into inventory
		public boolean gain() {
			if(pickupable<=0) {
				ItemList.remove(this);
				mainplayer.inventory.addItem(this);
				ininventory = true;
				return true;
			}
			return false;
		}
	}

	//class for planets
	public static class Planet {
		static ArrayList<FrameworkMO.DestroyableObject> TreeList = new ArrayList<>();
		public FrameworkMO.SpriteObjectCirc sprite;
		public Vector3 movevect;
		public double x = 0;
		public double y = 0;
		public double radius = 0;
		public int type = 0;
		public double mass = 0;

		public Planet(Vector3 pos, int type) {
			movevect = new Vector3();
			this.type = type;

			switch (type) {
				case 0 : {
					radius = 256;
					this.mass = 100;
					break;
				}
				case 1: {
					radius = 512;
					this.mass = 1950;
					break;
				}
			}

			sprite = new FrameworkMO.SpriteObjectCirc("planet"+type+".png", pos.x, pos.y, radius, CollisionList);

			x=pos.x;
			y=pos.y;

			for(int i = 0; i<50; i++) {
				double ranangle = Math.random() * 360;
				Vector3 ranpos = MovementMath.lengthDir(ranangle, radius+29);
				TreeList.add(new FrameworkMO.DestroyableObject("deadtree.png", x + ranpos.x, y + ranpos.y, 16, 64, 0, 0, ranangle, null));
			}

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

	//class for clouds formed by jumps
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

	//class for a rocket
	public static class Rocket {
		public FrameworkMO.ParticleSet flamePS;
		boolean loaded = false;
		public Player player = null;
		FrameworkMO.SpriteObjectSqr sprite;
		public Vector3 position;
		public Vector3 velocity;
		public double thrust = 0;
		public double rot = 0;
		public double gpointdir = 0;
		public boolean onground = false;
		public Rocket(Vector3 pos, double rot) {
			this.rot = rot+90;
			position = pos.add(MovementMath.lengthDir(rot-90,24));
			sprite = new FrameworkMO.SpriteObjectSqr("rocket.png",pos.x,pos.y,32,64,0,0, rot+90, null);
			velocity = new Vector3();
			flamePS = new FrameworkMO.ParticleSet(1,position,PlanetList,rot);

		}
		public FrameworkMO.TextureSet updateSpeed() {
			//gravity vectors
			Vector3 gvectdir = SpaceMath.getNetGravity(position, PlanetList, 2);
			gpointdir = MovementMath.pointDir(position, SpaceMath.getClosestPlanet(position,PlanetList).getPosition());
			Planet nearplanet = SpaceMath.getClosestPlanet(position, PlanetList);
			onground = MovementMath.overlaps(nearplanet.sprite.collision,sprite.collision,gpointdir,MovementMath.lengthDir(gpointdir,1));
			canboardship = false;
			canunboardship = false;

			if(loaded) {
				//check inputs for movement
				if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
					thrust += 20;
					int repetitions = (int)(Math.random()*20);
					for (int i = 0; i < repetitions; i++)
						flamePS.addParticle(1, MovementMath.addVect(position,MovementMath.lengthDir(rot,32)), PlanetList, rot);
				}
				thrust *= .5;
				if (Gdx.input.isKeyPressed(Input.Keys.D) && !onground) {
					rot -= 5;
				}
				if (Gdx.input.isKeyPressed(Input.Keys.A) && !onground) {
					rot += 5;
				}

				velocity = MovementMath.addVect(MovementMath.lengthDir(rot + 180, thrust), velocity);

				if(loaded){
					canunboardship = true;
					if(mainplayer.justtabbed&&!mainplayer.inventory.inventoryopen) {
						unload();
						mainplayer.justtabbed = false;
					}
				}
			} else {
				if(MovementMath.overlaps(mainplayer.sprite.collision, sprite.collision, rot)&&!loaded) {
					canboardship = true;
					if(mainplayer.justtabbed&&!mainplayer.inventory.inventoryopen) {
						load(mainplayer);
						mainplayer.justtabbed = false;
					}
				}
				rot = (rot+gpointdir)/2;
				Vector3 escapepos = MovementMath.lengthDir(gpointdir+180,1);
				while(MovementMath.overlaps(nearplanet.sprite.collision, sprite.collision, rot)) {
					position.add(escapepos);
					sprite.addPosition(escapepos);
				}
			}

			Vector3 netvect = new Vector3(gvectdir.x + velocity.x, gvectdir.y + velocity.y, 0);
			if (thrust > 10) {
				netvect = new Vector3(velocity.x, velocity.y, 0);
			}

			//add x
			Rectangle col = MovementMath.DuplicateRect(sprite.collision);
			if (MovementMath.overlaps(nearplanet.sprite.collision, col, rot, new Vector3((float) (netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0, 0))) {
				double sign = Math.abs(netvect.x) / (netvect.x);
				double sum = 0;
				if(!Double.isNaN(sign))
					while (!MovementMath.overlaps(nearplanet.sprite.collision, col, rot, new Vector3((float) (sign), 0, 0))) {
						sprite.addPosition(new Vector3((float)sign,0,0));
						col = MovementMath.DuplicateRect(sprite.collision);
						sum+=sign;
						if(sum>netvect.x) break;
					}
				netvect.x = 0;
			}
			sprite.addPosition(new Vector3((float)(netvect.x * Gdx.graphics.getDeltaTime() * SLOWSPEED),0,0));

			//add y
			col = MovementMath.DuplicateRect(sprite.collision);
			if (MovementMath.overlaps(nearplanet.sprite.collision, col, rot, new Vector3(0, (float) (netvect.y * Gdx.graphics.getDeltaTime() * SLOWSPEED), 0))) {
				double sign = Math.abs(netvect.y) / (netvect.y);
				double sum = 0;
				if(!Double.isNaN(sign))
					while (!MovementMath.overlaps(nearplanet.sprite.collision, col, rot, new Vector3(0, (float) (sign), 0))) {
						sprite.addPosition(new Vector3(0, (float)sign,0));
						col = MovementMath.DuplicateRect(sprite.collision);
						sum+=sign;
						if(sum>netvect.y) break;
					}
				netvect.y = 0;
			}
			sprite.addPosition(new Vector3(0,(float)(netvect.y * Gdx.graphics.getDeltaTime() * SLOWSPEED),0));

			//decay
			velocity.x *= .975f;
			velocity.y *= .975f;

			position = sprite.getPosition();
			sprite.rotation = rot;

			return new FrameworkMO.TextureSet(new TextureRegion(sprite.texture),position.x,position.y,-position.y,rot);
		}

		//load the player
		public void load(Player player) {
			if(!loaded) {
				this.player = player;
				loaded = true;
				player.loadedrocket = this;
				sprite.texture = new Texture("rocketloaded.png");
				player.jumpcount = 0;
			}
		}

		//unload the player
		public void unload() {
			if(loaded) {
				player.loadedrocket = null;
				player.setPosition(position);
				player.walkvect = new Vector3();
				player.jumpvect = new Vector3();
				player = null;
				loaded = false;
				velocity = new Vector3();
				sprite.texture = new Texture("rocket.png");
			}
		}
	}
}
