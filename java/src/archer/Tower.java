<<<<<<< HEAD:java/src/anotherfuncsplayer/Tower.java
package anotherfuncsplayer;

import battlecode.common.*;
import java.util.Random;

/**
 * Run a single turn for a Tower.
 * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
 */

public class Tower {
	
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
    
    public static void runTower(RobotController rc, int turnCount) throws GameActionException{
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        System.out.println(turnCount);

        if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }
        else if (rc.getRoundNum() > 800 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
        }
=======
package archer;

import battlecode.common.*;

public class Tower extends Robot {

    Tower(RobotController rc) throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException {
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        if (rc.getRoundNum() <= 300 && rc.getNumberTowers() < 20 && rc.getChips() > 300 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }

        else if (rc.getRoundNum() > 300 && rc.getChips() > 300 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
            System.out.println("BUILT A SPLASHER");
        }
        //sarah is really cool
>>>>>>> nehaexp2:java/src/archer/Tower.java

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

<<<<<<< HEAD:java/src/anotherfuncsplayer/Tower.java
        // TODO: can we attack other bots?
    }
}
=======
        // Attack Nearby Bots
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        for (MapInfo tile : nearbyTiles) {
        	if ((rc.senseRobotAtLocation(tile.getMapLocation()) != null) && (rc.senseRobotAtLocation(tile.getMapLocation()).getTeam() != rc.getTeam())) {
        		MapLocation enemy = tile.getMapLocation();
        		if (rc.canAttack(enemy)) {
        			rc.attack(enemy);
        		}
        	}
        }
    }

}
>>>>>>> nehaexp2:java/src/archer/Tower.java
