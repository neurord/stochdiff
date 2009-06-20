package org.catacomb.be;




public final class TimePoint {

    int istep;

    double dt;
    double time;
    double previousTime;
    double runtime;
    Timestep timestep;



    public TimePoint() {
        time = 0.;
        dt = 1.;
        runtime = 100.;
        timestep = new BasicTimestep(dt);
    }

    public TimePoint(double dt, double t0) {
        istep = 0;
        this.dt = dt;
        this.time = t0;
        this.previousTime = t0 - dt;
    }


    public void setDt(double d) {
        dt = d;
        timestep = new BasicTimestep(dt);
    }

    public void setRuntime(double d) {
        runtime = d;
    }


    public boolean isFinished() {
        return (time >= runtime);
    }

    public double getProgressFraction() {
        if (runtime <= 0) {
            runtime = 1.;
        }
        return time / runtime;
    }

    public String getProgressDescription() {
        Long ns = new Long(Math.round(runtime / dt));
        Integer iso = new Integer(istep > 0 ? istep-1 : 0);
        String ret = String.format("step %d of %d (t=%.4g)", iso, ns, new Double(time));
//      return "step " + istep + " of " + ns + " t=" + time;
        return ret;
    }

    public double getTime() {
        return time;
    }

    public double getPreviousTime() {
        return previousTime;
    }


    public double getDt() {
        return dt;
    }

    public int getStep() {
        return istep;
    }

    public void increment() {
        previousTime = time;
        time += dt;
        istep += 1;
    }

    public Timestep getTimestep() {
        return timestep;
    }



}
