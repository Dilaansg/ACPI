# ACPI: Asistente de Cruce Peatonal para Invidentes

Este proyecto tiene como objetivo brindar mayor autonomía y seguridad a personas invidentes al momento de cruzar una calle con semáforo. Utiliza dos dispositivos **ESP32**: uno ubicado en el semáforo y otro en una pulsera o dispositivo portátil del usuario invidente. También se emplea una aplicación móvil Android que actúa como brújula, rastreador y puente de comunicación con familiares.

---

## 🧠 Funcionalidades principales

### 📡 Comunicación entre ESP32 (ESP-NOW)
- El ESP32 del semáforo transmite el estado del semáforo (verde/rojo) mediante **ESP-NOW**.
- El ESP32 de la pulsera vibra al recibir señal de un semáforo cercano (~10 m de radio).
- Señal de doble vibración (`tin-tin`) indica que un semáforo ha sido detectado.

### 📱 Interacción con el celular (BLE + sensores)
- El ESP32 de la pulsera se conecta vía **Bluetooth Low Energy (BLE)** con el celular.
- El celular:
  - Detecta la orientación para saber si el usuario está bien ubicado para cruzar.
  - Informa si el usuario debe esperar o puede avanzar.
  - Permanece en el bolsillo y actúa como brújula de fondo.

### 📍 Localización y seguridad
- La app Android envía automáticamente la ubicación en tiempo real cada 15 minutos vía Telegram a un familiar o contacto de confianza.
- Se notifica la salida del usuario de un perímetro definido (como el hogar).

---

## ⚙️ Requisitos de hardware

- 2 × ESP32 con WiFi + Bluetooth
- Módulo vibrador (motor de vibración)
- Batería recargable + módulo de carga
- Protoboard o PCB impreso
- Smartphone Android con sensores magnéticos y de orientación
- Opcional: sensor de luz para detectar directamente el semáforo si se desea extender

---

## 🧩 Estructura del código

- `/esp32-semaforo/`: Código para el ESP32 ubicado en el semáforo.
- `/esp32-pulsera/`: Código para el ESP32 del usuario.
- `/android-app/`: Aplicación Android Studio (Kotlin/Java).
- `/docs/`: Diagramas de flujo, esquemas eléctricos y documentación adicional.

---

## 🚧 Problemas actuales / En desarrollo

- 🧠 Compatibilidad de ESP-NOW con BLE de forma simultánea (consumo de memoria y fallos de compilación).
- 📦 Optimización del código para reducir el uso de memoria flash del ESP32.
- 📏 Precisión de brújula en el bolsillo (recalibración de sensores, orientación no horizontal).
- 🔋 Gestión eficiente de energía en la pulsera.

---

## ✅ To-Do inmediato (04/08)
- [ ] Probar ESP-NOW + BLE funcionando simultáneamente.
- [ ] Conectar motor vibrador a la protoboard y verificar señal de vibración.
- [ ] Realizar prueba básica de detección de semáforo y respuesta con vibración.
- [ ] Verificar lectura de orientación del celular desde el bolsillo.

---

## 🤝 Contribuciones

Este es un proyecto académico abierto al aprendizaje. Puedes contribuir con mejoras en eficiencia energética, algoritmos de orientación, o integración con más sensores. ¡Toda ayuda es bienvenida!

---

## 🧑‍💻 Autor

Desarrollado por Dilan Sanit como parte de un proyecto de grado técnico.  
Asesorado por profesores del área tecnológica.

---
