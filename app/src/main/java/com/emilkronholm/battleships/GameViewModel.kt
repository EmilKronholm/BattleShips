package com.emilkronholm.battleships

import android.util.Log
import androidx.compose.runtime.currentRecomposeScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine()

    var gamesMap = MutableStateFlow<Map<String, Game>>(emptyMap())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // Create a new game and set initial state
    fun createGame(player1ID: String, player2ID: String) {
        val initialBoard = List(100) { BoardSquareState.EMPTY }

        val newGame = Game(
            player1ID = player1ID,
            player2ID = player2ID,
            player1Ready = false,
            player2Ready = false,
            board1 = initialBoard,
            board2 = initialBoard,
            gameState = GameState.PRE_GAME
        )

        gameEngine.createPreGame(newGame)
    }

    // Set a player ready
    fun setPlayerReady(gameID: String, isPlayer1: Boolean, isReady:Boolean) {
        gameEngine.setReady(
            gameID = gameID,
            isPlayer1 = isPlayer1,
            isReady = isReady
        )
    }

    // Start the game
    fun startGame(gameID: String) {
        gameEngine.startGame(gameID)
    }

    // Handle player moves
    fun makeMove(gameID: String, square: Coordinate, isPlayer1: Boolean) {
        gamesMap.value[gameID]?.let { currentGame ->

            //Is the move a valid move?
            val board = if (isPlayer1) currentGame.board2 else currentGame.board1
            val index = square.y * 10 + square.x

            //Invalid move (already shot)
            if (board[index] == BoardSquareState.HIT ||
                board[index] == BoardSquareState.MISSED ||
                board[index] == BoardSquareState.SUNK) {

                println("Invalid move! :)")
                return
            }

            val result = if (board[index] == BoardSquareState.HIDDEN) BoardSquareState.HIT
                         else BoardSquareState.MISSED

            println("RESULT IS: ${result}")

            val move = Move(
                square.x,
                square.y,
                isPlayer1,
                result
            )

            gameEngine.makeMove(move, currentGame, gameID)
        } ?: run {
            Log.e("GameViewModel", "Game for gameID was null")
        }
    }

    fun resignGame(gameID: String, isPlayer1: Boolean) {
        gameEngine.resignGame(gameID, isPlayer1)
    }

    // Fetch and observe game state
    fun observeGame(gameID: String) {
        gameEngine.listenToGame(gameID = gameID, onSuccess = { game ->
            gamesMap.value = gamesMap.value.toMutableMap().apply {
                put(gameID, game)
            }
        })
    }

    fun startScanForGames(playerID: String) {
        gameEngine.scanForGamesForPlayer(playerID = playerID, onSuccess = { (gameID, game) ->
            println("Callback for game in viewmodel")
            gamesMap.value = mapOf("123" to Game())
            gamesMap.value = mapOf(gameID to game)

        })

    }

    override fun onCleared() {
        super.onCleared()

    }

    fun uploadBoard(gameID: String, isPlayer1: Boolean, board: Board) {
        val list = boardToList(board)
        assert(list.size == 100)
        gameEngine.uploadBoard(gameID, isPlayer1, list)
    }

    private fun boardToList(board: Board) : List<BoardSquareState> {
        val list = List<BoardSquareState>(100) {BoardSquareState.EMPTY}.toMutableList()

        for (x in 0..9) {
            for (y in 0..9) {
                list[10*x+y] = board.getState(Coordinate(x, y))
            }
        }
        return list
    }

    //Takes a board (as list) and returns a copy where HIT's been updated to SUNK if all
    //connected parts are HIT
    private fun updateForSunk(list: List<BoardSquareState>): List<BoardSquareState> {
        val updatedList = list.toMutableList()
        val visited = mutableSetOf<Int>() // Tracks indices we've already processed

        fun dfs(index: Int, currentShip: MutableList<Int>) {
            if (index in visited || list[index] != BoardSquareState.HIT && list[index] != BoardSquareState.HIDDEN) return
            visited.add(index)
            currentShip.add(index)

            // Calculate row and column
            val row = index / 10
            val col = index % 10

            // Explore neighbors (up, down, left, right)
            if (row > 0) dfs(index - 10, currentShip) // Up
            if (row < 9) dfs(index + 10, currentShip) // Down
            if (col > 0) dfs(index - 1, currentShip) // Left
            if (col < 9) dfs(index + 1, currentShip) // Right
        }

        for (i in list.indices) {
            if (i in visited || list[i] != BoardSquareState.HIT && list[i] != BoardSquareState.HIDDEN) continue

            val currentShip = mutableListOf<Int>()
            dfs(i, currentShip)

            // If all parts of the ship are HIT, mark them as SUNK
            if (currentShip.all { list[it] == BoardSquareState.HIT }) {
                for (index in currentShip) {
                    updatedList[index] = BoardSquareState.SUNK
                }
            }
        }

        return updatedList
    }

}
