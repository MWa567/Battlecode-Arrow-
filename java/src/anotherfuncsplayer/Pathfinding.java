package anotherfuncsplayer;

import battlecode.common.*;

import java.util.Random;
public class Pathfinding {
    static RobotController rc;
    public static MapLocation target;
    static boolean[] impassable = null;
    static final Random rng = new Random(6147);
    static boolean changedTarget = false;
    static boolean hasResource = false;
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };
    public static void init(RobotController r) {
        rc = r;
        Util.init(rc);
        BugNav.rotateRight = Util.rng.nextDouble() > 0.5;
        if (rc.getMapWidth() >= 30 || rc.getMapHeight() >= 30) {
        	hasResource = true;
        }
    }
    
    static void setImpassable(boolean[] imp) {
        impassable = imp;
    }
    public static void initTurn() {
        impassable = new boolean[directions.length];
    }
    static boolean canMove(Direction dir) {
        if (!rc.canMove(dir))
            return false;
        if (impassable[dir.ordinal()])
            return false;
        return true;
    }
    
    static public void paint(boolean isSplasher) throws GameActionException {
    	if (isSplasher) {
    		boolean existsEmpty = false;
    		MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
            for (MapInfo tile : nearbyTiles) {
            	if (tile.hasRuin() && rc.senseRobotAtLocation(tile.getMapLocation()) != null && rc.senseRobotAtLocation(tile.getMapLocation()).getTeam() != rc.getTeam()) {
            		setTarget(tile.getMapLocation());
            		if (rc.canAttack(target)) {
            			rc.attack(tile.getMapLocation());
            		}
            	}
            	else if (hasResource && tile.getMark().isAlly()) {
            		return ;
            	}
            	else if (tile.getPaint() == PaintType.EMPTY || tile.getPaint().isEnemy()) {
            		existsEmpty = true;
            	}
            }
            if (!existsEmpty) {
        		return ;
            }
    	}
    	MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (!currentTile.getPaint().isAlly() && rc.canAttack(rc.getLocation())){
            rc.attack(rc.getLocation());
        }
    }
    
    static public void setTarget(MapLocation newTarget) {
    	target = newTarget;
    }
    
    static public void splasherNewTarget(MapLocation target) throws GameActionException {
    	MapLocation rotation = new MapLocation(Robot.mapWidth - target.x - 1, Robot.mapHeight - target.y - 1);
    	rotation = Util.clipToWithinMap(rotation);
    	setTarget(rotation);
    }
    
    static public void move(MapLocation loc, boolean isSplasher) throws GameActionException {
    	target = loc;
    	BugNav.init(rc);
    	BugNav.initTurn();
        if (!BugNav.nav(target)) {
        	greedyPath();
        	paint(isSplasher);
        }
		else {
			BugNav.move(target);
			paint(isSplasher);
        }
    }
    
    static final double eps = 1e-5;
    static void greedyPath() {
        try {
            MapLocation myLoc = rc.getLocation();
            Direction bestDir = null;
            double bestEstimation = 0;
            int bestEstimationDist = 0;
            for (Direction dir : directions) {
                MapLocation newLoc = myLoc.add(dir);
                if (!rc.onTheMap(newLoc))
                    continue;
                if (!canMove(dir))
                    continue;
                if (!strictlyCloser(newLoc, myLoc, target))
                    continue;
                int newDist = newLoc.distanceSquaredTo(target);
                // TODO: Better estimation?
                double estimation = 1 + Util.distance(target, newLoc);
                if (bestDir == null || estimation < bestEstimation - eps
                        || (Math.abs(estimation - bestEstimation) <= 2 * eps && newDist < bestEstimationDist)) {
                    bestEstimation = estimation;
                    bestDir = dir;
                    bestEstimationDist = newDist;
                }
            }
            if (bestDir != null) {
                rc.move(bestDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static boolean strictlyCloser(MapLocation newLoc, MapLocation oldLoc, MapLocation target) {
        int dOld = Util.distance(target, oldLoc), dNew = Util.distance(target, newLoc);
        if (dOld < dNew)
            return false;
        if (dNew < dOld)
            return true;
        return target.distanceSquaredTo(newLoc) < target.distanceSquaredTo(oldLoc);
    }
}