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
}
