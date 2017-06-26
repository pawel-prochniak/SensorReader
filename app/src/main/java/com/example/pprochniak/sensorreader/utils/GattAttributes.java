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

import java.util.HashMap;
import java.util.UUID;

/**
 * This class includes a subset of standard GATT attributes
 */
public class GattAttributes {

    public static final HashMap<UUID, String> attributesCapSense = new HashMap<UUID, String>();
    private static HashMap<String, String> descriptorAttributes = new HashMap<String, String>();
    private static HashMap<UUID, String> attributesUUID = new HashMap<UUID, String>();
    private static HashMap<Integer, String> rdkAttributesUUID = new HashMap<Integer, String>();
    /**
     * Services
     */
    public static final String BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static final String SENSOR_READ_SERVICE = "0000A101-0000-1000-8000-00805F9B34FB";
    /**
     * Sensor read characteristics
     */
    public static final String ACC_X = "0000A102-0000-1000-8000-00805f9b34fb";
    public static final String ACC_Y = "0000A102-0000-1000-8000-00805f9b34fc";
    public static final String ACC_Z = "0000A102-0000-1000-8000-00805f9b34fd";

    /**
     * Unused service UUIDS
     */
    public static final String RGB_LED_SERVICE = "0000cbbb-0000-1000-8000-00805f9b34fb";
    public static final String RGB_LED_SERVICE_CUSTOM = "0003cbbb-0000-1000-8000-00805f9b0131";
    public static final String LINK_LOSS_SERVICE = "00001803-0000-1000-8000-00805f9b34fb";
    public static final String TRANSMISSION_POWER_SERVICE = "00001804-0000-1000-8000-00805f9b34fb";
    public static final String ACCELEROMETER_SERVICE = "00040020-0000-1000-8000-00805f9b0131";
    public static final String ANALOG_TEMPERATURE_SERVICE = "00040030-0000-1000-8000-00805f9b0131";
    public static final String SCAN_PARAMETERS_SERVICE = "00001813-0000-1000-8000-00805f9b34fb";
    public static final String IMMEDIATE_ALERT_SERVICE = "00001802-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_SERVICE = "0000cab5-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_SERVICE_CUSTOM = "0003cab5-0000-1000-8000-00805f9b0131";
    public static final String DEVICE_INFORMATION_SERVICE = "0000180a-0000-1000-8000-00805f9b34fb";
    public static final String OTA_UPDATE_SERVICE = "00060000-f8ce-11e4-abf4-0002a5d5c51b";

    /**
     * Device information characteristics
     */
    public static final String SYSTEM_ID = "00002a23-0000-1000-8000-00805f9b34fb";
    public static final String MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
    public static final String SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";
    public static final String FIRMWARE_REVISION_STRING = "00002a26-0000-1000-8000-00805f9b34fb";
    public static final String HARDWARE_REVISION_STRING = "00002a27-0000-1000-8000-00805f9b34fb";
    public static final String SOFTWARE_REVISION_STRING = "00002a28-0000-1000-8000-00805f9b34fb";
    public static final String MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";
    public static final String PNP_ID = "00002a50-0000-1000-8000-00805f9b34fb";
    public static final String IEEE = "00002a2a-0000-1000-8000-00805f9b34fb";


    /**
     * Battery characteristics
     */
    public static final String BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";
    /**
     * Gatt services
     */
    public static final String GENERIC_ACCESS_SERVICE = "00001800-0000-1000-8000-00805f9b34fb";
    public static final String GENERIC_ATTRIBUTE_SERVICE = "00001801-0000-1000-8000-00805f9b34fb";
    /**
     * Find me characteristics
     */
    public static final String ALERT_LEVEL = "00002a06-0000-1000-8000-00805f9b34fb";
    public static final String TRANSMISSION_POWER_LEVEL = "00002a07-0000-1000-8000-00805f9b34fb";
    /**
     * Capsense characteristics
     */
    public static final String CAPSENSE_PROXIMITY = "0000caa1-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_SLIDER = "0000caa2-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_BUTTONS = "0000caa3-0000-1000-8000-00805f9b34fb";
    public static final String CAPSENSE_PROXIMITY_CUSTOM = "0003caa1-0000-1000-8000-00805f9b0131";
    public static final String CAPSENSE_SLIDER_CUSTOM = "0003caa2-0000-1000-8000-00805f9b0131";
    public static final String CAPSENSE_BUTTONS_CUSTOM = "0003caa3-0000-1000-8000-00805f9b0131";
    /**
     * RGB characteristics
     */
    public static final String RGB_LED = "0000cbb1-0000-1000-8000-00805f9b34fb";
    public static final String RGB_LED_CUSTOM = "0003cbb1-0000-1000-8000-00805f9b0131";
    /**
     * Accelerometer service characteristics
     */
    public static final String ACCELEROMETER_ANALOG_SENSOR = "00040021-0000-1000-8000-00805f9b0131";
    public static final String ACCELEROMETER_SENSOR_SCAN_INTERVAL = "00040023-0000-1000-8000-00805f9b0131";
    public static final String ACCELEROMETER_DATA_ACCUMULATION = "00040026-0000-1000-8000-00805f9b0131";
    public static final String ACCELEROMETER_READING_X = "00040028-0000-1000-8000-00805f9b0131";
    public static final String ACCELEROMETER_READING_Y = "0004002b-0000-1000-8000-00805f9b0131";
    public static final String ACCELEROMETER_READING_Z = "0004002d-0000-1000-8000-00805f9b0131";
    /**
     * Analog Temperature service characteristics
     */
    public static final String TEMPERATURE_ANALOG_SENSOR = "00040031-0000-1000-8000-00805f9b0131";
    public static final String TEMPERATURE_SENSOR_SCAN_INTERVAL = "00040032-0000-1000-8000-00805f9b0131";
    public static final String TEMPERATURE_READING = "00040033-0000-1000-8000-00805f9b0131";
    /**
     * Descriptor UUID's
     */
    public static final String CHARACTERISTIC_EXTENDED_PROPERTIES = "00002900-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_USER_DESCRIPTION = "00002901-0000-1000-8000-00805f9b34fb";
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static final String SERVER_CHARACTERISTIC_CONFIGURATION = "00002903-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_PRESENTATION_FORMAT = "00002904-0000-1000-8000-00805f9b34fb";
    public static final String CHARACTERISTIC_AGGREGATE_FORMAT = "00002905-0000-1000-8000-00805f9b34fb";
    public static final String VALID_RANGE = "00002906-0000-1000-8000-00805f9b34fb";
    public static final String EXTERNAL_REPORT_REFERENCE = "00002907-0000-1000-8000-00805f9b34fb";
    public static final String REPORT_REFERENCE = "00002908-0000-1000-8000-00805f9b34fb";
    public static final String ENVIRONMENTAL_SENSING_CONFIGURATION = "0000290B-0000-1000-8000-00805f9b34fb";
    public static final String ENVIRONMENTAL_SENSING_MEASUREMENT = "0000290C-0000-1000-8000-00805f9b34fb";
    public static final String ENVIRONMENTAL_SENSING_TRIGGER_SETTING = "0000290D-0000-1000-8000-00805f9b34fb";

    static {

        // Services.
        attributesUUID.put(UUIDDatabase.UUID_GENERIC_ACCESS_SERVICE, "Generic Access Service");
        attributesUUID.put(UUIDDatabase.UUID_GENERIC_ATTRIBUTE_SERVICE, "Generic Attribute Service");
        attributesUUID
                .put(UUIDDatabase.UUID_DEVICE_INFORMATION_SERVICE, "Device Information Service");
        attributesUUID.put(UUIDDatabase.UUID_BATTERY_SERVICE, "Battery Service");
        attributesUUID.put(UUIDDatabase.UUID_IMMEDIATE_ALERT_SERVICE, "Immediate Alert");
        attributesUUID.put(UUIDDatabase.UUID_LINK_LOSS_SERVICE, "Link Loss");
        attributesUUID.put(UUIDDatabase.UUID_TRANSMISSION_POWER_SERVICE, "Tx Power");
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_SERVICE, "CapSense Service");
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_SERVICE_CUSTOM, "CapSense Service");
        attributesUUID.put(UUIDDatabase.UUID_RGB_LED_SERVICE, "RGB LED Service");
        attributesUUID.put(UUIDDatabase.UUID_RGB_LED_SERVICE_CUSTOM, "RGB LED Service");
        attributesUUID.put(UUIDDatabase.UUID_ACCELEROMETER_SERVICE, "Accelerometer Service");
        attributesUUID
                .put(UUIDDatabase.UUID_ANALOG_TEMPERATURE_SERVICE, "Analog Temperature Service");
        attributesUUID.put(UUIDDatabase.UUID_SENSOR_READ_SERVICE, "Sensor Read Service");

        // Sensor Read Characteristics
        attributesUUID.put(UUIDDatabase.UUID_ACC_X, "Accelerometer X read");
        attributesUUID.put(UUIDDatabase.UUID_ACC_Y, "Accelerometer Y read");
        attributesUUID.put(UUIDDatabase.UUID_ACC_Z, "Accelerometer Z read");


        // Device Information Characteristics
        attributesUUID.put(UUIDDatabase.UUID_SYSTEM_ID, "System ID");
        attributesUUID.put(UUIDDatabase.UUID_MODEL_NUMBER_STRING, "Model Number String");
        attributesUUID.put(UUIDDatabase.UUID_SERIAL_NUMBER_STRING, "Serial Number String");
        attributesUUID.put(UUIDDatabase.UUID_FIRMWARE_REVISION_STRING, "Firmware Revision String");
        attributesUUID.put(UUIDDatabase.UUID_HARDWARE_REVISION_STRING, "Hardware Revision String");
        attributesUUID.put(UUIDDatabase.UUID_SOFTWARE_REVISION_STRING, "Software Revision String");
        attributesUUID.put(UUIDDatabase.UUID_MANUFACTURE_NAME_STRING, "Manufacturer Name String");
        attributesUUID.put(UUIDDatabase.UUID_PNP_ID, "PnP ID");
        attributesUUID.put(UUIDDatabase.UUID_IEEE,
                "IEEE 11073-20601 Regulatory Certification Data List");

        // Battery service characteristics
        attributesUUID.put(UUIDDatabase.UUID_BATTERY_LEVEL, "Battery Level");

        // Find me service characteristics
        attributesUUID.put(UUIDDatabase.UUID_ALERT_LEVEL, "Alert Level");
        attributesUUID.put(UUIDDatabase.UUID_TRANSMISSION_POWER_LEVEL, "Tx Power Level");

        // Capsense Characteristics
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_BUTTONS, "CapSense Button");
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_PROXIMITY, "CapSense Proximity");
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_SLIDER, "CapSense Slider");
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_BUTTONS_CUSTOM, "CapSense Button");
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_PROXIMITY_CUSTOM, "CapSense Proximity");
        attributesUUID.put(UUIDDatabase.UUID_CAPSENSE_SLIDER_CUSTOM, "CapSense Slider");

        // RGB Characteristics
        attributesUUID.put(UUIDDatabase.UUID_RGB_LED, "RGB LED");
        attributesUUID.put(UUIDDatabase.UUID_RGB_LED_CUSTOM, "RGB LED");


        // SensorHub Characteristics
        attributesUUID.put(UUIDDatabase.UUID_ACCELEROMETER_ANALOG_SENSOR,
                "Accelerometer Analog Sensor");
        attributesUUID.put(UUIDDatabase.UUID_ACCELEROMETER_DATA_ACCUMULATION,
                "Accelerometer Data Accumulation");
        attributesUUID.put(UUIDDatabase.UUID_ACCELEROMETER_SENSOR_SCAN_INTERVAL,
                "Accelerometer Sensor Scan Interval");
        attributesUUID.put(UUIDDatabase.UUID_TEMPERATURE_ANALOG_SENSOR, "Temperature Analog Sensor");
        attributesUUID.put(UUIDDatabase.UUID_TEMPERATURE_READING, "Temperature Reading");
        attributesUUID.put(UUIDDatabase.UUID_TEMPERATURE_SENSOR_SCAN_INTERVAL,
                "Temperature Sensor Scan Interval");

        // Unused Characteristics
        attributesUUID.put(UUIDDatabase.UUID_FIRMWARE_REVISION_STRING, "Firmware Revision String");
        attributesUUID.put(UUIDDatabase.UUID_MANUFACTURE_NAME_STRING, "Manufacturer Name String");
        attributesUUID.put(UUIDDatabase.UUID_MODEL_NUMBER_STRING, "Model Number String");

        // Descriptors
        attributesUUID.put(UUIDDatabase.UUID_CHARACTERISTIC_EXTENDED_PROPERTIES, "Characteristic Extended Properties");
        attributesUUID.put(UUIDDatabase.UUID_CHARACTERISTIC_USER_DESCRIPTION, "Characteristic User Description");
        attributesUUID.put(UUIDDatabase.UUID_CLIENT_CHARACTERISTIC_CONFIG, "Client Characteristic Configuration");
        attributesUUID.put(UUIDDatabase.UUID_SERVER_CHARACTERISTIC_CONFIGURATION, "Server Characteristic Configuration");
        attributesUUID.put(UUIDDatabase.UUID_CHARACTERISTIC_PRESENTATION_FORMAT, "Characteristic Presentation Format");
        attributesUUID.put(UUIDDatabase.UUID_CHARACTERISTIC_AGGREGATE_FORMAT, "Characteristic Aggregate Format");
        attributesUUID.put(UUIDDatabase.UUID_VALID_RANGE, "Valid Range");
        attributesUUID.put(UUIDDatabase.UUID_EXTERNAL_REPORT_REFERENCE, "External Report Reference");
        attributesUUID.put(UUIDDatabase.UUID_REPORT_REFERENCE, "Report Reference");
        attributesUUID.put(UUIDDatabase.UUID_ENVIRONMENTAL_SENSING_CONFIGURATION, "Environmental Sensing Configuration");
        attributesUUID.put(UUIDDatabase.UUID_ENVIRONMENTAL_SENSING_MEASUREMENT, "Environmental Sensing Measurement");
        attributesUUID.put(UUIDDatabase.UUID_ENVIRONMENTAL_SENSING_TRIGGER_SETTING, "Environmental Sensing Trigger Setting");

        //RDK Report Attributes
        rdkAttributesUUID.put(0, "Report Mouse");
        rdkAttributesUUID.put(1, "Report Keyboard");
        rdkAttributesUUID.put(2, "Report Multimedia");
        rdkAttributesUUID.put(3, "Report Power");
        rdkAttributesUUID.put(4, "Report Audio Control");
        rdkAttributesUUID.put(5, "Report Audio Data");


        // Capsense Characteristics
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_SERVICE, "CapSense Services");
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_SERVICE_CUSTOM, "CapSense Services");
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_BUTTONS, "CapSense Button");
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_BUTTONS_CUSTOM, "CapSense Button");
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_PROXIMITY, "CapSense Proximity");
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_PROXIMITY_CUSTOM, "CapSense Proximity");
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_SLIDER, "CapSense Slider");
        attributesCapSense.put(UUIDDatabase.UUID_CAPSENSE_SLIDER_CUSTOM, "CapSense Slider");

        /**
         * Descriptor key value mapping
         */

        descriptorAttributes.put("0", "Reserved For Future Use");
        descriptorAttributes.put("1", "Boolean");
        descriptorAttributes.put("2", "unsigned 2-bit integer");
        descriptorAttributes.put("3", "unsigned 4-bit integer");
        descriptorAttributes.put("4", "unsigned 8-bit integer");
        descriptorAttributes.put("5", "unsigned 12-bit integer");
        descriptorAttributes.put("6", "unsigned 16-bit integer");
        descriptorAttributes.put("7", "unsigned 24-bit integer");
        descriptorAttributes.put("8", "unsigned 32-bit integer");
        descriptorAttributes.put("9", "unsigned 48-bit integer");
        descriptorAttributes.put("10", "unsigned 64-bit integer");
        descriptorAttributes.put("11", "unsigned 128-bit integer");
        descriptorAttributes.put("12", "signed 8-bit integer");
        descriptorAttributes.put("13", "signed 12-bit integer");
        descriptorAttributes.put("14", "signed 16-bit integer");
        descriptorAttributes.put("15", "signed 24-bit integer");
        descriptorAttributes.put("16", "signed 32-bit integer");
        descriptorAttributes.put("17", "signed 48-bit integer");
        descriptorAttributes.put("18", "signed 64-bit integer");
        descriptorAttributes.put("19", "signed 128-bit integer");
        descriptorAttributes.put("20", "IEEE-754 32-bit floating point");
        descriptorAttributes.put("21", "IEEE-754 64-bit floating point");
        descriptorAttributes.put("22", "IEEE-11073 16-bit SFLOAT");
        descriptorAttributes.put("23", "IEEE-11073 32-bit FLOAT");
        descriptorAttributes.put("24", "IEEE-20601 format");
        descriptorAttributes.put("25", "UTF-8 string");
        descriptorAttributes.put("26", "UTF-16 string");
        descriptorAttributes.put("27", "Opaque Structure");

    }

    public static String lookupUUID(UUID uuid, String defaultName) {
        String name = attributesUUID.get(uuid);
        return name == null ? defaultName : name;
    }

    public static String lookupReferenceRDK(int instanceid, String defaultName) {
        String name = rdkAttributesUUID.get(instanceid);
        return name == null ? defaultName : name;
    }

    public static String lookupNameCapSense(UUID uuid, String defaultName) {
        String name = attributesCapSense.get(uuid);
        return name == null ? defaultName : name;
    }

    public static String lookCharacteristicPresentationFormat(String key) {
        String value = descriptorAttributes.get(key);
        return value == null ? "Reserved" : value;
    }

}