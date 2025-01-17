package anotherfuncsplayer;

import java.util.Random;

import battlecode.common.*;
import funcsplayer0.Explore;
import funcsplayer0.Pathfinding;
import funcsplayer0.Util;

public class Mopper extends Robot {
	static MapLocation nearestTower;
	
    Mopper(RobotController rc) throws GameActionException {
        super(rc);
        MapRecorder.initMap(mapWidth, mapHeight);
    }

    void play() throws GameActionException {
        MapLocation myLocation = rc.getLocation();
        /*
        // Initialize symmetry analysis
        if (!Comm.isSymmetryConfirmed) {
            eliminateSymmetry(myLocation);
        }

        // Get target based on symmetry and enemy towers
        MapLocation target = findEnemyTower();
        if (target != null) {
            // Move toward the target using Pathfinding
            Pathfinding.init(rc);
            Pathfinding.initTurn();
            Pathfinding.move(target, true);
           }
            */
            // No target found, fallback to random movement
        anotherfuncsplayer.Util.init(rc);
        	anotherfuncsplayer.Explore.init(rc);
            anotherfuncsplayer.Pathfinding.init(rc);
            anotherfuncsplayer.Pathfinding.initTurn();
            target = Explore.getExploreTarget();
        	while (true) {
    		    MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
    	        
    		    MapLocation ourLoc = rc.getLocation();
    		    //If we can see enemy paint, move towards it
    		    for (MapInfo tile : nearbyTiles){
    		        if (tile.getPaint().isEnemy()){
    		            MapLocation targetLoc = tile.getMapLocation();
    		            Direction dir = ourLoc.directionTo(targetLoc);
    		            if (rc.canAttack(targetLoc)){
    		                rc.attack(targetLoc);
    		            }

    		            if (rc.canMove(dir)) {
    		                rc.move(dir);
    		                ourLoc.add(dir);

    		            }
    		        }
    		    }
    		    
    		    int coord_x = originalLocation.x;
    	    	int coord_y = originalLocation.y;
    	    	
    	    	if (coord_x < coord_y) {
    		    	if (coord_x < mapWidth / 2) {
    		            Random rand = new Random();
    		            int randomInd = rand.nextInt(mapHeight);
    		            MapLocation my_target = new MapLocation(mapWidth, randomInd);

    		            anotherfuncsplayer.Pathfinding.move(my_target, true);
    		    	}
    		    	else {
    		    		Random rand = new Random();
    		            int randomInd = rand.nextInt(mapHeight);
    		            MapLocation my_target = new MapLocation(0, randomInd);

    		            anotherfuncsplayer.Pathfinding.move(my_target, true);
    		    	}
    	    	}
    	    	else {
    	    		if (coord_y < mapHeight / 2) {
    		            Random rand = new Random();
    		            int randomInd = rand.nextInt(mapWidth);
    		            MapLocation my_target = new MapLocation(randomInd, mapHeight);

    		            anotherfuncsplayer.Pathfinding.move(my_target, true);
    		    	}
    		    	else {
    		    		Random rand = new Random();
    		    		int randomInd = rand.nextInt(mapWidth);
    		            MapLocation my_target = new MapLocation(randomInd, 0);

    		            anotherfuncsplayer.Pathfinding.move(my_target, true);
    		    	}
    	    	}
    		    Clock.yield();
        }
    }

    void eliminateSymmetry(MapLocation myLocation) throws GameActionException {
        // Check visible tiles for symmetry elimination
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo info : infos) {
            if (!Comm.isSymmetryConfirmed) {
                MapLocation loc = info.getMapLocation();
                char val = MapRecorder.vals[loc.x * rc.getMapHeight() + loc.y];
                if ((val & MapRecorder.SEEN_BIT) != 0) {
                    for (int sym = 0; sym < 3; sym++) {
                        if (!Comm.isSymEliminated[sym] && !validateSymmetry(myLocation, loc, sym)) {
                            Comm.eliminateSym(sym);
                        }
                    }
                }
            }
        }
    }

    boolean validateSymmetry(MapLocation origin, MapLocation test, int sym) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        // Calculate symmetric location
        MapLocation symLoc = switch (sym) {
            case Comm.SYM_ROTATIONAL -> new MapLocation(mapWidth - 1 - test.x, mapHeight - 1 - test.y);
            case Comm.SYM_VERTICAL -> new MapLocation(mapWidth - 1 - test.x, test.y);
            case Comm.SYM_HORIZONTAL -> new MapLocation(test.x, mapHeight - 1 - test.y);
            default -> null;
        };

        if (symLoc == null) return false;

        // Validate symmetry
        char originalVal = MapRecorder.vals[origin.x * mapHeight + origin.y];
        char symVal = MapRecorder.vals[symLoc.x * mapHeight + symLoc.y];
        return (originalVal & MapRecorder.CURRENT_MASK) == (symVal & MapRecorder.CURRENT_MASK);
    }

    MapLocation findEnemyTower() throws GameActionException {
        Team enemyTeam = rc.getTeam().opponent();
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
        MapLocation closestTarget = null;
        int closestDist = Integer.MAX_VALUE;

        // Prioritize enemy towers
        for (RobotInfo robot : enemyRobots) {
            if (robot.type.isTowerType()) {
                int dist = rc.getLocation().distanceSquaredTo(robot.location);
                if (dist < closestDist) {
                    closestTarget = robot.location;
                    closestDist = dist;
                }
            }
        }

        // Use symmetry scouting if no direct enemy structure found
        if (closestTarget == null && !Comm.isSymmetryConfirmed) {
            for (int sym = 0; sym < 3; sym++) {
                if (!Comm.isSymEliminated[sym]) {
                    MapLocation potentialTarget = calculateSymmetricLocation(rc.getLocation(), sym);
                    if (rc.onTheMap(potentialTarget)) {
                        closestTarget = potentialTarget;
                        break;
                    }
                }
            }
        }
        return closestTarget;
    }

    MapLocation calculateSymmetricLocation(MapLocation loc, int sym) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        return switch (sym) {
            case Comm.SYM_ROTATIONAL -> new MapLocation(mapWidth - 1 - loc.x, mapHeight - 1 - loc.y);
            case Comm.SYM_VERTICAL -> new MapLocation(mapWidth - 1 - loc.x, loc.y);
            case Comm.SYM_HORIZONTAL -> new MapLocation(loc.x, mapHeight - 1 - loc.y);
            default -> loc;
        };
    }

    void randomMove() throws GameActionException {
        // Fallback movement if no specific target is found
        Direction randomDir = Direction.values()[(int) (Math.random() * Direction.values().length)];
        if (rc.canMove(randomDir)) {
            rc.move(randomDir);
        }
    }
}