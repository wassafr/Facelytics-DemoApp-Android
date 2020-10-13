package io.wassa.facelytics_demoapp_android

import io.wassa.facelyticssdk.model.AgePredictionResult
import io.wassa.facelyticssdk.model.Face
import io.wassa.facelyticssdk.model.GenderPredictionResult


data class PredictionResult(val face: Face, var age: AgePredictionResult?, var gender: GenderPredictionResult.Gender?)