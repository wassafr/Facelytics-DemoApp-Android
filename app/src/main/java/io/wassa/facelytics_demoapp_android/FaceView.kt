package io.wassa.facelytics_demoapp_android

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import io.wassa.facelyticssdk.model.Face
import io.wassa.facelyticssdk.model.GenderPredictionResult

class FaceView : View {

    var faceList: List<Face> = listOf()
    var predictionMap: MutableMap<Int?, PredictionResult> = mutableMapOf()

    private val paint: Paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 5f
        textSize = 50f
    }

    fun updateFace(faces: List<Face>) {
        faceList = faces
        invalidate()
    }

    fun updatePrediction(prediction: PredictionResult) {
        predictionMap[prediction.face.trackingId] = prediction
        invalidate()
    }

    /**
     * In the example view, this drawable is drawn above the text.
     */
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.d("Draw", "size = ${canvas?.width}")
        faceList.forEach {
            if (it.rect != null) {
                canvas?.drawCircle(
                    it.rect!!.exactCenterX() / 680 * 1300,
                    it.rect!!.exactCenterY() / 680 * 1300,
                    100f,
                    paint
                )
                if (predictionMap[it.trackingId]?.age != null) {
                    canvas?.drawText(
                        predictionMap[it.trackingId]?.age?.average.toString(),
                        it.rect!!.exactCenterX() / 680 * 1300 - 20,
                        it.rect!!.exactCenterY() / 680 * 1300 - 20,
                        paint
                    )
                    predictionMap[it.trackingId]?.age?.rawResultArray?.forEachIndexed { index, fl ->
                        canvas?.drawRect((it.rect!!.exactCenterX() / 680 * 1300) - 50 + index,
                            ((it.rect!!.exactCenterY() / 680 * 1300) + 50) - (50 * (fl)),
                            (it.rect!!.exactCenterX() / 680 * 1300) - 50 + index + 4,
                            ((it.rect!!.exactCenterY() / 680 * 1300) + 50), paint)
                    }
                }
                if (predictionMap[it.trackingId]?.gender != null)
                    canvas?.drawText(
                        if (predictionMap[it.trackingId]?.gender == GenderPredictionResult.Gender.FEMALE) "Femme" else "Homme",
                        it.rect!!.exactCenterX() / 680 * 1300 - 70,
                        it.rect!!.exactCenterY() / 680 * 1300 + 100,
                        paint
                    )
            }
        }
    }
}