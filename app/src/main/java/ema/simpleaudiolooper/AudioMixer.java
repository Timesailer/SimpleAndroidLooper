package ema.simpleaudiolooper;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by milan_000 on 12.01.2018.
 * Mixes Multiple Audiotracks
 */

/**
 * LAYOUTNOTIZEN
 * Grün - freie Audiospur - colorPrimary
 * Orange - Aufgenommene Audiospur - colorPrimaryDark
 * Rot - Aufnahme - colorAccent
 */

public class AudioMixer {

    static short[] combineTracks(AudioTrackData[] audiodata){
        int nrOfTracks = audiodata.length;
        short[][] samples = new short[nrOfTracks][];
        //Extract all samples in shortArrays
        for(int tracknr = 0; tracknr < nrOfTracks; tracknr++){
            samples[tracknr] = convertToShortArray(audiodata[tracknr].getAudioBuffer());
        }

        //compare lengths
        int longestSample = 0;
        for(int i = 0; i < samples.length; i++){
            if(samples[i].length > longestSample){
                longestSample = samples[i].length;
            }
        }

        //check for overflow and add
        short[] mix = new short[longestSample*3];
        //initialisiere mixfeld
        for(int i = 0; i < mix.length; i++){
            mix[i] = 0;
        }
        //fill mix
        for(int mixindex = 0; mixindex < mix.length; mixindex++){
            for(int trackindex = 0; trackindex < nrOfTracks; trackindex++){
                int sampleindex = mixindex;
                while(sampleindex >= samples[trackindex].length){
                    sampleindex = sampleindex - samples[trackindex].length;
                }
                if(mix[mixindex] + samples[trackindex][sampleindex] < Short.MAX_VALUE){
                    if(mix[mixindex] + samples[trackindex][sampleindex] > Short.MIN_VALUE){
                        mix[mixindex] = (short)(mix[mixindex] + samples[trackindex][sampleindex]);
                    }else{
                        mix[mixindex] = Short.MIN_VALUE;
                    }
                }else{
                    mix[mixindex] = Short.MAX_VALUE;
                }
            }
        }
        return mix;
    }

    public static short[] convertToShortArray(ArrayList<BufferTuple> tracktuples){
        //figure out nr of samples for new short[]
        int length = 0;
        for(int tuplenr = 0; tuplenr < tracktuples.size(); tuplenr++){
            length += tracktuples.get(tuplenr).samples;
        }
        short[] samples = new short[length];
        int extractedSampleIndex = 0;
        //extract samples into new short[]
        for(int tuplenr = 0; tuplenr < tracktuples.size(); tuplenr++){
           for(int samplenr = 0; samplenr < tracktuples.get(tuplenr).samples; samplenr++){
                samples[extractedSampleIndex] = tracktuples.get(tuplenr).buffer[samplenr];
                extractedSampleIndex++;
            }
        }
        return samples;
    }
}
