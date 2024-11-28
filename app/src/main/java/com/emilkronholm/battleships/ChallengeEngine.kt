package com.emilkronholm.battleships

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore

data class Challenge (
    var challenger : String = "",
    var recipient : String = "",
    var status : String = "pending", //"pending", "accepted", "declined"
    var createdAt : Timestamp = Timestamp.now()
)

//Handels all bussiness logic
class ChallengeEngine {
    private val database = Firebase.firestore
    private var listenerRegistration: ListenerRegistration? = null

    //Create a new challenge
    //OnSuccess: Returns the challengeID as string
    //OnFailure: Returns exception as string
    fun createChallenge(challenge: Challenge, onSuccess : (String) -> Unit = {}, onFailure : (String) -> Unit = {}) {
        database.collection("challenges")
            .add(challenge)
            .addOnSuccessListener {
                onSuccess(it.id)
            }
            .addOnFailureListener { exception ->
                Log.e("GameModelError (challengePlayer)", exception.toString())
                onFailure(exception.toString())
            }
    }

    //Update challengeList with new challenges for player with playerID
    //OnSuccess: Callback with map of challenges
    //OnFailure: Callback with exception
    fun startScanningForChallenge(playerID : String, onSuccess : (Map<String, Challenge>) -> Unit = {}, onFailure : (String) -> Unit = {}) {
        listenerRegistration  = database.collection("challenges")
            .whereEqualTo("recipient", playerID)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    onFailure(error.message.toString())
                    return@addSnapshotListener
                }

                if (value != null) {
                    val newChallenges = value.documents.associate { doc ->
                        doc.id to doc.toObject(Challenge::class.java)!!
                    }
                    onSuccess(newChallenges)
                } else {
                    onSuccess(emptyMap())
                }
            }
    }

    fun stopScanningForChallenge()
    {
        listenerRegistration?.remove()
        listenerRegistration = null
    }

    //TODO: Implement transactions?
    //Accept challenge with challengeID,
    //for player with playerID
    fun acceptChallenge(challengeID : String, onSuccess : () -> Unit = {}, onFailure : (String) -> Unit = {}) {
        database.collection("challenges").document(challengeID)
            .update("status", "accepted")
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("GameModelError (acceptChallenge)", exception.toString())
                onFailure(exception.toString())
            }
    }

    //Decline an challenge with challengeID,
    //for player with playerID
    fun declineChallenge(challengeID : String, onSuccess : () -> Unit = {}, onFailure : (String) -> Unit = {}) {
        database.collection("challenges").document(challengeID)
            .update("status", "accepted")
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                Log.e("GameModelError (acceptChallenge)", exception.toString())
                onFailure(exception.toString())
            }
    }
}