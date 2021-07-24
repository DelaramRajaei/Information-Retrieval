import java.io.*;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InvertedIndex {

    private static HashMap<String, ArrayList<Integer>> dictionary = new HashMap<>();
    private static ArrayList<String> endExceptions = new ArrayList<>();
    private static ArrayList<String> beginExceptions = new ArrayList<>();
    private static ArrayList<String> pluralWords = new ArrayList<String>();
    private static int docID = 1;
    private static String fileName = "Dictionary.txt";
    private static File dictionaryFile = new File(fileName);

    public static ArrayList<String> getPluralWords() {
        return pluralWords;
    }

    /**
     * Create an invertedIndex with given documents!
     *
     * @param docs
     * @throws IOException
     */
    public static void createInvertedIndex(ArrayList<String> docs) throws IOException {
            setPluralWords();
            for (String content : docs) {
                String[] tokens = extractTokens(content);
                createDictionary(tokens, docID++);
            }
    }

    private static void readFromFile() throws FileNotFoundException {
        Scanner myReader = new Scanner(dictionaryFile);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] split = data.split(":");
            ArrayList<Integer> temp = new ArrayList<>();
            for (int i = 1; i < split.length; i++) {
                temp.add(Integer.parseInt(split[i]));
            }
            dictionary.put(split[0],temp);
        }
    }

    private static void saveFile() throws IOException {
        PrintWriter myWriter = new PrintWriter(fileName);
        myWriter.println("This is the Dictionary\n");
        myWriter.flush();
        for (String term : dictionary.keySet()) {
            myWriter.println(term + ": " + dictionary.values());
            myWriter.flush();
        }
        myWriter.close();
    }

    /**
     * Read excel and extract plural words.
     *
     * @throws IOException
     */
    private static void setPluralWords() throws IOException {
        FileInputStream file = new FileInputStream(new File("Normalization.xlsx"));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            // کلمات مرکب
            if (String.valueOf(row.getCell(6)) != null) {
                pluralWords.add(String.valueOf(row.getCell(6)).trim());
            }
        }
    }

    private static void normalizeTokens() throws IOException {
        FileInputStream file = new FileInputStream(new File("Normalization.xlsx"));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue;
            // جمع های مکسر
            checkTerm(row, 1);
            // ریشه فعل
            checkTerm(row, 5);
            // استئناها
            if (String.valueOf(row.getCell(2)) != null) endExceptions.add(String.valueOf(row.getCell(2)).trim());
            if (String.valueOf(row.getCell(3)) != null) beginExceptions.add(String.valueOf(row.getCell(3)).trim());
        }
        // Preventing data conversion
        ArrayList<String> temp = new ArrayList<>();
        temp.addAll(dictionary.keySet());
        for (Iterator<String> iterator = temp.iterator(); iterator.hasNext(); ) {
            String term = iterator.next();
            // یکسان سازی حروف و کلمات چند جزئی
            // Removing numbers
            try {
                double d = Double.parseDouble(term);
                dictionary.remove(term);
            } catch (NumberFormatException nfe) {
            }
            // Remove HTML tags
            term.replace("<.*>", "").trim();
            // حذف پیشوندها و پسوندها
            if (term.endsWith("ات") || term.endsWith("ان") || term.endsWith("ها") || term.endsWith("ی")) {
                if (!endExceptions.contains(term)) {
                    String newKey = "";
                    if (term.endsWith("ی")) newKey = term.substring(0, term.length() - 1);
                    else newKey = term.substring(0, term.length() - 2);
                    updateKey(term, newKey);
                    dictionary.remove(term);
                }
            } else if (term.startsWith("می")) {
                if (!beginExceptions.contains(term)) {
                    String newKey = term.substring(2, term.length());
                    updateKey(term, newKey);
                    dictionary.remove(term);
                }
            }
        }
    }


    /**
     * Check if the term is in the row,
     * if it is true then update the key value.
     *
     * @param row
     * @param cellNum
     */
    private static void checkTerm(Row row, int cellNum) {
        String oldKey = String.valueOf(row.getCell(cellNum));
        String newKey = String.valueOf(row.getCell(cellNum - 1));
        updateKey(oldKey, newKey);
        dictionary.remove(String.valueOf(oldKey));
    }

    /**
     * Update a key in hashmap.
     *
     * @param oldKey
     * @param newKey
     */
    private static void updateKey(String oldKey, String newKey) {
        if (dictionary.containsKey(oldKey)) {
            if (dictionary.containsKey(newKey))
                dictionary.get(newKey).addAll(dictionary.get(oldKey));
            else
                dictionary.put(String.valueOf(newKey), new ArrayList<>(dictionary.get(oldKey)));
        }
    }

    /**
     * First check if it contains any plural word extract it separately.
     * Get the content of a document and split it by space.
     *
     * @param content Content of a document
     */
    private static String[] extractTokens(String content) throws IOException {
        for (String plural : pluralWords) {
            if (content.contains(plural)) {
                addIDToDictionary(plural, docID);
                content.replace(plural, "");
            }
        }
        String[] tokens = content.split("\\s+");
        return tokens;
    }

    /**
     * Check if the term repeated in almost every document (more than 2/3 documents),
     * if it repeated too much, it will be removed from dictionary.
     * There should be more than 10 documents to search for repeated words!
     */
    private static void deleteRepetitiveWords() {
        if (docID > 10) {
            for (Iterator<String> iterator = dictionary.keySet().iterator(); iterator.hasNext(); ) {
                String term = iterator.next();
                if (Math.ceil(docID * 2 / 3) <= dictionary.get(term).size())
                    iterator.remove();
            }
        }
    }

    /**
     * Add DocID to the list of given key!
     *
     * @param key
     * @param id
     */
    private static void addIDToDictionary(String key, int id) {
        if (dictionary.containsKey(key)) {
            if (!dictionary.get(key).contains(id))
                dictionary.get(key).add(id);
        } else {
            ArrayList<Integer> docIDs = new ArrayList<>();
            docIDs.add(id);
            dictionary.put(key, docIDs);
        }
    }

    /**
     * Create a dictionary from the extracted tokens.
     * The dictionary is list of words and a pointer to the list of docIDs.
     *
     * @param tokens splitted words by space
     * @param id     document ID
     */
    private static void createDictionary(String[] tokens, int id) throws IOException {
        for (int i = 0; i < tokens.length; i++) {
            addIDToDictionary(tokens[i], id);
        }
        deleteRepetitiveWords();
        normalizeTokens();
    }

    public static ArrayList<Integer> search(String query) {
        return dictionary.get(query);
    }
}
