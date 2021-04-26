package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.mygdx.game.MyGdxGame.BAT_GROUP;
import static com.mygdx.game.MyGdxGame.HAZARD_GROUP;
import static com.mygdx.game.MyGdxGame.LEVER_GROUP;
import static com.mygdx.game.MyGdxGame.PLAYER_GROUP;

public class Bat {
    private static final BodyDef def;

    private static final FixtureDef fix;
    private static final float FLAP_TIME = .5f;
    private static final float SIZE = .35f;
    private static final float ACTIVATION_DISTANCE2 = 40 * 40;
    private static final float FLAP_STRENGTH = 15;
    private static Vector2[] offset = new Vector2[]{
            new Vector2(-SIZE, -SIZE),
            new Vector2(SIZE, -SIZE),
            new Vector2(-SIZE, SIZE),
            new Vector2(SIZE, SIZE)};

    static {
        def = new BodyDef();
        fix = new FixtureDef();
        def.linearDamping = 5.5f;
        def.gravityScale = .1f;
        def.type = BodyDef.BodyType.DynamicBody;
        PolygonShape shape = new PolygonShape();
        shape.set(offset
        );
        fix.shape = shape;
        fix.filter.categoryBits = BAT_GROUP;
        fix.filter.maskBits = ~(LEVER_GROUP | HAZARD_GROUP | BAT_GROUP);
    }

    protected Body body;
    private float x;
    private float y;
    private float time;
    public float animTime;

    public Bat(){
        time = MathUtils.random(FLAP_TIME);
    }
    static Vector2 v = new Vector2();
    public void update(float delta, Body player) {
        animTime += delta;
        time += delta;
        if (time > FLAP_TIME ){//flap
            time = 0f;
            if ( player.getPosition().dst2(body.getPosition()) > ACTIVATION_DISTANCE2){
                body.setTransform(x, y, 0);
                body.setLinearVelocity(0, 0);
            } else {
                v.set(body.getPosition()).sub(player.getPosition());
                v.nor().scl(-FLAP_STRENGTH).rotate(MathUtils.random(-60, 60));
                body.applyLinearImpulse(v, body.getPosition(), true);
            }

        }
    }

    public void add(World world, float x, float y){
        def.position.set(x, y);
        body = world.createBody(def);
        body.createFixture(fix).setUserData(this);
        this.x = x;
        this.y = y;
    }


}
