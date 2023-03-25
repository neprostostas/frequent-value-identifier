import java.util.Arrays;
import java.util.stream.IntStream;

public class WorkerThread extends Thread {
    private final int[] array;
    private final int startIndex;
    private final int endIndex;
    private int mostFrequent;
    private int count;

    /**
     * Constructor for the WorkerThread class
     * @param array The array to process
     * @param startIndex The starting index of the chunk of the array to process
     * @param endIndex The ending index of the chunk of the array to process
     */
    public WorkerThread(int[] array, int startIndex, int endIndex) {
        this.array = array;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    /**
     * Runs the worker thread, processing the chunk of the array that it was given
     */
    @Override
    public void run() {
        int[] subArray = Arrays.copyOfRange(array, startIndex, endIndex);
        int[] frequency = new int[10];
        for (int j : subArray) {
            frequency[j]++;
        }
        int localMostFrequent = IntStream.range(0, 10)
                .reduce((a, b) -> frequency[a] > frequency[b] ? a : b)
                .orElse(-1);
        int localCount = frequency[localMostFrequent];
        synchronized (this) {
            if (localCount > count) {
                count = localCount;
                mostFrequent = localMostFrequent;
            }
        }
    }

    /**
     * Gets the most frequent element processed by this worker thread
     * @return The most frequent element processed by this worker thread
     */
    public int getMostFrequent() {
        return mostFrequent;
    }

    /**
     * Gets the count of the most frequent element processed by this worker thread
     * @return The count of the most frequent element processed by this worker thread
     */
    public int getCount() {
        return count;
    }
}