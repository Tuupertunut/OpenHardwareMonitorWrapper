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
import java.util.Optional;

/**
 * The controllable part of a sensor. For example, a fan control. A control can
 * be set to default or software controlled mode. In default mode, the control
 * is not controlled by OpenHardwareMonitor. In software mode, the control can
 * be set to a given value, such as a fan speed percentage.
 *
 * @author Tuupertunut
 */
public class Control {

    private final Computer computer;

    private final String identifier;
    private Optional<Float> softwareValue;
    private final float minSoftwareValue;
    private final float maxSoftwareValue;

    Control(String identifier, float minSoftwareValue, float maxSoftwareValue, Computer computer) {
        this.identifier = identifier;
        this.softwareValue = Optional.empty();
        this.minSoftwareValue = minSoftwareValue;
        this.maxSoftwareValue = maxSoftwareValue;

        this.computer = computer;
    }

    /**
     * Sets this control to the default (non software controlled) mode.
     *
     * @throws IOException if an IOException occurs in the communication with
     * the OpenHardwareMonitor.
     * @throws IllegalStateException if the computer has already been closed.
     */
    public void setDefault() throws IOException {
        computer.setControlDefault(identifier);
        softwareValue = Optional.empty();
    }

    /**
     * Sets this control to be software controlled at the specified value.
     *
     * @param value the value this control will be set to.
     * @throws IOException if an IOException occurs in the communication with
     * the OpenHardwareMonitor.
     * @throws IllegalStateException if the computer has already been closed.
     */
    public void setSoftware(float value) throws IOException {
        computer.setControlSoftware(identifier, value);
        softwareValue = Optional.of(value);
    }

    /**
     * Returns the unique identifier of this control.
     *
     * @return the unique identifier of this control.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns whether this control is software controlled or not.
     *
     * @return whether this control is software controlled or not.
     */
    public boolean isSoftwareControlled() {
        return softwareValue.isPresent();
    }

    /**
     * Returns the current software controlled value this control is set to, or
     * empty if not software controlled.
     *
     * @return the current software controlled value this control is set to, or
     * empty if not software controlled.
     */
    public Optional<Float> getSoftwareValue() {
        return softwareValue;
    }

    /**
     * Returns the minimum value this control can be set to.
     *
     * @return the minimum value this control can be set to.
     */
    public float getMinSoftwareValue() {
        return minSoftwareValue;
    }

    /**
     * Returns the maximum value this control can be set to.
     *
     * @return the maximum value this control can be set to.
     */
    public float getMaxSoftwareValue() {
        return maxSoftwareValue;
    }
}
