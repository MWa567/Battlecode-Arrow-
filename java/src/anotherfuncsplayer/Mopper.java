package anotherfuncsplayer;

import battlecode.common.*;

public class Mopper extends Robot {

    Mopper(RobotController rc) throws GameActionException {
        super(rc);
        MapRecorder.initMap(mapWidth, mapHeight);
    }

    void play() throws GameActionException {
        MapLocation myLocation = rc.getLocation();

        // Initialize symmetry analysis
        if (!Comm.isSymmetryConfirmed) {
            eliminateSymmetry(myLocation);
        }

        // Get target based on symmetry and enemy towers
        MapLocation target = findEnemyTower();
        if (target != null) {
            // Move toward the target using Pathfinding
            Pathfinding.init(rc);
            Pathfinding.initTurn();
            Pathfinding.move(target, true);
        } else {
            // No target found, fallback to random movement
            randomMove();
        }
    }

    void eliminateSymmetry(MapLocation myLocation) throws GameActionException {
        // Check visible tiles for symmetry elimination
        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo info : infos) {
            if (!Comm.isSymmetryConfirmed) {
                MapLocation loc = info.getMapLocation();
                char val = MapRecorder.vals[loc.x * rc.getMapHeight() + loc.y];
                if ((val & MapRecorder.SEEN_BIT) != 0) {
                    for (int sym = 0; sym < 3; sym++) {
                        if (!Comm.isSymEliminated[sym] && !validateSymmetry(myLocation, loc, sym)) {
                            Comm.eliminateSym(sym);
                        }
                    }
                }
            }
        }
    }

    boolean validateSymmetry(MapLocation origin, MapLocation test, int sym) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();

        // Calculate symmetric location
        MapLocation symLoc = switch (sym) {
            case Comm.SYM_ROTATIONAL -> new MapLocation(mapWidth - 1 - test.x, mapHeight - 1 - test.y);
            case Comm.SYM_VERTICAL -> new MapLocation(mapWidth - 1 - test.x, test.y);
            case Comm.SYM_HORIZONTAL -> new MapLocation(test.x, mapHeight - 1 - test.y);
            default -> null;
        };

        if (symLoc == null) return false;

        // Validate symmetry
        char originalVal = MapRecorder.vals[origin.x * mapHeight + origin.y];
        char symVal = MapRecorder.vals[symLoc.x * mapHeight + symLoc.y];
        return (originalVal & MapRecorder.CURRENT_MASK) == (symVal & MapRecorder.CURRENT_MASK);
    }

    MapLocation findEnemyTower() throws GameActionException {
        Team enemyTeam = rc.getTeam().opponent();
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, enemyTeam);
        MapLocation closestTarget = null;
        int closestDist = Integer.MAX_VALUE;

        // Prioritize enemy towers
        for (RobotInfo robot : enemyRobots) {
            if (robot.type.isTowerType()) {
                int dist = rc.getLocation().distanceSquaredTo(robot.location);
                if (dist < closestDist) {
                    closestTarget = robot.location;
                    closestDist = dist;
                }
            }
        }

        // Use symmetry scouting if no direct enemy structure found
        if (closestTarget == null && !Comm.isSymmetryConfirmed) {
            for (int sym = 0; sym < 3; sym++) {
                if (!Comm.isSymEliminated[sym]) {
                    MapLocation potentialTarget = calculateSymmetricLocation(rc.getLocation(), sym);
                    if (rc.onTheMap(potentialTarget)) {
                        closestTarget = potentialTarget;
                        break;
                    }
                }
            }
        }
        return closestTarget;
    }

    MapLocation calculateSymmetricLocation(MapLocation loc, int sym) {
        int mapWidth = rc.getMapWidth();
        int mapHeight = rc.getMapHeight();
        return switch (sym) {
            case Comm.SYM_ROTATIONAL -> new MapLocation(mapWidth - 1 - loc.x, mapHeight - 1 - loc.y);
            case Comm.SYM_VERTICAL -> new MapLocation(mapWidth - 1 - loc.x, loc.y);
            case Comm.SYM_HORIZONTAL -> new MapLocation(loc.x, mapHeight - 1 - loc.y);
            default -> loc;
        };
    }

    void randomMove() throws GameActionException {
        // Fallback movement if no specific target is found
        Direction randomDir = Direction.values()[(int) (Math.random() * Direction.values().length)];
        if (rc.canMove(randomDir)) {
            rc.move(randomDir);
        }
    }
}