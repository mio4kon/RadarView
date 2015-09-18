package mio.kon.radarview;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * Created by mio on 15-9-17.
 */
public class RadarMap extends LinkedHashMap<String, Float> implements Subject {

    private List<Observer> observers;

    public RadarMap() {
        observers = new ArrayList<> ();
    }

    @Override
    public Float put(String key, Float value) {
        notifyObservers ();
        //can check value
        return super.put (key, value);
    }

    @Override
    public void registerObserver(Observer o) {
        observers.add (o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove (o);
    }

    @Override
    public void notifyObservers() {
        for(int i = 0; i < observers.size (); i++) {
            observers.get (i).update (this);
        }
    }
}
