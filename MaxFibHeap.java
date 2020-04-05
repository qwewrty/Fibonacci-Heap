import java.util.ArrayList;
import java.util.List;

/**
 * The Class MaxFibHeap.
 * 
 * This class Implements the max fibonacci heap with some of the supported
 * operations. All operations implemented here are an implementation of
 * fibonacci heap as explained in the CLRS textbook.
 * 
 * @author Sandeep S
 */
public class MaxFibHeap {

	/**
	 * The Constant DEGREE_TABLE_SIZE.
	 * 
	 * Can handle almost 100 Million nodes. => log(100000000) = 26.something.
	 */
	private static final int DEGREE_TABLE_SIZE = 26;

	/**
	 * The Class Node.
	 */
	public static final class Node {

		/** The priority. */
		private int priority;

		/** The data. */
		private String data;

		/** The left sibling. */
		private Node leftSibling;

		/** The right sibling. */
		private Node rightSibling;

		/** The parent. */
		private Node parent;

		/** The child. */
		private Node child;

		/** The degree. */
		private int degree;

		/** The child cut. */
		private boolean childCut;

		/**
		 * Instantiates a new node.
		 *
		 * @param data     the data
		 * @param priority the priority
		 */
		public Node(String data, int priority) {
			leftSibling = rightSibling = this;
			this.data = data;
			this.priority = priority;
			this.degree = 0;
			this.parent = null;
			this.child = null;
			this.childCut = false;
		}

		/**
		 * Gets the priority.
		 *
		 * @return the priority
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Gets the data.
		 *
		 * @return the data
		 */
		public String getData() {
			return data;
		}
	}

	/** The pointer to the node with the max value in the heap. */
	Node max = null;

	/** The no of nodes in the heap. */
	int noOfNodes = 0;

	/**
	 * Merges two separate circular doubly linked lists into one.
	 *
	 * @param node1 A node from the first list.
	 * @param node2 A node from the second list.
	 * @return The node with a higher priority(i.e max of node1 and node2).
	 */
	private static Node mergeSiblingLists(Node node1, Node node2) {
		if (null == node1) {
			return node2;
		}
		if (null == node2) {
			return node1;
		}

		// Since both are non empty have to do a crisscross connection between the nodes
		// and next elements in each list.
		Node temp = node1.rightSibling;
		node1.rightSibling = node2.rightSibling;
		node1.rightSibling.leftSibling = node1;
		node2.rightSibling = temp;
		node2.rightSibling.leftSibling = node2;
		// return the max of the 2 nodes.
		return node1.priority > node2.priority ? node1 : node2;
	}

	/**
	 * Insert.
	 *
	 * @param node the node
	 */
	public void insert(Node node) {
		// Set default values.
		node.degree = 0;
		node.child = null;
		node.parent = null;
		node.childCut = false;
		// Done so that a single node is also considered as a circular list;
		node.rightSibling = node.leftSibling = node;
		// Increment number of nodes.
		++noOfNodes;

		// Recalculate max.
		if (null == max) {
			max = node;
			return;
		}
		// Merge the node into the root list.
		max = mergeSiblingLists(node, max);
	}

	/**
	 * Extract max.
	 *
	 * @return The node with the highest priority.
	 */
	public Node extractMax() {
		Node toReturn = max;
		if (max == null) {
			throw new RuntimeException("No nodes in the tree!");
		}

		Node child = max.child;
		if (null != child) {
			// Move all children of max to the root list.
			while (child.parent != null) {
				child.parent = null;
				child = child.rightSibling;
			}
			// Merging the child list with the root list.
			mergeSiblingLists(child, max);
		}

		// Remove max from the root list.
		if (max.rightSibling == max) {
			max = null;
		} else {
			max.rightSibling.leftSibling = max.leftSibling;
			max.leftSibling.rightSibling = max.rightSibling;
			max = max.rightSibling;
		}

		// Degree wise merge of remaining nodes.
		pairwiseCombine();

		// Decrement number of nodes.
		--noOfNodes;

		return toReturn;
	}

	/**
	 * Increase key.
	 * 
	 * Used to increase the priority of a node.
	 *
	 * @param node              the node
	 * @param increasedPriority the increased priority
	 */
	public void increaseKey(Node node, int increasedPriority) {
		if (increasedPriority <= node.priority) {
			throw new RuntimeException("Invalid increaseKey");
		}

		node.priority = increasedPriority;
		Node parent = node.parent;
		// Check if the increased priority is greater than its parent's priority.
		if (parent != null && node.priority > parent.priority) {
			cut(node, parent);
			cascadingCut(parent);
		}

		// Make sure the max pointer is still correct.
		if (node.priority > max.priority) {
			max = node;
		}
	}

	/**
	 * Cut.
	 * 
	 * Performs the cut operation.
	 *
	 * @param node   the node
	 * @param parent the parent
	 */
	private void cut(Node node, Node parent) {
		// Remove node from the child list of the parent
		if (node.rightSibling == node) {
			// Parent no more has a child.
			parent.child = null;
			parent.degree = 0;
		} else {
			// Yank the child node out from the children list.
			node.rightSibling.leftSibling = node.leftSibling;
			node.leftSibling.rightSibling = node.rightSibling;
			// Change the parent's child pointer if needed.
			if (parent.child == node) {
				parent.child = node.rightSibling;
			}
			// Decrement the degree of the parent.
			parent.degree--;
		}
		// Prepare for merging with root list.
		node.parent = null;
		node.leftSibling = node.rightSibling = node;
		node.childCut = false;

		// Merge with root list
		max = mergeSiblingLists(node, max);
	}

	/**
	 * Cascading cut.
	 *
	 * @param node the node
	 */
	private void cascadingCut(Node node) {
		Node parent = node.parent;
		if (null == parent) {
			return;
		}
		// If childCut is true perform the cut operation.
		if (node.childCut) {
			cut(node, parent);
			cascadingCut(parent);
		} else {
			// Mark that the node had its child cut once.
			node.childCut = true;
		}
	}

	/**
	 * Pairwise combine.
	 * 
	 * Performs the degreewise merge of the nodes in the fibonacci heap.
	 */
	private void pairwiseCombine() {
		// Use array list as we are building a priority queue so using a HashMap inside
		// doesn't make sense.
		List<Node> degreeTable = new ArrayList<>(DEGREE_TABLE_SIZE);
		// Initialize degree table.
		for (int i = 0; i < DEGREE_TABLE_SIZE; i++) {
			degreeTable.add(null);
		}

		// Have to check all nodes in the root list.
		List<Node> nodesToCheck = new ArrayList<>();
		for (Node temp = max; nodesToCheck.isEmpty() || nodesToCheck.get(0) != temp; temp = temp.rightSibling) {
			nodesToCheck.add(temp);
		}

		for (Node currentNode : nodesToCheck) {
			while (true) {
				int degree = currentNode.degree;
				// Just in case degree goes beyond the initial table size.
				while (currentNode.degree >= degreeTable.size()) {
					degreeTable.add(null);
				}

				Node previousSameDegreeNode = degreeTable.get(degree);
				// If there was a node of same degree then merge.
				if (null == previousSameDegreeNode) {
					degreeTable.set(degree, currentNode);
					break;
				}

				// Take the node of same degree
				degreeTable.set(degree, null);

				// Find max of the two.
				Node max = previousSameDegreeNode.priority < currentNode.priority ? currentNode
						: previousSameDegreeNode;
				Node min = previousSameDegreeNode.priority < currentNode.priority ? previousSameDegreeNode
						: currentNode;

				// Yank min from the root list and make it a child of max
				min.rightSibling.leftSibling = min.leftSibling;
				min.leftSibling.rightSibling = min.rightSibling;
				min.leftSibling = min.rightSibling = min;
				min.parent = max;
				max.child = mergeSiblingLists(min, max.child);

				min.childCut = false;

				// Increment the degree of max.
				max.degree++;

				// Redo the operation for the newly created tree.
				currentNode = max;
			}

			// Making sure the pointer points to the max.
			if (currentNode.priority > max.priority) {
				max = currentNode;
			}
		}
	}

    /**
     * Removes the node from the fib heap.
     *
     * @param node the node
     */
    public void remove(Node node) {
        increaseKey(node, Integer.MAX_VALUE);
        extractMax();
    }

    /**
     * Merge.
     * 
     * Merges heap2 into heap1.
     *
     * @param heap1 the heap to merge into.
     * @param heap2 the heap to merge.
     * @return the max fibonacci heap.
     */
    public static MaxFibHeap merge(MaxFibHeap heap1, MaxFibHeap heap2) {
        if (null == heap2) {
            return heap1;
        }
        if (null == heap1) {
            return heap2;
        }
        mergeSiblingLists(heap1.max, heap2.max);
        if (null == heap1.max || (null != heap2.max && heap2.max.getPriority() > heap1.max.getPriority())) {
            heap1.max = heap2.max;
        }
        heap1.noOfNodes += heap2.noOfNodes;
        return heap1;
    }
}
