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
    public static HashMap<Integer, String> documents = new HashMap<>();
    public static MaxHeap score;
    public static Scanner scan = new Scanner(System.in);
    public static int k = 5;

    public static void main(String[] args) throws IOException {
        String[] fileNames = {"IR00_3_11k News.xlsx", "IR00_3_17k News.xlsx", "IR00_3_20k News.xlsx"};
        for (int i = 0; i < fileNames.length; i++) {
            // Get input
            readAFile(fileNames[i],0);
        }

        // Query Processing
        String query = getQuery();
        System.out.println("Choose a number:\n1.K_means\n2.KNN");
        int option = scan.nextInt();
        System.out.println("File name or path: ");
        readAFile(scan.next(),option);

        // Print the result
        printResult(score);
    }

    private static void printResult(MaxHeap heap) {
        int id;
        for (int i = 0; i < k; i++) {
            id = heap.extractMax().getFirst();
            System.out.println(i + 1 + ": " + id + "->" + documents.get(id));
        }
    }


    private static String getQuery() {
        String query = "";
        System.out.println("Please Write down your query: ");
        query = scan.nextLine();
        return query;
    }

    private static void readAFile(String fileName, int option) throws IOException {
        FileInputStream file = new FileInputStream(new File(fileName));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getCell(0) == null) break;
            if (row.getRowNum() == 0) continue;
            String className;
            String url;
            if (row.getCell(2).getStringCellValue().length() > 8) {
                url = row.getCell(2).getStringCellValue();
                className = "";
            } else {
                url = row.getCell(3).getStringCellValue();
                className = row.getCell(2).getStringCellValue();
            }
            documents.put(row.getRowNum(), url);
            InformationRetrieval.addDocument(className, row.getCell(1).getStringCellValue(),option);
        }
    }

}
//test.xlsx