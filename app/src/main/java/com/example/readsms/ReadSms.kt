package com.example.readsms

import android.app.ListActivity
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import java.util.jar.Manifest

class ReadSms : ListActivity() {
    val SMS = Uri.parse("Content://sms")
    val PERMISSIONS_REQUEST_READ_SMS = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_read_sms)

        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)

        if(permissionCheck == PackageManager.PERMISSION_GRANTED)
            readSMS()
        else
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS), PERMISSIONS_REQUEST_READ_SMS)
    }

    private inner class SmsCursorAdapter(context: Context, c: Cursor, autoRequery: Boolean) :
        CursorAdapter(context, c, autoRequery) {
        override fun newView(context: Context, cursor: Cursor, viewGroup: ViewGroup): View {
            return View.inflate(
                context,
                R.layout.activity_read_sms,
                null
            )
        }

        override fun bindView(view: View, context: Context, cursor: Cursor) {
            view.findViewById<TextView>(R.id.sms_origin).text = cursor.getString(
                cursor.getColumnIndexOrThrow(
                    SmsColumns.ADDRESS
                )
            )
            view.findViewById<TextView>(R.id.sms_body).text =
                cursor.getString(cursor.getColumnIndexOrThrow(SmsColumns.BODY))
            view.findViewById<TextView>(R.id.sms_date).text = Date(
                cursor.getLong(
                    cursor.getColumnIndexOrThrow(
                        SmsColumns.DATE
                    )
                )
            ).toString()
        }
    }

    private fun readSMS() {
        val cursor = contentResolver.query(
            SMS, arrayOf(
                SmsColumns.ID,
                SmsColumns.ADDRESS,
                SmsColumns.DATE,
                SmsColumns.BODY
            ),
            null,
            null,
            SmsColumns.DATE + " DESC"
        )

        val adapter = cursor?.let { SmsCursorAdapter(this, it, true) }
        listAdapter = adapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSIONS_REQUEST_READ_SMS -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    readSMS()
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show()

                return
            }
        }
    }

    object SmsColumns {
        val ID = "_id"
        val ADDRESS = "address"
        val DATE = "date"
        val BODY = "body"
    }
}