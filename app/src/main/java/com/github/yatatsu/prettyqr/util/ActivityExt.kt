package com.github.yatatsu.prettyqr.util

import android.app.Activity
import android.content.Intent

inline fun <T> Activity.createIntent(clazz: Class<T>): Intent = Intent(this, clazz)