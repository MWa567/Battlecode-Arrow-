import random
from battlecode25.stubs import *

class Util:

    rng = None

    MAP_WIDTH = None
    MAP_HEIGHT = None
    MAP_AREA = None
    MAP_MAX_DIST_SQUARED = None

    # Directions for movement
    directions = [
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    ]

    @staticmethod
    def init(rc):
        Util.rc = rc
        Util.rng = random.Random(rc.get_round_num() * 23981 + rc.get_id() * 10289)
        Util.MAP_HEIGHT = rc.get_map_height()
        Util.MAP_WIDTH = rc.get_map_width()
        Util.MAP_AREA = Util.MAP_HEIGHT * Util.MAP_WIDTH
        Util.MAP_MAX_DIST_SQUARED = Util.MAP_HEIGHT ** 2 + Util.MAP_WIDTH ** 2

    @staticmethod
    def distance(A, B):
        if A is None or B is None:
            return 99999
        return abs(A.x - B.x) + abs(A.y - B.y)

    @staticmethod
    def invertLocation(loc):
        dx = loc.x - Util.rc.get_location().x
        dy = loc.y - Util.rc.get_location().y
        return Util.rc.get_location().translate(-dx, -dy)

    @staticmethod
    def guessEnemyLoc(ourLoc):
        height = Util.rc.get_map_height()
        width = Util.rc.get_map_width()
        verticalFlip = battlecode.MapLocation(ourLoc.x, height - ourLoc.y - 1)
        horizontalFlip = battlecode.MapLocation(width - ourLoc.x - 1, ourLoc.y)
        rotation = battlecode.MapLocation(width - ourLoc.x - 1, height - ourLoc.y - 1)
        return [verticalFlip, horizontalFlip, rotation]

    @staticmethod
    def clip(n, lo, hi):
        return min(max(n, lo), hi)

    @staticmethod
    def clip_double(n, lo, hi):
        return min(max(n, lo), hi)

    @staticmethod
    def reflectX(loc):
        return battlecode.MapLocation(-loc.x, loc.y)

    @staticmethod
    def reflectY(loc):
        return battlecode.MapLocation(loc.x, -loc.y)

    @staticmethod
    def clipToWithinMap(loc):
        return battlecode.MapLocation(
            Util.clip(loc.x, 0, Util.MAP_WIDTH),
            Util.clip(loc.y, 0, Util.MAP_HEIGHT)
        )

    @staticmethod
    def onTheMap(location):
        return 0 <= location.x < Util.MAP_WIDTH and 0 <= location.y < Util.MAP_HEIGHT

    @staticmethod
    def randomDirection():
        return Util.directions[Util.rng.randint(0, len(Util.directions) - 1)]

    @staticmethod
    def randomDirectionFromList(newDirections):
        return newDirections[Util.rng.randint(0, len(newDirections) - 1)]
