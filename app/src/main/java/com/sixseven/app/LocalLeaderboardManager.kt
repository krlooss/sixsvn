package com.sixseven.app

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class LocalLeaderboardEntry(
    val username: String,
    val score: Int,
    val timestamp: Long
)

class LocalLeaderboardManager(private val context: Context) {

    private val prefs = context.getSharedPreferences("LocalLeaderboard", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val SCORES_KEY = "local_scores"

    fun submitScore(username: String, score: Int) {
        val entry = LocalLeaderboardEntry(
            username = username,
            score = score,
            timestamp = System.currentTimeMillis()
        )

        val scores = getScores().toMutableList()
        scores.add(entry)
        saveScores(scores)
    }

    fun getTopScores(limit: Int = 10): List<LocalLeaderboardEntry> {
        return getScores()
            .sortedByDescending { it.score }
            .take(limit)
    }

    private fun getScores(): List<LocalLeaderboardEntry> {
        val json = prefs.getString(SCORES_KEY, null) ?: return emptyList()
        val type = object : TypeToken<List<LocalLeaderboardEntry>>() {}.type
        return gson.fromJson(json, type)
    }

    private fun saveScores(scores: List<LocalLeaderboardEntry>) {
        val json = gson.toJson(scores)
        prefs.edit().putString(SCORES_KEY, json).apply()
    }

    fun clearScores() {
        prefs.edit().remove(SCORES_KEY).apply()
    }
}
