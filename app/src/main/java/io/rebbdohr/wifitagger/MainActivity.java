package io.rebbdohr.wifitagger;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import io.rebbdohr.wifitagger.db.DaoMaster;
import io.rebbdohr.wifitagger.db.DaoSession;
import io.rebbdohr.wifitagger.db.WifiAP;


public class MainActivity extends AppCompatActivity {
    private Context context = this;
    private final int PERMISSION_ACCESS_COARSE_LOCATION = 1;
    private final int PERMISSION_EXTERNAL_STORAGE = 2;
    private SQLiteDatabase db;
    private DaoMaster.DevOpenHelper helper;
    private DaoMaster daoMaster;
    private DaoSession daoSession;
    private Button startScan, saveCSV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startScan = (Button) findViewById(R.id.openUserInputDialog);
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                View mView = layoutInflaterAndroid.inflate(R.layout.input_dialog, null);
                AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(context);
                alertDialogBuilderUserInput.setView(mView);

                final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
                alertDialogBuilderUserInput
                        .setCancelable(false)
                        .setPositiveButton("Start", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                // ToDo get user input here
                                String tag = userInputDialogEditText.getText().toString();
                                tagAP(tag);
                            }
                        })

                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        dialogBox.cancel();
                                    }
                                });

                AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                alertDialogAndroid.show();
            }
        });
        saveCSV = (Button) findViewById(R.id.saveCSV);
        saveCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportCSV();
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        initDB();
        requestPermissions();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (db != null && daoSession != null) {
            daoSession.clear();
            db.close();
        }
    }


    private void initDB(){
        helper = new DaoMaster.DevOpenHelper(this, "wifiAP-db", null);
        db = helper.getWritableDatabase();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();
    }

    private void requestPermissions(){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_EXTERNAL_STORAGE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    showToast("Permission for coarse location granted!");

                } else {

                    showToast("Permission for coarse location required!");
                }
                return;
            }
            case PERMISSION_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    showToast("Permission for external storage granted!");

                } else {

                    showToast("Permission for external storage required!");
                }
                return;
            }
        }
    }
    private void tagAP(String tag){

        long processed_on = new Date().getTime();
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if(wm.isWifiEnabled()){
            Log.v("TAG_AP", "Start tagging AP");
            List<ScanResult> wsl = wm.getScanResults();
            Log.v("TAG_AP", wsl.toString());

            for(ScanResult sr : wsl){
                String ssid = sr.SSID;
                Log.v("TAG_AP", ssid);
                String bssid = sr.BSSID;
                Log.v("TAG_AP", bssid);
                WifiAP wifiAP = new WifiAP(processed_on, tag, ssid, bssid);
                daoSession.insert(wifiAP);
            }
            showToast("All APs tagged with " + tag);
        } else{
            showToast("Wifi is not enabled!");
        }
    }

    private void exportCSV() {
        new ExportCSVTask(daoSession).execute();
    }

    private void showToast(String msg) {
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.getApplicationContext(), text, duration);
        toast.show();
    }
    private class ExportCSVTask extends AsyncTask<String ,String, String> {
        //private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        private DaoSession daoSession;

        public ExportCSVTask(DaoSession daoSession){
            this.daoSession = daoSession;
        }
        @Override
        protected void onPreExecute() {
            //this.dialog.setMessage("Exporting database...");
            //this.dialog.show();
        }

        protected String doInBackground(final String... args){
            try {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "taggedAPs.csv");
                Log.v("EXPORT CSV TASK", file.getAbsolutePath());
                file.createNewFile();
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write("timestamp,tag,ssid,bssid");
                bw.newLine();
                List<WifiAP> lwa = daoSession.getWifiAPDao().loadAll();
                for(WifiAP wa : lwa){
                    bw.write(wa.getProcessed_on() + "," + wa.getTag()+ "," + wa.getSSID() + "," + wa.getBSSID());
                    bw.newLine();
                }
                bw.flush();
            }
            catch(Exception e){
                Log.v("EXPORT CSV TASK", e.getMessage());
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(final String success) {

            //if (this.dialog.isShowing()){
            //    this.dialog.dismiss();
            //}
            if (success.isEmpty()){
                Toast.makeText(MainActivity.this, "Export successful!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "Export failed!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
