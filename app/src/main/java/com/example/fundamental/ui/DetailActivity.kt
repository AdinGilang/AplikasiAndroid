package com.example.fundamental.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.example.fundamental.R
import com.example.fundamental.data.response.DetailEventResponse
import com.example.fundamental.data.response.Event
import com.example.fundamental.data.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailActivity : AppCompatActivity() {

    private lateinit var eventImage: ImageView
    private lateinit var eventName: TextView
    private lateinit var eventDescription: TextView
    private lateinit var eventOwner: TextView
    private lateinit var eventQuota: TextView
    private lateinit var btnOpenButton: Button
    private lateinit var beginTime: TextView
    private lateinit var progressBar: ProgressBar

    private var eventButton: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        eventImage = findViewById(R.id.eventImage)
        eventName = findViewById(R.id.eventName)
        eventDescription = findViewById(R.id.eventDescription)
        eventOwner = findViewById(R.id.eventOwner)
        eventQuota = findViewById(R.id.eventQuota)
        beginTime = findViewById(R.id.beginTime)
        btnOpenButton = findViewById(R.id.eventButton)
        progressBar = findViewById(R.id.progressBar)

        val eventId = intent.getIntExtra("EVENT_ID", -1)
        if (eventId != -1) {
            loadEventDetail(eventId)
        } else {
            Log.e("DetailActivity", "Event ID tidak valid")
            Toast.makeText(this, "Event ID tidak valid", Toast.LENGTH_SHORT).show()
        }
        btnOpenButton.setOnClickListener {
            openEventButton()
        }
    }

    private fun loadEventDetail(eventId: Int) {
        progressBar.visibility = View.VISIBLE

        val client = ApiConfig.getApiService().getEventDetail(eventId)
        client.enqueue(object : Callback<DetailEventResponse> {
            override fun onResponse(
                call: Call<DetailEventResponse>,
                response: Response<DetailEventResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val detailResponse = response.body()
                    if (detailResponse != null && !detailResponse.error) {
                        val event = detailResponse.event
                        displayEventDetails(event)
                    } else {
                        Log.e("DetailActivity", "Event tidak ditemukan atau terdapat kesalahan: ${detailResponse?.message}")
                        Toast.makeText(this@DetailActivity, "Event tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("DetailActivity", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(this@DetailActivity, "Gagal memuat data: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<DetailEventResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("DetailActivity", "Gagal memuat detail event: ${t.message}")
                Toast.makeText(this@DetailActivity, "Gagal memuat data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayEventDetails(event: Event) {
        eventName.text = event.name
        eventDescription.text = HtmlCompat.fromHtml(event.description, HtmlCompat.FROM_HTML_MODE_LEGACY)
        eventOwner.text = "Organizer: ${event.ownerName}"
        eventQuota.text = "Quota: ${event.quota - event.registrants}"
        beginTime.text = "Begin Time: ${event.beginTime}"

        eventButton = event.link
        Glide.with(this)
            .load(event.mediaCover)
            .into(eventImage)
    }

    private fun openEventButton() {
        if (!eventButton.isNullOrEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(eventButton))
            startActivity(intent)
        } else {
            Toast.makeText(this, "Link tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }
}
