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

import com.github.tuupertunut.powershelllibjava.PowerShell;
import com.github.tuupertunut.powershelllibjava.PowerShellExecutionException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Tuupertunut
 */
class PowerShellService {

    private static final Path OHM_LIB_PATH = Paths.get("OpenHardwareMonitorLib.dll");

    private PowerShell psSession = null;

    PowerShellService() throws InsufficientPermissionsException, IOException {
        try {
            psSession = PowerShell.open();

            if (checkAdminRights()) {
                initialize();
            } else {
                throw new InsufficientPermissionsException("Admin rights are needed to use OpenHardwareMonitor.");
            }
        } catch (InsufficientPermissionsException | IOException | RuntimeException ex) {
            close();
            throw ex;
        }
    }

    void close() {
        if (psSession != null) {
            psSession.close();
            psSession = null;
        }
    }

    private boolean checkAdminRights() throws IOException {
        try {
            String commandOutput = psSession.executeCommands("([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)");
            return Boolean.parseBoolean(commandOutput.trim());
        } catch (PowerShellExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void initialize() throws IOException {
        try {
            psSession.executeCommands(
                    "[System.Reflection.Assembly]::LoadFile(" + PowerShell.escapePowerShellString(OHM_LIB_PATH.toAbsolutePath().toString()) + ")",
                    "$ohmObjs = @{}",
                    "function Initialize-And-Write-Hardware ($h) {",
                    "    $h.Update()",
                    "    $ohmObjs[$h.Identifier.ToString()] = $h",
                    "    '{'",
                    "    $h.Identifier",
                    "    $h.Name",
                    "    $h.HardwareType",
                    "    '['",
                    "    foreach ($sh in $h.SubHardware) {",
                    "        Initialize-And-Write-Hardware $sh",
                    "    }",
                    "    ']'",
                    "    '['",
                    "    foreach ($s in $h.Sensors) {",
                    "        $ohmObjs[$s.Identifier.ToString()] = $s",
                    "        '{'",
                    "        $s.Identifier",
                    "        $s.Name",
                    "        $s.SensorType",
                    "        $s.Value.ToString([System.Globalization.CultureInfo]::InvariantCulture)",
                    "        $c = $s.Control",
                    "        if ($c) {",
                    "            $ohmObjs[$c.Identifier.ToString()] = $c",
                    "            '{'",
                    "            $c.Identifier",
                    "            $c.MinSoftwareValue.ToString([System.Globalization.CultureInfo]::InvariantCulture)",
                    "            $c.MaxSoftwareValue.ToString([System.Globalization.CultureInfo]::InvariantCulture)",
                    "            '}'",
                    "        } else {",
                    "            ''",
                    "        }",
                    "        '}'",
                    "    }",
                    "    ']'",
                    "    '}'",
                    "}",
                    "function Update-And-Write-Sensor-Values ($h) {",
                    "    $h.Update()",
                    "    foreach ($sh in $h.SubHardware) {",
                    "        Update-And-Write-Sensor-Values $sh",
                    "    }",
                    "    foreach ($s in $h.Sensors) {",
                    "        '{'",
                    "        $s.Identifier",
                    "        $s.Value.ToString([System.Globalization.CultureInfo]::InvariantCulture)",
                    "        '}'",
                    "    }",
                    "}");
        } catch (PowerShellExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    List<String> openComputer() throws IOException {
        if (psSession == null) {
            throw new IllegalStateException("This PowerShellService has been closed.");
        }
        try {
            String commandOutput = psSession.executeCommands(
                    "$comp = New-Object OpenHardwareMonitor.Hardware.Computer",
                    "$comp.MainboardEnabled = $true",
                    "$comp.CPUEnabled = $true",
                    "$comp.RAMEnabled = $true",
                    "$comp.GPUEnabled = $true",
                    "$comp.FanControllerEnabled = $true",
                    "$comp.HDDEnabled = $true",
                    "$comp.Open()",
                    "'['",
                    "foreach ($h in $comp.Hardware) {",
                    "    Initialize-And-Write-Hardware $h",
                    "}",
                    "']'");
            return Arrays.asList(commandOutput.trim().split(System.lineSeparator()));
        } catch (PowerShellExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    List<String> updateAllHardware() throws IOException {
        if (psSession == null) {
            throw new IllegalStateException("This PowerShellService has been closed.");
        }
        try {
            String commandOutput = psSession.executeCommands(
                    "'['",
                    "foreach ($h in $comp.Hardware) {",
                    "    Update-And-Write-Sensor-Values $h",
                    "}",
                    "']'");
            return Arrays.asList(commandOutput.trim().split(System.lineSeparator()));
        } catch (PowerShellExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    List<String> updateHardware(String hardwareIdentifier) throws IOException {
        if (psSession == null) {
            throw new IllegalStateException("This PowerShellService has been closed.");
        }
        try {
            String commandOutput = psSession.executeCommands(
                    "'['",
                    "Update-And-Write-Sensor-Values $ohmObjs[" + PowerShell.escapePowerShellString(hardwareIdentifier) + "]",
                    "']'");
            return Arrays.asList(commandOutput.trim().split(System.lineSeparator()));
        } catch (PowerShellExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    void setControlDefault(String controlIdentifier) throws IOException {
        if (psSession == null) {
            throw new IllegalStateException("This PowerShellService has been closed.");
        }
        try {
            psSession.executeCommands("$ohmObjs[" + PowerShell.escapePowerShellString(controlIdentifier) + "].SetDefault()");
        } catch (PowerShellExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    void setControlSoftware(String controlIdentifier, float value) throws IOException {
        if (psSession == null) {
            throw new IllegalStateException("This PowerShellService has been closed.");
        }
        try {
            psSession.executeCommands("$ohmObjs[" + PowerShell.escapePowerShellString(controlIdentifier) + "].SetSoftware(" + Float.toString(value) + ")");
        } catch (PowerShellExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }
}
