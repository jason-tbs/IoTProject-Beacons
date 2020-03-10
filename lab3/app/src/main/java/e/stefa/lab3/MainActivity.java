package e.stefa.lab3;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Region;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

import static e.stefa.lab3.MonitoringActivity.beaconInRange;
import static e.stefa.lab3.MonitoringActivity.beaconInRange2;
import static e.stefa.lab3.MonitoringActivity.beaconInRange3;
import static e.stefa.lab3.MonitoringActivity.beaconRegion;

public class MainActivity extends AppCompatActivity{
    private static final String TAG ="MainActivity";

    private Button btn1;
    private Button btnSub;
    private Button btnPub;
    static Button btnLight1;
    private Button btnLight2;
    private Button btnLight3;
    private Button btnAllLights;
    private Button backBtn;
    private SwitchCompat btnOnOff;
    private SeekBar hueColor;
    private SeekBar hueSat;
    private SeekBar hueBri;
    private TextView textColor;
    private TextView textBri;
    private TextView textSat;
    TextView subText;
    EditText messageText;
    private Button btnRef;

    String onOff = "";
    int colorVal;
    int satVal;
    int briVal;

    //final String serverUri = "tcp://tailor.cloudmqtt.com:14826"; //gruppens
    final String serverUri = "tcp://farmer.cloudmqtt.com:10561";
    final String clientId = "test1";
    //final String subscriptionTopic = "Prov";
    String subscriptionTopic;
    //final String username = "rvotlhrs"; //gruppens
    //final String password = "GAWjnXJ7rX2z"; //gruppens
    final String username = "gwgepsxo";
    final String password = "4jpseV2ARi7y";
    MqttConnectOptions options;
    MqttAndroidClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.btn1);
        btnSub = (Button) findViewById(R.id.btnSub);
        btnPub = (Button) findViewById(R.id.btnPub);
        btnLight1 = (Button) findViewById(R.id.btnLight1);
        btnLight2 = (Button) findViewById(R.id.btnLight2);
        btnLight3 = (Button) findViewById(R.id.btnLight3);
        btnAllLights = (Button) findViewById(R.id.btnAllLights);
        subText = (TextView) findViewById(R.id.subText);
        messageText = (EditText) findViewById(R.id.messageText);
        btnOnOff = (SwitchCompat) findViewById(R.id.btnOnOff);
        hueColor = (SeekBar) findViewById(R.id.hueColor);
        hueSat = (SeekBar) findViewById(R.id.hueSat);
        hueBri = (SeekBar) findViewById(R.id.hueBri);
        textColor = (TextView) findViewById(R.id.textColor);
        textSat = (TextView) findViewById(R.id.textSat);
        textBri = (TextView) findViewById(R.id.textBri);
        backBtn = (Button) findViewById(R.id.backBtn);
        btnRef = (Button) findViewById(R.id.btnRef);

        textColor.setText("Color: "+hueColor.getProgress()+"/"+hueColor.getMax());
        textSat.setText("Saturation: "+hueSat.getProgress()+"/"+hueSat.getMax());
        textBri.setText("Brightness: "+hueBri.getProgress()+"/"+hueBri.getMax());

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToScan();
            }
        });


        btnRef.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MonitoringActivity.beaconInRange == true){
                    btnLight1.setEnabled(true);
                }else {
                    btnLight1.setEnabled(false);
                    Log.i(TAG, "Sätter första lampan till falsk");
                }
                if (beaconInRange2 == true){
                    btnLight2.setEnabled(true);
                } else {
                    btnLight2.setEnabled(false);
                }
                if (beaconInRange3 == true){
                    btnLight3.setEnabled(true);
                } else {
                    btnLight3.setEnabled(false);
                }
                if (beaconInRange==true &&beaconInRange2==true&& beaconInRange3==true){
                    btnAllLights.setEnabled(true);
                }else {
                    btnAllLights.setEnabled(false);
                }
            }
        });


        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clientId = MqttClient.generateClientId();
                final MqttAndroidClient client =
                        new MqttAndroidClient(MainActivity.this, serverUri,
                                clientId);
                options = new MqttConnectOptions();
                options.setUserName(username);
                options.setPassword(password.toCharArray());
                try {
                    IMqttToken token = client.connect(options);
                    token.setActionCallback(new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                btnLight1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String topic = subscriptionTopic;
                        String command1 = "{\"on\":" + onOff + ",\"hue\":"+colorVal+",\"sat\":"+satVal+",\"bri\":"+briVal+",\"transitiontime\":18}X";  //lamp 1
                        String message = command1.toString().trim();

                            try {
                                client.publish(topic, message.getBytes(), 0, false);
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        }

                });
                btnLight2.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        String topic = subscriptionTopic;
                        String command2 = "{\"on\":" + onOff + ",\"hue\":"+colorVal+",\"sat\":"+satVal+",\"bri\":"+briVal+",\"transitiontime\":18}Y";  //lamp 2
                        String message = command2.toString().trim();

                        try{
                            client.publish(topic,message.getBytes(),0,false);
                        }catch (MqttException e){
                            e.printStackTrace();
                        }
                    }
                });
                btnLight3.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        String topic = subscriptionTopic;
                        String command3 = "{\"on\":" + onOff + ",\"hue\":"+colorVal+",\"sat\":"+satVal+",\"bri\":"+briVal+",\"transitiontime\":18}Z";  //lamp 3
                        String message = command3.toString().trim();

                        try{
                            client.publish(topic,message.getBytes(),0,false);
                        }catch (MqttException e){
                            e.printStackTrace();
                        }
                    }
                });
                btnAllLights.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        String topic = subscriptionTopic;
                        String commandAll= "{\"on\":" + onOff + ",\"hue\":"+colorVal+",\"sat\":"+satVal+",\"bri\":"+briVal+",\"transitiontime\":18}A";
                        String message = commandAll.toString().trim();

                        try{
                            client.publish(topic,message.getBytes(),0,false);
                        }catch (MqttException e){
                            e.printStackTrace();
                        }
                    }
                });

                btnOnOff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (btnOnOff.isChecked()){
                            onOff = "true";
                            Toast.makeText(MainActivity.this, "On", Toast.LENGTH_SHORT).show();
                        } else{
                            onOff = "false";
                            Toast.makeText(MainActivity.this, "Off", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                hueColor.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progressColor, boolean fromUser) {
                        textColor.setText("Color: "+progressColor+"/"+hueColor.getMax());
                        colorVal = progressColor;

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                hueSat.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progressSat, boolean fromUser) {
                        textSat.setText("Saturation: "+progressSat+"/"+hueSat.getMax());
                        satVal = progressSat;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                hueBri.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progressBri, boolean fromUser) {
                        textBri.setText("Brightness: "+progressBri+"/"+hueBri.getMax());
                        briVal = progressBri;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {                    }
                });


                btnSub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        subscriptionTopic = messageText.getText().toString().trim();
                        String topic = subscriptionTopic;
                        int qos = 1;
                        try {
                            IMqttToken subToken = client.subscribe(topic, qos);
                            subToken.setActionCallback(new IMqttActionListener() {
                                @Override
                                public void onSuccess(IMqttToken asyncActionToken) {
                                    // The message was published
                                }
                                @Override
                                public void onFailure(IMqttToken asyncActionToken,
                                                      Throwable exception) {
                                    // The subscription could not be performed, maybe the user was not
                                    // authorized to subscribe on the specified topic e.g. using wildcards
                                }
                            });
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        btnPub.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String topic = subscriptionTopic;
                                //String message = "the payload";
                                String message = messageText.getText().toString().trim();
                                try {
                                    client.publish(topic, message.getBytes(),0,true);
                                } catch (MqttException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                });
                client.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable cause) {
                    }
                    @Override
                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                        subText.setText(new String(message.getPayload()));
                    }
                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {

                    }
                });
            }
        });
    }
    private void moveToScan(){
        Intent intent = new Intent(MainActivity.this, MonitoringActivity.class);
        startActivity(intent);
    }

}

