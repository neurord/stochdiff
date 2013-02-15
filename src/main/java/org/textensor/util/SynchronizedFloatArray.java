package org.textensor.util;

public class SynchronizedFloatArray {
    protected final float[] data;
    int head, size;

    public SynchronizedFloatArray(int capacity) {
        this.data = new float[capacity];
    }

    public synchronized void put(float d) {
        while (this.size == this.data.length)
            try {
                wait();
            } catch (InterruptedException e) {}
        this.data[ (this.head + this.size) % this.data.length ] = d;
        this.size++;
        notifyAll();
    }

    public synchronized float take() {
        while (this.size == 0)
            try {
                wait();
            } catch (InterruptedException e) {}
        float ans = this.data[this.head];
        this.head = (this.head + 1) % this.data.length;
        this.size--;
        notifyAll();
        return ans;
    }

    public synchronized int size() {
        return this.size;
    }
}
