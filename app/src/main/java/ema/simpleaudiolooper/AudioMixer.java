package ema.simpleaudiolooper;

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

    static short[] combineTracks(ArrayList<BufferTuple>... audiodata){
        int nrOfTracks = audiodata.length;
        short[][] samples = new short[nrOfTracks][];
        //Extract all samples in shortArrays
        for(int tracknr = 0; tracknr < nrOfTracks; tracknr++){
            ArrayList<BufferTuple> tracktuples = audiodata[tracknr];
            samples[tracknr] = convertToShortArray(audiodata[tracknr]);
        }

        //compare lengths
        int longestSample = 0;
        int longestIndex = -1;
        for(int i = 0; i < samples.length; i++){
            if(samples[i].length > longestSample){
                longestSample = samples.length;
                longestIndex = i;
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
                while(sampleindex > samples[trackindex].length){
                    sampleindex = sampleindex - samples[trackindex].length;
                }
                if(mix[mixindex] + samples[trackindex][sampleindex] < Short.MAX_VALUE){
                    mix[mixindex] = (short)(mix[mixindex] + samples[trackindex][sampleindex]);
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
