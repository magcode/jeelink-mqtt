# Jeelink MQTT

This program exposes Jeelink data via MQTT.
It connects to a given serial port and publishes data to a given MQTT topic.

The program is written in Java. You can run it on almost any device which can run Java.

## Config file
Create a `jeelink.properties` file with the following content. The file must be placed beside the *.jar file.

```
mqttServer=tcp://broker.my.lan   #address of your MQTT server
logLevel=INFO                    #log level
topic=home/temperatures          #root topic
sketchName=LACR                  #LACR or EC3K
serialPortName=/dev/usb-lacr     #port name of your jeelink
```

## Building and running
To build run the following commands:
```
mvn clean install
java -jar target/jeelink-mqtt-0.1.0-SNAPSHOT-jar-with-dependencies.jar
```

Alternatively you can download a [release](https://github.com/magcode/jeelink-mqtt/releases).

# Supported sketches
## Lacrosse 
Use `LACR` as sketchName.
The following data is published for each sensor:

```
home/temperatures/18/temperature 22.2
home/temperatures/18/humidity 48
home/temperatures/18/batterylow false
```

## Energy Count 3000
Use `EC3K` as sketchName.
The following data is published for each sensor:

```
home/ec3k/14E7/currentpower 31.0
home/ec3k/14E7/energy 199774
home/ec3k/14E7/timeon 24106151
home/ec3k/14E7/timetotal 24106151
home/ec3k/14E7/maxpower 51.6
```


# Integration into Openhab
## Sample for Lacrosse

```
Number temp_room1   "room1 temp [%.1f �C]" <temperature>  { mqtt="<[mosquitto:home/temperatures/63/temperature:state:default]" }
Number hum_room1   "room1 hum [%1d %%]" <humidity>  { mqtt="<[mosquitto:home/temperatures/63/humidity:state:default]" }
Switch bat_room1 "room1 battery low" {mqtt="<[mosquitto:home/temperatures/63/humidity/batterylow:state:MAP(battery.map)]"}
```

You need a `battery.map` file:
```
true=ON
false=OFF
NULL=OFF
```

## Sample for EC3K
```
Number ec3k1Power "ec3k 1 power" { mqtt="<[mosquitto:home/ec3k/0E3D/currentpower:state:default]" }
Number ec3k1Max "ec3k 1 max" { mqtt="<[mosquitto:home/ec3k/0E3D/maxpower:state:default]" }
Number ec3k1EnergyTotal "ec3k 1 total" { mqtt="<[mosquitto:home/ec3k/0E3D/energy:state:default]" }
```
