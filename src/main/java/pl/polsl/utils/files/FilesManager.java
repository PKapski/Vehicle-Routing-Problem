package pl.polsl.utils.files;

import com.opencsv.bean.CsvToBeanBuilder;
import pl.polsl.model.Node;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

public class FilesManager {

    @SuppressWarnings("unchecked")
    public List<Node> loadNodesFromCSV(String fileName) throws FileNotFoundException {
        FileReader inputFile = new FileReader(fileName);
        List<Node> nodes = new CsvToBeanBuilder(inputFile).withSeparator(';').withType(Node.class).build().parse();
        for (int i=0; i<nodes.size(); i++) {
            nodes.get(i).setId(i);
        }
        return nodes;
    }
}
