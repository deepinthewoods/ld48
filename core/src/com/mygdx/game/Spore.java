package com.mygdx.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.mygdx.game.MyGdxGame.BAT_GROUP;
import static com.mygdx.game.MyGdxGame.GATE_GROUP;
import static com.mygdx.game.MyGdxGame.HAZARD_GROUP;
import static com.mygdx.game.MyGdxGame.LEVER_GROUP;
import static com.mygdx.game.MyGdxGame.PLAYER_GROUP;
import static com.mygdx.game.MyGdxGame.SHOTGUN_PARTICLE_SIZE;
import static com.mygdx.game.MyGdxGame.SHOT_GROUP;
import static com.mygdx.game.MyGdxGame.SHROOM_GROUP;
import static com.mygdx.game.MyGdxGame.SPORE_GROUP;

public class Spore {
    private static final BodyDef def;

    private static final FixtureDef fix;
    private static final float FLAP_TIME = .5f;
    public static final float SIZE = .4f;

    private static Vector2[] offset = new Vector2[]{
            new Vector2(-SIZE, -SIZE),
            new Vector2(SIZE, -SIZE),
            new Vector2(-SIZE, SIZE),
            new Vector2(SIZE, SIZE)};

    static {
        def = new BodyDef();
        fix = new FixtureDef();
        //def.linearDamping = 5.5f;
        //def.gravityScale = .1f;
        fix.filter.categoryBits = SPORE_GROUP;
        fix.filter.maskBits = ~(SPORE_GROUP| SHROOM_GROUP| GATE_GROUP| LEVER_GROUP| HAZARD_GROUP| BAT_GROUP | 1);
        def.type = BodyDef.BodyType.DynamicBody;
        def.gravityScale = 0f;
        PolygonShape shape = new PolygonShape();
        shape.set(offset
        );
        fix.shape = shape;
    }

    protected Body body;
    private float x;
    private float y;
    private float time;
    public float animTime;

    public Spore(){
        time = MathUtils.random(FLAP_TIME);
    }
    static Vector2 v = new Vector2();
    public void update(float delta, Body player) {

    }

    public void add(World world, float x, float y){
        def.position.set(x, y);
        body = world.createBody(def);
        body.createFixture(fix).setUserData(this);
        this.x = x;
        this.y = y;
    }


}
