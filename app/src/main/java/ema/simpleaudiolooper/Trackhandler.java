package ema.simpleaudiolooper;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gc.materialdesign.views.Button;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by milan_000 on 16.01.2018.
 */

public class Trackhandler {

    //Samplestuff adapted: http://androidsourcecode.blogspot.de/2013/07/android-audio-demo-audiotracks.html
    //Audiostuff
    AudioManager am = null;
    AudioRecord rec = null;
    AudioTrackData[] audioTrackArray = new AudioTrackData[8];

    boolean isRecording = false;
    AudioTrackData audioMix = new AudioTrackData();

    //buffered recording
    //TODO: truncate the last chunk, optimize shit, see how it works with static tracks

    public Trackhandler(){
        for(int i = 0; i < 8; i++){
            audioTrackArray[i] = new AudioTrackData();
        }

        //initialize recorder sample code got an exception on my phone
        //int min = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        //rec = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, 8000, AudioFormat.CHANNEL_IN_MONO,
        //        AudioFormat.ENCODING_PCM_16BIT, min);
        rec = findAudioRecord();
        Log.v("audioshit", "samplerate: " + rec.getSampleRate() + "\nAudioFormat: " + rec.getAudioFormat() + "\nChannelconf: " + rec.getChannelConfiguration());

    }

    public void handleButton(Button button, TextView clear, ImageButton playImg, int index){
        //todo check btn state
        AudioTrackData atd = audioTrackArray[index];
        if(!audioTrackArray[index].isRecorded()){
            button.setBackgroundColor(0xFFdd2c00);
            onRecord(atd,clear, button);
        }else{
            onPlay(atd,playImg);
        }
    }

    public void handleClear(Button track, TextView clear, int index){
        clear.setVisibility(View.INVISIBLE);
        track.setBackgroundColor(0xFF64dd17);
        audioTrackArray[index].clear();
    }

    AudioTrack initATrack(){
        AudioTrack at = null;
        int maxJitter = AudioTrack.getMinBufferSize(rec.getSampleRate(), AudioFormat.CHANNEL_OUT_MONO, rec.getAudioFormat());
        at = new AudioTrack(AudioManager.MODE_IN_COMMUNICATION, rec.getSampleRate(), AudioFormat.CHANNEL_OUT_MONO,
                rec.getAudioFormat(), maxJitter, AudioTrack.MODE_STREAM);
        return at;
    }

    void onRecord(final AudioTrackData atd, final TextView clear, final Button btn){
        if(!isRecording){
            //reinitialize buffer, set bool
            isRecording = true;
            if (rec == null){
                rec = findAudioRecord();
            }

            (new Thread(){
                @Override
                public void run(){
                    Log.v("main", "Started recording Thread");
                    recording(atd, btn);
                    Log.v("main", "Close recording Thread");

                }
            }).start();

        }else{
            btn.setBackgroundColor(0xFFffab00);
            clear.setVisibility(View.VISIBLE);
            isRecording = false;
        }
    }

    void onPlay(final AudioTrackData atd, ImageButton playImg){
        if(!atd.isPlaying()){
            atd.setPlaying(true);
            if(playImg != null){
                playImg.setVisibility(View.VISIBLE);
            }

            atd.setAudioTrack(initATrack());
            (new Thread(){
                @Override
                public void run(){
                    Log.v("audioshit", "Started playing Thread for Track" + atd);
                    play(atd);
                    Log.v("audioshit", "Close playing Thread for Track" + atd);
                }
            }).start();
        }else{
            if(playImg != null){
                playImg.setVisibility(View.INVISIBLE);
            }
            atd.pause();

        }
    }

    void recording(AudioTrackData atd,  Button btn){
        short[] input = new short[1024];
        rec.startRecording();
        int samples = 0;
        Log.v("main", "start writing bufferchunks");
        while(isRecording){
            samples = rec.read(input, 0, 1024);
            atd.fillAudioBuffer(new BufferTuple(input, samples));
        }
        rec.stop();
        //todo not setting color properly
        Log.v("data","stop recordng");
        atd.setRecorded(true);
    }

    void play(AudioTrackData atd){
        atd.getAudioTrack().play();
        Log.v("audioshit", "start reading and looping buffer");
        //short[] buffer = atd.getAudioBuffer().get(2).buffer;
        while (atd.isPlaying()){
            for (int i = 0; i < atd.getAudioBuffer().size(); i++){
                atd.getAudioTrack().write(atd.getAudioBuffer().get(i).buffer, 0, atd.getAudioBuffer().get(i).samples);
            }
        }
    }

    void playMix(){
        for(AudioTrackData atd : audioTrackArray){
            atd.pause();
        }

        int recorded = 0;
        ArrayList<AudioTrackData> atdList = new ArrayList<AudioTrackData>();
        for(int i = 0; i< 8; i++) {
            if (audioTrackArray[i].isRecorded()) {
                recorded++;
                atdList.add(audioTrackArray[i]);
            }
        }
        AudioTrackData[] allSamples= new AudioTrackData[recorded];
        allSamples = atdList.toArray(allSamples);
        short[] mix =  AudioMixer.combineTracks(allSamples);

        audioMix.clear();
        audioMix.getAudioBuffer().add(new BufferTuple(mix,mix.length));
        onPlay(audioMix,null);
    }

    void stopMix(){
        audioMix.pause();
    }


    //Find The right stuff for your recording hardware on your phone
    //https://stackoverflow.com/questions/29695269/android-audiorecord-audiotrack-playing-recording-from-buffer
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

    public boolean saveWav(){
        //get mix
        for(AudioTrackData atd : audioTrackArray){
            atd.pause();
        }

        int recorded = 0;
        ArrayList<AudioTrackData> atdList = new ArrayList<AudioTrackData>();
        for(int i = 0; i< 8; i++) {
            if (audioTrackArray[i].isRecorded()) {
                recorded++;
                atdList.add(audioTrackArray[i]);
            }
        }
        AudioTrackData[] allSamples= new AudioTrackData[recorded];
        allSamples = atdList.toArray(allSamples);
        short[] mix =  AudioMixer.combineTracks(allSamples);
        //Bits per Sample maybe some work to be done for PCM8 format
        byte bitspersample = 16;
        int channelcount = 2;
        switch (rec.getChannelCount()){
            case AudioFormat.CHANNEL_IN_MONO:
                channelcount = 1;
                break;
            case AudioFormat.CHANNEL_IN_STEREO:
                channelcount = 2;
                break;
        }
        //save data half samplerate because otherwise its too fast
        WavExport export = new WavExport(bitspersample, rec.getSampleRate()/2, rec.getChannelCount(), rec.getAudioFormat());


        try {
            export.exportAsWav(mix);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.v("wavexport", "FileNotFoundException, something wrong with the File to write");
            return false;
        } catch(IOException e){
            e.printStackTrace();
            Log.v("wavexport", "IOException, something wrong with the Filestream");
            return false;
        }


    }
}
