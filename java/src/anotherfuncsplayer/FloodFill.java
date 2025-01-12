package anotherfuncsplayer;

import battlecode.common.*;
import java.util.Random;

import anotherfuncsplayer.Pathfinding.BugNav;

import java.util.ArrayList;
import java.util.List;
public class FloodFill {
    static RobotController rc;
    static MapLocation target = null;
    static boolean[] impassable = null;
    static final Random rng = new Random(6147);
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };
    public static void init(RobotController r) {
        rc = r;
    }
    
    public static void move(MapLocation location) throws GameActionException {
    	List<MapLocation> to_paint = new ArrayList<>();
    	to_paint.add(location);
    	List<MapLocation> visited = new ArrayList<>();
    	visited.add(location);
    	
    	while (to_paint.isEmpty() == false) {
    		int lastIndex = to_paint.size() - 1;
            MapLocation thisLoc = to_paint.remove(lastIndex);
            MapInfo currentTile;
			currentTile = rc.senseMapInfo(thisLoc);
	        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
				rc.attack(rc.getLocation());
				}
            }
        	for (Direction dir: directions) {
        		rc.move(dir);
        		MapLocation nextLoc = rc.getLocation().add(dir);
        		if ((visited.contains(nextLoc) == false) && (rc.onTheMap(nextLoc) == true)) {
        			to_paint.add(nextLoc);
        		}
        	}
    	}
    }