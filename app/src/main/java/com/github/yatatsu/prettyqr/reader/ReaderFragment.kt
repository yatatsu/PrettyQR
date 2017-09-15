package com.github.yatatsu.prettyqr.reader

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.yatatsu.prettyqr.R
import com.github.yatatsu.prettyqr.reader.ReaderFragmentPermissionsDispatcher.onRequestPermissionsResult
import com.github.yatatsu.prettyqr.reader.ReaderFragmentPermissionsDispatcher.startCameraSafelyWithCheck
import com.google.android.cameraview.CameraView
import com.google.android.cameraview.CameraView.Callback
import permissions.dispatcher.*
import timber.log.Timber


@RuntimePermissions() class ReaderFragment : Fragment() {

  private lateinit var cameraView: CameraView

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    return inflater!!.inflate(R.layout.fragment_reader, container, false)
        .also {
          cameraView = it.findViewById<CameraView>(R.id.camera)
              .apply { addCallback(cameraCallback) }
        }
  }

  override fun onResume() {
    super.onResume()
    startCameraSafelyWithCheck(this)
  }

  override fun onPause() {
    cameraView.stop()
    super.onPause()
  }

  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
      grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    onRequestPermissionsResult(this, requestCode, grantResults)
  }

  private var cameraCallback = object : Callback() {
    override fun onCameraOpened(cameraView: CameraView?) {
      Timber.d("onCameraOpened")
    }

    override fun onCameraClosed(cameraView: CameraView?) {
      Timber.d("onCameraClosed")
    }
  }

  @NeedsPermission(Manifest.permission.CAMERA) fun startCameraSafely() {
    cameraView.start()
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
}