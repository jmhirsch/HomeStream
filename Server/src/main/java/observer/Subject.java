package observer;

import java.util.HashSet;
import java.util.Set;

public abstract class Subject {
    private Set<Observer> observersList = new HashSet<>();


    //MODIFIES: Observers
    //EFFECTS: calls update on each observer
    public void notifyObservers() {
        for (Observer observer : observersList) {
            observer.update();
        }
    }

    public void notifyObservers(Object obj){
        for (Observer observer: observersList){
            observer.update(obj);
        }
    }

    //MODIFIES: this
    // EFFECTS: adds observer to observer list
    public void addObserver(Observer observer) {
        observersList.add(observer);
    }

    //MODIFIES: this
    // EFFECTS: removes specified observer from list
    public void removeObserver(Observer observer) {
        observersList.remove(observer);
    }
}
