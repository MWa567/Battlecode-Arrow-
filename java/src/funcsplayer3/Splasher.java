package funcsplayer3;

import java.util.Random;
import battlecode.common.*;

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
    	funcsplayer3.Util.init(rc);
    	funcsplayer3.Explore.init(rc);
    	funcsplayer3.BugNav.init(rc);
        funcsplayer3.Pathfinding.init(rc);
        funcsplayer3.Pathfinding.initTurn();
        getTarget();
        while (true) {
        	MapLocation closest_srp = new MapLocation(rc.getLocation().x - (rc.getLocation().x % 4) + 2, rc.getLocation().y - (rc.getLocation().y % 4) + 2);
    		if (rc.canCompleteResourcePattern(closest_srp)) {
    			rc.completeResourcePattern(closest_srp);
    		}
    		
        	if(towerSpotted){
        		prevTarget = myEnemyTower;
    			if (rc.canAttack(myEnemyTower)){
    				rc.attack(myEnemyTower);
    			}
    			else if(rc.isActionReady()){
    				funcsplayer3.Pathfinding.move(myEnemyTower, true);
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
    			Direction dir = rc.getLocation().directionTo(myEnemyTower).opposite();
    			if (rc.canMove(dir)){
    				rc.move(dir);
    				rc.setIndicatorString("GOING BACK");
    			}
    			return;
    		}
    		
        	
        	MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        	// boolean moveAway = false;
        	for (MapInfo tile : nearbyTiles){
        		if (rc.canCompleteResourcePattern(tile.getMapLocation())) {
        			rc.completeResourcePattern(tile.getMapLocation());
        		}
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
    					Direction new_dir = rc.getLocation().directionTo(myEnemyTower).opposite();
    					if (rc.canMove(new_dir)){
    						rc.move(new_dir);
    						rc.setIndicatorString("GOING BACK");
    					}
    					return;
    				}
    			}
    		}
        	if (funcsplayer3.Util.distance(rc.getLocation(), my_target) <= 5 || !rc.isMovementReady()) {
    			Explore.init(rc);
    			Explore.getNewTarget();
        		my_target = Explore.exploreTarget;
    		}
        	rc.setIndicatorString("MOVING TO TARGET AT " + my_target);
        	funcsplayer3.Pathfinding.move(my_target, true);
        	Clock.yield();
        }
    }
    
    public static void getTarget() throws GameActionException {
    	int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;
    	
    	funcsplayer3.Pathfinding.init(rc);
        funcsplayer3.Pathfinding.initTurn();

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