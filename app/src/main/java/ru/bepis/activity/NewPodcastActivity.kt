package ru.bepis.activity

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_podcast.*
import ru.bepis.R
import ru.bepis.utils.Store
import java.io.BufferedInputStream
import java.io.InputStream


class NewPodcastActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_podcast)

        headerToolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        listOf(nameEditText, descriptionEditText)
            .forEach { it.addTextChangedListener(createInfoEditTextWatcher(it)) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === 0 && resultCode == Activity.RESULT_OK) {
            if (data!!.data != null) {
                val uri = data!!.data
                val inputStream: InputStream = contentResolver.openInputStream(uri!!)!!
                val bufferedInputStream = BufferedInputStream(inputStream);
                val bmp = BitmapFactory.decodeStream(bufferedInputStream);

                Store.image = bmp

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
                val durationMillis = getFileDuration(uri)!!

                Store.audioUri = uri
                Store.audioFilename = name

                tryToUnlockButton()
                val minutesInt = durationMillis/1000/60
                val minutes = minutesInt.toString().padStart(2, '0')
                val secondsInt = (durationMillis/1000/60 - minutesInt*60)
                val seconds = (if (secondsInt < 0) 0 else secondsInt).toString().padStart(2, '0')
                val durationStr = "${minutes}:${seconds} -> ${durationMillis}"
                Store.duration = durationStr

                durationText.text = durationStr
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

    fun getFileDuration(uri: Uri): Int? {
        val mp =
            MediaPlayer.create(this, uri)
        return mp.duration
    }

    fun onPickImageClick(view: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0)
    }

    fun onRemoveImageClick(view: View) {
        Store.audioUri = null
        tryToUnlockButton()
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

    fun onEditAudioButtonClicked(view: View) {
        val intent = Intent(this, EditActivity::class.java)
        startActivity(intent)
    }

    fun onInfoEditTextChanged(editText: EditText, textBefore: String?) {
        val text = editText.text?.toString()
        if (text.isNullOrEmpty()) {
            return
        }

        if (text.isBlank()) {
            editText.setText("")
            return
        }

        if (text == textBefore) {
            return
        }

        when (editText.id) {
            R.id.nameEditText -> Store.name = text
            R.id.descriptionEditText -> Store.description = text
        }

        run {
            tryToUnlockButton()
        }
    }

    fun onEditPodcastButtonClick(view: View) {
        val intent = Intent(this, EditPodcastActivity::class.java)
        startActivity(intent)
    }

    private fun tryToUnlockButton() {
        val nameFilled = !Store.name.isNullOrBlank()
        val descriptionFilled = !Store.description.isNullOrBlank()
        val trackFilled = Store.audioUri != null

        buttonNext.isEnabled = nameFilled && descriptionFilled && trackFilled
    }

    private fun createInfoEditTextWatcher(editText: EditText) = object : TextWatcher {
        var textBefore: String? = null

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int, count: Int, after: Int
        ) {
            textBefore = s?.toString()
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onInfoEditTextChanged(editText, textBefore)
        }

        override fun afterTextChanged(s: Editable) {
        }
    }
}