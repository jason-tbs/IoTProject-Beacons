This project uses beacons to control Philip Hue Lights.

It has an android app that is meant to control the lights as well as a server in arduino.
Commands sent to the server is gone through an MQTT broker, this was mandatory for the task.

The android app has to connect to the MQTT broker, once connected the user is able to control the lights.
If the user is within range to control a beacon the light the button will be activated, 
there are three sliders to control brightness, saturation and colour. 