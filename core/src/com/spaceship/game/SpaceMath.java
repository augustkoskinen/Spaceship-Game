package com.spaceship.game;

public class SpaceMath {
    private static final double GRAVITATIONAL_CONSTANT = 6.674e-11;
    public static double getGravityForce(double m1, double m2, double d){
        return ((GRAVITATIONAL_CONSTANT*m1*m2)/Math.pow(d,2));
    }
    public static double getOrbitalSpeed(double m, double d){
        return Math.sqrt((GRAVITATIONAL_CONSTANT*m)/d);
    }
}