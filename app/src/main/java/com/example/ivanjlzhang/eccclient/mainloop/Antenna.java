package com.example.ivanjlzhang.eccclient.mainloop;

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
        public boolean dual = false;// 0x01
        public boolean primary = false;// 0x02
        public boolean secondary = false;// 0x03

        public String toString() {
            return "dual = " + dual + ", primary = " + primary + ", secondary = " + secondary;
        }
    }

    public static class ConfigurationStatus{

    }
}
