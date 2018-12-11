package ubicomp.com.db.entities;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import com.orm.SugarRecord;
import com.orm.util.ReflectionUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.*;
import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class SugarDumpIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_EXPORT = "ubicomp.com.suger.dump.action.export";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "ubicomp.com.suger.dump.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "ubicomp.com.suger.dump.extra.PARAM2";

    public SugarDumpIntentService() {
        super("SugarDumpIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     * @param param1 external file path
     * @param param2 Your sugar class name
     */
    // TODO: Customize helper method
    public static void startActionExport(Context context, String param1, Serializable param2) {
        Intent intent = new Intent(context, SugarDumpIntentService.class);
        intent.setAction(ACTION_EXPORT);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);

        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_EXPORT.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final Serializable param2 = intent.getSerializableExtra(EXTRA_PARAM2);
                handleActionFoo(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String filename) {
        // TODO: Handle action Foo

        try {

            if(SugarRecord.isSugarEntity(SensorRecord.class)){

                List<Field> fields = ReflectionUtil.getTableFields(SensorRecord.class);

                //List<AcceleroMeterRecord> records = SugarRecord.listAll(AcceleroMeterRecord.class);

                int totalRows =(int) SensorRecord.count(SensorRecord.class);
                int chunk=5000;

                File file = new File(Environment.getExternalStorageDirectory(), filename);

                Log.e("amit",file.getAbsolutePath());

                if(!file.exists()){
                    file.createNewFile();
                }
                Log.e("amit",file.getAbsolutePath());

                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter csvOutWriter = new OutputStreamWriter(fOut);

                String titleRow="";
                for (Field field:fields){

                    titleRow = titleRow+","+field.getName();

                }
                csvOutWriter.append(titleRow);
                csvOutWriter.append("\n");

                int i=0;

                do{

                    List<SensorRecord> records = SensorRecord.findWithQuery(SensorRecord.class,"Select * from sensor_record limit ? offset ?",chunk+"",i+"");

                    for (SensorRecord record:records){

                        String dataRow="";

                        for (Field field:fields){

                            //field.getName();
                            Object memberValue = field.get(record);
                            dataRow =dataRow+","+String.valueOf(memberValue);
                        }
                        csvOutWriter.append(dataRow);
                        csvOutWriter.append("\n");

                    }
                    i= i+chunk;
                }while (i<totalRows);

                csvOutWriter.close();
                fOut.close();

                Log.e("amit", "File write done");

                SensorRecord.deleteAll(SensorRecord.class);


            }else{
                new UnsupportedOperationException("Not a Sugar Record class");
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //throw new UnsupportedOperationException("Not yet implemented");
    }

}
