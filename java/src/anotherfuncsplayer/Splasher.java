package anotherfuncsplayer;

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
	static MapLocation prevTarget;
	static MapLocation my_target = null;
	static boolean reached_target = false;
	static boolean hasResource = false;
	static boolean hasWall = false;
	
	static boolean towerSpotted = false;
	static MapLocation myEnemyTower = null;

    Splasher(RobotController rc) throws GameActionException {
        super(rc);
        Splasher.rc = rc;
        
    }

    void play() throws GameActionException {
    	if (rc.canCompleteResourcePattern(rc.getLocation())) {
			rc.completeResourcePattern(rc.getLocation());
		}
    	if(towerSpotted){
    		prevTarget = myEnemyTower;
			if (rc.canAttack(myEnemyTower)){
				rc.attack(myEnemyTower);
			}
			else if(rc.isActionReady()){
				anotherfuncsplayer.Pathfinding.move(myEnemyTower, true);
			}
			if (rc.canAttack(myEnemyTower)){
				rc.attack(myEnemyTower);
			}
			if (rc.canAttack(myEnemyTower)){
				rc.attack(myEnemyTower);
			}
			if (rc.canAttack(myEnemyTower)){
				rc.attack(myEnemyTower);
			}
			if (rc.senseRobotAtLocation(myEnemyTower)==null){
				towerSpotted = false;
			}
			return;
		}
    	
    	MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
    	// boolean moveAway = false;
    	for (MapInfo tile : nearbyTiles){
    		RobotInfo potentialTower = rc.senseRobotAtLocation(tile.getMapLocation());
			if (tile.hasRuin() && potentialTower != null) {
				if (potentialTower.getTeam() != rc.getTeam()) {
					// enemy tower
					myEnemyTower = potentialTower.location;
					Direction dir = rc.getLocation().directionTo(myEnemyTower);
					towerSpotted = true;
					if(rc.canMove(dir)){
						rc.move(dir);
					}
					if (rc.canAttack(myEnemyTower)){
						rc.attack(myEnemyTower);
					}
					if (rc.canAttack(myEnemyTower)){
						rc.attack(myEnemyTower);
					}
					if (rc.canAttack(myEnemyTower)){
						rc.attack(myEnemyTower);
					}
					return;
				}
			}
		}
    	
		if (anotherfuncsplayer.Util.distance(my_target, rc.getLocation()) <= 5) {
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
    	anotherfuncsplayer.Pathfinding.move(my_target, true);
    }
    
    public static void getTarget() throws GameActionException {
    	int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;
    	
    	anotherfuncsplayer.Pathfinding.init(rc);
        anotherfuncsplayer.Pathfinding.initTurn();

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