package anotherfuncsplayer;

import battlecode.common.*;

public abstract class Robot {
    static RobotController rc;
    static int turnCount;

    static int mapWidth, mapHeight;

    static Team myTeam;
    static Team oppTeam;

    public static String indicator;
    public static int startRound;

    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc;
        originalLocation = rc.getLocation();
        RobotPlayer.rc = rc;
        myTeam = rc.getTeam();
        oppTeam = rc.getTeam().opponent();
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        turnCount = 0;
    }

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
    }

    void endTurn() throws GameActionException {
    	rc.setIndicatorString("Robot turn");
        turnCount += 1;
        if (startRound != rc.getRoundNum()) {
            System.out.printf("overran turn from %d to %d\n", startRound, rc.getRoundNum());
        }

        Clock.yield();
    }
}