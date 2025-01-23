package funcsplayer2;

import battlecode.common.*;

import java.util.Random;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */

public class RobotPlayer {
    static RobotController rc;
    static int turnCount;
    static int islandCount;

    static int mapWidth, mapHeight;

    static Team myTeam;
    static Team oppTeam;

    public static String indicator;
    public static int startRound;

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
        }
    }
}
    
