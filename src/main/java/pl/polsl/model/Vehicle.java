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
    private double currentLoad;
    private int routeTime;
    private int routeWaitingTime;

    public Vehicle(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        currentLoad = capacity;
    }

    public Vehicle(Vehicle veh) {
        this.id = veh.getId();
        this.capacity = veh.getCapacity();
        this.currentLoad = veh.getCurrentLoad();
        this.routeTime = veh.getRouteTime();
        this.routeWaitingTime = veh.getRouteWaitingTime();
    }

    public void decrementCurrentLoad(double value) {
        currentLoad -= value;
    }
}
