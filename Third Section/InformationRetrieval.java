import java.io.*;
import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.apache.lucene.analysis.fa.PersianNormalizer;

public class InformationRetrieval {

    private static int docID = 1;
    private static int classNum = 1;
    // File name
    private static String fileName = "Dictionary.txt";
    private static File dictionaryFile = new File(fileName);
    // Clustering
    private static HashMap<Integer, Pair<ArrayList<Double>, ArrayList<Integer>>> dictionary = new HashMap<>();
    // Classification
    private static ArrayList<Integer> sports = new ArrayList<>();
    private static ArrayList<Integer> economics = new ArrayList<>();
    private static ArrayList<Integer> political = new ArrayList<>();
    private static ArrayList<Integer> health = new ArrayList<>();
    private static ArrayList<Integer> cultural = new ArrayList<>();

    private static HashMap<Integer, HashMap<String, Double>> docVectors = new HashMap<>();
    private static HashMap<String, ArrayList<Integer>> termVector = new HashMap<>();

    private static PersianNormalizer persianNormalizer = new PersianNormalizer();
    //private static PersianNormalizationFilter persianNormalizationFilter = new PersianNormalizationFilter();


    /**
     * Create an invertedIndex with given documents!
     *
     * @param className
     * @param content
     * @param option
     * @throws IOException
     */
    public static void addDocument(String className, String content, int option) throws IOException {
        if (className != "") {
            switch (className) {
                case "sport":
                    sports.add(docID);
                    break;
                case "culture":
                    cultural.add(docID);
                    break;
                case "economy":
                    economics.add(docID);
                    break;
                case "politics":
                    political.add(docID);
                    break;
                case "health":
                    health.add(docID);
                    break;
            }
        }
        String[] tokens = extractTokens(content);
        createDictionary(tokens, docID++);
        if (option == 1) {
            clustering();
        } else {
            classification();
        }
    }

    private static void classification() {

    }

    private static void clustering() {
        boolean flag = true;
        double threshold = 1.5;
        if (dictionary.size() != 0) {
            Pair<Integer, Double> distance = new Pair<>(0, Double.MAX_VALUE);
            for (Integer leaders : dictionary.keySet()) {
                double temp = calculate_distance(dictionary.get(leaders).getFirst(), docVectors.get(docID).values());
                if (distance.getSecond() < threshold && temp < distance.getSecond()) {
                    distance = new Pair<>(leaders, temp);
                    flag = false;
                }
            }
            dictionary.get(distance.getFirst()).getSecond().add(docID);
            dictionary.put(distance.getFirst(), new Pair<>(calculate_mean(), dictionary.get(distance.getFirst()).getSecond()));
        }
        if (flag)
            dictionary.put(classNum, new Pair<>(calculate_mean(), new ArrayList<Integer>(docID)));
    }

    private static double calculate_distance(ArrayList<Double> firstDocument, Collection<Double> secondDocument) {
        double distance = 0;
        Iterator<Double> q = firstDocument.iterator();
        Iterator<Double> d = secondDocument.iterator();
        while (q.hasNext() && d.hasNext()) {
            distance += Math.pow((q.next() - d.next()), 2);
        }
        return Math.sqrt(distance);
    }

    private static ArrayList<Double> calculate_mean() {
        ArrayList<Double> mean = new ArrayList<>();
        return mean;
    }

    public static void createVector() {
        for (Iterator<String> terms = termVector.keySet().iterator(); terms.hasNext(); ) {
            String term = terms.next();
            for (Iterator<Integer> ids = termVector.get(term).iterator(); ids.hasNext(); ) {
                int id = ids.next();
                calculate_tf_idf(id, term);
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
            //TODO normalize better!
            persianNormalizer.normalize(tokens[i].toCharArray(), tokens[i].length());
        }
        return tokens;
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
    }

    /**
     * Add DocID to the list of given key!
     *
     * @param term
     * @param id
     */
    private static void addIDToDictionary(String term, int id) {
        // Document Vector
        HashMap<String, Double> temp = new HashMap();
        double count = 1;
        if (docVectors.containsKey(id))
            if (docVectors.get(id).containsKey(term)) {
                count = docVectors.get(id).get(term) + 1;
            }
        temp.put(term, count);
        docVectors.put(id, temp);

        // Term Vector
        if (termVector.containsKey(term))
            termVector.get(term).add(id);
        else {
            termVector.put(term, new ArrayList<Integer>(id));
        }
    }

    /**
     * Calculate the tf_idf for each term in a document.
     * Create a vector for each document.
     */
    public static void calculate_tf_idf(int id, String term) {
        double tf = 0;
        double idf = 0;
        double tf_idf = 0;

        idf = Math.log10(docID / termVector.get(term).size());
        tf = 1 + (Math.log10(docVectors.get(id).get(term)));
        tf_idf = tf * idf;
        docVectors.get(id).put(term, tf_idf);
    }


    public static double cosineScore(ArrayList<Double> query, ArrayList<Double> document) {
        double score = 0;
        Iterator<Double> q = query.iterator();
        Iterator<Double> d = document.iterator();
        while (q.hasNext() && d.hasNext()) {
            score += (q.next() * d.next());
        }
        score /= (getLength(query) * getLength(document));
        return score;
    }


    private static double getLength(ArrayList<Double> vector) {
        double sum = 0;
        for (Double d : vector) {
            sum += Math.pow(d, 2);
        }
        return Math.sqrt(sum);
    }

    public static ArrayList<Double> calculate_tf_idf_query(String[] queries) {
        HashMap<String, Double> weightQueries = new HashMap<>();
        double tf;
        double idf;
        double tf_idf;
        int count;
        for (int i = 0; i < queries.length; i++) {
            count = 1;
            idf = 0;
            for (int j = i + 1; j < queries.length; j++) {
                if (queries[i].equals(queries[j]))
                    count++;
            }
            tf = 1 + (Math.log10(count));
            if (termVector.containsKey(queries[i]))
                idf = Math.log10((docID + 1) / (termVector.get(queries[i]).size() + 1));
            tf_idf = tf * idf;
            if (tf_idf != 0)
                weightQueries.put(queries[i], tf_idf);
        }
        return new ArrayList<Double>(weightQueries.values());
    }

    public static void K_means() {

    }

    // Find the k nearest documents.
    public static void
    KNN(String query) throws IOException {

    }

}
    /*ArrayList<Double> q = calculate_tf_idf_query(extractTokens(query));
    MaxHeap score = new MaxHeap(docID);
        for (int id = 0; id < docID; id++) {
        score.insert(id, cosineScore(q, new ArrayList<Double>(docVectors.get(id).values())));
        }
        return score;*/