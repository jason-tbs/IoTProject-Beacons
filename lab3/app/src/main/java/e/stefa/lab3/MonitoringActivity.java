package e.stefa.lab3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TabHost;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static e.stefa.lab3.MainActivity.btnLight1;

public class MonitoringActivity extends Activity implements BeaconConsumer {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    private static final String TAG ="MonitoringActivity";
    private Button scanningBtn;
    private Button nextBtn;
    private BeaconManager beaconManager = null;
    static Region beaconRegion = null;
    static Region beaconRegion2 = null;
    static Region beaconRegion3 = null;
    private ListView listView;

    private ArrayList<String> deviceList;
    private ArrayAdapter<String> adapter;

    static boolean beaconInRange = false;
    static boolean beaconInRange2 = false;
    static boolean beaconInRange3 = false;


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitoring_main);
        deviceList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, deviceList);

        requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1234);
       // beaconManager.getBeaconParsers().clear();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
            setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);

        scanningBtn = findViewById(R.id.scanningBtn);
        nextBtn = findViewById(R.id.nextBtn);
        listView = findViewById(R.id.listView);

        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        scanningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBeaconMonitoring();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLights();
            }
        });
    }

    private void moveToLights(){
        Intent intent = new Intent(MonitoringActivity.this, MainActivity.class);
        startActivity(intent);
    }

    protected void onDestroy(){
        super.onDestroy();
        beaconManager.unbind(this);
    }
    public void updateList(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void onBeaconServiceConnect() {
        Log.d(TAG, "onBeaconServiceConnect called");
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i(TAG, "I just saw a beacon for the first time");

            }

            @Override
            public void didExitRegion(Region region) {
                Log.i(TAG, "I no longer see a beacon");
                beaconInRange = false;
                beaconInRange2 = false;
                beaconInRange3 = false;
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                Log.i(TAG, "i have just switched from seeing/not seeing beacons: " + state);
            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                deviceList.clear();
                if (beacons.size() > 0) {
                    Log.i(TAG, "The first beacon i see is " + region.getUniqueId() + " about " + beacons.iterator().next().getDistance() + " meters away");
                    if (region.getUniqueId().equals("Light1")&& beacons.size() > 0 ){
                        beaconInRange = true;
                        Log.i(TAG,"Sätter första lampan till True");
                    }
                    else if(region.getUniqueId().equals("Light2") && beacons.size() > 0 ){
                        beaconInRange2 =true;
                        Log.i(TAG,"Sätter andra lampan till true");
                    }
                    else if (region.getUniqueId().equals("Light3") && beacons.size() > 0 ){
                        beaconInRange3 = true;
                        Log.i(TAG,"Sätter tredje lampan till true");

                    } else {
                        beaconInRange = false;
                        beaconInRange2 = false;
                        beaconInRange3 = false;
                        Log.i(TAG,"Sätter dem till falska");
                    }
                    //deviceList.clear();
                    //adapter.clear();
                    for (Beacon beacon: beacons) {
                        deviceList.add("UUID: " + beacon.getId1()
                                + "\nMAJOR: " + beacon.getId2()
                                + "\nMINOR: " + beacon.getId3()
                                + "\nDISTANCE: " + beacon.getDistance());
                    }
                    updateList();
                }
            }
        });
    }
    private void startBeaconMonitoring(){
        Log.d(TAG, "startBeaconMonitoring called");
        try {
            beaconRegion = new Region("Light1", Identifier.parse("de9a7e78-dec0-4e61-b7e5-76cb5ce04aad"),
                    Identifier.parse("1"), Identifier.parse("1"));
            beaconRegion2 = new Region("Light2", Identifier.parse("de9a7e78-dec0-4e61-b7e5-76cb5ce04aad"),
                    Identifier.parse("1"), Identifier.parse("2"));
            beaconRegion3 = new Region("Light3", Identifier.parse("de9a7e78-dec0-4e61-b7e5-76cb5ce04aad"),
                    Identifier.parse("1"), Identifier.parse("3"));
            beaconManager.startMonitoringBeaconsInRegion(beaconRegion);
            beaconManager.startRangingBeaconsInRegion(beaconRegion);
            beaconManager.startMonitoringBeaconsInRegion(beaconRegion2);
            beaconManager.startRangingBeaconsInRegion(beaconRegion2);
            beaconManager.startMonitoringBeaconsInRegion(beaconRegion3);
            beaconManager.startRangingBeaconsInRegion(beaconRegion3);

        } catch (RemoteException e){
            e.printStackTrace();
        }
    }
}