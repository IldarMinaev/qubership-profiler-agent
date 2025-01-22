package org.qubership.profiler.agent;

import java.util.Arrays;

public class LocalBuffer {
    public final static int SIZE = Integer.getInteger(LocalBuffer.class.getName() + ".SIZE", 4096);
    volatile public LocalState state;
    public LocalBuffer prevBuffer;

    public final long[] data = new long[SIZE];
    public final Object[] value = new Object[SIZE];
    public long startTime;
    public int count;
    public int first;
    public boolean corrupted;

    public LocalBuffer() {
        init(null);
    }

    public void init(LocalBuffer prevBuffer) {
        startTime = TimerCache.now;
        count = 0;
        first = 0;
        this.prevBuffer = prevBuffer;
    }

    public void event(Object contents,
                      int tagId) {
        int r = count;
        long[] data = this.data;
        if (r >= 0 && r < data.length) {
            data[r] = tagId | TimerCache.timerSHL32;
            value[r] = contents;
            count = r + 1;
        } else {
            LocalState state = this.state;
            Profiler.exchangeBuffer(this);
            state.buffer.event(contents, tagId);
        }
    }

//    public LocalState getLocalState(){
//        if(this.state != null ){
//            return state;
//        }
//        if(!Thread.currentThread().isAlive()){
//            System.err.println("Thread reported that it is not alive. which is wierd. local state null will be returned. Thread: " + Thread.currentThread().getName());
//            return null;
//        }
//        System.out.println("Local state is null for thread  " + Thread.currentThread().getName());
//        return null;
//    }

    public void initEnter(int methodId) {
        initTimedEnter(methodId | TimerCache.timerSHL32);
    }

    public void initEnter(long methodAndTime) {
        initTimedEnter(methodAndTime);
    }

    public void initTimedEnter(long methodAndTime) {
        int r = count;
        long[] data = this.data;
        if (r >= 0 && r < data.length) {
            data[r] = methodAndTime;
            count = r + 1;
        } else {
            Profiler.exchangeBuffer(this, methodAndTime);
        }
    }

    public void initExit() {
        initTimedExit(TimerCache.timerSHL32);
    }

    public void initTimedExit(long time) {
        int r = count;
        long[] data = this.data;
        if (r >= 0 && r < data.length) {
            data[r] = time;
            count = r + 1;
        } else {
            Profiler.exchangeBuffer(this, time);
        }
    }

    public void reset() {
        if (this.first < this.count && this.first >= 0)
            Arrays.fill(this.value, this.first, this.count, null);
        this.first = 0;
        this.count = 0;
        this.prevBuffer = this; // Ensure Dumper will not think this is the first buffer in thread
        this.corrupted = false;
    }

    @Override
    public String toString() {
        return "LocalBuffer{" + System.identityHashCode(this) + " " +
                "corrupted=" + corrupted +
                ", state=" + System.identityHashCode(state) +
                ", count=" + count +
                ", first=" + first +
                ", startTime=" + startTime +
                ", data=" + StringUtils.arrayToString(new StringBuilder(), Arrays.copyOf(data, 20)) +
                ", value=" + StringUtils.arrayToString(new StringBuilder(), Arrays.copyOf(value, 20)) +
                ", prevBuffer=" + System.identityHashCode(prevBuffer) +
                '}';
    }

    public void command(byte commandId, Object result) {
        count = -1;
        data[0] = commandId;
        value[0] = result;
    }
}
