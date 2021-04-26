package com.mygdx.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.mygdx.game.MyGdxGame.BAT_GROUP;
import static com.mygdx.game.MyGdxGame.HAZARD_GROUP;
import static com.mygdx.game.MyGdxGame.PLAYER_GROUP;

public class Spike {
    private static final BodyDef def;

    private static final FixtureDef fix;
    public static final int LEFT = 0, RIGHT = 1, UP = 2, DOWN = 3;
    public int x;
    public float y;
    public boolean isLeft;
    PolygonShape shape = new PolygonShape();

    private static Vector2[] offset = new Vector2[]{
        new Vector2(-.9f, -1),
                new Vector2(.9f, -1),
                new Vector2(-.9f, .5f),
                new Vector2(.9f, .5f)};

    static {
        def = new BodyDef();
        fix = new FixtureDef();
        def.linearDamping = 5.5f;
        def.gravityScale = .1f;
        //def.type = BodyDef.BodyType.DynamicBody;
        PolygonShape shape = new PolygonShape();
        shape.set(offset
        );
        fix.shape = shape;
        fix.filter.categoryBits = HAZARD_GROUP;
        fix.filter.maskBits = PLAYER_GROUP;
    }

    public final int tile;

    public Body body;
    public int orientation;
    public float rotation;
    protected Vector2 drawOffset = new Vector2();
    ;

    public Spike(){
        tile = MathUtils.random(3);
    }

    public void add(World world, int x, float y, int orientation){
        this.orientation = orientation;

        switch (orientation){
            case LEFT: rotation = 90; break;
            case RIGHT: rotation = 270; break;
            case UP: rotation = 0; break;
            case DOWN: rotation = 180; break;
        }
        switch (orientation){
            case LEFT: drawOffset.set(0, 0); break;
            case RIGHT: drawOffset.set(0, 0); break;
            case UP: drawOffset.set(0, 0); break;
            case DOWN: drawOffset.set(0, 0); break;
        }

        def.position.set(x, y);

        shape.set(offset);
        fix.shape = shape;
        def.angle = MathUtils.degRad * rotation;
        body = world.createBody(def);
        body.createFixture(fix).setUserData(this);

        this.x = x;
        this.y = y;

    }

    public void remove(World world){
        world.destroyBody(body);
    }




}
