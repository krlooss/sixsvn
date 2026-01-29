package com.sixseven.app

import android.content.Context
import android.content.SharedPreferences

class ScoreManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveScore(score: Int) {
        prefs.edit().apply {
            putInt(KEY_CURRENT_SCORE, score)
            val bestScore = getBestScore()
            if (score > bestScore) {
                putInt(KEY_BEST_SCORE, score)
            }
            apply()
        }
    }

    fun getScore(): Int {
        return prefs.getInt(KEY_CURRENT_SCORE, 0)
    }

    fun getBestScore(): Int {
        return prefs.getInt(KEY_BEST_SCORE, 0)
    }

    fun resetScore() {
        prefs.edit().putInt(KEY_CURRENT_SCORE, 0).apply()
    }

    companion object {
        private const val PREFS_NAME = "SixSevenPrefs"
        private const val KEY_CURRENT_SCORE = "current_score"
        private const val KEY_BEST_SCORE = "best_score"
    }
}
