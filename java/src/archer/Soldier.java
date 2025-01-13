package archer;

import battlecode.common.*;

public class Soldier extends Robot {

    Soldier(RobotController rc) throws GameActionException {
        super(rc);
    }

    void initTurn() throws GameActionException {

    }


    void play() throws GameActionException {
        MapLocation targetLoc = curRuin.getMapLocation();
Direction dir = rc.getLocation().directionTo(targetLoc);
if (rc.canMove(dir))
    rc.move(dir);

    }

}
