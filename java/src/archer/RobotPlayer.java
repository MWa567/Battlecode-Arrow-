package archer;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {
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
            int round = rc.getRoundNum();
            r.initTurn();
            // Robot.bytecodeDebug += " BCINIT = " + Clock.getBytecodeNum();
            r.play();
            // Robot.bytecodeDebug += "  BCPLAY = " + Clock.getBytecodeNum();
            r.endTurn();
            int roundEnd = rc.getRoundNum();
            if (round < roundEnd){
                System.out.println("FAIL " + round + " " + rc.getLocation().toString() + Robot.bytecodeDebug);
            }
            // Robot.bytecodeDebug = new String();
            // Constants.indicatorString = new String();
            Clock.yield();
        }
    }
}
