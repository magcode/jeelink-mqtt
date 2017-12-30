# Jeelink MQTT

This program exposes Jeelink data via MQTT.
It connects to a given serial port and publishes data to a given MQTT topic.
It aims to comply with [homie MQTT convention](https://github.com/marvinroger/homie). The implementation of the convention is not complete yet.

The program is written in Java. You can run it on almost any device which can run Java.

## Building and running
To build run the following commands:
```
mvn clean install
java -jar target/jeelink-mqtt-0.1.0-SNAPSHOT-jar-with-dependencies.jar <protocol://mqtt broker> <topic> <Serial Interface> <Sketch LACR|EC3K> <optional: schedule in seconds, default 60>
```
Sample:
```
java -jar target/jeelink-mqtt-0.1.0-SNAPSHOT-jar-with-dependencies.jar tcp://192.168.1.20 home/temperatures COM1 LACR 10
```

## Download and running

You can also download a [release](https://github.com/magcode/jeelink-mqtt/releases) and just start it like this:
```
java -jar jeelink-mqtt-1.0.0-jar-with-dependencies.jar <protocol://mqtt broker> <topic> <Serial Interface> <Sketch LACR|EC3K> <optional: schedule in seconds, default 60>
```

## Device data publishing

The following data will be published every 60 minutes:
```
home/temperatures/$nodes 22,12,35,16,18,8,63
home/temperatures/$state ready
home/temperatures/$homie 2.1.0
home/temperatures/$name Jeelink MQTT Gateway on mymachine LACR sketch
home/temperatures/$version 1.0
home/temperatures/$localip 192.168.0.1
```

# Supported sketches
## Lacrosse 
Use `LACR` as starting parameter for the sketch.
The following data is published for each sensor:

```
home/temperatures/18/temperature 22.2
home/temperatures/18/humidity 48
home/temperatures/18/batterylow false
```

* Energy Count 3000
Use `EC3K` as starting parameter for the sketch.
The following data is published for each sensor:

```
home/ec3k/14E7/currentpower 31.0
home/ec3k/14E7/energy 199774
home/ec3k/14E7/timeon 24106151
home/ec3k/14E7/timetotal 24106151
home/ec3k/14E7/maxpower 51.6
```


## Integration into Openhab
Sample for Lacrosse:

```
Number temp_room1   "room1 temp [%.1f °C]" <temperature>  { mqtt="<[mosquitto:home/temperatures/63/temperature:state:default]" }
Number hum_room1   "room1 hum [%1d %%]" <humidity>  { mqtt="<[mosquitto:home/temperatures/63/humidity:state:default]" }
Switch bat_room1 "room1 battery low" {mqtt="<[mosquitto:home/temperatures/63/humidity/batterylow:state:MAP(battery.map)]"}
```

You need a `battery.map` file:
```
true=ON
false=OFF
NULL=OFF
```