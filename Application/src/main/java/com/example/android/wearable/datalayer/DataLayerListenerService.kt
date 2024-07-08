package com.example.android.wearable.datalayer

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.AsyncTask
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import edu.ucsd.sccn.LSL
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel


class DataLayerListenerService :
    WearableListenerService(),
    DataClient.OnDataChangedListener {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    //// LSL Outlet
    val LSL_OUTLET_NAME_HR = "HeartRate"
    val LSL_OUTLET_TYPE_HR = "DataLayer"
    val LSL_OUTLET_CHANNELS_HR = 1
    val LSL_OUTLET_NOMINAL_RATE_HR = LSL.IRREGULAR_RATE
    val LSL_OUTLET_CHANNEL_FORMAT_HR = LSL.ChannelFormat.int16
    var info_HR: LSL.StreamInfo? = null
    var outlet_HR: LSL.StreamOutlet? = null
    var samples_HR = IntArray(1)

    private fun sendDataHR(data: Float?) {
        Log.d("LSL", "Now sending HR:$data")

        try {
            /*final String dataString = Integer.toString(data);
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    showMessage("Now sending HR: " + dataString);
                }
            });*/
            samples_HR[0] = data!!.toInt()
            Log.d("LSL", "Pushing sample:$samples_HR")
            outlet_HR!!.push_sample(samples_HR)

            //Thread.sleep(5);
        } catch (ex: java.lang.Exception) {
            //ex.message?.let { showMessage(it) }
            Log.e("LSL", "Failed to push sample:")
            outlet_HR!!.close()
            info_HR!!.destroy()
        }
    }


    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    // サービスの初期化時に実行する処理
    override fun onCreate() {
            //// LSL Outlet
        println(LSL.local_clock())

        AsyncTask.execute(Runnable { // configure HR
            //showMessage("Creating a new StreamInfo HR...")
            Log.e("LSL", "Creating a new StreamInfo HR...")
            info_HR = LSL.StreamInfo(
                LSL_OUTLET_NAME_HR,
                LSL_OUTLET_TYPE_HR,
                LSL_OUTLET_CHANNELS_HR,
                LSL_OUTLET_NOMINAL_RATE_HR,
                LSL_OUTLET_CHANNEL_FORMAT_HR,
                // DEVICE_ID // Is this device id absolutely necessary?
            )
            //showMessage("Creating an outlet HR...")
            Log.e("LSL", "Creating an outlet HR...")
            Log.d("LSL", "Value:$info_HR")
            outlet_HR = try {
                Log.e("LSL", "LSL outlet opened!!!")
                LSL.StreamOutlet(info_HR)
            } catch (ex: IOException) {
                //showMessage("Unable to open LSL outlet. Have you added <uses-permission android:name=\"android.permission.INTERNET\" /> to your manifest file?")
                Log.e("LSL", "Unable to open LSL outlet. Have you added <uses-permission android:name=\"android.permission.INTERNET\" /> to your manifest file?")
                return@Runnable
            }
        })

    }

    private val _events = mutableStateListOf<Event>()

    /**
     * The list of events from the clients.
     */
    val events: List<Event> = _events
    var heartrate by mutableStateOf<Float?>(null)
        private set

    @SuppressLint("VisibleForTests")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        super.onDataChanged(dataEvents)

        _events.addAll(
            dataEvents.map { dataEvent ->
                val title = when (dataEvent.type) {
                    DataEvent.TYPE_CHANGED -> R.string.data_item_changed
                    DataEvent.TYPE_DELETED -> R.string.data_item_deleted
                    else -> R.string.data_item_unknown
                }

                Event(
                    title = title,
                    text = dataEvent.dataItem.toString()
                )
            }
        )

        // Do additional work for specific events
        dataEvents.forEach { dataEvent ->
            when (dataEvent.type) {
                DataEvent.TYPE_CHANGED -> {
                    when (dataEvent.dataItem.uri.path) {

                        DataLayerListenerService.HR_PATH -> {
                                heartrate = DataMapItem.fromDataItem(dataEvent.dataItem)
                                    .dataMap
                                    .getFloat(DataLayerListenerService.HR_KEY)

                            /*hrtime = DataMapItem.fromDataItem(dataEvent.dataItem)
                                .dataMap
                                .getLong(DataLayerListenerService.HR_TIME_KEY)*/

                            Log.d("LSL", "heart rate extracted from DataLayerListenerService")
                            sendDataHR(heartrate)

                        }

                    }
                }
            }

        }

        /*
        dataEvents.forEach { dataEvent ->
            val uri = dataEvent.dataItem.uri
            when (uri.path) {

                COUNT_PATH -> {
                    scope.launch {
                        try {
                            val nodeId = uri.host!!
                            val payload = uri.toString().toByteArray()
                            messageClient.sendMessage(
                                nodeId,
                                DATA_ITEM_RECEIVED_PATH,
                                payload
                            )
                                .await()
                            Log.d(TAG, "Count Message sent successfully")
                        } catch (cancellationException: CancellationException) {
                            throw cancellationException
                        } catch (exception: Exception) {
                            Log.d(TAG, "Count Message failed")
                        }
                    }
                }

                HR_PATH -> {
                    scope.launch {
                        try {
                            val nodeId = uri.host!!
                            val payload = uri.toString().toByteArray()
                            messageClient.sendMessage(
                                nodeId,
                                DATA_ITEM_RECEIVED_PATH,
                                payload
                            )
                                .await()
                            Log.d(TAG, "HR Message sent successfully")
                        } catch (cancellationException: CancellationException) {
                            throw cancellationException
                        } catch (exception: Exception) {
                            Log.d(TAG, "HR Message failed")
                        }
                    }
                }

                LIGHT_PATH -> {
                    scope.launch {
                        try {
                            val nodeId = uri.host!!
                            val payload = uri.toString().toByteArray()
                            messageClient.sendMessage(
                                nodeId,
                                DATA_ITEM_RECEIVED_PATH,
                                payload
                            )
                                .await()
                            Log.d(TAG, "Light Message sent successfully")
                        } catch (cancellationException: CancellationException) {
                            throw cancellationException
                        } catch (exception: Exception) {
                            Log.d(TAG, "Light Message failed")
                        }
                    }
                }
            }
        }*/

    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        _events.add(
            Event(
                title = R.string.message_from_watch,
                text = messageEvent.toString()
            )
        )
    }


    /**
     * A data holder describing a client event.
     */
    data class Event(
        @StringRes val title: Int,
        val text: String
    )


    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        //outlet_HR!!.close()
        //info_HR!!.destroy()
    }

    companion object {
        private const val TAG = "DataLayerListenerService"
        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val DATA_ITEM_RECEIVED_PATH = "/data-item-received"
        const val COUNT_PATH = "/count"
        const val IMAGE_PATH = "/image"
        const val IMAGE_KEY = "photo"
        const val HR_PATH = "/hr"
        const val HR_KEY = "hr"
        const val HR_TIME_KEY = "hr_time"
        const val LIGHT_PATH = "/light"
        const val LIGHT_KEY = "light"
        const val LIGHT_TIME_KEY = "light_time"
    }
}
