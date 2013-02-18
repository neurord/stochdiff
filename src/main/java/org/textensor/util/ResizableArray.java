package org.textensor.util;

import java.util.Arrays;

public abstract class ResizableArray {
    protected int size, offset;

    public final int FACTOR = 2;

    public ResizableArray(int capacity) {
        this.size = this.offset = 0;
    }

    public int size() {
        return this.size;
    }

    public void move(int howmany) {
        this.offset += howmany;
        assert 0 <= this.offset && this.offset <= this.size;
    }

    public int remaining() {
        return this.size - this.offset;
    }

    public static class Float extends ResizableArray {
        protected float[] data;

        public Float(int capacity) {
            super(capacity);
            this.data = new float[capacity];
        }

        public void put(float datum) {
            if (this.size == this.data.length)
                this.data = Arrays.copyOf(this.data, this.data.length * FACTOR);
            assert this.size < this.data.length;
            this.data[this.size++] = datum;
        }

        public float take() {
            return this.take(1);
        }

        public float take(int usage) {
            if (this.offset + usage > this.size)
                throw new RuntimeException("overflow");
            float ans = this.data[this.offset];
            this.offset += usage;
            return ans;
        }

        public float get(int offset) {
            assert offset < this.size;
            return this.data[offset];
        }
    }

    public static class Double extends ResizableArray {
        protected double[] data;

        public Double(int capacity) {
            super(capacity);
            this.data = new double[capacity];
        }

        public void put(double datum) {
            if (this.size == this.data.length)
                this.data = Arrays.copyOf(this.data, this.data.length * FACTOR);
            assert this.size < this.data.length;
            this.data[this.size++] = datum;
        }

        public double take() {
            return this.take(1);
        }

        public double take(int usage) {
            if (this.offset + usage > this.size)
                throw new RuntimeException("overflow");
            double ans = this.data[this.offset];
            this.offset += usage;
            return ans;
        }

        public double get(int offset) {
            assert offset < this.size;
            return this.data[offset];
        }
    }

    public static class Int extends ResizableArray {
        protected int[] data;

        public Int(int capacity) {
            super(capacity);
            this.data = new int[capacity];
        }

        public void put(int datum) {
            if (this.size == this.data.length)
                this.data = Arrays.copyOf(this.data, this.data.length * FACTOR);
            assert this.size < this.data.length;
            this.data[this.size++] = datum;
        }

        public int take() {
            return this.take(1);
        }

        public int take(int usage) {
            if (this.offset + usage > this.size)
                throw new RuntimeException("overflow");
            int ans = this.data[this.offset];
            this.offset += usage;
            return ans;
        }

        public int get(int offset) {
            assert offset < this.size;
            return this.data[offset];
        }
    }
}
