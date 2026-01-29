package com.sixseven.app

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import com.sixseven.app.databinding.ActivityMainBinding
import kotlin.math.abs

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var soundPool: SoundPool
    private lateinit var vibrator: Vibrator
    private lateinit var scoreManager: ScoreManager
    private lateinit var leaderboardManager: LeaderboardManager

    private var soundUp: Int = 0
    private var soundDown: Int = 0

    private var lastZ: Float = 0f
    private var isMovingUp = false
    private var hasMovedUp = false
    private var currentScore = 0
    private var isProcessing = false

    private val THRESHOLD = 3.0f
    private val COOLDOWN_MS = 500L
    private var lastTriggerTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeSensors()
        initializeSounds()
        initializeManagers()
        setupUI()
    }

    private fun initializeSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
        currentScore = scoreManager.getScore()
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
                leaderboardManager.submitScore(username, currentScore)
                binding.usernameInput.text.clear()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val z = event.values[2]

            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTriggerTime < COOLDOWN_MS) {
                return
            }

            val deltaZ = z - lastZ

            if (abs(deltaZ) > THRESHOLD && !isProcessing) {
                if (deltaZ > 0 && !isMovingUp) {
                    isMovingUp = true
                    hasMovedUp = true
                    playSound(soundUp)
                    vibrate(50)
                    lastTriggerTime = currentTime
                } else if (deltaZ < 0 && isMovingUp && hasMovedUp) {
                    isMovingUp = false
                    hasMovedUp = false
                    playSound(soundDown)
                    vibrate(100)
                    incrementScore()
                    lastTriggerTime = currentTime
                }
            }

            lastZ = z
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun playSound(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    private fun vibrate(duration: Long) {
        vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun incrementScore() {
        currentScore++
        scoreManager.saveScore(currentScore)
        updateScoreDisplay()
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
