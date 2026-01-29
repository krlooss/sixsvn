package com.sixseven.app

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

data class LeaderboardEntry(
    val username: String = "",
    val score: Int = 0,
    val timestamp: Long = 0
)

class LeaderboardManager {

    private val database = FirebaseDatabase.getInstance("https://sixsvn-6e1aa-default-rtdb.europe-west1.firebasedatabase.app/")
    private val leaderboardRef = database.getReference("leaderboard")

    fun submitScore(username: String, score: Int, onComplete: (Boolean) -> Unit) {
        val entry = LeaderboardEntry(
            username = username,
            score = score,
            timestamp = System.currentTimeMillis()
        )

        val key = UUID.randomUUID().toString()
        leaderboardRef.child(key).setValue(entry)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun getTopScores(limit: Int = 10, callback: (List<LeaderboardEntry>) -> Unit) {
        leaderboardRef.orderByChild("score")
            .limitToLast(limit)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val scores = mutableListOf<LeaderboardEntry>()
                    android.util.Log.d("LeaderboardManager", "Snapshot exists: ${snapshot.exists()}, children count: ${snapshot.childrenCount}")
                    for (child in snapshot.children) {
                        child.getValue(LeaderboardEntry::class.java)?.let {
                            android.util.Log.d("LeaderboardManager", "Loaded entry: ${it.username} - ${it.score}")
                            scores.add(it)
                        }
                    }
                    callback(scores.sortedByDescending { it.score })
                }

                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("LeaderboardManager", "Error loading scores: ${error.message}")
                    callback(emptyList())
                }
            })
    }
}
