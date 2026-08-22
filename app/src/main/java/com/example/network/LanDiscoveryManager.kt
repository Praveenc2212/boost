package com.example.network

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.example.model.DiscoveredRoom
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.NetworkInterface

class LanDiscoveryManager(private val context: Context) {
    private val TAG = "LanDiscovery"
    private var broadcastJob: Job? = null
    private var listenJob: Job? = null
    private var multicastLock: WifiManager.MulticastLock? = null

    private val _discoveredRooms = MutableStateFlow<List<DiscoveredRoom>>(emptyList())
    val discoveredRooms = _discoveredRooms.asStateFlow()

    private val knownRoomsMap = mutableMapOf<String, DiscoveredRoom>()

    init {
        try {
            val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifi?.createMulticastLock("BoostLanMulticast")
            multicastLock?.setReferenceCounted(true)
        } catch (e: Exception) {
            Log.e(TAG, "MulticastLock init error", e)
        }
    }

    fun startHostingBroadcast(
        scope: CoroutineScope,
        roomCode: String,
        roomName: String,
        hostName: String,
        playerCountProvider: () -> Int
    ) {
        stopHostingBroadcast()
        broadcastJob = scope.launch(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                socket.broadcast = true
                while (isActive) {
                    val message = NetworkProtocol.createBeacon(
                        roomCode = roomCode,
                        roomName = roomName,
                        hostName = hostName,
                        currentPlayers = playerCountProvider(),
                        port = NetworkProtocol.TCP_GAME_PORT
                    )
                    val bytes = message.toByteArray(Charsets.UTF_8)
                    
                    // Broadcast to 255.255.255.255
                    try {
                        val packet = DatagramPacket(
                            bytes,
                            bytes.size,
                            InetAddress.getByName("255.255.255.255"),
                            NetworkProtocol.UDP_DISCOVERY_PORT
                        )
                        socket.send(packet)
                    } catch (e: Exception) {
                        // ignore packet error
                    }

                    // Also broadcast to interface-specific broadcast addresses
                    getBroadcastAddresses().forEach { address ->
                        try {
                            val packet = DatagramPacket(
                                bytes,
                                bytes.size,
                                address,
                                NetworkProtocol.UDP_DISCOVERY_PORT
                            )
                            socket.send(packet)
                        } catch (e: Exception) {
                            // ignore
                        }
                    }

                    delay(1000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Broadcasting error", e)
            } finally {
                socket?.close()
            }
        }
    }

    fun stopHostingBroadcast() {
        broadcastJob?.cancel()
        broadcastJob = null
    }

    fun startScanning(scope: CoroutineScope) {
        stopScanning()
        try {
            multicastLock?.acquire()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire multicast lock", e)
        }

        knownRoomsMap.clear()
        _discoveredRooms.value = emptyList()

        listenJob = scope.launch(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(null).apply {
                    reuseAddress = true
                    bind(InetSocketAddress(NetworkProtocol.UDP_DISCOVERY_PORT))
                    soTimeout = 2000
                }

                val buffer = ByteArray(2048)
                val packet = DatagramPacket(buffer, buffer.size)

                while (isActive) {
                    try {
                        socket.receive(packet)
                        val text = String(packet.data, packet.offset, packet.length, Charsets.UTF_8)
                        val senderIp = packet.address.hostAddress ?: "127.0.0.1"

                        val room = NetworkProtocol.parseBeacon(text, senderIp)
                        if (room != null) {
                            val key = "${room.hostIp}:${room.roomCode}"
                            knownRoomsMap[key] = room
                            
                            // Prune rooms not seen in > 5 seconds
                            val now = System.currentTimeMillis()
                            val activeRooms = knownRoomsMap.values.filter { now - it.lastSeenTimestamp < 6000 }
                            _discoveredRooms.value = activeRooms.sortedByDescending { it.lastSeenTimestamp }
                        }
                    } catch (e: java.net.SocketTimeoutException) {
                        // Clean up stale rooms on timeout tick
                        val now = System.currentTimeMillis()
                        val activeRooms = knownRoomsMap.values.filter { now - it.lastSeenTimestamp < 6000 }
                        if (activeRooms.size != _discoveredRooms.value.size) {
                            _discoveredRooms.value = activeRooms
                        }
                    } catch (e: Exception) {
                        if (isActive) {
                            delay(500)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Discovery listener error", e)
            } finally {
                socket?.close()
            }
        }
    }

    fun stopScanning() {
        listenJob?.cancel()
        listenJob = null
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "MulticastLock release error", e)
        }
    }

    private fun getBroadcastAddresses(): List<InetAddress> {
        val addresses = mutableListOf<InetAddress>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                for (interfaceAddress in networkInterface.interfaceAddresses) {
                    val broadcast = interfaceAddress.broadcast
                    if (broadcast != null) {
                        addresses.add(broadcast)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting broadcast addresses", e)
        }
        return addresses
    }
}
