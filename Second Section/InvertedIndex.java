import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.fa.PersianNormalizer;
import org.apache.lucene.analysis.fa.PersianNormalizationFilter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InvertedIndex {

    private static int docID = 1;
    private static String fileName = "Dictionary.txt";
    private static File dictionaryFile = new File(fileName);
    private static HashMap<String, HashMap<Integer, Double>> dictionary = new HashMap<>();
    private static HashMap<Integer,HashMap<String, ArrayList<Integer>>> position = new HashMap<>();
    private static HashMap<String, HashMap<Integer, Double>> championList = new HashMap<>();
    private static HashMap<Integer, ArrayList<Double>> docVectors = new HashMap<>();
    private static PersianNormalizer persianNormalizer = new PersianNormalizer();
//    private static PersianNormalizationFilter persianNormalizationFilter = new PersianNormalizationFilter();


    /**
     * Create an invertedIndex with given documents!
     *
     * @param docs
     * @throws IOException
     */
    public static void createInvertedIndex(ArrayList<String> docs) throws IOException {
        if (dictionaryFile.createNewFile() && dictionaryFile.length() != 0) {
            readFromFile();
        } else {
            for (String content : docs) {
                String[] tokens = extractTokens(content);
                createDictionary(tokens, docID++);
            }
            calculate_tf_idf();
            saveFile();
        }
        //createChampionList();

       /* // Print
        for (String term : dictionary.keySet()) {
            System.out.println(term + " :");
            for (int id : dictionary.get(term).keySet()) {
                System.out.println("\t" + id + " -> " + dictionary.get(term).get(id));
            }
        }*/
    }

    private static void createChampionList() {
        double threshold = 1.5;
        for (String term : dictionary.keySet()) {
            HashMap<Integer, Double> temp = new HashMap<>();
            for (int id : dictionary.get(term).keySet()) {
                if (dictionary.get(term).get(id) >= threshold) {
                    temp.put(id, dictionary.get(term).get(id));
                    championList.put(term, temp);
                }
            }
        }
    }


    /**
     * Get the content of a document and split it by space.
     *
     * @param content Content of a document
     */
    public static String[] extractTokens(String content) throws IOException {
        String[] tokens = content.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            persianNormalizer.normalize(tokens[i].toCharArray(), tokens[i].length());
        }
        return tokens;
    }


    /**
     * Add DocID to the list of given key!
     *
     * @param key
     * @param id
     */
    private static void addIDToDictionary(String key, int id) {
        if (dictionary.containsKey(key)) {
            double count = 1;
            if (dictionary.get(key).containsKey(id)) {
                count += dictionary.get(key).get(id);
            }
            dictionary.get(key).put(id, count);
        } else {
            HashMap<Integer, Double> docIDs = new HashMap<>();
            docIDs.put(id, 1.0);
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
            if (position.containsKey(docID))
                if (position.get(docID).containsKey(tokens[i]))
                    position.get(docID).get(tokens[i]).add(i);
                else position.get(docID).put(tokens[i],new ArrayList<>(i));
            else {
                HashMap<String,ArrayList<Integer>>temp = new HashMap<>();
                temp.put(tokens[i],new ArrayList<>(i));
                position.put(docID,temp);
            }
        }
    }

    public static void calculate_tf_idf() {
        double tf = 0;
        double idf = 0;
        double tf_idf = 0;
        for (Iterator<String> terms = dictionary.keySet().iterator(); terms.hasNext(); ) {
            String term = terms.next();
            idf = Math.log10(docID / dictionary.get(term).size());
            for (Iterator<Integer> ids = dictionary.get(term).keySet().iterator(); ids.hasNext(); ) {
                int id = ids.next();
                tf = 1 + (Math.log10(dictionary.get(term).get(id)));
                tf_idf = tf * idf;
                if (tf_idf == 0)
                    ids.remove();
                else {
                    dictionary.get(term).put(id, tf_idf);
                    addItemToVector(docVectors, id, tf_idf);
                }
            }
            if (dictionary.get(term).size() == 0)
                terms.remove();
        }
    }

    private static void addItemToVector(HashMap<Integer, ArrayList<Double>> docVectors, int id, double tf_idf) {
        if (!docVectors.containsKey(id))
            docVectors.put(id, new ArrayList<>());
        docVectors.get(id).add(tf_idf);
    }

    private static void readFromFile() throws FileNotFoundException {
        Scanner myReader = new Scanner(dictionaryFile);
        String term = null;
        HashMap<Integer, Double> temp = null;
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            if (!data.contains(":") && !data.contains("~")) continue;
            else if (data.contains(":")) {
                if (term != null) {
                    dictionary.put(term, temp);
                }
                data.replace(":", "").trim();
                term = data;
                temp = new HashMap<>();
            } else {
                String[] split = data.trim().split("~");
                temp.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
            }
        }
    }

    private static void saveFile() throws IOException {
        PrintWriter myWriter = new PrintWriter(fileName);
        myWriter.println("This is the Dictionary\n");
        myWriter.flush();
        for (String term : dictionary.keySet()) {
            myWriter.println(term + ":");
            myWriter.flush();
            for (int id : dictionary.get(term).keySet()) {
                myWriter.println("\t" + id + " ~ " + dictionary.get(term).get(id));
                myWriter.flush();
            }
        }
        myWriter.close();
    }

    public static double[] cosineScore(HashMap<String, Double> queries) {
        double[] score = new double[docID];
        Arrays.fill(score, 0);
        for (String q : queries.keySet()) {
            if (dictionary.containsKey(q)) {
                for (int id : dictionary.get(q).keySet()) {
                    score[id] += (dictionary.get(q).get(id) * queries.get(q));
                }
            }
        }
        for (int document : docVectors.keySet()) {
            score[document] = score[document] / getLength(docVectors.get(document));
        }
        return score;
    }

    private static double getLength(ArrayList<Double> vector) {
        double sum = 0;
        for (Double d : vector) {
            sum += Math.pow(d, 2);
        }
        return Math.sqrt(sum);
    }

    public static HashMap<String, Double> calculate_tf_idf_query(String[] queries) {
        HashMap<String, Double> weightQueries = new HashMap<>();
        double tf;
        double idf;
        double tf_idf;
        int count;
        for (int i = 0; i < queries.length; i++) {
            count = 1;
            tf = 0;
            idf = 0;
            tf_idf = 0;
            for (int j = i + 1; j < queries.length; j++) {
                if (queries[i].equals(queries[j]))
                    count++;
            }
            tf = 1 + (Math.log10(count));
            if (dictionary.containsKey(queries[i]))
                idf = Math.log10((docID + 1) / (dictionary.get(queries[i]).size() + 1));
            tf_idf = tf * idf;
            if (tf_idf != 0)
                weightQueries.put(queries[i], tf_idf);
        }
        return weightQueries;
    }

}
