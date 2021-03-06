/*
 * The MIT License
 *
 * Copyright 2017 Tuupertunut.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.tuupertunut.openhardwaremonitorwrapper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Root object of a hardware and sensor tree. Also the entry point to this
 * library. A computer contains a tree of hardware, subhardware and sensors,
 * which can be monitored.
 *
 * A computer can be opened for hardware monitoring with
 * {@link #open(java.nio.file.Path)}. A user needs administrator privileges and
 * must have a copy of OpenHardwareMonitorLib.dll somewhere on the file system
 * to use this library. When no longer used, instances should be closed with
 * {@link #close()} to free resources.
 *
 * @author Tuupertunut
 */
public class Computer implements AutoCloseable {

    private PowerShellService psService;

    /* Maintaining a mapping of identifiers to sensors so future sensor value
     * updates can be connected to the right sensors. */
    private final Map<String, Sensor> allSensorsByIdentifier;

    private final List<Hardware> hardware;

    private Computer(Path ohmLibPath) throws IOException {
        try {
            psService = new PowerShellService(ohmLibPath);

            allSensorsByIdentifier = new HashMap<>();

            List<String> hardwareResponse = psService.openComputer();
            hardware = parseAllHardware(hardwareResponse.iterator(), Optional.empty());

        } catch (IOException | RuntimeException ex) {
            close();
            throw ex;
        }
    }

    /* Recursively parsing the JSON-like PowerShell output into an object tree. */
    private List<Hardware> parseAllHardware(Iterator<String> hardwareRowIterator, Optional<Hardware> parent) {
        List<Hardware> hardware = new ArrayList<>();

        /* Jumping over the hardware array opening bracket [ */
        hardwareRowIterator.next();

        /* Either jumping over the hardware array closing bracket ] or a
         * hardware item opening brace { */
        while (!hardwareRowIterator.next().equals("]")) {

            String identifier = hardwareRowIterator.next();
            String name = hardwareRowIterator.next();
            String hardwareType = hardwareRowIterator.next();

            Hardware hardwareItem = new Hardware(identifier, name, hardwareType, parent, this);
            hardware.add(hardwareItem);

            /* Recursively parsing the subhardware with the same iterator. */
            List<Hardware> subHardware = parseAllHardware(hardwareRowIterator, Optional.of(hardwareItem));
            for (Hardware sub : subHardware) {
                hardwareItem.addSubHardware(sub);
            }

            /* Jumping over the sensors array opening bracket [ */
            hardwareRowIterator.next();

            /* Either jumping over the sensors array closing bracket ] or a
             * sensor opening brace { */
            while (!hardwareRowIterator.next().equals("]")) {

                String sensorIdentifier = hardwareRowIterator.next();
                String sensorName = hardwareRowIterator.next();
                String sensorType = hardwareRowIterator.next();
                float sensorValue = Float.parseFloat(hardwareRowIterator.next());

                Optional<Control> control;

                /* Either jumping over the control opening brace { or the empty
                 * space if there was no control. */
                if (hardwareRowIterator.next().equals("{")) {

                    String controlIdentifier = hardwareRowIterator.next();
                    float controlMinSoftwareValue = Float.parseFloat(hardwareRowIterator.next());
                    float controlMaxSoftwareValue = Float.parseFloat(hardwareRowIterator.next());

                    control = Optional.of(new Control(controlIdentifier, controlMinSoftwareValue, controlMaxSoftwareValue, this));

                    /* Jumping over the control closing brace } */
                    hardwareRowIterator.next();
                } else {
                    control = Optional.empty();
                }

                Sensor sensor = new Sensor(sensorIdentifier, sensorName, sensorType, hardwareItem, sensorValue, control, this);
                hardwareItem.addSensor(sensor);

                allSensorsByIdentifier.put(sensorIdentifier, sensor);

                /* Jumping over the sensor closing brace } */
                hardwareRowIterator.next();
            }

            /* Jumping over the hardware item closing brace } */
            hardwareRowIterator.next();
        }
        return hardware;
    }

    /**
     * Opens a new hardware monitoring session with the provided
     * OpenHardwareMonitorLib. Admin privileges are required for this action.
     *
     * @param ohmLibPath path to OpenHardwareMonitorLib.dll.
     * @return a Computer object, representing the hardware of the computer.
     * @throws IOException if an IOException occurs in the communication with
     * the OpenHardwareMonitor.
     * @throws InsufficientPermissionsException if the user does not have
     * administrator privileges.
     * @throws LibraryLoadException if loading the library dll fails.
     */
    public static Computer open(Path ohmLibPath) throws IOException {
        return new Computer(ohmLibPath);
    }

    /**
     * Closes this hardware monitoring session and frees all resources
     * associated with it.
     */
    @Override
    public void close() {
        if (psService != null) {
            psService.close();
            psService = null;
        }
    }

    /**
     * Returns a list of all top level hardware in this computer. Does not
     * include subhardware of top level hardware.
     *
     * @return a list of all top level hardware in this computer.
     */
    public List<Hardware> getHardware() {
        return Collections.unmodifiableList(hardware);
    }

    /**
     * Measures and updates new values to all sensors in this computer. Sensors
     * of subhardware will also be updated.
     *
     * @throws IOException if an IOException occurs in the communication with
     * the OpenHardwareMonitor.
     * @throws IllegalStateException if this computer has already been closed.
     */
    public void updateAllHardware() throws IOException {
        if (psService == null) {
            throw new IllegalStateException("This computer has been closed.");
        }

        List<String> sensorResponse = psService.updateAllHardware();
        Iterator<String> sensorRowIterator = sensorResponse.iterator();

        /* Jumping over the sensors array opening bracket [ */
        sensorRowIterator.next();

        /* Either jumping over the sensors array closing bracket ] or a sensor
         * opening brace { */
        while (!sensorRowIterator.next().equals("]")) {

            String sensorIdentifier = sensorRowIterator.next();
            float sensorValue = Float.parseFloat(sensorRowIterator.next());

            allSensorsByIdentifier.get(sensorIdentifier).setValue(sensorValue);

            /* Jumping over the sensor closing brace } */
            sensorRowIterator.next();
        }
    }

    /* Internal methods that other hardware and sensors can use. */
    void updateHardware(String hardwareIdentifier) throws IOException {
        if (psService == null) {
            throw new IllegalStateException("This computer has been closed.");
        }

        List<String> sensorResponse = psService.updateHardware(hardwareIdentifier);
        Iterator<String> sensorRowIterator = sensorResponse.iterator();

        /* Jumping over the sensors array opening bracket [ */
        sensorRowIterator.next();

        /* Either jumping over the sensors array closing bracket ] or a sensor
         * opening brace { */
        while (!sensorRowIterator.next().equals("]")) {

            String sensorIdentifier = sensorRowIterator.next();
            float sensorValue = Float.parseFloat(sensorRowIterator.next());

            allSensorsByIdentifier.get(sensorIdentifier).setValue(sensorValue);

            /* Jumping over the sensor closing brace } */
            sensorRowIterator.next();
        }
    }

    void setControlDefault(String controlIdentifier) throws IOException {
        if (psService == null) {
            throw new IllegalStateException("This computer has been closed.");
        }

        psService.setControlDefault(controlIdentifier);
    }

    void setControlSoftware(String controlIdentifier, float value) throws IOException {
        if (psService == null) {
            throw new IllegalStateException("This computer has been closed.");
        }

        psService.setControlSoftware(controlIdentifier, value);
    }
}
