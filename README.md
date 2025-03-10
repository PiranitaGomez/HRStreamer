# HRStreamer

![Contributors](https://img.shields.io/badge/contributor-PiranitaGomez-green)

*Quick links*

- [Description](#description)
	- [Wear/Andorid side](#wear-android-side)
	- [Unity side](#unity-side)
	- [Disclaimer](#disclaimer)
- [How to use?](#how-to-use)
  - [Step1: Pair up watch and phone](#step1-pair-up-watch-and-phone)
  - [Step 2: Install HRStreamer apps on the watch and phone](#step-2-install-hrstreamer-apps-on-the-watch-and-phone)
  - [Step 3: Set up Unity environment and add script](#step-3-set-up-unity-environment-and-add-script)
  - [Step 4: Start streaming](#start-streaming)
- [Cite the research paper](#cite-the-research-paper)
- [Trouble shooting](#trouble-shooting)
- [Miscellaneous](#more-information)
  - [Lab Homepage](#lab-homepage)
  - [Funding](#funding)

---

# Description
The `HRStreamer` is a software ecosystem that facilitates the data flow from Wear OS to Android and ultimately to Unity. Data communication from Wear OS to Android is achieved via the Wear Data Layer API using BLE, while data transmission from Android to Unity is handled using the Lab Stream Layer (LSL) framework. The user interfaces on both the Wear OS and Android platforms are built using Jetpack Compose. 


## Wear/Android side
This consists of a Wear OS companion app that runs on the Google Pixel Watch, and an Android app that runs on an Android smartphone. The apps work together to enable the streaming of heart rate data from a wearable device to a handheld Android device using the Wear Data Layer API. The Wear OS app utilizes a [`WearableListenerService`][1] to consume and transmit DataEvents, while the Android app implements the [`DataClient.OnDataChangedListener`][2] interface.

The code was developed based on the following resources:
- [Android DataLayer Sample][3]
- [Composesensor library][4]

[1]: https://developers.google.com/android/reference/com/google/android/gms/wearable/WearableListenerService
[2]: https://developers.google.com/android/reference/com/google/android/gms/wearable/DataClient
[3]: https://github.com/android/wear-os-samples/tree/main/DataLayer
[4]: https://github.com/mutualmobile/ComposeSensors

For further development, the following IDE is required:
- Android Studio (with Android SDK platform-tools including ADB installed)
- USB debugging enabled on Android device and Watch. This option is typically found in the Developer Options menu on the device, which can be enabled by tapping the build number in the About Phone section of device settings multiple (7?) times.
- Use a USB cable to connect the devices to the PC for testing


## Unity side
This consists of ...

There is a C# script for LSL receiver located at Assets/LSLreceiver.cs
The scene is extremely simple; it just shows the received HR readings. It's located at Assets/Scenes/SampleScene.unity

There is a LSL plug in in the menu bar LSL->Show Streams. It opens a pane to see the LSL streams found. The HR stream comes with Name="HeartRate", Type="DataLayer", HostName="localhost", DataRate=0 (which means irregular rate).

An example of console debugging information of the LSL receiver

```XML
<?xml version="1.0"?>
<info>
	<name>HeartRate</name>
	<type>DataLayer</type>
	<channel_count>3</channel_count>
	<channel_format>int32</channel_format>
	<source_id>PixelWatch</source_id>
	<nominal_srate>0.000000</nominal_srate>
	<version>1.100000</version>
	<created_at>413934.535274</created_at>
	<uid>5a2e51d0-6162-44a9-b6e0-e719ecf2fea0</uid>
	<session_id>default</session_id>
	<hostname>localhost</hostname>
	<v4address />
	<v4data_port>16572</v4data_port>
	<v4service_port>16572</v4service_port>
	<v6address />
	<v6data_port>16574</v6data_port>
	<v6service_port>16573</v6service_port>
	<desc />
</info>

```

## Disclaimer
The code was tested on the following devices:
- Wear app: Google Pixel Watch 2, Wear OS 4.0
- Android app: OUKITEL WP12 Pro, Android 11
- Unity script: Windows 11, Unity 2022.3.18f

# How to use?

## Step1: Pair up watch and phone
- Download and install the Wear OS app and the Watch app on the Android smartphone 
- Pair up the Google Pixel Watch with the Android smartphone in the Watch app

<!--img src="screenshots/phone_image.png" height="400" alt="Screenshot"/> <img src="screenshots/wearable_background_image.png" height="400" alt="Screenshot"/--> 


## Step 2: Install HRStreamer apps on the watch and phone
- Download and install the HRStreamer Wear app on the Pixel Watch
- Download and install the HRStreamer Android app on the Android smartphone

<!--img src="screenshots/phone_image.png" height="400" alt="Screenshot"/> <img src="screenshots/wearable_background_image.png" height="400" alt="Screenshot"/--> 


## Step 3: Set up Unity environment and add script 
- 

<!--img src="screenshots/phone_image.png" height="400" alt="Screenshot"/> <img src="screenshots/wearable_background_image.png" height="400" alt="Screenshot"/--> 

## Step 4: Start streaming
- Make sure the Pixel Watch is connected to the Android smartphone via BLE, then open the HRStreamer Android app on the smartphone, tap the "start wear activity" button to start streaming.

<!--img src="screenshots/phone_image.png" height="400" alt="Screenshot"/> <img src="screenshots/wearable_background_image.png" height="400" alt="Screenshot"/--> 


# Cite the research paper

If the `HRStreamer` is useful for your research, please consider citing the following paper:

> Liang Z. <a href="https://www.researchgate.net/publication/387920640_Harnessing_Consumer_Smartwatches_for_Heart_Rate_Streaming_in_Unity_Game_Engine"> Harnessing Consumer Smartwatches for Heart Rate Streaming in Unity Game Engine </a>. In: *2024 IEEE Consumer Life Tech Conference (ICLT 2024)*. Sydney, Australia; 2024.

```tex
@inproceedings{liang2024harnessing,
    author = {Liang, Zilu},
    title = {{Harnessing Consumer Smartwatches for Heart Rate Streaming in Unity Game Engine}},
    booktitle = {2024 IEEE Consumer Life Tech Conference (ICLT 2024)},
    year = {2024}
}
```

# Trouble shooting

## Not able to start streaming on Android phone

## LSL stream not detected in Unity


# Miscellaneous
## Lab Homepage
More details of this project, as well as other project of our research group, feel free to drop by the homepage of our ubicomp lab (<http://www.ubicomp-lab.org>) . 

## Funding
This work was supported by the JSPS Grant-in-Aid for Scientific Research (B) (Grant Number: 23K25701)


