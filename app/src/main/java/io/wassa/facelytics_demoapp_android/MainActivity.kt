package io.wassa.facelytics_demoapp_android

import android.Manifest
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.wassa.facelyticssdk.Facelytics
import io.wassa.facelyticssdk.model.Face
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), ImageAnalysis.Analyzer {

    lateinit var facelytics: Facelytics
    var computeFaces = false
    var computesPredictions = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        facelytics = Facelytics(
            this,
            "YOUR LICENSE KEY"
        )
        facelytics.loadModel().subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                RxPermissions(this).request(Manifest.permission.CAMERA).subscribe {
                    if (it)
                        cameraView.post { startCamera() }
                }
            }, {
                Log.e("Facelytics Sample", "Error while loading models", it)
            })

        cameraView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    private fun startCamera() {
        // Create configuration object for the viewfinder use case
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(AspectRatio.RATIO_4_3)
            //setTargetResolution(Size(1024, 1024))
        }.build()


        // Build the viewfinder use case
        val preview = Preview(previewConfig)

        // Every time the viewfinder is updated, recompute layout
        preview.setOnPreviewOutputUpdateListener {

            // To update the SurfaceTexture, we have to remove it and re-add it
            val parent = cameraView.parent as ViewGroup
            parent.removeView(cameraView)
            parent.addView(cameraView, 0)

            cameraView.setSurfaceTexture(it.surfaceTexture)
            updateTransform()
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // In our analysis, we care more about the latest image than
            // analyzing *every* image
            setImageReaderMode(
                ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE
            )
            setImageQueueDepth(5)
            setTargetResolution(Size(680, 680))
        }.build()

        // Build the image analysis use case and instantiate our analyzer
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            setAnalyzer(
                ThreadPoolExecutor(
                    2,
                    8,
                    2000,
                    TimeUnit.MILLISECONDS,
                    LinkedBlockingQueue<Runnable>()
                ), this@MainActivity
            )
        }

        // Bind use cases to lifecycle
        CameraX.bindToLifecycle(this, preview, analyzerUseCase)
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // Compute the center of the view finder
        val centerX = cameraView.width / 2f
        val centerY = cameraView.height / 2f

        // Correct preview output to account for display rotation
        val rotationDegrees = when (cameraView.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // Finally, apply transformations to our TextureView
        cameraView.setTransform(matrix)
    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        if (image?.image != null && !computeFaces) {
            computeFaces = true
            facelytics.detectFaces(image.image!!, rotationDegrees)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ onFacesComputed(it.first) }, {
                    computeFaces = false
                    Log.e("Facelytics Sample", "Error while recognize faces", it)
                })
        }
    }

    private fun onFacesComputed(faces: List<Face>) {
        computeFaces = false
        faceView.updateFace(faces)
        faceView.invalidate()
        if (!computesPredictions) {
            computesPredictions = true
            Observable.fromIterable(faces)
                .flatMap { face ->
                    facelytics.predictAttributes(face).map { PredictionResult(face, it) }
                        .toObservable()
                }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    faceView.updatePrediction(it)
                    faceView.invalidate()
                }, {
                    Log.e("Facelytics Sample", "Error while predict", it)
                    computesPredictions = false
                }, {
                    computesPredictions = false
                })
        }
    }
}