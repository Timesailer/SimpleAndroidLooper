package ema.simpleaudiolooper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.Button;

public class MainActivity extends AppCompatActivity {

    //Interfacing zu Audiofunktionen
    Trackhandler trackhandler = new Trackhandler();

    //Interface
    Button[] buttons = new Button[8];
    TextView[] clear = new TextView[8];
    ImageButton[] playImg = new ImageButton[8];
    TextView saveWav;

    //rec permissions
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 300;
    private boolean permissionToRecordAccepted = false;
    private boolean permissionToWriteAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
            case REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION:
                permissionToWriteAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }
        if (!permissionToRecordAccepted ) finish();
        if (!permissionToWriteAccepted){
            //saveWav.setVisibility(View.INVISIBLE);
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask recording permission
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
        //UI-Stuff

        buttons[0] = (Button) findViewById(R.id.btn1);
        buttons[1] = (Button) findViewById(R.id.btn2);
        buttons[2] = (Button) findViewById(R.id.btn3);
        buttons[3] = (Button) findViewById(R.id.btn4);
        buttons[4] = (Button) findViewById(R.id.btn5);
        buttons[5] = (Button) findViewById(R.id.btn6);
        buttons[6] = (Button) findViewById(R.id.btn7);
        buttons[7] = (Button) findViewById(R.id.btn8);

        clear[0] = findViewById(R.id.clear1);
        clear[1] = findViewById(R.id.clear2);
        clear[2] = findViewById(R.id.clear3);
        clear[3] = findViewById(R.id.clear4);
        clear[4] = findViewById(R.id.clear5);
        clear[5] = findViewById(R.id.clear6);
        clear[6] = findViewById(R.id.clear7);
        clear[7] = findViewById(R.id.clear8);

        playImg[0] = findViewById(R.id.imgBtn1);
        playImg[1] = findViewById(R.id.imgBtn2);
        playImg[2] = findViewById(R.id.imgBtn3);
        playImg[3] = findViewById(R.id.imgBtn4);
        playImg[4] = findViewById(R.id.imgBtn5);
        playImg[5] = findViewById(R.id.imgBtn6);
        playImg[6] = findViewById(R.id.imgBtn7);
        playImg[7] = findViewById(R.id.imgBtn8);


        final TextView playMix = findViewById(R.id.playMix);
        saveWav = findViewById(R.id.saveWav);




        playMix.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                String btnText = (String) playMix.getText();
                if(btnText.equals("Play Mix")){
                    playMix.setText("Stop Mix");
                    trackhandler.playMix();
                }else{
                    playMix.setText("Play Mix");
                    trackhandler.stopMix();
                }
            }
        });

        saveWav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                if(trackhandler.saveWav()){
                    CharSequence text = "Mix saved successfully!";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }else{
                    CharSequence text = "Error saving Mix!";
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }

            }
        });

        for (int i = 0; i < buttons.length; i++){
            final int index = i;
            buttons[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    trackhandler.handleButton(buttons[index], clear[index], playImg[index],  index);
                }
            });

            clear[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    trackhandler.handleClear(buttons[index], clear[index], index);
                }
            });
        }

    }


}
