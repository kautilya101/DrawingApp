 package com.example.drawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.OnColorSelectedListener
import com.flask.colorpicker.builder.ColorPickerClickListener
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.brushdialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


 class MainActivity : AppCompatActivity() {


    val gallerypick = registerForActivityResult(ActivityResultContracts.GetContent(), ActivityResultCallback {
        bg_img.visibility = View.VISIBLE
        bg_img.setImageURI(it!!)
    })


    private var ImageCurrentPostion :ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        draw_view.setSizeForBrush(10.toFloat())

        ImageCurrentPostion = pallet[7] as ImageButton
        ImageCurrentPostion!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_selected)
        )


        brushbtn.setOnClickListener { chooseSize() }

        Image_selector.setOnClickListener {
            if (isReadStorageAllowed()){
                gallerypick.launch("image/*")
            }
            else{
                requestpermission()
            }
        }

        undo.setOnClickListener {
            draw_view.undomove()
        }

        saveEdit.setOnClickListener {
            if (isReadStorageAllowed()){
                BitmapAsync(getBitmapfromview(frame_background)).execute() // here i forgot to write execute last time  :|
            }
            else{
                requestpermission()
            }
        }

        colorpicker.setOnClickListener {
            val colorpick = ColorPickerDialogBuilder.with(this@MainActivity)
                    .setTitle("Choose Color")
                    .initialColor(Color.BLACK)
                    .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                    .density(8)
                    .setOnColorSelectedListener(object : OnColorSelectedListener {
                        override fun onColorSelected(selectedColor: Int) {
                            Toast.makeText(this@MainActivity, " $selectedColor", Toast.LENGTH_SHORT).show()
                        }
                    })
                    colorpick.setPositiveButton("OK",object :ColorPickerClickListener{
                        override fun onClick(d: DialogInterface?, selectedcolor: Int, allColors: Array<out Int>?) {
                            draw_view.setnewColor(selectedcolor)
                        }
                    })

                    colorpick.setNegativeButton("Cancel",object :DialogInterface.OnClickListener{
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                        }
                    })
                    colorpick.build()
                            .show()

        }

    }
     

    fun paintClicked(view: View){
        if(view != ImageCurrentPostion) {
            var imageButton = view as ImageButton

            var colorTag = imageButton.tag.toString()
            draw_view.setColor(colorTag)
            imageButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_selected)
            )
            ImageCurrentPostion!!.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            ImageCurrentPostion = view
        }
    }


//    Funtion to display brush size dialog and their click listeners
    private fun chooseSize(){
        val brushdialog = Dialog(this)
    /**
     * Set the screen content from a layout resource.  The resource will be
     * inflated, adding all top-level views to the screen.
     *
     * @param layoutResID Resource ID to be inflated.
     */
        brushdialog.setContentView(R.layout.brushdialog)
        brushdialog.setTitle("Brush Size")
        val smallbtn = brushdialog.small_size
        smallbtn.setOnClickListener {
            draw_view.setSizeForBrush(5.toFloat())
            brushdialog.dismiss()
        }

        val mediumbtn = brushdialog.medium_size
        mediumbtn.setOnClickListener {
            draw_view.setSizeForBrush(10.toFloat())
            brushdialog.dismiss()
        }

        val largebtn = brushdialog.large_size
        largebtn.setOnClickListener {
            draw_view.setSizeForBrush(20.toFloat())
            brushdialog.dismiss()
        }
        brushdialog.show()
    }

    private fun  requestpermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"Need Permission to access Photo",Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this@MainActivity,
                        "Permission granted by the user",
                        Toast.LENGTH_SHORT).show()
            }
            else
            {
                Toast.makeText(this@MainActivity,
                        "Permission denied....abbe dede na saale",
                        Toast.LENGTH_SHORT).show()
            }

        }
    }



    private fun isReadStorageAllowed() :Boolean{
        var results = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return results == PackageManager.PERMISSION_GRANTED
    }



    private fun getBitmapfromview(view:View):Bitmap{
        val SendBitmap  = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(SendBitmap)
        val bgd = view.background

        if (bgd != null){
            bgd.draw(canvas)
        }
        else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)
        return SendBitmap
    }



    private inner class BitmapAsync(val mbitmap: Bitmap): ViewModel()
    {   fun execute() = viewModelScope.launch()
        {
        val result = doInBackground()
        onPostExecute(result)
        }

        private suspend fun doInBackground():String = withContext(Dispatchers.IO){
            var result = ""
            if(mbitmap != null){
                try {
                    val bytes = ByteArrayOutputStream()
                    mbitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f = File(externalCacheDir!!.absolutePath.toString()+
                            File.separator +"KidsDrawing_"+ System.currentTimeMillis()/1000 + ".png")

                    val fos = FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()

                    result = f.absolutePath

                }catch (e:Exception){
                    result = ""
                    e.printStackTrace()
                }
            }
            return@withContext result
        }

        private fun onPostExecute(result: String?) {

            if(!result!!.isEmpty()){
            Toast.makeText(this@MainActivity,
                    "File saved successfully at $result",
                    Toast.LENGTH_SHORT).show()
            }
            else{
            Toast.makeText(this@MainActivity,
                    "Something went wrong!! Please try again",
                    Toast.LENGTH_SHORT).show()
            }

            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path,uri -> val shared = Intent()
                shared.action = Intent.ACTION_SEND
                shared.putExtra(Intent.EXTRA_STREAM,uri)
                shared.type = "image/png"
                startActivity(
                        Intent.createChooser(
                                shared,"Share"
                        )
                )

            }
            }

        }



    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

 }

