package anotherfuncsplayer;

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
    	anotherfuncsplayer.Util.init(rc);
    	anotherfuncsplayer.Explore.init(rc);
    	anotherfuncsplayer.BugNav.init(rc);
        anotherfuncsplayer.Pathfinding.init(rc);
        anotherfuncsplayer.Pathfinding.initTurn();
        getTarget();
        while (true) {
    		if (rc.canCompleteResourcePattern(rc.getLocation())) {
    			rc.completeResourcePattern(rc.getLocation());
    		}
    		
        	MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        	for (MapInfo tile : nearbyTiles){
        		if (rc.canCompleteResourcePattern(tile.getMapLocation())) {
        			rc.completeResourcePattern(tile.getMapLocation());
        		}
    		}
        	if (anotherfuncsplayer.Util.distance(rc.getLocation(), my_target) <= 4 || !rc.isMovementReady()) {
    			Explore.init(rc);
    			Explore.getNewTarget();
        		my_target = Explore.exploreTarget;
    		}
        	rc.setIndicatorString("MOVING TO TARGET AT " + my_target);
        	anotherfuncsplayer.Pathfinding.move(my_target, true);
        	Clock.yield();
        }
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