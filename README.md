# Tower-Defense-Game

TowerDefenseGame

A simple console-based Tower Defense game written in Java.
Stop enemies from reaching your base by placing towers strategically.

Features

Grid-based map (default 20×6).
Waves of enemies that move from left to right.
Towers with range and damage; cost gold to place.
Earn gold by defeating enemies.
Lose lives if enemies reach the base.
Turn-based gameplay, playable entirely in the console.

How to Play

Commands per turn:
p x y → Place a tower at coordinates (x,y) (cost: 12 gold).
s → Skip placing a tower.
q → Quit the game.

Grid Legend:

. → Empty space
B → Base (enemies try to reach this column)
T → Tower
0-9 → Number of enemies in the cell

Objective:

Prevent enemies from reaching the base.
Survive all waves to win.

Requirements

Java 8 or later
IntelliJ IDEA (or any Java IDE / command-line Java compiler)
