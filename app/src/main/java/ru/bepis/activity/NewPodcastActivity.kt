package ru.bepis.activity

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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

//                fundView.image = bmp

                val name = getFileName(uri!!)
                trackName.text = name
                pickedTrackLayout.visibility = View.VISIBLE
                TrackForm.visibility = View.GONE
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.getScheme().equals("content")) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor!!.close()
            }
        }
        if (result == null) {
            result = uri.getPath()
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
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

    fun onPodcastCreateButtonClicked(view: View) {
        val intent = Intent(this, ViewPodcastActivity::class.java)
        startActivity(intent)
    }
}