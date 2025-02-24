/*
 * Copyright 2025 HRStreamer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.datalayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.mutualmobile.composesensors.rememberHeartRateSensorState
import com.mutualmobile.composesensors.rememberLightSensorState
//import edu.ucsd.sccn.LSL
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()

    /*
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

        Log.d("watch2PC", "Now sending HR:$data")

        try {
                /*final String dataString = Integer.toString(data);
            runOnUiThread(new Runnable(){
                @Override
                public void run(){
                    showMessage("Now sending HR: " + dataString);
                }
            });*/
                samples_HR[0] = data!!.toInt()
                Log.d("watch2PC", "Pushing sample:$samples_HR")
                Log.d("watch2PC", "Double check Outlet:$outlet_HR")

                outlet_HR!!.push_sample(samples_HR)

                //Thread.sleep(5);
            } catch (ex: java.lang.Exception) {
                //ex.message?.let { showMessage(it) }
                Log.e("watch2PC", "Failed to push sample:")
                outlet_HR!!.close()
                info_HR!!.destroy()
            }
    }*/


    private val isBodySensorsPermissionGranted: Boolean
        get() {
            return checkSelfPermission(Manifest.permission.BODY_SENSORS) ==
                PackageManager.PERMISSION_GRANTED
        }


    // What's wrong with hr being composable? --> it worked!

    var heartRate by mutableStateOf<Float?>(null)
        private set

    private val hr: Float
        @Composable
        get() = getHR(isBodySensorsPermissionGranted = isBodySensorsPermissionGranted)

    private val light: Float
        @Composable
        get() = getLight(isBodySensorsPermissionGranted = isBodySensorsPermissionGranted)

    fun navigateToAppInfo() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", packageName, null))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        //// LSL Outlet
        println(LSL.local_clock())

        if(outlet_HR == null) {
            AsyncTask.execute(Runnable { // configure HR
                //showMessage("Creating a new StreamInfo HR...")
                Log.e("watch2PC", "Creating a new StreamInfo HR...")

                info_HR = LSL.StreamInfo(
                    LSL_OUTLET_NAME_HR,
                    LSL_OUTLET_TYPE_HR,
                    LSL_OUTLET_CHANNELS_HR,
                    LSL_OUTLET_NOMINAL_RATE_HR,
                    LSL_OUTLET_CHANNEL_FORMAT_HR
                    // DEVICE_ID // Is this device id absolutely necessary?
                )
                //showMessage("Creating an outlet HR...")
                Log.d("watch2PC", "Creating an outlet HR...")
                Log.d("watch2PC", "Value:$info_HR")
                outlet_HR = try {
                    Log.d("watch2PC", "LSL outlet opening!!!")
                    LSL.StreamOutlet(info_HR)
                } catch (ex: IOException) {
                    //showMessage("Unable to open LSL outlet. Have you added <uses-permission android:name=\"android.permission.INTERNET\" /> to your manifest file?")
                    Log.e(
                        "watch2PC",
                        "Unable to open LSL outlet. Have you added <uses-permission android:name=\"android.permission.INTERNET\" /> to your manifest file?"
                    )
                    return@Runnable
                }

                Log.d("watch2PC", "Outlet opened:$outlet_HR")

            })
        }*/

        setContent {
            MainApp(
                events = clientDataViewModel.events,
                isBodySensorsPermissionGranted = isBodySensorsPermissionGranted,
                //onQueryOtherDevicesClicked = ::onQueryOtherDevicesClicked,
                //onQueryMobileCameraClicked = ::onQueryMobileCameraClicked,
                navigateToAppInfo = ::navigateToAppInfo,
                hr = hr,
                light = light
            )

            sendHR(hr) //send to hand-held device
            sendLight(light) //send to hand-held device
            //sendDataHR(light)

            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        }

    }

    private fun onQueryOtherDevicesClicked() {
        lifecycleScope.launch {
            try {
                val nodes = getCapabilitiesForReachableNodes()
                    .filterValues { MOBILE_CAPABILITY in it || WEAR_CAPABILITY in it }.keys
                displayNodes(nodes)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Querying nodes failed: $exception")
            }
        }
    }

    private fun onQueryMobileCameraClicked() {
        lifecycleScope.launch {
            try {
                val nodes = getCapabilitiesForReachableNodes()
                    .filterValues { MOBILE_CAPABILITY in it && CAMERA_CAPABILITY in it }.keys
                displayNodes(nodes)
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Querying nodes failed: $exception")
            }
        }
    }

    /**
     * Collects the capabilities for all nodes that are reachable using the [CapabilityClient].
     *
     * [CapabilityClient.getAllCapabilities] returns this information as a [Map] from capabilities
     * to nodes, while this function inverts the map so we have a map of [Node]s to capabilities.
     *
     * This form is easier to work with when trying to operate upon all [Node]s.
     */
    private suspend fun getCapabilitiesForReachableNodes(): Map<Node, Set<String>> =
        capabilityClient.getAllCapabilities(CapabilityClient.FILTER_REACHABLE)
            .await()
            // Pair the list of all reachable nodes with their capabilities
            .flatMap { (capability, capabilityInfo) ->
                capabilityInfo.nodes.map { it to capability }
            }
            // Group the pairs by the nodes
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            // Transform the capability list for each node into a set
            .mapValues { it.value.toSet() }

    private fun displayNodes(nodes: Set<Node>) {
        val message = if (nodes.isEmpty()) {
            getString(R.string.no_device)
        } else {
            getString(R.string.connected_nodes, nodes.joinToString(", ") { it.displayName })
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        dataClient.addListener(clientDataViewModel)
        messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )
    }

    override fun onPause() {
        super.onPause()
        dataClient.removeListener(clientDataViewModel)
        messageClient.removeListener(clientDataViewModel)
        capabilityClient.removeListener(clientDataViewModel)
    }

    private fun sendHR(hr: Float?) {
        lifecycleScope.launch {
            try {
                val request = PutDataMapRequest.create(HR_PATH).apply {
                    dataMap.putFloat(HR_KEY, hr!!)
                    dataMap.putLong(HR_SEND_TIME_KEY, System.currentTimeMillis())//Instant.now().epochSecond)
                }
                    .asPutDataRequest()
                    .setUrgent()

                val result = dataClient.putDataItem(request).await()

                Log.d(TAG, "HR_KEY: $hr")
                Log.d(TAG, "HR DataItem saved: $result")
                //Log.d(TAG, "HR_TIME_KEY: ${Instant.now().atZone(ZoneId.of("Asia/Tokyo"))}")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "HR Saving DataItem failed: $exception")
            }
        }

    }

    private fun sendLight(light: Float?) {
        lifecycleScope.launch {
            try {
                val request = PutDataMapRequest.create(LIGHT_PATH).apply {
                    dataMap.putFloat(LIGHT_KEY, light!!)
                    dataMap.putLong(LIGHT_TIME_KEY, Instant.now().epochSecond)
                    //Log.d(TAG, "LIGHT_TIME_KEY: ${Instant.now().atZone(ZoneId.of("Asia/Tokyo"))}")
                    //${Instant.ofEpochSecond(hrtime).atZone(ZoneId.of(timezone)).toLocalDateTime()}
                }
                    .asPutDataRequest()
                    .setUrgent()

                val result = dataClient.putDataItem(request).await()

                Log.d(TAG, "LIGHT: $light")
                Log.d(TAG, "LIGHT DataItem saved: $result")

            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "LIGHT Saving DataItem failed: $exception")
            }
        }

    }

    companion object {
        private const val TAG = "WearMainActivity"

        private const val CAMERA_CAPABILITY = "camera"
        private const val WEAR_CAPABILITY = "wear"
        private const val MOBILE_CAPABILITY = "mobile"
        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val COUNT_PATH = "/count"
        private const val HR_PATH = "/hr"
        private const val HR_KEY = "hr"
        private const val HR_SEND_TIME_KEY = "hr_time"
        private const val LIGHT_PATH = "/light"
        private const val LIGHT_KEY = "light"
        private const val LIGHT_TIME_KEY = "light_time"
        private const val TIME_KEY = "time"
        private const val COUNT_KEY = "count"

    }
}


@Composable
fun getHR(isBodySensorsPermissionGranted: Boolean): Float {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.observeAsState()
    var isPermissionGranted: Boolean? by remember { mutableStateOf(null) }
    var hr: Float? by remember { mutableStateOf(null) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isPermissionGranted = isGranted
        }
    )

    val heartRateSensorState = rememberHeartRateSensorState(autoStart = false)

    // BODY_SENSORS permission must be granted before accessing sensor

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.Event.ON_RESUME) {
            isPermissionGranted = isBodySensorsPermissionGranted
            if (isPermissionGranted == true) {
                heartRateSensorState.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    hr = heartRateSensorState.heartRate

    return hr as Float

}



@Composable
fun getLight(isBodySensorsPermissionGranted: Boolean): Float {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.observeAsState()
    var isPermissionGranted: Boolean? by remember { mutableStateOf(null) }
    var light: Float? by remember { mutableStateOf(null) }


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            isPermissionGranted = isGranted
        }
    )

    val lightSensorState = rememberLightSensorState()

    // BODY_SENSORS permission must be granted before accessing sensor

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.Event.ON_RESUME) {
            isPermissionGranted = isBodySensorsPermissionGranted
            if (isPermissionGranted == true) {
                lightSensorState.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    light = lightSensorState.illuminance

    return light as Float

}
