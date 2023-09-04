import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GenerateRq2Table {
    public static void main(String [] args) {
        String file = "phase2-analysis.csv";
        String delimiter = ",";
        String headers;
        String line;
        List<List<String>> lines = new ArrayList();

        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            headers = br.readLine();
            while((line = br.readLine()) != null) {
                List<String> values = Arrays.asList(line.split(delimiter));
                lines.add(values);
            }
            lines.forEach(l -> System.out.println(l));
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
