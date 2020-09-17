package ru.bepis.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_view_podcast.*
import kotlinx.android.synthetic.main.activity_view_podcast.headerToolbar
import ru.bepis.R
import ru.bepis.utils.Store

class ViewPodcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_podcast)
        if (Store.image != null)
            imageView.setImageBitmap(Store.image)

        headerToolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })
        titleP.text = Store.name
        descriptionText.text = Store.description


        duration.text = "Длительность: ${Store.duration}"

        for (timeCodeModel in Store.timeCodes) {
            val timecode = LinearLayout(this);
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.topMargin = 10
            params.leftMargin = 5
            timecode.layoutParams = params

            val time = TextView(this)
            time.textSize = 15.toFloat()
            time.text = timeCodeModel.split(" - ")[0]
            time.setTextColor(resources.getColor(R.color.colorAccent))
            val description = TextView(this)
            description.textSize = 15.toFloat()
            description.text = "  -  ${timeCodeModel.split(" - ")[1]}"
            timecode.addView(time)
            timecode.addView(description)

            timecodesLayout.addView(timecode)
        }

    }

    fun onPodcastCreateButtonClicked(view: View) {
        val intent = Intent(this, EndActivity::class.java)
        startActivity(intent)
    }
}
