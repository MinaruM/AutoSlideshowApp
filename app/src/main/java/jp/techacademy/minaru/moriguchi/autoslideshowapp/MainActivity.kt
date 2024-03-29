package jp.techacademy.minaru.moriguchi.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 100
    private var mTimer: Timer? = null
    private var mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                }else{
                    showAlertDialog()
                }
        }
    }

    private fun showAlertDialog() {
        // AlertDialog.Builderクラスを使ってAlertDialogの準備をする
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("許可が必要です")

        // 肯定ボタンに表示される文字列、押したときのリスナーを設定する
        alertDialogBuilder.setPositiveButton("OK") { dialog, which ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // パーミッションの許可状態を確認する
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    getContentsInfo()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                }
                // Android 5系以下の場合
            } else {
                getContentsInfo()
            }
        }
        // AlertDialogを作成して表示する
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        )

        if (cursor.moveToFirst()) {
            setImageView(cursor)
        }

        next_button.setOnClickListener {
            if (cursor.moveToNext()){
                setImageView(cursor)
            }else {
                cursor.moveToFirst()
                setImageView(cursor)
            }
        }

        back_button.setOnClickListener {
            if(cursor.moveToPrevious()){
                setImageView(cursor)
            }else{
                cursor.moveToLast()
                setImageView(cursor)
            }
        }

        start_button.setOnClickListener{
            back_button.setEnabled(false)
            next_button.setEnabled(false)
            start_button.text="停止"
            if (mTimer ==null) {
                mTimer = Timer()
                mTimer!!.schedule(object: TimerTask(){
                    override fun run() {
                        mHandler.post{
                            if (cursor.moveToNext()){
                                setImageView(cursor)
                            }else {
                                cursor.moveToFirst()
                                setImageView(cursor)
                            }
                        }
                    }
                }, 2000,2000)
            }else{
                back_button.setEnabled(true)
                next_button.setEnabled(true)
                start_button.text="再生"
                mTimer!!.cancel()
                mTimer = null


            }
        }
    }

    private fun setImageView(cursor: Cursor) {
        // indexからIDを取得し、そのIDから画像のURIを取得する
        val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val id = cursor.getLong(fieldIndex)
        var imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        imageView.setImageURI(imageUri)
    }
}