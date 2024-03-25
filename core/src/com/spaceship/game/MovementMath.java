package com.spaceship.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;

import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.clamp;

public class MovementMath extends ApplicationAdapter {

    //simple trig

    //gets position based on direction and magnitude
    static public Vector3 lengthDir(double direction, double length){
        return new Vector3((float)(Math.cos(Math.toRadians(direction))*length),(float)(Math.sin(Math.toRadians(direction))*length),0);
    }

    //gets dir between points
    static public double hyp(double a, double b){
        return Math.sqrt(Math.pow(a,2)+Math.pow(b,2));
    }

    //gets dir between points
    static public double pointDir(Vector3 pointa, Vector3 pointb){
        return Math.toDegrees(Math.atan2((pointb.y-pointa.y),(pointb.x-pointa.x)));
    }

    //gets dist between points
    static public double pointDis(Vector3 pointa, Vector3 pointb){
        return Math.sqrt(Math.pow(pointb.y-pointa.y,2)+Math.pow(pointb.x-pointa.x,2));
    }

    static public int CheckCollisions(Circle colcirc, ArrayList collist, Vector3 offset){
        for(int i = 0; i< collist.size();i++) {
            if (collist.get(i) instanceof Circle) {
                Circle curcirc = (Circle) collist.get(i);
                if (!colcirc.equals(curcirc) && overlaps(DuplicateCirc(colcirc.x+offset.x,colcirc.y+offset.y,colcirc.radius), curcirc)) {
                    return i;
                }
            } else if (collist.get(i) instanceof Rectangle) {
                Rectangle currec = (Rectangle) collist.get(i);
                if (overlaps(currec,DuplicateCirc(colcirc.x+offset.x,colcirc.y+offset.y,colcirc.radius))) {
                    return i;
                }
            }
        }
        return -1;
    }
    static public int CheckCollisions(Circle colcirc, ArrayList collist, Vector3 offset, float colrad){
        for(int i = 0; i< collist.size();i++) {
            if (collist.get(i) instanceof Circle) {
                Circle curcirc = (Circle) collist.get(i);
                if (!colcirc.equals(curcirc) && overlaps(DuplicateCirc(colcirc.x + offset.x + (colcirc.radius-colrad),colcirc.y + offset.y + (colcirc.radius-colrad),colrad), curcirc)) {
                    return i;
                }
            } else if (collist.get(i) instanceof Rectangle) {
                Rectangle currec = (Rectangle) collist.get(i);
                if (overlaps(currec,DuplicateCirc(colcirc.x + offset.x + (colcirc.radius-colrad),colcirc.y + offset.y + (colcirc.radius-colrad),colrad))) {
                    return i;
                }
            }
        }
        return -1;
    }
    static public Rectangle DuplicateRect(Rectangle rect){
        return new Rectangle(rect.x,rect.y,rect.width,rect.height);
    }
    static public Circle DuplicateCirc(Circle circ){
        return new Circle(circ.x,circ.y,circ.radius);
    }
    static public Circle DuplicateCirc(float x, float y, float rad){
        return new Circle(x,y,rad);
    }
    static public int toDegrees() {
        if (Gdx.input.isKeyPressed(Input.Keys.W) && Gdx.input.isKeyPressed(Input.Keys.D)) {
            return 315;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) && Gdx.input.isKeyPressed(Input.Keys.W)) {
            return 45;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) && Gdx.input.isKeyPressed(Input.Keys.A)) {
            return 135;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && Gdx.input.isKeyPressed(Input.Keys.S)) {
            return 225;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            return 0;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            return 90;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            return 180;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            return 270;
        }
        return -1;
    }

    //checks an overlap between a circle and a rectangle
    public static boolean overlaps(Circle circ, Rectangle rect, double rectrot){
        Circle newcirc = MovementMath.DuplicateCirc(circ);
        Vector3 newpos = (MovementMath.addVect(MovementMath.lengthDir(
            MovementMath.pointDir(new Vector3(rect.x, rect.y, 0), new Vector3(circ.x, circ.y, 0))-(rectrot-90),
            MovementMath.pointDis(new Vector3(rect.x, rect.y, 0), new Vector3(circ.x, circ.y, 0))
        ), new Vector3(rect.x, rect.y, 0)));

        Vector3 addnewpos = MovementMath.lengthDir(
            MovementMath.pointDir(new Vector3(newcirc.x, newcirc.y, 0),newpos),
            MovementMath.pointDis(new Vector3(newcirc.x, newcirc.y, 0),newpos)
        );

        newcirc.x += addnewpos.x;
        newcirc.y += addnewpos.y;

        return overlaps(rect, newcirc);
    }


    static public boolean overlaps(Rectangle rect, Circle circ){
        double circDisX = Math.abs((circ.x) - (rect.x));
        double circDisY = Math.abs((circ.y) - (rect.y));

        if (circDisX > (rect.width/2 + circ.radius)) { return false; }
        if (circDisY > (rect.height/2 + circ.radius)) { return false; }

        if (circDisX <= (rect.width/2)) { return true; }
        if (circDisY <= (rect.height/2)) { return true; }

        double cornerDistance = Math.sqrt(Math.pow(circDisX - rect.width/2,2) + Math.pow(circDisY - rect.height/2,2));

        return (cornerDistance <= circ.radius);
    }

    //checks an overlap between 2 circles
    static public boolean overlaps(Circle circ, Circle circ2){
        return (pointDis(new Vector3(circ.x, circ.y, 0),new Vector3(circ2.x, circ2.y, 0))<=circ.radius+circ2.radius);
    }
    static public boolean overlaps(Circle circ, Circle circ2, Vector3 circoffset){
        return (pointDis(new Vector3(circ.x+circoffset.x, circ.y+circoffset.y, 0),new Vector3(circ2.x, circ2.y, 0))<=circ.radius+circ2.radius);
    }
    static public boolean overlaps(Rectangle r1, Rectangle r2) {
        return !(r1.x + r1.width < r2.x || r1.y + r1.height < r2.y || r1.x > r2.x + r2.width || r1.y > r2.y + r2.height);
    }

    public static Vector3 addVect(Vector3 v1, Vector3 v2){
        return new Vector3(v1.x+v2.x,v1.y+v2.y,v1.z+v2.z);
    }
    public static Vector3 addVect(Vector3 v1, Vector3 v2, Vector3 v3){
        return new Vector3(v1.x+v2.x+v3.x,v1.y+v2.y+v3.y,v1.z+v2.z+v3.z);
    }

    public static float getCameraAngle(OrthographicCamera cam) {
        return (float)-Math.toDegrees(Math.atan2(cam.up.x, cam.up.y)) + 180;
    }

    public static void setCamPos(OrthographicCamera cam, double pos){
        cam.rotate((MovementMath.getCameraAngle(cam) - (float)pos)+90);
    }
}