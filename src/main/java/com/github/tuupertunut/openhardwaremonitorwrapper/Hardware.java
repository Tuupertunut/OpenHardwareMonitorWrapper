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

    public void update() throws IOException {
        computer.updateHardware(identifier);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public Optional<Hardware> getParent() {
        return parent;
    }

    public List<Hardware> getSubHardware() {
        return Collections.unmodifiableList(subHardware);
    }

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
