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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Sensor that measures the value of some property of the hardware. For example,
 * a temperature sensor. It holds a measured value, and remembers the smallest
 * and largest value during the measurement period. Some sensors are
 * controllable, meaning that the value can be changed with the associated
 * Control object.
 *
 * The sensor values are not automatically updated real time, but need to be
 * manually updated by calling Hardware.update() or
 * Computer.updateAllHardware().
 *
 * @author Tuupertunut
 */
public class Sensor {

    /* These mappings are not available in the OpenHardwareMonitor API, so they
     * are managed manually. */
    private static final Map<String, String> KNOWN_MEASUREMENT_UNITS = new HashMap<>();

    static {
        KNOWN_MEASUREMENT_UNITS.put("Voltage", "V");
        KNOWN_MEASUREMENT_UNITS.put("Clock", "MHz");
        KNOWN_MEASUREMENT_UNITS.put("Temperature", "°C");
        KNOWN_MEASUREMENT_UNITS.put("Load", "%");
        KNOWN_MEASUREMENT_UNITS.put("Fan", "RPM");
        KNOWN_MEASUREMENT_UNITS.put("Flow", "L/h");
        KNOWN_MEASUREMENT_UNITS.put("Control", "%");
        KNOWN_MEASUREMENT_UNITS.put("Level", "%");
        KNOWN_MEASUREMENT_UNITS.put("Factor", "");
        KNOWN_MEASUREMENT_UNITS.put("Power", "W");
        KNOWN_MEASUREMENT_UNITS.put("Data", "GiB");
        KNOWN_MEASUREMENT_UNITS.put("SmallData", "MiB");
    }

    private final Computer computer;

    private final String identifier;
    private final String name;
    private final String sensorType;
    private final Hardware hardware;
    private float value;
    private float min;
    private float max;
    private final Optional<Control> control;

    Sensor(String identifier, String name, String sensorType, Hardware hardware, float value, Optional<Control> control, Computer computer) {
        this.identifier = identifier;
        this.name = name;
        this.sensorType = sensorType;
        this.hardware = hardware;
        this.value = value;
        this.min = value;
        this.max = value;
        this.control = control;

        this.computer = computer;
    }

    /* Optimization: It is much faster to keep track of min and max directly in
     * Java instead of managing them through PowerShell. */
    /**
     * Resets the memory of the smallest measured value of this sensor.
     */
    public void resetMin() {
        min = value;
    }

    /**
     * Resets the memory of the largest measured value of this sensor.
     */
    public void resetMax() {
        max = value;
    }

    /**
     * Returns the unique identifier of this sensor.
     *
     * @return the unique identifier of this sensor.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the name of this sensor.
     *
     * @return the name of this sensor.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this sensor. For example, "Voltage" or "Temperature".
     *
     * @return the type of this sensor.
     */
    public String getSensorType() {
        return sensorType;
    }

    /**
     * Returns the unit of measurement of this sensor. Units are returned as
     * unit symbols, such as "V" for voltage or "°C" for temperature.
     *
     * @return the unit of measurement of this sensor.
     */
    public String getMeasurementUnit() {
        return KNOWN_MEASUREMENT_UNITS.getOrDefault(sensorType, "");
    }

    /**
     * Returns the parent Hardware item that this sensor is in.
     *
     * @return the parent Hardware item that this sensor is in.
     */
    public Hardware getHardware() {
        return hardware;
    }

    /**
     * Returns the current value of this sensor.
     *
     * @return the current value of this sensor.
     */
    public float getValue() {
        return value;
    }

    /**
     * Returns the smallest measured value of this sensor during the measurement
     * period. The initial measurement period begins on Computer.open(). It can
     * be restarted with {@link #resetMin()} which resets the value to the
     * current sensor value.
     *
     * @return the smallest measured value of this sensor during the measurement
     * period.
     */
    public float getMin() {
        return min;
    }

    /**
     * Returns the largest measured value of this sensor during the measurement
     * period. The initial measurement period begins on Computer.open(). It can
     * be restarted with {@link #resetMax()} which resets the value to the
     * current sensor value.
     *
     * @return the largest measured value of this sensor during the measurement
     * period.
     */
    public float getMax() {
        return max;
    }

    /**
     * Returns whether this sensor is controllable or not.
     *
     * @return whether this sensor is controllable or not.
     */
    public boolean isControllable() {
        return control.isPresent();
    }

    /**
     * Returns the Control object of this sensor, or empty if not controllable.
     *
     * @return the Control object of this sensor, or empty if not controllable.
     */
    public Optional<Control> getControl() {
        return control;
    }

    void setValue(float value) {
        this.value = value;
        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
    }
}
