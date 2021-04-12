package io.wassa.facelytics_demoapp_android

import io.wassa.facelyticssdk.model.Face
import io.wassa.facelyticssdk.model.PredictionResult

data class PredictionResult(val face: Face, var result: PredictionResult?)