package ema.simpleaudiolooper;

import android.media.AudioTrack;
import android.provider.MediaStore;

/**
 * Created by milan_000 on 16.01.2018.
 */

public class AudioTrackData {
    AudioTrack at = null;
    boolean isRecorded = false;

    public  AudioTrack getAudioTrack(){
        return at;
    }

    public void setAudioTrack(AudioTrack at){
        this.at = at;
    }

    public boolean isRecorded(){
        return isRecorded;
    }

    public void setRecorded(boolean isRecorded){
        this.isRecorded = isRecorded;
    }
}
