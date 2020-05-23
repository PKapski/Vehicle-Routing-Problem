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
    private int currentNode = 0;

    public Vehicle(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
        currentLoad = capacity;
        currentNode = 0;
    }
}
