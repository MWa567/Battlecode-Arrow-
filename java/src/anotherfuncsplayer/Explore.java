package anotherfuncsplayer;

import battlecode.common.*;
import java.util.Random;

public class Explore {
    static RobotController rc;
    static Direction lastExploreDir;
    static final int EXPLORE_BOREDOM = 20;
    static int boredom;
    static final int MIN_DIST_FROM_WALL = 3;
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
    static int mapWidth;
	static int mapHeight;
	private static Random rand = new Random();
	
    static Direction[] dirPath;
    static final int MAX_MAP_SIZE = 60;
    static final int MAX_MAP_SIZE_SQ = MAX_MAP_SIZE * MAX_MAP_SIZE;
    static final int MAX_MAP_SIZE2 = 120;
    static boolean[][] visited = new boolean[MAX_MAP_SIZE][];
    static int visionRadius;
    static boolean initialized = false;
    static int initRow = 0;
    static final int INIT_BC_LEFT = 300;
    static final int VISITED_BC_LEFT = 100;
    static MapLocation exploreTarget = null;
    static Direction exploreDir = Direction.CENTER;
    static double angle = 0;
    static final double EXPLORE_DIST = 100;
    static MapLocation explore3Target = null;
    static Boolean rotateLeft = null;
    static final int BYTECODES_USED = 2500;
    static Direction lastDirMoved = null;
    static int initialX, initialY;
    static enum ExploreType {
        PATHFINDING,
        GREEDY,
    }
    static ExploreType exploreType;
    public static void init(RobotController r) {
        rc = r;
        mapWidth = rc.getMapWidth();
    	mapHeight = rc.getMapHeight();
        getNewTarget();
    }
    
    public static void pickNewExploreDir() {
        Direction[] newDirChoices = {
                lastExploreDir.rotateLeft(),
                lastExploreDir,
                lastExploreDir.rotateRight(),
        };
        Direction[] validDirs = new Direction[5];
        int numValidDirs = 0;
        for (Direction dir : newDirChoices) {
            validDirs[numValidDirs++] = dir;
        }
        if (numValidDirs > 0) {
            lastExploreDir = validDirs[Util.rng.nextInt(numValidDirs)];
        } else {
            // This can happen if you're going straight into a corner or wall
            // In this case, we choose from close to the opposite current explore dir
            switch (Util.rng.nextInt(3)) {
                case 0:
                    lastExploreDir = lastExploreDir.opposite().rotateLeft();
                    break;
                case 1:
                    lastExploreDir = lastExploreDir.opposite().rotateRight();
                    break;
                default:
                    lastExploreDir = lastExploreDir.opposite();
                    break;
            }
        }
    }
    
    // If you're traveling south *right* next to a wall, you should go
    // southwest/east for a turn
    public static Direction rotateAwayFromWallIfNecessary(Direction dir) {
        MapLocation currLoc = rc.getLocation();
        switch (dir) {
            case SOUTH:
                if (currLoc.x < MIN_DIST_FROM_WALL) {
                    return dir.rotateLeft();
                }
                if (Util.MAP_WIDTH - currLoc.x < MIN_DIST_FROM_WALL) {
                    return dir.rotateRight();
                }
                break;
            case NORTH:
                if (currLoc.x < MIN_DIST_FROM_WALL) {
                    return dir.rotateRight();
                }
                if (Util.MAP_WIDTH - currLoc.x < MIN_DIST_FROM_WALL) {
                    return dir.rotateLeft();
                }
                break;
            case WEST:
                if (currLoc.y < MIN_DIST_FROM_WALL) {
                    return dir.rotateRight();
                }
                if (Util.MAP_HEIGHT - currLoc.y < MIN_DIST_FROM_WALL) {
                    return dir.rotateLeft();
                }
                break;
            case EAST:
                if (currLoc.y < MIN_DIST_FROM_WALL) {
                    return dir.rotateLeft();
                }
                if (Util.MAP_HEIGHT - currLoc.y < MIN_DIST_FROM_WALL) {
                    return dir.rotateRight();
                }
                break;
        }
        return dir;
    }
    
    static void initExploreDir() {
        assignExplore3Dir(directions[Util.rng.nextInt(8)]);
    }
    
    static void initialize() {
        if (initialized)
            return;
        while (initRow < MAX_MAP_SIZE) {
            if (Clock.getBytecodesLeft() < INIT_BC_LEFT)
                return;
            visited[initRow] = new boolean[MAX_MAP_SIZE];
            // muckrakerLocations[initRow] = new int[MAX_MAP_SIZE];
            initRow++;
        }
        initialized = true;
    }
    // Finds a new target anywhere on the map outside of the vision radius
    // Only used when visited hasn't been initialized yet
    static void emergencyTarget(int tries) {
        MapLocation myLoc = rc.getLocation();
        if (exploreTarget != null && myLoc.distanceSquaredTo(exploreTarget) > visionRadius)
            return;
        int X = rc.getLocation().x;
        int Y = rc.getLocation().y;
        for (int i = tries; i-- > 0;) {
            int dx = (int) (Util.rng.nextInt(MAX_MAP_SIZE2) - MAX_MAP_SIZE);
            int dy = (int) (Util.rng.nextInt(MAX_MAP_SIZE2) - MAX_MAP_SIZE);
            exploreTarget = new MapLocation(X + dx, Y + dy);
            if (myLoc.distanceSquaredTo(exploreTarget) > visionRadius)
                return;
        }
        exploreTarget = null;
    }
    public static MapLocation getExploreTarget() {
        return exploreTarget;
    }
    
    static MapLocation getExplore3Target() {
        checkDirection();
        return explore3Target;
    }
    
    static void assignExplore3Dir(Direction dir) {
        exploreDir = dir;
        double tempAngle = Math.atan2(exploreDir.dy, exploreDir.dx);
        int tries = 10;
        double x = rc.getLocation().x, y = rc.getLocation().y;
        for (int i = tries; i-- > 0;) {
            // Try for more variance in the direction?
            angle = tempAngle + (Util.rng.nextDouble() * 45 - 22.5) / 180 * Math.PI;
            x += Math.cos(angle) * EXPLORE_DIST;
            y += Math.sin(angle) * EXPLORE_DIST;
            explore3Target = new MapLocation((int) x, (int) y);
            if (Util.onTheMap(explore3Target))
                return;
        }
    }
    static void checkDirection() {
        if (explore3Target.isWithinDistanceSquared(rc.getLocation(), visionRadius))
            return;
        // System.err.println("Checking new direction!");
        switch (exploreDir) {
            case SOUTHEAST:
            case NORTHEAST:
            case NORTHWEST:
            case SOUTHWEST:
            case NORTH:
            case SOUTH:
                int east = eastCloser();
                switch (east) {
                    case 1:
                        assignExplore3Dir(Direction.WEST);
                        return;
                    case -1:
                        assignExplore3Dir(Direction.EAST);
                        return;
                }
                // Direction dir = exploreDir.rotateLeft().rotateLeft();
                // if (!isValidExploreDir(dir)) assignExplore3Dir(dir);
                // else assignExplore3Dir(dir.opposite());
                return;
            case EAST:
            case WEST:
                int north = northCloser();
                switch (north) {
                    case 1:
                        assignExplore3Dir(Direction.SOUTH);
                        return;
                    case -1:
                        assignExplore3Dir(Direction.NORTH);
                        return;
                }
                // dir = exploreDir.rotateLeft().rotateLeft();
                // if (!isValidExploreDir(dir)) assignExplore3Dir(dir);
                // else assignExplore3Dir(dir.opposite());
                return;
        }
    }
    static int eastCloser() {
        return rc.getMapWidth() - rc.getLocation().x <= rc.getLocation().x ? 1 : -1;
    }
    static int northCloser() {
        return rc.getMapHeight() - rc.getLocation().y <= rc.getLocation().y ? 1 : -1;
    }

    static boolean hasVisited(MapLocation loc) {
        if (!initialized)
            return false;
        return visited[loc.x][loc.y];
    }
    public static void getNewTarget() {
    	int robotX = rc.getLocation().x;
    	int robotY = rc.getLocation().y;
    	
    	// Random angle generation
        double angle = rand.nextDouble() * 360; // Random angle in degrees

        // Convert angle to radians
        double angleRad = Math.toRadians(angle);

        // Direction vector (dx, dy) as floating-point values
        double dx = Math.cos(angleRad);
        double dy = Math.sin(angleRad);

        // Calculate intersections with map boundaries
        double tMin = Double.MAX_VALUE;
        int edgeX = robotX;
        int edgeY = robotY;

        // Check intersection with vertical edges (x = 0 and x = mapWidth - 1)
        if (dx != 0) {
            // Intersection with left edge (x = 0)
            double tLeft = (0 - robotX) / dx;
            if (tLeft > 0) {
                double yLeft = robotY + tLeft * dy;
                if (yLeft >= 0 && yLeft < mapHeight && tLeft < tMin) {
                    tMin = tLeft;
                    edgeX = 0;
                    edgeY = (int) Math.round(yLeft);
                }
            }

            // Intersection with right edge (x = mapWidth - 1)
            double tRight = (mapWidth - 1 - robotX) / dx;
            if (tRight > 0) {
                double yRight = robotY + tRight * dy;
                if (yRight >= 0 && yRight < mapHeight && tRight < tMin) {
                    tMin = tRight;
                    edgeX = mapWidth - 1;
                    edgeY = (int) Math.round(yRight);
                }
            }
        }

        // Check intersection with horizontal edges (y = 0 and y = mapHeight - 1)
        if (dy != 0) {
            // Intersection with top edge (y = 0)
            double tTop = (0 - robotY) / dy;
            if (tTop > 0) {
                double xTop = robotX + tTop * dx;
                if (xTop >= 0 && xTop < mapWidth && tTop < tMin) {
                    tMin = tTop;
                    edgeX = (int) Math.round(xTop);
                    edgeY = 0;
                }
            }

            // Intersection with bottom edge (y = mapHeight - 1)
            double tBottom = (mapHeight - 1 - robotY) / dy;
            if (tBottom > 0) {
                double xBottom = robotX + tBottom * dx;
                if (xBottom >= 0 && xBottom < mapWidth && tBottom < tMin) {
                    tMin = tBottom;
                    edgeX = (int) Math.round(xBottom);
                    edgeY = mapHeight - 1;
                }
            }
        }
        exploreTarget = new MapLocation(edgeX, edgeY);
    }
    static void markSeen() {
        if (!initialized)
            return;
        try {
            MapLocation loc = rc.getLocation();
            for (int i = dirPath.length; i-- > 0;) {
                if (Clock.getBytecodesLeft() < VISITED_BC_LEFT)
                    return;
                loc = loc.add(dirPath[i]);
                // TODO: We can't sense clouds right now :(
                if (rc.onTheMap(loc))
                        visited[loc.x][loc.y] = true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    static void fillDirPath() {
        switch (visionRadius) {
            case 20:
                dirPath = new Direction[] { Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH,
                        Direction.NORTH, Direction.NORTH, Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST,
                        Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST,
                        Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST,
                        Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST,
                        Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH,
                        Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST,
                        Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH,
                        Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST,
                        Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST,
                        Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH,
                        Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.WEST,
                        Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST,
                        Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.CENTER };
                break;
            default:
                dirPath = new Direction[] { Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHWEST,
                        Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST,
                        Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST,
                        Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTH,
                        Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHWEST,
                        Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST,
                        Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH,
                        Direction.NORTH, Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST, Direction.EAST,
                        Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTH,
                        Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHWEST,
                        Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST,
                        Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST,
                        Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST,
                        Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST,
                        Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH,
                        Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST,
                        Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH,
                        Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTH,
                        Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST, Direction.SOUTH,
                        Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.CENTER };
                break;
        }
    }
    /*
    public static void pickNewExploreDir90() {
        Direction[] newDirChoices = {
                lastExploreDir.rotateLeft().rotateLeft(),
                lastExploreDir.rotateRight().rotateRight(),
        };
        Direction[] validDirs = new Direction[5];
        int numValidDirs = 0;
        for (Direction dir : newDirChoices) {
            validDirs[numValidDirs++] = dir;
        }
        if (numValidDirs > 0) {
            lastExploreDir = validDirs[Util.rng.nextInt(numValidDirs)];
        } else {
            // This can happen if you're going straight into a corner or wall
            // In this case, we choose from close to the opposite current explore dir
            switch (Util.rng.nextInt(3)) {
                case 0:
                    lastExploreDir = lastExploreDir.opposite().rotateLeft();
                    break;
                case 1:
                    lastExploreDir = lastExploreDir.opposite().rotateRight();
                    break;
                default:
                    lastExploreDir = lastExploreDir.opposite();
                    break;
            }
        }
    }
    */
}