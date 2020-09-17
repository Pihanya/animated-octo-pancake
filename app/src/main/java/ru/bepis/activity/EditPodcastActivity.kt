package ru.bepis.activity

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.GestureDetector
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.masoudss.lib.SeekBarOnProgressChanged
import com.masoudss.lib.WaveformSeekBar
import kotlinx.android.synthetic.main.activity_choose_music.*
import kotlinx.android.synthetic.main.activity_edit_podcast.*
import kotlinx.android.synthetic.main.activity_edit_podcast.headerToolbar
import ru.bepis.R
import ru.bepis.audio.SoundFile
import ru.bepis.utils.Store
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit


class EditPodcastActivity : AppCompatActivity() {

    private val times =
        floatArrayOf(0f, 0.25f, 0.5f, 0.75f, 1f) // can add more points, volume points must correspond to time points

    private val samples = intArrayOf(
        50, 60, 70, 100, 125, 85, 75, 80, 235, 220, 150, 120, 90, 60, 30,
        50, 60, 70, 100, 125, 85, 75, 80, 235, 220, 150, 120, 90, 60, 30,
        50, 60, 70, 100, 125, 85, 75, 80, 235, 220, 150, 120, 90, 60, 30,
        50, 60, 70, 100, 125, 85, 75, 80, 235, 220, 150, 120, 90, 60, 30,
        50, 60, 70, 100, 125, 85, 75, 80, 235, 220, 150, 120, 90, 60, 30,
        50, 60
    )

    private val progressSteps = 100;

    private val fadeInBegin = intArrayOf(0, 15, 35, 75)
    private val fadeOutEnd = intArrayOf(50, 25, 35, 20, 0)

    private fun IntArray.setFadeIn(): IntArray {
        val list = this.toMutableList()
        list[0] = fadeInBegin[0]
        list[1] = fadeInBegin[1]
        list[2] = fadeInBegin[2]
        list[3] = fadeInBegin[3]
        return list.toIntArray()
    }

    private fun IntArray.setFadeOut(): IntArray {
        val list = this.toMutableList()
        list[list.size - 4] = fadeOutEnd[0]
        list[list.size - 3] = fadeOutEnd[1]
        list[list.size - 2] = fadeOutEnd[2]
        list[list.size - 1] = fadeOutEnd[3]
        return list.toIntArray()
    }

    private var hasInitialized: Boolean = false
    private var mSoundFile: SoundFile? = null

    private lateinit var player: MediaPlayer

    private var leftBorderMillis: Int = 0
    private var rightBorderMillis: Int = 0
    private var currentPositionMillis: Int? = 0

    private var isPlaying = false
    private var isFadeIn = false
    private var isFadeOut = false

    // TODO
    private lateinit var mGestureDetector: GestureDetector
    private lateinit var mScaleGestureDetector: ScaleGestureDetector

    private var mZoomLevel = 0
    private var mNumZoomLevels = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_podcast)

        if (Store.musicIsPicked) {
            toggleButtonVisuals("music", musicButton, true);
        } else {
            toggleButtonVisuals("music", musicButton, false);
        }

        player = MediaPlayer.create(this, Store.audioUri)
        rightBorderMillis = player.duration

        headerToolbar.setNavigationOnClickListener(View.OnClickListener {
            val childCount = timecodesContainer.getChildCount();
            Store.timeCodes.clear()
            for (i in 0..(childCount-1)) {
                val v = timecodesContainer.getChildAt(i) as LinearLayout
                val nameEventView = v.getChildAt(0) as EditText
                val timeEventView = v.getChildAt(1) as EditText

                Store.timeCodes.add("${timeEventView.text} - ${nameEventView.text}")
            }

            finish()
        })

        player.setOnCompletionListener {
            currentPositionMillis = leftBorderMillis
            isPlaying = false
            toggleButtonVisuals("play", playStopButton, false);
            waveformSeekBar.progress = 0
        }

        waveformSeekBar.sample = samples
        waveformSeekBar.onProgressChanged = object : SeekBarOnProgressChanged {
            override fun onProgressChanged(waveformSeekBar: WaveformSeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    var selectedTime = (player.duration.toFloat() * (progress.toFloat() / progressSteps)).toInt();
                    currentPositionMillis = selectedTime;
                    player.seekTo(selectedTime);
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Store.musicIsPicked) {
            toggleButtonVisuals("music", musicButton, true);
        } else {
            toggleButtonVisuals("music", musicButton, false);
        }
    }
    fun onPlayStopButtonClick(view: View) {
        if(player.currentPosition >= rightBorderMillis) {
            isPlaying = false
            toggleButtonVisuals("play", playStopButton, false);
            currentPositionMillis = 0
            updateWaveFormProgress(0);
            waveformSeekBar.progress = 0
        }

        if (isPlaying) {
            player.pause()
            currentPositionMillis = player.currentPosition
            progressFuture?.cancel(true)
        } else { // not playing
            currentPositionMillis =
                if (currentPositionMillis == null && player.currentPosition > 0) player.currentPosition
                else if (currentPositionMillis == null) leftBorderMillis
                else currentPositionMillis

            startWork(currentPositionMillis!!)

            currentPositionMillis!!.also { currentPositionMillis ->
                player.seekTo(currentPositionMillis)

                if (isFadeIn && currentPositionMillis - 1L <= leftBorderMillis) {
                    val periods = 50L
                    val duration = 3L

                    var volume = 0.0f.also { player.setVolume(it, it) }
                    val future = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                        volume += 1.0f / periods
                        player.setVolume(volume, volume)
                    }, (duration * 1000) / periods, (duration * 1000) / periods, TimeUnit.MILLISECONDS)
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        future.cancel(true)
                        player.setVolume(1f, 1f)
                    }, duration, TimeUnit.SECONDS)

                    /*val fadeInTimeout = 5L
                    currentFadeIn = player.createVolumeShaper(fadeInConfig(fadeInTimeout)).also { it.apply(VolumeShaper.Operation.PLAY) }
                    Executors.newSingleThreadScheduledExecutor().schedule({ currentFadeIn?.close() }, fadeInTimeout, TimeUnit.SECONDS)*/
                }

                if(isFadeOut) {
                    val periods = 50L
                    val duration = 3L

                    var volume = 1.0f.also { player.setVolume(it, it) }
                    val future = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
                        volume -= 1.0f / 20
                        player.setVolume(volume, volume)
                    }, (duration * 1000) / periods, (duration * 1000) / periods, TimeUnit.MILLISECONDS)
                    Executors.newSingleThreadScheduledExecutor().schedule({
                        future.cancel(true)
                        player.setVolume(.0f, .0f)
                    }, duration, TimeUnit.SECONDS)

                    /*val fadeOutTimeout = 5L
                    currentFadeOut = player.createVolumeShaper(fadeOutConfig(fadeOutTimeout)).also { it.apply(VolumeShaper.Operation.PLAY) }*/
                }

                player.start()
            }
        }
        isPlaying = !isPlaying
        toggleButtonVisuals("play", playStopButton, isPlaying);
    }

    private var progressFuture: ScheduledFuture<*>? = null
    fun startWork(from: Int) {
        if(progressFuture != null) {
            progressFuture!!.cancel(true)
            progressFuture = null
            updateWaveFormProgress()
        }

        val timeLeft = (player.duration - player.currentPosition)
        progressFuture = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            updateWaveFormProgress()
            runOnUiThread { updateWaveFormProgress() }
        }, 250, 250, TimeUnit.MILLISECONDS)
        Executors.newSingleThreadScheduledExecutor().schedule({
            if(progressFuture != null) {
                progressFuture!!.cancel(true)
                progressFuture = null
            }
            updateWaveFormProgress(progressSteps)
        }, timeLeft.toLong(), TimeUnit.MILLISECONDS)
    }

    fun updateWaveFormProgress(value: Int? = null) {
        if (value!= null) {
            waveformSeekBar.progress = value;
        } else if (player.duration > 0)  {
            waveformSeekBar.progress = progressSteps * player.currentPosition/player.duration;
        } else {
            waveformSeekBar.progress = 0;
        }
    }

    fun onRollbackButtonClick(view: View) {

    }

    fun onMusicButtonClick(view: View) {
        val intent = Intent(this, MusicSelectionActivity::class.java)
        startActivity(intent)
    }

    fun onNewTimeCodeClick(view: View) {
        val layout = layoutInflater.inflate(R.layout.timecode_input_row, null, false) as LinearLayout
        timecodesContainer.addView(layout)
    }

    fun onRemoveTimeCodeClick(view: View) {
        val parent = view.parent
        timecodesContainer.removeView(parent as View)
    }


    fun onCutButtonClick(view: View) {

    }

    fun onFadeInButtonClick(view: View) {
        isFadeIn = !isFadeIn
        toggleButtonVisuals("bar_chart_1", fadeInButton, isFadeIn);
        waveformSeekBar.sample = samples.let {
            var arr = it
            if (isFadeIn) {
                arr = arr.setFadeIn()
            }
            if (isFadeOut) {
                arr = arr.setFadeOut()
            }
            arr
        }
    }

    fun onFadeOutButtonClick(view: View) {
        isFadeOut = !isFadeOut
        toggleButtonVisuals("bar_chart_2", fadeOutButton, isFadeOut);
        waveformSeekBar.sample = samples.let {
            var arr = it
            if (isFadeIn) {
                arr = arr.setFadeIn()
            }
            if (isFadeOut) {
                arr = arr.setFadeOut()
            }
            arr
        }
    }

    fun toggleButtonVisuals(base: String, button: ImageView, active: Boolean) {
        if (active) {
            var iconId = getResources().getIdentifier(base + "_active", "drawable", getPackageName());
            var icon = getResources().getDrawable(iconId);
            button.setBackgroundResource(R.drawable.btn_icon_primary_bg);
            button.setImageDrawable(icon);
        } else {
            var iconId = getResources().getIdentifier(base, "drawable", getPackageName());
            var icon = getResources().getDrawable(iconId);
            button.setBackgroundResource(R.drawable.btn_icon_light_bg);
            button.setImageDrawable(icon);
        }
    }
}
