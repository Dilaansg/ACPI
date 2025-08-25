# ACPI: Asistente de Cruce Peatonal para Invidentes 🚦👨‍🦯‍➡️

ACPI es un sistema IoT integral diseñado para brindar mayor autonomía y seguridad a personas con discapacidad visual al momento de cruzar intersecciones semaforizadas. El sistema combina hardware dedicado (dispositivos ESP32) y una aplicación móvil inteligente para crear una red de asistencia personal en tiempo real.

![Versión](https://img.shields.io/badge/versión-2.0.5-blue)  
![Estado](https://img.shields.io/badge/estado-en%20prototipo-yellow)  
![Licencia](https://img.shields.io/badge/licencia-MIT-green)<br>
<img width="600" height="355" alt="image" src="https://github.com/user-attachments/assets/3d3c16ef-f4fe-4246-87d1-1e000d5f8a1c" />


---

## Funcionalidades Principales
### El ecosistema de ACPI se compone de tres elementos clave que trabajan en perfecta sincronía:

🧠 1. El Cerebro del Semáforo (Dispositivo ESP32 Fijo)
Misión: Monitorear el estado del semáforo y comunicar cambios al entorno.

Tecnología: Utiliza el protocolo de comunicación inalámbrica ESP-NOW para transmitir datos de forma instantánea y de bajo consumo a la pulsera del usuario.

Información Transmitida: Envía el estado actual del semáforo (ej. ROJO para los coches) y el ángulo de la vía que controla.

👋 2. La Pulsera Inteligente (Dispositivo ESP32 Portátil)
Misión: Ser el punto de contacto físico y de alerta para el usuario.

Comunicación Dual:

Recibe datos del semáforo vía ESP-NOW.

Actúa como un puente, retransmitiendo esta información y comunicándose de forma bidireccional con la aplicación móvil a través de Bluetooth Low Energy (BLE).

Retroalimentación Táctil: Contiene un motor que vibra de forma continua únicamente cuando la aplicación determina que es seguro para el usuario cruzar la calle.

📱 3. La Aplicación Móvil (Android)
Es el centro de control inteligente que procesa toda la información y toma las decisiones de seguridad.

Siempre Activa en Segundo Plano: Funciona como un Foreground Service, garantizando que el monitoreo y la protección no se detengan, incluso con el teléfono en el bolsillo o la pantalla apagada.

Sensores de Alta Precisión:

Brújula Robusta: Utiliza un filtro de media móvil para suavizar las lecturas de los sensores, ofreciendo una dirección de caminata estable y eliminando "deslices" o temblores.

Contador de Pasos Inteligente: Analiza la fuerza y el ritmo del movimiento para contar únicamente los pasos reales.

Lógica de Decisión Inteligente:

La vibración no es una alerta de peligro, sino una señal afirmativa de "permiso para cruzar".

Se activa solo cuando se cumplen dos condiciones: el semáforo es seguro (coches en rojo) y el usuario está orientado para cruzar. Si alguna de las dos falla, la vibración se detiene al instante.

Red de Seguridad y Análisis:

Alertas a Telegram: Envía la ubicación GPS a un contacto de confianza cada 15 minutos y genera una alerta inmediata si el usuario sale de una "zona segura" predefinida.

Recolección de Datos: Guarda un registro completo del trazado de cada viaje en una base de datos de Firebase, permitiendo un análisis posterior.

📊 Panel de Visualización (Dashboard Web)
Para complementar el sistema, se ha desarrollado un panel de control web. Es una página HTML simple que se conecta a la base de datos de Firebase y permite:

Visualizar una lista de todos los viajes registrados.

Seleccionar un viaje y dibujar su trazado completo sobre un mapa interactivo.

Marcar los puntos exactos donde se generaron alertas de salida de la zona segura.

⚙️ Requisitos de Hardware
2 × ESP32 con WiFi y Bluetooth.

Módulo vibrador (motor de vibración).

Batería recargable y módulo de carga para la pulsera.

Smartphone Android con sensores magnéticos y de orientación.
## 🧪 Prototipos Anteriores 
- Antes de llegar a trabajar la versión final, realizamos un prototipo antiguo que consistía en el mismo funcionamiento pero en el ambiente de Microbit - Python. Este era más simple, ya que no contaba con el uso de brújula, ni la aplicación y estaba implementada en un bastón.
🔗 
<img width="494" height="529" alt="image" src="https://github.com/user-attachments/assets/4720d02c-78aa-47c8-bcb3-a7cb67a1c43c" />

(Primer diseño del proyecto, usando microbit e implementandolo en el bastón)

### 📸 Capturas Actuales
<img width="602" height="536" alt="image" src="https://github.com/user-attachments/assets/e34938da-208f-4bd1-ba17-537f4fd0d4ba" /><br>
(Referencia del diseño de la pulsera)

<img width="529" alt="image" src="https://github.com/user-attachments/assets/de0256ff-53d7-4e37-957a-6dc584ca8b16" /><br>
(Componente ESP32)

<img width="802" height="425" alt="image" src="https://github.com/user-attachments/assets/32727e58-22cc-4f1b-8db5-713ccf9a367d" /><br>
(Referencia del diseño de la aplicación                )

<img width="529" height="510" alt="image" src="https://github.com/user-attachments/assets/16a6f681-7dcb-4a7b-b878-278d1721fa58" /><br>
(Referencia del espacio de proyecto)


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

## 🧑‍💻 Autores

Desarrollado por Dilan Osorio, Andrea Cárdenas y Nicolás Rodríguez como parte de un proyecto de grado técnico.  
Asesorado por la docente del área Johanna Carolina Sánchez Ramírez.

---
