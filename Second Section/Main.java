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
    public static int k = 5;

    public static void main(String[] args) throws IOException {
        // Get input
        long start = System.currentTimeMillis();
        readAFile(docContent, documents);

        // Creating Inverted Index
        InvertedIndex.createInvertedIndex(docContent);

        // Query Processing
        String query = getQuery();
        MaxHeap heap = queryProcessor(query);

        // Print the result
        printResult(heap);
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        System.out.println(elapsedTime);
    }

    private static void printResult(MaxHeap heap) {
        int id = 0;
        for (int i = 0; i < k; i++) {
            id = heap.extractMax().getFirst();
            System.out.println(i+1 + ": " + id + "->" +documents.get(id));
        }
    }

    private static MaxHeap queryProcessor(String query) throws IOException {
        String[] queries = InvertedIndex.extractTokens(query);
        HashMap<String, Double> weightQueries = InvertedIndex.calculate_tf_idf_query(queries);
        double[] score = InvertedIndex.cosineScore(weightQueries);
        MaxHeap heap = new MaxHeap(score.length);
        for (int i = 0; i < score.length; i++) {
            heap.insert(i, score[i]);
        }
        return heap;
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
            if (row.getCell(0) == null) break;
            if (row.getRowNum() == 0) continue;
            documents.put(row.getRowNum(), row.getCell(2).getStringCellValue());
            docContent.add(row.getCell(1).getStringCellValue());
        }
    }
}
//test.xlsx