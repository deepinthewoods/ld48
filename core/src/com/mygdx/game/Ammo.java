package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.mygdx.game.MyGdxGame.BAT_GROUP;
import static com.mygdx.game.MyGdxGame.GRAB_TIME;
import static com.mygdx.game.MyGdxGame.PLAYER_GROUP;
import static com.mygdx.game.MyGdxGame.SHOT_GROUP;
import static com.mygdx.game.MyGdxGame.SPORE_GROUP;

public class Ammo implements IPowerUp{
    private static final BodyDef def;

    private static final FixtureDef fix;
    private static final float FLAP_TIME = .5f;
    protected static final float SIZE = 1;
    private static final String TAG = "gate";

    private static Vector2[] offset = new Vector2[]{
            new Vector2(-SIZE, -SIZE*.5f),
            new Vector2(SIZE, -SIZE*.5f),
            new Vector2(-SIZE, SIZE*.5f),
            new Vector2(SIZE, SIZE*.5f)};

    private static final FixtureDef postFix;

    static {
        def = new BodyDef();
        fix = new FixtureDef();
        //def.linearDamping = 5.5f;
        //def.gravityScale = .1f;
        fix.filter.categoryBits = MyGdxGame.ITEMS_GROUP;
        fix.filter.maskBits = ~( SHOT_GROUP| BAT_GROUP | SPORE_GROUP);
        postFix = new FixtureDef();
        postFix.filter.maskBits = ~(PLAYER_GROUP | SHOT_GROUP | BAT_GROUP | SPORE_GROUP);
        //def.type = BodyDef.BodyType.DynamicBody;
        PolygonShape shape = new PolygonShape();
        shape.set(offset
        );
        fix.shape = shape;
    }

    protected Body body;
    private float x;
    private int y;
    private float time;
    public float animTime;
    public boolean isDone;
    private boolean isActivated;

    public Ammo(){
        time = MathUtils.random(FLAP_TIME);
    }
    static Vector2 v = new Vector2();
    public boolean update(float delta, Body player) {
        if (!isDone && isActivated){
            animTime += delta;
            float alpha = animTime / GRAB_TIME;
            v.set(x, y);
            v.lerp(player.getPosition(), alpha);
            body.setTransform(v, 0);
            //Gdx.app.log(TAG, "tick"+alpha);
            if (animTime > GRAB_TIME){
                isDone = true;
                return true;
            }
        }
        return false;
    }

    public void add(World world, float x, int y){
        //y += SIZE*.25f;
        def.position.set(x, y+SIZE*.25f);
        body = world.createBody(def);
        body.createFixture(fix).setUserData(this);
        this.x = x;
        this.y = y;
        isActivated = false;
        isDone = false;
    }

    @Override
    public void hit() {
        if (!isActivated){
            isActivated = true;
            animTime = 0f;
        }
        if (body.getFixtureList().size == 0) return;
        body.getFixtureList().get(0).setFilterData(postFix.filter);

    }
}
