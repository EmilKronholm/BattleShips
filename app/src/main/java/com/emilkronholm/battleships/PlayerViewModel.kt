package com.emilkronholm.battleships

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class PlayerViewModel : ViewModel() {
    private var players = MutableStateFlow<Map<String, Player>>(emptyMap())
    val localUserID = "?"
    val playerEngine = PlayerEngine()

    fun addPlayer(name: String) {
        val player = Player (name = name)
        playerEngine.addPlayer(player)
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

    override fun onCleared() {
        super.onCleared()
        playerEngine.stopScanningForPlayers()
    }
}