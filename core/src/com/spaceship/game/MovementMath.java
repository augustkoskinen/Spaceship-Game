package com.spaceship.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Circle;

import java.util.ArrayList;

import static com.badlogic.gdx.math.MathUtils.clamp;

public class MovementMath extends ApplicationAdapter {
    //trig

    //double cos functions
    public static double cosf(double f){
        return (double) Math.cos(f);
    }
    public static double sinf(double f){
        return (double) Math.sin(f);
    }

    //simple trig

    //gets position based on direction and magnitude
    static public Vector3 lengthDir(double direction, double length){
        return new Vector3((float)(MovementMath.cosf(direction)*length),(float)(MovementMath.sinf(direction)*length),0);
    }

    //gets dir between points
    static public double pointDir(Vector3 pointa, Vector3 pointb){
        return (double) Math.atan2((pointb.y-pointa.y),(pointb.x-pointa.x));
    }

    //gets dist between points
    static public double pointDis(Vector3 pointa, Vector3 pointb){
        return (double) Math.sqrt(Math.pow(pointb.y-pointa.y,2)+Math.pow(pointb.x-pointa.x,2));
    }

    //gets mp of two points
    static public Vector3 midpoint(Vector3 pointa, Vector3 pointb){
        return new Vector3((pointa.x+pointb.x)/2,(pointa.y+pointb.y)/2,0);
    }
    static public int CheckCollisions(Rectangle colbox, ArrayList collist){
        for(int i = 0; i< collist.size();i++)
            if(collist.get(i) instanceof Circle) {
                if (overlaps(colbox, (Circle) collist.get(i))) {
                    return i;
                }
            }
            else if(collist.get(i) instanceof Rectangle)
                if(colbox!=(Rectangle)collist.get(i)&&overlaps(colbox,(Rectangle) collist.get(i))) {
                    return i;
                }
        return -1;
    }
    static public int CheckCollisions(Rectangle colbox, ArrayList collist, Vector3 offset, Vector3 bounds){
        for(int i = 0; i< collist.size();i++) {
            if (collist.get(i) instanceof Circle) {
                Circle curcirc = (Circle) collist.get(i);
                if (overlaps(DuplicateRect((colbox.x+offset.x+(16-bounds.x)/2),(colbox.y+offset.y+(16-bounds.y)/2),bounds.x,bounds.y), curcirc)) {
                    return i;
                }
            } else if (collist.get(i) instanceof Rectangle) {
                Rectangle currec = (Rectangle) collist.get(i);
                if (!colbox.equals(currec) && overlaps(DuplicateRect((colbox.x+offset.x+(16-bounds.x)/2),(colbox.y+offset.y+(16-bounds.y)/2),bounds.x,bounds.y), currec)) {
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
                if (!colcirc.equals(curcirc) && overlaps(DuplicateCirc(colcirc.x+offset.x-colcirc.radius+(colcirc.radius-colrad),colcirc.y+offset.y-colcirc.radius+(colcirc.radius-colrad),colrad), curcirc)) {//+(colcirc.radius-colrad)
                    return i;
                }
            } else if (collist.get(i) instanceof Rectangle) {
                Rectangle currec = (Rectangle) collist.get(i);
                if (overlaps(currec,DuplicateCirc(colcirc.x+offset.x-colcirc.radius+(colcirc.radius-colrad),colcirc.y+offset.y-colcirc.radius+(colcirc.radius-colrad),colrad))) {
                    return i;
                }
            }
        }
        return -1;
    }
    static public boolean CheckCollisions(int[][] map, Rectangle player, int extendamount){
         if (map[clamp((int) ((player.x + 16) / 32) + extendamount/2,0,map.length-1)][clamp((int) (player.y / 32) + extendamount/2,0,map[0].length-1)] !=0) {
             return true;
         }

         return false;
    }
    static public boolean CheckCollisions(int[][] map, Rectangle col, int extendamount, Vector3 offset, Vector3 bounds){
        if (map[clamp((int) ((col.x+offset.x+(16-bounds.x)) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+(16-bounds.y)) / 16) + extendamount/2,0,map[0].length-1)] == 1||
            map[clamp((int) ((col.x+offset.x+(16-bounds.x)) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+bounds.y) / 16) + extendamount/2,0,map[0].length-1)] == 1||
            map[clamp((int) ((col.x+offset.x+bounds.x) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+bounds.y) / 16) + extendamount/2,0,map[0].length-1)] == 1||
            map[clamp((int) ((col.x+offset.x+bounds.x) / 16) + extendamount/2,0,map.length-1)][clamp((int) ((col.y+offset.y+(16-bounds.y)) / 16) + extendamount/2,0,map[0].length-1)] == 1) {
            return true;
        }

        return false;
    }
    static public Rectangle DuplicateRect(Rectangle rect){
        return new Rectangle(rect.x,rect.y,rect.width,rect.height);
    }
    static public Rectangle DuplicateRect(float x, float y, float width, float height){
        return new Rectangle(x,y,width,height);
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
    static public boolean overlaps(Rectangle rect, Circle circ){
        double circDisX = Math.abs((circ.x+circ.radius) - (rect.x));
        double circDisY = Math.abs((circ.y+circ.radius) - (rect.y));

        if (circDisX > (rect.width/2 + circ.radius)) { return false; }
        if (circDisY > (rect.height/2 + circ.radius)) { return false; }

        if (circDisX <= (rect.width/2)) { return true; }
        if (circDisY <= (rect.height/2)) { return true; }

        double cornerDistance = Math.sqrt(Math.pow(circDisX - rect.width/2,2) + Math.pow(circDisY - rect.height/2,2));

        return (cornerDistance <= circ.radius);
    }

    //checks an overlap between 2 circles
    static public boolean overlaps(Circle circ, Circle circ2){
        return (pointDis(new Vector3(circ.x+circ.radius, circ.y+circ.radius, 0),new Vector3(circ2.x+circ2.radius, circ2.y+circ2.radius, 0))<=circ.radius+circ2.radius);
    }
    static public boolean overlaps (Rectangle r1, Rectangle r2) {
        return !(r1.x + r1.width < r2.x || r1.y + r1.height < r2.y || r1.x > r2.x + r2.width || r1.y > r2.y + r2.height);
    }

    //gets the slope of two points
    static public Vector3 getSlope(Vector3 pointa, Vector3 pointb){
        Vector3 slope = new Vector3(pointa.x-pointb.x, pointa.y-pointb.y, 0);
        return slope;
    }

    //checks if there's a circle between two points
    static public boolean lineCol(Vector3 pointa, Vector3 pointb, Circle circ){
        double rate = 10;
        double dist = pointDis(pointa, pointb);
        double repeat =(double)Math.ceil(dist/rate);
        Vector3 curpoint = pointa;
        Vector3 velocity = getSlope(pointa,pointb);
        velocity.x /= repeat;
        velocity.y /= repeat;
        for(int i = 0; i<repeat;i++){
            if(pointDis(curpoint, new Vector3(circ.x+circ.radius, circ.y+circ.radius, 0))<+circ.radius)
                return true;
            curpoint.x+=velocity.x;
            curpoint.y+=velocity.y;
        }

        return false;
    }

    static public Vector3 averagePos(ArrayList<SpaceshipGameManager.Player> playerlist) {
        double sumx = 0;
        double sumy = 0;
        for(int i  = 0; i<playerlist.size();i++) {
            sumx+=playerlist.get(i).sprite.x+8;
            sumy+=playerlist.get(i).sprite.y+8;
        }
        return new Vector3((float)(sumx/playerlist.size()),(float)(sumy/playerlist.size()),0);
    }
    static public Vector3 averagePosOnline(ArrayList<SpaceshipGameManager.Player> playerlist) {
        double sumx = 0;
        double sumy = 0;
        for(int i  = 0; i<playerlist.size();i++) {
            sumx+=playerlist.get(i).sprite.x+8;
            sumy+=playerlist.get(i).sprite.y+8;
        }
        return new Vector3((float)(sumx/playerlist.size()),(float)(sumy/playerlist.size()),0);
    }
    static public double furthestDist(ArrayList<SpaceshipGameManager.Player> playerlist) {
        double furthestdist = 0;
        for(int i  = 0; i<playerlist.size()-1;i++)
            for (int j = i+1; j < playerlist.size(); j++) {
                double dist = pointDis(playerlist.get(i).sprite.getPosition(),playerlist.get(j).sprite.getPosition());
                if (dist > furthestdist)
                    furthestdist = dist;
            }

        return furthestdist;
    }
    static public double furthestDistOnline(ArrayList<SpaceshipGameManager.Player> playerlist) {
        double furthestdist = 0;
        for(int i  = 0; i<playerlist.size()-1;i++)
            for (int j = i+1; j < playerlist.size(); j++) {
                double dist = pointDis(playerlist.get(i).sprite.getPosition(),playerlist.get(j).sprite.getPosition());
                if (dist > furthestdist)
                    furthestdist = dist;
            }

        return furthestdist;
    }

    public static Vector3 addVect(Vector3 v1, Vector3 v2){
        return new Vector3(v1.x+v2.x,v1.y+v2.y,v1.z+v2.z);
    }
    public static Vector3 addVect(Vector3 v1, Vector3 v2, Vector3 v3){
        return new Vector3(v1.x+v2.x+v3.x,v1.y+v2.y+v3.y,v1.z+v2.z+v3.z);
    }

    public static float getCameraAngle(OrthographicCamera cam) {
        return ((float) Math.toDegrees(-Math.atan2(cam.up.x, cam.up.y))) + 180;
    }

    public static void setCamPos(OrthographicCamera cam, double pos){
        cam.rotate((MovementMath.getCameraAngle(cam) - (float)pos)+90);
    }

    /*
    static boolean polygonIntersection(Polygon a, Polygon b)
    {
        for (int x=0; x<2; x++)
        {
            Polygon polygon = (x==0) ? a : b;

            for (int i1=0; i1<polygon.npoints; i1++)
            {
                int   i2 = (i1 + 1) % polygon.npoints;
                Point p1 = new Point(polygon.xpoints[i1],polygon.ypoints[i1]);
                Point p2 = new Point(polygon.xpoints[i2],polygon.ypoints[i2]);

                Point normal = new Point(p2.y - p1.y, p1.x - p2.x);

                double minA = Double.POSITIVE_INFINITY;
                double maxA = Double.NEGATIVE_INFINITY;

                for (int i = 0; i < a.npoints; i++)
                {
                    double projected = normal.x * a.xpoints[i] + normal.y * a.ypoints[i];

                    if (projected < minA)
                        minA = projected;
                    if (projected > maxA)
                        maxA = projected;
                }

                double minB = Double.POSITIVE_INFINITY;
                double maxB = Double.NEGATIVE_INFINITY;

                for (int i = 0; i < b.npoints; i++) {
                    double projected = normal.x * b.xpoints[i] + normal.y * b.xpoints[i];

                    if (projected < minB)
                        minB = projected;
                    if (projected > maxB)
                        maxB = projected;
                }

                if (maxA < minB || maxB < minA)
                    return false;
            }
        }

        return true;
    }
    */
}