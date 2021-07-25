package pl.polsl.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Vehicle {

    private int id;
    private double capacity;
    private double currentFreeLoad;
    private int routeTime;
    private int routeWaitingTime;

    public Vehicle(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        currentFreeLoad = capacity;
    }

    public Vehicle(Vehicle veh) {
        this.id = veh.getId();
        this.capacity = veh.getCapacity();
        this.currentFreeLoad = veh.getCurrentFreeLoad();
        this.routeTime = veh.getRouteTime();
        this.routeWaitingTime = veh.getRouteWaitingTime();
    }

    public void incrementCurrentFreeLoad(double value) {
        currentFreeLoad += value;
    }

    public void decrementCurrentFreeLoad(double value) {
        currentFreeLoad -= value;
    }
}
