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

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

//import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter


/**
 * The UI affording the actions the user can take, along with a list of the events and the image
 * to be sent to the wearable devices.
 */
@Composable
fun MainApp(
    //events: List<Event>,
    //image: Bitmap?,
    hr: Float?,
    light: Float?,
    hrtime: Long?,
    lighttime: Long?,
    //isCameraSupported: Boolean,
    //onTakePhotoClick: () -> Unit,
    //onSendPhotoClick: () -> Unit,
    onStartWearableActivityClick: () -> Unit
) {
    val nowtimestamp = LocalDateTime.now()
    val timezone = "Asia/Tokyo"

    /*
    val rows=listOf(hrtime,hr)
    csvWriter().WriteAll(rows, "test.csv")

     */

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Button(onClick = onStartWearableActivityClick) {
                Text(stringResource(id = R.string.start_wearable_activity))
            }
            Divider()
        }
        item { // SHOW HEART RATE!!
            Column( modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Heart Rate (bmp)",
                    //text = "Heart Rate: ",
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (hrtime != null) "${Instant.ofEpochMilli(hrtime).atZone(ZoneId.of(timezone)).toLocalDateTime()}" else "$nowtimestamp",
                    //text = "Heart Rate: ",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (hr != null) "$hr" else "0",
                    //text = "Heart Rate: ",
                    textAlign = TextAlign.Center,
                    fontSize = 66.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = Color.Red,
                    style = MaterialTheme.typography.subtitle1
                )
            }
            Divider()
        }

        item { // SHOW HEART RATE!!
            Column( modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Illuminance (lux)",
                    //text = "Heart Rate: ",
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (lighttime != null) "${Instant.ofEpochSecond(lighttime).atZone(ZoneId.of(timezone)).toLocalDateTime()}" else "$nowtimestamp",
                    //text = "Heart Rate: ",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (light != null) String.format("%.1f", light) else "0.0",
                    //text = "Heart Rate: ",
                    textAlign = TextAlign.Center,
                    fontSize = 66.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = Color.Magenta,
                    style = MaterialTheme.typography.subtitle1
                )
            }
            Divider()
        }


        /*item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Button(
                        onClick = onTakePhotoClick,
                        enabled = isCameraSupported
                    ) {
                        Text(stringResource(id = R.string.take_photo))
                    }
                    Button(
                        onClick = onSendPhotoClick,
                        enabled = image != null
                    ) {
                        Text(stringResource(id = R.string.send_photo))
                    }
                }

                Box(modifier = Modifier.size(100.dp)) {
                    if (image == null) {
                        Image(
                            painterResource(id = R.drawable.ic_content_picture),
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
            Divider()
        }*/

       /*
        items(events) { event ->
            Column {
                Text(
                    stringResource(id = event.title),
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    event.text,
                    style = MaterialTheme.typography.body2
                )
            }
            Divider()
        }*/
    }
}



@Preview
@Composable
fun MainAppPreview() {
    MainApp(
        /*events = listOf(
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
                title = R.string.message_from_watch,
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
        image = null,*/
        hr = 66.toFloat(),
        light = 10.toFloat(),
        hrtime = Instant.now().epochSecond,
        lighttime = Instant.now().epochSecond,
        //isCameraSupported = true,
        //onTakePhotoClick = {},
        //onSendPhotoClick = {}
    ) {}
}
