package com.spaceship.game;

import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class SpaceMath {
    private static final double GRAVITATIONAL_CONSTANT = 1e13;
    public static double getGravityForce(double m1, double m2, double d){
        return ((GRAVITATIONAL_CONSTANT*m1*m2)/Math.pow(d,5));
    }
    public static double getOrbitalSpeed(double m, double d){
        return Math.sqrt((GRAVITATIONAL_CONSTANT*m)/d);
    }

    public static Vector3 getNetGravity(Vector3 ppos, ArrayList<SpaceshipGameManager.Planet> plist, double objmass){
        Vector3 gvect = new Vector3();
        for (SpaceshipGameManager.Planet planet : plist){
            double gdis = MovementMath.pointDis(planet.getPosition(),ppos);
            if(gdis<512*4) {
                double gdir = MovementMath.pointDir(ppos,planet.getPosition());
                double gforce = getGravityForce(planet.mass, objmass, gdis);
                gvect=new Vector3((float)(gvect.x+Math.cos(Math.toRadians(gdir))*gforce),(float)(gvect.y+Math.sin(Math.toRadians(gdir))*gforce),0);
            }
        }
        return gvect;
    }

    public static SpaceshipGameManager.Planet getClosestPlanet(Vector3 ppos, ArrayList<SpaceshipGameManager.Planet> plist){
        double maxmag = 0;
        int pi = -1;
        for(int i = 0; i<plist.size();i++) {
            double curmag = getGravityForce(plist.get(i).mass, 1, MovementMath.pointDis(plist.get(i).getPosition(),ppos));
            if(curmag>maxmag) {
                pi = i;
                maxmag = curmag;
            }
        }
        return plist.get(pi);
    }
}