package com.example.android.wearable.datalayer

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DataTypeAvailability
import androidx.health.services.client.data.DeltaDataType
import androidx.health.services.client.data.SampleDataPoint
import androidx.health.services.client.getCapabilities
import androidx.health.services.client.unregisterMeasureCallback
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

private const val TAG = "HealthServicesManager"

class HealthServicesManager(context: Context) {
    private val measureClient = HealthServices.getClient(context).measureClient
    private val dataClient by lazy { Wearable.getDataClient(context) }

    suspend fun hasHeartRateCapability() = runCatching {
        val capabilities = measureClient.getCapabilities()
        (DataType.HEART_RATE_BPM in capabilities.supportedDataTypesMeasure)
    }.getOrDefault(false)

    /**
     * Returns a cold flow. When activated, the flow will register a callback for heart rate data
     * and start to emit messages. When the consuming coroutine is canceled, the measure callback
     * is unregistered.
     *
     * [callbackFlow] creates a  bridge between a callback-based API and Kotlin flows.
     */
    @ExperimentalCoroutinesApi
    fun heartRateMeasureFlow(): Flow<MeasureMessage> = callbackFlow {
        val callback = object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {
                // Only send back DataTypeAvailability (not LocationAvailability)
                if (availability is DataTypeAvailability) {
                    trySendBlocking(MeasureMessage.MeasureAvailability(availability))
                }
            }

            override fun onDataReceived(data: DataPointContainer) {
                val heartRateBpm = data.getData(DataType.HEART_RATE_BPM)
                Log.d(TAG, "💓 Received heart rate: ${heartRateBpm.first().value}")
                trySendBlocking(MeasureMessage.MeasureData(heartRateBpm))
            }
        }

        Log.d(TAG, "⌛ Registering for data...")
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, callback)

        awaitClose {
            Log.d(TAG, "👋 Unregistering for data")
            runBlocking {
                measureClient.unregisterMeasureCallback(DataType.HEART_RATE_BPM, callback)
            }
        }
    }

    suspend fun sendToHandheldDevice(heartRate: Int) {
        try {
            val result = dataClient
                .putDataItem(
                    PutDataMapRequest
                    .create("/heartrate")
                    .apply { dataMap.putInt("heartrate", heartRate) }
                    .asPutDataRequest()
                    .setUrgent())
                .await()

            Log.d(TAG, "DataItem saved: $result")
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d(TAG, "Saving DataItem failed: $exception")
        }
    }

}

sealed class MeasureMessage {
    class MeasureAvailability(val availability: DataTypeAvailability) : MeasureMessage()
    class MeasureData(val data: List<SampleDataPoint<Double>>) : MeasureMessage()
}

