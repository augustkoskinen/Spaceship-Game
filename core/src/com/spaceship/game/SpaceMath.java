package com.spaceship.game;

import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class SpaceMath {
    private static final double GRAVITATIONAL_CONSTANT = 100;//6.674e-11
    public static double getGravityForce(double m2, double d){
        return ((GRAVITATIONAL_CONSTANT*m2)/Math.pow(d,2));
    }
    public static double getOrbitalSpeed(double m, double d){
        return Math.sqrt((GRAVITATIONAL_CONSTANT*m)/d);
    }

    public static Vector3 getNetGravity(Vector3 ppos, ArrayList<SpaceshipGameManager.Planet> plist){
        Vector3 gvect = new Vector3();
        for (SpaceshipGameManager.Planet planet : plist){
            double gdis = MovementMath.pointDis(planet.centerpos,ppos);
            if(gdis<512*4) {
                double gdir = MovementMath.pointDir(ppos,planet.centerpos);
                double gforce = getGravityForce(planet.mass,gdis);
                gvect=new Vector3((float)(gvect.x+Math.cos(gdir)*gforce),(float)(gvect.y+Math.sin(gdir)*gforce),0);
            }
        }
        return gvect;
    }

    public static SpaceshipGameManager.Planet getClosestPlanet(Vector3 ppos, ArrayList<SpaceshipGameManager.Planet> plist){
        double dis = Integer.MAX_VALUE;
        int pi = -1;
        for(int i = 0; i<plist.size();i++) {
            double curdis = MovementMath.pointDis(plist.get(i).centerpos,ppos);
            if(curdis<dis) {
                pi = i;
                dis = curdis;
            }
        }
        return plist.get(pi);
    }
}