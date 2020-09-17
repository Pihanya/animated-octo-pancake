package ru.bepis.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ru.bepis.R

class ViewPodcastActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_podcast)
    }

    fun onPodcastCreateButtonClicked(view: View) {
        val intent = Intent(this, NewPodcastActivity::class.java)
        startActivity(intent)
    }
}
