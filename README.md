# 🚦 ACPI: Asistente de Cruce Peatonal para Invidentes 👨‍🦯➡️

**ACPI** es un sistema **IoT integral** diseñado para brindar **autonomía y seguridad** a personas con discapacidad visual al momento de cruzar intersecciones semaforizadas.  
El sistema combina **hardware dedicado (ESP32)** y una **aplicación móvil inteligente** para crear una red de asistencia en tiempo real.

![Versión](https://img.shields.io/badge/versión-2.0.5-blue)  
![Estado](https://img.shields.io/badge/estado-en%20prototipo-yellow)  
![Licencia](https://img.shields.io/badge/licencia-MIT-green)  

<img width="300" height="100" alt="image" src="(https://github.com/user-attachments/assets/5ae23bf0-dc78-44a2-8649-234e68ff17b6" />



---

## ✨ Funcionalidades Principales

El ecosistema de **ACPI** se compone de **tres elementos clave** que trabajan en sincronía:

### 🧠 1. Cerebro del Semáforo (ESP32 Fijo)
- **Misión:** Monitorear el estado del semáforo y comunicar cambios.  
- **Tecnología:** Comunicación mediante **ESP-NOW**, rápida y de bajo consumo.  
- **Información transmitida:** Estado del semáforo (ej. 🚦 *rojo para autos*) y ángulo de la vía.  

---

### 👋 2. Pulsera Inteligente (ESP32 Portátil)
- **Misión:** Ser el **punto de contacto y alerta** para el usuario.  
- **Comunicación dual:**  
  - Recibe datos del semáforo vía **ESP-NOW**.  
  - Retransmite información a la app vía **Bluetooth Low Energy (BLE)**.  
- **Retroalimentación táctil:** Vibra únicamente cuando la app determina que es seguro cruzar.  

---

### 📱 3. Aplicación Móvil (Android)
- **Siempre activa:** Corre en **Foreground Service** para funcionar incluso en segundo plano.  
- **Sensores de alta precisión:**  
  - 📍 **Brújula robusta** con filtro de media móvil.  
  - 🚶 **Contador de pasos inteligente** que detecta movimiento real.  
- **Lógica de decisión:**  
  - Vibra **solo si** el semáforo está en rojo para coches **y** el usuario está orientado correctamente.  
- **Red de seguridad:**  
  - Envía ubicación GPS a Telegram cada 15 minutos.  
  - Alerta inmediata si el usuario sale de su **zona segura**.  
- **Base de datos:** Guarda todos los viajes en **Firebase** para análisis posterior.  

---

### 📊 Dashboard Web
Una página **HTML sencilla** conectada a Firebase que permite:  
- Listar todos los viajes registrados.  
- Dibujar el trazado en un **mapa interactivo**.  
- Marcar puntos con alertas de zona insegura.  

---

## ⚙️ Requisitos de Hardware
- 2 × **ESP32** con WiFi y Bluetooth.  
- 1 × **Módulo vibrador (motor de vibración).**  
- 1 × **Batería recargable + módulo de carga** (para la pulsera).  
- **Smartphone Android** con sensores magnéticos y de orientación.  

---

## 🧪 Prototipos Anteriores
Antes de la versión final, existió un **prototipo inicial con Micro:bit y Python**, implementado en un bastón.  
Este era más simple y no incluía brújula ni aplicación móvil.  

<img width="494" height="529" alt="image" src="https://github.com/user-attachments/assets/4720d02c-78aa-47c8-bcb3-a7cb67a1c43c" />  
*(Primer diseño del proyecto en bastón con Micro:bit)*  

---

## 📸 Capturas del Prototipo Actual
| Pulsera | ESP32 | Aplicación | Espacio de trabajo |
|---------|-------|------------|---------------------|
| <img width="250" src="https://github.com/user-attachments/assets/e34938da-208f-4bd1-ba17-537f4fd0d4ba" /> | <img width="250" src="https://github.com/user-attachments/assets/de0256ff-53d7-4e37-957a-6dc584ca8b16" /> | <img width="350" src="https://github.com/user-attachments/assets/32727e58-22cc-4f1b-8db5-713ccf9a367d" /> | <img width="250" src="https://github.com/user-attachments/assets/16a6f681-7dcb-4a7b-b878-278d1721fa58" /> |

---

## 🧩 Estructura del Código

- /sender/ -> Código para el ESP32 en el semáforo
- /pulsera_main/ -> Código para el ESP32 en la pulsera
- /android-app/ -> App Android (Kotlin/Java)
- /dashboard/ -> Panel web conectado a Firebas


---

## 🤝 Contribuciones
Este es un **proyecto académico abierto al aprendizaje**.  
Se agradecen aportes en:  
- 🔋 Eficiencia energética.  
- 🧭 Algoritmos de orientación.  
- 📡 Integración con más sensores.  

---

## 🧑‍💻 Autores
Proyecto desarrollado por:  
###**Dilan Osorio, Andrea Cárdenas y Nicolás Rodríguez**  
📚 Asesoría: *Johanna Carolina Sánchez Ramírez*  
