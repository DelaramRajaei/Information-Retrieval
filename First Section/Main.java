import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Delaram Rajaei 9731084
 * IR Project
 */

public class Main {
    public static ArrayList<String> docContent = new ArrayList<>();
    public static HashMap<Integer, String> documents = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // Get input
        readAFile(docContent, documents);

        // Creating Inverted Index
        InvertedIndex.createInvertedIndex(docContent);

        // Query Processing
        String query = getQuery();
        queryProcessor(query);
    }

    private static void queryProcessor(String query) {
        ArrayList<String> queries = new ArrayList<>();
        for (String plural : InvertedIndex.getPluralWords()) {
            if (query.contains(plural)) {
                queries.add(plural);
                query.replace(plural, "");
            }
        }
        String[] split = query.split("\\s+");
        for (int i = 0; i < split.length; i++) {
            queries.add(split[i]);
        }
        if (queries.size() == 1)
            singleQuery(query);
        else multipleQuery(queries);
    }

    private static void multipleQuery(ArrayList<String> queries) {
        HashMap<Integer, Integer> relatedDocs = new HashMap<>(); // DocId - Number of times the docID mentioned!
        for (String query : queries) {
            ArrayList<Integer> docIDs = InvertedIndex.search(query);
            for (int id : docIDs) {
                if (relatedDocs.containsKey(id))
                    relatedDocs.put(id, relatedDocs.get(id) + 1);
                else relatedDocs.put(id, 1);
            }
        }
        LinkedHashMap<Integer, Integer> reverseSortedMap = new LinkedHashMap<>();
        relatedDocs.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
        for (Map.Entry<Integer, Integer> mapElement : reverseSortedMap.entrySet()) {
            System.out.println("Most to least related documents:\nDocument ID is: " + mapElement.getKey()
                    + "\t URL: " + documents.get(mapElement.getKey()));
        }
    }

    private static void singleQuery(String query) {
        ArrayList<Integer> docIDs = InvertedIndex.search(query);
        Collections.sort(docIDs);
        for (int id : docIDs) {
            System.out.println("Document ID is: " + id + "\t URL: " + documents.get(id));
        }
    }

    private static String getQuery() {
        Scanner scan = new Scanner(System.in);
        String query = "";
        System.out.println("Please Write down your query: ");
        query = scan.nextLine();
        return query;
    }

    private static void readAFile(ArrayList<String> docContent, HashMap<Integer, String> documents) throws IOException {
        FileInputStream file = new FileInputStream(new File("IR_Spring2021_ph12_7k.xlsx"));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            documents.put(row.getRowNum(), row.getCell(2).getStringCellValue());
            docContent.add(row.getCell(1).getStringCellValue());
        }
    }
}
