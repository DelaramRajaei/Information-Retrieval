import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

// Java program to implement Max Heap
public class MaxHeap {
    private ArrayList<Pair<Integer, Double>> Heap;
    private int size;
    private int maxsize;

    // Constructor to initialize an
    // empty max heap with given maximum
    // capacity.
    public MaxHeap(int maxsize) {
        this.maxsize = maxsize;
        this.size = 0;
        Heap = new ArrayList<>();
        Heap.add(new Pair<Integer, Double>(1, Double.MAX_VALUE));
    }

    // Returns position of parent
    private int parent(int pos) {
        return pos / 2;
    }

    // Below two functions return left and
    // right children.
    private int leftChild(int pos) {
        return (2 * pos);
    }

    private int rightChild(int pos) {
        return (2 * pos) + 1;
    }

    // Returns true of given node is leaf
    private boolean isLeaf(int pos) {
        if (pos > (size / 2) && pos <= size) {
            return true;
        }
        return false;
    }

    private void swap(int fpos, int spos) {
        Pair<Integer, Double> tmp;
        tmp = Heap.get(fpos);
        Heap.set(fpos, Heap.get(spos));
        Heap.set(spos, tmp);
    }

    // A recursive function to max heapify the given
    // subtree. This function assumes that the left and
    // right subtrees are already heapified, we only need
    // to fix the root.
    private void maxHeapify(int pos) {
        if (isLeaf(pos))
            return;

        if (Heap.get(pos).getSecond() < Heap.get(leftChild(pos)).getSecond()
                || Heap.get(pos).getSecond() < Heap.get(rightChild(pos)).getSecond()) {

            if (Heap.get(leftChild(pos)).getSecond()
                    > Heap.get(rightChild(pos)).getSecond()) {
                swap(pos, leftChild(pos));
                maxHeapify(leftChild(pos));
            } else {
                swap(pos, rightChild(pos));
                maxHeapify(rightChild(pos));
            }
        }
    }

    // Inserts a new element to max heap
    public void insert(int id, double element) {
        Heap.add(new Pair<Integer, Double>(id, element));

        // Traverse up and fix violated property
        int current = ++size;
        while (Heap.get(current).getSecond() > Heap.get(parent(current)).getSecond()) {
            swap(current, parent(current));
            current = parent(current);
        }
    }

    // Remove an element from max heap
    public Pair<Integer, Double> extractMax() {
        Pair<Integer, Double> popped = Heap.get(1);
        Heap.set(1, Heap.get(size--));
        maxHeapify(1);
        return popped;
    }

}
