package com.github.yatatsu.prettyqr.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.github.yatatsu.prettyqr.R
import com.github.yatatsu.prettyqr.reader.ReaderFragment

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    savedInstanceState?: supportFragmentManager.beginTransaction()
        .replace(R.id.container, ReaderFragment(), ReaderFragment::class.java.name)
        .commit()
  }
}
