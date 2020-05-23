package pl.polsl.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Node {

    @CsvBindByName(column = "id")
    private int id;

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "x")
    private int x;

    @CsvBindByName(column = "y")
    private int y;

    @CsvBindByName(column = "demand")
    private double demand;

    public boolean wasVisited = false;
}
