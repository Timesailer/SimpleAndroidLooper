package ema.simpleaudiolooper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /*Erstmal ohne c++ weil das echt meeeeeega den overhead macht und etwas zu mächtig für unsere
    Zwecke ist, für n kleinen Einblick, die audio-echo implementation
    https://github.com/googlesamples/android-ndk/tree/master/audio-echo hier bietet n einblick,
    der shit ist mir zu kompliziert
    */

    //Code largely untested, it does stuff and mostly doesnt crash, but i dont have good logs atm and its 1 am

    //Samplestuff adapted: http://androidsourcecode.blogspot.de/2013/07/android-audio-demo-audiotrack.html
    //Audiostuff
    AudioManager am = null;
    AudioRecord rec = null;
    AudioTrack track = null;

    //buffered recording
    //TODO: truncate the last chunk, optimize shit, see how it works with static track
    ArrayList<BufferTuple> audiobuffer = new ArrayList<BufferTuple>();

    //Interface
    Button btnRec;
    Button btnPlay;

    boolean isRecording = false;
    boolean isPlaying = true;

    //rec permissions
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Ask recording permission
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        btnRec = (Button) findViewById(R.id.btnRec);
        btnRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRecord();
            }
        });

        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPlay();
            }
        });

        //initialize recorder sample code got an exception on my phone
        int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        //rec = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO,
        //        AudioFormat.ENCODING_PCM_16BIT, min);
        //rec = findAudioRecord();

        //initialize track
        int maxJitter = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        track = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, 8000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, maxJitter, AudioTrack.MODE_STREAM);

    }

    void onRecord(){
        if(!isRecording){
            //reinitialize buffer, set bool
            isRecording = true;
            audiobuffer = new ArrayList<BufferTuple>();
            btnRec.setText("recording..");
            if (rec == null){
                rec = findAudioRecord();
            }

            (new Thread(){
                @Override
                public void run(){
                    Log.v("main", "Started recording Thread");
                    recording();
                    Log.v("main", "Close recording Thread");
                }
            }).start();

        }else{
            isRecording = false;
            btnRec.setText("record");
        }
    }

    void onPlay(){
        if(!isPlaying){
            isPlaying = true;
            btnPlay.setText("playing");
            (new Thread(){
                @Override
                public void run(){
                    Log.v("main", "Started playing Thread");
                    play();
                    Log.v("main", "Close playing Thread");
                }
            }).start();
        }else{
            isPlaying = false;
            btnPlay.setText("play");
        }
    }

    void recording(){
        short[] input = new short[1024];
        rec.startRecording();
        int samples = 0;
        Log.v("main", "start writing bufferchunks");
        while(isRecording){
            samples = rec.read(input, 0, 1024);
            audiobuffer.add(new BufferTuple(input, samples));
        }
        rec.stop();
        Log.v("Main", "Written " + audiobuffer.size() + " chunks to buffer");
    }

    void play(){
        track.play();
        Log.v("main", "start reading and looping buffer");
        while (isPlaying){
            for (int i = 0; i < audiobuffer.size(); i++){
                track.write(audiobuffer.get(i).buffer, 0, audiobuffer.get(i).samples);
            }
        }
        track.release();
    }

    //Find The right stuff for your recording hardware on your phone
    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        Log.d("main", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return recorder;
                        }
                    } catch (Exception e) {
                        Log.e("main", rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
    }

}
