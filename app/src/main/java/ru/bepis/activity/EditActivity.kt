package ru.bepis.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_podcast.*
import ru.bepis.R

class EditActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_podcast)
        headerToolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })
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
