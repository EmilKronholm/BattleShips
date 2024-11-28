package com.emilkronholm.battleships

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

data class Player (
    val name : String = "",
    var isInGame : Boolean = false,
    val createdAt : Timestamp = Timestamp.now()
)

class PlayerEngine {

    private val database = Firebase.firestore
    private var listenerRegistration: ListenerRegistration? = null

    fun startScanningForPlayers(onSuccess : (Map<String, Player>) -> Unit = {}, onFailure : (String) -> Unit = {}) {
        listenerRegistration  = database.collection("players")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onFailure(error.message.toString())
                }

                if (value != null) {
                    val updatedPlayerMap = value.documents.associate { doc ->
                        doc.id to doc.toObject(Player::class.java)!!
                    }
                    onSuccess(updatedPlayerMap)
                }
            }
    }

    fun stopScanningForPlayers()
    {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    fun addPlayer(player: Player, onSuccess : (String) -> Unit = {}, onFailure : (String) -> Unit = {}) {
        database.collection("players")
            .add(player)
            .addOnSuccessListener { documentReference ->
                onSuccess(documentReference.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message.toString())
            }
    }
}