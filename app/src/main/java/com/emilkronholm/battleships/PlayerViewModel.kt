package com.emilkronholm.battleships

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerViewModel : ViewModel() {
    var players = MutableStateFlow<Map<String, Player>>(emptyMap())
    var localUserID = "?"
    var localUserName : String? = null
    private val playerEngine = PlayerEngine()

    fun addPlayer(name: String,
                  onSuccess : (String) -> Unit = {},
                  onFailure : (String) -> Unit = {}) {
        val player = Player (name = name)
        playerEngine.addPlayer(player, onSuccess = { id ->
            println("New player added with id: $id")
            onSuccess(id)
        }, onFailure = { exception ->
            onFailure("")
            Log.e("PlayerViewModel", exception)
        })
    }

    fun getName(onResult: (String?) -> Unit) {
        //If player doesn't exist in local map, refer to the engine (database)
        if (players.value[localUserID]?.name == null) {
            playerEngine.getPlayerName(localUserID, onSuccess = { n ->
                onResult(n)
                return@getPlayerName
            })

        }
        else {
            onResult(players.value[localUserID]?.name!!)
        }
    }

    fun scanForPlayers() {
        playerEngine.startScanningForPlayers(
            onSuccess = { map ->
                players.value = map
            },
            onFailure = { error ->
                println(error)
            }
        )
    }

    fun stopScanningForPlayer() {
        playerEngine.startScanningForPlayers()
    }

    override fun onCleared() {
        super.onCleared()
        playerEngine.stopScanningForPlayers()
    }
}