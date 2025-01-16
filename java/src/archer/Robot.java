package archer;

import battlecode.common.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public abstract class Robot {

    RobotController rc;

    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc;
        RobotPlayer.rc = rc;
        myTeam = rc.getTeam();
        oppTeam = rc.getTeam().opponent();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        turnCount = 0;
    }

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


    abstract void play() throws GameActionException;

    void initTurn() throws GameActionException {
        startRound = rc.getRoundNum();
        indicator = "";
        Comm.turn_starts();
    }

    void endTurn() throws GameActionException {
        rc.setIndicatorString(indicator);
        turnCount += 1;
        if (startRound != rc.getRoundNum()) {
            System.out.printf("overran turn from %d to %d\n", startRound, rc.getRoundNum());
        }

        Clock.yield();
    }
}
