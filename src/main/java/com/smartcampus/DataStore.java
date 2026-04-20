/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus;

/**
 *
 * @author Rowel
 */

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    // Single shared instance (singleton)
    private static final DataStore INSTANCE = new DataStore();

    // In-memory storage using ConcurrentHashMap for thread safety
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {}

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // Room operations
    public Map<String, Room> getRooms() { return rooms; }

    public Room getRoom(String id) { return rooms.get(id); }

    public void addRoom(Room room) { rooms.put(room.getId(), room); }

    public void deleteRoom(String id) { rooms.remove(id); }

    // Sensor operations
    public Map<String, Sensor> getSensors() { return sensors; }

    public Sensor getSensor(String id) { return sensors.get(id); }

    public void addSensor(Sensor sensor) { sensors.put(sensor.getId(), sensor); }

    public void deleteSensor(String id) { sensors.remove(id); }

    // SensorReading operations
    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
