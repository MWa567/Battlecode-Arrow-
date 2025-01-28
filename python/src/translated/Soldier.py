import random
import funcsplayer0.Util #TODO Idk why this is here in the java version
from battlecode25.stubs import *

class Soldier(Robot):
    turn_count = 0
    rng = random.Random(6147)

    target = None
    original_location = None
    nearest_tower = None
    prev_target = None

    paint_tower_pattern = None
    money_tower_pattern = None
    defense_tower_pattern = None

    painting_ruin_loc = None
    painting_tower_type = None
    painting_turns = 0
    turns_without_attack = 0

    cur_resource = None
    override = False

    has_enemy_paint = False
    need_mopper_at = None

    # Array containing all the possible movement directions
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

    def __init__(self, rc: RobotController):
        super().__init__(rc)
        self.rc = rc
        # Initialize the map recorder with map dimensions
        MapRecorder.init_map(rc.getMapWidth(), rc.getMapHeight())
        self.paint_tower_pattern = rc.getTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER)
        self.money_tower_pattern = rc.getTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER)
        self.defense_tower_pattern = rc.getTowerPattern(UnitType.LEVEL_ONE_DEFENSE_TOWER)

    def play(self):
        translated.Util.init(self.rc)
        translated.Explore.init(self.rc)
        translated.Pathfinding.init(self.rc)
        translated.Pathfinding.initTurn()
        self.target = translated.Explore.getExploreTarget()

        while True:
            nearby_tiles = self.rc.senseNearbyMapInfos()
            cur_ruin = None
            ruin_dist = 999999
            tower_dist = 999999

            for tile in nearby_tiles:
                potential_tower = self.rc.senseRobotAtLocation(tile.getMapLocation())
                if tile.hasRuin() and potential_tower is None:
                    dist = tile.getMapLocation().distanceSquaredTo(self.rc.getLocation())
                    if dist < ruin_dist:
                        cur_ruin = tile
                        ruin_dist = dist
                elif tile.hasRuin() and potential_tower is not None:
                    if potential_tower.getTeam() == self.rc.getTeam():
                        dist = tile.getMapLocation().distanceSquaredTo(self.rc.getLocation())
                        if dist < tower_dist:
                            self.nearest_tower = tile.getMapLocation()
                            tower_dist = dist
                            if self.rc.getPaint() < 100:
                                if self.rc.canTransferPaint(self.nearest_tower, -75):
                                    self.rc.transferPaint(self.nearest_tower, -75)
                        if self.rc.canUpgradeTower(tile.getMapLocation()):
                            self.rc.upgradeTower(tile.getMapLocation())
                            print("Tower was upgraded!")

            if cur_ruin is not None:
                self.painting_ruin_loc = cur_ruin.getMapLocation()
                self.painting_tower_type = self.get_new_tower_type(self.rc)
                self.turns_without_attack = 0
                self.painting_turns = 0
                self.run_paint_pattern(self.rc)
                return

            if self.rc.getMovementCooldownTurns() > 10:
                Clock.yield()

            if self.rc.getLocation().x % 4 == 0 and self.rc.getLocation().y % 4 == 3:
                has_mark = False
                for pattern_tile in self.rc.senseNearbyMapInfos(self.rc.getLocation(), -1):
                    if pattern_tile.getMark() != pattern_tile.getPaint() and pattern_tile.getMark() != PaintType.EMPTY:
                        if pattern_tile.getMark() != PaintType.EMPTY:
                            has_mark = True
                        if self.rc.canAttack(pattern_tile.getMapLocation()):
                            self.rc.attack(pattern_tile.getMapLocation(), pattern_tile.getMark() == PaintType.ALLY_SECONDARY)

                if self.rc.getNumberTowers() > 8 and not has_mark and self.rc.canMarkResourcePattern(self.rc.getLocation()):
                    self.cur_resource = self.rc.getLocation()
                    self.target = self.cur_resource
                    self.rc.markResourcePattern(self.cur_resource)

            if self.cur_resource is not None:
                direction = self.rc.getLocation().directionTo(self.cur_resource)
                if self.rc.getMovementCooldownTurns() <= 10:
                    if self.rc.canMove(direction):
                        self.rc.move(direction)
                    elif self.rc.canMove(direction.rotateRight()):
                        self.rc.move(direction.rotateRight())
                    if self.rc.canCompleteResourcePattern(self.cur_resource):
                        self.rc.completeResourcePattern(self.cur_resource)
                        self.cur_resource = None
                        break
                    for pattern_tile in self.rc.senseNearbyMapInfos(self.cur_resource, -1):
                        if pattern_tile.getMark() != pattern_tile.getPaint() and pattern_tile.getMark() != PaintType.EMPTY:
                            if self.rc.canAttack(pattern_tile.getMapLocation()):
                                self.rc.attack(pattern_tile.getMapLocation(), pattern_tile.getMark() == PaintType.ALLY_SECONDARY)

            if cur_ruin is None and self.cur_resource is None:
                try:
                    if translated.Util.distance(self.rc.getLocation(), self.target) <= 5 or not self.rc.isMovementReady():
                        old_target = self.target
                        while translated.Util.distance(translated.Explore.getExploreTarget(), old_target) <= 20:
                            translated.Explore.getNewTarget(10)
                        self.target = translated.Explore.explore_target

                    self.rc.setIndicatorString(f"Moving toward target at {self.target}")
                    translated.Pathfinding.move(self.target, False)
                except Exception as e:
                    print(f"{self.rc.getType()} Exception")

            Clock.yield()

    @staticmethod
    def is_within_pattern(ruin_loc: MapLocation, paint_loc: MapLocation) -> bool:
        return abs(paint_loc.x - ruin_loc.x) <= 2 and abs(paint_loc.y - ruin_loc.y) <= 2 and not ruin_loc.equals(paint_loc)

    @staticmethod
    def get_is_secondary(ruin_loc: MapLocation, paint_loc: MapLocation, tower_type: UnitType) -> bool:
        if not Soldier.is_within_pattern(ruin_loc, paint_loc):
            return False
        col = paint_loc.x - ruin_loc.x + 2
        row = paint_loc.y - ruin_loc.y + 2
        if tower_type == UnitType.LEVEL_ONE_PAINT_TOWER:
            return Soldier.paint_tower_pattern[row][col]
        elif tower_type == UnitType.LEVEL_ONE_MONEY_TOWER:
            return Soldier.money_tower_pattern[row][col]
        else:
            return Soldier.defense_tower_pattern[row][col]

    @staticmethod
    def run_paint_pattern(rc: RobotController) -> None:
        rc.setIndicatorString("SEARCHING FOR NEARBY ROBOTS")
        for robot in rc.senseNearbyRobots(-1):
            if robot.team == rc.getTeam() and robot.type == UnitType.SOLDIER and \
                    Soldier.painting_ruin_loc.distanceSquaredTo(robot.location) < rc.getLocation().distanceSquaredTo(Soldier.painting_ruin_loc):
                translated.Pathfinding.move(Soldier.target, False)
                return

        # paint tiles for the pattern
        if rc.isActionReady():
            infos = rc.senseNearbyMapInfos()
            attacked = False
            for info in infos:
                paint_loc = info.getMapLocation()
                should_be_secondary = Soldier.get_is_secondary(Soldier.painting_ruin_loc, paint_loc, Soldier.painting_tower_type)
                if rc.canAttack(paint_loc) and (info.getPaint() == PaintType.EMPTY or info.getPaint().isSecondary() != should_be_secondary) \
                        and Soldier.is_within_pattern(Soldier.painting_ruin_loc, paint_loc):
                    rc.attack(paint_loc, should_be_secondary)
                    attacked = True
                    Soldier.turns_without_attack = 0
                    break
            if not attacked:
                Soldier.turns_without_attack += 1

        direction = rc.getLocation().directionTo(Soldier.painting_ruin_loc)

        if rc.getMovementCooldownTurns() > 10:
            # Do nothing
            pass
        elif rc.canMove(direction):
            rc.move(direction)
        elif rc.canMove(direction.rotateRight()):
            rc.move(direction.rotateRight())

        if rc.canCompleteTowerPattern(Soldier.painting_tower_type, Soldier.painting_ruin_loc):
            rc.completeTowerPattern(Soldier.painting_tower_type, Soldier.painting_ruin_loc)

    @staticmethod
    def get_new_tower_type(rc: RobotController) -> UnitType:
        random_number = random.randint(0, 6)
        in_middle_x = 0.3 * rc.getMapWidth() < rc.getLocation().x < 0.7 * rc.getMapWidth()
        in_middle_y = 0.3 * rc.getMapHeight() < rc.getLocation().y < 0.7 * rc.getMapHeight()

        if in_middle_x and in_middle_y:
            return UnitType.LEVEL_ONE_DEFENSE_TOWER
        elif rc.getNumberTowers() < 4:
            return UnitType.LEVEL_ONE_MONEY_TOWER
        elif random_number in {0, 1, 2}:
            return UnitType.LEVEL_ONE_PAINT_TOWER
        else:
            return UnitType.LEVEL_ONE_MONEY_TOWER
