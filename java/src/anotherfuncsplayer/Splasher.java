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
	static MapLocation prevLoc = new MapLocation(-1, -1);
	
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
    		boolean existsEmpty = false;
            for (MapInfo tile : nearbyTiles) {
            	if (tile.getPaint() == PaintType.ALLY_SECONDARY) {
            		existsEmpty = false;
            	}
            	else if (tile.getPaint() == PaintType.EMPTY || tile.getPaint().isEnemy()) {
            		existsEmpty = true;
            	}
            }
            MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
            if (existsEmpty && !currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
            	rc.attack(rc.getLocation());
        	}
    		
        	for (MapInfo tile : nearbyTiles){
        		if (rc.canCompleteResourcePattern(tile.getMapLocation())) {
        			rc.completeResourcePattern(tile.getMapLocation());
        		}
    		}
        	MapLocation ourLoc = rc.getLocation();
        	
            //If we can see enemy paint, move towards it
            for (MapInfo tile : nearbyTiles){
            	if (tile.getPaint().isEnemy()){
	            	MapLocation targetLoc = tile.getMapLocation();
	            	Direction dir = ourLoc.directionTo(targetLoc);
	            	if (rc.canMove(dir)) {
	            		rc.move(dir);
	            	}
	            	else {
	            		anotherfuncsplayer.Pathfinding.move(my_target, false);
	            	}
	            	rc.setIndicatorString("TYRING TO ATTACK " + targetLoc + rc.canAttack(targetLoc));
	            	if (rc.canAttack(targetLoc)){
	            		rc.setIndicatorString("TRYING TO ATTACK " + targetLoc + rc.canAttack(targetLoc));
	            		rc.attack(targetLoc);
	            		return ;
	            	}
	            	else {
	            		if (rc.canAttack(rc.getLocation())) {
	            			rc.setIndicatorString("TRYING TO ATTACK " + rc.getLocation());
	            			rc.attack(rc.getLocation());
		            		return ;
	            		}
	            		Clock.yield();
	            	}
            	}
            }
        	if (anotherfuncsplayer.Util.distance(rc.getLocation(), my_target) <= 4 || !rc.isMovementReady()) {
    			Explore.init(rc);
    			Explore.getNewTarget();
        		my_target = Explore.exploreTarget;
    		}
        	rc.setIndicatorString("MOVING TO TARGET AT " + my_target);
        	anotherfuncsplayer.Pathfinding.move(my_target, false);
        	Clock.yield();
        }
    }
    
    public static void getTarget() throws GameActionException {
    	int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;
    	anotherfuncsplayer.Pathfinding.init(rc);
        anotherfuncsplayer.Pathfinding.initTurn();
        if (coord_x <= mapWidth / 2 && coord_y <= mapHeight / 2) {
        	Random random = new Random();
        	int randomX = random.nextInt(mapWidth / 2 ) + mapWidth / 2;
        	int randomY = random.nextInt(mapHeight / 2 ) + mapHeight / 2;
        	my_target = new MapLocation(randomX, randomY);
        	updateEnemyRobots(rc);
        }
        else if (coord_x > mapWidth / 2 && coord_y <= mapHeight / 2) {
        	Random random = new Random();
        	int randomX = random.nextInt(mapWidth / 2 );
        	int randomY = random.nextInt(mapHeight / 2 ) + mapHeight / 2;
        	my_target = new MapLocation(randomX, randomY);
        	updateEnemyRobots(rc);
        }
        else if (coord_x <= mapWidth / 2 && coord_y > mapHeight / 2) {
        	Random random = new Random();
        	int randomX = random.nextInt(mapWidth / 2 ) + mapWidth / 2;
        	int randomY = random.nextInt(mapHeight / 2 );
        	my_target = new MapLocation(randomX, randomY);
        	updateEnemyRobots(rc);
        }
        else {
        	Random random = new Random();
        	int randomX = random.nextInt(mapWidth / 2 );
        	int randomY = random.nextInt(mapHeight / 2 );
        	my_target = new MapLocation(randomX, randomY);
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