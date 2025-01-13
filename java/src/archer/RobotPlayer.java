package archer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Robot r;
        switch (rc.getType()) {
            case SOLDIER:  r = new Soldier(rc);  break;
            case MOPPER: r = new Mopper(rc);  break;
            case SPLASHER: r = new Splasher(rc);  break;
            default: r = new Tower(rc);   break;
        }

        while(true){
            r.initTurn();
            r.play();
            r.endTurn();
            Clock.yield();
        }
    }
}
