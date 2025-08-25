#include <esp_now.h>
#include <WiFi.h>

// Callback con firma nueva compatible con ESP32 core 3.x o superior
void OnMessageReceived(const esp_now_recv_info_t *mac_info, const uint8_t* data, int len) {
  float receivedFloat;
  memcpy(&receivedFloat, data, sizeof(float));

  Serial.print("Received from MAC: ");
  for (int i = 0; i < 6; ++i) {
    Serial.printf("%02X", mac_info->src_addr[i]);
    if (i < 5) Serial.print(":");
  }

  Serial.print(" - Value: ");
  Serial.println(receivedFloat);
}

void initEspNow() {
  if (esp_now_init() != ESP_OK) {
    Serial.println("Error initializing ESP-NOW");
    return;
  }

  Serial.println("ESP-NOW Initialized and ready to receive.");

  // Registra el callback con la firma actual (esp_now_recv_info_t)
  esp_now_register_recv_cb(OnMessageReceived);
}

void setup() {
  Serial.begin(115200);
  delay(2000); // Espera para conectar el monitor serial

  WiFi.mode(WIFI_STA); // Modo estación
  Serial.print("Receiver MAC Address: ");
  Serial.println(WiFi.macAddress());

  initEspNow();
}

void loop() {
  // El receptor solo espera datos por callback
} //ESTE ES EL CODIO FUNCIONAL DEL RECEPTOR

