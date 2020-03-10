#include <SPI.h>
#include <Ethernet.h>
#include <PubSubClient.h>

EthernetClient client;
EthernetClient client1;
PubSubClient mqttClient(client1);
byte mac[] = { 0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xEF };

void subscribeReceive(char* topic, byte* payload, unsigned int length);

const char* server = "farmer.cloudmqtt.com";
String username = "kqcvyxsi";
String password = "Agap2a76cMgn";
//const char* server = "broker.example.com"; //test server

const char hueHubIP[] = "192.168.20.223";  // Hue hub IP  // ÄNDRA
const char hueUsername[] = "vBcEi8JwuI4CJB2cxP-qQK4gyjpalIq2MwA44aCk";  // Hue username  // ÄNDRA
const int hueHubPort = 80;

boolean hueOn;  // on/off
int hueBri;  // brightness value
long hueHue;  // hue value

char cstr1[64];
String str1;
char cstr2[64];
String str2;
char cstr3[64];
String str3;

String msgString;
void setup() {
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  if (Ethernet.begin(mac) == 0) {
  
    }
  else{
    Serial.println(Ethernet.localIP());
    }
 Ethernet.begin(mac);
 delay(100);
  
 mqttClient.setServer(server, 10561);   //MQTT SERVER

  if (mqttClient.connect("myClientID", "gwgepsxo", "4jpseV2ARi7y")) 
  {
    Serial.println(F("Connection has been established, well done"));
 
    // Establish the subscribe event
    mqttClient.setCallback(subscribeReceive);
 
  } 
  else 
  {
    Serial.println(F("Looks like the server connection failed..."));
  }
}
void loop() {
  if (!mqttClient.connected()) {
    reconnect();
  }
    mqttClient.loop();
    mqttClient.subscribe("pp4");
   
    //1
    if(msgString.endsWith("X")){ 
    int lastIndex = msgString.length()-1;
    msgString.remove(lastIndex);
    Serial.println(msgString);
    setHue(1,msgString);

    //2
 } else if(msgString.endsWith("Y")){
    int lastIndex = msgString.length()-1;
    msgString.remove(lastIndex);
    Serial.println(msgString);
    setHue(2,msgString);

    //3
 } else if(msgString.endsWith("Z")){
    int lastIndex = msgString.length()-1;
    msgString.remove(lastIndex);
    Serial.println(msgString);
    setHue(3,msgString);

    //all
 }else if(msgString.endsWith("A")){
    int lastIndex = msgString.length()-1;
    msgString.remove(lastIndex);
    Serial.println(msgString);
    setHue(1,msgString);
    setHue(2,msgString);
    setHue(3,msgString);
 }
    GetHue(1);
    mqttClient.publish("pp3", cstr1,true);
    delay(1000);  
    GetHue(2);
    mqttClient.publish("pp3", cstr2,true);
    delay(1000);
    GetHue(3);
    mqttClient.publish("pp3", cstr3,true);
    delay(1000);
}

void subscribeReceive(char* topic, byte* payload, unsigned int length)
{
  String msgIN = "";
  // Print the topic
  Serial.print(F("Topic: "));
  Serial.println(topic);
 
  // Print the message
  //Serial.print(F("Message: "));
  for(int i = 0; i < length; i ++)
  {
   // Serial.print(char(payload[i]));
    msgIN += (char)payload[i];
    
  }  
  msgString = msgIN;
  Serial.print("Message2 = ");
  Serial.println(msgString);
}

 boolean GetHue(int lightNum)
{

  if (client.connect(hueHubIP, hueHubPort))
  {
    client.print(F("GET /api/"));
    client.print(hueUsername);
    client.print(F("/lights/"));
    client.print(lightNum);  // hueLight zero based, add 1
    client.println(F(" HTTP/1.1"));
    client.print(F("Host: "));
    client.println(hueHubIP);
    client.println(F("Content-type: application/json"));
    client.println(F("keep-alive"));
    client.println();
    while (client.connected())
    {
      if (client.available())
      {
        
        client.findUntil("\"on\":",'\0');
        hueOn = (client.readStringUntil(',') == "true");  // if light is on, set variable to true
 
      //  Serial.print("light on ") ;Serial.println(hueOn);
        
        client.findUntil("\"bri\":",'\0');
        hueBri = client.readStringUntil(',').toInt();  // set variable to brightness value
 
        //Serial.print("Brightness ") ;Serial.println(hueBri);
        
        client.findUntil("\"hue\":", '\0');
        hueHue = client.readStringUntil(',').toInt();  // set variable to hue value
       // Serial.print("Color ") ;Serial.println(hueHue);
       
      //get
        String strO="L1-Lgh: ";
        String strB=" Bri: ";
        String strC=" Co: ";
        String strO1="L2-Lgh: ";
        String strO2="L3-Lgh: ";
        str1 = String(strO+hueOn+strB+hueBri+strC+hueHue);
        str2 = String(strO1+hueOn+strB+hueBri+strC+hueHue);
        str3 = String(strO2+hueOn+strB+hueBri+strC+hueHue);
        str1.toCharArray(cstr1,64); 
        str2.toCharArray(cstr2,64);
        str3.toCharArray(cstr3,64); 
         
           
        break;  // not capturing other light attributes yet
      }
    }
    client.stop();
    return true;  // captured on,bri,hue
  }
  else
    return false;  // error reading on,bri,hue
}
boolean setHue(int lightNum,String command)
{
  if (client.connect(hueHubIP, hueHubPort))
  {
  
    while (client.connected())
    {
      client.print(F("PUT /api/"));
      client.print(hueUsername);
      client.print(F("/lights/"));
      client.print(lightNum);  // hueLight zero based, add 1
      client.println(F("/state HTTP/1.1"));
      client.println(F("keep-alive"));
      client.print(F("Host: "));
      client.println(hueHubIP);
      client.print(F("Content-Length: "));
      client.println(command.length());
      client.println(F("Content-Type: text/plain;charset=UTF-8"));
      client.println();  // blank line before body
      client.println(command);  // Hue command
    }
    client.stop();
    return true;  // command executed
  }
  else
  
    return false;  // command failed
}

void reconnect() {
  // Loop until we're reconnected
  while (!mqttClient.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (mqttClient.connect("myClientID", "gwgepsxo", "4jpseV2ARi7y")) {
      Serial.println("connected");
      // ... and resubscribe
      mqttClient.subscribe("pp4");
    } else {
      Serial.print("failed, rc=");
      Serial.print(mqttClient.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

