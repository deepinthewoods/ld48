package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

class Rope {

    private static final String TAG = "Rope";
    private final RopeJointDef def;
    private final Array<Rock> collisions;
    //private final Array<Vector2> collisionPoints;
    private final IntArray collisionDirections;
    private final IntArray collisionCorners;
    private final FloatArray collisionLength;
    private Body player;
    protected Joint joint;
    private Body anchor;
    private boolean hasChangedLength;

    public Rope(){
        def = new RopeJointDef();
        collisions = new Array<Rock>();
       // collisionPoints = new Array<Vector2>();
        collisionDirections = new IntArray();
        collisionCorners = new IntArray();
        collisionLength = new FloatArray();
    }

    Vector2 a = new Vector2(),
            b = new Vector2(),
            corner = new Vector2();
    Rectangle rect = new Rectangle();
    public void updateWind(Array<Rock> rocks, World world, ShapeRenderer shape) {
        if (joint == null) return;
        Rock prevRock = collisions.get(collisions.size-1);
        int prevCorner = collisionCorners.get(collisions.size-1);
        for (int i = 1; i < rocks.size; i++){
            Rock rock = rocks.get(i);
            rect.setHeight(2);
            rect.setWidth(2*rock.w);
            rect.setCenter(rock.body.getPosition());
            a.set(joint.getAnchorA());
            b.set(joint.getAnchorB());
            boolean hit = Intersector.intersectSegmentRectangle(a, b, rect);
            //shape.setColor(Color.RED);
            //shape.line(a, b);
            //shape.rect(rect.x, rect.y, rect.width, rect.height);

            if (hit){
                float closest = 100;
                int closestCorner = -1;
                int closestDir = 0;
                for (int c = 0; c < 4; c++){
                    if (rock == prevRock && c == prevCorner) continue;
                    corner.set(rock.getCorner(c));
                    float dist = Intersector.distanceSegmentPoint(a, b, corner);
                    //shape.setColor(Color.YELLOW);
                    //shape.line(a, b);
                    if (dist < closest){
                        closest = dist;
                        closestCorner = c;
                        closestDir = Intersector.pointLineSide(a, b, corner);

                    }
                }
                if (closestCorner != -1){//has Collided
                    collisions.add(rock);
                    collisionCorners.add(closestCorner);
                    collisionDirections.add(closestDir);
                    collisionLength.add(def.maxLength);
                    float slack = 0f;//def.maxLength - joint.getAnchorA().dst(joint.getAnchorB());
                    world.destroyJoint(joint);
                    anchor.setTransform(rock.getBiggerCorner(closestCorner), 0);
                    float newLength = player.getPosition().dst(rock.getBiggerCorner(closestCorner));
                    //Gdx.app.log(TAG, "wind old: " + def.maxLength + " new: " + newLength + "slack" + slack);
                    def.maxLength = newLength;
                    world.createJoint(def);
                    hasChangedLength = false;

                    switch (rock.spikes){
                        case Rock.SPIKE_BOTH:
                            world.destroyJoint(joint);
                            joint= null;
                            MyGdxGame.showRopePoint.set(corner.x, corner.y, 0);
                            break;
                        case Rock.SPIKE_LEFT:
                            if (closestCorner == Rock.CORNER_BL || closestCorner == Rock.CORNER_TL){
                                world.destroyJoint(joint);
                                joint = null;
                                MyGdxGame.showRopePoint.set(corner.x, corner.y, 0);
                            }
                            break;

                        case Rock.SPIKE_RIGHT:
                            if (closestCorner == Rock.CORNER_BR || closestCorner == Rock.CORNER_TR){
                                world.destroyJoint(joint);
                                joint = null;
                                MyGdxGame.showRopePoint.set(corner.x, corner.y, 0);
                            }
                            break;

                    }
                    return;
                }
            }

        }
    }

    public void setPlayer(Body player, World world, Body anchor) {
        this.player = player;
        float jointLength = player.getPosition().dst(anchor.getPosition());
        def.maxLength = jointLength;
        def.bodyA = player;
        def.bodyB = anchor;
        def.localAnchorB.set(0.f, 0);
        def.localAnchorA.set(0.f, 0);
        //Gdx.app.log(TAG, "length " + def.maxLength);
        if (joint != null){
            //Gdx.app.log(TAG, "DESTROY OLD");
            world.destroyJoint(joint);
        }
        joint = world.createJoint(def);
        player.setLinearVelocity(-1f, 0);
    }

    public void setAnchor(Body anchor, Rock firstRock) {
        this.anchor = anchor;
        collisions.clear();
        collisionLength.clear();
        collisionDirections.clear();
        collisionCorners.clear();

        collisions.add(firstRock);
        //collisionPoints.add(new Vector2(.5f, -.5f).add(firstRock.body.getPosition()));
        collisionCorners.add(Rock.CORNER_BL);
        collisionDirections.add(1);
        collisionLength.add(1000f);
    }

    public void updateUnwind(Array<Rock> rocks, World world, ShapeRenderer shape) {
        if (joint == null) return;
        if (collisions.size <= 1) return;
        int i = collisions.size-1;
        Rock curr = collisions.get(i);
        Rock prev = collisions.get(i-1);
        a.set(curr.getCorner(collisionCorners.get(i)));
        b.set(prev.getCorner(collisionCorners.get(i-1)));
        int currSide = Intersector.pointLineSide(b, joint.getAnchorA(), a);
        int side = collisionDirections.get(i);
        if (side == currSide && !false){
            //Gdx.app.log(TAG, "unwind");
            float slack = 0;//def.maxLength - joint.getAnchorA().dst(joint.getAnchorB());
            //float newLength = a.dst(joint.getAnchorA()) + slack;
            Rock rock = prev;
            int closestCorner = collisionCorners.get(i-1);
            collisions.pop();
            collisionCorners.pop();
            collisionDirections.pop();
            float collisionLengthOld = collisionLength.pop();
            world.destroyJoint(joint);
            anchor.setTransform(rock.getBiggerCorner(closestCorner), 0);
            float newLength = player.getPosition().dst(rock.getBiggerCorner(closestCorner));
            if (!hasChangedLength)
                newLength = collisionLengthOld;
            //Gdx.app.log(TAG, "unwind old: " + def.maxLength + " new: " + newLength + "slack" + slack);
            def.maxLength = newLength;
            world.createJoint(def);

        }



    }

    public void extend(World world) {
        //Gdx.app.log(TAG, "extend" + def.maxLength);
        if (joint == null) return;
        world.destroyJoint(joint);
        def.maxLength+= .3f;
        world.createJoint(def);
        hasChangedLength = true;
    }

    public void stopExtending(World world) {
        if (joint == null) return;
        //Gdx.app.log("rope", "stop");
        world.destroyJoint(joint);
        Rock rock = collisions.get(collisions.size-1);
        int closestCorner = collisionCorners.get(collisions.size-1);
        anchor.setTransform(rock.getBiggerCorner(closestCorner), 0);
        float newLength = player.getPosition().dst(rock.getBiggerCorner(closestCorner));
        //Gdx.app.log(TAG, "stop old: " + def.maxLength + " new: " + newLength + "slack" );
        def.maxLength = newLength;
        world.createJoint(def);
    }

    public void draw(ShapeRenderer shape) {

        if (joint == null) shape.setColor(Color.RED);
        for (int i = 0; i < collisions.size-1; i++){
            Rock rock = collisions.get(i);
            int corner = collisionCorners.get(i);
            Rock rockB = collisions.get(i+1);
            int cornerB = collisionCorners.get(i+1);

            shape.line(rock.getBiggerCorner(corner), rockB.getBiggerCorner(cornerB));

        }
        if (joint == null) return;
        int i = collisions.size-1;
        Rock rock = collisions.get(i);
        int corner = collisionCorners.get(i);
        shape.line(rock.getBiggerCorner(corner), player.getPosition());

    }


}
