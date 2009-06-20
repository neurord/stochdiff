package org.catacomb.numeric.data;

import org.catacomb.datalish.Box;
import org.catacomb.datalish.array.Array;



public class EventSequence implements NumDataItem {

    String name;

    int nevent;
    double[] times;
    int[] channels;
    int maxch;

    public EventSequence(String s) {
        name = s;
        times = new double[10];
        channels = new int[10];
        maxch = 0;
    }


    public String getUnit() {
        return null;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return name;
    }

    public void addEvent(double t, int ich) {
        if (nevent >= times.length) {
            int nn = nevent + nevent / 2 + 10;
            times = Array.extendDArray(times, nevent, nn);
            channels = Array.extendIArray(channels, nevent, nn);
        }
        times[nevent] = t;
        channels[nevent] = ich;
        nevent += 1;
        if (ich > maxch) {
            maxch = ich;
        }
    }

    public int getNEvent() {
        return nevent;
    }

    public int[] getChannels() {
        return channels;
    }

    public double[] getTimes() {
        return times;
    }


    public Box getLimitBox() {
        Box ret = null;

        if (nevent == 0) {
            ret = new Box();
        } else {
            ret = new Box(times[0], 0, times[nevent-1], maxch + 1.);
        }
        return ret;
    }

}
