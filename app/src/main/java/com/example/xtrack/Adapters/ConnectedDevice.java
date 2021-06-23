package com.example.xtrack.Adapters;

public class ConnectedDevice {
    int ICON;
    String deviceName;
    String deviceStatus;

    public ConnectedDevice(int ICON, String deviceName, String deviceStatus){
        this.ICON = ICON;
        this.deviceName = deviceName;
        this.deviceStatus = deviceStatus;
    }

    public int getICON() {
        return ICON;
    }

    public void setICON(int ICON) {
        this.ICON = ICON;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }
}
