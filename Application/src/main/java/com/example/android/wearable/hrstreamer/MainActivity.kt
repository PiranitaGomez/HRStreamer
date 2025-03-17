/*
 * Copyright 2025 The HR Streamer Project
 *
 * Licensed under the MIT License
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wearable.hrstreamer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Manages Wearable clients to showcase the [DataClient], [MessageClient], [CapabilityClient]
 *
 * This activity allows the user to launch the companion wear activity via the [MessageClient].
 *
 * While resumed, this activity also logs all interactions across the clients, which includes events
 * sent from this activity and from the watch(es).
 */
@SuppressLint("VisibleForTests")
class MainActivity : ComponentActivity() {

    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val capabilityClient by lazy { Wearable.getCapabilityClient(this) }

    private val clientDataViewModel by viewModels<ClientDataViewModel>()
    private var isStreaming by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                    MainApp(
                        hr = clientDataViewModel.heartrate,
                        hrtime = clientDataViewModel.hrsendtime,
                        isStreaming = isStreaming,
                        onStartWearableActivityClick = ::startWearableActivity,
                        onToggleStreamingClick = ::toggleStreaming
                    )
            }
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


    }



    override fun onResume() {
        super.onResume()
        if (isStreaming) {
            dataClient.addListener(clientDataViewModel)
        }
        /*messageClient.addListener(clientDataViewModel)
        capabilityClient.addListener(
            clientDataViewModel,
            Uri.parse("wear://"),
            CapabilityClient.FILTER_REACHABLE
        )

        if (isCameraSupported) {
            lifecycleScope.launch {
                try {
                    capabilityClient.addLocalCapability(CAMERA_CAPABILITY).await()
                } catch (cancellationException: CancellationException) {
                    throw cancellationException
                } catch (exception: Exception) {
                    Log.e(TAG, "Could not add capability: $exception")
                }
            }
        }*/
    }

    override fun onPause() {
        super.onPause()
        if (isStreaming) {
            dataClient.removeListener(clientDataViewModel)
        }
        //messageClient.removeListener(clientDataViewModel)
        //capabilityClient.removeListener(clientDataViewModel)
    }

    private fun toggleStreaming() {
        isStreaming = !isStreaming
        if (isStreaming) {
            dataClient.addListener(clientDataViewModel)
        } else {
            dataClient.removeListener(clientDataViewModel)
        }
    }

    private fun startWearableActivity() {
        lifecycleScope.launch {
            try {
                val nodes = capabilityClient
                    .getCapability(WEAR_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
                    .await()
                    .nodes

                // Send a message to all nodes in parallel
                nodes.map { node ->
                    async {
                        messageClient.sendMessage(node.id, START_ACTIVITY_PATH, byteArrayOf())
                            .await()
                    }
                }.awaitAll()

                Log.d(TAG, "Starting activity requests sent successfully")
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (exception: Exception) {
                Log.d(TAG, "Starting activity failed: $exception")
            }
        }
    }


    companion object {
        private const val TAG = "AndroidMainActivity"
        private const val START_ACTIVITY_PATH = "/start-activity"
        private const val WEAR_CAPABILITY = "wear"

    }

}
