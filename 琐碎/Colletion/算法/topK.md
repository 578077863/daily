## 703. 数据流中的第 K 大元素
[703. 数据流中的第 K 大元素](https://leetcode-cn.com/problems/kth-largest-element-in-a-stream/)
设计一个找到数据流中第 k 大元素的类（class）。注意是排序后的第 k 大元素，不是第 k 个不同的元素。

请实现 KthLargest 类：

* KthLargest(int k, int[] nums) 使用整数 k 和整数流 nums 初始化对象。
* int add(int val) 将 val 插入数据流 nums 后，返回当前数据流中第 k 大的元素。

平衡二叉树
```java
class KthLargest {
    /**
     * 平衡二叉搜索树
     */
    static class IndexedAVL<T extends Comparable<T>> {
        //节点
        private class Node {
            //节点存储的真实的数据
            T val;
            /**
             * size 是这节点统辖的树的所有元素的总个数,
             * cnt 是这个节点存储 val 的次数,
             * height 是节点的高度
             */
            int size, cnt, height;
            Node left, right;

            public Node(T val) {
                this.val = val;
                this.cnt = this.height = this.size = 1;
            }
        }

        private int size;
        private Node root;
        private final Deque<T> queue = new LinkedList<>();

        //-------------------------------私有方法-------------------------------

        //获取节点的高度 height
        private int heightOf(Node node) {
            return node == null ? 0 : node.height;
        }

        //获取节点的所有元素个数 size
        private int sizeOf(Node p) {
            return p == null ? 0 : p.size;
        }

        //更新状态 —— 树高、树的元素总数
        private void updateStatus(Node p) {
            p.height = Math.max(heightOf(p.left), heightOf(p.right)) + 1;
            p.size = p.cnt + sizeOf(p.left) + sizeOf(p.right);
        }

        //左单旋
        private Node singleLeftRotation(Node p) {
            Node q = p.left;
            p.left = q.right;
            q.right = p;
            //先更新子节点的状态, 再更新父节点的状态
            updateStatus(p);
            updateStatus(q);
            return q;
        }

        //右单旋
        private Node singleRightRotation(Node q) {
            Node p = q.right;
            q.right = p.left;
            p.left = q;
            //先更新子节点的状态, 再更新父节点的状态
            updateStatus(q);
            updateStatus(p);
            return p;
        }

        //左左调整
        private Node LL(Node t) {
            return singleLeftRotation(t);
        }

        //左右调整
        private Node LR(Node t) {
            t.left = singleRightRotation(t.left);
            return singleLeftRotation(t);
        }

        //右右调整
        private Node RR(Node t) {
            return singleRightRotation(t);
        }

        //右左调整
        private Node RL(Node t) {
            t.right = singleLeftRotation(t.right);
            return singleRightRotation(t);
        }

        //向 AVL树 t 中插入一个元素 value
        private Node insert(Node t, T value) {
            if (t == null) {
                return new Node(value);
            }
            Node newRoot = t;

            int comp = value.compareTo(t.val);
            if (comp < 0) {
                //值比当前的小, 往左节点插入
                t.left = insert(t.left, value);

                //插入完成之后, 要将搜索路径上的点依次进行调整 height,调整 size 的大小
                int leftH = heightOf(t.left);
                int rightH = heightOf(t.right);

                if (leftH - rightH > 1) {
                    if (value.compareTo(t.left.val) <= 0) {
                        //LL型
                        newRoot = LL(t);
                    } else {
                        //LR型
                        newRoot = LR(t);
                    }
                }
            } else if (comp > 0) {
                //值比当前的大, 往右节点插入
                t.right = insert(t.right, value);

                //插入完成之后, 要将搜索路径上的点依次进行调整 height,调整 size 的大小
                int leftH = heightOf(t.left);
                int rightH = heightOf(t.right);

                if (rightH - leftH > 1) {
                    if (value.compareTo(t.right.val) >= 0) {
                        //RR型
                        newRoot = RR(t);

                    } else {
                        //RL型
                        newRoot = RL(t);
                    }
                }
            } else {
                t.cnt++;
            }
            updateStatus(newRoot);
            return newRoot;
        }

        //从 AVL树 t 中删除一个元素 value
        private Node remove(Node t, T value) {
            if (t == null) return null;

            Node newRoot = t;

            int comp = value.compareTo(t.val);
            if (comp < 0) {
                t.left = remove(t.left, value);

                //删除左子树的节点, 唯一可能导致"失衡"的情况是 BF(平衡因子) 由 -1 变成 -2
                int leftH = heightOf(t.left);
                int rightH = heightOf(t.right);

                if (rightH - leftH > 1) {
                    if (heightOf(t.right.right) >= heightOf(t.right.left)) {
                        //RR型
                        newRoot = RR(t);
                    } else {
                        //RL型
                        newRoot = RL(t);
                    }
                }

            } else if (comp > 0) {
                t.right = remove(t.right, value);
                int leftH = heightOf(t.left);
                int rightH = heightOf(t.right);
                //删除右子树的节点, 唯一可能导致"失衡"的情况是 BF(平衡因子) 由 1 变成 2
                if (leftH - rightH > 1) {
                    if (heightOf(t.left.left) >= heightOf(t.left.right)) {
                        //LL型
                        newRoot = LL(t);
                    } else {
                        //LR型
                        newRoot = LR(t);
                    }
                }
            } else {
                if (t.cnt > 1) {
                    t.cnt--;
                } else {
                    //下面细分成 4 种情况
                    if (t.left == null && t.right == null) {
                        //(1)左右子树都为空, 返回 null
                        return null;
                    } else if (t.left != null && t.right == null) {
                        //(2)只有左子树, 返回 left
                        return t.left;
                    } else if (t.left == null) {
                        //(3)只有右子树, 返回 right
                        return t.right;
                    } else {
                        //(4)左右子树都存在, 用前驱的值代替（后继也可以）
                        Node cur = t.left;
                        while (cur.right != null) {
                            cur = cur.right;
                        }
                        //将左子树的最右节点的值 val 放到 t 上
                        t.val = cur.val;
                        //从左子树中移除 val
                        t.left = remove(t.left, cur.val);
                        // 这个地方仍然要有形态的调整
                        // 删除左子树的节点，唯一可能导致"失衡"的情况是 BF(平衡因子) 由 -1 变成 -2
                        int leftH = heightOf(t.left);
                        int rightH = heightOf(t.right);

                        if (rightH - leftH > 1) {
                            if (heightOf(t.right.right) >= heightOf(t.right.left)) {
                                //RR型
                                newRoot = RR(t);
                            } else {
                                //RL型
                                newRoot = RL(t);
                            }
                        }
                    }
                }
            }
            updateStatus(newRoot);
            return newRoot;
        }

        //根据次序来获取元素
        private T getItemByRank(Node node, int rank) {
            //若 node 为空, 返回自定义的值(null)
            if (node == null) return null;
            //如果左子树的元素个数大于 rank, 表示元素会在左子树中, 就从左子树中找
            if (sizeOf(node.left) >= rank) return getItemByRank(node.left, rank);
            //又, 若在 node 的范围内, 返回 node 的值
            if (sizeOf(node.left) + node.cnt >= rank) return node.val;
            //否则, 就从右子树中找
            return getItemByRank(node.right, rank - sizeOf(node.left) - node.cnt);
        }

        //以树形的方式打印节点的值
        private void prettyPrintTree(Node node, String prefix, boolean isLeft) {
            if (node == null) {
                System.out.println("Empty tree");
                return;
            }

            if (node.right != null) {
                prettyPrintTree(node.right, prefix + (isLeft ? "│    " : "     "), false);
            }

            System.out.println(prefix + (isLeft ? "└──── " : "┌──── ") + node.val);

            if (node.left != null) {
                prettyPrintTree(node.left, prefix + (isLeft ? "     " : "│    "), true);
            }
        }

        //中序遍历, 将数据存入数组中
        private void midOrder(Node node) {
            if (node == null) return;
            midOrder(node.left); //左子树
            int count = node.cnt;
            for (int i = 0; i < count; i++) {
                queue.add(node.val);
            }
            midOrder(node.right); //右子树
        }

        //------------------------------------------------------------------

        public int getSize() {
            return size;
        }

        //根据次序来获取元素
        public T getItemByRank(int rank) {
            return getItemByRank(root, rank);
        }

        //获取排好序的数据数组
        public Object[] getOrderedData() {
            queue.clear();
            midOrder(root);
            return queue.toArray();
        }

        //以树形方式打印节点的值, 支持链式调用
        public IndexedAVL<T> prettyPrintTree() {
            prettyPrintTree(root, "", true);
            return this;
        }

        //打印换行
        public IndexedAVL<T> println() {
            System.out.println();
            return this;
        }

        //添加元素, 支持链式调用
        public IndexedAVL<T> add(T value) {
            root = insert(root, value);
            this.size++;
            return this;
        }

        //删除元素, 支持链式调用
        public IndexedAVL<T> erase(T value) {
            root = remove(root, value);
            this.size--;
            return this;
        }
    }
    
    private final int kth;
    private final IndexedAVL<Integer> tree;

    public KthLargest(int k, int[] nums) {
        this.kth = k;
        this.tree = new IndexedAVL<>();
        for(int n : nums) {
            tree.add(n);
        }
    }
    
    public int add(int val) {
        int value = tree.add(val).getItemByRank(tree.getSize() + 1 - kth);
        //tree.prettyPrintTree().println();
        return value;
    }
}
```

小根堆
```java
class KthLargest {

    int k;
    int[] minHeap;
    int len;
    public KthLargest(int k, int[] nums) {
        int[] minPriorityQueue = new int[k];
        this.minHeap = minPriorityQueue;

        this.k = k;
        for(int i = 0; i < k; i++){
            if(i < nums.length){
                minPriorityQueue[i] = nums[i];
            }else{
                minPriorityQueue[i] = Integer.MIN_VALUE;
            }
        }


        buildMinHeap(minPriorityQueue,k);

        for(int i = k; i < nums.length; i++){
            if(minPriorityQueue[0] < nums[i]){
                minPriorityQueue[0] = nums[i];
                buildMinHeap(minPriorityQueue,k);
            }
        }

    }


    private void buildMinHeap(int[] nums, int heapSize){
        for(int i = (heapSize - 2) / 2; i >= 0; i--){
            minHeapify(nums,i);
        }
    }

    private void minHeapify(int[] nums, int parent){
        int left = parent * 2 + 1;
        int right = parent * 2 + 2;

        int smallest = parent;

        if( left < k && nums[smallest] > nums[left]) { smallest = left;}
        if( right < k && nums[smallest] > nums[right]) { smallest = right;}

        if(parent != smallest){
            swap(nums, smallest, parent);
            minHeapify(nums, smallest);
        }
    }

    private void swap(int[] nums, int a, int b){
            int temp = nums[a];
            nums[a] = nums[b];
            nums[b] = temp;
    }
    
    public int add(int val) {
        if(minHeap[0] < val){
            minHeap[0] = val;
            buildMinHeap(minHeap,k);
        }

        return minHeap[0];
    }
}

/**
 * Your KthLargest object will be instantiated and called as such:
 * KthLargest obj = new KthLargest(k, nums);
 * int param_1 = obj.add(val);
 */


//  class KthLargest {
//     /*
//     因为每次只保留前k大的元素,因此每一次插入只要保存好前k大的元素即可
//     也就是说<=第k大的元素都可以不用保留,因为他们对当前返回的结果以及后面的结果均无影响
//     边界条件非常多!!!!做好心理准备
//     */
//     // 存储最大的前k个元素
//     int[] preK;
//     int k;
//     public KthLargest(int k, int[] nums) {
//         this.k = k;
//         this.preK = new int[k];
//         // 这里要先将nums装箱才可以用匿名类排序
//         Integer[] arr = new Integer[nums.length];
//         for(int i = 0; i < nums.length; i++) {
//             arr[i] = nums[i];
//         }
//         int len = arr.length;
//         // 先将nums降序排序
//         Arrays.sort(arr, (a, b) -> b - a);
//         // 将初始nums的最大前k个元素装进preK
//         for(int i = 0; i < k; i++) {
//             // 这里出现k<len的情况,要用MIN_VALUE补充后面的空位,表示还没有数填充的地方
//             // 利用MIN_VALUE填充之后,有数进来就将MIN_VALUE驱逐出preK,先填满preK
//             if(i > len - 1) {
//                 preK[i] = Integer.MIN_VALUE;
//                 continue;
//             }
//             preK[i] = arr[i];
//         }
//     }
    
//     public int add(int val) {
//         // 添加元素时,当且仅当val>preK[k-1]对结果有影响,否则直接返回preK[k-1]
//         // eg:preK=[8,6,5],加入4不影响结果,而加入5也不影响,而加入6就变成了[8,6,6]返回6
//         if(val > preK[k - 1]) {
//             // 将val加入preK并重新调整位置
//             // 从后开始找出首个不小于val的位置
//             int pos = k - 1;
//             while(pos >= 0 && preK[pos] < val) {
//                 pos--;
//             }
//             // 如果val=6,那么pos将停留在上面例子的6位置,将pos右边的额元素全部右移一位
//             // 这里有个坑:覆盖元素要从后面开始,如果从前面开始,下一个要用的preK[i]将会被覆盖
//             // 从后面遍历可以取得原始数据
//             for(int i = k - 2; i >= pos + 1; i--) {
//                 preK[i + 1] = preK[i];
//             }
//             // 腾出来preK[pos+1]空间就是用来装val的
//             preK[pos + 1] = val;
//         }
//         // 最后再返回preK最后一个数字(无论有没有执行if的都返回这个,执行了if的充其量就是更新了preK罢了)
//         return preK[k - 1];
//     }
// }

// /**
//  * Your KthLargest object will be instantiated and called as such:
//  * KthLargest obj = new KthLargest(k, nums);
//  * int param_1 = obj.add(val);
//  */
```


快排
```java
贴个代码，初始化使用快速排序时间复杂度O(nlogn),单次插入采用二分查找加插入排序时间复杂度O(k)。 因为需要从大到小排序，二分查找也是查找的从大到小排序的数组，所以采用手写的快排和JDK二分查找微调版本。
public class KthLargest {
    private final int k;
    private final int[] nums;

    public KthLargest(int k, int[] nums) {
        this.k = k;
        int n = nums.length;
        quickSort(nums, 0, n - 1);
        this.nums = Arrays.copyOf(nums, k);
        if (n < k) {
            // Arrays.fill(this.nums, n, k, Integer.MIN_VALUE);
            for (int i = n; i < k; i++) {
                this.nums[i] = Integer.MIN_VALUE;
            }
        }
    }

    public int add(int val) {
        int index = binarySearch(nums, val);
        if (index < 0) {
            index = -index - 1;
        }
        if (k - 1 - index >= 0) {
            System.arraycopy(nums, index, nums, index + 1, k - 1 - index);
            nums[index] = val;
        }
        return nums[k - 1];
    }

    private static void quickSort(int[] arr, int l, int r) {
        if (l >= r) return;
        int pivot = arr[l];
        int i = l, j = r;
        while (i < j) {
            // 一定要先右再左
            while (i < j && arr[j] <= pivot)
                j--;
            while (i < j && arr[i] >= pivot)
                i++;
            if (i < j) {
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        // 基准数归位
        arr[l] = arr[i];
        arr[i] = pivot;
        quickSort(arr, l, i);
        quickSort(arr, i + 1, r);
    }

    private int binarySearch(int[] a, int key) {
        int low = 0;
        int high = a.length - 1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = a[mid];

            if (midVal > key)
                low = mid + 1;
            else if (midVal < key)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found.
    }
}
```