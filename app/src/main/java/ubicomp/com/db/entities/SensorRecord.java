package ubicomp.com.db.entities;

/**
 * Created by amit on 4/27/16.
 */
import com.orm.SugarRecord;

import java.io.Serializable;


public class SensorRecord extends SugarRecord implements Serializable {

    public String X;
    public String Y;
    public String Z;
    public long time; // in millis
    public String activity;
    public String sensorType;

    public SensorRecord(){

    }

    public SensorRecord(String x, String y, String z, long timestamp, String activity, String sensorType) {

        this.X = x;
        this.Y = y;
        this.Z = z;
        this.time = timestamp;
        this.activity = activity;
        this.sensorType = sensorType;

    }



}