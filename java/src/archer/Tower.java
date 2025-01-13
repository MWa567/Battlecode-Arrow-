package archer;

import battlecode.common.*;

public class Tower extends Robot {

    Tower(RobotController rc) throws GameActionException {
        super(rc);
    }

    void play() throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);

        if (rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }
    }
}
