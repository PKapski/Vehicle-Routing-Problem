package pl.polsl.model;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;

@Data
@ToString
public class Node {

    private int id;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "x")
    private int x;

    @CsvBindByName(column = "y")
    private int y;

    @CsvBindByName(column = "demand")
    private double demand;

    @CsvBindByName(column = "availableFrom")
    @CsvDate("HH:mm")
    private LocalTime availableFrom;

    @CsvBindByName(column = "availableTo")
    @CsvDate("HH:mm")
    private LocalTime availableTo;

    @CsvBindByName(column = "serviceTime")
    private int serviceTime;

    public boolean visited = false;

    public boolean isVisited() {
        return visited;
    }
}
