package ru.bepis.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_choose_music.*
import ru.bepis.R
import ru.bepis.utils.Store

class MusicSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_music)

        headerToolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })
    }

    fun onTrackClicked(view: View) {
        Store.musicIsPicked = true
        finish()
    }
}
