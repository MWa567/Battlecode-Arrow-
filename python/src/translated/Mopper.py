import random
from battlecode25.stubs import *

class Mopper:
    nearest_tower = None
    my_target = None
    reached_target = False
    ruins = []
    visited = []

    def __init__(self, rc: RobotController) -> None:
        self.rc = rc
        self.my_target = rc.getLocation()
        bc.MapRecorder.init_map(self.rc.getMapWidth(), self.rc.getMapHeight())

    def play(self) -> None:
        translated.Util.init(self.rc)
        translated.Pathfinding.init(self.rc)
        translated.Pathfinding.init_turn()

        while True:
            nearby_robots = self.rc.senseNearbyRobots()
            for robo in nearby_robots:
                if robo.team != self.rc.getTeam() and (robo.type == UnitType.SOLDIER or robo.type == UnitType.SPLASHER):
                    self.my_target = robo.location
                    dir = self.rc.getLocation().directionTo(self.my_target)
                    if self.rc.canMopSwing(dir) and robo.team != self.rc.getTeam():
                        self.rc.mopSwing(dir)
                        translated.Pathfinding.move(self.my_target, False)
                        Clock.yield()

            nearby_tiles = self.rc.senseNearbyMapInfos()
            cur_ruin = None
            ruin_dist = 999999
            for tile in nearby_tiles:
                potential_tower = self.rc.senseRobotAtLocation(tile.getMapLocation())
                if tile.hasRuin():
                    if potential_tower is None:
                        dist = tile.getMapLocation().distanceSquaredTo(self.rc.getLocation())
                        if dist < ruin_dist:
                            cur_ruin = tile
                            ruin_dist = dist
                    if not tile.getMapLocation() in self.ruins:
                        for symmetric_ruin in self.getSymmetry(tile.getMapLocation()):
                            if symmetric_ruin not in self.ruins:
                                self.ruins.append(symmetric_ruin)
                                Clock.yield()

                if tile.getPaint().isEnemy():
                    target_loc = tile.getMapLocation()
                    if self.rc.canAttack(target_loc):
                        self.rc.attack(target_loc)
                        Clock.yield()

            if cur_ruin is not None:
                ruin_loc = cur_ruin.getMapLocation()
                dir = self.rc.getLocation().directionTo(ruin_loc)

                if self.rc.getMovementCooldownTurns() > 10:
                    # Do nothing
                    pass
                elif self.rc.canMove(dir):
                    self.rc.move(dir)
                elif self.rc.canMove(dir.rotateRight()):
                    self.rc.move(dir.rotateRight())

                counter = 0
                while counter <= 4:
                    if self.rc.isActionReady():
                        infos = self.rc.senseNearbyMapInfos()
                        for info in infos:
                            counter += 1
                            paint_loc = info.getMapLocation()
                            if info.getPaint().isEnemy() and self.rc.canAttack(paint_loc):
                                self.rc.attack(paint_loc)
                                counter = 0
                                Clock.yield()
                        Clock.yield()
                    Clock.yield()

            if not self.ruins:
                self.ruins = self.visited
                self.visited = []

            if translated.Util.distance(self.rc.getLocation(), self.my_target) <= 5 or not self.rc.isMovementReady():
                self.ruins.remove(self.my_target)
                self.visited.append(self.my_target)
                random_index = random.randint(0, len(self.ruins) - 1)
                self.my_target = self.ruins[random_index]
                Clock.yield()
            translated.Pathfinding.move(self.my_target, False)
            Clock.yield()

    def eliminateSymmetry(self, my_location: MapLocation) -> None:
        infos = self.rc.senseNearbyMapInfos()
        for info in infos:
            if not Comm.isSymmetryConfirmed:
                loc = info.getMapLocation()
                val = MapRecorder.vals[loc.x * self.rc.getMapHeight() + loc.y]
                if (val & MapRecorder.SEEN_BIT) != 0:
                    for sym in range(3):
                        if not Comm.isSymEliminated[sym] and not self.validateSymmetry(my_location, loc, sym):
                            Comm.eliminateSym(sym)

    def getSymmetry(self, loc: MapLocation) -> list:
        x = loc.x
        y = loc.y
        horizontal = MapLocation(self.rc.getMapWidth() - x - 1, y)
        vertical = MapLocation(x, self.rc.getMapHeight() - y - 1)
        diagonal = MapLocation(y, x)
        anti_diagonal = MapLocation(self.rc.getMapWidth() - y - 1, self.rc.getMapHeight() - x - 1)
        return [horizontal, vertical, diagonal, anti_diagonal]

    def getTarget(self) -> None:
        coord_x = self.rc.getLocation().x
        coord_y = self.rc.getLocation().y

        translated.Pathfinding.init(self.rc)
        translated.Pathfinding.init_turn()

        if coord_x < coord_y:
            if coord_x < self.rc.getMapWidth() // 2:
                random_ind = random.randint(0, self.rc.getMapHeight() - 1)
                self.my_target = MapLocation(self.rc.getMapWidth() - 1, random_ind)
            else:
                random_ind = random.randint(0, self.rc.getMapHeight() - 1)
                self.my_target = MapLocation(0, random_ind)
        else:
            if coord_y < self.rc.getMapHeight() // 2:
                random_ind = random.randint(0, self.rc.getMapWidth() - 1)
                self.my_target = MapLocation(random_ind, self.rc.getMapHeight() - 1)
            else:
                random_ind = random.randint(0, self.rc.getMapWidth() - 1)
                self.my_target = MapLocation(random_ind, 0)

    def validateSymmetry(self, origin: MapLocation, test: MapLocation, sym: int) -> bool:
        map_width = self.rc.getMapWidth()
        map_height = self.rc.getMapHeight()

        sym_loc = None
        if sym == Comm.SYM_ROTATIONAL:
            sym_loc = MapLocation(map_width - 1 - test.x, map_height - 1 - test.y)
        elif sym == Comm.SYM_VERTICAL:
            sym_loc = MapLocation(map_width - 1 - test.x, test.y)
        elif sym == Comm.SYM_HORIZONTAL:
            sym_loc = MapLocation(test.x, map_height - 1 - test.y)

        if sym_loc is None:
            return False

        original_val = MapRecorder.vals[origin.x * map_height + origin.y]
        sym_val = MapRecorder.vals[sym_loc.x * map_height + sym_loc.y]
        return (original_val & MapRecorder.CURRENT_MASK) == (sym_val & MapRecorder.CURRENT_MASK)

    def findEnemyTower(self) -> MapLocation:
        enemy_team = self.rc.getTeam().opponent()
        enemy_robots = self.rc.senseNearbyRobots(-1, enemy_team)
        closest_target = None
        closest_dist = float('inf')

        for robot in enemy_robots:
            if robot.type.isTowerType():
                dist = self.rc.getLocation().distanceSquaredTo(robot.location)
                if dist < closest_dist:
                    closest_target = robot.location
                    closest_dist = dist

        if closest_target is None and not Comm.isSymmetryConfirmed:
            for sym in range(3):
                if not Comm.isSymEliminated[sym]:
                    potential_target = self.calculateSymmetricLocation(self.rc.getLocation(), sym)
                    if self.rc.onTheMap(potential_target):
                        closest_target = potential_target
                        break

        return closest_target

    def calculateSymmetricLocation(self, loc: MapLocation, sym: int) -> MapLocation:
        map_width = self.rc.getMapWidth()
        map_height = self.rc.getMapHeight()
        if sym == Comm.SYM_ROTATIONAL:
            return MapLocation(map_width - 1 - loc.x, map_height - 1 - loc.y)
        elif sym == Comm.SYM_VERTICAL:
            return MapLocation(map_width - 1 - loc.x, loc.y)
        elif sym == Comm.SYM_HORIZONTAL:
            return MapLocation(loc.x, map_height - 1 - loc.y)
        return loc

    def randomMove(self) -> None:
        random_dir = Direction.values()[random.randint(0, len(Direction.values()) - 1)]
        if self.rc.canMove(random_dir):
            self.rc.move(random_dir)
