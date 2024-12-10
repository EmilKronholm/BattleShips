package com.emilkronholm.battleships

import android.util.Log
import androidx.compose.runtime.currentRecomposeScope
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CurrentGame(
    val gameID: String,
    val playerID: String,
    val isPlayer1: Boolean
)

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine()

    var gamesMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    var currentGame = MutableStateFlow<CurrentGame?>(null)

    // Create a new game and start pregame. State = PRE_GAME
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
    fun setPlayerReady(isReady:Boolean) {
        gameEngine.setReady(
            gameID = currentGame.value!!.gameID,
            isPlayer1 = currentGame.value!!.isPlayer1,
            isReady = isReady
        )
    }

    // Start the game
    fun startGame() {
        gameEngine.setGameState(currentGame.value!!.gameID, GameState.PLAYER1_TURN)
    }

    // Handle player moves
    fun makeMove(square: Coordinate, onError: (String) -> Unit) {
            //Is the move a valid move?
        val _game = gamesMap.value[currentGame.value!!.gameID]!!
        val board = if (currentGame.value!!.isPlayer1)
                    _game.board2 else _game.board1
        val index = square.y * 10 + square.x

        //Invalid move (already shot)
        if (board[index] == BoardSquareState.HIT ||
            board[index] == BoardSquareState.MISSED ||
            board[index] == BoardSquareState.SUNK) {

            println("Invalid move! :)")
            return
        }

        //Calculate result
        val result = if (board[index] == BoardSquareState.HIDDEN) BoardSquareState.HIT
                     else BoardSquareState.MISSED

        //Create updated board
        val temp = board.toMutableList()
        temp[index] = result
        val updatedBoard = updateForSunk(temp)

        //Upload board
        gameEngine.uploadBoard(currentGame.value!!.gameID, !currentGame.value!!.isPlayer1, updatedBoard)

        //If missed, switch turns
        if (result == BoardSquareState.MISSED) {
            val newGameState = if (currentGame.value!!.isPlayer1)
                GameState.PLAYER2_TURN else GameState.PLAYER1_TURN
            gameEngine.setGameState(currentGame.value!!.gameID, newGameState)
        }
    }

    fun resignGame() {
        val newState = if (currentGame.value!!.isPlayer1)
                        GameState.PLAYER2_WIN else GameState.PLAYER1_WIN
        gameEngine.setGameState(currentGame.value!!.gameID, newState)
    }

    // Fetch and observe game state
    fun observeGame(gameID: String, playerID: String) {
        gameEngine.listenToGame(gameID = gameID, onSuccess = { game ->
            gamesMap.value = gamesMap.value.toMutableMap().apply {
                put(gameID, game)
            }

            currentGame.value = CurrentGame(
                gameID = gameID,
                playerID = playerID,
                isPlayer1 = (playerID == game.player1ID)
            )

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
        uploadBoardAsList(list)
    }

    fun uploadBoardAsList(list: List<BoardSquareState> ) {
        gameEngine.uploadBoard(currentGame.value!!.gameID, currentGame.value!!.isPlayer1, list)
    }


    // Utility functions
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
