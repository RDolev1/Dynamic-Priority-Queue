/**
 * Heap
 *
 * An implementation of Fibonacci heap over positive integers 
 * with the possibility of not performing lazy melds and 
 * the possibility of not performing lazy decrease keys.
 *
 */
public class Heap
{
    public final boolean lazyMelds;
    public final boolean lazyDecreaseKeys;
    public HeapNode min;
    public int size;
    public int numTrees;
    public int markedNodes;
    public int tot_links;
    public int tot_cuts;
    public int tot_Heapify_cost;
    
    /**
     *
     * Constructor to initialize an empty heap.
     *
     */
    public Heap(boolean lazyMelds, boolean lazyDecreaseKeys)
    {
        this.lazyMelds = lazyMelds;
        this.lazyDecreaseKeys = lazyDecreaseKeys;
        this.min = null;
        this.size = 0;
        this.numTrees = 0;
        this.markedNodes = 0;
        this.tot_links = 0;
        this.tot_cuts = 0;
        this.tot_Heapify_cost = 0;
    }

    /**
     * 
     * pre: key > 0
     *
     * Insert (key,info) into the heap and return the newly generated HeapNode.
     *
     */
    public HeapItem insert(int key, String info) 
    {    
        //Create new HeapItem and insert properties
        HeapNode node = new HeapNode();
        HeapItem item = new HeapItem();
        item.key = key;
        item.info = info;
        item.node = node;
        node.item = item;
        
        //Create new HeapNode and insert properties
        node.child = null;
        node.next = node;
        node.prev = node;
        node.parent = null;
        node.rank = 0;
        node.mark = false;

        //Create Temporary heap for meld
        Heap tempHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
        tempHeap.min = node;
        tempHeap.size = 1;
        tempHeap.numTrees = 1;

        this.meld(tempHeap); 

        return item;
    }

    /**
     * 
     * Return the minimal HeapNode, null if empty.
     *
     */
    public HeapItem findMin()
    {
        return (this.min == null) ? null : this.min.item; 
    }

    /**
     * 
     * Delete the minimal item.
     *
     */
    public void deleteMin(){
        if (this.min == null) {             //Edgecase - empty heap
            return; 
        }

        HeapNode z = this.min;

        if (z.next == z) {                  //Edgecase - single tree heap
        this.min = null;
        }
        else {
            this.min = z.next;
            z.prev.next = z.next;
            z.next.prev = z.prev;
        }
        this.size--; 
        this.numTrees--;    //Update Stats
        
        //Create new child-heap for meld
        if (z.child != null) {
            Heap childHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
            HeapNode firstChild = z.child;
            
            //Make children to roots of the heap and update its stats
            HeapNode curr = firstChild;
            int childCount = 0;
            do {
                curr.parent = null;
                if (curr.mark) {
                    this.markedNodes--;
                    curr.mark = false; 
                }
                childCount++;
                curr = curr.next;
            } while (curr != firstChild);

            //Update stats
            childHeap.min = firstChild; 
            //childHeap.size = childCount;
            childHeap.numTrees = childCount;

            if (this.min == null) {
                childHeap.size = this.size; 
            } else {
                childHeap.size = 0;
            }
            
            this.meld(childHeap);
        }

        if (this.size > 0) {
                successivelinking(this.min);
        }
    }

    /**
     * 
     * pre: 0<=diff<=x.key
     * 
     * Decrease the key of x by diff and fix the heap.
     * 
     */
    public void decreaseKey(HeapItem x, int diff) {
        if (x == null){return;}

        HeapNode node = x.node;
        x.key -= diff; 

        if (node.parent == null || x.key >= node.parent.item.key) {             //Edgecase - heap still legal
            if (x.key < this.min.item.key) {                                    //check in edgecase if min changed
                this.min = node; 
            }
            return;
        }

        //General Case - heap order needs fixing
        if (this.lazyDecreaseKeys) {        //Cascading cut if lazyDecreaseKeys
            HeapNode parent = node.parent;
            cut(node, parent);
            cascadingCut(parent);
            
        } else {
            while (node.parent != null && node.item.key < node.parent.item.key) {  //Heapify up if not lazyDecreaseKeys
                swapItems(node, node.parent);
                node = node.parent;                                 //switch logic pretains to the item, move up manualy
                this.tot_Heapify_cost++;
            }
        }
        
        if (x.key < this.min.item.key) {                        //Update min pointer
            this.min = x.node;
        }
    }


    /**
     * 
     * Delete the x from the heap.
     *
     */
    public void delete(HeapItem x) 
    {
        if (x == null){return;}    
        int diff = x.key - Integer.MIN_VALUE;
        decreaseKey(x, diff); //Make x the min of the heap
        deleteMin();
    }


    /**
     * 
     * Meld the heap with heap2
     * pre: heap2.lazyMelds = this.lazyMelds AND heap2.lazyDecreaseKeys = this.lazyDecreaseKeys
     *
     */
    public void meld(Heap heap2){

        if (heap2 == null || heap2.min == null){return;}     //Edgecase - nothing to meld


        if (this.min == null) {             //Edgecase - current list empty -> make heap2 the main heap
        this.min = heap2.min;
        this.size = heap2.size;
        this.numTrees = heap2.numTrees;
        this.markedNodes = heap2.markedNodes;
        this.tot_links += heap2.tot_links;         
        this.tot_cuts += heap2.tot_cuts;
        this.tot_Heapify_cost += heap2.tot_Heapify_cost;
        return;
        }

    
        //General Case
        HeapNode min1 = this.min;           
        HeapNode min2 = heap2.min;
        HeapNode min1_next = min1.next;
        HeapNode min2_prev = min2.prev;

        min1.next = min2;                       //Join the circular lists
        min2.prev = min1;
        min1_next.prev = min2_prev;
        min2_prev.next = min1_next;

        if (min1.item.key > min2.item.key){     //Update min pointer
            this.min = min2;
        }


        //Update sizes
        this.size += heap2.size;                
        this.numTrees += heap2.numTrees;
        this.tot_links += heap2.tot_links;
        this.tot_Heapify_cost += heap2.tot_Heapify_cost;
        this.markedNodes += heap2.markedNodes;
        this.tot_cuts += heap2.tot_cuts;    
        
        //Use successive-linking if not a lazy meld
        if (!this.lazyMelds){
            successivelinking(min1);
        }       
    }
    

    /**
     * 
     * Return the number of elements in the heap
     *   
     */
    public int size()
    {
        return this.size; 
    }


    /**
     * 
     * Return the number of trees in the heap.
     * 
     */
    public int numTrees()
    {
        return this.numTrees; 
    }
    
    
    /**
     * 
     * Return the number of marked nodes in the heap.
     * 
     */
    public int numMarkedNodes()
    {
        return this.markedNodes; 
    }
    
    
    /**
     * 
     * Return the total number of links.
     * 
     */
    public int totalLinks()
    {
        return this.tot_links; 
    }
    
    
    /**
     * 
     * Return the total number of cuts.
     * 
     */
    public int totalCuts()
    {
        return this.tot_cuts; 
    }
    

    /**
     * 
     * Return the total heapify costs.
     * 
     */
    public int totalHeapifyCosts()
    {
        return this.tot_Heapify_cost; 
    }
    
    /**
     * Class implementing a node in a Heap.
     *  
     */
    public static class HeapNode{
        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode prev;
        public HeapNode parent;
        public int rank;
        public boolean mark;
    }
    
    /**
     * Class implementing an item in a Heap.
     *  
     */
    public static class HeapItem{
        public HeapNode node;
        public int key;
        public String info;
    }

    private void successivelinking(HeapNode startNode) {
        if (startNode == null || this.size == 0) {      //Edgecase - empty Heap
            return;
        }
        
        //Make a list of buckets for consolidating, being mindful of Fibonaci as an upper limit (log(phi)
        int maxRank = (int)(Math.log(this.size) / Math.log(1.618)) + 5;
        HeapNode[] bucketList = new HeapNode[maxRank];
        
        //Number of roots check
        int rootCount = 0;
        HeapNode curr = startNode;
        HeapNode initial = startNode;
        do {
            rootCount++;
            curr = curr.next;
        } while (curr != initial && rootCount <= this.size + 1);
        

        //Successive linking pass
        curr = startNode;
        for (int i = 0; i < rootCount; i++) {
            HeapNode next = curr.next;  // Save pointer to next before we modify curr
            HeapNode x = curr;
            
            //Remove curr connection in the Linked-List, will remedy later
            x.next = x;
            x.prev = x;
            x.parent = null;
            
            //Insert to bucket list based on rank - check and act based on collisions
            while (x.rank < bucketList.length && bucketList[x.rank] != null) { 
                HeapNode y = bucketList[x.rank];
                bucketList[x.rank] = null; 
                x = link(x, y);
            }

            if (x.rank < bucketList.length) {
                bucketList[x.rank] = x;
            }
            curr = next;
        }
        
        //Reconstruct the Linked-List
        this.min = null;
        this.numTrees = 0;
        
        for (int i = 0; i < bucketList.length; i++) {
            HeapNode tree = bucketList[i];
            if (tree != null) {
                tree.parent = null;  //Ensure roots have no parent
                
                if (this.min == null) { //Edgecase - first tree in heap
                    this.min = tree;
                    tree.next = tree;
                    tree.prev = tree;
                    this.numTrees = 1;
                } else {             //General case - Insert tree into Linked-List after min
                    HeapNode minNext = this.min.next;
                    this.min.next = tree;
                    tree.prev = this.min;
                    tree.next = minNext;
                    minNext.prev = tree;
                    this.numTrees++;
                    
                    
                    if (tree.item.key < this.min.item.key) { //Update min pointer
                        this.min = tree;
                    }
                }
            }
        }
    }
    

    private HeapNode link(HeapNode x, HeapNode y) {

        if (x.item.key > y.item.key) {
            HeapNode temp = x;
            x = y;
            y = temp;
        }

        y.parent = x;

        if (x.child == null) {
            x.child = y;
            y.next = y;
            y.prev = y;
        } else {   
            HeapNode head = x.child;
            HeapNode tail = head.prev;
            y.next = head;
            y.prev = tail;
            head.prev = y;
            tail.next = y;
            x.child = y; 
        }

        x.rank ++;
        this.tot_links++;

        return x;
    }

    private void swapItems(HeapNode node1, HeapNode node2) {
        HeapItem item1 = node1.item;
        HeapItem item2 = node2.item;
        
        node1.item = item2;
        node2.item = item1;
        
        item1.node = node2;
        item2.node = node1;
    }

    private void cut(HeapNode x, HeapNode y) {
        x.parent = null; 
        
        
        if (x.mark) { //Checks if x was marked and acts accordingly (roots dont have marks)
            this.markedNodes--;
            x.mark = false; 
        }

        y.rank--;
        
        // Remove x from y's child list
        if (x.next == x) {                  //Edgecase - only child
            y.child = null;
        } 
        else {
            if (y.child == x) {             //Edgecase - x was the child pointer of y
                y.child = x.next;
            }

            x.prev.next = x.next;
            x.next.prev = x.prev;
        }
        
        // Create temporary heap with x and meld it
            Heap tempHeap = new Heap(this.lazyMelds, this.lazyDecreaseKeys);
            x.next = x;
            x.prev = x;
            tempHeap.min = x;
            tempHeap.size = 0;
            tempHeap.numTrees = 1;
            
            this.meld(tempHeap);
            this.tot_cuts++;           //Update stats
    }

    private void cascadingCut(HeapNode y) {
        HeapNode z = y.parent;
        if (z != null) {
            if (!y.mark) {              //First cut from y
                y.mark = true;
                this.markedNodes++;
            } else { //Second cut from y - needs to become root and cascade cut up
                cut(y, z);
                cascadingCut(z);
            }
        }
    }
}
