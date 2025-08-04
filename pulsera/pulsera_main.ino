#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <esp_now.h>
#include <WiFi.h>
#include "estructura.h" // Incluimos la estructura de datos compartida

// --- Variables Globales ---
esp_now_data_structure datosSemaforo; // Almacena datos recibidos de ESP-NOW
volatile bool nuevosDatosEspNow = false;

portMUX_TYPE mux = portMUX_INITIALIZER_UNLOCKED;

// Variables para BLE
BLEServer* pServer = NULL;
BLECharacteristic* pTxCharacteristic;
bool deviceConnected = false;

// --- Callbacks ---

// Callback para cuando se reciben datos por ESP-NOW.
// CORREGIDO: La firma de la función ahora coincide con las versiones más recientes de la librería ESP-NOW.
void OnDataRecv(const esp_now_recv_info * info, const uint8_t *incomingData, int len) {
  // El primer parámetro 'info' contiene metadatos como la dirección MAC del emisor.
  // Por ahora, solo nos interesa la carga de datos (incomingData).
  
  // Copiamos los datos solo si el tamaño coincide con nuestra estructura.
  if (len == sizeof(datosSemaforo)) {
    portENTER_CRITICAL_ISR(&mux);
    memcpy(&datosSemaforo, incomingData, sizeof(datosSemaforo));
    nuevosDatosEspNow = true; // Activamos la bandera para que el loop procese los datos
    portEXIT_CRITICAL_ISR(&mux);
  }
}

// Callbacks para el estado de la conexión del servidor BLE
class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("BLE: Cliente Conectado");
    }

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("BLE: Cliente Desconectado, reiniciando advertising...");
      pServer->getAdvertising()->start();
    }
};

void setup() {
  Serial.begin(115200);

  // --- Configuración de ESP-NOW ---
  WiFi.mode(WIFI_STA);
  if (esp_now_init() != ESP_OK) {
    Serial.println("Error inicializando ESP-NOW");
    return;
  }
  // Registramos el callback de recepción
  esp_now_register_recv_cb(OnDataRecv);

  // --- Configuración de BLE ---
  BLEDevice::init("ESP32_Cerebro_IoT");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());
  
  BLEService *pService = pServer->createService("12345678-1234-1234-1234-123456789abc");

  // Característica de Notificación (ESP32 -> App) para enviar el ángulo
  pTxCharacteristic = pService->createCharacteristic(
                      "87654321-4321-4321-4321-cba987654321",
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
  pTxCharacteristic->addDescriptor(new BLE2902());

  // NOTA: La característica de escritura (App -> ESP32) se añadirá en el siguiente paso.

  pService->start();
  pServer->getAdvertising()->start();
  Serial.println("BLE: Esperando conexión del cliente...");
}

void loop() {
  esp_now_data_structure datosActuales;
  bool hayDatosNuevos = false;

  // Copiamos los datos de forma segura si la bandera está activa
  portENTER_CRITICAL(&mux);
  if (nuevosDatosEspNow) {
    memcpy(&datosActuales, &datosSemaforo, sizeof(datosSemaforo));
    nuevosDatosEspNow = false; // Reseteamos la bandera para la próxima recepción
    hayDatosNuevos = true;
  }
  portEXIT_CRITICAL(&mux);

  // Si hemos copiado datos nuevos, los procesamos
  if (hayDatosNuevos) {
    Serial.printf("ESP-NOW: Datos recibidos -> Estado: %d, Ángulo: %d\n", datosActuales.estado, datosActuales.angulo);

    /*
    // --- LÓGICA DE PROXIMIDAD (RSSI) ---
    // En este paso, la lógica de RSSI está comentada para probar la comunicación base.
    // Se activará en el siguiente paso. Por ahora, siempre intentaremos notificar.
    // if (rssi > -70) { ... }
    */

    // Si hay un dispositivo BLE conectado, enviamos la notificación
    if (deviceConnected) {
      Serial.printf("BLE: Cliente conectado. Notificando ángulo: %d\n", datosActuales.angulo);
      
      // Preparamos el ángulo como un solo byte para ser enviado
      uint8_t anguloNotificacion = (uint8_t)datosActuales.angulo;
      pTxCharacteristic->setValue(&anguloNotificacion, 1);
      pTxCharacteristic->notify(); // ¡Enviamos la notificación!

    } else {
      Serial.println("BLE: No hay cliente conectado, no se puede notificar.");
    }
  }
  
  // Pequeña pausa para que el sistema respire
  delay(50);
}
