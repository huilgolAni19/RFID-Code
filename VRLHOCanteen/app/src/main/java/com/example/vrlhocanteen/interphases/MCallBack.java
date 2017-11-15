package com.example.vrlhocanteen.interphases;

import com.felhr.usbserial.UsbSerialInterface;

/**
 * Created by Anirudh on 30/05/17.
 */

public abstract class MCallBack implements UsbSerialInterface.UsbReadCallback {

    @Override
    public void onReceivedData(byte[] bytes) {
        String data = new String(bytes);
        if(data.contains("\r")) {
            if(!data.contains("MIFARE_read")) {
                if(!data.contains("PCD_Authenticate")) {
                    if(!data.contains("%") || !data.contains("�")) {
                        if(data.split("~").length == 4) {

                            String completeData = data.replace("\n", "").replace("\u0000", "");
                            onDataRecieveComplete(completeData);
                        }
                    }
                }
            }
        }
    }

    public abstract void onDataRecieveComplete(String dataRecieved);

}