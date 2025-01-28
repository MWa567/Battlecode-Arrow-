package anotherfuncsplayer;

import battlecode.common.*;

import java.util.Random;
public class Pathfinding {
    static RobotController rc;
    public static MapLocation target;
    static boolean[] impassable = null;
    static final Random rng = new Random(6147);
    static boolean changedTarget = false;
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
        anotherfuncsplayer.Util.init(rc);
        
        BugNav.rotateRight = Util.rng.nextDouble() > 0.5;
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
    	MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
    	MapLocation closest_srp = new MapLocation(rc.getLocation().x - (rc.getLocation().x % 4) + 2, rc.getLocation().y - (rc.getLocation().y % 4) + 2);
		if (rc.canCompleteResourcePattern(closest_srp)) {
			rc.completeResourcePattern(closest_srp);
		}
    	
    	if (isSplasher) {
    		boolean existsEmpty = false;
            for (MapInfo tile : nearbyTiles) {
            	if (tile.getPaint() == PaintType.ALLY_SECONDARY) {
            		existsEmpty = false;
            	}
            	else if (tile.getPaint() == PaintType.EMPTY || tile.getPaint().isEnemy()) {
            		existsEmpty = true;
            	}
            }
            if (!existsEmpty) {
        		return ;
            }
    	}
    	else if (rc.getType() == UnitType.MOPPER) {
    		MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
            if (currentTile.getPaint().isEnemy() && rc.canAttack(rc.getLocation())){
            	rc.attack(rc.getLocation());
        	}
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
            for (RobotInfo robo : nearbyRobots){
            	if (robo.team != rc.getTeam() && (robo.type == UnitType.SOLDIER || robo.type == UnitType.SPLASHER)) {
	                MapLocation my_target = robo.location;
	                Direction dir = rc.getLocation().directionTo(my_target);
	                if (rc.canMopSwing(dir) && robo.team!=rc.getTeam()){
	                    rc.mopSwing(dir);
	                    return ;
	                }
            	}
            	if (robo.team == rc.getTeam() && robo.type == UnitType.SOLDIER) {
            		if (robo.paintAmount <= 75 && rc.getPaint() >= 50 && rc.canTransferPaint(robo.location, 25)) {
            			rc.transferPaint(robo.location, 25);
            			return ;
            		}
            	}
            }
            return ;
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
    	MapLocation rotation = new MapLocation(target.y, target.x);
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