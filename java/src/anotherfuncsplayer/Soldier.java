package anotherfuncsplayer;

import battlecode.common.*;
import funcsplayer0.Explore;
import funcsplayer0.Pathfinding;
import funcsplayer0.Util;

import java.util.Random;

public class Soldier extends Robot {
	static int turnCount = 0;

    static final Random rng = new Random(6147);
    static MapLocation target;
    static MapLocation originalLocation = null;
    static MapLocation nearestTower;

    static RobotController rc;
    
    static boolean[][] paintTowerPattern = null;
    static boolean[][] moneyTowerPattern = null;
    static boolean[][] defenseTowerPattern = null;
    
    static MapLocation paintingRuinLoc = null;
    static UnitType paintingTowerType = null;
    
    static MapLocation needMopperAt;
    
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
    
    Soldier(RobotController rc) throws GameActionException {
        super(rc);
        Soldier.rc = rc;
        // Initialization that needs to happen when a Soldier instance is created
        MapRecorder.initMap(rc.getMapWidth(), rc.getMapHeight()); // Initialize the map recorder with map dimensions
        paintTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        moneyTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        defenseTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);
    }

    void play() throws GameActionException {
    	Util.init(rc);
        Explore.init(rc);
        Pathfinding.init(rc);
        Pathfinding.initTurn();
        target = Explore.getExploreTarget();
    	while (true) {
	    	// Sense information about all visible nearby tiles.
		    MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
		    // Search for a nearby ruin to complete.
	        
		    //If we can see a ruin, move towards it
		    MapInfo curRuin = null;
		    int ruinDist = 999999;
		    int towerDist = 999999;
		    for (MapInfo tile : nearbyTiles){
	    		if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) == null){
	            	int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	            	if (dist < ruinDist) {
		                curRuin = tile;
		                ruinDist = dist;
	            	}
	            }
	    		else if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) != null) {
	    			int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	    			if (dist < towerDist) {
	    				nearestTower = tile.getMapLocation();
	    				towerDist = dist;
	    			}
	    			if (rc.getPaint() < 30) {
	    				if (rc.canTransferPaint(tile.getMapLocation(), -100)) {
	    					rc.transferPaint(tile.getMapLocation(), -100);
	    				}
	    			}
	    			if (rc.canUpgradeTower(tile.getMapLocation())) {
	    				rc.upgradeTower(tile.getMapLocation());
	    				System.out.println("Tower was upgraded!");
	    			}
	    		}
	    	}
	    
		    if (curRuin != null){
	        	MapLocation targetLoc = curRuin.getMapLocation();
	            // Complete the ruin if we can.
	        	if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
	                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
	                rc.setTimelineMarker("Tower built", 0, 255, 0);
	                System.out.println("Built a paint tower at " + targetLoc + "!");
	            }
	        	else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
	                rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
	                rc.setTimelineMarker("Tower built", 0, 255, 0);
	                System.out.println("Built a money tower at " + targetLoc + "!");
	                }
	        	else if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)){
	                rc.completeTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc);
	                rc.setTimelineMarker("Tower built", 0, 255, 0);
	                System.out.println("Built a defense tower at " + targetLoc + "!");
	                }
	        	
	            Direction dir = rc.getLocation().directionTo(targetLoc);
	            
	            if (rc.getMovementCooldownTurns() > 10) {
	            	// Do nothing
	            }
	            else if (rc.canMove(dir)) {
	            	rc.move(dir);
	            }
	            else if (rc.canMove(dir.rotateRight())){
	            	rc.move(dir.rotateRight());
	            }
	            
	            UnitType randomTower = getNewTowerType(rc);
	            
	            if (randomTower == UnitType.LEVEL_ONE_PAINT_TOWER) {
	            	// Mark the pattern for paint tower
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc)){
	                	rc.markTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	            }
	            else if (randomTower == UnitType.LEVEL_ONE_MONEY_TOWER) {
	            	// Mark the pattern for money tower
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc)){
	                	rc.markTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	            }
	            else {
	            	// Mark the pattern for defense tower
	                MapLocation shouldBeMarked = curRuin.getMapLocation().subtract(dir);
	                if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)){
	                	rc.markTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc);
	                    System.out.println("Trying to build a tower at " + targetLoc);
	                }
	            }
	            
	            // Fill in any spots in the pattern with the appropriate paint.
	            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, -1)){
	                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
	                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
	                    if (rc.canAttack(patternTile.getMapLocation()))
	                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
	                }
	            }
	        }
		    
		    if (rc.getPaint() < 50) {
	        	System.out.println("GOING BACK FOR MORE PAINT");
		    	Pathfinding.setTarget(nearestTower);
		    	try {
		    		Pathfinding.move(nearestTower, false);
		    		} catch (Exception e) {
		    			System.out.println(rc.getType() + " Exception");
		    		}
		    }
        
	        if (rc.getMovementCooldownTurns() > 10) {
	        	Clock.yield();
	        }
	        if (curRuin == null) {
	        	try {
		    		Pathfinding.move(target, false);
		    		System.out.println("TELLING BOT TO MOVE TOWARD " + target);
		    		rc.setIndicatorString("Moving toward target at "+ Pathfinding.target);
	    		}
	        	catch (Exception e) {
	    			System.out.println(rc.getType() + " Exception");
	    		}
	        }
		    Clock.yield();
    	}
    }
    
    public static UnitType getNewTowerType(RobotController rc) {
    	Random random = new Random();
        int randomNumber = random.nextInt(7);

        boolean inMiddleX = 0.3 * mapWidth < rc.getLocation().x && rc.getLocation().x < 0.7 * mapWidth;
        boolean inMiddleY = 0.3 * mapHeight < rc.getLocation().y && rc.getLocation().y < 0.7 * mapHeight;
        if (inMiddleX && inMiddleY) {
    		return UnitType.LEVEL_ONE_DEFENSE_TOWER;
    	}
        else if (rc.getNumberTowers() < 4) {
        	return UnitType.LEVEL_ONE_MONEY_TOWER;
        }
    	else if (randomNumber == 0 || randomNumber == 1) {
	    	return UnitType.LEVEL_ONE_PAINT_TOWER;
    	}
	    else {
	    	return UnitType.LEVEL_ONE_MONEY_TOWER;
	    }
    }
}