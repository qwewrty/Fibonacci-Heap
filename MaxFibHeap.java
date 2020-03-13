import java.util.ArrayList;
import java.util.List;

public class MaxFibHeap {
    // Can handle almost upto a 100 Million nodes. => log(100000000) = 26.something
    private static final int DEGREE_TABLE_SIZE = 26;
    public static final class Node {
        private int priority;
        private String data;
        private Node leftSibling;
        private Node rightSibling;
        private Node parent;
        private Node child;
        private int degree;
        private boolean childCut;

        public Node(String data, int priority) {
            leftSibling = rightSibling = this;
            this.data = data;
            this.priority = priority;
            this.degree = 0;
            this.parent = null;
            this.child = null;
            this.childCut = false;
        }

        public int getPriority() {
            return priority;
        }

        public String getData() {
            return data;
        }
    }

    Node max = null;
    int noOfNodes = 0;

    private static Node mergeSiblingLists(Node node1, Node node2) {
        if(null == node1) {
            return node2;
        }
        if(null == node2) {
            return node1;
        }

        Node temp = node1.rightSibling;
        node1.rightSibling = node2.rightSibling;
        node1.rightSibling.leftSibling = node1;
        node2.rightSibling = temp;
        node2.rightSibling.leftSibling = node2;
        return node1.priority > node2.priority ? node1 : node2;
    }

    public void insert(Node node) {
        // Set default values.
        node.degree = 0;
        node.child = null;
        node.parent = null;
        node.childCut = false;
        node.rightSibling = node.leftSibling = node;
        // Increment number of nodes.
        ++noOfNodes;

        //Recalculate max.
        if(null == max) {
            max = node;
            return;
        }
        max = mergeSiblingLists(node, max);
    }

    public Node extractMax() {
        Node toReturn = max;
        if(max == null) {
            throw new RuntimeException("No nodes in the tree!");
        }

        Node child = max.child;
        if(null != child) {
            // Move all children of max to the root list.
            while(child.parent != null) {
                child.parent = null;
                child = child.rightSibling;
            }
            mergeSiblingLists(child, max);
        }

        // remove max from the root list.
        if(max.rightSibling == max) {
            max = null;
        } else {
            max.rightSibling.leftSibling = max.leftSibling;
            max.leftSibling.rightSibling = max.rightSibling;
            max = max.rightSibling;
        }

        // Degree wise merge of remaining nodes
        pairwiseCombine();

        // Decrement no. of nodes.
        --noOfNodes;

        return toReturn;
    }

    public void increaseKey(Node node, int increasedPriority) {
        if(increasedPriority <= node.priority ) {
            throw new RuntimeException("Invalid increaseKey");
        }

        node.priority = increasedPriority;
        Node parent = node.parent;
        if(parent != null && node.priority > parent.priority) {
            cut(node, parent);
            cascadingCut(parent);
        }
        if(node.priority > max.priority) {
            max = node;
        }
    }

    private void cut(Node node, Node parent) {
        //Remove node from the child list of the parent
        if(node.rightSibling == node) {
            parent.child = null;
            parent.degree = 0;
        } else {
            node.rightSibling.leftSibling = node.leftSibling;
            node.leftSibling.rightSibling = node.rightSibling;
            if(parent.child == node) {
                parent.child = node.rightSibling;
            }
            parent.degree--;
        }
        // Prepare for merging with root list.
        node.parent = null;
        node.leftSibling = node.rightSibling = node;
        node.childCut = false;

        // Merge with root list
        max = mergeSiblingLists(node, max);
    }

    private void cascadingCut(Node node) {
        Node parent = node.parent;
        if(null == parent) {
            return;
        }
        if(node.childCut) {
            cut(node, parent);
            cascadingCut(parent);
        } else {
            node.childCut = true;
        }
    }

    private void pairwiseCombine() {
        //Use array list as we are building a priority queue so using a HashMap inside doesn't make sense.
        List<Node> degreeTable = new ArrayList<>(DEGREE_TABLE_SIZE);
        // Initialize degree table.
        for (int i=0; i<DEGREE_TABLE_SIZE; i++) {
            degreeTable.add(null);
        }

        List<Node> nodesToCheck = new ArrayList<>();
        for(Node temp = max; nodesToCheck.isEmpty() || nodesToCheck.get(0) != temp; temp = temp.rightSibling) {
            nodesToCheck.add(temp);
        }

        for(Node currentNode : nodesToCheck) {
            while(true) {
                int degree = currentNode.degree;
                // Just in case degree goes beyond the initial table size.
                while (currentNode.degree >= degreeTable.size()) {
                    degreeTable.add(null);
                }

                Node previousSameDegreeNode = degreeTable.get(degree);
                if(null == previousSameDegreeNode) {
                    degreeTable.set(degree, currentNode);
                    break;
                }
                degreeTable.set(degree, null);

                Node max = previousSameDegreeNode.priority < currentNode.priority ? currentNode : previousSameDegreeNode;
                Node min = previousSameDegreeNode.priority < currentNode.priority ? previousSameDegreeNode : currentNode;

                // Yank min from the root list and make it a child of max
                min.rightSibling.leftSibling = min.leftSibling;
                min.leftSibling.rightSibling = min.rightSibling;

                min.leftSibling = min.rightSibling = min;
                min.parent = max;
                max.child = mergeSiblingLists(min, max.child);

                min.childCut = false;

                max.degree++;

                currentNode = max;
            }

            if(currentNode.priority > max.priority) {
                max = currentNode;
            }
        }
    }


}
