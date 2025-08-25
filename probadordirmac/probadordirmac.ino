#include "WiFi.h"

void setup(){
  Serial.begin(115200);
  WiFi.mode(WIFI_STA);
  Serial.println("--- DIRECCIÓN MAC DEL EMISOR ---");
  Serial.println(WiFi.macAddress());
  Serial.println("---------------------------------");
}

void loop(){
  Serial.println("--- DIRECCIÓN MAC DEL EMISOR ---");
  Serial.println(WiFi.macAddress());
}