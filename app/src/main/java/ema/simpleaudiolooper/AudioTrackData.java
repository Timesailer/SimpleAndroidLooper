package ema.simpleaudiolooper;

import android.media.AudioTrack;
import android.provider.MediaStore;

import java.nio.Buffer;
import java.util.ArrayList;

/**
 * Created by milan_000 on 16.01.2018.
 */

public class AudioTrackData {
    private AudioTrack at = null;
    private ArrayList<BufferTuple> audiobuffer = new ArrayList<BufferTuple>();

    private boolean isRecorded = false;
    private boolean isPlaying = false;

    AudioTrack getAudioTrack(){
        return at;
    }

    ArrayList<BufferTuple> getAudioBuffer(){
        return audiobuffer;
    }

    void fillAudioBuffer(BufferTuple bt){
        this.audiobuffer.add(bt);
    }

    void setAudioTrack(AudioTrack at){
        this.at = at;
    }

    boolean isRecorded(){
        return isRecorded;
    }

    void setRecorded(boolean isRecorded){
        this.isRecorded = isRecorded;
    }

    boolean isPlaying() {return isPlaying;}

    void setPlaying(boolean isPlaying){
        this.isPlaying = isPlaying;
    }

    void clear(){
        this.audiobuffer = new ArrayList<BufferTuple>();
        this.at = null;
        this.isRecorded = false;
        this.isPlaying = false;
    }
}
