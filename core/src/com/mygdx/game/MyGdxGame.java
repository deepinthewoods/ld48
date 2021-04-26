package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;

import sun.nio.cs.ext.MacHebrew;

import static com.mygdx.game.Rock.SPIKE_BOTH;
import static com.mygdx.game.Rock.SPIKE_LEFT;
import static com.mygdx.game.Rock.SPIKE_NONE;
import static com.mygdx.game.Rock.SPIKE_RIGHT;

public class MyGdxGame extends ApplicationAdapter {
	private static final String TAG = "game";
	private static final int MAP_WIDTH = 120, MAP_HEIGHT = 200;
	private static final int CAMERA_WIDTH = 40;

	private static final float LEVEL_TEXT_SHOW_TIME = 3f;

	private static final float SHOTGUN_SPEED = 50;
	private static final float SHOTGUN_SCATTER_ANGLE = 15;
	protected static final float SHOTGUN_PARTICLE_SIZE = .21f;
	private static final float RECOVERY_TIME = .5f;
	private static final int SHOTGUN_PARTICLES = 15;
	private static final int SHOTGUN_NO_AMMO_PARTICLES = 2;
	private static final float RECOIL_IMPULSE = 10f;
	private static final float PLAYER_DAMAGE_TIMEOUT = .5f;
	private static final float PLAYER_DAMAGE_FLASH_TIME = .1f;


	protected static final short SHOT_GROUP = 4;
	protected static final short PLAYER_GROUP = 2;
	protected static final short BAT_GROUP = 8;
	public static final short GATE_GROUP = 16;
	public static final short LEVER_GROUP = 32;
	public static final short ITEMS_GROUP = 64;
	public static final short HAZARD_GROUP = 128;
	public static final short SHROOM_GROUP = 256;
	public static final short SPORE_GROUP = 256;

	protected static final float LEVER_TIME = 1f;
	protected static final float GRAB_TIME = .2f;
	private static final float GATE_TIME = 1f;
	private static final float GRAB_DISTANCE2 = 10*10;
	private static final int AMMO_CHANCE = 20, MEDKIT_CHANCE = 10;
	private static final int SPIKE_CHANCE_MAX = 50;

	private static final int STATUS_SIZE = 20;
	private static final float WALK_SPEED = 20;
	private static final int MEDKIT_INCREMENT = 5, AMMO_INCREMENT = 5;
	private static final int BATS_PER_GATE_MAX = 10, BATS_PER_GATE_MIN = 0;
	private static final int BATS_MAX = 120, BATS_MIN = 0;
	private static final int SHROOMS_CHANCE_MIN = 0, SHROOMS_CHANCE_MAX = 50;
	private static final int LEVELS = 6;

	private static final Color ROPE_COLOR = new Color(.8f, .8f, 0f, 1f);
	private static final float DEATH_FADE_TIME = 2f;
	public static final float SHROOM_SPORE_TIME = 2f;
	public static final float SHROOM_RANGE2 = 10 * 10;
	private static final float SPORE_SPEED = 2;
	private static final float SHROOM_VEL_VARIANCE = 1;
	private static final int SPORES_PER_SHROOM = 6;
	private static final float TIME_STEP = 1f / 130f;
	private static final int VELOCITY_ITERATIONS = 3, POSITION_ITERATIONS = 1;
	public static Vector3 showRopePoint = new Vector3(0, 100, 0);



	SpriteBatch batch;

	Texture img;

	private World world;
	private Box2DDebugRenderer debugRenderer;
	private OrthographicCamera camera;
	private Body player;
	private InputMultiplexer mux;
	private int playerMove, playerJump;
	private final int MOVE_LEFT = -1, MOVE_RIGHT = 1, MOVE_NONE = 0;
	private final int STAND = 0, JUMP = 1;
	private Array<Rock> rocks;
	private Rope rope, pickRope;
	private Body anchor;
	private ShapeRenderer shape;
	private TextureAtlas atlas;
	private Sprite[] rockSprite;
	private Animation<Sprite> playerAnimBack, playerAnimForward, playerAnimStill, playerAnim;
	private Animation<Sprite> batAnim;
	private Array<Bat> bats;
	private BodyDef shotD;
	private FixtureDef shotF;
	private Array<Shot> shots;
	private Sprite shotSprite;
    private Array<Shot> destroyShots;
    private Array<Bat> destroyBats;
	private Player playerClass;
	private Animation<Sprite> leverAnim;
	private Animation<Sprite> gateAnim;
	private Array<Gate> gates;
	private Array<Lever> levers;
	private Sprite ammoSprite;
	private Sprite medKitSprite;
	private Array<MedKit> medKits;
	private Array<Ammo> ammos;
    private PlayerFoot playerFoot;
	private Rock playerTouchingGround;
	private Array<Ammo> destroyAmmos;
	private Array<MedKit> destroyMedKits;
	private Sprite ammoBarSprite;
	private Sprite healthBarSprite;
	private Sprite obscureSprite;
	private Array<Spike> spikes;
	private Sprite spikeSprite;
	private float playerTime, impulseTime, damageTime;
	private float deathTime = 100;
	private boolean hasDied = true;

	public int startAmmo = 5, ammo = startAmmo, maxHealth = 5, health = 0;
	private Color batchColor = new Color();
	private float fadeAlpha;
	private BitmapFont font;
	private LevelFinish levelFinish = new LevelFinish();
	private int currentLevel;
	private boolean qNextLevel;
	private float levelShowTime = LEVEL_TEXT_SHOW_TIME;
	private Sprite sporeSprite;
	private Animation<Sprite> shroomAnim;
	private Array<Shroom> shrooms;
	private Array<Spore> spores;
	private Array<Spore> destroySpores;
	private Preferences prefs;
	private int maxLevel;
	private float shootAngle;
	private Animation<Sprite> playerArmAnim;
    private Sprite[] backSprites;
    private Vector2[] backPositions;
	private Color backgroundColor = new Color(.0612f, .0612f, .0612f, 1f);
	private Sprite bottomSprite;
	private Sound[] shootSnd;
	private Sound healSnd;
	private Sound damageSnd;
	private Sound ammoSnd;
	private Sound[] noAmmoShootSnd;
	private BitmapFont titleFont;
	private Music[] music;
	private Music currentMusic;
	private Music introMusic;
	private boolean firstRun = true;


	@Override
	public void create () {
		shootSnd = new Sound[8];
		for (int i = 0; i < 8; i++)
			shootSnd[i] = Gdx.audio.newSound(Gdx.files.internal("shoot"+i+".wav"));
		noAmmoShootSnd = new Sound[3];
		for (int i = 0; i < 3; i++)
			noAmmoShootSnd[i] = Gdx.audio.newSound(Gdx.files.internal("gun"+i+".wav"));
		healSnd = Gdx.audio.newSound(Gdx.files.internal("heal.wav"));
		damageSnd = Gdx.audio.newSound(Gdx.files.internal("hit.wav"));
		ammoSnd = Gdx.audio.newSound(Gdx.files.internal("ammo.wav"));
		music = new Music[2];
		music[0] = Gdx.audio.newMusic(Gdx.files.internal("LD fast.wav"));
		music[1] = Gdx.audio.newMusic(Gdx.files.internal("LD chill2.wav"));
		music[0].setLooping(true);
		music[1].setLooping(true);
		introMusic = Gdx.audio.newMusic(Gdx.files.internal("LD intro.wav"));
		introMusic.setLooping(true);
		currentMusic = introMusic;
		currentMusic.play();
		//music[0].play();

		prefs = Gdx.app.getPreferences("prefs.info");
		maxLevel = prefs.getInteger("maxLevel", 1);

		atlas = new TextureAtlas("tiles.atlas");
		titleFont= new BitmapFont(Gdx.files.internal("font-title-export.fnt"));
		font= new BitmapFont(Gdx.files.internal("font-export.fnt"));
		//font.getData().setScale(3f);
		rockSprite = new Sprite[4];
		for (int i = 0; i < rockSprite.length; i++){
			rockSprite[i] = atlas.createSprite("stone"+ i);
			rockSprite[i].setSize(8, 8);
		}

		backSprites = new Sprite[4];
		for (int i = 0; i < backSprites.length; i++){
		    backSprites[i] = atlas.createSprite("back"+i);
		    backSprites[i].setSize(8, 8);
		    backSprites[i].setColor(backgroundColor);
        }

        backPositions = new Vector2[100];
		for (int i = 0; i < backPositions.length; i++){
		    backPositions[i] = new Vector2(MathUtils.random(MAP_WIDTH), -MathUtils.random(MAP_HEIGHT));
        }

        bottomSprite = atlas.createSprite("bottom");
		bottomSprite.setSize(MAP_WIDTH*2.5f, 20);
		bottomSprite.setPosition(-MAP_WIDTH, -MAP_HEIGHT*2-6);


		Array<Sprite> playerAnimFrames = atlas.createSprites("player");
		for (Sprite f : playerAnimFrames)
			f.setSize(8, 8);
		Array<Sprite> pFf = new Array<Sprite>(playerAnimFrames);
		pFf.removeRange(10, 19);
		Array<Sprite> pFb = new Array<Sprite>(playerAnimFrames);
		pFb.removeRange(0, 9);
		Array<Sprite> pFs = new Array<Sprite>(playerAnimFrames);
		pFs.removeRange(11, 19);
		pFs.removeRange(0, 9);

		playerAnimBack = new Animation<Sprite>(.02f, pFb);
		playerAnimBack.setPlayMode(Animation.PlayMode.NORMAL);
		playerAnimForward = new Animation<Sprite>(.02f, pFf);
		playerAnimForward.setPlayMode(Animation.PlayMode.REVERSED);
		playerAnimStill = new Animation<Sprite>(.4f, pFs);
		playerAnim = playerAnimStill;

		playerAnimStill = new Animation<Sprite>(.4f, atlas.createSprites("playerdangle"));
		for (Sprite s : playerAnimStill.getKeyFrames())
			s.setSize(8, 8);
		playerAnimStill.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

		playerArmAnim = new Animation<Sprite>(18f, atlas.createSprites("playerarm"));
		playerArmAnim.setPlayMode(Animation.PlayMode.NORMAL);
		for (Sprite s : playerArmAnim.getKeyFrames())
			s.setSize(8, 8);


		Array<Sprite> batFrames = atlas.createSprites("bat");
		for (Sprite frame : batFrames)
			frame.setSize(8, 8);
		batAnim = new Animation<Sprite>(.01f, batFrames);
		batAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

		shotSprite = atlas.createSprite("shotgunparticle");
		shotSprite.setSize(SHOTGUN_PARTICLE_SIZE*2, SHOTGUN_PARTICLE_SIZE*2);

		leverAnim = new Animation<Sprite>(LEVER_TIME/20f, atlas.createSprites("lever"));
		leverAnim.setPlayMode(Animation.PlayMode.NORMAL);
		for (Sprite s : leverAnim.getKeyFrames())
			s.setSize(8, 8);

		gateAnim = new Animation<Sprite>(GATE_TIME/20f, atlas.createSprites("gate"));
		gateAnim.setPlayMode(Animation.PlayMode.NORMAL);
		for (Sprite s : gateAnim.getKeyFrames())
			s.setSize(8, 8);

		ammoSprite = atlas.createSprite("ammo");
		ammoSprite.setSize(Ammo.SIZE*2, Ammo.SIZE);

		medKitSprite = atlas.createSprite("medkit");
		medKitSprite.setSize(MedKit.SIZE*2, MedKit.SIZE);

		ammoBarSprite = atlas.createSprite("ammobar");
		healthBarSprite = atlas.createSprite("healthbar");
		obscureSprite = atlas.createSprite("obscure");

		spikeSprite = atlas.createSprite("spike0");
		spikeSprite.setSize(8, 8);
		spikeSprite.setOrigin(4, 4);

		sporeSprite = atlas.createSprite("spore");
		sporeSprite.setSize(Spore.SIZE*2, Spore.SIZE*2);
		sporeSprite.setOrigin(Spore.SIZE, Spore.SIZE);

		shroomAnim = new Animation<Sprite>(.01f, atlas.createSprites("shroom"));
		shroomAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
		for (Sprite s : shroomAnim.getKeyFrames()){
			s.setSize(8, 8);
		}
		batch = new SpriteBatch();
		shape = new ShapeRenderer();
		img = new Texture("badlogic.jpg");
		world = new World(new Vector2(0, -20f), true);
		debugRenderer = new Box2DDebugRenderer();

		camera = new OrthographicCamera(1, 1);
		resize(1, 1);
		rocks = new Array<Rock>();
		bats = new Array<Bat>();
		shots = new Array<Shot>();
		gates = new Array<Gate>();
		levers = new Array<Lever>();
		medKits = new Array<MedKit>();
		ammos = new Array<Ammo>();
		spikes = new Array<Spike>();
		shrooms = new Array<Shroom>();
		spores = new Array<Spore>();

		destroyShots = new Array<Shot>();
		destroySpores = new Array<Spore>();
		destroyBats = new Array<Bat>();
		destroyAmmos = new Array<Ammo>();
		destroyMedKits = new Array<MedKit>();
		playerClass = new Player();
        playerFoot = new PlayerFoot();
		createWorld(world);
		world.setContactListener(new ContactListener() {
			

			@Override
			public void beginContact(Contact contact) {

				Object a = contact.getFixtureA().getUserData();
				Object b = contact.getFixtureB().getUserData();
				//if (a != null && b != null)Gdx.app.log(TAG, "coll " + a.getClass() + b.getClass());
				if (a instanceof Shot && b instanceof LevelFinish){
					destroyShots.add((Shot) a);
				}
				if (b instanceof Shot && a instanceof LevelFinish){
					destroyShots.add((Shot) b);
				}

				if (a instanceof Shot && b instanceof Spore){
					destroyShots.add((Shot) a);
					destroySpores.add((Spore) b);
				}
				if (b instanceof Shot && a instanceof Spore){
					destroyShots.add((Shot) b);
					destroySpores.add((Spore) a);
				}

				if (a instanceof Player && b instanceof Spore){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
						destroySpores.add((Spore) b);
					}
				}
				if (b instanceof Player && a instanceof Spore){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
						destroySpores.add((Spore) a);
					}
				}
				if (a instanceof PlayerFoot && b instanceof Spore){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
						destroySpores.add((Spore) b);
					}
				}
				if (b instanceof PlayerFoot && a instanceof Spore){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
						destroySpores.add((Spore) a);
					}
				}
				if (a instanceof Shot && b instanceof Shroom){
					Shroom s = (Shroom) b;
					if (!s.isDone && s.body.getPosition().dst(player.getPosition()) < CAMERA_WIDTH)s.sporesQueued = true;

					destroyShots.add((Shot) a);
				}
				if (b instanceof Shot && a instanceof Shroom){
					Shroom s = (Shroom) a;
					if (!s.isDone && s.body.getPosition().dst(player.getPosition()) < CAMERA_WIDTH)s.sporesQueued = true;
					destroyShots.add((Shot) b);
				}


				if (a instanceof PlayerFoot && b instanceof LevelFinish){
					//Gdx.app.log(TAG, "level finish a " + currentLevel);
					if (!hasDied) qNextLevel = true;
				}
				if (b instanceof PlayerFoot && a instanceof LevelFinish){
					//Gdx.app.log(TAG, "level finish a " + currentLevel);
					if (!hasDied) qNextLevel = true;

				}

				if (a instanceof Player && b instanceof Bat){
					//Gdx.app.log(TAG, "cplayer vs bat");
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
					}
				}
				if (b instanceof Player && a instanceof Bat){
					//Gdx.app.log(TAG, "player vs bat");
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
					}
				}



				if (b instanceof PlayerFoot && a instanceof Rock){
					Gdx.app.log(TAG, "player rock");
				}
				if (a instanceof PlayerFoot && b instanceof Rock){
					//Gdx.app.log(TAG, "rock player " );
					playerTouchingGround = (Rock)b;
//					v.set(player.getLinearVelocity());
//					if (v.x < 0) v.x = -WALK_SPEED;
//					else v.x = WALK_SPEED;
					v.set(WALK_SPEED, 0).scl(((Rock)b).isLeft?-1:1);
					player.setLinearVelocity(v);
					
				}

				if (a instanceof Shot && b instanceof Bat){
					//Gdx.app.log(TAG, "shot vs bat");
					destroyShots.add((Shot)a);
					destroyBats.add((Bat)b);

				}else if (a instanceof Shot && b instanceof Lever){
					//Gdx.app.log(TAG, "lever vs shot");
					destroyShots.add((Shot)a);
					Lever lever = (Lever) b;
					if (lever.body.getPosition().dst(player.getPosition()) < CAMERA_WIDTH) lever.hit();
				}
				else if (a instanceof Shot && b == null){
					destroyShots.add((Shot)a);
				}


				if (b instanceof Shot && a instanceof Bat){

					destroyShots.add((Shot)b);
					destroyBats.add((Bat)a);
				} else if (b instanceof Shot && a instanceof Lever){
					//Gdx.app.log(TAG, "lever vs shot");
					destroyShots.add((Shot)b);
					Lever lever = (Lever) a;
					if (lever.body.getPosition().dst(player.getPosition()) < CAMERA_WIDTH)lever.hit();
				}
				else if (b instanceof Shot && a == null){
					destroyShots.add((Shot)b);
				}

				if (a instanceof Player && b instanceof Spike){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
					}
				}
				if (b instanceof  Player && a instanceof  Spike){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
					}
				}
				if (a instanceof PlayerFoot && b instanceof Spike){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
					}
				}
				if (b instanceof  PlayerFoot && a instanceof  Spike){
					if (damageTime > PLAYER_DAMAGE_TIMEOUT){
						damageTime = 0f;
						health--;
						if (health > 0) damageSnd.play();
					}
				}
			}

			@Override
			public void endContact(Contact contact) {
				Object a = contact.getFixtureA().getUserData();
				Object b = contact.getFixtureB().getUserData();
				if (b instanceof PlayerFoot && a instanceof Rock){
					//Gdx.app.log(TAG, "off player rock");
				}
				if (a instanceof PlayerFoot && b instanceof Rock){
					//Gdx.app.log(TAG, "off rock player " );
					playerTouchingGround = null;
				}
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				Object a = contact.getFixtureA().getUserData();
				Object b = contact.getFixtureB().getUserData();
				if (b instanceof PlayerFoot && a instanceof Ammo){
					//Gdx.app.log(TAG, "player ammo");
					contact.setEnabled(false);
					//destroyAmmos.add((Ammo) a);
					((Ammo) a).hit();
					//((Ammo)a).hit();
				}
				if (b instanceof PlayerFoot && a instanceof MedKit){
					//Gdx.app.log(TAG, "player medkit");
					contact.setEnabled(false);
					//destroyMedKits.add((MedKit) a);

					((MedKit)a).hit();
				}
				if (a instanceof PlayerFoot && b instanceof Ammo){
					//Gdx.app.log(TAG, "player ammo");
					contact.setEnabled(false);
					((Ammo) b).hit();
					//destroyAmmos.add((Ammo) b);
					//((Ammo)a).hit();
				}
				if (a instanceof PlayerFoot && b instanceof MedKit){
					//Gdx.app.log(TAG, "player medkit");
					contact.setEnabled(false);

					//estroyMedKits.add((MedKit) b);
					((MedKit)b).hit();
				}
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}
		});

		mux = new InputMultiplexer();
		mux.addProcessor(new InputProcessor() {
			@Override
			public boolean keyDown(int keycode) {
				switch (keycode){
					case Input.Keys.A:
						//playerMove = MOVE_LEFT;
						break;
					case Input.Keys.D:
						//playerMove = MOVE_RIGHT;
						break;
					case Input.Keys.S:
					case Input.Keys.W:
					case Input.Keys.SPACE:
						/*if (hasDied){
							currentLevel = 0;
							startLevel(currentLevel);
							levelShowTime = LEVEL_TEXT_SHOW_TIME;
						}*/
						playerJump = JUMP;

						break;
				}
				return false;
			}



			@Override
			public boolean keyUp(int keycode) {
				switch (keycode){
					case Input.Keys.A:
						if (!Gdx.input.isKeyPressed(Input.Keys.D))
							playerMove = MOVE_NONE;
						else playerMove = MOVE_RIGHT;
						break;
					case Input.Keys.D:
						if (!Gdx.input.isKeyPressed(Input.Keys.A))
							playerMove = MOVE_NONE;
						else playerMove = MOVE_LEFT;
						break;
					case Input.Keys.S:
					case Input.Keys.W:
					case Input.Keys.SPACE:
						playerJump = STAND;
						rope.stopExtending(world);
						break;
				}
				return false;
			}

			@Override
			public boolean keyTyped(char character) {
				return false;
			}

			@Override
			public boolean touchDown(int screenX, int screenY, int pointer, int button) {
				if (hasDied){
					if (deathTime < DEATH_FADE_TIME) return true;
					int level = screenY / (Gdx.graphics.getHeight()/4);
					level = level * 2 -1;
					if (level > maxLevel) return true;
					currentLevel = level;
					startLevel(currentLevel);
					levelShowTime = LEVEL_TEXT_SHOW_TIME;
					return true;
				}
				v.set(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2).sub(screenX, Gdx.graphics.getHeight() - screenY);
				v.nor().scl(-1);
				shootShotgun(v.x, v.y);
				return false;
			}

			@Override
			public boolean touchUp(int screenX, int screenY, int pointer, int button) {
				return false;
			}

			@Override
			public boolean touchDragged(int screenX, int screenY, int pointer) {
				return false;
			}

			@Override
			public boolean mouseMoved(int screenX, int screenY) {
				return false;
			}

			@Override
			public boolean scrolled(int amount) {
				return false;
			}
		});
		Gdx.input.setInputProcessor(mux);

	}

	private void startLevel(int currentLevel) {

		for (Bat a : bats)
			world.destroyBody(a.body);
		bats.clear();
		for (Spike a : spikes)
			world.destroyBody(a.body);
		spikes.clear();
		for (Shot a : shots)
			world.destroyBody(a.body);
		shots.clear();
		for (Gate a : gates)
			world.destroyBody(a.body);
		gates.clear();
		for (Lever a : levers)
			world.destroyBody(a.body);
		levers.clear();
		for (MedKit a : medKits)
			world.destroyBody(a.body);
		medKits.clear();
		for (Rock a : rocks)
			world.destroyBody(a.body);
		rocks.clear();
		for (Ammo a : ammos)
			world.destroyBody(a.body);
		ammos.clear();
		for (Spore a : spores)
			world.destroyBody((a.body));
		spores.clear();
		for (Shroom a : shrooms)
			world.destroyBody(a.body);
		shrooms.clear();
		//world.destroyBody(player);

		health = maxHealth;
		ammo = startAmmo;
		deathTime = 0f;
		hasDied = false;
		levelShowTime = LEVEL_TEXT_SHOW_TIME;

		showRopePoint.set(0, 100, 0);

		generateLevel(world, currentLevel);
	}

	Vector2 dir = new Vector2();
	private void shootShotgun(float x, float y) {
	    if (impulseTime < RECOVERY_TIME) return;
		dir.set(x,y);
		shootAngle = dir.angle() + 9;
		int totalParticles = SHOTGUN_PARTICLES;
		if (ammo == 0){
			totalParticles = SHOTGUN_NO_AMMO_PARTICLES;
			noAmmoShootSnd[MathUtils.random(noAmmoShootSnd.length-1)].play(.5f);
		}
		else{
			ammo--;
			shootSnd[MathUtils.random(shootSnd.length-1)].play(.5f);
		}
		for (int i = 0; i < totalParticles; i++){
			float speed = SHOTGUN_SPEED * MathUtils.random(.7f, 1f);
			v.set(dir).scl(speed).rotate(MathUtils.random(-SHOTGUN_SCATTER_ANGLE, SHOTGUN_SCATTER_ANGLE));
			Shot shot = Pools.obtain(Shot.class);
			shot.add(world, player.getPosition().x, player.getPosition().y);
			shot.body.setLinearVelocity(v);
			shot.body.setAngularVelocity(MathUtils.random(20));
			shots.add(shot);
		}
		dir.scl(-RECOIL_IMPULSE);
		player.applyLinearImpulse(dir, player.getPosition(), true);

		impulseTime = 0f;
		playerAnim = dir.x > 0?playerAnimBack:playerAnimForward;
		playerTime = 0f;
	}

	private void createWorld(World world) {

		BodyDef bottomDef = new BodyDef();
		bottomDef.position.set(0,-MAP_HEIGHT*2-6);
		FixtureDef bottomFixture = new FixtureDef();
		bottomFixture.filter.categoryBits = 1;
		PolygonShape bottomShape = new PolygonShape();
		bottomShape.set(new Vector2[]{
				new Vector2(-1100, -1),
				new Vector2(1100, -1),
				new Vector2(-1100, 1),
				new Vector2(1100, 1)}
				);
		bottomFixture.shape = bottomShape;

		Body bottom = world.createBody(bottomDef);
		bottom.createFixture(bottomFixture).setUserData(levelFinish);
//		bottomDef.position.set(0, 100);
//		Body top = world.createBody(bottomDef);
//		bottom.createFixture(bottomFixture);
//		bottomDef.angle = 90;
//		bottomDef.position.set(0, 0);
//		Body left = world.createBody(bottomDef);
//		bottom.createFixture(bottomFixture);
//		bottomDef.angle = 90;
//		bottomDef.position.set(MAP_WIDTH, 0);
//		Body right = world.createBody(bottomDef);
//		bottom.createFixture(bottomFixture);


		BodyDef playerD = new BodyDef();
		FixtureDef playerF = new FixtureDef();
		playerD.type = BodyDef.BodyType.DynamicBody;
		playerF.filter.maskBits = ~(SHOT_GROUP | LEVER_GROUP);
		playerF.filter.categoryBits = PLAYER_GROUP;
		PolygonShape playerS = new PolygonShape();
		playerS.set(new Vector2[]{
				new Vector2(-.45f, -1.5f),
				new Vector2(.45f, -1.5f),
				new Vector2(-.45f, 1.5f),
				new Vector2(.45f, 1.5f)}
		);

		playerF.shape = playerS;

		FixtureDef playerFootF = new FixtureDef();
		playerD.type = BodyDef.BodyType.DynamicBody;
		playerFootF.filter.maskBits = ~SHOT_GROUP;
		playerFootF.filter.categoryBits = PLAYER_GROUP;
		PolygonShape playerFootS = new PolygonShape();
		playerFootS.set(new Vector2[]{
				new Vector2(-.40398f, -1.51f),
				new Vector2(.40398f, -1.51f),
				new Vector2(-.40398f, .51f),
				new Vector2(.40398f, .51f)}
		);

		playerFootF.shape = playerFootS;
		playerD.position.set(MAP_WIDTH/2, 0);
		player = world.createBody(playerD);
		player.createFixture(playerF).setUserData(playerClass);
		player.createFixture(playerFootF).setUserData(playerFoot);

		BodyDef anchorD = new BodyDef();
		FixtureDef anchorF = new FixtureDef();
		//anchorD.type = BodyDef.BodyType.DynamicBody;
		PolygonShape anchorS = new PolygonShape();
		anchorS.set(new Vector2[]{
				new Vector2(-1, -1),
				new Vector2(1, -1),
				new Vector2(-1, 1),
				new Vector2(1, 1)}
		);
		anchorD.position.set(0, 13);
		anchorF.shape = anchorS;
		anchor = world.createBody(anchorD);
		anchor.createFixture(anchorF);

		rope = new Rope();

		//generateLevel(world, 1);
	}

	private void generateLevel(World world, int levelNumber) {
		firstRun = false;
		Gdx.app.log(TAG, "generate LEvel" + levelNumber);
		int m = MathUtils.random(music.length-1);
		currentMusic.stop();
		currentMusic = music[m];
		currentMusic.play();
		float levelAlpha = (float)levelNumber / LEVELS;
		Rock firstRock = createRock(MAP_WIDTH/2, 22, 1, world, true, Rock.SPIKE_NONE);
		player.setTransform(MAP_WIDTH/2, 10, 0);
		createRock(MAP_WIDTH/2 + 11, 22, 10, world, true, Rock.SPIKE_NONE);
		createRock(MAP_WIDTH/2 -13, 22, 10, world, true, Rock.SPIKE_NONE);
		//createRock(3, 7, MathUtils.random(1, 5), world);
		//int total = MAP_WIDTH * MAP_HEIGHT/2;
		int minX = 10, maxX = MAP_WIDTH - 20;
		for (int i = 0; i < MAP_HEIGHT; i++){

			int y =  - i*2;
			if (MathUtils.random(25) < 5){//room
				int x = 0;
				int w = 7;
				if (MathUtils.randomBoolean()){
					x = MAP_WIDTH-w-3;
					Gate gate = createGate(x, y - 3, world);
					createRock(x, y, w, world, true, Rock.SPIKE_NONE);
					Rock rock = createRock(x, y - 6, w, world, true, Rock.SPIKE_NONE);
					if (MathUtils.randomBoolean() ) {
						Ammo amm = createAmmo(rock.x + 3, (int) (rock.y + 1), world);
						createLever(x-4, y-4, world, gate, amm);

					}
					else {
						MedKit medkit = createMedKit(rock.x + 3, (int) (rock.y + 1), world);
						createLever(x-4, y-4, world, gate, medkit);

					}
					if (MathUtils.random(10) < 5) createSpike(x-6, y-4, world, Spike.UP);


					createRock(x+w-1, y-4, 1, world, true, Rock.SPIKE_NONE);
					createRock(x+w-1, y-2, 1, world, true, Rock.SPIKE_NONE);
					int batsTotal = (int)MathUtils.lerp(BATS_PER_GATE_MIN, BATS_PER_GATE_MAX, levelAlpha);
					for (int b = 0; b < batsTotal; b++){
						createBat(rock.x+3, (int)rock.y+2, world);
					}

					int spikeStatus = SPIKE_NONE;
					float spikeChance = MathUtils.lerp(0, SPIKE_CHANCE_MAX, Math.min(1, levelAlpha));
					if (MathUtils.random(100) < spikeChance*3  ){
						if (MathUtils.random(10) < 1)
							spikeStatus = SPIKE_BOTH;
						else spikeStatus = MathUtils.randomBoolean()?SPIKE_LEFT:SPIKE_RIGHT;
					}
					createRock(6, y, 1, world, false, spikeStatus);
					createRock(6, y-2, 1, world, false, SPIKE_NONE);
					createRock(6, y-4, 1, world, false, spikeStatus);
					//createRock(MAP_WIDTH - 16, y, 1, world, false, SPIKE_NONE);
				} else {
					x = 0;
					Gate gate = createGate(x, y - 3, world);

					createRock(x, y, w, world, false, Rock.SPIKE_NONE);
					Rock rock = createRock(x, y - 6, w, world, false, SPIKE_NONE);
					if (MathUtils.randomBoolean() ) {
						Ammo amm = createAmmo(rock.x - 1, (int) (rock.y + 1), world);
						createLever(x+w-3, y-4, world, gate, amm);
					}
					else {
						MedKit medkit = createMedKit(rock.x - 1, (int) (rock.y + 1), world);
						createLever(x+w-3, y-4, world, gate, medkit);
					}
					if (MathUtils.random(10) < 5) createSpike(x+w-1, y-4, world, Spike.UP);
					//createSpike(x+w+1, y-6, world, Spike.RIGHT);

					createRock(x-w+1, y-4, 1, world, false, SPIKE_NONE);
					createRock(x-w+1, y-2, 1, world, false, Rock.SPIKE_NONE);
					int batsTotal = (int)MathUtils.lerp(BATS_PER_GATE_MIN, BATS_PER_GATE_MAX, levelAlpha);
					for (int b = 0; b < batsTotal; b++){
						createBat(rock.x-4, (int)rock.y+2, world);
					}
					//createRock(6, y, 1, world, false, SPIKE_NONE);

					int spikeStatus = SPIKE_NONE;
					float spikeChance = MathUtils.lerp(0, SPIKE_CHANCE_MAX, Math.min(1, levelAlpha));
					if (MathUtils.random(100) < spikeChance*3){
						if (MathUtils.random(10) < 1)
							spikeStatus = SPIKE_BOTH;
						else spikeStatus = MathUtils.randomBoolean()?SPIKE_LEFT:SPIKE_RIGHT;
					}


					createRock(MAP_WIDTH - 16, y, 1, world, false, spikeStatus);
					createRock(MAP_WIDTH - 16, y-2, 1, world, false, SPIKE_NONE);
					createRock(MAP_WIDTH - 16, y-4, 1, world, false, spikeStatus);
				}


				i+=2;
			} else {
				createRock(6, y, 1, world, false, SPIKE_NONE);
				createRock(MAP_WIDTH - 16, y, 1, world, false, SPIKE_NONE);
			}

			if (MathUtils.random(6) < 6){

				// createRock(maxX, y, 1, world, MathUtils.randomBoolean(), Rock.SPIKE_NONE);
//				int x = MathUtils.random(MAP_WIDTH-30)+10;
				int w = MathUtils.random(1, 4);
				//w = 5;
				int x = MathUtils.random(minX + w - 1, maxX - w +1);

				float middleAlpha = x -
						((maxX + minX)/2);
				middleAlpha /= (float)((maxX + minX)/2);
				middleAlpha = Math.abs(middleAlpha);
				float shroomChance = MathUtils.lerp(SHROOMS_CHANCE_MIN, SHROOMS_CHANCE_MAX, Math.min(1, levelAlpha)) * middleAlpha;
				//Gdx.app.log(TAG, "middle a " + middleAlpha);

				int spikeStatus = SPIKE_NONE;
				float spikeChance = MathUtils.lerp(0, SPIKE_CHANCE_MAX, Math.min(1, levelAlpha));
				if (MathUtils.random(100) < spikeChance){
					if (MathUtils.random(10) < 1)
						spikeStatus = SPIKE_BOTH;
					else spikeStatus = MathUtils.randomBoolean()?SPIKE_LEFT:SPIKE_RIGHT;
				}
				int powerUpAdjust = 0;
				if (levelNumber > LEVELS)
					powerUpAdjust = levelNumber - LEVELS;

				Rock rock = createRock(x, y, w, world, MathUtils.randomBoolean(), spikeStatus);
				if (MathUtils.random(100) < AMMO_CHANCE - powerUpAdjust) createAmmo(rock.x + MathUtils.random(-rock.w/2, rock.w/2), (int) (rock.y+1), world);
				if (MathUtils.random(100) < MEDKIT_CHANCE - powerUpAdjust)createMedKit(rock.x + MathUtils.random(-rock.w/2, rock.w/2), (int) (rock.y+1), world);
				if (MathUtils.random(100) < shroomChance)createShroom(rock.x + MathUtils.random(-rock.w/2, rock.w/2), (int) (rock.y+1), world);
			}


		}

		anchor.setTransform(0, 13, 0);
		rope.setAnchor(anchor, firstRock);
		rope.setPlayer(player, world, anchor);
		rope.stopExtending(world);
		damageTime = PLAYER_DAMAGE_TIMEOUT;

		int batsTotal = (int)MathUtils.lerp(BATS_MIN, BATS_MAX, levelAlpha);
		//Gdx.app.log(TAG, "bats " + currentLevel + " " + levelAlpha);
		for (int i = 0; i < batsTotal; i++){
			createBat(MathUtils.random(minX, maxX), -MathUtils.random(MAP_HEIGHT*2-50)-50, world);
		}

		for (int i = 0; i < 20; i++){
			//createMedKit(MathUtils.random(-20, 20), -MathUtils.random(250), world);
			//Gate gate = createGate(MathUtils.random(-20, 20), -MathUtils.random(250), world);
			//Lever lever = createLever(gate.body.getPosition().x - 2, gate.body.getPosition().y, world, gate);
		}
	}

	IntArray placed = new IntArray(), placedWidth = new IntArray();

	private Rock createRock(int x, int y, int w, World world, boolean left, int spikes) {
		Rock rock = Pools.obtain(Rock.class);
		rock.add(world, x, y, w, left, spikes);
		rocks.add(rock);
		if (spikes == SPIKE_RIGHT || spikes == SPIKE_BOTH){
			createSpike(x+w+1, y, world, Spike.RIGHT);
		}
		if (spikes == SPIKE_LEFT || spikes == SPIKE_BOTH){
			createSpike(x-w-1, y, world, Spike.LEFT);
		}
		return rock;
	}

	private Bat createBat(int x, int y, World world) {
		Bat rock = Pools.obtain(Bat.class);
		rock.add(world, x, y);
		bats.add(rock);
		return rock;
	}

	private Spore createSpore(Vector2 pos, World world, Vector2  angle) {
		Spore rock = Pools.obtain(Spore.class);
		rock.add(world, pos.x, pos.y);

		angle.scl(SPORE_SPEED).add(MathUtils.random(-SHROOM_VEL_VARIANCE, SHROOM_VEL_VARIANCE), MathUtils.random(-SHROOM_VEL_VARIANCE, SHROOM_VEL_VARIANCE));
		rock.body.setLinearVelocity(angle);
		spores.add(rock);
		return rock;
	}

	private Shroom createShroom(int x, int y, World world) {
		Shroom rock = Pools.obtain(Shroom.class);
		rock.add(world, x, y+1);
		shrooms.add(rock);
		return rock;
	}

	private Lever createLever(float x, float y, World world, Gate gate, IPowerUp powerup) {
		Lever rock = Pools.obtain(Lever.class);
		rock.add(world, x, y-.5f, gate, powerup);
		levers.add(rock);
		return rock;
	}

	private Gate createGate(float x, float y, World world) {
		Gate rock = Pools.obtain(Gate.class);
		rock.add(world, x, y);
		gates.add(rock);
		return rock;
	}

	private Ammo createAmmo(int x, int y, World world) {
		Ammo rock = Pools.obtain(Ammo.class);
		rock.add(world, x, y);
		ammos.add(rock);
		return rock;
	}

	private MedKit createMedKit(int x, int y, World world) {
		MedKit rock = Pools.obtain(MedKit.class);
		rock.add(world, x, y);
		medKits.add(rock);
		return rock;
	}
	private Spike createSpike(int x, int y, World world, int orientation) {
		Spike rock = Pools.obtain(Spike.class);
		rock.add(world, x, y, orientation);
		spikes.add(rock);
		return rock;
	}

	float accumulator;
	@Override
	public void render () {


		float deltaTime = Gdx.graphics.getDeltaTime();
		float frameTime = Math.min(deltaTime, 0.25f);
		accumulator += frameTime;
		while (accumulator >= TIME_STEP) {
			accumulator -= TIME_STEP;
			float delta = TIME_STEP;
			playerTime += delta;
			impulseTime += delta;
			damageTime += delta;
			levelShowTime += delta;
			if (qNextLevel){
				startLevel(++currentLevel);
				qNextLevel = false;
				levelShowTime = 0;
				if (currentLevel > maxLevel){
					prefs.putInteger("maxLevel", currentLevel);
					prefs.flush();
					maxLevel = currentLevel;
				}
			}

			for (Bat bat : bats)
				bat.update(delta, player);
			for (Lever lev : levers)
				lev.update(delta, player);
			for (Gate gate : gates) {
				gate.update(delta, player);
				if (gate.isDone){
					world.destroyBody(gate.body);
					gates.removeValue(gate, true);
					Pools.free(gate);
				}
			}

			for (MedKit a : medKits){
				a.update(delta, player);
				if (a.isDone){
					medKits.removeValue(a, true);
					world.destroyBody(a.body);
					health += MEDKIT_INCREMENT;
					health = Math.min(health, maxHealth);
					healSnd.play();
					Pools.free(a);
				}
			}
			for (Ammo a : ammos){
				a.update(delta, player);
				if (a.isDone){
					ammos.removeValue(a, true);
					world.destroyBody(a.body);
					ammo += AMMO_INCREMENT;
					ammoSnd.play();
					Pools.free(a);
				}
			}
			for (Shroom a : shrooms){
				a.update(delta, player);
				if (a.sporesQueued){
					v.set(player.getPosition()).sub(a.body.getPosition());
					v.nor();
					if (!a.isDone) {
						a.isDone = true;
						a.animTime = MathUtils.random(2f);
						for (int i = 0; i < SPORES_PER_SHROOM/2; i++){
							angleV.set(v);
							angleV.rotate(MathUtils.random(360));
							createSpore(a.body.getPosition(), world, angleV);

						}
					} else {
						for (int i = 0; i < SPORES_PER_SHROOM; i++){
							angleV.set(v);
							createSpore(a.body.getPosition(), world, angleV);
						}
					}
					a.sporesQueued = false;
				}
			}
			updateDestroy();
			rope.updateUnwind(rocks, world, shape);
			rope.updateWind(rocks, world, shape);
			if (rope.joint == null){
				health = 0;
				damageTime = 0f;

				//damageSnd.play();
			}
			updatePlayer();
			//world.step(delta, 6, 2);
			world.step(delta, VELOCITY_ITERATIONS, POSITION_ITERATIONS);

		}



		drawMain(Gdx.graphics.getDeltaTime());
//		debugRenderer.render(world, camera.combined);

	}

	private void drawMain(float delta) {
		if (showRopePoint.y < 15){
			camera.position.lerp(showRopePoint, .04f);
		} else {
			camera.position.y = player.getPosition().y;
			camera.position.x = player.getPosition().x;
		}

		camera.update();
		Gdx.gl.glClearColor(0f, 0f, 0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		fadeAlpha = deathTime / DEATH_FADE_TIME;
		fadeAlpha = Math.min(1f, fadeAlpha);
		fadeAlpha = 1f - fadeAlpha;
		batchColor.set(Color.BLACK);
		batchColor.lerp(Color.WHITE, fadeAlpha);
		//Gdx.app.log(TAG, "fade " + fadeAlpha);
		batch.setColor(batchColor);
		//batch.setColor(Color.YELLOW);
		batch.begin();
		draw();
		batch.end();
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
		//batch.setColor(Color.RED);
		batch.begin();
		//batch.draw(img, 0, 0);
		for (int i = 0; i < ammo; i++){
			batch.draw(ammoBarSprite, Gdx.graphics.getWidth() - i * STATUS_SIZE - STATUS_SIZE, Gdx.graphics.getHeight()-STATUS_SIZE, STATUS_SIZE, STATUS_SIZE);
		}
		for (int i = 0; i < health; i++){
			batch.draw(healthBarSprite, i * STATUS_SIZE, Gdx.graphics.getHeight()-STATUS_SIZE, STATUS_SIZE, STATUS_SIZE);
		}
		float alpha = impulseTime / RECOVERY_TIME;
		batch.draw(obscureSprite, 0, h - 30, alpha * w, 30);
		if (levelShowTime < LEVEL_TEXT_SHOW_TIME)
			titleFont.draw(batch, "Level " + (currentLevel+1), 50, Gdx.graphics.getHeight() /3 * 2);
		//if (playerJump != 0)font.draw(batch, "jump", 100, 200);
		batch.end();

		if (health <= 0){
			deathTime += delta;
			if (!hasDied){
				currentMusic.stop();
				currentMusic = introMusic;
				currentMusic.play();
			}
			hasDied = true;
			if (deathTime > DEATH_FADE_TIME){
				showDeathScreen();
			}
			if (rope.joint != null){
				world.destroyJoint(rope.joint);
				rope.joint = null;
			}
		}
		shape.setProjectionMatrix(camera.combined);
		shape.setColor(ROPE_COLOR);
		shape.begin(ShapeRenderer.ShapeType.Line);
		rope.draw(shape);
		shape.end();
	}

	Vector2 angleV = new Vector2();
	private void showDeathScreen() {

		batch.begin();
		//titleFont.getData().setScale(.5f);
		if (firstRun) {
			titleFont.draw(batch, "Depth \nSeilor", Gdx.graphics.getWidth() / 2.5f, Gdx.graphics.getHeight() / 4 * 3 + 50);
			titleFont.getData().setScale(.5f);
			font.draw(batch, "[SPACE] to use rope \n\nClick screen to shoot", Gdx.graphics.getWidth() / 2.2f, Gdx.graphics.getHeight() / 4 * 1 + 50);
		}
		else{
			titleFont.getData().setScale(.5f);
			titleFont.draw(batch, "Your body \nhas been \nlost to \nthe \nDepths", Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/4*3+50);
		}
		titleFont.getData().setScale(1);
		font.getData().setScale(1);
		font.draw(batch, "Start in Sandbox", 5, Gdx.graphics.getHeight()/8 * 7+10);
		font.draw(batch, "Start at Level 1", 5, Gdx.graphics.getHeight()/8 * 5+10);
		if (maxLevel >= 3) font.draw(batch, "Start at Level 3", 5, Gdx.graphics.getHeight()/8 * 3+10);
		if (maxLevel >= 5) font.draw(batch, "Start at Level 5", 5, Gdx.graphics.getHeight()/8 * 1+10);
		batch.end();
		shape.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//		shape.setColor(Color.RED);
//		shape.begin(ShapeRenderer.ShapeType.Line);
//		int w = Gdx.graphics.getWidth();
//		int h;
//		h = Gdx.graphics.getHeight()/4 * 1;
//		shape.line(-110,-110,1000,1000);
//		shape.line(0, h, w, h);
//		h = Gdx.graphics.getHeight()/4 * 2;
//		shape.line(0, h, w, h, Color.YELLOW, Color.YELLOW);
//
//		h = Gdx.graphics.getHeight()/4 * 3;
//		shape.line(0, h, w, h);
//		shape.end();
	}

	private void draw() {
		int backIndex = 0;
		bottomSprite.draw(batch, fadeAlpha);
		for (Vector2 back : backPositions){
			Sprite s = backSprites[backIndex];
			s.setPosition(back.x, back.y);
			backIndex = ++backIndex % backSprites.length;
			s.draw(batch, fadeAlpha);
		}
		drawRocks();
		drawPlayer();
		drawBats();
		drawShots();
		for (Gate gate : gates){
			v.set(gate.body.getPosition());
			Sprite tile = gateAnim.getKeyFrame(gate.animTime);
			int gateI = gateAnim.getKeyFrameIndex(gate.animTime);
			//if (gateI != 0) Gdx.app.log(TAG, "gate inndex " + gateI);
			tile.setPosition(v.x-4, v.y-4-Gate.SIZE/2);
			tile.draw(batch, fadeAlpha);

		}
		for (Lever lever : levers){
			v.set(lever.body.getPosition());
			Sprite tile = leverAnim.getKeyFrame(lever.animTime);
			tile.setPosition(v.x-4, v.y-4-Lever.SIZE/2);
			tile.draw(batch, fadeAlpha);
		}
		for (MedKit a : medKits){
			v.set(a.body.getPosition());
			Sprite tile = medKitSprite;//.getKeyFrame(a.animTime);
			tile.setPosition(v.x - MedKit.SIZE, v.y-MedKit.SIZE/2);
			tile.draw(batch, fadeAlpha);
		}
		for (Ammo a : ammos){
			v.set(a.body.getPosition());
			Sprite tile = ammoSprite;//.getKeyFrame(a.animTime);
			tile.setPosition(v.x -Ammo.SIZE, v.y-Ammo.SIZE/2);
			tile.draw(batch, fadeAlpha);
		}
		for (Spike a : spikes){
			v.set(a.body.getPosition()).add(a.drawOffset);
			Sprite tile = spikeSprite;//.getKeyFrame(a.animTime);
			tile.setRotation(a.rotation);

			tile.setPosition(v.x -4, v.y-4);
			tile.draw(batch, fadeAlpha);
		}
		for (Shroom a : shrooms){
			v.set(a.body.getPosition());
			Sprite tile = shroomAnim.getKeyFrame(a.animTime);
			tile.setPosition(v.x -4,  v.y -4);
			tile.draw(batch, fadeAlpha);
		}
		for (Spore a : spores){
			v.set(a.body.getPosition());
			Sprite tile = sporeSprite;//.getKeyFrame(a.animTime);
			tile.setPosition(v.x -Spore.SIZE, v.y-Spore.SIZE/2);
			//tile.setRotation();
			tile.draw(batch, fadeAlpha);
		}


	}

	private void updateDestroy() {
        for (Bat bat : destroyBats){
            if (bats.removeValue(bat, true)) { //Gdx.app.log(TAG, "twice removed");
				world.destroyBody(bat.body);
				Pools.free(bat);
			}
        }
        destroyBats.clear();
        for (Shot shot : destroyShots){
            if (shots.removeValue(shot, true)){
            	world.destroyBody(shot.body);
            	Pools.free(shot);

			}

        }
        destroyShots.clear();
		for (Spore shot : destroySpores){
			if (spores.removeValue(shot, true)){
				world.destroyBody(shot.body);
				Pools.free(shot);

			}

		}
		destroySpores.clear();
    }

    private void drawShots() {
		for (Shot shot : shots){
			v.set(shot.body.getPosition());
			Sprite tile = shotSprite;
			tile.setPosition(v.x-SHOTGUN_PARTICLE_SIZE, v.y-SHOTGUN_PARTICLE_SIZE);
			tile.draw(batch, fadeAlpha);
		}
	}

	private void drawBats() {
		for (Bat bat : bats){

			v.set(bat.body.getPosition());
			Sprite tile = batAnim.getKeyFrame(bat.animTime);
			tile.setPosition(v.x-4, v.y-4);
			tile.draw(batch, fadeAlpha);
			//Gdx.app.log(TAG, "draw bat" + v + batAnim.getKeyFrames().length);
		}
	}

	Vector2 v = new Vector2();
	private void drawPlayer() {
		v.set(player.getPosition());
		//batch.draw(, v.x - 1, v.y - 1);
		Sprite frame = playerAnim.getKeyFrame(playerTime);
		frame.setPosition(v.x-4, v.y-4);
		if (health <= 0){
			frame.setColor(Color.RED);
		} else {
			frame.setColor(Color.WHITE);
			if (damageTime < PLAYER_DAMAGE_TIMEOUT){
				int index = (int)(damageTime / PLAYER_DAMAGE_FLASH_TIME);
				if (index % 2 == 0)
					frame.setColor(Color.RED);
			}
		}
		frame.draw(batch, fadeAlpha);

		Sprite arm = playerArmAnim.getKeyFrame(shootAngle);
		arm.setPosition(v.x-4, v.y-4);
		arm.draw(batch, fadeAlpha);
	}

	private void drawRocks() {
		for (Rock rock : rocks){
			float off = - 3f - (rock.w);
			for (int i = 0; i < rock.w; i++){
				Sprite tile = rockSprite[(rock.tile + i) % 4];
//				Sprite tile = rockSprite[(rock.tile)];
				tile.setFlip(!rock.isLeft, false);

				tile.setPosition(rock.x+off, rock.y-4);
				tile.draw(batch, fadeAlpha);
				off+= 2;
			}


		}
	}

	Vector2 moveV = new Vector2();
	private void updatePlayer() {
		if (impulseTime > RECOVERY_TIME) {
			playerAnim = playerAnimStill;
		}

		if (playerMove != MOVE_NONE){
			moveV.set(40, 0).scl(playerMove);
			//player.applyForce(moveV, player.getPosition(), true);
			if (impulseTime > RECOVERY_TIME) {
				player.applyLinearImpulse(moveV, player.getPosition(), true);
				impulseTime = 0f;
				playerAnim = playerMove==1?playerAnimBack:playerAnimForward;
				playerTime = 0f;
			}
		}
		if (playerJump != STAND){
			moveV.set(0, 30f);
			//player.applyForce(moveV, player.getPosition(), true);
			rope.extend(world);
		}
		if (playerTouchingGround != null){
			float y = playerTouchingGround.y + 1;

			for (Ammo a : ammos){
				float dist = y - a.body.getPosition().y;
				if (dist > -1f && dist < .5f && a.body.getPosition().dst2(playerTouchingGround.body.getPosition()) < GRAB_DISTANCE2)
					a.hit();
			}
			for (MedKit a : medKits){
				float dist = y - a.body.getPosition().y;
				if (dist > -1f && dist < .5f && a.body.getPosition().dst2(playerTouchingGround.body.getPosition()) < GRAB_DISTANCE2)
					a.hit();
			}
		}
	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
	}

	@Override
	public void resize(int width, int height) {
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		int ww = CAMERA_WIDTH;
		float ar = (float)w/h;
		int hh = (int)(ww / ar);
		camera.setToOrtho(false, ww, hh);
		super.resize(width, height);
	}
}
