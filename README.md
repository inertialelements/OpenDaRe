# OpenDaRe

OpenDaRe – an open Android API for Osmium MIMU22BTP(X) / MIMU22B9P(X) – performs Pedestrian Dead Reckoning (PDR) on the data transmitted from shoe-mounted MIMU22BTP(X) / MIMU22B9P(X), over Bluetooth, to the Android device (Smartphone, Tab etc). 

For more information about Osmium MIMU22BTP(X) / MIMU22B9P(X), visit the official website http://www.inertialelements.com/osmium-mimu22btp.html

BluetoothChatFragment.java implemented below core functionalities:
- Send stepwise command to device
- Receive data from device and processing it to get step details
- Store step details to log file e.g com.inertialements.opendare\LOG_FILES\LOG_FILE_20180215_135524.txt
