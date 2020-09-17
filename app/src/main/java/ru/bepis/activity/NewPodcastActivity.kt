package ru.bepis.activity

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_podcast.*
import ru.bepis.R
import java.io.BufferedInputStream
import java.io.InputStream

class NewPodcastActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_podcast)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 0 && resultCode == Activity.RESULT_OK) {
            if (data!!.data != null) {
                val uri = data!!.data
                val inputStream: InputStream = contentResolver.openInputStream(uri!!)!!
                val bufferedInputStream = BufferedInputStream(inputStream);
                val bmp = BitmapFactory.decodeStream(bufferedInputStream);

//                fundView.image = bmp

                imageView.setImageBitmap(bmp)
                loadedImageLayout.visibility = View.VISIBLE
                iconViewForm.visibility = View.GONE
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
        if (requestCode === 1 && resultCode == Activity.RESULT_OK) {
            if (data!!.data != null) {
                val uri = data!!.data
                val inputStream: InputStream = contentResolver.openInputStream(uri!!)!!
                val bufferedInputStream = BufferedInputStream(inputStream);
                val bmp = BitmapFactory.decodeStream(bufferedInputStream);

//                fundView.image = bmp

                imageView.setImageBitmap(bmp)
                loadedImageLayout.visibility = View.VISIBLE
                iconViewForm.visibility = View.GONE
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    fun onPickImageClick(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0)
    }

    fun onRemoveImageClick(view: View) {
        loadedImageLayout.visibility = View.GONE
        iconViewForm.visibility = View.VISIBLE
    }

    fun onPickAudioClick(view: View) {
        val intent = Intent()
        intent.type = "audio/mpeg"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), 1)
    }
}