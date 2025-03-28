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
package com.example.android.wearable.hrstreamer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import androidx.wear.compose.material.rememberScalingLazyListState

import android.Manifest
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mutualmobile.composesensors.rememberHeartRateSensorState
import com.mutualmobile.composesensors.rememberLightSensorState
import kotlin.reflect.KSuspendFunction1
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.takeWhile

@OptIn(ExperimentalWearMaterialApi::class, ExperimentalCoroutinesApi::class)
@Composable
fun MainApp(
    events: List<Event>,
    isBodySensorsPermissionGranted: Boolean,
    //onQueryOtherDevicesClicked: () -> Unit,
    //onQueryMobileCameraClicked: () -> Unit,
    navigateToAppInfo: () -> Unit,
    hr: Float
) {
    val scalingLazyListState = rememberScalingLazyListState()
    var isPermissionGranted: Boolean? by remember { mutableStateOf(null) }

    isPermissionGranted = isBodySensorsPermissionGranted


    Scaffold(
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = scalingLazyListState) },
        timeText = { TimeText() }
    ) {
        ScalingLazyColumn(
            state = scalingLazyListState,
            contentPadding = PaddingValues(
                horizontal = 8.dp,
                vertical = 32.dp
            )
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedContent(
                        targetState = isPermissionGranted
                    ) { animatedIsGranted ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            animatedIsGranted?.let { safeIsPermissionGranted ->
                                Text(
                                    text = if (safeIsPermissionGranted) "Heart Rate" else "Please " +
                                        "grant the sensors permission first",
                                    textAlign = TextAlign.Center,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium,
                                    color = androidx.compose.ui.graphics.Color.Yellow
                                    //modifier = Modifier.padding(10.dp).width(200.dp)
                                )
                                Text(
                                    text = if (safeIsPermissionGranted) String.format("%.1f", hr) else "Please " +
                                        "grant the sensors permission first",
                                    textAlign = TextAlign.Center,
                                    fontSize = 56.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium,
                                    color = androidx.compose.ui.graphics.Color.Yellow,
                                    modifier = Modifier.padding(5.dp).width(200.dp)
                                )
                                /*
                                Text(

                                    text = if (safeIsPermissionGranted) String.format(
                                        "Illuminance: %.1f",
                                        light
                                    ) else "Please " +
                                        "grant the sensors permission first",
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium,
                                )*/

                                if (!safeIsPermissionGranted) {
                                    Button(
                                        modifier = Modifier.padding(16.dp),
                                        onClick = { navigateToAppInfo },
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            text = "Grant Permission"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }


                /*
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(32.dp)
                ) {
                    if (image == null) {
                        Image(
                            painterResource(id = R.drawable.photo_placeholder),
                            contentDescription = stringResource(
                                id = R.string.photo_placeholder
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Image(
                            image.asImageBitmap(),
                            contentDescription = stringResource(
                                id = R.string.captured_photo
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            item {
                Button(
                    onClick = onQueryOtherDevicesClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.query_other_devices))
                }
            }

            item {
                Button(
                    onClick = onQueryMobileCameraClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(id = R.string.query_mobile_camera))
                }
            }*/


                /*if (events.isEmpty()) {
                item {
                    Text(
                        stringResource(id = R.string.waiting),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(events) { event ->
                    Card(
                        onClick = {},
                        enabled = false
                    ) {
                        Column {
                            Text(
                                stringResource(id = event.title),
                                style = MaterialTheme.typography.title3
                            )
                            Text(
                                event.text,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                }
            }*/
            }
        }

    }
}

fun Activity.navigateToAppInfo() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", packageName, null))
    )
}

@Composable
fun Lifecycle.observeAsState(): State<Lifecycle.Event> {
    val state = remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    DisposableEffect(this) {
        val observer = LifecycleEventObserver { _, event -> state.value = event }
        this@observeAsState.addObserver(observer)
        onDispose { this@observeAsState.removeObserver(observer) }
    }
    return state
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun MainAppPreviewEvents() {
    MainApp(
        events = listOf(
            Event(
                title = R.string.data_item_changed,
                text = "Event 1"
            ),
            Event(
                title = R.string.data_item_deleted,
                text = "Event 2"
            ),
            Event(
                title = R.string.data_item_unknown,
                text = "Event 3"
            ),
            Event(
                title = R.string.message,
                text = "Event 4"
            ),
            Event(
                title = R.string.data_item_changed,
                text = "Event 5"
            ),
            Event(
                title = R.string.data_item_deleted,
                text = "Event 6"
            )
        ),
        /*
        image = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.WHITE)
        },*/
        isBodySensorsPermissionGranted = true,
        //onQueryOtherDevicesClicked = {},
        //onQueryMobileCameraClicked = {},
        navigateToAppInfo = {},
        hr = 66.toFloat(),
        //light = 10.toFloat()
    )
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun MainAppPreviewEmpty() {
    MainApp(
        events = emptyList(),
        isBodySensorsPermissionGranted = true,
        //onQueryOtherDevicesClicked = {},
        //onQueryMobileCameraClicked = {},
        navigateToAppInfo = {},
        hr = 66.toFloat(),
        //light = 10.toFloat()
    )
}
