package ema.simpleaudiolooper;

/**
 * Created by milan_000 on 19.12.2017.
 */

//Class to keep the buffer with the number of grabbed samples together in the list
public class BufferTuple {
    public final short[] buffer;
    public final int samples;

    public BufferTuple(short[] buffer, int samples){
        this.buffer = buffer.clone();
        this.samples = samples;
    }

}
