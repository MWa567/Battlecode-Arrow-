package anotherfuncsplayer;

import battlecode.common.*;
import funcsplayer0.Explore;
import funcsplayer0.Pathfinding;
import funcsplayer0.Util;

import java.util.Random;

public class Mopper extends Robot {
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

    Mopper(RobotController rc) throws GameActionException {
        super(rc);
        Mopper.rc = rc;
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
            MapLocation ourLoc = rc.getLocation();

            //If we can see enemy paint, move towards it
            // for (MapInfo tile : nearbyTiles){
            //     if (tile.getPaint().isEnemy()){
            //         MapLocation targetLoc = tile.getMapLocation();
            //         Direction dir = ourLoc.directionTo(targetLoc);
            //         if (rc.canAttack(targetLoc)){
            //             rc.attack(targetLoc);
            //         }

            //         if (rc.canMove(dir)) {
            //             rc.move(dir);
            //             ourLoc.add(dir);
            //             // Clock.yield();

            //         }
            //         // Clock.yield();
            //     }
            // }

            RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
            //If we can see a soldier, follow it
            int numMoppers = 0;
            for (RobotInfo robo : nearbyRobots){
                MapLocation targetLoc = robo.location;
                Direction dir = rc.getLocation().directionTo(targetLoc);

                if (rc.canMopSwing(dir) && robo.team!=rc.getTeam()){
                    rc.mopSwing(dir);
                }

                if (robo.type==UnitType.SOLDIER){
                    if (rc.canMove(dir)) {
                        rc.move(dir);
                    }
                }


            }

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
						}
						if (rc.getPaint() < 100) {
							if (rc.canTransferPaint(tile.getMapLocation(), -100)) {
								rc.transferPaint(tile.getMapLocation(), -100);
							}
						}
                    }
                    else{
                        //retreat from enemy towers
                        Direction dir = rc.getLocation().directionTo(potentialTower.location).opposite();
                        if (rc.canMove(dir)){
                            rc.move(dir);
                        }

                    }
	    		}
	    	}

		    if (curRuin != null){
	        	MapLocation targetLoc = curRuin.getMapLocation();
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

            }
        }

    }
}
