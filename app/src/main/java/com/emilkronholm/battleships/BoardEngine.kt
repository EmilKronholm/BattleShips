package com.emilkronholm.battleships

import androidx.compose.runtime.mutableStateListOf
import kotlin.random.Random

data class Coordinate(val x: Int, val y: Int) {
    operator fun plus(offset: Coordinate): Coordinate {
        return Coordinate(x + offset.x, y + offset.y)
    }
}

data class ShipInfo (
            val ship: Ship,
            val headPos: Coordinate
)

class Board() {
    var ships = mutableStateListOf<Ship>()
    val size = Coordinate(10, 10)

    init {
        shuffle()
    }

    fun getShipAt(coordinate : Coordinate) : ShipInfo? {
        for (ship in ships)
        {
            if (ship.contains(coordinate))
            {
                return ShipInfo(ship, ship.parts[0].coordinate)
            }
        }
        return null
    }

    //Check if the given ship is valid for current board
    private fun isShipValid(ship : Ship) : Boolean {
        for (part in ship.parts) {
            if (!(part.coordinate.x in 0 until size.x &&
                part.coordinate.y in 0 until size.y)) {
                return false
            }
        }

        val illegalCoordinates = mutableListOf<Coordinate>()
        for (ship in ships) {
            for (part in ship.parts) illegalCoordinates.add(part.coordinate)
            illegalCoordinates += ship.getListOfSurroundingCoordinates()
        }

        for (part in ship.parts) {
            for (illegalCoordinate in illegalCoordinates) {
                if (part.coordinate == illegalCoordinate) {
                    return false
                }
            }
        }

        return true
    }

    //Each square on the board can be HIT, WHOLE or EMPTY
    //Returns the state of square given by coordinate
    fun getState(coordinate: Coordinate): BoardSquareState {
        //PRECONDITION: Assert that coordinate is valid and in range of Board size
        require(coordinate.x in 0 until size.x && coordinate.y in 0 until size.y) {
            "Coordinate $coordinate is out of bounds. Valid range is (0,0) to (${size.x - 1}, ${size.y - 1})"
        }

        return ships.firstOrNull { it.contains(coordinate) }?.let { ship ->
            ship.getState(coordinate) ?: BoardSquareState.EMPTY
        } ?: BoardSquareState.EMPTY
    }


    //For every boat, we add all of its occupied squares (the body and surrounding parts)
    //into a set, and if we have duplicates then we exit early and return false.
    fun isValid(): Boolean {
        val occupiedCoordinates = mutableSetOf<Coordinate>()
        for (ship in ships) {
            val allCoordinates = ship.parts.map { it.coordinate }

            ship.getListOfSurroundingCoordinates().forEach { coordinate: Coordinate ->
                if (occupiedCoordinates.contains(coordinate)) {
                    return false
                }
            }

            if (allCoordinates.any {
                !occupiedCoordinates.add(it) ||
                        (it.x < 0 || it.x > 9 || it.y < 0 || it.y > 9)
            }) {
                return false
            }
        }
        return true
    }

    fun getListOfInvalidCoordinates(): List<Coordinate> {
        val validCoordinates = mutableSetOf<Coordinate>()
        val invalidCoordinates = mutableSetOf<Coordinate>()

        for (ship in ships) {
            val allCoordinates = ship.parts.map { it.coordinate }

            ship.getListOfSurroundingCoordinates().forEach { coordinate: Coordinate ->
                if (validCoordinates.contains(coordinate)) {
                    for (part in ship.parts) {
                        invalidCoordinates.add(part.coordinate)
                    }
                }
            }

            allCoordinates.forEach { coordinate ->
                if (!validCoordinates.add((coordinate))
                    || (coordinate.x < 0 || coordinate.x > 9 || coordinate.y < 0 || coordinate.y > 9)) {
                    for (part in ship.parts) {
                        invalidCoordinates.add(part.coordinate)
                    }
                }
            }
        }
        return invalidCoordinates.toList()
    }

    fun shuffle() {
        ships.clear()
        for (x in arrayOf(1, 1, 2, 2, 3, 4)) {
            while (true) {
                val pos = Coordinate(Random.nextInt(0, 10), Random.nextInt(0, 10))
                val newShip = Ship(pos, Random.nextInt(0, 2) % 2 == 0, x)

                if (isShipValid(newShip)) {
                    ships.add(newShip)
                    break
                }
            }
        }
    }
}

class Ship(position: Coordinate, var isVertical: Boolean, val length: Int) {
    var parts = mutableListOf<ShipPart>()

    init {
        updatePosition(position, isVertical, length)
    }

    fun updatePosition(position : Coordinate, isVertical : Boolean, length : Int)
    {
        parts.clear()
        for (a in 0..length-1) {
            val newPosition = if (isVertical) Coordinate(position.x, position.y+a)
            else Coordinate(position.x+a, position.y)
            parts.add(ShipPart(newPosition))
        }
    }

    //Returns true if Ship has a part on the given coordinate,
    //else it will return false
    fun contains(coordinate: Coordinate) : Boolean =
        parts.any { it.coordinate == coordinate }

    //Returns the state of a given part by its coordinate
    //Returns null if coordinate is invalid
    fun getState(coordinate: Coordinate) : BoardSquareState? =
        parts.firstOrNull { it.coordinate == coordinate }?.let {
            part -> if (part.isHit) BoardSquareState.HIT
                    else BoardSquareState.HIDDEN
        }

    fun getListOfSurroundingCoordinates() : List<Coordinate> {
        val surroundingCoordinates = mutableSetOf<Coordinate>()

        for (part in parts) {
            //8 Surrounding coordinates
            for (x in -1..1) {
                for (y in -1..1) {
                    if (x == 0 && y == 0) continue
                    surroundingCoordinates.add(Coordinate((part.coordinate.x+x), part.coordinate.y+y))
                }
            }
        }

        return surroundingCoordinates.toList()
    }

    fun moveShipTo(position: Coordinate) {
        updatePosition(position, isVertical, length);
    }

    fun rotate() {
        isVertical = !isVertical
        updatePosition(parts[0].coordinate, isVertical, length);
    }
}

class ShipPart(val coordinate: Coordinate) {
    var isHit = false
}