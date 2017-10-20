package com.github.yatatsu.prettyqr.result

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.github.yatatsu.prettyqr.R
import com.github.yatatsu.prettyqr.util.createIntent
import kotlinx.android.synthetic.main.activity_result.button_open
import kotlinx.android.synthetic.main.activity_result.label_url
import timber.log.Timber

class ResultActivity: AppCompatActivity() {

  companion object {

    private const val EXTRA_BARCODE_URL = "EXTRA_BARCODE_URL"

    fun startActivity(activity: Activity, urlString: String) {
      activity.createIntent(ResultActivity::class.java)
          .putExtra(EXTRA_BARCODE_URL, urlString)
          .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
          .let { activity.startActivity(it) }
    }
  }

  private val urlString = lazy {
    intent.getStringExtra(EXTRA_BARCODE_URL)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_result)

    label_url.text = urlString.value
    button_open.setOnClickListener {
      val i = Intent(Intent.ACTION_VIEW, Uri.parse(urlString.value))
      try {
        startActivity(i)
      } catch (e: ActivityNotFoundException) {
        Timber.w(e, "not found activity")
        Toast.makeText(this, "can't open", Toast.LENGTH_LONG).show()
      }
    }
  }

}