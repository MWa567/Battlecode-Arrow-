package anotherfuncsplayer;

import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.PaintType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import funcsplayer0.Util;

public class Soldier extends Robot {
	static int turnCount = 0;

    static final Random rng = new Random(6147);
    static MapLocation target;
    static MapLocation originalLocation = null;
    static MapLocation nearestTower;
    static MapLocation prevTarget = null;

    static boolean[][] paintTowerPattern = null;
    static boolean[][] moneyTowerPattern = null;
    static boolean[][] defenseTowerPattern = null;
    MapLocation curResource = null;
    boolean override = false;

    static MapLocation paintingRuinLoc = null;
    static UnitType paintingTowerType = null;

    static boolean hasEnemyPaint = false;
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
        Robot.rc = rc;
        // Initialization that needs to happen when a Soldier instance is created
        MapRecorder.initMap(rc.getMapWidth(), rc.getMapHeight()); // Initialize the map recorder with map dimensions
        paintTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER);
        moneyTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER);
        defenseTowerPattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER);
    }

    @Override
	void play() throws GameActionException {
    	if (rc.getMapHeight() <= 20 || rc.getMapWidth() <= 20) {
    		funcsplayer0.RobotPlayer.runSoldier(rc);
    	}
    	anotherfuncsplayer.Util.init(rc);
    	anotherfuncsplayer.Explore.init(rc);
        anotherfuncsplayer.Pathfinding.init(rc);
        anotherfuncsplayer.Pathfinding.initTurn();
        target = anotherfuncsplayer.Explore.getExploreTarget();
    	while (true) {
	    	// Sense information about all visible nearby tiles.
		    MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
		    // Search for a nearby ruin to complete.

		    //If we can see a ruin, move towards it
		    MapInfo curRuin = null;
		    int ruinDist = 999999;
		    int towerDist = 999999;
		    for (MapInfo tile : nearbyTiles){
		    	RobotInfo potentialTower = rc.senseRobotAtLocation(tile.getMapLocation());
		    	if (tile.hasRuin() && potentialTower == null){
	            	int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	            	if (dist < ruinDist) {
		                curRuin = tile;
		                ruinDist = dist;
	            	}
	            }
		    	else if (tile.hasRuin() && potentialTower != null) {
					if (potentialTower.getTeam() == rc.getTeam()){
						int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
						if (dist < towerDist) {
							nearestTower = tile.getMapLocation();
							towerDist = dist;
							if (rc.getPaint() < 50) {
								if (rc.canTransferPaint(nearestTower, -75)) {
									rc.transferPaint(nearestTower, -75);
								}
							}
						}
						if (rc.canUpgradeTower(tile.getMapLocation())) {
							rc.upgradeTower(tile.getMapLocation());
							System.out.println("Tower was upgraded!");
						}
					}
	    		}
	    	}
		    Clock.yield();
		    
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
	        	else if (rc.getNumberTowers() >= 4 && rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER, targetLoc)){
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
	            for (MapInfo patternTile : rc.senseNearbyMapInfos(targetLoc, 8)){
	            	rc.setIndicatorString("TARGET IS " + targetLoc);
	                if (!patternTile.getPaint().isEnemy() && patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
	                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
	                    if (rc.canAttack(patternTile.getMapLocation())) {
							rc.attack(patternTile.getMapLocation(), useSecondaryColor);
						}
	                }
	                else if (patternTile.getPaint().isEnemy()) {
	                	hasEnemyPaint = true;
	                	needMopperAt = patternTile.getMapLocation();
	                	rc.setIndicatorString("DETECTED ENEMY PAINT");
	                	curRuin = null;
	                }
	            }
	        }

	        if (rc.getMovementCooldownTurns() > 10) {
	        	Clock.yield();
	        }
	        if (mapWidth > 50 || mapHeight > 50) {
	        	if ((rc.getLocation().x > mapWidth * 0.7 || rc.getLocation().x < mapWidth * 0.3)
		        		&& (rc.getLocation().y > mapHeight * 0.7 || rc.getLocation().y < mapHeight * 0.3)
		        		&& rc.getLocation().x % 5 == 0 && rc.getLocation().y % 5 == 0) {
		        	boolean has_mark = false;
		        	for (MapInfo patternTile : rc.senseNearbyMapInfos(rc.getLocation(), -1)){
		                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
		                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
		                    if (patternTile.getMark() != PaintType.EMPTY) {
		                    	has_mark = true;
		                    }
		                    if (rc.canAttack(patternTile.getMapLocation())) {
								rc.attack(patternTile.getMapLocation(), useSecondaryColor);
							}
		                }
		            }

		        	if (rc.getNumberTowers() > 8 && !has_mark && rc.canMarkResourcePattern(rc.getLocation())) {
		        		curResource = rc.getLocation();
		        		target = curResource;
		        		rc.markResourcePattern(curResource);
		        	}
		        }

		        if (curResource != null) {
		        	Direction dir = rc.getLocation().directionTo(curResource);

		            if (rc.getMovementCooldownTurns() > 10) {
		            	// Do nothing
		            }
		            else if (rc.canMove(dir)) {
		            	rc.move(dir);
		            }
		            else if (rc.canMove(dir.rotateRight())){
		            	rc.move(dir.rotateRight());
		            }
		            if (rc.canCompleteResourcePattern(curResource)) {
		            	rc.completeResourcePattern(curResource);
		            	curResource = null;
		            	break;
		            }
		        	for (MapInfo patternTile : rc.senseNearbyMapInfos(curResource, -1)){
		                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
		                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
		                    if (rc.canAttack(patternTile.getMapLocation())) {
								rc.attack(patternTile.getMapLocation(), useSecondaryColor);
							}
		                }
		            }
		        }
	        }

	        if (curRuin == null && curResource == null) {
	        	try {
	        		if (anotherfuncsplayer.Util.distance(rc.getLocation(), target) <= 5 || !rc.isMovementReady()) {
	        			MapLocation oldTarget = target;
	        			while (Util.distance(anotherfuncsplayer.Explore.getExploreTarget(), oldTarget) <= 20) {
	        				anotherfuncsplayer.Explore.getNewTarget(10);
	        			}
	            		target = anotherfuncsplayer.Explore.exploreTarget;
	        		}
	        		rc.setIndicatorString("Moving toward target at "+ target);
	        		anotherfuncsplayer.Pathfinding.move(target, false);
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
    	else if (randomNumber == 0 || randomNumber == 1 || randomNumber == 2) {
	    	return UnitType.LEVEL_ONE_PAINT_TOWER;
    	}
	    else {
	    	return UnitType.LEVEL_ONE_MONEY_TOWER;
	    }
    }
}