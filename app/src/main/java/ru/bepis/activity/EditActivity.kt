package ru.bepis.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_edit_podcast.*
import kotlinx.android.synthetic.main.activity_view_podcast.*
import ru.bepis.R

class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_podcast)
    }

    fun onMusicButtonClicked(view: View) {
        val intent = Intent(this, MusicSelectionActivity::class.java)
        startActivity(intent)
    }


    fun onNewTimeCodeClicked(view: View) {
        val layout =
            layoutInflater.inflate(R.layout.timecode_input_row, null, false) as LinearLayout

        timecodesContainer.addView(layout)
    }
}
