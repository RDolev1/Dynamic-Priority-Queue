# Flexible Fibonacci Heap

A robust, dynamic priority queue implementation in Java, configurable at runtime to operate under four distinct configurations. 

## 🚀 Features & Configurations

The core behavior of the heap is determined by two boolean flags (`lazyMelds`, `lazyDecreaseKeys`) initialized upon creation. This architectural choice allows the data structure to dynamically adapt its consolidation strategy and node-cutting mechanisms:

* **Fibonacci Heap** (`lazyMelds=true`, `lazyDecreaseKeys=true`): Optimized for O(1) amortized insertions and melds, relying on delayed consolidation and cascading cuts.
* **Binomial Heap** (`lazyMelds=false`, `lazyDecreaseKeys=false`): Enforces strict structural properties with immediate successive linking and standard heapify-up operations.
* **Lazy Binomial Heap** (`lazyMelds=true`, `lazyDecreaseKeys=false`): Defers successive linking until `deleteMin` is executed, balancing strict tree structure with fast insertions.
* **Binomial Heap with Cuts** (`lazyMelds=false`, `lazyDecreaseKeys=true`): A custom hybrid variant that forces strict structure (no lazy melds) but utilizes the cascading cuts mechanism of a Fibonacci heap.

## ⚙️ Supported Operations

* `insert(key, info)`: Inserts a new element into the heap.
* `findMin()`: Retrieves the element with the minimum key without modifying the heap structure.
* `deleteMin()`: Extracts the minimum element and triggers tree consolidation (successive linking) if required by the configuration.
* `decreaseKey(node, diff)`: Reduces the key of a specific node, utilizing either cascading cuts or heapify-up logic based on the active flags.
* `delete(node)`: Removes a specific node entirely from the data structure.
* `meld(heap2)`: Merges another heap into the current one (lazy or strict, depending on initialization).

## ⏱️ Time Complexities (Amortized)

The dynamic configuration directly dictates the amortized time complexity of core operations:

| Operation | Fibonacci Heap | Binomial Heap | Lazy Binomial Heap | Binomial Heap (with Cuts) |
| :--- | :--- | :--- | :--- | :--- |
| `insert(key, info)` | O(1) | O(log n) | O(1) | O(log n) |
| `findMin()` | O(1) | O(1) | O(1) | O(1) |
| `deleteMin()` | O(log n) | O(log n) | O(log n) | O(log n) |
| `decreaseKey(x, diff)` | O(1) | O(log n) | O(log n) | O(log n) |
| `delete(x)` | O(log n) | O(log n) | O(log n) | O(log n) |
| `meld(heap2)` | O(1) | O(log n) | O(1) | O(log n) |

## 📊 Analytical Metrics Tracking

To facilitate theoretical amortized analysis, the heap internally tracks key runtime statistics:

* **Total Links:** The cumulative number of tree linkings performed during successive linking operations.
* **Total Cuts:** The total number of cascading cuts executed.
* **Total Heapify Costs:** The total distance nodes have traveled during heapify-up operations.
* **Marked Nodes:** Tracks the number of marked nodes for internal structure monitoring.

## 🛠️ Implementation Details

* **Language:** Java
* **Successive Linking:** Utilizes a rank-based bucket array bounded by $\log_{\phi}(n)$ to consolidate trees efficiently, ensuring no two roots share the same degree.

## 👥 Authors

* Roy Dolev
* Ofir Sher
