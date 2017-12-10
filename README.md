# Jeelink MQTT

This program exposes Jeelink data via MQTT.
It connects to a given serial port and publishes read data via a given MQTT topic.

The program is written in Java. You can run it on almost any device which can run Java.

To build run the following commands:
```
mvn clean install
java -jar target/jeelink-mqtt-0.1.0-SNAPSHOT-jar-with-dependencies.jar <protocol://mqtt broker> <topic> <Serial Interface> <Sketch LACR|EC3K> <optional: schedule in seconds, default 60>
```

Sample:
```
java -jar target/jeelink-mqtt-0.1.0-SNAPSHOT-jar-with-dependencies.jar tcp://192.168.1.20 home/temp COM1 LACR 10
```

## Supported sketches ##
## Lacrosse 
Use `LACR` as starting parameter for the sketch.
The resulting MQTT message is JSON formatted and looks like this:
```
{
   "12":{
      "sensorId":"12",
      "temp":23.2,
      "hum":59,
      "batNew":false,
      "batLow":false,
      "batLowOH":"OFF"
   },
   "18":{
      "sensorId":"18",
      "temp":23.7,
      "hum":48,
      "batNew":false,
      "batLow":true,
      "batLowOH":"ON"
   }
}
```
* Energy Count 3000
Use `EC3K` as starting parameter for the sketch.


## Integration into Home Automation (Openhab)
Sample for Lacrosse:

```
Number temp_lc1   "lc1 [%.1f °C]" <temperature>  { mqtt="<[mosquitto:home/temp:state:JSONPATH($.18.temp)]" }
Number lf_lc1   "lc1 [%1d %%]" <hum>  { mqtt="<[mosquitto:home/temp:state:JSONPATH($.18.humidity)]" }
Switch switch_lc1_bat "lc1 bat" { mqtt="<[mosquitto:home/temp:state:JSONPATH($.18.batLowOH)]" }
```

Sample for EC3K:

```
{
   "14E7":{
      "curPow":31.3,
      "maxPow":51.6,
      "energy":184757,
      "timeOn":22366206,
      "timeTot":22366206,
      "sensorId":"14E7"
   }
}
```