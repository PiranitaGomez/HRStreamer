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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.axis.axisGuidelineComponent
import com.patrykandpatrick.vico.compose.axis.axisLabelComponent
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId


var dataTotal = 0
data class DataPoint(val number: Int, val data: Float?)


@Composable
fun tsChart(
    modifier: Modifier = Modifier,
    data: Float?,
    listLen: Int
) {
    // List to hold the heart rate data
    val listData = remember { mutableStateListOf<DataPoint>()}
    val listlen = listLen // Limit the length of data in the plot

    // Update listData whenever new data value comes in
    LaunchedEffect(data) {
        data.let {
            if (listData.size > (listlen - 1)) {
                //listData.removeAt(0)
                listData.removeRange(0, listData.size - listlen)
            } // Limit the data size to the last 50 points
            listData.add(DataPoint(dataTotal, it)) // Add new heart rate entry
            dataTotal += 1
        }
    }

    // Map hrData to ChartEntry
    val chartEntries = listData.map {
        entryOf(it.number, it.data ?: 0f) // Use data (if available), otherwise 0
    }

    LineChartComponent(chartEntries)

}

//@SuppressLint("RememberReturnType")
@Composable
fun LineChartComponent(chartEntries: List<FloatEntry>) {
    val chartEntryModelProducer = remember { ChartEntryModelProducer() }

    LaunchedEffect(chartEntries) {
        //chartEntryModelProducer.setEntries(chartEntries.map { entryOf(it.first, it.second) })
        chartEntryModelProducer.setEntries(chartEntries)
    }

    Chart(
        chart = lineChart(),
        chartModelProducer = chartEntryModelProducer,
        startAxis =
        rememberStartAxis(
            label = axisLabelComponent(textSize = 15.sp),
            guideline = axisGuidelineComponent(thickness = 1.dp),
            itemPlacer = remember { AxisItemPlacer.Vertical.default() }
        ),
        bottomAxis = rememberBottomAxis(guideline = null),
        chartScrollSpec = rememberChartScrollSpec(isScrollEnabled = false)
    )
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainApp(
    hr: Float?,
    //light: Float?,
    hrtime: Long?,
    //lighttime: Long?,
    isStreaming: Boolean,
    onToggleStreamingClick: () -> Unit,
    onStartWearableActivityClick: () -> Unit
) {
    val nowtimestamp = LocalDateTime.now()
    val timezone = "Asia/Tokyo"

    // State for user input of listlen
    var listLen by remember { mutableStateOf(10)}  // Default value is 10
    val options = listOf(10, 20, 50)
    var expanded by remember { mutableStateOf(false) }
    var selectedOptionText by remember { mutableStateOf(options[0].toString()) }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Button(onClick = onStartWearableActivityClick,
                   colors = androidx.compose.material.ButtonDefaults.buttonColors(
                       backgroundColor = Color.DarkGray //Color(0xFF333333)
                   ),
                elevation = null, // Removes the gray outline
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(id = R.string.start_wearable_activity), color = Color.White)
            }
        }
        item {
            Button(
                onClick = onToggleStreamingClick,
                colors = androidx.compose.material.ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red //Color(0xFFD32F2F) // Grass Green
                ),
                elevation = null, // Removes the gray outline
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(if (isStreaming) "Stop Streaming" else "Start Streaming", color = Color.White)
            }
            Spacer(modifier = Modifier.height(12.dp))
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
            Spacer(modifier = Modifier.height(16.dp))
        }


        item {
            Text(
                text = "Select the number of data to display:",
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(bottom = 8.dp) // Optional: adds space between the label and the dropdown
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.width(100.dp) // Optional: set the width and height of the dropdown
                                    .height(50.dp)
            ) {
                TextField(
                    value = selectedOptionText,
                    onValueChange = {},
                    //label = { Text("Select") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown Icon"
                        )
                    },
                    //style = TextStyle(
                    //    fontSize = 12.sp, // Set smaller font size here
                    //fontWeight = FontWeight.Normal // Optional: set font weight to normal
                    //)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { selectionOption ->
                        DropdownMenuItem(onClick = {
                            selectedOptionText = selectionOption.toString()
                            listLen = selectionOption
                            expanded = false
                        }) {
                            Text(text = selectionOption.toString(),
                                style = TextStyle(
                                    fontSize = 12.sp, // Smaller text size
                                    // fontWeight = FontWeight.Normal // Optional: set font weight to normal
                            )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            tsChart(data = hr, listLen = listLen)

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
        hrtime = Instant.now().toEpochMilli(),
        isStreaming = true,
        onToggleStreamingClick = {},
        onStartWearableActivityClick = {}
        )
}
