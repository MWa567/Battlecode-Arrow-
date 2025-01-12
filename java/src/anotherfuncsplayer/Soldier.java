package anotherfuncsplayer;

import battlecode.common.*;
import java.util.Random;

public class Soldier {
	/**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
	static int towerCount = 0;
	
    static final Random rng = new Random(6147);
    static MapLocation target;
    static MapLocation my_target;
    static MapLocation originalLocation;

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
    
	/**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException{
    	FloodFill.init(rc);
    	while (true) {
    		towerCount += 1;
	    	// Sense information about all visible nearby tiles.
	        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
	        // Search for a nearby ruin to complete.
	        
	        //If we can see a well, move towards it
	        MapInfo curRuin = null;
	        int curDist = 999999;
	        for (MapInfo tile : nearbyTiles){
	            if (tile.hasRuin()){
	            	int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	            	if (dist < curDist) {
		                curRuin = tile;
		                curDist = dist;
	            	}
	            }
	        }
	        if (curRuin != null){
	            MapLocation targetLoc = curRuin.getMapLocation();
	            Direction dir = rc.getLocation().directionTo(targetLoc);
	            if (rc.canMove(dir))
	            	rc.move(dir);
	            int randomNumber = towerCount % 6;
	            if (randomNumber == 0 || randomNumber == 1 || randomNumber == 2) {
	            	// Mark the pattern we need to draw to build a tower here if we haven't already.
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
	                    rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	                // Fill in any spots in the pattern with the appropriate paint.
	                for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
	                    if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
	                        boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
	                        if (rc.canAttack(patternTile.getMapLocation()))
	                            rc.attack(patternTile.getMapLocation(), useSecondaryColor);
	                    }
	                }
	            }
	            else {
	            	// Mark the pattern we need to draw to build a tower here if we haven't already.
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
	                    rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	                // Fill in any spots in the pattern with the appropriate paint.
	                for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
	                    if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
	                        boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
	                        if (rc.canAttack(patternTile.getMapLocation()))
	                            rc.attack(patternTile.getMapLocation(), useSecondaryColor);
	                    }
	                }
	            }
	            
	            // Complete the ruin if we can.
	            if (randomNumber == 0 || randomNumber == 1 || randomNumber == 2) {
	            	if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
	                    rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
	                    towerCount = 1;
	                    System.out.println(towerCount);
	                    rc.setTimelineMarker("Tower built", 0, 255, 0);
	                    System.out.println("Built a paint tower at " + targetLoc + "!");
	                }
	            }
	            else {
	            	if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
	                    rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
	                    towerCount = 1;
	                    System.out.println(towerCount);
	                    rc.setTimelineMarker("Tower built", 0, 255, 0);
	                    System.out.println("Built a money tower at " + targetLoc + "!");
	                }
	            }
	        }
	        FloodFill.move(rc.getLocation());
            }
        }
    }