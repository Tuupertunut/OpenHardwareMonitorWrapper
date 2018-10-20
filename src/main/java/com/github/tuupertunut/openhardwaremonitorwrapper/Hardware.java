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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Hardware item of the computer. For example, a GPU or RAM module. A hardware
 * item has sensors that each hold a value. It may have multiple subhardware,
 * each with their own sensors.
 *
 * @author Tuupertunut
 */
public class Hardware {

    private final Computer computer;

    private final String identifier;
    private final String name;
    private final String hardwareType;
    private final Optional<Hardware> parent;
    private final List<Hardware> subHardware;
    private final List<Sensor> sensors;

    Hardware(String identifier, String name, String hardwareType, Optional<Hardware> parent, Computer computer) {
        this.identifier = identifier;
        this.name = name;
        this.hardwareType = hardwareType;
        this.parent = parent;
        this.subHardware = new ArrayList<>();
        this.sensors = new ArrayList<>();

        this.computer = computer;
    }

    /**
     * Measures and updates new values to the sensors of this hardware item.
     * Sensors of subhardware will also be updated.
     *
     * @throws IOException if an IOException occurs in the communication with
     * the OpenHardwareMonitor.
     */
    public void update() throws IOException {
        computer.updateHardware(identifier);
    }

    /**
     * Returns the unique identifier of this hardware item.
     *
     * @return the unique identifier of this hardware item.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the name of this hardware item.
     *
     * @return the name of this hardware item.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this hardware item. For example, "GPU" or "RAM".
     *
     * @return the type of this hardware item.
     */
    public String getHardwareType() {
        return hardwareType;
    }

    /**
     * Returns the parent hardware item of this hardware item, or empty if this
     * is a top level hardware item.
     *
     * @return the parent hardware item of this hardware item, or empty if this
     * is a top level hardware item.
     */
    public Optional<Hardware> getParent() {
        return parent;
    }

    /**
     * Returns a list of all subhardware of this hardware item.
     *
     * @return a list of all subhardware of this hardware item.
     */
    public List<Hardware> getSubHardware() {
        return Collections.unmodifiableList(subHardware);
    }

    /**
     * Returns a list of all sensors of this hardware item.
     *
     * @return a list of all sensors of this hardware item.
     */
    public List<Sensor> getSensors() {
        return Collections.unmodifiableList(sensors);
    }

    void addSubHardware(Hardware hardware) {
        subHardware.add(hardware);
    }

    void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }
}
