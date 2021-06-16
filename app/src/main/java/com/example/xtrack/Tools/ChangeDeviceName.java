package com.example.xtrack.Tools;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ChangeDeviceName {

    private final String TAG = "ChangedDeviceName";
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    public ChangeDeviceName(WifiP2pManager mManager, WifiP2pManager.Channel mChannel){
    this.mManager = mManager;
    this.mChannel = mChannel;
    }

    public void setDeviceName(String devName) {
        try {
            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = mManager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object arglist[] = new Object[3];
            arglist[0] = mChannel;
            arglist[1] = devName;
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Log.d(TAG,"setDeviceName succeeded");
                }

                @Override
                public void onFailure(int reason) {
                    Log.d(TAG,"setDeviceName failed: Error Code" +reason);
                }
            };

            setDeviceName.invoke(mManager, arglist);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
