package com.emilkronholm.battleships

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GameViewModel : ViewModel() {
    private val gameEngine = GameEngine()

    val gamesMap = MutableStateFlow<Map<String, Game>>(emptyMap())

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    // Create a new game and set initial state
    fun createGame(player1ID: String, player2ID: String) {
        val initialBoard = List(10) { List(10) { BoardSquareState.EMPTY } }

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
    fun setPlayerReady(gameID: String, isPlayer1: Boolean) {
        gameEngine.setReady(
            gameID = gameID,
            isPlayer1 = isPlayer1
        )
    }

    // Start the game
    fun startGame(gameID: String) {
        gameEngine.startGame(gameID)
    }

    // Handle player moves
    fun makeMove(gameID: String, move: Move) {
        gamesMap.value[gameID]?.let { currentGame ->
            gameEngine.makeMove(move, currentGame, gameID)
        } ?: run {
            Log.e("GameViewModel", "Game for gameID was null")
        }
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
            gamesMap.value = gamesMap.value.toMutableMap().apply {
                put(gameID, game)
            }
        })
    }

    override fun onCleared() {
        super.onCleared()

    }
}
