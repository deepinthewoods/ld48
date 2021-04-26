package com.mygdx.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.mygdx.game.MyGdxGame.SHOT_GROUP;

public class Shroom {
    private static final BodyDef def;

    private static final FixtureDef fix;
    private static final float FLAP_TIME = .5f;
    protected static final float SIZE = 1;
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
        fix.filter.categoryBits = MyGdxGame.SHROOM_GROUP;
        fix.filter.maskBits = (SHOT_GROUP);
        //def.type = BodyDef.BodyType.DynamicBody;
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
    public boolean isActivated = false, isDone = false;
    public boolean sporesQueued;


    public Shroom(){
        time = MathUtils.random(FLAP_TIME);
    }
    static Vector2 v = new Vector2();
    public void update(float delta, Body player) {
        if (!isActivated && !isDone){
            if (player.getPosition().dst2(body.getPosition()) < MyGdxGame.SHROOM_RANGE2){
                isActivated = true;
                animTime = 0f;

            }

        }

        if (!isDone && isActivated){
            animTime += delta;
            if (animTime > MyGdxGame.SHROOM_SPORE_TIME){
                isDone = true;
                sporesQueued = true;

            }
        }
    }

    public void add(World world, float x, float y){
        def.position.set(x, y);
        body = world.createBody(def);
        body.createFixture(fix).setUserData(this);
        this.x = x;
        this.y = y;
        isActivated = false;
        isDone = false;
        animTime = 0f;
        sporesQueued = false;
    }


    public void hit() {
        if (!isActivated){
            isActivated = true;
            animTime = 0f;

        }

    }
}
