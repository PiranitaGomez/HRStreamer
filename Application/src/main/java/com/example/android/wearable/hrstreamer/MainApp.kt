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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun MainApp(
    hr: Float?,
    //light: Float?,
    hrtime: Long?,
    //lighttime: Long?,
    onStartWearableActivityClick: () -> Unit
) {
    val nowtimestamp = LocalDateTime.now()
    val timezone = "Asia/Tokyo"

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
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (hrtime != null) "${Instant.ofEpochMilli(hrtime).atZone(ZoneId.of(timezone)).toLocalDateTime()}" else "$nowtimestamp",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (hr != null) "$hr" else "0",
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

        /*
        item { // SHOW Light!!
            Column( modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Illuminance (lux)",
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (lighttime != null) "${Instant.ofEpochSecond(lighttime).atZone(ZoneId.of(timezone)).toLocalDateTime()}" else "$nowtimestamp",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.subtitle1
                )
                Text(
                    text = if (light != null) String.format("%.1f", light) else "0.0",
                    textAlign = TextAlign.Center,
                    fontSize = 66.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = Color.Magenta,
                    style = MaterialTheme.typography.subtitle1
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
        hr = 66.toFloat(),
        //light = 10.toFloat(),
        hrtime = Instant.now().epochSecond,
        //lighttime = Instant.now().epochSecond
    ) {}
}
