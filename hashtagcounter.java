import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class hashtagcounter {
    public static void main(String[] args) {
        if(args.length < 1) {
            throw new RuntimeException("Please provide input file as first argument.");
        }
        long startTime = System.currentTimeMillis();
        try (Scanner scanner = new Scanner(new File(args[0]))) {
            PrintStream fileOut = new PrintStream("output_file.txt");
            System.setOut(fileOut);
            Map<String, MaxFibHeap.Node> hashtagMap = new HashMap<>();
            MaxFibHeap fibHeap = new MaxFibHeap();

            while(scanner.hasNext()) {
                String line = scanner.nextLine();
                if(line.startsWith("#")) {
                    String[] arguments = line.split(" ");
                    String hashTag = arguments[0].substring(1);
                    int count = Integer.parseInt(arguments[1]);
                    if(hashtagMap.containsKey(hashTag)) {
                        MaxFibHeap.Node node = hashtagMap.get(hashTag);
                        fibHeap.increaseKey(node, node.getPriority() + count );
                    } else {
                        MaxFibHeap.Node node = new MaxFibHeap.Node(hashTag, count);
                        fibHeap.insert(node);
                        hashtagMap.put(hashTag, node);
                    }
                } else if(line.equalsIgnoreCase("stop")) {
                    break;
                } else {
                    int requiredNoOfHashTags = Integer.parseInt(line);
                    List<MaxFibHeap.Node> nodesToReinsert = new LinkedList<>();
                    String[] hashTags = new String[requiredNoOfHashTags];
                    for(int i=0;i<requiredNoOfHashTags;i++) {
                        MaxFibHeap.Node max = fibHeap.extractMax();
                        hashTags[i] = max.getData();
                        nodesToReinsert.add(max);
                    }
                    System.out.println(String.join(",", hashTags));
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
