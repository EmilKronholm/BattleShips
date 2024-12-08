package com.emilkronholm.battleships

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects

enum class GameState {
    PRE_GAME,
    PLAYER1_TURN,
    PLAYER2_TURN,
    PLAYER1_WIN,
    PLAYER2_WIN
}

enum class BoardSquareState {
    EMPTY,
    HIDDEN,
    MISSED,
    HIT,
    SUNK
}

data class Game (
    val player1ID: String = "",
    val player2ID: String = "",
    val player1Ready: Boolean = false,
    val player2Ready: Boolean= false,
    val board1 : List<BoardSquareState> = List(100) { BoardSquareState.EMPTY },
    val board2 : List<BoardSquareState> = List(100) { BoardSquareState.EMPTY },
    val gameState : GameState = GameState.PRE_GAME
)

data class Move (
    val x : Int,
    val y : Int,
    val isPlayer1 : Boolean,
    val result : BoardSquareState
)

class GameEngine {

    private val database = Firebase.firestore
    private var listenerRegistration: ListenerRegistration? = null

    fun scanForGamesForPlayer(playerID : String, onSuccess : (Map.Entry<String, Game>) -> Unit = {}, onFailure : (String) -> Unit = {})
    {
        listenerRegistration = database.collection("games")
            .whereEqualTo("gameState", GameState.PRE_GAME)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure("Error observing game: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val allGamesMap = snapshot.documents.associate { doc ->
                        doc.id to doc.toObject(Game::class.java)!!
                    }

                    for (game in allGamesMap) {
                        if (game.value.player1ID == playerID || game.value.player2ID == playerID)
                        {
                            onSuccess(game)
                            break
                        }
                    }
                }
            }
    }

    fun listenToGame(gameID: String, onSuccess : (Game) -> Unit = {}, onFailure : (String) -> Unit = {}) {
        listenerRegistration = database.collection("games")
            .document(gameID)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure("Error observing game: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val game = snapshot.toObject(Game::class.java)
                    if (game != null)
                    {
                        onSuccess(game)
                    }
                }
            }
    }

    fun stopListen() {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    //Used for pregame
    fun createPreGame(game: Game)
    {
        println("HEJHEJHEJHEJ")
        database.collection("games")
            .add(game).addOnSuccessListener { id ->
                println("Successfully added game with $id")
            }.addOnFailureListener { exception ->
                Log.e("GameEngine", "Failed to create pregame: $exception")
            }
    }

    fun setReady(gameID: String, isPlayer1: Boolean, isReady: Boolean)
    {
        val field = if (isPlayer1) "player1Ready" else "player2Ready"
        database.collection("games")
            .document(gameID)
            .update(field, isReady)
    }

    //Game
    fun startGame(gameID : String)
    {
        database.collection("games")
            .document(gameID)
            .update("gameState", GameState.PLAYER1_TURN)
    }

    fun makeMove(move: Move, game: Game, gameID: String)
    {
        var updatedGame : Game
        var boardFieldName : String

        if (move.isPlayer1) {
            boardFieldName = "board1"
            val updatedBoard = game.board1.toMutableList()
            updatedBoard[move.x+move.y*10] = move.result
            updatedGame = game.copy(board1 = updatedBoard)
        } else {
            boardFieldName = "board2"
            val updatedBoard = game.board2.toMutableList()
            updatedBoard[move.x+move.y*10] = move.result
            updatedGame = game.copy(board2 = updatedBoard)
        }


        val board = if (move.isPlayer1) updatedGame.board1 else updatedGame.board2
        val gameState = if (move.isPlayer1) GameState.PLAYER1_TURN else GameState.PLAYER2_TURN
        //No validation is made here
        database.collection("games")
            .document(gameID)
            .update(mapOf(
                "gameState" to gameState,
                boardFieldName to board
            ))


    }

    fun uploadBoard(gameID:String, isPlayer1: Boolean, list: List<BoardSquareState>) {
        val field = if (isPlayer1) "board1" else "board2"
        database.collection("games").document(gameID)
            .update(field, list).addOnSuccessListener {
                println("Updated ${field}")
            }.addOnFailureListener { exception ->
                println("error wehn updating ${field}  ${exception}")
            }
    }
}