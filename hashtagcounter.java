import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

/**
 * The Class hashtagcounter.
 * 
 * @author Sandeep S
 */
public class hashtagcounter {
    
    public static void main(String[] args) {
        if(args.length < 1) {
            throw new RuntimeException("Please provide input file as first argument.");
        }
        long startTime = System.currentTimeMillis();
        try (Scanner scanner = new Scanner(new File(args[0]))) {
            PrintStream fileOut = new PrintStream("output_file.txt");
            System.setOut(fileOut);
            //Initiate hash map to store hashtag and its node.
            Map<String, MaxFibHeap.Node> hashtagMap = new HashMap<>();
            // Initiate the max fibonacci heap.
            MaxFibHeap fibHeap = new MaxFibHeap();

            // Start reading the file
            while(scanner.hasNext()) {
                String line = scanner.nextLine();

                if(line.startsWith("#")) {
                    // Line is a hashtag.
                    String[] arguments = line.split(" ");
                    String hashTag = arguments[0].substring(1);
                    int count = Integer.parseInt(arguments[1]);
                    // Check if its a recurring hashtag or a new one.
                    if(hashtagMap.containsKey(hashTag)) {
                        // Increase key if its recurring.
                        MaxFibHeap.Node node = hashtagMap.get(hashTag);
                        fibHeap.increaseKey(node, node.getPriority() + count );
                    } else {
                        // Insert if its new.
                        MaxFibHeap.Node node = new MaxFibHeap.Node(hashTag, count);
                        fibHeap.insert(node);
                        hashtagMap.put(hashTag, node);
                    }
                } else if(line.equalsIgnoreCase("stop")) {
                    // End the reading.
                    break;
                } else {
                    // It is a query and has an integer.
                    int requiredNoOfHashTags = Integer.parseInt(line);
                    // List of nodes to reinsert.
                    List<MaxFibHeap.Node> nodesToReinsert = new LinkedList<>();
                    // Array to store the hastags as a string and print.
                    String[] hashTags = new String[requiredNoOfHashTags];
                    // performing extract min operation requiredNoOfHashTags number of times.
                    for(int i=0;i<requiredNoOfHashTags;i++) {
                        MaxFibHeap.Node max = fibHeap.extractMax();
                        hashTags[i] = max.getData();
                        nodesToReinsert.add(max);
                    }
                    // Print out the tags in the array seperated by commas.
                    System.out.println(String.join(",", hashTags));
                    // Reinsert the removed nodes.
                    for(MaxFibHeap.Node node : nodesToReinsert) {
                        fibHeap.insert(node);
                    }
                }
            }
        } catch(FileNotFoundException ex) {
            System.err.println("The file specified could not be found in the given path");
        }
        long endTime   = System.currentTimeMillis();
        //System.out.println(endTime - startTime);
    }
}
