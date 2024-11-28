package com.emilkronholm.battleships

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

//Only handles state (by talking to engine etc)
class ChallengeViewModel : ViewModel() {
    var challenges = MutableStateFlow<Map<String, Challenge>>(emptyMap())
    private val challengeEngine = ChallengeEngine()

    fun scanChallengesForPlayer(playerId: String) {
        challengeEngine.startScanningForChallenge(
            playerId,
            onSuccess = { map ->
                challenges.value = map
            },
            onFailure = { error ->
                println(error)
            }
        )
    }

    fun createChallenge(opponentID : String, playerID: String) {

        val challenge = Challenge(
            challenger = playerID,
            recipient = opponentID
        )

        challengeEngine.createChallenge(
            challenge = challenge,
            onSuccess = { id ->
                println("Successfully created challenge with id: $id")
            },
            onFailure = { error ->
                println("Error message: $error")
            }
        )
    }

    fun declineChallenge(challengeID : String) {
        challengeEngine.declineChallenge(
            challengeID = challengeID,
            onSuccess = {
                println("Successfully declined challenge $challengeID")
            },
            onFailure = { error ->
                println("Error message: $error")
            }
        )
    }

    fun acceptChallenge(challengeID: String) {
        challengeEngine.acceptChallenge(
            challengeID = challengeID,
            onSuccess = {
                println("Successfully accepted challenge $challengeID")
            },
            onFailure = { error ->
                println("Error message: $error")
            }
        )
    }

    override fun onCleared() {
        super.onCleared()
        challengeEngine.stopScanningForChallenge()
    }
}