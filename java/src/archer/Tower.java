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

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

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
