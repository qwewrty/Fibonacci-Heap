import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * The Class hashtagcounter.
 * 
 * @author Sandeep S
 */
public class hashtagcounter {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new RuntimeException("Please provide input file as first argument.");
        }
        long startTime = System.currentTimeMillis();
        try (Scanner scanner = new Scanner(new File(args[0]))) {
            PrintStream fileOut = args.length == 2 ? new PrintStream(args[1]) : System.out;
            System.setOut(fileOut);
            // Initiate hash map to store hashtag and its node.
            Map<String, MaxFibonacciHeap.Node> hashtagMap = new HashMap<>();
            // Initiate the max fibonacci heap.
            MaxFibonacciHeap fibHeap = new MaxFibonacciHeap();

            // Start reading the file
            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (line.startsWith("#")) {
                    // Line is a hashtag.
                    String[] arguments = line.split(" ");
                    String hashTag = arguments[0].substring(1);
                    int count = Integer.parseInt(arguments[1]);
                    // Check if its a recurring hashtag or a new one.
                    if (hashtagMap.containsKey(hashTag)) {
                        // Increase key if its recurring.
                        MaxFibonacciHeap.Node node = hashtagMap.get(hashTag);
                        fibHeap.increaseKey(node, node.getPriority() + count);
                    } else {
                        // Insert if its new.
                        MaxFibonacciHeap.Node node = new MaxFibonacciHeap.Node(hashTag, count);
                        fibHeap.insert(node);
                        hashtagMap.put(hashTag, node);
                    }
                } else if (line.equalsIgnoreCase("stop")) {
                    // End the reading.
                    break;
                } else {
                    // It is a query and has an integer.
                    int requiredNoOfHashTags = Integer.parseInt(line);
                    // List of nodes to reinsert.
                    List<MaxFibonacciHeap.Node> nodesToReinsert = new LinkedList<>();
                    // Array to store the hastags as a string and print.
                    String[] hashTags = new String[requiredNoOfHashTags];
                    // performing extract min operation requiredNoOfHashTags number of times.
                    for (int i = 0; i < requiredNoOfHashTags; i++) {
                        MaxFibonacciHeap.Node max = fibHeap.extractMax();
                        hashTags[i] = max.getData();
                        nodesToReinsert.add(max);
                    }
                    // Print out the tags in the array seperated by commas.
                    System.out.println(String.join(",", hashTags));
                    // Reinsert the removed nodes.
                    for (MaxFibonacciHeap.Node node : nodesToReinsert) {
                        fibHeap.insert(node);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            System.err.println("The file specified could not be found in the given path");
        }
        long endTime = System.currentTimeMillis();
        // System.out.println(endTime - startTime);
    }
}
