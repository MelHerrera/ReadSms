package com.example.readsms

import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.text.SimpleDateFormat
import java.util.*

class ReadSms:AppCompatActivity() {
    val SMS = Uri.parse("content://sms")
    val PERMISSIONS_REQUEST_READ_SMS = 1
    lateinit var mRecyList:RecyclerView
    lateinit var mRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_sms)

        mRecyList = findViewById(R.id.vRecyMessages)
        mRefresh = findViewById(R.id.vSwipeRefresh)

        checkPermissionAndReadSms()//inicialmente cargar los mensajes

        mRefresh.setOnRefreshListener {
            mRefresh.isRefreshing = true
            checkPermissionAndReadSms()
            mRefresh.isRefreshing = false
        }
    }

    private fun checkPermissionAndReadSms(){
        val permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)

        if(permissionCheck == PackageManager.PERMISSION_GRANTED)
            readSMS()
        else
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS), PERMISSIONS_REQUEST_READ_SMS)
    }

    private inner class SmsCursorAdapterRecy(private var c: Cursor):
        RecyclerView.Adapter<SmsCursorAdapterRecy.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_sms, parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bindView(c, position)
        }

        override fun getItemCount(): Int {
           return c.count
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val smsOrigin = view.findViewById<TextView>(R.id.sms_origin)
            val smsBody =  view.findViewById<TextView>(R.id.sms_body)
            val smsDate =  view.findViewById<TextView>(R.id.sms_date)

            fun bindView(cursor: Cursor, pos: Int){
                cursor.moveToPosition(pos)

                smsOrigin.text = cursor.getString( cursor.getColumnIndexOrThrow( SmsColumns.ADDRESS) )
                smsBody.text =  cursor.getString( cursor.getColumnIndexOrThrow(SmsColumns.BODY) )

                val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val dateFormatted = simpleDateFormat.format(cursor.getLong( cursor.getColumnIndexOrThrow(SmsColumns.DATE))).toString()
                smsDate.text =   dateFormatted
            }
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

        val adapter = cursor?.let { SmsCursorAdapterRecy(it) }
        val layoutManager = LinearLayoutManager(this)
        mRecyList.layoutManager = layoutManager
        mRecyList.adapter = adapter
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