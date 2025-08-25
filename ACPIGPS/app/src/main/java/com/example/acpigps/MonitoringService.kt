package com.example.acpigps

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Date
import java.util.UUID
import kotlin.math.roundToInt

class MonitoringService : Service(), ConnectionCallback {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "MonitoringServiceChannel"
        const val ACTION_STOP_MONITORING = "ACTION_STOP_MONITORING"
    }

    inner class LocalBinder : Binder() {
        fun getService(): MonitoringService = this@MonitoringService
    }
    private val binder = LocalBinder()

    // --- LiveData para la UI ---
    private val _bleConnectionState = MutableLiveData(BleConnectionState())
    val bleConnectionState: LiveData<BleConnectionState> = _bleConnectionState
    private val _compassData = MutableLiveData(CompassData())
    val compassData: LiveData<CompassData> = _compassData
    private val _logMessages = MutableLiveData<List<String>>(emptyList())
    val logMessages: LiveData<List<String>> = _logMessages

    // --- Managers y Clientes ---
    private lateinit var bleManager: BLEManager
    private lateinit var directionDetector: WalkDirectionDetector
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var telegramManager: TelegramManager
    private lateinit var sharedPreferences: SharedPreferences

    // --- Variables de Firebase ---
    private lateinit var db: FirebaseFirestore
    private var tripId: String? = null

    // --- Variables de Estado ---
    private var isServiceRunning = false
    private var stepCount = 0
    private var homeLocation: Location? = null
    private val safeRadius = 50.0
    private val locationUpdateHandler = Handler(Looper.getMainLooper())
    private lateinit var locationSenderRunnable: Runnable
    private val locationUpdateInterval = 15 * 60 * 1000L // 15 minutos

    override fun onCreate() {
        super.onCreate()
        // Inicializar todos los componentes
        bleManager = BLEManager(this)
        bleManager.connectionCallback = this
        directionDetector = WalkDirectionDetector(this) { angle, direction ->
            stepCount++
            _compassData.postValue(CompassData(angle, direction, stepCount))
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        telegramManager = TelegramManager(this)
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Inicializar Firebase
        db = Firebase.firestore

        setupLocationCallback()
        setupLocationSender()
        loadHomeLocation()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_MONITORING) {
            stopMonitoring()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, createNotification("Iniciando monitoreo..."))
        startMonitoring()
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun startMonitoring() {
        if (isServiceRunning) return
        addLog("Servicio de monitoreo iniciado.")

        // Crear un ID único para este viaje/sesión
        tripId = UUID.randomUUID().toString()
        addLog("Nuevo ID de viaje: $tripId")

        stepCount = 0
        directionDetector.startDetection()
        bleManager.startScan()
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 5000
            },
            locationCallback,
            Looper.getMainLooper()
        )
        isServiceRunning = true
        locationUpdateHandler.post(locationSenderRunnable)
    }

    private fun stopMonitoring() {
        if (!isServiceRunning) return
        addLog("Servicio de monitoreo detenido.")
        locationUpdateHandler.removeCallbacks(locationSenderRunnable)
        directionDetector.stopDetection()
        bleManager.sendCommandToEsp32("VIBRATE_STOP")
        bleManager.disconnect()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isServiceRunning = false
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder = binder

    // --- Lógica de Telegram y Geofence ---

    private fun loadHomeLocation() {
        val lat = sharedPreferences.getString("home_lat", null)?.toDouble()
        val lon = sharedPreferences.getString("home_lon", null)?.toDouble()
        if (lat != null && lon != null) {
            homeLocation = Location("").apply {
                latitude = lat
                longitude = lon
            }
            addLog("Ubicación segura cargada.")
        } else {
            addLog("No hay ubicación segura guardada.")
        }
    }

    private fun checkGeofence(currentLocation: Location) {
        homeLocation?.let { home ->
            val distance = currentLocation.distanceTo(home)
            if (distance > safeRadius) {
                val message = "¡Alerta! El usuario ha salido de la zona segura. Distancia: ${distance.roundToInt()} metros."
                telegramManager.sendMessage(message) { log ->
                    addLog("Enviando alerta de geofence: $log")
                }
                // Guardar el evento de geocerca en Firebase
                saveLocationToFirebase(currentLocation, "GEOFENCE_EXIT")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupLocationSender() {
        locationSenderRunnable = object : Runnable {
            override fun run() {
                if (isServiceRunning) {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            telegramManager.sendLocation(location.latitude, location.longitude) { log ->
                                addLog("Envío periódico de ubicación: $log")
                            }
                        }
                    }
                    // Volver a programar el envío
                    locationUpdateHandler.postDelayed(this, locationUpdateInterval)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun saveCurrentLocationAsHome() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                homeLocation = location
                with(sharedPreferences.edit()) {
                    putString("home_lat", location.latitude.toString())
                    putString("home_lon", location.longitude.toString())
                    apply()
                }
                val logMessage = "Ubicación segura guardada: Lat: ${location.latitude}, Lon: ${location.longitude}"
                addLog(logMessage)
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "Ubicación segura guardada.", Toast.LENGTH_SHORT).show()
                }
            } else {
                addLog("Error: No se pudo obtener la ubicación actual para guardarla.")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    checkGeofence(location)
                    // Guardar cada punto del trazado en Firebase
                    saveLocationToFirebase(location, "TRACK")
                }
            }
        }
    }

    // --- NUEVA FUNCIÓN PARA GUARDAR EN FIREBASE ---
    private fun saveLocationToFirebase(location: Location, eventType: String) {
        if (tripId == null) return // No guardar si no hay un viaje activo

        val locationData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "timestamp" to Date(), // Guarda la fecha y hora actual
            "event" to eventType // "TRACK" o "GEOFENCE_EXIT"
        )

        db.collection("trips").document(tripId!!)
            .collection("points").add(locationData)
            .addOnSuccessListener {
                Log.d("Firestore", "Punto de ubicación guardado con éxito.")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error al guardar el punto de ubicación.", e)
                addLog("Error de Firestore: ${e.message}")
            }
    }

    // --- Callbacks y Métodos Existentes ---

    override fun onTrafficLightAlert(estado: Int, angulo: Int) {
        addLog("Alerta de semáforo recibida. Estado para coches: $estado, Angulo: $angulo")
        val userQuadrant = directionDetector.getCurrentQuadrant()

        if (estado == 1 && isCrossingPathAligned(angulo, userQuadrant)) {
            addLog("ACCIÓN: ¡SEGURO PARA CRUZAR! Iniciando vibración.")
            bleManager.sendCommandToEsp32("VIBRATE_START")
        } else {
            addLog("INFO: Condición no segura. Deteniendo vibración.")
            bleManager.sendCommandToEsp32("VIBRATE_STOP")
        }

        bleManager.sendQuadrantToEsp32(userQuadrant)
    }

    private fun isCrossingPathAligned(trafficLightAngle: Int, userQuadrant: Int): Boolean {
        val trafficLightQuadrant = when (trafficLightAngle) {
            in 45..134 -> 1; in 135..224 -> 2; in 225..314 -> 3; else -> 4
        }
        return when (trafficLightQuadrant) {
            1, 3 -> userQuadrant == 2 || userQuadrant == 4
            2, 4 -> userQuadrant == 1 || userQuadrant == 3
            else -> false
        }
    }

    override fun onConnectionStateChanged(isConnected: Boolean, deviceName: String?) {
        val currentState = _bleConnectionState.value ?: BleConnectionState()
        _bleConnectionState.postValue(currentState.copy(isConnected = isConnected, deviceName = deviceName, isScanning = false))
        updateNotification(if (isConnected) "Conectado a ${deviceName ?: "dispositivo"}" else "Buscando dispositivo...")
    }

    override fun onScanningStateChanged(isScanning: Boolean) {
        val currentState = _bleConnectionState.value ?: BleConnectionState()
        _bleConnectionState.postValue(currentState.copy(isScanning = isScanning))
    }

    override fun logMessage(message: String) { addLog(message) }

    private fun createNotification(text: String): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Canal de Monitoreo", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val stopIntent = Intent(this, MonitoringService::class.java).apply { action = ACTION_STOP_MONITORING }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ACPI GPS Monitoreando")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_media_pause, "Detener", stopPendingIntent)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = createNotification(text)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notification)
    }

    private fun addLog(message: String) {
        val currentLogs = _logMessages.value?.toMutableList() ?: mutableListOf()
        currentLogs.add(0, message)
        _logMessages.postValue(currentLogs)
    }

    fun sendTestToEsp32() {
        val quadrant = directionDetector.getCurrentQuadrant()
        bleManager.sendQuadrantToEsp32(quadrant)
        addLog("Enviando cuadrante de prueba manual: $quadrant")
    }
}
