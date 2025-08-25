// Archivo: ble_test.ino

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// --- CONFIGURACIÓN BLE (Exactamente la misma que en tu app) ---
#define SERVICE_UUID        "12345678-1234-1234-1234-123456789abc"
#define TX_CHAR_UUID        "87654321-4321-4321-4321-cba987654321"

// --- Variables Globales ---
BLEServer* pServer = NULL;
BLECharacteristic* pTxCharacteristic = NULL;
bool deviceConnected = false;
unsigned long lastTime = 0;
int valueToSend = 0;

// --- Callbacks de Conexión ---
class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("BLE: ¡Dispositivo Conectado!");
    }

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("BLE: Dispositivo Desconectado. Reiniciando advertising...");
      pServer->getAdvertising()->start(); // Vuelve a ser visible
    }
};

// --- SETUP ---
void setup() {
  Serial.begin(115200);
  Serial.println("Iniciando prueba de BLE...");

  // 1. Iniciar el dispositivo BLE
  BLEDevice::init("Pulsera_Vibratoria");

  // 2. Crear el servidor BLE
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // 3. Crear el servicio
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // 4. Crear la característica para enviar datos (Notificar)
  pTxCharacteristic = pService->createCharacteristic(
                      TX_CHAR_UUID,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pTxCharacteristic->addDescriptor(new BLE2902());

  // 5. Iniciar el servicio y el advertising
  pService->start();
  pServer->getAdvertising()->start();
  Serial.println("Dispositivo anunciándose como 'Pulsera_Vibratoria'. Esperando conexión...");
}

// --- LOOP PRINCIPAL ---
void loop() {
  // Si un dispositivo está conectado
  if (deviceConnected) {
    // Y ha pasado un segundo desde el último envío
    if (millis() - lastTime >= 1000) {
      
      // Preparamos el mensaje. Usamos el mismo formato "estado,valor"
      char msg[32];
      sprintf(msg, "1,%d", valueToSend); // Estado 1, y el valor del contador

      // Asignamos el valor y notificamos
      pTxCharacteristic->setValue(msg);
      pTxCharacteristic->notify();
      
      Serial.printf("Enviando valor: %s\n", msg);
      
      valueToSend++; // Incrementamos el valor para el próximo envío
      lastTime = millis(); // Actualizamos el tiempo del último envío
    }
  }
  
  delay(50); // Pequeña pausa
}
