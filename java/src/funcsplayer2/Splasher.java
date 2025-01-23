package funcsplayer2;

import java.util.Random;
import battlecode.common.*;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Splasher extends Robot {
	
	static RobotController rc;
	static MapLocation nearestTower = null;
	static MapInfo nearestTowerInfo = null;
	static MapLocation prevTarget;
	static MapLocation my_target = null;
	static boolean reached_target = false;
	static boolean hasResource = false;

    Splasher(RobotController rc) throws GameActionException {
        super(rc);
        Splasher.rc = rc;
        
    }

    void play() throws GameActionException {
    	/*
    	MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
    	boolean moveAway = false;
    	for (MapInfo tile : nearbyTiles){
    		RobotInfo potentialTower = rc.senseRobotAtLocation(tile.getMapLocation());
			if (tile.hasRuin() && potentialTower != null) {
				if (potentialTower.getTeam() != rc.getTeam()) {
					nearestTower = potentialTower.location;
					nearestTowerInfo = tile;
					if (rc.canAttack(potentialTower.location)){
						rc.attack(potentialTower.location);
						moveAway = true;
					}
				}
			}
			else if (tile.hasRuin() && potentialTower == null){
				nearestTower = null;
			}
		}
		
    	if (nearestTower != null) {
    		if (moveAway) {
        		Direction dir = rc.getLocation().directionTo(nearestTower).opposite();
    			if (rc.canMove(dir)){
    				rc.move(dir);
    				rc.setIndicatorString("GOING AWAY");
    				}
    			return ;
        	}
			prevTarget = my_target;
			my_target = nearestTower;
		}
		*/
    	if (funcsplayer2.Util.distance(my_target, rc.getLocation()) <= 5) {
    		reached_target = true;
    		int newX;
    		int newY;
    		if (originalLocation.x <= mapWidth / 2) {
    			newX = mapWidth - my_target.x - 1;
    		}
    		else {
    			newX = mapWidth - my_target.x + 1;
    		}
    		if (originalLocation.y <= mapHeight / 2) {
    			newY = mapHeight - my_target.y - 1;
    		}
    		else {
    			newY = mapHeight - my_target.y + 1;
    		}
        	MapLocation rotation = new MapLocation(newX, newY);
        	my_target = rotation;
    	}
    	else if (!reached_target){
    		getTarget();
    	}
    	rc.setIndicatorString("MOVING TO TARGET AT " + my_target);
    	funcsplayer2.Pathfinding.move(my_target, true);
    }
    
    public static void getTarget() throws GameActionException {
    	int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;
    	
    	funcsplayer2.Pathfinding.init(rc);
    	funcsplayer2.Pathfinding.initTurn();
    	if (coord_x < coord_y) {
	    	if (coord_x < mapWidth / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(mapHeight);
	            my_target = new MapLocation(mapWidth - 1, randomInd);
	    	}
	    	else {
	    		Random rand = new Random();
	            int randomInd = rand.nextInt(mapHeight);
	            my_target = new MapLocation(0, randomInd);
	    	}
	        updateEnemyRobots(rc);
    	}
    	else {
    		if (coord_y < mapHeight / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(mapWidth);
	            my_target = new MapLocation(randomInd, mapHeight - 1);
	    	}
	    	else {
	    		Random rand = new Random();
	    		int randomInd = rand.nextInt(mapWidth);
	            my_target = new MapLocation(randomInd, 0);
	    	}
			// We can also move our code into different methods or classes to better organize it!
	        updateEnemyRobots(rc);
        }
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0){
                for (RobotInfo ally : allyRobots){
                    if (rc.canSendMessage(ally.location, enemyRobots.length)){
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }
}