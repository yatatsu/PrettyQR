package com.github.yatatsu.prettyqr.reader

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.github.yatatsu.prettyqr.R
import com.github.yatatsu.prettyqr.reader.camera.CameraSource
import com.github.yatatsu.prettyqr.reader.camera.CameraSourcePreview
import com.github.yatatsu.prettyqr.result.ResultActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector.Detections
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.MultiProcessor.Factory
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.OnShowRationale
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.RuntimePermissions
import timber.log.Timber
import java.io.IOException

typealias TrackerCallback = (barcode: Barcode) -> Unit

@RuntimePermissions class ReaderFragment : Fragment() {

  companion object {
    const val RC_HANDLE_GMS = 9001
  }

  private var cameraSource: CameraSource? = null
  private lateinit var preview: CameraSourcePreview
  private lateinit var handler: Handler

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    handler = Handler(Looper.getMainLooper())
    return inflater!!.inflate(R.layout.fragment_reader, container, false)
        .also {
          preview = it.findViewById(R.id.camera)
        }
  }

  override fun onResume() {
    super.onResume()
    startCameraSafelyWithPermissionCheck()
  }

  override fun onPause() {
    preview.stop()
    super.onPause()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
      grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    onRequestPermissionsResult(requestCode, grantResults)
  }

  @SuppressLint("MissingPermission")
  @NeedsPermission(Manifest.permission.CAMERA) fun startCameraSafely() {
    Timber.d("start camera")
    GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context.applicationContext)
        .takeIf { it != ConnectionResult.SUCCESS }
        ?.let { GoogleApiAvailability.getInstance().getErrorDialog(activity, it, RC_HANDLE_GMS).show() }

    if (cameraSource == null) {
      createDetector(true, false)
    }
    cameraSource?.let {
      try {
        preview.start(cameraSource)
      } catch (e: IOException) {
        Timber.e(e, "Unable to start camera source.")
        cameraSource?.release()
        cameraSource = null
      }
    }
  }

  @OnShowRationale(Manifest.permission.CAMERA)
  fun onShowRationaleForCamera(request: PermissionRequest) {
    // TODO
  }

  @OnPermissionDenied(Manifest.permission.CAMERA)
  fun onPermissionDeniedForCamera() {
    // TODO
  }

  @OnNeverAskAgain(Manifest.permission.CAMERA)
  fun onNeverAskAgainForCamera() {
    // TODO
  }

  class SimpleBarcodeTrackerFactory(private val callback: TrackerCallback) : Factory<Barcode> {
    override fun create(barcode: Barcode?): Tracker<Barcode> {
      return SimpleTracker(callback)
    }
  }

  class SimpleTracker(private val callback: TrackerCallback): Tracker<Barcode>() {

    override fun onUpdate(detections: Detections<Barcode>?, barcode: Barcode?) {
      barcode?.let { callback(it) }
    }
  }

  private fun createDetector(autoFocus: Boolean, useFlash: Boolean) {
    Timber.d("start creating detector")
    val detector = BarcodeDetector.Builder(context)
        .setBarcodeFormats(Barcode.QR_CODE)
        .build()
    detector.setProcessor(
        MultiProcessor.Builder<Barcode>(SimpleBarcodeTrackerFactory({ barcode ->
          handler.post({
            Timber.d("barcode = ${barcode.displayValue}")
            ResultActivity.startActivity(activity, barcode.displayValue)
          })
        })).build())

    if (!detector.isOperational) {
      // Note: The first time that an app using the barcode or face API is installed on a
      // device, GMS will download a native libraries to the device in order to do detection.
      // Usually this completes before the app is run for the first time.  But if that
      // download has not yet completed, then the above call will not detect any barcodes
      // and/or faces.
      //
      // isOperational() can be used to check if the required native libraries are currently
      // available.  The detectors will automatically become operational once the library
      // downloads complete on device.
      Timber.d("Detector dependencies are not yet available.")

      val lowStorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
      val hasLowStorage = context?.registerReceiver(null, lowStorageFilter) != null

      if (hasLowStorage) {
        Toast.makeText(context, "low storage error", Toast.LENGTH_LONG).show()
        Timber.w("low storage error")
      }
    }

    val builder = CameraSource.Builder(context.applicationContext, detector)
        .setFacing(CameraSource.CAMERA_FACING_BACK)
        .setRequestedPreviewSize(1600, 1024)
        .setRequestedFps(15.0f)

    // make sure that auto focus is an available option
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      builder.setFocusMode(
          if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null)
    }

    cameraSource = builder
        .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
        .build()

    Timber.d("create camera source")
  }
}