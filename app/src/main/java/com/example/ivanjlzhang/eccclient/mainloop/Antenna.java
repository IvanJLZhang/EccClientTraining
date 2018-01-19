package com.example.ivanjlzhang.eccclient.mainloop;

import android.util.Log;

import java.net.PortUnreachableException;
import java.util.Calendar;

/**
 * Created by ivanjlzhang on 18-1-9.
 */

public class Antenna {
    public final static int ANTENNA_INFORMATION_REQUEST             = 0X00;
    public final static int ANTENNA_INFORMATION_RESPONSE            = 0X01;
    public final static int ANTENNA_CONFIGURATION_REQUEST           = 0X02;
    public final static int ANTENNA_ERROR_RESPONSE                  = 0X03;
    public final static int ANTENNA_CONFIGURATION_STATUS_REQUEST    = 0X04;
    public final static int ANTENNA_CONFIGURATION_STATUS_RESPONSE   = 0X05;


    public static class Information {
        public boolean primary = false;// 0x01
        public boolean secondary = false;// 0x02
        public float primaryRSSI = 0.0f;
        public float secondaryRSSI = 0.0f;
        public float primaryPhase = 0.0f;
        public float secondaryPhase = 0.0f;
        public Calendar calendar = null;

        public float getRelativePhase(){
            return Math.abs(primaryPhase - secondaryPhase)/100;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (primary) {
                sb.append("primaryRSSI = " + primaryRSSI + "\n");
                sb.append("primaryPhase = " + primaryPhase + "\n");
            }
            if (secondary) {
                sb.append("secondaryRSSI = " + secondaryRSSI + "\n");
                sb.append("secondaryPhase = " + secondaryPhase + "\n");
            }
            if (calendar != null) {
                sb.append(calendar.getTime().toString() + "\n");
            }
            return sb.toString();
        }
    }

    public static class Configuration {
        public boolean dual = false;// 0x01/0x00
        public boolean primary = false;// 0x02
        public boolean secondary = false;// 0x03

        public String toString() {
            return "dual = " + dual + ", primary = " + primary + ", secondary = " + secondary;
        }
    }

    public static Information parseInformation(byte[] data){
        Information information = new Information();
        if(data[0] == ANTENNA_INFORMATION_REQUEST){
            information.primary = (data[1] & 0x01) == 0x01;
            information.secondary = (data[1] & 0x02) == 0x02;
        }
        return information;
    }

    public static class InformationResult{
        public int tuned;
        public int rxpwr;
        public int ecio;
        public int rscp;
        public int rsrp;
        public int phase;

        public static InformationResult[] parseResult(String[] results){
            InformationResult[] informationResults = new InformationResult[2];
            informationResults[0] = new InformationResult();
            informationResults[1] = new InformationResult();
            try {
                for (String result : results) {
                    if (result.contains("=")) {
                        String[] keyValuePair = result.split("=");
                        if (keyValuePair[0].contains(".tuned")) {
                            int chainIndex = Integer.parseInt(keyValuePair[0].trim().substring(5, 6));
                            informationResults[chainIndex].tuned = Integer.parseInt(keyValuePair[1]);
                        } else if (keyValuePair[0].contains(".rxpwr")) {
                            int chainIndex = Integer.parseInt(keyValuePair[0].trim().substring(5, 6));
                            informationResults[chainIndex].rxpwr = Integer.parseInt(keyValuePair[1]);
                        } else if (keyValuePair[0].contains(".ecio")) {
                            int chainIndex = Integer.parseInt(keyValuePair[0].trim().substring(5, 6));
                            informationResults[chainIndex].ecio = Integer.parseInt(keyValuePair[1]);
                        } else if (keyValuePair[0].contains(".rscp")) {
                            int chainIndex = Integer.parseInt(keyValuePair[0].trim().substring(5, 6));
                            informationResults[chainIndex].rscp = Integer.parseInt(keyValuePair[1]);
                        } else if (keyValuePair[0].contains(".rsrp")) {
                            int chainIndex = Integer.parseInt(keyValuePair[0].trim().substring(5, 6));
                            informationResults[chainIndex].rsrp = Integer.parseInt(keyValuePair[1]);
                        } else if (keyValuePair[0].contains(".phase")) {
                            int chainIndex = Integer.parseInt(keyValuePair[0].trim().substring(5, 6));
                            informationResults[chainIndex].phase = Integer.parseInt(keyValuePair[1]);
                        }
                    }
                }
            }catch(Exception ex){
                Log.e("ECC_MAIN", ex.getMessage());
            }
            for (InformationResult informationResult : informationResults){
                Log.d("ECC_MAIN", "tuned: " + informationResult.tuned);
                Log.d("ECC_MAIN", "rxpwr: " + informationResult.rxpwr);
                Log.d("ECC_MAIN", "ecio: " + informationResult.ecio);
                Log.d("ECC_MAIN", "rscp: " + informationResult.rscp);
                Log.d("ECC_MAIN", "phase: " + informationResult.phase);
            }
            return informationResults;
        }

        public static int getRelativePhase(InformationResult[] informationResults){
            int relativePhase = 0;
            if(informationResults.length == 2){
                relativePhase = Math.abs(informationResults[0].phase - informationResults[1].phase);
            }
            return relativePhase;
        }
    }

    public static Configuration parseConfiguration(byte[] data) {
        Configuration config = new Configuration();
        if (data[0] == ANTENNA_CONFIGURATION_REQUEST ||
                data[0] == ANTENNA_CONFIGURATION_STATUS_RESPONSE) {
            config.dual = data[1] == 0 || data[1] == 0x01;
            config.primary = data[1] == 0x02;
            config.secondary = data[1] == 0x03;
        }
        return config;
    }

    /*
01-19 09:04:44.063 5824-5824/com.android.phone D/ECC_MAIN: index: 0,value: chain0.tuned=0
01-19 09:04:44.064 5824-5824/com.android.phone D/ECC_MAIN: index: 1,value: chain0.rxpwr=0
01-19 09:04:44.065 5824-5824/com.android.phone D/ECC_MAIN: index: 2,value: chain0.ecio=0
01-19 09:04:44.066 5824-5824/com.android.phone D/ECC_MAIN: index: 3,value: chain0.rscp=0
01-19 09:04:44.067 5824-5824/com.android.phone D/ECC_MAIN: index: 4,value: chain0.rsrp=0
01-19 09:04:44.068 5824-5824/com.android.phone D/ECC_MAIN: index: 5,value: chain0.phase=0
01-19 09:04:44.071 5824-5824/com.android.phone D/ECC_MAIN: index: 6,value: chain1.tuned=1
01-19 09:04:44.072 5824-5824/com.android.phone D/ECC_MAIN: index: 7,value: chain1.rxpwr=-863
01-19 09:04:44.075 5824-5824/com.android.phone D/ECC_MAIN: index: 8,value: chain1.ecio=95
01-19 09:04:44.076 5824-5824/com.android.phone D/ECC_MAIN: index: 9,value: chain1.rscp=955
01-19 09:04:44.077 5824-5824/com.android.phone D/ECC_MAIN: index: 10,value: chain1.rsrp=-2147483648
01-19 09:04:44.077 5824-5824/com.android.phone D/ECC_MAIN: index: 11,value: chain1.phase=-1

     */
    public static byte[] packConfigurationStatusResp(String[] results){
        byte[] data = new byte[2];
        int offset = 0;
        data[offset++] = (byte)ANTENNA_CONFIGURATION_STATUS_RESPONSE;
        boolean primary = false;
        boolean secondary = false;
        /*
        MsgType=0x05, Ant-status {0x03=RxSec-only, 0x02=RxPri-only, 0x00 or 0x01=RxDual}
         */
        for (String result : results){
            if(result.contains("=")){
                String[] keyValuePair = result.split("=");
                if(keyValuePair[0].trim().equals("chain0.tuned")){
                    if(keyValuePair[1].equals("1")){
                        primary = true;
                    }
                }
                if(keyValuePair[0].trim().equals("chain1.tuned")){
                    if(keyValuePair[1].equals("1")){
                        secondary = true;
                    }
                }
            }
        }
        if(primary && secondary){
            data[offset++] = (byte) 0x01;
        }else if(primary){
            data[offset++] = (byte) 0x02;
        }else if(secondary){
            data[offset++] = (byte) 0x03;
        }else {
            data[offset++] = (byte) 0x00;
        }
        return data;
    }

    public static byte[] packInformationResp(Information infoReq, String[] results){
        InformationResult[] informationResults = InformationResult.parseResult(results);
        if((infoReq.primary && informationResults[0].tuned == 0) ||
                (infoReq.secondary && informationResults[1].tuned == 0)){
            // request中要求的antRx在获取到的状态为关闭， 则返回错误回复
            return packErrorResponse(true);
        }
        if (infoReq.primary && infoReq.secondary) {
            return packInformationResponse(informationResults);
        } else if (infoReq.primary) {
            return packInformationResponse(true, informationResults[0].rxpwr / 10);
        } else if (infoReq.secondary) {
            return packInformationResponse(false, informationResults[1].rxpwr / 10);
        }
        return null;
    }
    public static byte[] packInformationResponse(InformationResult[] informationResults) {
        int rssi, phase;
        byte[] data = new byte[18];
        int offset = 0;
        data[offset++] = (byte)ANTENNA_INFORMATION_RESPONSE;
        offset = packDateTime(data, offset);
        data[offset++] = (byte)0x00;//reserved
        data[offset++] = (byte)0x00;//primary
        rssi = (int)((informationResults[0].rxpwr / 10) * (-100));
        data[offset++] = (byte)((rssi >> 8) & 0xFF);
        data[offset++] = (byte)(rssi & 0xFF);
        phase = (int)(Math.abs(informationResults[0].phase - informationResults[1].phase));
        data[offset++] = (byte)((phase >> 8) & 0xFF);
        data[offset++] = (byte)(phase & 0xFF);
        data[offset++] = (byte)0x01;//secondary
        rssi = (int)((informationResults[1].rxpwr / 10) * (-100));
        data[offset++] = (byte)((rssi >> 8) & 0xFF);
        data[offset++] = (byte)(rssi & 0xFF);
        return data;
    }

    public static byte[] packInformationResponse(boolean primary, float fRSSI) {
        int rssi;
        byte[] data = new byte[13];
        int offset = 0;
        data[offset++] = (byte)ANTENNA_INFORMATION_RESPONSE;
        offset = packDateTime(data, offset);
        data[offset++] = (byte)0x00;//reserved
        data[offset++] = (byte)(primary?0x00:0x01);
        rssi = (int)(fRSSI * (-100));
        data[offset++] = (byte)((rssi >> 8) & 0xFF);
        data[offset++] = (byte)(rssi & 0xFF);
        return data;
    }
    public static byte[] packErrorResponse(boolean forInformation) {
        byte[] data = new byte[2];
        int offset = 0;
        data[offset++] = (byte)ANTENNA_ERROR_RESPONSE;
        data[offset++] = (byte)(forInformation ? 0x00 : 0x01); // 0x00 for INFORMATION_REQUEST; 0x01 for CONFIGURATION REQUEST
        return data;
    }

    private static int packDateTime(byte[] data, int offset) {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int millisecond = now.get(Calendar.MILLISECOND);
        data[offset++] = (byte)((year >> 8) & 0xFF); // 2017 --> 07E0
        data[offset++] = (byte)(year & 0xFF);
        data[offset++] = (byte)(month & 0xFF);
        data[offset++] = (byte)(day & 0xFF);
        data[offset++] = (byte)(hour & 0xFF);
        data[offset++] = (byte)(minute & 0xFF);
        data[offset++] = (byte)((millisecond >> 8) & 0xFF);
        data[offset++] = (byte)(millisecond & 0xFF);
        return offset;
    }
}
