package funcsplayer2;

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

    static MapLocation paintingRuinLoc = null;
    static UnitType paintingTowerType = null;
    static int paintingTurns = 0;
    static int turnsWithoutAttack = 0;
    
    MapLocation curResource = null;
    boolean override = false;

    static boolean hasEnemyPaint = false;
    static MapLocation needMopperAt;
    
    static boolean weaving = false;
	static boolean moveAway = false;
	static MapLocation myEnemyTower = null;

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
    	funcsplayer2.Util.init(rc);
    	funcsplayer2.Explore.init(rc);
    	funcsplayer2.BugNav.init(rc);
        funcsplayer2.Pathfinding.init(rc);
        funcsplayer2.Pathfinding.initTurn();
        target = funcsplayer2.Explore.getExploreTarget();
    	while (true) {
    		
    		if (rc.getPaint() < 100) {
    			anotherfuncsplayer.Pathfinding.move(nearestTower, false);
				if (rc.canTransferPaint(nearestTower, -75)) {
					rc.transferPaint(nearestTower, -75);
				}
				return ;
			}
    		
    		if (weaving) {
				Direction dir = rc.getLocation().directionTo(myEnemyTower);
				if (moveAway) {
					if (rc.canAttack(myEnemyTower)){
						rc.setIndicatorString("ATTACKING1");
						rc.attack(myEnemyTower);
					}
					dir = dir.opposite();
					if (rc.canMove(dir)){
						rc.move(dir);
						rc.setIndicatorString("GOING AWAY");
					}

				}
				else {
					if (rc.canMove(dir)){
						rc.move(dir);
						rc.setIndicatorString("GOING TOWARDS" + myEnemyTower);
					}
					if (rc.canAttack(myEnemyTower)){
						rc.setIndicatorString("ATTACKING2");
						rc.attack(myEnemyTower);
					}
					// else if(rc.canMove(dir.opposite())){
					// 	rc.move(dir.opposite());
					// }
					if (rc.senseRobotAtLocation(myEnemyTower)==null){
						weaving = false;
					}

				}
				moveAway = !moveAway;
				return;
			}
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
							if (rc.getPaint() < 100) {
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
					else{
						//enemy tower!!
						Direction dir = rc.getLocation().directionTo(potentialTower.location);
						myEnemyTower = potentialTower.location;
						rc.setIndicatorString("updating myEnemyTower" + myEnemyTower);
						weaving = true;
						// if (!weaving && potentialTower.getType()!=UnitType.LEVEL_TWO_DEFENSE_TOWER && potentialTower.getType()!=UnitType.LEVEL_ONE_DEFENSE_TOWER && potentialTower.getType()!=UnitType.LEVEL_THREE_DEFENSE_TOWER) weaving=true;
						if (rc.canMove(dir)){
							rc.move(dir);
							moveAway = true;
						}
						if (rc.canAttack(myEnemyTower)){
							rc.attack(myEnemyTower);
						}
						return;
					}
	    		}
	    	}
		    
		    if (curRuin != null){
		    	paintingRuinLoc = curRuin.getMapLocation();
                paintingTowerType = getNewTowerType(rc);
                turnsWithoutAttack = 0;
                paintingTurns = 0;
                runPaintPattern(rc);
                return ;
	        }

	        if (rc.getMovementCooldownTurns() > 10) {
	        	Clock.yield();
	        }
	        if (true) {
	        	if ((rc.getLocation().x > mapWidth * 0.4 || rc.getLocation().x < mapWidth * 0.6)
		        		&& (rc.getLocation().y > mapHeight * 0.6 || rc.getLocation().y < mapHeight * 0.4)
		        		&& rc.getLocation().x % 4 == 0 && rc.getLocation().y % 4 == 3) {
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

		        	if (rc.getNumberTowers() > 5 && !has_mark && rc.canMarkResourcePattern(rc.getLocation())) {
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
	        		if (funcsplayer2.Util.distance(rc.getLocation(), target) <= 5 || !rc.isMovementReady()) {
	        			MapLocation oldTarget = target;
	        			while (Util.distance(funcsplayer2.Explore.getExploreTarget(), oldTarget) <= Math.max(mapHeight, mapWidth) / 2) {
	        				funcsplayer2.Explore.getNewTarget(10);
	        			}
	            		target = funcsplayer2.Explore.exploreTarget;
	        		}
	        		rc.setIndicatorString("Moving toward target at "+ target);
	        		funcsplayer2.Pathfinding.move(target, false);
	    		}
	        	catch (Exception e) {
	    			System.out.println(rc.getType() + " Exception");
	    		}
	        }
		    Clock.yield();
    	}
    }
    
    public static boolean isWithinPattern(MapLocation ruinLoc, MapLocation paintLoc) {
    	return Math.abs(paintLoc.x - ruinLoc.x) <= 2 && Math.abs(paintLoc.y - ruinLoc.y) <= 2 && !ruinLoc.equals(paintLoc);
    }
    
    public static boolean getIsSecondary(MapLocation ruinLoc, MapLocation paintLoc, UnitType towerType) {
        if(!isWithinPattern(ruinLoc, paintLoc)) return false;
        int col = paintLoc.x - ruinLoc.x + 2;
        int row = paintLoc.y - ruinLoc.y + 2;
        if (towerType == UnitType.LEVEL_ONE_PAINT_TOWER) {
        	return paintTowerPattern[row][col];
        }
        else if (towerType == UnitType.LEVEL_ONE_MONEY_TOWER) {
        	return moneyTowerPattern[row][col];
        }
        else {
        	return defenseTowerPattern[row][col];
        }
    }
    
    public static void runPaintPattern(RobotController rc) throws GameActionException {
    	/*
    	if(paintingTurns % 3 == 0) {
            Direction toRuin = rc.getLocation().directionTo(paintingRuinLoc);
            Direction tangent = toRuin.rotateRight().rotateRight();
            int distance = rc.getLocation().distanceSquaredTo(paintingRuinLoc);

            if(distance > 4) {
                //don't make the circle too big
                tangent = tangent.rotateLeft();
            }

            if(rc.canMove(tangent)) rc.move(tangent);
            else if(rc.canMove(tangent.rotateRight())) rc.move(tangent.rotateRight());
            else if(rc.canMove(tangent.rotateLeft())) rc.move(tangent.rotateLeft());
        }
        */
    	rc.setIndicatorString("SEARCHING FOR NEARBY ROBOTS");
    	for (RobotInfo robot: rc.senseNearbyRobots(-1)) {
	    	if (robot.team == rc.getTeam() && robot.type == UnitType.SOLDIER &&
	    			paintingRuinLoc.distanceSquaredTo(robot.location) < rc.getLocation().distanceSquaredTo(paintingRuinLoc)) {
	    		funcsplayer2.Pathfinding.move(target, false);
	    		return ;
	    	}
	    }
    	
        //paint tiles for the pattern
        if(rc.isActionReady()) {
            MapInfo[] infos = rc.senseNearbyMapInfos();
            boolean attacked = false;
            for(MapInfo info : infos) {
                MapLocation paintLoc = info.getMapLocation();
                boolean shouldBeSecondary = getIsSecondary(paintingRuinLoc, paintLoc, paintingTowerType);
                if(rc.canAttack(paintLoc)
                        && (info.getPaint() == PaintType.EMPTY || info.getPaint().isSecondary() != shouldBeSecondary) 
                        && isWithinPattern(paintingRuinLoc, paintLoc)) {
                    rc.attack(paintLoc, shouldBeSecondary);
                    attacked = true;
                    turnsWithoutAttack = 0;
                    break;
                }
            }
            if(!attacked) turnsWithoutAttack++;
        }
        
        Direction dir = rc.getLocation().directionTo(paintingRuinLoc);

        if (rc.getMovementCooldownTurns() > 10) {
        	// Do nothing
        }
        else if (rc.canMove(dir)) {
        	rc.move(dir);
        }
        else if (rc.canMove(dir.rotateRight())){
        	rc.move(dir.rotateRight());
        }

        if (rc.canCompleteTowerPattern(paintingTowerType, paintingRuinLoc)){
            rc.completeTowerPattern(paintingTowerType, paintingRuinLoc);
        }
    }
    
    public static UnitType getNewTowerType(RobotController rc) {
    	Random random = new Random();
        int randomNumber = random.nextInt(7);

        boolean inMiddleX = 0.3 * mapWidth < paintingRuinLoc.x && paintingRuinLoc.x < 0.7 * mapWidth;
        boolean inMiddleY = 0.3 * mapHeight < paintingRuinLoc.y && paintingRuinLoc.y < 0.7 * mapHeight;
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