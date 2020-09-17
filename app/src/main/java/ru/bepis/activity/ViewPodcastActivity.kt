package ru.bepis.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_view_podcast.*
import ru.bepis.R
import ru.bepis.utils.Store

class ViewPodcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_podcast)
        if (Store.image != null)
            imageView.setImageBitmap(Store.image)

        Store.duration = "12:34"
        duration.text = "Длительность: ${Store.duration}"

        val timecode = LinearLayout(this);
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.topMargin = 10
        params.leftMargin = 5
        timecode.layoutParams = params

        val time = TextView(this)
        time.textSize = 15.toFloat()
        time.text = "22:22"
        time.setTextColor(resources.getColor(R.color.colorAccent))
        val description = TextView(this)
        description.textSize = 15.toFloat()
        description.text = "  -  Кормлю енотов"
        timecode.addView(time)
        timecode.addView(description)

        timecodesLayout.addView(timecode)
    }

    fun onPodcastCreateButtonClicked(view: View) {
        val intent = Intent(this, EndActivity::class.java)
        startActivity(intent)
    }
}
