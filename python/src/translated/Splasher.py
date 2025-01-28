import random
from battlecode25.stubs import *

class Splasher:

    rc = None
    nearestTower = None
    nearestTowerInfo = None
    prevTarget = None
    my_target = None
    reached_target = False
    hasResource = False

    towerSpotted = False
    myEnemyTower = None

    def __init__(self, rc):
        self.rc = rc

    def play(self):
        if self.towerSpotted:
            if self.rc.can_attack(self.myEnemyTower):
                self.rc.attack(self.myEnemyTower)
            elif self.rc.is_action_ready():
                dir_to_tower = self.rc.get_location().direction_to(self.myEnemyTower)
                if self.rc.can_move(dir_to_tower):
                    self.rc.move(dir_to_tower)
            if self.rc.can_attack(self.myEnemyTower):
                self.rc.attack(self.myEnemyTower)
            if self.rc.sense_robot_at_location(self.myEnemyTower) is None:
                self.towerSpotted = False
            return

        nearby_tiles = self.rc.sense_nearby_map_infos()
        for tile in nearby_tiles:
            potential_tower = self.rc.sense_robot_at_location(tile.get_map_location())
            if tile.has_ruin() and potential_tower is not None:
                if potential_tower.get_team() != self.rc.get_team():
                    # enemy tower
                    self.myEnemyTower = potential_tower.location
                    dir_to_tower = self.rc.get_location().direction_to(self.myEnemyTower)
                    self.towerSpotted = True
                    if self.rc.can_move(dir_to_tower):
                        self.rc.move(dir_to_tower)
                    if self.rc.can_attack(self.myEnemyTower):
                        self.rc.attack(self.myEnemyTower)
                    return

        if Util.distance(self.my_target, self.rc.get_location()) <= 5:
            self.reached_target = True
            new_x = self.map_width - self.my_target.x - 1 if self.original_location.x <= self.map_width / 2 else self.map_width - self.my_target.x + 1
            new_y = self.map_height - self.my_target.y - 1 if self.original_location.y <= self.map_height / 2 else self.map_height - self.my_target.y + 1
            self.my_target = MapLocation(new_x, new_y)
        elif not self.reached_target:
            self.get_target()

        self.rc.set_indicator_string(f"MOVING TO TARGET AT {self.my_target}")
        Pathfinding.move(self.my_target, True)

    def get_target(self):
        coord_x = self.original_location.x
        coord_y = self.original_location.y

        Pathfinding.init(self.rc)
        Pathfinding.init_turn()

        if coord_x < coord_y:
            if coord_x < self.map_width / 2:
                random_ind = random.randint(0, self.map_height - 1)
                self.my_target = MapLocation(self.map_width - 1, random_ind)
            else:
                random_ind = random.randint(0, self.map_height - 1)
                self.my_target = MapLocation(0, random_ind)
            self.update_enemy_robots(self.rc)
        else:
            if coord_y < self.map_height / 2:
                random_ind = random.randint(0, self.map_width - 1)
                self.my_target = MapLocation(random_ind, self.map_height - 1)
            else:
                random_ind = random.randint(0, self.map_width - 1)
                self.my_target = MapLocation(random_ind, 0)
            self.update_enemy_robots(self.rc)

    def update_enemy_robots(self, rc):
        enemy_robots = rc.sense_nearby_robots(-1, rc.get_team().opponent())
        if enemy_robots:
            self.rc.set_indicator_string("There are nearby enemy robots! Scary!")
            enemy_locations = [robot.get_location() for robot in enemy_robots]

            ally_robots = rc.sense_nearby_robots(-1, rc.get_team())
            if rc.get_round_num() % 20 == 0:
                for ally in ally_robots:
                    if rc.can_send_message(ally.location, len(enemy_robots)):
                        rc.send_message(ally.location, len(enemy_robots))
