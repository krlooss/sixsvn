package com.sixseven.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sixseven.app.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var soundPool: SoundPool
    private lateinit var scoreManager: ScoreManager
    private lateinit var leaderboardManager: LeaderboardManager

    private var soundUp: Int = 0
    private var soundDown: Int = 0

    private var lastAcceleration: Float = 0f
    private var isMovingUp = false
    private var hasMovedUp = false
    private var currentScore = 0
    private var isProcessing = false

    private val THRESHOLD = 1.5f
    private val COOLDOWN_MS = 200L
    private var lastTriggerTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        initializeSensors()
        initializeSounds()
        initializeManagers()
        setupUI()

        showTutorialIfFirstLaunch()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                showTutorial()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
    }

    private fun initializeSounds() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(attributes)
            .build()

        soundUp = soundPool.load(this, R.raw.sound_up, 1)
        soundDown = soundPool.load(this, R.raw.sound_down, 1)
    }

    private fun initializeManagers() {
        scoreManager = ScoreManager(this)
        leaderboardManager = LeaderboardManager()
        currentScore = 0
        scoreManager.saveScore(0)
    }

    private fun setupUI() {
        updateScoreDisplay()
        loadLeaderboard()

        binding.resetButton.setOnClickListener {
            scoreManager.resetScore()
            currentScore = 0
            updateScoreDisplay()
        }

        binding.submitScoreButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            if (username.isNotEmpty()) {
                leaderboardManager.submitScore(username, currentScore) { success ->
                    runOnUiThread {
                        if (success) {
                            android.widget.Toast.makeText(this, "Score submitted!", android.widget.Toast.LENGTH_SHORT).show()
                            binding.usernameInput.text.clear()
                            currentScore = 0
                            scoreManager.saveScore(0)
                            updateScoreDisplay()
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                loadLeaderboard()
                            }, 1000)
                        } else {
                            android.widget.Toast.makeText(this, "Failed to submit score. Check internet connection.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun showTutorialIfFirstLaunch() {
        val prefs = getSharedPreferences("SixSevenPrefs", Context.MODE_PRIVATE)
        val hasSeenTutorial = prefs.getBoolean("has_seen_tutorial", false)

        if (!hasSeenTutorial) {
            showTutorial()
            prefs.edit().putBoolean("has_seen_tutorial", true).apply()
        }
    }

    private fun showTutorial() {
        AlertDialog.Builder(this)
            .setTitle("How to Play Six Seven")
            .setMessage(
                """
                Welcome to Six Seven!

                📱 How to Play:
                1. Hold your phone in any orientation
                2. Move it UP quickly (you'll hear a sound)
                3. Move it DOWN quickly (you'll hear another sound)
                4. Each complete UP-DOWN cycle = 1 point!

                🎯 Features:
                • Your score is saved automatically
                • Submit your score to the global leaderboard
                • Compete with Six Seveners worldwide

                Tap the ? icon anytime to see these instructions again.

                Let's go!
                """.trimIndent()
            )
            .setPositiveButton("Got it!") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = kotlin.math.sqrt(x * x + y * y + z * z)

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTriggerTime < COOLDOWN_MS) {
                lastAcceleration = acceleration
                return
            }

            val deltaAcceleration = acceleration - lastAcceleration

            if (abs(deltaAcceleration) > THRESHOLD && !isProcessing) {
                if (deltaAcceleration > 0 && !isMovingUp) {
                    isMovingUp = true
                    hasMovedUp = true
                    playSound(soundUp)
                    lastTriggerTime = currentTime
                } else if (deltaAcceleration < 0 && isMovingUp && hasMovedUp) {
                    isMovingUp = false
                    hasMovedUp = false
                    playSound(soundDown)
                    incrementScore()
                    lastTriggerTime = currentTime
                }
            }

            lastAcceleration = acceleration
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun incrementScore() {
        currentScore++
        scoreManager.saveScore(currentScore)
        updateScoreDisplay()

        if (currentScore % 10 == 0) {
            binding.scoreText.animate()
                .scaleX(1.5f)
                .scaleY(1.5f)
                .setDuration(150)
                .withEndAction {
                    binding.scoreText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start()
                }
                .start()
        }
    }

    private fun updateScoreDisplay() {
        binding.scoreText.text = "Score: $currentScore"
        binding.bestScoreText.text = "Best: ${scoreManager.getBestScore()}"
    }

    private fun loadLeaderboard() {
        leaderboardManager.getTopScores { scores ->
            runOnUiThread {
                val leaderboardText = scores.mapIndexed { index, entry ->
                    "${index + 1}. ${entry.username}: ${entry.score}"
                }.joinToString("\n")
                binding.leaderboardText.text = if (leaderboardText.isEmpty()) {
                    "No scores yet"
                } else {
                    leaderboardText
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        loadLeaderboard()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }
}
