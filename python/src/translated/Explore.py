import math
import random
from battlecode25.stubs import *
class Explore:
    rc = None
    last_explore_dir = None
    EXPLORE_BOREDOM = 20
    boredom = 0
    MIN_DIST_FROM_WALL = 3
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
    dir_path = []
    MAX_MAP_SIZE = 60
    MAX_MAP_SIZE_SQ = MAX_MAP_SIZE * MAX_MAP_SIZE
    MAX_MAP_SIZE2 = 120
    visited = [[False] * MAX_MAP_SIZE for _ in range(MAX_MAP_SIZE)]
    vision_radius = 0
    initialized = False
    init_row = 0
    INIT_BC_LEFT = 300
    VISITED_BC_LEFT = 100
    explore_target = None
    explore_dir = Direction.CENTER
    angle = 0
    EXPLORE_DIST = 100
    explore3_target = None
    rotate_left = None
    BYTECODES_USED = 2500
    last_dir_moved = None
    initial_x = 0
    initial_y = 0

    class ExploreType:
        PATHFINDING = 0
        GREEDY = 1

    explore_type = None

    @staticmethod
    def init(r):
        Explore.rc = r
        Explore.get_new_target(10)

    @staticmethod
    def pick_new_explore_dir():
        new_dir_choices = [
            Explore.last_explore_dir.rotate_left(),
            Explore.last_explore_dir,
            Explore.last_explore_dir.rotate_right()
        ]
        valid_dirs = [dir for dir in new_dir_choices]
        if valid_dirs:
            Explore.last_explore_dir = random.choice(valid_dirs)
        else:
            # This can happen if you're going straight into a corner or wall
            # In this case, we choose from close to the opposite current explore dir
            switch_choice = random.randint(0, 2)
            if switch_choice == 0:
                Explore.last_explore_dir = Explore.last_explore_dir.opposite().rotate_left()
            elif switch_choice == 1:
                Explore.last_explore_dir = Explore.last_explore_dir.opposite().rotate_right()
            else:
                Explore.last_explore_dir = Explore.last_explore_dir.opposite()

    @staticmethod
    def rotate_away_from_wall_if_necessary(dir):
        curr_loc = Explore.rc.get_location()
        if dir == Direction.SOUTH:
            if curr_loc.x < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_left()
            if Util.MAP_WIDTH - curr_loc.x < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_right()
        elif dir == Direction.NORTH:
            if curr_loc.x < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_right()
            if Util.MAP_WIDTH - curr_loc.x < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_left()
        elif dir == Direction.WEST:
            if curr_loc.y < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_right()
            if Util.MAP_HEIGHT - curr_loc.y < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_left()
        elif dir == Direction.EAST:
            if curr_loc.y < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_left()
            if Util.MAP_HEIGHT - curr_loc.y < Explore.MIN_DIST_FROM_WALL:
                return dir.rotate_right()
        return dir

    @staticmethod
    def init_explore_dir():
        Explore.assign_explore3_dir(Explore.directions[random.randint(0, 7)])

    @staticmethod
    def initialize():
        if Explore.initialized:
            return
        while Explore.init_row < Explore.MAX_MAP_SIZE:
            if Clock.get_bytecodes_left() < Explore.INIT_BC_LEFT:
                return
            Explore.visited[Explore.init_row] = [False] * Explore.MAX_MAP_SIZE
            Explore.init_row += 1
        Explore.initialized = True

    @staticmethod
    def emergency_target(tries):
        my_loc = Explore.rc.get_location()
        if Explore.explore_target is not None and my_loc.distance_squared_to(Explore.explore_target) > Explore.vision_radius:
            return
        x, y = my_loc.x, my_loc.y
        for _ in range(tries):
            dx = random.randint(0, Explore.MAX_MAP_SIZE2) - Explore.MAX_MAP_SIZE
            dy = random.randint(0, Explore.MAX_MAP_SIZE2) - Explore.MAX_MAP_SIZE
            Explore.explore_target = MapLocation(x + dx, y + dy)
            if my_loc.distance_squared_to(Explore.explore_target) > Explore.vision_radius:
                return
        Explore.explore_target = None

    @staticmethod
    def get_explore_target():
        return Explore.explore_target

    @staticmethod
    def get_explore3_target():
        Explore.check_direction()
        return Explore.explore3_target

    @staticmethod
    def assign_explore3_dir(dir):
        Explore.explore_dir = dir
        temp_angle = math.atan2(dir.dy, dir.dx)
        tries = 10
        x, y = Explore.rc.get_location().x, Explore.rc.get_location().y
        for _ in range(tries):
            # Try for more variance in the direction?
            Explore.angle = temp_angle + (random.random() * 45 - 22.5) / 180 * math.pi
            x += math.cos(Explore.angle) * Explore.EXPLORE_DIST
            y += math.sin(Explore.angle) * Explore.EXPLORE_DIST
            Explore.explore3_target = MapLocation(int(x), int(y))
            if Util.on_the_map(Explore.explore3_target):
                return

    @staticmethod
    def check_direction():
        if Explore.explore3_target.is_within_distance_squared(Explore.rc.get_location(), Explore.vision_radius):
            return
        east = Explore.east_closer()
        if east == 1:
            Explore.assign_explore3_dir(Direction.WEST)
        elif east == -1:
            Explore.assign_explore3_dir(Direction.EAST)
        else:
            return
        north = Explore.north_closer()
        if north == 1:
            Explore.assign_explore3_dir(Direction.SOUTH)
        elif north == -1:
            Explore.assign_explore3_dir(Direction.NORTH)

    @staticmethod
    def east_closer():
        return 1 if Explore.rc.get_map_width() - Explore.rc.get_location().x <= Explore.rc.get_location().x else -1

    @staticmethod
    def north_closer():
        return 1 if Explore.rc.get_map_height() - Explore.rc.get_location().y <= Explore.rc.get_location().y else -1

    @staticmethod
    def has_visited(loc):
        if not Explore.initialized:
            return False
        return Explore.visited[loc.x][loc.y]

    @staticmethod
    def get_new_target(tries):
        curr_loc = Explore.rc.get_location()
        for _ in range(tries):
            dx = 4 * (random.randint(0, 16) - 8)
            dy = 4 * (random.randint(0, 16) - 8)
            Explore.explore_target = MapLocation(curr_loc.x + dx, curr_loc.y + dy)
            Explore.explore_target = Util.clip_to_within_map(Explore.explore_target)
            if not Explore.has_visited(Explore.explore_target):
                return

    @staticmethod
    def mark_seen():
        if not Explore.initialized:
            return
        try:
            loc = Explore.rc.get_location()
            for i in range(len(Explore.dir_path) - 1, -1, -1):
                if Clock.get_bytecodes_left() < Explore.VISITED_BC_LEFT:
                    return
                loc = loc.add(Explore.dir_path[i])
                if Explore.rc.on_the_map(loc):
                    Explore.visited[loc.x][loc.y] = True
        except Exception as e:
            print(e)

    @staticmethod
    def fill_dir_path():
        if Explore.vision_radius == 20:
            Explore.dir_path = [
                Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH,
                Direction.NORTH, Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST,
                Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH,
                Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST,
                Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH,
                Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST,
                Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST,
                Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH,
                Direction.NORTH, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTH,
                Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.WEST,
                Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.EAST, Direction.EAST, Direction.SOUTH,
                Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.CENTER
            ]
        else:
            Explore.dir_path = [
                Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH,
                Direction.NORTH, Direction.NORTH, Direction.NORTHEAST, Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST,
                Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTHEAST,
                Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.SOUTHWEST,
                Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST, Direction.NORTHWEST,
                Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH,
                Direction.NORTHEAST, Direction.NORTHEAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST,
                Direction.SOUTHEAST, Direction.SOUTHEAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH,
                Direction.SOUTHWEST, Direction.SOUTHWEST, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTHWEST,
                Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTHEAST,
                Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.SOUTH,
                Direction.SOUTH, Direction.SOUTH, Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.WEST,
                Direction.WEST, Direction.NORTHWEST, Direction.NORTH, Direction.NORTH, Direction.NORTH, Direction.NORTH,
                Direction.EAST, Direction.EAST, Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.SOUTH,
                Direction.SOUTH, Direction.WEST, Direction.WEST, Direction.WEST, Direction.NORTH, Direction.NORTH,
                Direction.NORTH, Direction.EAST, Direction.EAST, Direction.SOUTH, Direction.SOUTH, Direction.WEST,
                Direction.NORTH, Direction.CENTER
            ]
