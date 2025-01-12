package anotherfuncsplayer;

import battlecode.common.*;
import java.util.Random;

/**
 * Run a single turn for a Mopper.
 * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
 */
public class Mopper {
	
	static final Random rng = new Random(6147);
    static MapLocation target;
    static MapLocation my_target;
    static MapLocation originalLocation;

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
    
    public static void runMopper(RobotController rc) throws GameActionException{
    	
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
	            Pathfinding.move(my_target);
	    	}
	    	else {
	    		Random rand = new Random();
	            int randomInd = rand.nextInt(map_height);
	            MapLocation my_target = new MapLocation(0, randomInd);
	            
	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target);
	    	}
	    	
	    	// Attack randomly.
	    	Direction dir = directions[rng.nextInt(directions.length)];
	        MapLocation nextLoc = rc.getLocation().add(dir);
			if (rc.canAttack(nextLoc)){
	            rc.attack(nextLoc);
	        }
			else if (rc.canMopSwing(dir)){
	            rc.mopSwing(dir);
	            System.out.println("Mop Swing! Booyah!");
	        }
			
			// We can also move our code into different methods or classes to better organize it!
	        RobotPlayer.updateEnemyRobots(rc);
    	}
    	else {
    		if (coord_y < map_height / 2) {
	            Random rand = new Random();
	            int randomInd = rand.nextInt(map_width);
	            MapLocation my_target = new MapLocation(randomInd, map_height);
	            
	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target);
	    	}
	    	else {
	    		Random rand = new Random();
	    		int randomInd = rand.nextInt(map_width);
	            MapLocation my_target = new MapLocation(randomInd, 0);
	            
	            Pathfinding.init(rc);
	            Pathfinding.initTurn();
	            Pathfinding.move(my_target);
	    	}
	    	
	    	// Attack randomly.
	    	Direction dir = directions[rng.nextInt(directions.length)];
	        MapLocation nextLoc = rc.getLocation().add(dir);
			if (rc.canAttack(nextLoc)){
	            rc.attack(nextLoc);
	        }
			else if (rc.canMopSwing(dir)){
	            rc.mopSwing(dir);
	            System.out.println("Mop Swing! Booyah!");
	        }
			
			// We can also move our code into different methods or classes to better organize it!
	        RobotPlayer.updateEnemyRobots(rc);
    	}
    }
}