/*
 * Copyright Cypress Semiconductor Corporation, 2014-2015 All rights reserved.
 * 
 * This software, associated documentation and materials ("Software") is
 * owned by Cypress Semiconductor Corporation ("Cypress") and is
 * protected by and subject to worldwide patent protection (UnitedStates and foreign), United States copyright laws and international
 * treaty provisions. Therefore, unless otherwise specified in a separate license agreement between you and Cypress, this Software
 * must be treated like any other copyrighted material. Reproduction,
 * modification, translation, compilation, or representation of this
 * Software in any other form (e.g., paper, magnetic, optical, silicon)
 * is prohibited without Cypress's express written permission.
 * 
 * Disclaimer: THIS SOFTWARE IS PROVIDED AS-IS, WITH NO WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * NONINFRINGEMENT, IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE. Cypress reserves the right to make changes
 * to the Software without notice. Cypress does not assume any liability
 * arising out of the application or use of Software or any product or
 * circuit described in the Software. Cypress does not authorize its
 * products for use as critical components in any products where a
 * malfunction or failure may reasonably be expected to result in
 * significant injury or death ("High Risk Product"). By including
 * Cypress's product in a High Risk Product, the manufacturer of such
 * system or application assumes all risk of such use and in doing so
 * indemnifies Cypress against all liability.
 * 
 * Use of this Software may be limited by and subject to the applicable
 * Cypress software license agreement.
 * 
 * 
 */

package com.example.pprochniak.sensorreader.utils;

import java.util.UUID;

/**
 * This class will store the UUID of the GATT services and characteristics
 */
public class UUIDDatabase {
    /**
     * Sensor read related UUID
     */
    public static final UUID UUID_SENSOR_READ_SERVICE = UUID
            .fromString(GattAttributes.SENSOR_READ_SERVICE);
    public static final UUID UUID_ACC_X = UUID.fromString(GattAttributes.ACC_X);
    public static final UUID UUID_ACC_Y = UUID.fromString(GattAttributes.ACC_Y);
    public static final UUID UUID_ACC_Z = UUID.fromString(GattAttributes.ACC_Z);

    /**
     * Device information related UUID
     */
    public final static UUID UUID_DEVICE_INFORMATION_SERVICE = UUID
            .fromString(GattAttributes.DEVICE_INFORMATION_SERVICE);
    public final static UUID UUID_SYSTEM_ID = UUID
            .fromString(GattAttributes.SYSTEM_ID);
    public static final UUID UUID_MANUFACTURE_NAME_STRING = UUID
            .fromString(GattAttributes.MANUFACTURER_NAME_STRING);
    public static final UUID UUID_MODEL_NUMBER_STRING = UUID
            .fromString(GattAttributes.MODEL_NUMBER_STRING);
    public static final UUID UUID_SERIAL_NUMBER_STRING = UUID
            .fromString(GattAttributes.SERIAL_NUMBER_STRING);
    public static final UUID UUID_HARDWARE_REVISION_STRING = UUID
            .fromString(GattAttributes.HARDWARE_REVISION_STRING);
    public static final UUID UUID_FIRMWARE_REVISION_STRING = UUID
            .fromString(GattAttributes.FIRMWARE_REVISION_STRING);
    public static final UUID UUID_SOFTWARE_REVISION_STRING = UUID
            .fromString(GattAttributes.SOFTWARE_REVISION_STRING);
    public static final UUID UUID_PNP_ID = UUID
            .fromString(GattAttributes.PNP_ID);
    public static final UUID UUID_IEEE = UUID
            .fromString(GattAttributes.IEEE);


    /**
     * Accelerometer related uuid
     */
    public final static UUID UUID_ACCELEROMETER_SERVICE = UUID
            .fromString(GattAttributes.ACCELEROMETER_SERVICE);
    public final static UUID UUID_ACCELEROMETER_ANALOG_SENSOR = UUID
            .fromString(GattAttributes.ACCELEROMETER_ANALOG_SENSOR);
    public final static UUID UUID_ACCELEROMETER_DATA_ACCUMULATION = UUID
            .fromString(GattAttributes.ACCELEROMETER_DATA_ACCUMULATION);
    public final static UUID UUID_ACCELEROMETER_SENSOR_SCAN_INTERVAL = UUID
            .fromString(GattAttributes.ACCELEROMETER_SENSOR_SCAN_INTERVAL);

    /**
     * GATT related UUID
     */
    public final static UUID UUID_GENERIC_ACCESS_SERVICE = UUID
            .fromString(GattAttributes.GENERIC_ACCESS_SERVICE);
    public final static UUID UUID_GENERIC_ATTRIBUTE_SERVICE = UUID
            .fromString(GattAttributes.GENERIC_ATTRIBUTE_SERVICE);


}
