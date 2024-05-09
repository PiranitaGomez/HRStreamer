/*
 * Copyright 2021 The Android Open Source Project
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
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import java.time.Instant
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()


    private val isBodySensorsPermissionGranted: Boolean
        get() {
            return checkSelfPermission(Manifest.permission.BODY_SENSORS) ==
                PackageManager.PERMISSION_GRANTED
        }


    // What's wrong with hr being composable? --> it worked!
    private val hr: Float
        @Composable
        get() = getHR(isBodySensorsPermissionGranted = isBodySensorsPermissionGranted)

    fun navigateToAppInfo() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", packageName, null))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainApp(
                events = clientDataViewModel.events,
                image = clientDataViewModel.image,
                isBodySensorsPermissionGranted = isBodySensorsPermissionGranted,
                onQueryOtherDevicesClicked = ::onQueryOtherDevicesClicked,
                onQueryMobileCameraClicked = ::onQueryMobileCameraClicked,
                navigateToAppInfo = ::navigateToAppInfo,
                hr = hr
                    //hr = getHR(isBodySensorsPermissionGranted)
            )

            sendHR(hr)

        }



        //sendHR(clientDataViewModel.hr)
        //sendHR(66.toFloat())

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
                    //dataMap.putLong(TIME_KEY, Instant.now().epochSecond)
                }
                    .asPutDataRequest()
                    .setUrgent()

                val result = dataClient.putDataItem(request).await()

                Log.d(TAG, "HR DataItem saved: $result")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "HR Saving DataItem failed: $exception")
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
    val lightSensorState = rememberLightSensorState()

    // BODY_SENSORS permission must be granted before accessing sensor

    LaunchedEffect(lifecycleState) {
        if (lifecycleState == Lifecycle.Event.ON_RESUME) {
            isPermissionGranted = isBodySensorsPermissionGranted
            if (isPermissionGranted == true) {
                heartRateSensorState.startListening()
                lightSensorState.startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
            }
        }
    }

    hr = heartRateSensorState.heartRate

    return hr as Float

}



