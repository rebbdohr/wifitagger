package io.rebbdohr.wifitagger.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Filip on 24.05.17.
 */
@Entity
public class WifiAP {
    long processed_on;
    String tag;
    String SSID;
    String BSSID;
    @Generated(hash = 949786471)
    public WifiAP(long processed_on, String tag, String SSID, String BSSID) {
        this.processed_on = processed_on;
        this.tag = tag;
        this.SSID = SSID;
        this.BSSID = BSSID;
    }
    @Generated(hash = 1718081638)
    public WifiAP() {
    }
    public long getProcessed_on() {
        return this.processed_on;
    }
    public void setProcessed_on(long processed_on) {
        this.processed_on = processed_on;
    }
    public String getTag() {
        return this.tag;
    }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public String getSSID() {
        return this.SSID;
    }
    public void setSSID(String SSID) {
        this.SSID = SSID;
    }
    public String getBSSID() {
        return this.BSSID;
    }
    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }
    
}
