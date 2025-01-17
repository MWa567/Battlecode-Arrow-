package anotherfuncsplayer;

import java.util.Random;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Splasher extends Robot {

	static RobotController rc;

    Splasher(RobotController rc) throws GameActionException {
        super(rc);
        Splasher.rc = rc;

    }

    void play() throws GameActionException {
        int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;

		// RobotInfo [] nearbyRobots = rc.senseNearbyRobots()
		// 		    for (MapInfo tile : nearbyTiles){
		// 		RobotInfo potentialTower = rc.senseRobotAtLocation(tile.getMapLocation());

	    // 		if (tile.hasRuin() && potentialTower == null){
	    //         	int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
	    //         	if (dist < ruinDist) {
		//                 curRuin = tile;
		//                 ruinDist = dist;
	    //         	}
	    //         }
	    // 		else if (tile.hasRuin() && potentialTower != null) {
		// 			if (potentialTower.getTeam == rc.getTeam()){
		// 				int dist = tile.getMapLocation().distanceSquaredTo(rc.getLocation());
		// 				if (dist < towerDist) {
		// 					nearestTower = tile.getMapLocation();
		// 					towerDist = dist;
		// 				}
		// 				if (rc.getPaint() < 100) {
		// 					if (rc.canTransferPaint(tile.getMapLocation(), -100)) {
		// 						rc.transferPaint(tile.getMapLocation(), -100);
		// 					}
		// 				}
		// 				if (rc.canUpgradeTower(tile.getMapLocation())) {
		// 					rc.upgradeTower(tile.getMapLocation());
		// 					System.out.println("Tower was upgraded!");
		// 				}
		// 			}
		// 			else{
		// 				if(rc.canAttack(potentialTower.location)){
		// 					rc.attack(potentialTower.location);
		// 					Direction dir = opposite(rc.getLocation().directionTo(potentialTower));
		// 					if (rc.canMove(dir)){
		// 						rc.move(dir);
		// 					}
		// 				}
		// 			}
	    // 		}
	    // 	}



    	if (coord_x < coord_y) {
	    	if (coord_x < mapWidth / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(mapHeight);
	            MapLocation my_target = new MapLocation(mapWidth, randomInd);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	    	else {
	    		Random rand = new Random();
	            int randomInd = rand.nextInt(mapHeight);
	            MapLocation my_target = new MapLocation(0, randomInd);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	        updateEnemyRobots(rc);
    	}
    	else {
    		if (coord_y < mapHeight / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(mapWidth);
	            MapLocation my_target = new MapLocation(randomInd, mapHeight);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	    	else {
	    		Random rand = new Random();
	    		int randomInd = rand.nextInt(mapWidth);
	            MapLocation my_target = new MapLocation(randomInd, 0);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
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
				if(rc.canAttack(enemyLocations[i])){
					rc.attack(enemyLocations[i]);
				}
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
