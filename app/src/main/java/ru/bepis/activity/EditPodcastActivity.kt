package ru.bepis.activity

import android.media.MediaPlayer
import android.media.VolumeShaper
import android.os.Bundle
import android.view.GestureDetector
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.bepis.R
import ru.bepis.audio.SoundFile
import ru.bepis.utils.Store
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class EditPodcastActivity : AppCompatActivity() {

    private var hasInitialized: Boolean = false
    private var mSoundFile: SoundFile? = null

    private lateinit var player: MediaPlayer

    private var currentFadeIn: VolumeShaper? = null
    private var currentFadeOut: VolumeShaper? = null

    private var leftBorderMillis: Int = 0
    private var rightBorderMillis: Int = 0
    private var currentPositionMillis: Int? = 0

    private var isPlaying = false
    private var isFadeIn = false
    private var isFadeOut = false


    // TODO
    private lateinit var mGestureDetector: GestureDetector
    private lateinit var mScaleGestureDetector: ScaleGestureDetector

    private lateinit var mLenByZoomLevel: IntArray
    private lateinit var mValuesByZoomLevel: Array<DoubleArray?>
    private lateinit var mZoomFactorByZoomLevel: DoubleArray
    private lateinit var mHeightsAtThisZoomLevel: IntArray

    private var mZoomLevel = 0
    private var mNumZoomLevels = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_podcast)

        player = MediaPlayer.create(this, Store.audioUri)
        rightBorderMillis = player.duration

        player.setOnCompletionListener {
            currentPositionMillis = leftBorderMillis

            currentFadeIn?.close()
            currentFadeIn = null

            currentFadeOut?.close()
            currentFadeOut = null

            isPlaying = false
        }
    }

    fun fadeOutConfig(duration: Long): VolumeShaper.Configuration {
        val times = floatArrayOf(0f, 1f) // can add more points, volume points must correspond to time points
        val volumes = floatArrayOf(1f, 0f)
        return VolumeShaper.Configuration.Builder()
            .setDuration(duration)
            .setCurve(times, volumes)
            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_CUBIC)
            .build()
    }

    fun fadeInConfig(duration: Long): VolumeShaper.Configuration {
        val times = floatArrayOf(0f, 1f) // can add more points, volume points must correspond to time points
        val volumes = floatArrayOf(0f, 1f)
        return VolumeShaper.Configuration.Builder()
            .setDuration(duration)
            .setCurve(times, volumes)
            .setInterpolatorType(VolumeShaper.Configuration.INTERPOLATOR_TYPE_CUBIC)
            .build()
    }

    fun onPlayStopButtonClick(view: View) {
        if (isPlaying) {
            player.pause()
            currentPositionMillis = player.currentPosition
        } else { // not playing
            currentPositionMillis =
                if (currentPositionMillis == null) leftBorderMillis
                else currentPositionMillis

            currentPositionMillis!!.also { currentPositionMillis ->
                player.seekTo(currentPositionMillis)

                if (isFadeIn && currentPositionMillis - 1L <= leftBorderMillis) {
                    currentFadeIn = player.createVolumeShaper(fadeInConfig(5L)).also { it.apply(VolumeShaper.Operation.PLAY) }
                    Executors.newSingleThreadScheduledExecutor().schedule({ currentFadeIn?.close() }, 2L, TimeUnit.SECONDS)
                } else {
                    currentFadeIn?.close()
                    currentFadeIn = null
                }

                if(isFadeOut) {
                    currentFadeOut = player.createVolumeShaper(fadeOutConfig(5L)).also { it.apply(VolumeShaper.Operation.PLAY) }
                } else {
                    currentFadeOut?.close()
                    currentFadeOut = null
                }

                player.start()
            }
        }
        isPlaying = !isPlaying
    }

    fun onRollbackButtonClick(view: View) {

    }

    fun onCutButtonClick(view: View) {

    }

    fun onFadeInButtonClick(view: View) {
        if (isFadeIn) {

        } else {

        }

        isFadeIn = !isFadeIn
    }

    fun onFadeOutButtonClick(view: View) {
        if (isFadeOut) {

        } else {

        }

        isFadeOut = !isFadeOut
    }

    /*override fun onResume() {
        super.onResume()

        val barWidth = appBarLayout.width
        val barHeight = appBarLayout.height
        for(i in 0..72) {
            val layoutWidth = 720
            val layoutHeight = 122

            val volume = (i % 9)
            val volumeWidth = (layoutWidth / 72) - 3
            val volumeHeight = (volume + 1) * (layoutHeight / 8)

            val imageView = ImageView(applicationContext)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.layoutParams = LinearLayout.LayoutParams(volumeWidth, volumeHeight, 1.0F)
//            imageView.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0F)
            val imageId = when(volume) {
                0, 1 -> R.drawable.volume_1
                2 -> R.drawable.volume_2
                3 -> R.drawable.volume_3
                4 -> R.drawable.volume_4
                5 -> R.drawable.volume_5
                6 -> R.drawable.volume_6
                7 -> R.drawable.volume_7
                8 -> R.drawable.volume_8
                else -> throw IllegalStateException()
            }
            imageView.setImageResource(imageId)
            waveformLayout.addView(imageView)
        }
    }*/

    private fun computeDoublesForAllZoomLevels() {
        val numFrames = mSoundFile!!.numFrames
        val frameGains = mSoundFile!!.frameGains
        val smoothedGains = DoubleArray(numFrames)
        when {
            numFrames == 1 -> {
                smoothedGains[0] = frameGains[0].toDouble()
            }
            numFrames == 2 -> {
                smoothedGains[0] = frameGains[0].toDouble()
                smoothedGains[1] = frameGains[1].toDouble()
            }
            numFrames > 2 -> {
                smoothedGains[0] = (frameGains[0] / 2.0 + frameGains[1] / 2.0)
                for (i in 1 until numFrames - 1) {
                    smoothedGains[i] = (frameGains[i - 1] / 3.0 + frameGains[i] / 3.0 + frameGains[i + 1] / 3.0)
                }
                smoothedGains[numFrames - 1] = (frameGains[numFrames - 2] / 2.0 + frameGains[numFrames - 1] / 2.0)
            }
        }

        // Make sure the range is no more than 0 - 255
        var maxGain = 1.0
        for (i in 0 until numFrames) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i]
            }
        }
        var scaleFactor = 1.0
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain
        }

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0.0
        val gainHist = IntArray(256)
        for (i in 0 until numFrames) {
            var smoothedGain = (smoothedGains[i] * scaleFactor).toInt()
            if (smoothedGain < 0) smoothedGain = 0
            if (smoothedGain > 255) smoothedGain = 255
            if (smoothedGain > maxGain) maxGain = smoothedGain.toDouble()
            gainHist[smoothedGain]++
        }

        // Re-calibrate the min to be 5%
        var minGain = 0.0
        var sum = 0
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[minGain.toInt()]
            minGain++
        }

        // Re-calibrate the max to be 99%
        sum = 0
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[maxGain.toInt()]
            maxGain--
        }

        // Compute the heights
        val heights = DoubleArray(numFrames)
        val range = maxGain - minGain
        for (i in 0 until numFrames) {
            var value = (smoothedGains[i] * scaleFactor - minGain) / range
            if (value < 0.0) value = 0.0
            if (value > 1.0) value = 1.0
            heights[i] = value * value
        }

        mNumZoomLevels = 5
        mLenByZoomLevel = IntArray(5)
        mZoomFactorByZoomLevel = DoubleArray(5)
        mValuesByZoomLevel = arrayOfNulls<DoubleArray>(5)

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel[0] = numFrames * 2
        mZoomFactorByZoomLevel[0] = 2.0
        mValuesByZoomLevel[0] = DoubleArray(mLenByZoomLevel[0]).also { valuesAtZoom ->
            if (numFrames > 0) {
                valuesAtZoom[0] = 0.5 * heights[0]
                valuesAtZoom[1] = heights[0]
            }
            for (i in 1 until numFrames) {
                valuesAtZoom[2 * i] = 0.5 * (heights[i - 1] + heights[i])
                valuesAtZoom[2 * i + 1] = heights[i]
            }
        }

        // Level 1 is normal
        mLenByZoomLevel[1] = numFrames
        mValuesByZoomLevel[1] = DoubleArray(mLenByZoomLevel[1]).also { valuesAtZoom ->
            mZoomFactorByZoomLevel[1] = 1.0
            for (i in 0 until mLenByZoomLevel[1]) {
                valuesAtZoom[i] = heights[i]
            }
        }

        // 3 more levels are each halved
        for (j in 2..4) {
            mLenByZoomLevel[j] = mLenByZoomLevel[j - 1] / 2
            mValuesByZoomLevel[j] = DoubleArray(mLenByZoomLevel[j])
            mZoomFactorByZoomLevel[j] = mZoomFactorByZoomLevel[j - 1] / 2.0
            for (i in 0 until mLenByZoomLevel[j]) {
                mValuesByZoomLevel[j]!![i] = 0.5 * (mValuesByZoomLevel[j - 1]!![2 * i] +
                        mValuesByZoomLevel[j - 1]!![2 * i + 1])
            }
        }

        mZoomLevel = when {
            numFrames > 5000 -> 3
            numFrames > 1000 -> 2
            numFrames > 300 -> 1
            else -> 0
        }
        hasInitialized = true
    }
}