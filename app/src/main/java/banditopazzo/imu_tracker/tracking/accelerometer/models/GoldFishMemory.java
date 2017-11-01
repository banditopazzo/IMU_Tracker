package banditopazzo.imu_tracker.tracking.accelerometer.models;

import java.util.Iterator;
import java.util.LinkedList;

public class GoldFishMemory<E> {
    private int maxSize;
    private LinkedList<E> data;

    public GoldFishMemory(int maxSize) {
        this.maxSize = maxSize;
        this.data = new LinkedList<>();
    }

    public void remember(E item){
        if (isFull()){
            data.removeFirst();
        }
        data.add(item);
    }

    public boolean isFull() {
        return data.size() == maxSize;
    }

    public int currentSize() {
        return data.size();
    }

    public int maxSize() {
        return maxSize;
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public Iterator<E> iterator() {
        return data.iterator();
    }

    public E getLast() {
        return data.getLast();
    }

}
