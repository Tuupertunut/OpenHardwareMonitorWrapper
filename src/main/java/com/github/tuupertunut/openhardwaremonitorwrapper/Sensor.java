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
 *
 * @author Tuupertunut
 */
public class Sensor {

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

    /* Optimization: It is much faster to handle min and max directly in java
     * instead of managing them through powershell. */
    public void resetMin() {
        min = value;
    }

    public void resetMax() {
        max = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getSensorType() {
        return sensorType;
    }

    public String getMeasurementUnit() {
        return KNOWN_MEASUREMENT_UNITS.getOrDefault(sensorType, "");
    }

    public Hardware getHardware() {
        return hardware;
    }

    public float getValue() {
        return value;
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public boolean isControllable() {
        return control.isPresent();
    }

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
