package archer;

import battlecode.common.*;
import java.util.Random;


public class Splasher extends Robot {

    Splasher(RobotController rc) throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException {
        int coord_x = originalLocation.x;
    	int coord_y = originalLocation.y;

    	int map_height = rc.getMapHeight();
        int map_width = rc.getMapWidth();

    	if (coord_x < coord_y) {
	    	if (coord_x < map_width / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(map_height);
	            MapLocation my_target = new MapLocation(map_width, randomInd);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	    	else {
	    		Random rand = new Random();
	            int randomInd = rand.nextInt(map_height);
	            MapLocation my_target = new MapLocation(0, randomInd);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
			/*
			else if (rc.canMopSwing(dir)){
	            rc.mopSwing(dir);
	            System.out.println("Mop Swing! Booyah!");
	        }
	        */

			// We can also move our code into different methods or classes to better organize it!
	        updateEnemyRobots(rc);
    	}
    	else {
    		if (coord_y < map_height / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(map_width);
	            MapLocation my_target = new MapLocation(randomInd, map_height);

	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target, true);
	    	}
	    	else {
	    		Random rand = new Random();
	    		int randomInd = rand.nextInt(map_width);
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
