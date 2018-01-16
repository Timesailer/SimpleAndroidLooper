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

    public static short[] combineTracks(ArrayList<BufferTuple>... audiodata){
        int nrOfTracks = audiodata.length;
        short[][] samples = new short[nrOfTracks][];
        //Extract all samples in shortArrays
        for(int tracknr = 0; tracknr < nrOfTracks; tracknr++){
            ArrayList<BufferTuple> tracktuples = audiodata[tracknr];
            //figure out nr of samples for new short[]
            int length = 0;
            for(int tuplenr = 0; tuplenr < tracktuples.size(); tuplenr++){
                length += tracktuples.get(tuplenr).samples;
            }
            samples[tracknr] = new short[length];
            int extractedSampleIndex = 0;
            //extract relevant samples into new short[]
            for(int tuplenr = 0; tuplenr < tracktuples.size(); tuplenr++){
                for(int samplenr = 0; samplenr < tracktuples.get(tuplenr).samples; samplenr++){
                    samples[tracknr][extractedSampleIndex] = tracktuples.get(tuplenr).buffer[samplenr];
                    extractedSampleIndex++;
                }
            }
        }
        //compare lengths


        //check for overflow and add
        return null;
    }
}
