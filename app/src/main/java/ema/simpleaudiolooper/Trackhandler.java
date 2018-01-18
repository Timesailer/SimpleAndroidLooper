package ema.simpleaudiolooper;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;

import com.gc.materialdesign.views.Button;

import java.util.ArrayList;

/**
 * Created by milan_000 on 16.01.2018.
 */

public class Trackhandler {

    //Samplestuff adapted: http://androidsourcecode.blogspot.de/2013/07/android-audio-demo-audiotracks.html
    //Audiostuff
    AudioManager am = null;
    AudioRecord rec = null;
    AudioTrackData[] tracks = new AudioTrackData[8];

    boolean isRecording = false;
    boolean[] isPlaying = new boolean[8];
    int recordingIndex = -1;

    //buffered recording
    //TODO: truncate the last chunk, optimize shit, see how it works with static tracks
    ArrayList<BufferTuple>[] audiobuffer = new ArrayList[8];

    public Trackhandler(){
        //init audiobuffer
        for(int i = 0; i < 7; i++){
            audiobuffer[i] = new ArrayList<BufferTuple>();
        }

        //initialize recorder sample code got an exception on my phone
        //int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        //rec = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO,
        //        AudioFormat.ENCODING_PCM_16BIT, min);
        rec = findAudioRecord();
        Log.v("audioshit", "samplerate: " + rec.getSampleRate() + "\nAudioFormat: " + rec.getAudioFormat() + "\nChannelconf: " + rec.getChannelConfiguration());

    }

    public void handleButton(Button button, int index){
        //todo check btn state
        //if empty onRecord idx // change btn color  // set Audiotrackdata flag
    }

    AudioTrack initATrack(){
        AudioTrack at = null;
        int maxJitter = AudioTrack.getMinBufferSize(rec.getSampleRate(), AudioFormat.CHANNEL_OUT_MONO, rec.getAudioFormat());
        at = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, rec.getSampleRate(), AudioFormat.CHANNEL_OUT_MONO,
                rec.getAudioFormat(), maxJitter, AudioTrack.MODE_STREAM);
        return at;
    }

    void onRecord(final int index){
        if(!isRecording){
            //reinitialize buffer, set bool
            isRecording = true;
            audiobuffer[index] = new ArrayList<BufferTuple>();
            if (rec == null){
                rec = findAudioRecord();
            }

            (new Thread(){
                @Override
                public void run(){
                    Log.v("main", "Started recording Thread");
                    recording(index);
                    Log.v("main", "Close recording Thread");
                }
            }).start();

        }else{
            isRecording = false;
        }
    }

    void onPlay(final int track){
        //if(!isPlaying){
        isPlaying[track] = true;
        tracks[track].setAudioTrack(initATrack());
        (new Thread(){
            @Override
            public void run(){
                Log.v("audioshit", "Started playing Thread for Track" + track);
                play(track);
                Log.v("audioshit", "Close playing Thread for Track" + track);
            }
        }).start();
        //}else{
        //    isPlaying = false;
        //    btnPlay.setText("play" + track);
        //}
    }

    void recording(int track){
        short[] input = new short[1024];
        rec.startRecording();
        int samples = 0;
        Log.v("main", "start writing bufferchunks");
        while(isRecording){
            samples = rec.read(input, 0, 1024);
            audiobuffer[track].add(new BufferTuple(input, samples));
        }
        rec.stop();
        tracks[track].setRecorded(true);
        Log.v("Main", "Written " + audiobuffer[track].size() + " chunks to buffer");
    }

    void play(int track){
        tracks[track].getAudioTrack().play();
        Log.v("main", "start reading and looping buffer");
        while (isPlaying[track]){
            for (int i = 0; i < audiobuffer[track].size(); i++){
                tracks[track].getAudioTrack().write(audiobuffer[track].get(i).buffer, 0, audiobuffer[track].get(i).samples);
            }
        }
    }


    //Find The right stuff for your recording hardware on your phone
    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    public AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT }) {
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
