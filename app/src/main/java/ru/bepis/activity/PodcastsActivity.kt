package ru.bepis.activity

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import ru.bepis.R

class PodcastsActivity : AppCompatActivity() {

    private val REQUEST_ID_PERMISSIONS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        requestStoragePermission()
    }

    fun onPodcastCreateButtonClicked(view: View) {
        val intent = Intent(this, NewPodcastActivity::class.java)
        startActivity(intent)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this@PodcastsActivity, arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_ID_PERMISSIONS
        )
    }
}
