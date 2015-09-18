package mio.kon.radarview;

/**
 * Created by mio on 15-9-17.
 */
public interface Subject {
     void registerObserver(Observer o);

     void removeObserver(Observer o);

     void notifyObservers();
}
