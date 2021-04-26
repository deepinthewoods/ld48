package com.mygdx.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.mygdx.game.MyGdxGame.BAT_GROUP;
import static com.mygdx.game.MyGdxGame.PLAYER_GROUP;
import static com.mygdx.game.MyGdxGame.SHOT_GROUP;

public class Rock {
    private static final BodyDef def;
    public static final int SPIKE_LEFT = 0, SPIKE_RIGHT = 1, SPIKE_BOTH = 2, SPIKE_NONE = 3;
    public static final int CORNER_BL = 0, CORNER_BR = 1, CORNER_TL = 2, CORNER_TR = 3;
    private static final FixtureDef fix;
    public int x;
    public float y;
    public boolean isLeft;
    public int spikes;
    PolygonShape shape = new PolygonShape();

    private Vector2[] offset = new Vector2[]{
        new Vector2(-1, -1),
                new Vector2(1, -1),
                new Vector2(-1, 1),
                new Vector2(1, 1)};



    static {
        def = new BodyDef();
        fix = new FixtureDef();
        fix.friction = 0f;
        fix.filter.maskBits = (PLAYER_GROUP | BAT_GROUP | SHOT_GROUP);
    }

    public final int tile;

    public Body body;
    protected int w;

    public Rock(){
        tile = MathUtils.random(3);
    }

    public void add(World world, int x, float y, int w, boolean left, int spikes){
        isLeft = left;
        def.position.set(x, y);
        for  (int i = 0; i < 4; i++)
            offset[i].x *= w;
        shape.set(offset);
        fix.shape = shape;
        body = world.createBody(def);
        body.createFixture(fix).setUserData(this);
        this.spikes = spikes;
        this.x = x;
        this.y = y;
        this.w = w;
    }

    public void remove(World world){
        world.destroyBody(body);
    }

    Vector2 corner = new Vector2();
    public Vector2 getCorner(int c) {
        corner.set(body.getPosition()).add(offset[c]);
        return corner;
    }
    public Vector2 getBiggerCorner(int c) {

        corner.set(offset[c]).scl(1.01f).add(body.getPosition());
        return corner;
    }
}
