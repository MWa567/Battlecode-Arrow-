import random
from battlecode25.stubs import *

class Pathfinding:
    rc = None
    target = None
    impassable = None
    rng = random.Random(6147)
    changed_target = False
    has_resource = False
    directions = [
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
        Direction.CENTER
    ]

    @staticmethod
    def init(r: RobotController):
        Pathfinding.rc = r
        Util.init(r)
        BugNav.rotate_right = Util.rng.random() > 0.5
        if r.getMapWidth() >= 30 or r.getMapHeight() >= 30:
            Pathfinding.has_resource = True

    @staticmethod
    def set_impassable(imp):
        Pathfinding.impassable = imp

    @staticmethod
    def init_turn():
        Pathfinding.impassable = [False] * len(Pathfinding.directions)

    @staticmethod
    def can_move(dir: Direction) -> bool:
        if not Pathfinding.rc.canMove(dir):
            return False
        if Pathfinding.impassable[dir.ordinal()]:
            return False
        return True

    @staticmethod
    def paint(is_splasher: bool):
        if is_splasher:
            exists_empty = False
            nearby_tiles = Pathfinding.rc.senseNearbyMapInfos()
            for tile in nearby_tiles:
                if tile.hasRuin() and Pathfinding.rc.senseRobotAtLocation(tile.getMapLocation()) is not None and Pathfinding.rc.senseRobotAtLocation(tile.getMapLocation()).team != Pathfinding.rc.getTeam():
                    Pathfinding.set_target(tile.getMapLocation())
                    if Pathfinding.rc.canAttack(Pathfinding.target):
                        Pathfinding.rc.attack(tile.getMapLocation())
                elif Pathfinding.has_resource and tile.getMark().isAlly():
                    exists_empty = False
                elif tile.getPaint() == PaintType.EMPTY or tile.getPaint().isEnemy():
                    exists_empty = True
            if not exists_empty:
                return

        current_tile = Pathfinding.rc.senseMapInfo(Pathfinding.rc.getLocation())
        if not current_tile.getPaint().isAlly() and Pathfinding.rc.canAttack(Pathfinding.rc.getLocation()):
            Pathfinding.rc.attack(Pathfinding.rc.getLocation())

    @staticmethod
    def set_target(new_target: MapLocation):
        Pathfinding.target = new_target

    @staticmethod
    def splasher_new_target(target: MapLocation):
        rotation = MapLocation(target.y, target.x)
        rotation = Util.clip_to_within_map(rotation)
        Pathfinding.set_target(rotation)

    @staticmethod
    def move(loc: MapLocation, is_splasher: bool):
        Pathfinding.target = loc
        BugNav.init(Pathfinding.rc)
        BugNav.init_turn()
        if not BugNav.nav(Pathfinding.target):
            Pathfinding.greedy_path()
            Pathfinding.paint(is_splasher)
        else:
            BugNav.move(Pathfinding.target)
            Pathfinding.paint(is_splasher)

    eps = 1e-5

    @staticmethod
    def greedy_path():
        try:
            my_loc = Pathfinding.rc.getLocation()
            best_dir = None
            best_estimation = 0
            best_estimation_dist = 0
            for dir in Pathfinding.directions:
                new_loc = my_loc.add(dir)
                if not Pathfinding.rc.onTheMap(new_loc):
                    continue
                if not Pathfinding.can_move(dir):
                    continue
                if not Pathfinding.strictly_closer(new_loc, my_loc, Pathfinding.target):
                    continue
                new_dist = new_loc.distanceSquaredTo(Pathfinding.target)
                estimation = 1 + Util.distance(Pathfinding.target, new_loc)
                if best_dir is None or estimation < best_estimation - Pathfinding.eps or (abs(estimation - best_estimation) <= 2 * Pathfinding.eps and new_dist < best_estimation_dist):
                    best_estimation = estimation
                    best_dir = dir
                    best_estimation_dist = new_dist
            if best_dir is not None:
                Pathfinding.rc.move(best_dir)
        except Exception as e:
            print(e)

    @staticmethod
    def strictly_closer(new_loc: MapLocation, old_loc: MapLocation, target: MapLocation) -> bool:
        d_old = Util.distance(target, old_loc)
        d_new = Util.distance(target, new_loc)
        if d_old < d_new:
            return False
        if d_new < d_old:
            return True
        return target.distanceSquaredTo(new_loc) < target.distanceSquaredTo(old_loc)
