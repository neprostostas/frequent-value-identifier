import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.Scanner;

public class Main {
    /**
     * The main method of the program
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int option;
        boolean fileExists = checkFileExists("input.txt");

        do {
            clearConsole();
            System.out.println("\033[1m" + "--------------------------------------");
            System.out.println("|     Find Most Frequent Element     |");
            System.out.println("|         by Stanislav Kinash        |");
            System.out.println("--------------------------------------" + "\033[0m");

            if (fileExists) {
                System.out.println("Choose an option:");
                System.out.println("1. Generate random input data");
                System.out.println("2. Read input data from file (input.txt)");
                option = scanner.nextInt();
            } else {
                System.out.println("1. Generate random input data");
                System.out.println("\u001B[37;2m\u001B[9m2. Read input data from file (input.txt) - unavailable because input.txt file not found\u001B[0m");
                option = 1;
            }

            int[] array;

            switch (option) {
                case 1 -> {
                    System.out.print("Enter the size of the array: ");
                    int size = scanner.nextInt();
                    array = generateArrayInt(size);
                    saveArrayToFile(array, "input.txt");
                    System.out.println("Array written to file input.txt");
                    fileExists = true;
                }
                case 2 -> {
                    if (checkFileExists("input.txt")) {
                        array = readArrayFromFile("input.txt");
                    } else {
                        System.out.println("input.txt file not found, only option 1 is available:");
                        option = 1;
                        continue;
                    }

                }
                default -> {
                    System.out.println("Invalid option");
                    continue;
                }
            }

            // Show "Calculating..." message while calculating
            System.out.println("\033[36m" + "Calculating..." + "\033[0m");
            long start = System.nanoTime();
            assert array != null;
            int mostFrequent = findMostFrequent(array);
            long end = System.nanoTime();
            System.out.println("\033[32m" + "Most frequent element (serial): " + mostFrequent + "\033[0m");
            System.out.println("\033[33m" + "Time taken (serial): " + formatNanoTime(end - start) + "\033[0m");

            start = System.nanoTime();
            int mostFrequentParallel = findMostFrequentParallel(array);
            end = System.nanoTime();
            System.out.println("\033[32m" + "Most frequent element (parallel): " + mostFrequentParallel + "\033[0m");
            System.out.println("\033[33m" + "Time taken (parallel): " + formatNanoTime(end - start) + "\033[0m");

            System.out.println("\nPress: \n1 - to run again\n2 - to exit");
            option = scanner.nextInt();
        } while (option == 1);
    }

    /**
     * Checks if the file exists
     * @param fileName The name of the file to check
     * @return True if the file exists, false otherwise
     */
    public static boolean checkFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists();
    }

    /**
     * Generates an array of random integers of a specified size
     * @param size The size of the array to generate
     * @return The generated array
     */
    public static int[] generateArrayInt(int size) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(10);
        }
        return array;
    }

    /**
     * Saves an array to a file
     * @param array The array to save
     * @param fileName The name of the file to save the array to
     */
    public static void saveArrayToFile(int[] array, String fileName) {
        try {
            FileWriter writer = new FileWriter(fileName);
            for (int j : array) {
                writer.write(j + " ");
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Reads an array from a file
     * @param fileName The name of the file to read the array from
     * @return The array that was read from the file
     */
    public static int[] readArrayFromFile(String fileName) {
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            String line = scanner.nextLine();
            String[] stringArray = line.split(" ");
            int[] array = new int[stringArray.length];
            for (int i = 0; i < stringArray.length; i++) {
                array[i] = Integer.parseInt(stringArray[i]);
            }
            scanner.close();
            return array;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds the most frequent element in an array using a serial algorithm
     * @param array The array to find the most frequent element of
     * @return The most frequent element of the array
     */
    public static int findMostFrequent(int[] array) {
        int maxCount = 0;
        int mostFrequent = array[0];
        for (int i = 0; i < array.length; i++) {
            int count = 1;
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] == array[j]) {
                    count++;
                }
            }
            if (count > maxCount) {
                maxCount = count;
                mostFrequent = array[i];
            }
        }
        return mostFrequent;
    }

    /**
     * Finds the most frequent element in an array using a parallel algorithm
     * @param array The array to find the most frequent element of
     * @return The most frequent element of the array
     */
    public static int findMostFrequentParallel(final int[] array) {
        int maxCount = 0;
        int mostFrequent = array[0];
        int numThreads = Runtime.getRuntime().availableProcessors();
        int chunkSize = array.length / numThreads;
        WorkerThread[] threads = new WorkerThread[numThreads];
        for (int i = 0; i < numThreads; i++) {
            final int startIndex = i * chunkSize;
            final int endIndex = (i == numThreads - 1) ? array.length : (i + 1) * chunkSize;
            threads[i] = new WorkerThread(array, startIndex, endIndex);
            threads[i].start();
        }
        try {
            for (WorkerThread thread : threads) {
                thread.join();
                int count = thread.getCount();
                if (count > maxCount) {
                    maxCount = count;
                    mostFrequent = thread.getMostFrequent();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return mostFrequent;
    }

    /**
     * Formats a time in nanoseconds as a string
     * @param nanoTime The time in nanoseconds to format
     * @return The formatted time as a string
     */
    public static String formatNanoTime(long nanoTime) {
        double seconds = (double) nanoTime / 1_000_000_000.0;
        DecimalFormat df = new DecimalFormat("#.######");
        return df.format(seconds) + " seconds";
    }

    /**
     * Clears the console.
     * Works on both Unix-based systems and Windows.
     */
    public static void clearConsole() {
        try {
            // Get the operating system
            String operatingSystem = System.getProperty("os.name");

            // For Windows operating systems
            if (operatingSystem.contains("Windows")) {
                // Use the command prompt to clear the console
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
            // For Unix-based operating systems
            else {
                // Use the bash shell to clear the console
                ProcessBuilder pb = new ProcessBuilder("bash", "-c", "clear");
                // Set the terminal environment variable to xterm to improve compatibility
                pb.environment().put("TERM", "xterm");
                pb.inheritIO().start().waitFor();
            }
        }
        // Catch any exceptions if they occur
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}