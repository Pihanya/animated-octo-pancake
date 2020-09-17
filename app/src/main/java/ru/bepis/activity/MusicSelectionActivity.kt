package ru.bepis.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import ru.bepis.R
import ru.bepis.utils.Store

class MusicSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_music)
    }

    fun onTrackClicked(view: View) {
        Store.musicIsPicked = true

        finish()
    }
}
