package funcsplayer0;

import battlecode.common.*;
import java.util.Random;

public class Util {
	
	static Random rng;
	
    private static RobotController rc;

    static int MAP_WIDTH;
    static int MAP_HEIGHT;
    static int MAP_AREA;
    static int MAP_MAX_DIST_SQUARED;
    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    public static void init(RobotController r) {
        rc = r;
        rng = new Random(rc.getRoundNum() * 23981 + rc.getID() * 10289);
        MAP_HEIGHT = rc.getMapHeight();
        MAP_WIDTH = rc.getMapWidth();
        MAP_AREA = MAP_HEIGHT * MAP_WIDTH;
        MAP_MAX_DIST_SQUARED = MAP_HEIGHT * MAP_HEIGHT + MAP_WIDTH * MAP_WIDTH;
    }
    
    static public int distance(MapLocation A, MapLocation B) {
        return Math.max(Math.abs(A.x - B.x), Math.abs(A.y - B.y));
    }
    
    // Returns the location on the opposite side from loc wrt to your own location
    static MapLocation invertLocation(MapLocation loc) {
        int dx = loc.x - rc.getLocation().x;
        int dy = loc.y - rc.getLocation().y;
        return rc.getLocation().translate(-dx, -dy);
    }
    
    public static MapLocation[] guessEnemyLoc(MapLocation ourLoc) throws GameActionException {
        MapLocation[] results;
        int height = rc.getMapHeight();
        int width = rc.getMapWidth();
        MapLocation verticalFlip = new MapLocation(ourLoc.x, height - ourLoc.y - 1);
        MapLocation horizontalFlip = new MapLocation(width - ourLoc.x - 1, ourLoc.y);
        MapLocation rotation = new MapLocation(width - ourLoc.x - 1, height - ourLoc.y - 1);
        results = new MapLocation[] { verticalFlip, horizontalFlip, rotation };
        return results;
    }
    
    static int clip(int n, int lo, int hi) {
        return Math.min(Math.max(n, lo), hi);
    }
    
    static double clip(double n, double lo, double hi) {
        return Math.min(Math.max(n, lo), hi);
    }
    
    public static MapLocation reflectX(MapLocation loc) {
    	return new MapLocation(-loc.x, loc.y);
    }
    
    public static MapLocation reflectY(MapLocation loc) {
    	return new MapLocation(loc.x, -loc.y);
    }
    
    static MapLocation clipToWithinMap(MapLocation loc) {
        return new MapLocation(clip(loc.x, 0, MAP_WIDTH), clip(loc.y, 0, MAP_HEIGHT));
    }
    
    public static boolean onTheMap(MapLocation location) {
        return 0 <= location.x && location.x < MAP_WIDTH &&
                0 <= location.y && location.y < MAP_HEIGHT;
    }
    static Direction randomDirection() {
        return directions[Util.rng.nextInt(Util.directions.length)];
    }
    static Direction randomDirection(Direction[] newDirections) {
        return newDirections[Util.rng.nextInt(newDirections.length)];
    }
}