# 	数据结构

## 排序算法

### 希尔排序



### 冒泡排序

依次比较数组中相邻两个元素的大小,若 a[j] > a[j+1],则交换两个元素,两两都比较一遍称为一轮冒泡,结果是让最大的元素排至最后

重复以上步骤,直到整个数组有序

```java
for(int j = 0; j < a.length - 1; j++){
    boolean swapped = false;
    
    for(int i = 0; i < a.length - 1 - j; i++){
        if(a[i] > a[i + 1]){
            swap(a, i, i + 1);
            swapped = true;
        }
    }
    
    if(!swapped){
        break;
    }
}


//改进

int n = a.length - 1;
while(true){
    int last = 0;//表示最后一次交换索引位置
    for(int i = 0; i < n; i++){
        if(a[i] > a[i + 1]){
            swap(a, i, i + 1);
            last = i;
        }
    }
    
    n = last;// n == 1 ,means a[0] need to compare to a[1]
    if(n == 0){
        break;
    }
}
```





### 插入排序(稳定排序)

```java
// i 代表待插入元素的索引
for(int i = 1; i < a.length; i++){
    int t = a[i];
    int j = i - 1;//代表已排序区域的元素索引
    
    while(j >= 0){
        if(t < a[j]){
            a[j + 1] = a[j];
        }else{
            break;
        }
        j--;
    }
    
    a[j + 1] = t;
}
```



### 选择排序(不稳定排序)

```java
for(int i = 0; i < a.length - 1; i++){
    int s = i;
    for(int j = s + 1; j < a.length; j++){
        if(a[s] > a[j]){
            s = j;
        }
    }
    
    if(s != i){
        swap(a,s,i);
    }
}
```





### 快排

思想就是 : 选取某一个数作为基准值,从左往右寻找比该数小的 , 从右往左寻找比该数大的,然后交换.   

**<u>此处需注意 : 如果一开始 取最左端为基准值,就得先从最右边开始往左搜索 , 取最右端为基准值,就得先从最左边开始往右搜索</u>**

原因如下 : 假设取最左端为基准值,先从最左边开始搜, 假设 现在 i 的位置是小于基准值  , j的位置是大于基准值, i接着向右搜寻大于基准值的数,但直到 i == j 才找到,此时循环结束, 基准值和 j 的值交换,没有达到 比基准值小的在左边,大的在右边的目的

```java
public class QuickSort {

    public static void main(String[] args) {
        QuickSort quickSort = new QuickSort();

        int[] arr=new int[]{1,3,4,6};
        quickSort.quickSort(arr,0,arr.length-1);
        System.out.println(arr.toString());
    }

    private void quickSort(int[] arr,int l, int r){

        if(l>=r) return;
        //设置基准值,基准值的取值规定:  取最左端为基准值,就得先从最右边开始往左搜索
        //                          取最右端为基准值,就得先从最左边开始往右搜索

        int b=r;
        int i=l,j=r;
        while(i<j){
            while(i < j && arr[i]<= arr[b]) i++;
            while(i < j && arr[j]>= arr[b]) j--;
            swap(arr,i,j);
        }
        swap(arr,b,i);
        quickSort(arr,l,i-1);
        quickSort(arr,i+1,r);
    }

    public void swap(int[] arr,int l,int r){
        int temp=arr[l];
        arr[l]=arr[r];
        arr[r]=temp;
    }
}

```



## 字符串





#### [28. 实现 strStr()](https://leetcode-cn.com/problems/implement-strstr/)

实现 strStr() 函数。

给你两个字符串 haystack 和 needle ，请你在 haystack 字符串中找出 needle 字符串出现的第一个位置（下标从 0 开始）。如果不存在，则返回  -1 。

 

说明：

当 needle 是空字符串时，我们应当返回什么值呢？这是一个在面试中很好的问题。

对于本题而言，当 needle 是空字符串时我们应当返回 0 。这与 C 语言的 strstr() 以及 Java 的 indexOf() 定义相符。

```java
class Solution {

    private void getNext(int[] next, String s){

        int j = 0;
        next[0] = j;

        for(int i = 1; i < s.length(); i++){


            while( j >=1 && s.charAt(i) != s.charAt(j)){
                j = next[j-1];//指向前后缀公共部分外的后一个
            }

            // a b a b  c  0 0 1 2

            if(s.charAt(i) == s.charAt(j)){
                j++;
            }


            next[i] = j;
        }
    }
    public int strStr(String haystack, String needle) {

        if(needle.length() == 0){
            return 0;
        }

        int[] next = new int[needle.length()];
        getNext(next,needle);

        int j = 0;
        for(int i = 0; i < haystack.length(); i++){
            while(j >= 1 && haystack.charAt(i) != needle.charAt(j)){
                j = next[j-1];
            }

            if(haystack.charAt(i) == needle.charAt(j)){
                j++;
            }

            if(j == needle.length()){
                return (i-needle.length()+1);
            }
        }

        return -1;
    }
}







class Solution {
    public int strStr(String haystack, String needle) {
        if(needle == null || needle.length() == 0){return 0;}
        int[] next = new int[needle.length()];

        int j = 0;
        getNext(next,needle);
        for(int i = 0; i < haystack.length(); i++){
            while(j >= 1 && haystack.charAt(i) != needle.charAt(j)){
                j = next[j-1];
            }

            if(haystack.charAt(i) == needle.charAt(j)){j++;}

            if(j == needle.length()){
                return i-needle.length() + 1;
            }
        }

        return -1;
    }


    private void getNext(int[] next,String needle){

        int j = 0;
        next[0] = j;
        for(int i = 1; i < needle.length(); i++){

            while(j >=1 && needle.charAt(i) != needle.charAt(j)){
                j = next[j-1];
            }

            if(needle.charAt(j) == needle.charAt(i)){
                j++;
            }

            next[i] = j;
        }
    }
}
```



#### [剑指 Offer 58 - II. 左旋转字符串](https://leetcode-cn.com/problems/zuo-xuan-zhuan-zi-fu-chuan-lcof/)

```java
class Solution {
    public String reverseLeftWords(String s, int n) {
        //方法一
        //return s.substring(n,s.length())+s.substring(0,n);

        //方法二
       StringBuilder stringBuilder=new StringBuilder();
       for(int i=n;i<s.length();i++){
           stringBuilder.append(s.charAt(i));
       }

       for(int i=0;i<n;i++){
           stringBuilder.append(s.charAt(i));
       }
       return stringBuilder.toString();


//        方法三:以二为原理
        // StringBuilder stringBuilder=new StringBuilder();
        // for(int i=n;i<s.length()+n;i++){
        //     stringBuilder.append(s.charAt(i%s.length()));
        // }
        // return stringBuilder.toString();

//        //方法四:耗费空间
//        String res="";
//        for (int i=n;i<s.length()+n;i++){
//            res+=s.charAt(i%s.length());
//        }
    }
}
```



#### [541. 反转字符串 II](https://leetcode-cn.com/problems/reverse-string-ii/)

给定一个字符串 s 和一个整数 k，从字符串开头算起，每计数至 2k 个字符，就反转这 2k 字符中的前 k 个字符。

如果剩余字符少于 k 个，则将剩余字符全部反转。
如果剩余字符小于 2k 但大于或等于 k 个，则反转前 k 个字符，其余字符保持原样。

```java
class Solution {
    public String reverseStr(String s, int k) {
        char[] arr=s.toCharArray();
        int len=arr.length;

        for(int i=0;i<len;i+=2*k){

            change(arr,i,Math.min(len,i+k)-1);
        }

        return new String(arr);
    }
    public void change(char[] arr,int left,int right){

        while(left<right){
            char temp=arr[left];
            arr[left]=arr[right];
            arr[right]=temp;
            left++;
            right--;
        }
    }
}
```



#### [151. 翻转字符串里的单词](https://leetcode-cn.com/problems/reverse-words-in-a-string/)

```java
class Solution {
   /**
     * 不使用Java内置方法实现
     * <p>
     * 1.去除首尾以及中间多余空格
     * 2.反转整个字符串
     * 3.反转各个单词
     */
    public String reverseWords(String s) {
        // System.out.println("ReverseWords.reverseWords2() called with: s = [" + s + "]");
        // 1.去除首尾以及中间多余空格
        StringBuilder sb = removeSpace(s);
        // 2.反转整个字符串
        reverseString(sb, 0, sb.length() - 1);
        // 3.反转各个单词
        reverseEachWord(sb);
        return sb.toString();
    }

    private StringBuilder removeSpace(String s) {
        // System.out.println("ReverseWords.removeSpace() called with: s = [" + s + "]");
        int start = 0;
        int end = s.length() - 1;
        while (s.charAt(start) == ' ') start++;
        while (s.charAt(end) == ' ') end--;
        StringBuilder sb = new StringBuilder();
        while (start <= end) {
            char c = s.charAt(start);
            if (c != ' ' || sb.charAt(sb.length() - 1) != ' ') {
                sb.append(c);
            }
            start++;
        }
        // System.out.println("ReverseWords.removeSpace returned: sb = [" + sb + "]");
        return sb;
    }

    /**
     * 反转字符串指定区间[start, end]的字符
     */
    public void reverseString(StringBuilder sb, int start, int end) {
        // System.out.println("ReverseWords.reverseString() called with: sb = [" + sb + "], start = [" + start + "], end = [" + end + "]");
        while (start < end) {
            char temp = sb.charAt(start);
            sb.setCharAt(start, sb.charAt(end));
            sb.setCharAt(end, temp);
            start++;
            end--;
        }
        // System.out.println("ReverseWords.reverseString returned: sb = [" + sb + "]");
    }

    private void reverseEachWord(StringBuilder sb) {
        int start = 0;
        int end = 1;
        int n = sb.length();
        while (start < n) {
            while (end < n && sb.charAt(end) != ' ') {
                end++;
            }
            reverseString(sb, start, end - 1);
            start = end + 1;
            end = start + 1;
        }
    }
}
```



```java
//双指针
class Solution {
    public String reverseWords(String s) {
        StringBuilder res = new StringBuilder();
        int start = 0 ;
        // 去除前面的空格
        while(s.charAt(start)==' ') start++;
        int len = s.length();
        // 从后往前遍历字符串 定义i，j j记录单词末尾 i为单词起始
        for(int i=len-1 ; i>=start ; i--){
            // 当遍历字符是空格时i,j 同步向前移动，j保存第一个不为空格的字符
            int j = i;
            // 当i位置不为空格时，i继续向前移动知道遇到空格
            while(i>=0 && s.charAt(i)!=' '){
                i--;
            }
            // 若i！=j 此时i代表单词首个字母前一个空格 j为单词末尾
            if(i!=j){
                res.append(s.substring(i+1,j+1)); // substring endindex 不包括j+1；
                // 第一个单词前都加空格
                if(i>start){
                    res.append(' ');
                }
            }
        }
        return res.toString();
    }
}

```



## 二分法

二分用这个模板就不会出错了。满足条件的都写`l = mid`或者`r = mid`，mid首先写成`l + r >> 1`，如果满足条件选择的是`l = mid`，那么mid那里就加个1，写成`l + r + 1 >> 1`。然后就是else对应的写法`l = mid`对应`r = mid - 1`，`r = mid`对应`l = mid + 1`。跟着y总学的，嘿嘿。

```cpp
class Solution {
public:
    vector<int> searchRange(vector<int>& nums, int target) {
        int n = nums.size();

        vector<int> ans(2, -1);
        if (n == 0) return ans;
        
        int l = 0, r = n - 1;
        while (l < r) {
            int mid = l + r >> 1;
            if (nums[mid] >= target) r = mid;
            else l = mid + 1;
        }
        if (nums[r] != target) return ans;
        ans[0] = r;
        l = 0, r = n - 1;
        while (l < r) {
            int mid = l + r + 1 >> 1;
            if (nums[mid] <= target) l = mid;
            else r = mid - 1;
        }
        ans[1] = r;

        return ans;
    }
};
```



#### [34. 在排序数组中查找元素的第一个和最后一个位置](https://leetcode-cn.com/problems/find-first-and-last-position-of-element-in-sorted-array/)

给定一个按照升序排列的整数数组 nums，和一个目标值 target。找出给定目标值在数组中的开始位置和结束位置。

如果数组中不存在目标值 target，返回 [-1, -1]。

进阶：

你可以设计并实现时间复杂度为 O(log n) 的算法解决此问题吗？

```java
class Solution {
    public int[] searchRange(int[] nums, int target) {
        int[] res = new int[] {-1, -1};
        res[0] = binarySearch(nums, target, true);
        res[1] = binarySearch(nums, target, false);
        return res;
    }
    //leftOrRight为true找左边界 false找右边界
    public int binarySearch(int[] nums, int target, boolean leftOrRight) {
        int res = -1;
        int left = 0, right = nums.length - 1, mid;
        while(left <= right) {
            mid = left + (right - left) / 2;
            if(target < nums[mid])
                right = mid - 1;
            else if(target > nums[mid])
                left = mid + 1;

            //处理target == nums[mid]
            else {
                res = mid;//先记录下当前已知的值等于target的坐标,至于是靠近左边还是右边取决于 leftOrRight
                //往左边找
                if(leftOrRight)
                    right = mid - 1;
                else
                    left = mid + 1;
            }
        }
        return res;
    }
}


class Solution {
    public int[] searchRange(int[] nums, int target) {
        int leftIdx = binarySearch(nums, target - 1);
        int rightIdx = binarySearch(nums, target) - 1;
        if (leftIdx <= rightIdx && nums[leftIdx] == target) {
            return new int[]{leftIdx, rightIdx};
        } 
        return new int[]{-1, -1};
    }

    // 第一个大于 target 的数的下标
    public int binarySearch(int[] nums, int target) {
        int left = 0, right = nums.length - 1, ans = nums.length;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (nums[mid] > target) {
                right = mid - 1;
                ans = mid;
            } else {
                left = mid + 1;
            }
        }
        return ans;
    }
}
```



#### [35. 搜索插入位置](https://leetcode-cn.com/problems/search-insert-position/)

给定一个排序数组和一个目标值，在数组中找到目标值，并返回其索引。如果目标值不存在于数组中，返回它将会被按顺序插入的位置。

请必须使用时间复杂度为 O(log n) 的算法。

```java
class Solution {
    public int searchInsert(int[] nums, int target) {

        int left=0,right=nums.length-1;

        while(left<=right){

            int mid=(right-left)/2+left;

            if(target>nums[mid]){ left=mid+1;}
            else if(target < nums[mid]) {right=mid-1;}
            else return mid;
        }

        //上面的while循环最后一次假设为 left=right,先不考虑 target== nums[mid],则若是target>nums[mid] left=mid+1刚好是放置在 mid后面的位置,若<,则left还是等于该放置的位置,但可能难以理解这一步,所以最后 return nums[md] < target? mid+1:mid 会比较好
        return left;
    }
}
```



#### 69.Sqrt(x)

```java
给你一个非负整数 x ，计算并返回 x 的 算术平方根 。

由于返回类型是整数，结果只保留 整数部分 ，小数部分将被 舍去 。

注意：不允许使用任何内置指数函数和算符，例如 pow(x, 0.5) 或者 x ** 0.5 。


class Solution {
    public int mySqrt(int x) {
	//特殊值特殊处理
        if(x == 1 || x == 0){
            return x;
        }
        int left = 0;
        int right = x;
        int mid = 0;
	//经典二分—注意等值处理
        while(left <= right){
            mid = left + (right - left) / 2;
            if(mid > x / mid){
                right = mid - 1;
            }else if(mid < x / mid){
                left = mid + 1;
            }else{
		//相等毫无疑问返回mid
                return mid;
            }
        }
	//否则，则一定是right
        return right;
    }
}

// 如果 mid==left==right,if mid * mid >x,则right=mid-1,  if mid * mid <x,则left=mid+1,无论那种情况right都是小于left,小于
```







#### [367. 有效的完全平方数](https://leetcode-cn.com/problems/valid-perfect-square/)

```java
class Solution {
    public boolean isPerfectSquare(int num) {
        if(num == 1 || num == 0) return true;
        long left=0,right=num;
        
        while(left<=right){
            long mid=(right-left)/2+left;
            long r=mid * mid;
            if(r > num){
                right=mid-1;
            }else if(r < num){
                left=mid+1;
            }else{
                return true;
            }
        }

        return false;
    }
}
```



## 双指针



#### [15. 三数之和](https://leetcode-cn.com/problems/3sum/)

给你一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？请你找出所有和为 0 且不重复的三元组。

注意：答案中不可以包含重复的三元组。

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        
        List<List<Integer>> result=new ArrayList<List<Integer>>();

        //首先判断是否为null
        if(null == nums){
            return result;
        }

        //排序
        Arrays.sort(nums);

        //首先固定一个数,然后利用双指针
        for(int i=0;i<nums.length-2;i++){
            if(nums[i]>0) break;
            if(i>=1 && nums[i] == nums[i-1]) continue;
            
            int target=-nums[i];

            int left=i+1,right=nums.length-1;

            //这一步将 nums[i] 所有可能出现的结果都添加进result中
            while(left<right){
                if(target == nums[left] + nums[right]){
                    result.add(Arrays.asList(new Integer[]{nums[i],nums[left],nums[right]}));
                    left++;right--;
                    while(left<right && nums[left] == nums[left-1]) {left++;}
                    while(left<right && nums[right] == nums[right+1]) {right--;}
                }else if(target > nums[left] + nums[right]){
                    left++;
                }else{
                    right--;
                }
            }

        }

        return result;
    }
}
```



#### [18. 四数之和](https://leetcode-cn.com/problems/4sum/)

给你一个由 n 个整数组成的数组 nums ，和一个目标值 target 。请你找出并返回满足下述全部条件且不重复的四元组 [nums[a], nums[b], nums[c], nums[d]] （若两个四元组元素一一对应，则认为两个四元组重复）：

0 <= a, b, c, d < n
a、b、c 和 d 互不相同
nums[a] + nums[b] + nums[c] + nums[d] == target
你可以按 任意顺序 返回答案 。

```java
class Solution {
    public List<List<Integer>> fourSum(int[] nums, int target) {
        List<List<Integer>> result = new ArrayList<>();
        Arrays.sort(nums);
       
        for (int i = 0; i < nums.length; i++) {

            if (i > 0 && nums[i - 1] == nums[i]) {
                continue;
            }
            
            for (int j = i + 1; j < nums.length; j++) {

                if (j > i + 1 && nums[j - 1] == nums[j]) {
                    continue;
                }

                int left = j + 1;
                int right = nums.length - 1;
                while (right > left) {
                    int sum = nums[i] + nums[j] + nums[left] + nums[right];
                    if (sum > target) {
                        right--;
                    } else if (sum < target) {
                        left++;
                    } else {
                        result.add(Arrays.asList(nums[i], nums[j], nums[left], nums[right]));
                        
                        while (right > left && nums[right] == nums[right - 1]) right--;
                        while (right > left && nums[left] == nums[left + 1]) left++;

                        left++;
                        right--;
                    }
                }
            }
        }
        return result;
    }
}
```



#### [26. 删除有序数组中的重复项](https://leetcode-cn.com/problems/remove-duplicates-from-sorted-array/)

```java
给你一个有序数组 nums ，请你 原地 删除重复出现的元素，使每个元素 只出现一次 ，返回删除后数组的新长度。

不要使用额外的数组空间，你必须在 原地 修改输入数组 并在使用 O(1) 额外空间的条件下完成。

    
public int removeDuplicates(int[] nums) {
    if(nums == null || nums.length == 0) return 0;
    int p = 0;
    int q = 1;
    while(q < nums.length){
        //保证 小于等于slow范围内的元素全是不重复的
        if(nums[p] != nums[q]){
            if(q - p > 1){//这一步是优化
                nums[p + 1] = nums[q];
            }
            p++;
        }
        q++;
    }
    return p + 1;
}

```



#### [27. 移除元素](https://leetcode-cn.com/problems/remove-element/)

给你一个数组 nums 和一个值 val，你需要 原地 移除所有数值等于 val 的元素，并返回移除后数组的新长度。

不要使用额外的数组空间，你必须仅使用 O(1) 额外空间并 原地 修改输入数组。

元素的顺序可以改变。你不需要考虑数组中超出新长度后面的元素。

```java
class Solution {
    public int removeElement(int[] nums, int val) {
        if(nums == null || nums.length == 0) return 0;
        //左指针
        int left = 0;
        //数组的长度 ->  (len - 1)表示右指针的索引
        int len = nums.length;
        //结束循环条件: 双指针相遇
        while (left < len) {
            if (nums[left] == val){
                //左指针元素和右指针元素交换
                nums[left] = nums[len - 1];
                nums[len - 1] = val;
                //每次交换后相当于移除1次val元素
                len--;
            } else {
                //遍历指针指向下一个元素
                left++;
            }
        }
        //val元素全部交换后，新数组长度: len
        return len;
    }
}
```



#### [34. 在排序数组中查找元素的第一个和最后一个位置](https://leetcode-cn.com/problems/find-first-and-last-position-of-element-in-sorted-array/)

给定一个按照升序排列的整数数组 nums，和一个目标值 target。找出给定目标值在数组中的开始位置和结束位置。

如果数组中不存在目标值 target，返回 [-1, -1]。

进阶：

你可以设计并实现时间复杂度为 O(log n) 的算法解决此问题吗？

```java
class Solution {
    public int[] searchRange(int[] nums, int target) {
        int[] res = new int[] {-1, -1};
        res[0] = binarySearch(nums, target, true);
        res[1] = binarySearch(nums, target, false);
        return res;
    }
    //leftOrRight为true找左边界 false找右边界
    public int binarySearch(int[] nums, int target, boolean leftOrRight) {
        int res = -1;
        int left = 0, right = nums.length - 1, mid;
        while(left <= right) {
            mid = left + (right - left) / 2;
            if(target < nums[mid])
                right = mid - 1;
            else if(target > nums[mid])
                left = mid + 1;

            //处理target == nums[mid]
            else {
                res = mid;//先记录下当前已知的值等于target的坐标,至于是靠近左边还是右边取决于 leftOrRight
                //往左边找
                if(leftOrRight)
                    right = mid - 1;
                else
                    left = mid + 1;
            }
        }
        return res;
    }
}
```



#### [283. 移动零](https://leetcode-cn.com/problems/move-zeroes/)

   给定一个数组 nums，编写一个函数将所有 0 移动到数组的末尾，同时保持非零元素的相对顺序。

```java
1.
class Solution {
    public void moveZeroes(int[] nums) {

        int fast=0,slow=0;

        while(fast<nums.length){

            if(nums[fast] != 0){

                if(fast>slow){
                    nums[slow]=nums[fast];
                    nums[fast]=0;        
                }
                slow++;
            }
            fast++;
        }
    }
}

2.    //思路：设置一个index，表示非0数的个数，循环遍历数组，
    // 如果不是0，将非0值移动到第index位置,然后index + 1
    //遍历结束之后，index值表示为非0的个数，再次遍历，从index位置后的位置此时都应该为0
    public void moveZeroes(int[] nums) {
        if (nums == null || nums.length <= 1) {
            return;
        }
        int index = 0;
        for (int i = 0; i < nums.length; i++) {
            if (nums[i] != 0) {
                nums[index] = nums[i];
                index++;
            }
        }

        for (int i = index; i < nums.length; i++) {
            nums[i] = 0;
        }
    }
```



#### [454. 四数相加 II](https://leetcode-cn.com/problems/4sum-ii/)

给你四个整数数组 nums1、nums2、nums3 和 nums4 ，数组长度都是 n ，请你计算有多少个元组 (i, j, k, l) 能满足：

0 <= i, j, k, l < n
nums1[i] + nums2[j] + nums3[k] + nums4[l] == 0

```java
class Solution {
    public int fourSumCount(int[] nums1, int[] nums2, int[] nums3, int[] nums4) {
        Map<Integer, Integer> map = new HashMap<>();
        int temp;
        int res = 0;
        //统计两个数组中的元素之和，同时统计出现的次数，放入map
        for (int i : nums1) {
            for (int j : nums2) {
                temp = i + j;
                if (map.containsKey(temp)) {
                    map.put(temp, map.get(temp) + 1);
                } else {
                    map.put(temp, 1);
                }
            }
        }
        //统计剩余的两个元素的和，在map中找是否存在相加为0的情况，同时记录次数
        for (int i : nums3) {
            for (int j : nums4) {
                temp = i + j;
                if (map.containsKey(0 - temp)) {
                    res += map.get(0 - temp);
                }
            }
        }
        return res;
    }
}
```



#### [844. 比较含退格的字符串](https://leetcode-cn.com/problems/backspace-string-compare/)

给定 S 和 T 两个字符串，当它们分别被输入到空白的文本编辑器后，判断二者是否相等，并返回结果。 # 代表退格字符。

注意：如果对空文本输入退格字符，文本继续为空。

```java
class Solution {
    public boolean backspaceCompare(String s, String t) {
        char[] s_chars=s.toCharArray();
        char[] t_chars=t.toCharArray();

        int s_n=0,t_n=0;
        int s_index=s.length()-1;
        int t_index=t.length()-1;

        // 这里用 || 的原因是可能存在 一方已经完毕了,而另一方剩余的都是 #
        while(s_index>=0 || t_index >=0){
            while(s_index >=0){

                if(s_chars[s_index] == '#'){
                    s_n++;
                    s_index--;
                }else if(s_n >0){
                    s_index--;
                    s_n--;
                }else{
                    break;
                }
            }

            while(t_index>=0){
                if(t_chars[t_index] == '#'){
                    t_n++;
                    t_index--;
                }else if(t_n >0){
                    t_index--;
                    t_n--;
                }else{
                    break;
                }
            }

			//此时的 两个 index : 如果 >=0 ,则必指向数字
            
            if(s_index >=0 && t_index >=0){
                if(s_chars[s_index] != t_chars[t_index]){
                    return false;
                }
            }else if(s_index >=0 || t_index >=0){
                return false;
            }

            s_index--;
            t_index--;
        }

        return true;
    }
}
```



## 滑动窗口

### [209. 长度最小的子数组](https://leetcode-cn.com/problems/minimum-size-subarray-sum/)

给定一个含有 n 个正整数的数组和一个正整数 target 。

找出该数组中满足其和 ≥ target 的长度最小的 连续子数组 [numsl, numsl+1, ..., numsr-1, numsr] ，并返回其长度。如果不存在符合条件的子数组，返回 0 。

```java
class Solution {
    public int minSubArrayLen(int s, int[] nums) {

        int i=0;
        int sum=0;
        int len=0;

        for(int j=0;j<nums.length;j++){
            sum+=nums[j];

            //如果加入 nums[j]后的sum比target大,判断len后就可以减去窗口首位,一直减到当前sum小于target
            while(sum>=s){
                len=len==0?j-i+1:Math.min(len,j-i+1);
                sum-=nums[i++];
            }
        }
        return len;
    }
}
```



### [904. 水果成篮](https://leetcode-cn.com/problems/fruit-into-baskets/)

在一排树中，第 i 棵树产生 tree[i] 型的水果。
你可以从你选择的任何树开始，然后重复执行以下步骤：

把这棵树上的水果放进你的篮子里。如果你做不到，就停下来。
移动到当前树右侧的下一棵树。如果右边没有树，就停下来。
请注意，在选择一颗树后，你没有任何选择：你必须执行步骤 1，然后执行步骤 2，然后返回步骤 1，然后执行步骤 2，依此类推，直至停止。

你有两个篮子，每个篮子可以携带任何数量的水果，但你希望每个篮子只携带一种类型的水果。

用这个程序你能收集的水果树的最大总量是多少？

```java
class Solution {
    public int totalFruit(int[] fruits) {

        Map<Integer,Integer> map=new HashMap<>();

        int left=0;
        int max=-1;
        for(int i=0;i<fruits.length;i++){
            map.put(fruits[i],map.getOrDefault(fruits[i],0)+1);
            
            while(map.size() >2){
                map.put(fruits[left],map.get(fruits[left])-1);
                if(map.get(fruits[left]) == 0) {map.remove(fruits[left]);}
                left++;
            }

            max= max> i-left+1? max:i-left+1;
        }
        return max;
    }
}
```

还有个改良版,我是看不懂 - -!

```java
解题思路
前瞻
仅保留滑动窗口右窗口,省却了滑动窗口左窗口之后,时间复杂度比较卓越
变量
滑动窗口右窗口为i
记录第一个篮子起始位置first,记录第二个篮子起始位置second
记录新的两个篮子中第一个篮子的起始位置temp,有如下规律
(两个篮子中最后一个篮子出现的起始位置,即为新的两个篮子中第一个篮子的起始位置)
水果最大长度len
过程


for循环窗口右滑
    判断是否出现第三个篮子,第一个,第二个篮子值和tree[i]不相等
        如果第一个篮子和第二个篮子不相等,第一个篮子起始位置改成temp(只有最开始第二个篮子和第一个篮子都为0时才相等)
        第二个篮子起始位置改成i
    记录最大长度len
    记录temp
代码

class Solution {
    public int totalFruit(int[] tree) {
        if (tree.length == 0) {
            return 0;
        }
        int len = 0,first = 0,second = 0,temp = 0;
        for (int i = 0; i < tree.length; i++) {
            if (tree[i] != tree[first] && tree[i] != tree[second]){
                if(first != second) {
                    first = temp;
                }
                second = i;
            }
            len = Math.max(len,i - first + 1);
            if(tree[temp] != tree[i]){
                temp = i;
            }
        }
        return len;
    }
}

作者：chun-hua-qiu-shi-2
链接：https://leetcode-cn.com/problems/fruit-into-baskets/solution/javaduo-zhi-zhen-jie-fa-hua-dong-chuang-rs00w/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```



---



### [15. 三数之和](https://leetcode-cn.com/problems/3sum/)

给你一个包含 n 个整数的数组 nums，判断 nums 中是否存在三个元素 a，b，c ，使得 a + b + c = 0 ？请你找出所有和为 0 且不重复的三元组。

注意：答案中不可以包含重复的三元组。

```java
class Solution {
    public List<List<Integer>> threeSum(int[] nums) {
        
        List<List<Integer>> result=new ArrayList<List<Integer>>();

        //首先判断是否为null
        if(null == nums){
            return result;
        }

        //排序
        Arrays.sort(nums);

        //首先固定一个数,然后利用双指针
        for(int i=0;i<nums.length-2;i++){
            
            //排除掉重复出现的nums[i]
            if(nums[i]>0) break;
            if(i>=1 && nums[i] == nums[i-1]) continue;
            
            int target=-nums[i];

            int left=i+1,right=nums.length-1;

            //这一步将 nums[i] 所有可能出现的结果都添加进result中
            while(left<right){
                if(target == nums[left] + nums[right]){
                    result.add(Arrays.asList(new Integer[]{nums[i],nums[left],nums[right]}));
                    left++;right--;
                    while(left<right && nums[left] == nums[left-1]) {left++;}
                    while(left<right && nums[right] == nums[right+1]) {right--;}
                }else if(target > nums[left] + nums[right]){
                    left++;
                }else{
                    right--;
                }
            }

        }

        return result;
    }
}
```



### [467. 环绕字符串中唯一的子字符串](https://leetcode-cn.com/problems/unique-substrings-in-wraparound-string/)

把字符串 s 看作是“abcdefghijklmnopqrstuvwxyz”的无限环绕字符串，所以 s 看起来是这样的："...zabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcd....". 

现在我们有了另一个字符串 p 。你需要的是找出 s 中有多少个唯一的 p 的非空子串，尤其是当你的输入是字符串 p ，你需要输出字符串 s 中 p 的不同的非空子串的数目。 

注意: p 仅由小写的英文字母组成，p 的大小可能超过 10000。

```java
class Solution {
    public int findSubstringInWraproundString(String p) {
        // 记录 p 中以每个字符结尾的最长连续子串的长度
        int[] dp = new int[26];
        char[] array = p.toCharArray();
        // 记录当前连续子串的长度
        int count = 0;
        // 遍历 p 中的所有字符
        for (int i = 0; i < array.length; i++) {
            // 判断字符是否连续
            if (i > 0 && (array[i] - array[i - 1] - 1) % 26 == 0) {
                // 连续则自加
                count++;
            } else {
                // 不连续则刷新
                count = 1;
            }
            // 只存储最长的连续长度,思路是 以每个字母结尾的最大子序列数相加
            dp[array[i] - 'a'] = Math.max(dp[array[i] - 'a'], count);
        }
        int result = 0;
        // 统计所有以每个字符结尾的最长连续子串的长度，就是唯一相等子串的个数
        for (int i : dp) {
            result += i;
        }
        return result;
    }
}
```





#### [剑指 Offer 63. 股票的最大利润](https://leetcode-cn.com/problems/gu-piao-de-zui-da-li-run-lcof/)

假设把某股票的价格按照时间先后顺序存储在数组中，请问买卖该股票一次可能获得的最大利润是多少？

```java
class Solution {
    public int maxProfit(int[] prices) {

/**
int maxProfit(vector<int>& prices){
    int len = prices.size();
    if(len <= 1)
        return 0;
    
    int left = 0, right = 0, maxP = 0;
    while(right < len){
        if((prices[right]-prices[left]) < 0){
            left = right;
            right++;
            continue;
        }
        maxP = max(maxP, prices[right]-prices[left]);
        right++;
    }
    return maxP;
}
 */
        int cost=Integer.MAX_VALUE,profit=0;

        for(int price : prices){

            cost=Math.min(cost,price);
            profit=Math.max(profit,price-cost);
        }

        return profit;
    }
}
```



## 链表



### [19. 删除链表的倒数第 N 个结点](https://leetcode-cn.com/problems/remove-nth-node-from-end-of-list/)

给你一个链表，删除链表的倒数第 `n` 个结点，并且返回链表的头结点。

```java
//快慢指针
class Solution {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode(-1);
        dummy.next = head;

        ListNode slow = dummy;
        ListNode fast = dummy;
        while (n-- > 0) {
            fast = fast.next;
        }
        // 记住 待删除节点slow 的上一节点
        ListNode prev = null;
        while (fast != null) {
            prev = slow;
            slow = slow.next;
            fast = fast.next;
        }
        // 上一节点的next指针绕过 待删除节点slow 直接指向slow的下一节点
        prev.next = slow.next;
        // 释放 待删除节点slow 的next指针, 这句删掉也能AC
        slow.next = null;

        return dummy.next;
    }
}

//栈
class Solution {
    public ListNode removeNthFromEnd(ListNode head, int n) {
        ListNode dummy = new ListNode();
        dummy.next = head;
        ListNode res =dummy;
        Stack<ListNode> stack = new Stack<ListNode>();

        ListNode target = null;

        ListNode pre = null;
        ListNode t = dummy;
        while(t != null){
            stack.push(t);
            t = t.next;
        }

        while( n-- > 1){
            stack.pop();
        }

        target = stack.pop();
        pre = stack.pop();
        pre.next = target.next;

        return dummy.next;


    }
}
```



### [203. 移除链表元素](https://leetcode-cn.com/problems/remove-linked-list-elements/)

给你一个链表的头节点 `head` 和一个整数 `val` ，请你删除链表中所有满足 `Node.val == val` 的节点，并返回 **新的头节点** 。



#### 迭代法

>  创建一个新节点指向head,从新节点角度出发,如果下个节点存在,判断其值是否 == val,如果存在,当前节点的next指向下个节点的next,最后的情况就是指向null,如果不存在说明到达最后一个节点,不用担心最后一个节点的值 == val,因为前面已经清除掉了

```java
class Solution {
    public ListNode removeElements(ListNode head, int val) {

        ListNode first=new ListNode(0);
        first.next=head;
        ListNode index=first;
        while(index.next != null){
            if(index.next.val == val){
                index.next=index.next.next;
               
            }else{
                index=index.next;
            }
                
        }

        return first.next;
    }
}
```



#### 递归

> 递归到最下面一层,如果当前值 == val,返回head.next充当上一层的next

```java
class Solution {
    public ListNode removeElements(ListNode head, int val) {

        if(head == null) return null;
        head.next=removeElements(head.next,val);
        return head.val == val? head.next:head;
    }
}

```











### [707. 设计链表](https://leetcode-cn.com/problems/design-linked-list/)

```java
//单链表
class ListNode {
    int val;
    ListNode next;
    ListNode(){}
    ListNode(int val) {
        this.val=val;
    }
}
class MyLinkedList {
    //size存储链表元素的个数
    int size;
    //虚拟头结点
    ListNode head;

    //初始化链表
    public MyLinkedList() {
        size = 0;
        head = new ListNode(0);
    }

    //获取第index个节点的数值
    public int get(int index) {
        //如果index非法，返回-1
        if (index < 0 || index >= size) {
            return -1;
        }
        ListNode currentNode = head;
        //包含一个虚拟头节点，所以查找第 index+1 个节点
        for (int i = 0; i <= index; i++) {
            currentNode = currentNode.next;
        }
        return currentNode.val;
    }

    //在链表最前面插入一个节点
    public void addAtHead(int val) {
        addAtIndex(0, val);
    }

    //在链表的最后插入一个节点
    public void addAtTail(int val) {
        addAtIndex(size, val);
    }

    // 在第 index 个节点之前插入一个新节点，例如index为0，那么新插入的节点为链表的新头节点。
    // 如果 index 等于链表的长度，则说明是新插入的节点为链表的尾结点
    // 如果 index 大于链表的长度，则返回空
    public void addAtIndex(int index, int val) {
        if (index > size) {
            return;
        }
        if (index < 0) {
            index = 0;
        }
        size++;
        //找到要插入节点的前驱
        ListNode pred = head;
        for (int i = 0; i < index; i++) {
            pred = pred.next;
        }
        ListNode toAdd = new ListNode(val);
        toAdd.next = pred.next;
        pred.next = toAdd;
    }

    //删除第index个节点
    public void deleteAtIndex(int index) {
        if (index < 0 || index >= size) {
            return;
        }
        size--;
        ListNode pred = head;
        for (int i = 0; i < index; i++) {
            pred = pred.next;
        }
        pred.next = pred.next.next;
    }
}
```



我自己写的

```java
class ListNode{
    int val;
    ListNode next;

    public ListNode(){};
    public ListNode(int val){
        this.val = val;
    }
}
class MyLinkedList {
    ListNode pre;
    int size;

    public MyLinkedList() {
        pre = new ListNode();
    }
    
    public int get(int index) {
        if(index < 0 || index >=size){return -1;}
        ListNode node = pre;
        while(index > 0){
            node = node.next;
            index--;
        }

        return node.next.val;
    }
    
    public void addAtHead(int val) {
        addAtIndex(-1,val);
    }
    
    public void addAtTail(int val) {
        addAtIndex(size,val);
    }
    
    public void addAtIndex(int index, int val) {

        if(index > size){return;}
        ListNode node = new ListNode(val);
        if(index < 0){
            node.next = pre.next;
            pre.next = node;
            size++;
            return;
        }

        ListNode head = pre;

        while(index > 0){
            head = head.next;
            index--;
        }
        node.next = head.next;
        head.next = node;
        size++;
    }
    
    public void deleteAtIndex(int index) {
        if(index >= size || index < 0){return;}

        ListNode head = pre;

        while(index > 0){
            head = head.next;
            index--;
        }
        head.next = head.next.next;
        size--;
    }
}

/**
 * Your MyLinkedList object will be instantiated and called as such:
 * MyLinkedList obj = new MyLinkedList();
 * int param_1 = obj.get(index);
 * obj.addAtHead(val);
 * obj.addAtTail(val);
 * obj.addAtIndex(index,val);
 * obj.deleteAtIndex(index);
 */
```



---



## 二叉树



### [98. 验证二叉搜索树](https://leetcode-cn.com/problems/validate-binary-search-tree/)

给你一个二叉树的根节点 root ，判断其是否是一个有效的二叉搜索树。

有效 二叉搜索树定义如下：

节点的左子树只包含 小于 当前节点的数。
节点的右子树只包含 大于 当前节点的数。
所有左子树和右子树自身必须也是二叉搜索树。




```java
class Solution {
    TreeNode pre;
    public boolean isValidBST(TreeNode root) {
        if(root == null) return true;
        if(!isValidBST(root.left)){ return false;}
        if(pre == null) pre=root;
        else if(pre.val>=root.val) return false;
        pre=root;
        return isValidBST(root.right);
    }
}
```



### [106. 从中序与后序遍历序列构造二叉树](https://leetcode-cn.com/problems/construct-binary-tree-from-inorder-and-postorder-traversal/)

根据一棵树的中序遍历与后序遍历构造二叉树。

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */

 /**
     后序遍历:根节点在最后
     中序遍历:根据根节点的位置来分左右子树
  */
class Solution {
    int[] inorder,postorder;
    int post_idx;
    Map<Integer,Integer> map=new HashMap<>();

    public TreeNode buildTree(int[] inorder, int[] postorder) {
        this.inorder=inorder;
        this.postorder=postorder;

        int in_left=0;
        int in_right=inorder.length-1;

        post_idx=postorder.length-1;
        //int index=0;
        for(int i=0;i<inorder.length;i++){
            map.put(inorder[i],i);
        }

        return helper(0,in_right);
    }

    private TreeNode helper(int left,int right){
        if(left>right) return null;
        int rootVal=postorder[post_idx];
        TreeNode root=new TreeNode(rootVal);
        

        int in_idx=map.get(rootVal);
        post_idx--;
        root.right=helper(in_idx+1,right);
        root.left=helper(left,in_idx-1);

        
        return root;
    }
}
```



### [110. 平衡二叉树](https://leetcode-cn.com/problems/balanced-binary-tree/)

给定一个二叉树，判断它是否是高度平衡的二叉树。

本题中，一棵高度平衡二叉树定义为：

> 一个二叉树*每个节点* 的左右两个子树的高度差的绝对值不超过 1 。



```java
class Solution {
    public boolean isBalanced(TreeNode root) {
        
        return recur(root)!=-1;
    }

    private int recur(TreeNode root){
        if(root == null) return 0;
        int left=recur(root.left);
        if(left == -1) return -1;
        int right=recur(root.right);
        if(right == -1) return -1;

        return Math.abs(left-right) < 2?Math.max(left,right)+1:-1;
    }
}
```





### [199. 二叉树的右视图](https://leetcode-cn.com/problems/binary-tree-right-side-view/)

给定一个二叉树的 **根节点** `root`，想象自己站在它的右侧，按照从顶部到底部的顺序，返回从右侧所能看到的节点值。

```JAVA
//BFS
思路： 利用 BFS 进行层次遍历，记录下每层的最后一个元素。
时间复杂度： O(N)O(N)，每个节点都入队出队了 1 次。
空间复杂度： O(N)O(N)，使用了额外的队列空间。

class Solution {
    public List<Integer> rightSideView(TreeNode root) {
        List<Integer> res = new ArrayList<>();
        if (root == null) {
            return res;
        }
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                TreeNode node = queue.poll();
                if (node.left != null) {
                    queue.offer(node.left);
                }
                if (node.right != null) {
                    queue.offer(node.right);
                }
                if (i == size - 1) {  //将当前层的最后一个节点放入结果列表
                    res.add(node.val);
                }
            }
        }
        return res;
    }
}



//DFS
思路： 我们按照 「根结点 -> 右子树 -> 左子树」 的顺序访问，就可以保证每层都是最先访问最右边的节点的。

（与先序遍历 「根结点 -> 左子树 -> 右子树」 正好相反，先序遍历每层最先访问的是最左边的节点）

时间复杂度： O(N)O(N)，每个节点都访问了 1 次。
空间复杂度： O(N)O(N)，因为这不是一棵平衡二叉树，二叉树的深度最少是 logNlogN, 最坏的情况下会退化成一条链表，深度就是 NN，因此递归时使用的栈空间是 O(N)O(N) 的。


class Solution {
    List<Integer> res = new ArrayList<>();

    public List<Integer> rightSideView(TreeNode root) {
        dfs(root, 0); // 从根节点开始访问，根节点深度是0
        return res;
    }

    private void dfs(TreeNode root, int depth) {
        if (root == null) {
            return;
        }
        // 先访问 当前节点，再递归地访问 右子树 和 左子树。
        if (depth == res.size()) {   // 如果当前节点所在深度还没有出现在res里，说明在该深度下当前节点是第一个被访问的节点，因此将当前节点加入res中。
            res.add(root.val);
        }
        depth++;
        dfs(root.right, depth);
        dfs(root.left, depth);
    }
}

```



### [222. 完全二叉树的节点个数](https://leetcode-cn.com/problems/count-complete-tree-nodes/)

给你一棵 完全二叉树 的根节点 root ，求出该树的节点个数。

完全二叉树 的定义如下：在完全二叉树中，除了最底层节点可能没填满外，其余每层节点数都达到最大值，并且最下面一层的节点都集中在该层最左边的若干位置。若最底层为第 h 层，则该层包含 1~ 2h 个节点。

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    public int countNodes(TreeNode root) {
        /**
        完全二叉树的高度可以直接通过不断地访问左子树就可以获取
        判断左右子树的高度: 
        如果相等说明左子树是满二叉树, 然后进一步判断右子树的节点数(最后一层最后出现的节点必然在右子树中)
        如果不等说明右子树是深度小于左子树的满二叉树, 然后进一步判断左子树的节点数(最后一层最后出现的节点必然在左子树中)
        **/
        if (root==null) return 0;
        int ld = getDepth(root.left);
        int rd = getDepth(root.right);
        if(ld == rd) return (1 << ld) + countNodes(root.right); // 1(根节点) + (1 << ld)-1(左完全左子树节点数) + 右子树节点数量
        else return (1 << rd) + countNodes(root.left);  // 1(根节点) + (1 << rd)-1(右完全右子树节点数) + 左子树节点数量
        
    }

    private int getDepth(TreeNode r) {
        int depth = 0;
        while(r != null) {
            depth++;
            r = r.left;
        }
        return depth;
    }
}
```





### [230. 二叉搜索树中第K小的元素](https://leetcode-cn.com/problems/kth-smallest-element-in-a-bst/)

给定一个二叉搜索树的根节点 `root` ，和一个整数 `k` ，请你设计一个算法查找其中第 `k` 个最小元素（从 1 开始计数）。

```java
class Solution {
    public int kthSmallest(TreeNode root, int k) {
        // Deque<TreeNode> d = new ArrayDeque<>();
        // while (root != null || !d.isEmpty()) {
        //     while (root != null) {
        //         d.addLast(root);
        //         root = root.left;
        //     }
        //     root = d.pollLast();
        //     if (--k == 0) return root.val;
        //     root = root.right;
        // }

        Stack<TreeNode> stack=new Stack<>();

        while(root != null || !stack.isEmpty()){
            //这句这要是防止root.right 为 null
            while(root!= null){
                stack.push(root);
                root=root.left;
            }
            root = stack.pop();
            if(--k == 0) return root.val;
            root=root.right;
        }
        return -1; // never
    }
}
```



### [235. 二叉搜索树的最近公共祖先](https://leetcode-cn.com/problems/lowest-common-ancestor-of-a-binary-search-tree/)

给定一个二叉搜索树, 找到该树中两个指定节点的最近公共祖先。

百度百科中最近公共祖先的定义为：“对于有根树 T 的两个结点 p、q，最近公共祖先表示为一个结点 x，满足 x 是 p、q 的祖先且 x 的深度尽可能大（一个节点也可以是它自己的祖先）。”

例如，给定如下二叉搜索树:  root = [6,2,8,0,4,7,9,null,null,3,5]



```java
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        
        TreeNode ances=root;

        while(true){
            if(ances.val < p.val && ances.val <q.val){
                ances=ances.right;
            }else if(ances.val > p.val && ances.val > q.val){
                ances=ances.left;
            }else break;
        }
        return ances;
    }
}
```





### [236. 二叉树的最近公共祖先](https://leetcode-cn.com/problems/lowest-common-ancestor-of-a-binary-tree/)

给定一个二叉树, 找到该树中两个指定节点的最近公共祖先。

百度百科中最近公共祖先的定义为：“对于有根树 T 的两个节点 p、q，最近公共祖先表示为一个节点 x，满足 x 是 p、q 的祖先且 x 的深度尽可能大（一个节点也可以是它自己的祖先）。”

 ```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        
        1.
        if(root == null || root == p || root == q) return root;
        
        TreeNode left = lowestCommonAncestor(root.left, p, q);
        TreeNode right = lowestCommonAncestor(root.right, p, q);
        
        //由于 left == null && root !=p && !=q 所以只可能出现在 right中
        if(left == null) return right;
        if(right == null) return left;
        return root;
    }

 ```





### [257. 二叉树的所有路径](https://leetcode-cn.com/problems/binary-tree-paths/)

给你一个二叉树的根节点 `root` ，按 **任意顺序** ，返回所有从根节点到叶子节点的路径。

**叶子节点** 是指没有子节点的节点

```java
class Solution {
    List<String> res=new ArrayList<>();
    public List<String> binaryTreePaths(TreeNode root) {
        dps(root,"");
        return res;
    }

    private void dps(TreeNode root,String s){
        StringBuilder sb=new StringBuilder(s);
        if(root != null){
            sb.append(Integer.toString(root.val));
           // s=s+root.val;

            if(root.left == null && root.right == null){
                res.add(sb.toString());
            }else{
                sb.append("->");
                //s=s+"->";
                dps(root.left,sb.toString());
                dps(root.right,sb.toString());
            }
        }
    }
}
```





### [404. 左叶子之和](https://leetcode-cn.com/problems/sum-of-left-leaves/)

计算给定二叉树的所有左叶子之和。

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    public int sumOfLeftLeaves(TreeNode root) {
        return dfs(root);
    }

    private int dfs(TreeNode root){

        if(root == null) return 0;

        int left=dfs(root.left);
        int right=dfs(root.right);
        int mid=0;

        if(root.left != null && root.left.left == null && root.left.right == null){
            mid=root.left.val;
        }

        //这里加这三个数的原因是: 本题是由父类判断子类是否是左叶子,假如 A->(B,(C->D,E)),B已经得到确认,但还得确认B子树的左叶子结点,该值就体现在 right上,所以得加 mid 和left ,right
        return left+right+mid;
    }
}
```



### [429. N 叉树的层序遍历](https://leetcode-cn.com/problems/n-ary-tree-level-order-traversal/)

给定一个 N 叉树，返回其节点值的*层序遍历*。（即从左到右，逐层遍历）。

树的序列化输入是用层序遍历，每组子节点都由 null 值分隔（参见示例）。

 

```java
/*
// Definition for a Node.
class Node {
    public int val;
    public List<Node> children;

    public Node() {}

    public Node(int _val) {
        val = _val;
    }

    public Node(int _val, List<Node> _children) {
        val = _val;
        children = _children;
    }
};
*/
//二叉树多出一个for循环
class Solution {
    public List<List<Integer>> levelOrder(Node root) {
        
        List<List<Integer>> res=new ArrayList<>();
        Queue<Node> queue=new LinkedList<>();

        if(root != null){
            queue.offer(root);    
        }

        while(!queue.isEmpty()){
            int queueSize=queue.size();
             List list=new ArrayList<Integer>();
            for(int j=0;j<queueSize;j++){
                Node node=queue.poll();
                int len=node.children.size();
                
                for(int i=0;i<len;i++){
                    queue.add(node.children.get(i));
                }
                list.add(node.val);
            }
            res.add(list);
        }
        return res;
    }
}
```





### [450. 删除二叉搜索树中的节点](https://leetcode-cn.com/problems/delete-node-in-a-bst/)

给定一个二叉搜索树的根节点 root 和一个值 key，删除二叉搜索树中的 key 对应的节点，并保证二叉搜索树的性质不变。返回二叉搜索树（有可能被更新）的根节点的引用。

一般来说，删除节点可分为两个步骤：

首先找到需要删除的节点；
如果找到了，删除它。

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    public TreeNode deleteNode(TreeNode root, int key) {

        if(root == null) return null;

        TreeNode cur=root;
        TreeNode pre=null;

        // cur != null 主要是防止叶子结点出现导致空指针
        while(cur != null){
            if(cur.val == key) break;
            pre=cur;
            if(cur.val > key){ cur=cur.left;}
            else{ cur=cur.right;}
        }

        if(pre == null){
            return deleteOneNode(root);
        }

        //当前的pre必定是目标节点的上方
        if(pre.left!= null && pre.left.val == key){
            pre.left=deleteOneNode(cur);
        }

        if(pre.right!= null && pre.right.val == key){
            pre.right=deleteOneNode(cur);
        }

        return root;
    }

    private TreeNode deleteOneNode(TreeNode root){

        if(root.right == null) {return root.left;}
        if(root.left == null){ return root.right;}
        
        //如果两侧都不为 null
        TreeNode cur=root.right;
        while(cur.left != null){ cur=cur.left;}

        cur.left=root.left;
        return root.right;
    }
}
```



### [513. 找树左下角的值](https://leetcode-cn.com/problems/find-bottom-left-tree-value/)

给定一个二叉树的 **根节点** `root`，请找出该二叉树的 **最底层 最左边** 节点的值。

假设二叉树中至少有一个节点。

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {

    int maxDepth;
    TreeNode node;
    public int findBottomLeftValue(TreeNode root) {
        dfs(root,0);
        return node.val;
    }

    private void dfs(TreeNode root,int depth){
        if(root == null) return;
        depth++;
        if(root.left==null && root.right==null){
            if(depth > maxDepth){
                maxDepth=depth;
                node=root;
            }
        }else{
            dfs(root.left,depth);
            dfs(root.right,depth);
        }
    }
}
```







### [669. 修剪二叉搜索树](https://leetcode-cn.com/problems/trim-a-binary-search-tree/)

给你二叉搜索树的根节点 root ，同时给定最小边界low 和最大边界 high。通过修剪二叉搜索树，使得所有节点的值在[low, high]中。修剪树不应该改变保留在树中的元素的相对结构（即，如果没有被移除，原有的父代子代关系都应当保留）。 可以证明，存在唯一的答案。

所以结果应当返回修剪好的二叉搜索树的新的根节点。注意，根节点可能会根据给定的边界发生改变。

```java
/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode() {}
 *     TreeNode(int val) { this.val = val; }
 *     TreeNode(int val, TreeNode left, TreeNode right) {
 *         this.val = val;
 *         this.left = left;
 *         this.right = right;
 *     }
 * }
 */
class Solution {
    public TreeNode trimBST(TreeNode root, int low, int high) {
        if (root == null) {
            return null;
        }

        //如果 root.val 不合格,就踢掉,往下找合格的,然后再返回遇到的第一个合格的节点就行
        if (root.val < low) {
            return trimBST(root.right, low, high);
        }
        if (root.val > high) {
            return trimBST(root.left, low, high);
        }
        // root在[low,high]范围内
        root.left = trimBST(root.left, low, high);
        root.right = trimBST(root.right, low, high);
        return root;
    }
}
```



### [116. 填充每个节点的下一个右侧节点指针](https://leetcode-cn.com/problems/populating-next-right-pointers-in-each-node/)

给定一个 **完美二叉树** ，其所有叶子节点都在同一层，每个父节点都有两个子节点。二叉树定义如下：.....    填充它的每个 next 指针，让这个指针指向其下一个右侧节点。如果找不到下一个右侧节点，则将 next 指针设置为 NULL。

初始状态下，所有 next 指针都被设置为 NULL。

```java
public Node connect(Node root) {
        if (root == null)
            return root;
        //cur我们可以把它看做是每一层的链表
        Node cur = root;
        while (cur != null) {
            //遍历当前层的时候，为了方便操作在下一
            //层前面添加一个哑结点（注意这里是访问
            //当前层的节点，然后把下一层的节点串起来）
            Node dummy = new Node(0);
            //pre表示下一层节点的前一个节点
            Node pre = dummy;
            
            //然后开始遍历当前层的链表
            //因为是完美二叉树，如果有左子节点就一定有右子节点
            while (cur != null && cur.left != null) {
                //让pre节点的next指向当前节点的左子节点，也就是把它串起来
                pre.next = cur.left;
                //然后再更新pre
                pre = pre.next;

                //pre节点的next指向当前节点的右子节点，
                pre.next = cur.right;
                pre = pre.next;
                //继续访问这一行的下一个节点
                cur = cur.next;
            }
            //把下一层串联成一个链表之后，让他赋值给cur，
            //后续继续循环，直到cur为空为止
            cur = dummy.next;
        }
        return root;
    }

作者：sdwwld
链接：https://leetcode-cn.com/problems/populating-next-right-pointers-in-each-node/solution/bfshe-di-gui-zui-hou-liang-chong-ji-bai-liao-100-2/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```



### [5927. 反转偶数长度组的节点](https://leetcode-cn.com/problems/reverse-nodes-in-even-length-groups/)

给你一个链表的头节点 head 。

链表中的节点 按顺序 划分成若干 非空 组，这些非空组的长度构成一个自然数序列（1, 2, 3, 4, ...）。一个组的 长度 就是组中分配到的节点数目。换句话说：

节点 1 分配给第一组
节点 2 和 3 分配给第二组
节点 4、5 和 6 分配给第三组，以此类推
注意，最后一组的长度可能小于或者等于 1 + 倒数第二组的长度 。

反转 每个 偶数 长度组中的节点，并返回修改后链表的头节点 head 。

```java
class Solution {
    public ListNode reverseEvenLengthGroups(ListNode head) {
          if(head==null || head.next==null) return head;
           return  reverse(head,1);// 1 表示初始状态下 分组的长度
           // 之后 为2，3，4，.....
    }
    ListNode  reverse(ListNode head, int group ){
        if(head==null || head.next==null) return head;
        ListNode slow = head; // 每组链表的表头
        ListNode fast =  head; //下一组链表的表头
        ListNode end= head ; // 每组链表的末尾
        int count=0; // 记录当前已经遍历这组链表的节点个数
        for( int i=0;i<group;++i){
            if(fast==null) { // 针对最后一组链表 长度达不到group
                if(count<group && count%2!=0){ //当最后一组链表的长度为奇数
                    ListNode tmp= reverse(fast, group+1 ); 
                    return slow;
                }else if(count<group &&  count %2==0){  // 当最后一组链表的长度为偶数
                    ListNode  newHead =reverseList(slow,fast);
                    slow.next= reverse(fast, group+1); // 下一组链表的起始地址位置
                    return newHead;
                } 
                return null; 
            }else{
                 count++;
            } 
            if(i<group-1){
                end=end.next; // 遍历每组链表的节点 得到 该组链表的尾节点
            }

            fast=fast.next;
        }

        if(group%2!=0){
            ListNode tmp = reverse(fast, group+1);
            end.next= tmp;
            return slow;  // 这里进行递归回去
        }else{
             ListNode  newHead =reverseList(slow,fast);
              slow.next= reverse(fast, group+1);
              return newHead; 
        }

    }

    ListNode reverseList(ListNode head ,ListNode fast ){
        ListNode curNode=head;
        ListNode pre =null; 
        //只反转第 n 组: 1-> 2 -> 3  => 1 <- 2 <- 3,没有涉及组外
        while(curNode!=fast){
            ListNode temp= curNode.next;
            curNode.next = pre;
            pre= curNode;
            curNode=temp; 
        }

        return pre;
    }
}
```



## 贪心算法



### [56. 合并区间](https://leetcode-cn.com/problems/merge-intervals/)

以数组 intervals 表示若干个区间的集合，其中单个区间为 intervals[i] = [starti, endi] 。请你合并所有重叠的区间，并返回一个不重叠的区间数组，该数组需恰好覆盖输入中的所有区间。

```java
class Solution {
    public int[][] merge(int[][] intervals) {

        Arrays.sort(intervals,(a,b)->{
            if(a[0] != b[0]){return Integer.compare(a[0],b[0]);}
            else return Integer.compare(a[1],b[1]);
        });

        LinkedList<int[]> list=new LinkedList<>();
        int start=intervals[0][0];
        for(int i=1;i<intervals.length;i++){
            //更新最大的右边界
            if(intervals[i][0] <= intervals[i-1][1]){
                intervals[i][1]=Math.max(intervals[i][1],intervals[i-1][1]);
            }else{
                list.add(new int[]{start,intervals[i-1][1]});
                
                //记录区间的开始
                start=intervals[i][0];
            }
        }

        list.add(new int[]{start,intervals[intervals.length-1][1]});
        return list.toArray(new int[list.size()][]);
    }
}


```





### [376. 摆动序列](https://leetcode-cn.com/problems/wiggle-subsequence/)

如果连续数字之间的差严格地在正数和负数之间交替，则数字序列称为 摆动序列 。第一个差（如果存在的话）可能是正数或负数。仅有一个元素或者含两个不等元素的序列也视作摆动序列。

例如， [1, 7, 4, 9, 2, 5] 是一个 摆动序列 ，因为差值 (6, -3, 5, -7, 3) 是正负交替出现的。

相反，[1, 4, 7, 2, 5] 和 [1, 7, 4, 5, 5] 不是摆动序列，第一个序列是因为它的前两个差值都是正数，第二个序列是因为它的最后一个差值为零。
子序列 可以通过从原始序列中删除一些（也可以不删除）元素来获得，剩下的元素保持其原始顺序。

给你一个整数数组 nums ，返回 nums 中作为 摆动序列 的 最长子序列的长度 。

```java
class Solution {
    public int wiggleMaxLength(int[] nums) {
        //首先排除掉 nums.length == 1 || nums == null 的情况
        if(nums == null || nums.length == 1) {return nums.length;}

        //由于是从数量2开始,所以至少为1
        int count=1;

        int cur=0,pre=0;

        for(int i=1;i<nums.length;i++){
            cur=nums[i]-nums[i-1];
            if((cur > 0 && pre <= 0) || (cur < 0 && pre >=0)){
                count++;
                pre=cur;
            }
        }

        return count;
    }
}
```





### [53. 最大子序和](https://leetcode-cn.com/problems/maximum-subarray/)

给定一个整数数组 `nums` ，找到一个具有最大和的连续子数组（子数组最少包含一个元素），返回其最大和。

```java
class Solution {
    public int maxSubArray(int[] nums) {

        int res=Integer.MIN_VALUE;
        int count=0;
		//局部最优推整体最优
        for(int i=0;i<nums.length;i++){
            count+=nums[i];
			//如果连续位置相加大于0就可以判断是否为当前最大值
            if(count > res){
                res=count;
            }
            //相加后小于0就没必要再累计结果了
            if(count < 0){
                count = 0;
            }
        }

        return res;
    }
}
```



### [134. 加油站](https://leetcode-cn.com/problems/gas-station/)

在一条环路上有 N 个加油站，其中第 i 个加油站有汽油 gas[i] 升。

你有一辆油箱容量无限的的汽车，从第 i 个加油站开往第 i+1 个加油站需要消耗汽油 cost[i] 升。你从其中的一个加油站出发，开始时油箱为空。

如果你可以绕环路行驶一周，则返回出发时加油站的编号，否则返回 -1。

说明: 

如果题目有解，该答案即为唯一答案。
输入数组均为非空数组，且长度相同。
输入数组中的元素均为非负数。

```java
// 解法1
class Solution {
    public int canCompleteCircuit(int[] gas, int[] cost) {
        int sum = 0;
        int min = 0;
        for (int i = 0; i < gas.length; i++) {
            sum += (gas[i] - cost[i]);
            min = Math.min(sum, min);
        }

        //不可能跑完
        if (sum < 0) return -1;

        //从0开始是能跑完的
        if (min >= 0) return 0;

        for (int i = gas.length - 1; i > 0; i--) {
            //由于从0开始到某个特定位置发现油不够消耗了,那么思路是从0的前面找能填补这个坑的出发点,因为是个环路,所以0前面就是数组的倒叙
            min += (gas[i] - cost[i]);
            if (min >= 0) return i;
        }

        return -1;
    }
}
```

```java
// 解法2
class Solution {
    public int canCompleteCircuit(int[] gas, int[] cost) {
        int curSum = 0;
        int totalSum = 0;
        int index = 0;
        for (int i = 0; i < gas.length; i++) {
            curSum += gas[i] - cost[i];
            totalSum += gas[i] - cost[i];
            
            //如果加上当前值小于0,说明起点选择错误,尝试下一个节点; 假设: 1 2 3,到2这里就小于0,为什么要选3开始,跳过2呢?原因是 2 必然是小于0的,代码发现小于0就直接跳过,选下一个
            if (curSum < 0) {
                //这里用 % 是因为环路,如果题目要求答出路径,用% , 但本题只要求从哪个节点开始,直接 i+1 也行,因为从第一个节点开始检测,如果totalSum大于0,就说明最终的start停留的节点是没问题的
                index = (i + 1) % gas.length ; 
                curSum = 0;
            }
        }
        if (totalSum < 0) return -1;
        return index;
    }
}
```



#### [135. 分发糖果](https://leetcode-cn.com/problems/candy/)

老师想给孩子们分发糖果，有 N 个孩子站成了一条直线，老师会根据每个孩子的表现，预先给他们评分。

你需要按照以下要求，帮助老师给这些孩子分发糖果：

每个孩子至少分配到 1 个糖果。
评分更高的孩子必须比他两侧的邻位孩子获得更多的糖果。
那么这样下来，老师至少需要准备多少颗糖果呢？

```java
class Solution {
    public int candy(int[] ratings) {
        int[] candy = new int[ratings.length];
        for (int i = 0; i < candy.length; i++) {
            candy[i] = 1;
        }

        for (int i = 1; i < ratings.length; i++) {
            if (ratings[i] > ratings[i - 1]) {
                candy[i] = candy[i - 1] + 1;
            }
        }

        for (int i = ratings.length - 2; i >= 0; i--) {
            if (ratings[i] > ratings[i + 1]) {
                //这里的candy[i]是因为从前往后遍历可能出现都是 index<index+1,导致当前的candy[i]远大于candy[i+1]+1
                candy[i] = Math.max(candy[i],candy[i + 1] + 1);
            }
        }

        int count = 0;
        for (int i = 0; i < candy.length; i++) {
            count += candy[i];
        }

        return count;
    }
}
```





#### [406. 根据身高重建队列](https://leetcode-cn.com/problems/queue-reconstruction-by-height/)

```java
class Solution {
    public int[][] reconstructQueue(int[][] people) {

        Arrays.sort(people,(a,b)->{
            if(a[0] == b[0]) return a[1]-b[1];
            else return b[0]-a[0];
        });

        LinkedList<int[]> list=new LinkedList<>();
		
        //贪心法,自己按照排序后的队列推演下
        for(int[] p:people){
            list.add(p[1],p);
        }

        return list.toArray(new int[people.length][]);
    }
}
```



#### [435. 无重叠区间](https://leetcode-cn.com/problems/non-overlapping-intervals/)

给定一个区间的集合，找到需要移除区间的最小数量，使剩余区间互不重叠。

注意:

可以认为区间的终点总是大于它的起点。
区间 [1,2] 和 [2,3] 的边界相互“接触”，但没有相互重叠。

```java
//由452迁移过来的思路,一箭代表一个不重叠的区域
class Solution {
    public int eraseOverlapIntervals(int[][] intervals) {
        Arrays.sort(intervals,(a,b)->{
            if(a[0] == b[0]){ return Integer.compare(a[1],b[1]);}
            else {return Integer.compare(a[0],b[0]);}
        });

        int count=1;
        for(int i=1;i<intervals.length;i++){
            if(intervals[i][0] >= intervals[i-1][1]){
                count++;
            }else{
                intervals[i][1]=Math.min(intervals[i][1],intervals[i-1][1]);
            }
        }

        return intervals.length-count;
    }
}
```



#### [438. 找到字符串中所有字母异位词](https://leetcode-cn.com/problems/find-all-anagrams-in-a-string/)

给定两个字符串 s 和 p，找到 s 中所有 p 的 异位词 的子串，返回这些子串的起始索引。不考虑答案输出的顺序。

异位词 指由相同字母重排列形成的字符串（包括相同的字符串）。

```java
// class Solution {
//     public List<Integer> findAnagrams(String s, String p) {
//         int n = s.length(), m = p.length();
//         List<Integer> res = new ArrayList<>();
//         if(n < m) return res;
//         int[] pCnt = new int[26];
//         int[] sCnt = new int[26];
//         for(int i = 0; i < m; i++){
//             pCnt[p.charAt(i) - 'a']++;
//             sCnt[s.charAt(i) - 'a']++;
//         }
//         if(Arrays.equals(sCnt, pCnt)){
//             res.add(0);
//         }
//         for(int i = m; i < n; i++){
//             sCnt[s.charAt(i - m) - 'a']--;
//             sCnt[s.charAt(i) - 'a']++;
//             if(Arrays.equals(sCnt, pCnt)){
//                 res.add(i - m + 1);
//             }
//         }
//         return res;
//     }
// }


class Solution {
    public List<Integer> findAnagrams(String s, String p) {
        int n = s.length(), m = p.length();
        List<Integer> res = new ArrayList<>();
        if(n < m) return res;

        int[] pCnt = new int[26];
        int[] sCnt = new int[26];

        for(int i = 0; i < m; i++){
            pCnt[p.charAt(i) - 'a'] ++;
        }

        int left = 0;
        for(int right = 0; right < n; right++){
            int curRight = s.charAt(right) - 'a';
            sCnt[curRight]++;
            //多了就减到相等就行
            while(sCnt[curRight] > pCnt[curRight]){
                int curLeft = s.charAt(left) - 'a';
                sCnt[curLeft]--;
                left++;
            }
            if(right - left + 1 == m){
                res.add(left);
            }
        }
        return res;
    }
}
```



### [452. 用最少数量的箭引爆气球](https://leetcode-cn.com/problems/minimum-number-of-arrows-to-burst-balloons/)

在二维空间中有许多球形的气球。对于每个气球，提供的输入是水平方向上，气球直径的开始和结束坐标。由于它是水平的，所以纵坐标并不重要，因此只要知道开始和结束的横坐标就足够了。开始坐标总是小于结束坐标。

一支弓箭可以沿着 x 轴从不同点完全垂直地射出。在坐标 x 处射出一支箭，若有一个气球的直径的开始和结束坐标为 xstart，xend， 且满足  xstart ≤ x ≤ xend，则该气球会被引爆。可以射出的弓箭的数量没有限制。 弓箭一旦被射出之后，可以无限地前进。我们想找到使得所有气球全部被引爆，所需的弓箭的最小数量。

给你一个数组 points ，其中 points [i] = [xstart,xend] ，返回引爆所有气球所必须射出的最小弓箭数。

```java
class Solution {
    public int findMinArrowShots(int[][] points) {
        Arrays.sort(points,(a,b)->{
            return Integer.compare(a[0],b[0]);
        });
        
        int current=0;
        int count=0;

        for(int i=1;i<points.length;i++){
            if(points[i-1][1] < points[i][0]){
                count++;
            }else{
                //保证能射爆局部最多的气球的有边界情况,局部最优推全局最优
                points[i][1]=Math.min(points[i][1],points[i-1][1]);
            }
        }

        //这个代表最后一组(可能有多个))
        count++;
        return count;

    }
}

```





### [738. 单调递增的数字](https://leetcode-cn.com/problems/monotone-increasing-digits/)

给定一个非负整数 N，找出小于或等于 N 的最大的整数，同时这个整数需要满足其各个位数上的数字是单调递增。

（当且仅当每个相邻位数上的数字 x 和 y 满足 x <= y 时，我们称这个整数是单调递增的。）

```java
class Solution {
    public int monotoneIncreasingDigits(int N) {
        String[] strings = (N + "").split("");
        int start = strings.length;
        for (int i = strings.length - 1; i > 0; i--) {
            if (Integer.parseInt(strings[i]) < Integer.parseInt(strings[i - 1])) {
                //只要前面的大于后面,前面的数必须减1,后面的数必为9,用start来记录从第几位开始往后为9
                strings[i - 1] = (Integer.parseInt(strings[i - 1]) - 1) + "";
                start = i;
            }
        }
        for (int i = start; i < strings.length; i++) {
            strings[i] = "9";
        }
        return Integer.parseInt(String.join("",strings));
    }
}
```





### [763. 划分字母区间](https://leetcode-cn.com/problems/partition-labels/)

字符串 `S` 由小写字母组成。我们要把这个字符串划分为尽可能多的片段，同一字母最多出现在一个片段中。返回一个表示每个字符串片段的长度的列表。

```java
class Solution {
    public List<Integer> partitionLabels(String S) {
        List<Integer> res=new ArrayList<>();
        char[] chars=S.toCharArray();
        int[] edge=new int[123];

        for(int i=0;i<S.length();i++){
            //更新字母的最远距离
            edge[chars[i]]=i;
        }

        int index=0;
        int last=-1;
        for(int i=0;i<S.length();i++){
            index= Math.max(index,edge[chars[i]]);
            if(index == i){
                res.add(i-last);
                last=i;
            }           
        }

        return res;
    }
}
```





### [860. 柠檬水找零](https://leetcode-cn.com/problems/lemonade-change/)

在柠檬水摊上，每一杯柠檬水的售价为 5 美元。顾客排队购买你的产品，（按账单 bills 支付的顺序）一次购买一杯。

每位顾客只买一杯柠檬水，然后向你付 5 美元、10 美元或 20 美元。你必须给每个顾客正确找零，也就是说净交易是每位顾客向你支付 5 美元。

注意，一开始你手头没有任何零钱。

给你一个整数数组 bills ，其中 bills[i] 是第 i 位顾客付的账。如果你能给每位顾客正确找零，返回 true ，否则返回 false 。

```java
//虽然看不懂为什么这就是贪心
class Solution {
    public boolean lemonadeChange(int[] bills) {
        int cash_5 = 0;
        int cash_10 = 0;

        for (int i = 0; i < bills.length; i++) {
            if (bills[i] == 5) {
                cash_5++;
            } else if (bills[i] == 10) {
                cash_5--;
                cash_10++;
            } else if (bills[i] == 20) {
                if (cash_10 > 0) {
                    cash_10--;
                    cash_5--;
                } else {
                    cash_5 -= 3;
                }
            }
            if (cash_5 < 0 || cash_10 < 0) return false;
        }
        
        return true;
    }
}
```







### [1005. K 次取反后最大化的数组和](https://leetcode-cn.com/problems/maximize-sum-of-array-after-k-negations/)

给定一个整数数组 A，我们只能用以下方法修改该数组：我们选择某个索引 i 并将 A[i] 替换为 -A[i]，然后总共重复这个过程 K 次。（我们可以多次选择同一个索引 i。）

以这种方式修改数组后，返回数组可能的最大和。

```java
class Solution {
    public int largestSumAfterKNegations(int[] A, int K) {
        if (A.length == 1) return K % 2 == 0 ? A[0] : -A[0];
        Arrays.sort(A);
        int sum = 0;
        int idx = 0;
        for (int i = 0; i < K; i++) {

            //如果当前数是复数 and abs(当前数)>abs(下一个数),那么指针就停在下一个数
            //if 当前数小于其next,那么指针就一直停在当前数
            if (i < A.length - 1 && A[idx] < 0) {
                A[idx] = -A[idx];
                if (A[idx] >= Math.abs(A[idx + 1])) {idx++;}
                continue;
            }
            A[idx] = -A[idx];
        }

        for (int i = 0; i < A.length; i++) {
            sum += A[i];
        }
        return sum;
    }
}




class Solution {
    public int largestSumAfterKNegations(int[] nums, int k) {
        if(nums.length == 1){
            int t=k%2;
            if(t == 0){
                return nums[0];
            }else{
                return -nums[0];
            }
        }

        Arrays.sort(nums);

        int index=0;

        for(int i=0;i<k;i++){

            // 这里的 nums.length-1 和下面的 if搭配,保证了index最后停留的数的绝对值是最小的
            if(index < nums.length-1 && nums[index] < 0){
                nums[index]=-nums[index];

                if(nums[index] >= Math.abs(nums[index + 1])){
                    index++;
                }
                continue;
            }

            nums[index]=-nums[index];
        }

        int sum=0;
        for(Integer i:nums){
            sum+=i;
        }
        return sum;
    }
}
```







## 栈与队列

#### [1047. 删除字符串中的所有相邻重复项](https://leetcode-cn.com/problems/remove-all-adjacent-duplicates-in-string/)

给出由小写字母组成的字符串 S，重复项删除操作会选择两个相邻且相同的字母，并删除它们。

在 S 上反复执行重复项删除操作，直到无法继续删除。

在完成所有重复项删除操作后返回最终的字符串。答案保证唯一。

```java
class Solution {
    public String removeDuplicates(String S) {
        char[] s = S.toCharArray();
        int top = -1;
        for (int i = 0; i < S.length(); i++) {
            if (top == -1 || s[top] != s[i]) {
                s[++top] = s[i];
            } else {
                top--;
            }
        }
        return String.valueOf(s, 0, top + 1);
    }
}
```





#### [239. 滑动窗口最大值](https://leetcode-cn.com/problems/sliding-window-maximum/)

给你一个整数数组 nums，有一个大小为 k 的滑动窗口从数组的最左侧移动到数组的最右侧。你只可以看到在滑动窗口内的 k 个数字。滑动窗口每次只向右移动一位。

返回滑动窗口中的最大值。

```java
class Solution {
    public int[] maxSlidingWindow(int[] nums, int k) {
        if(nums == null || nums.length < 2) return nums;
        // 双向队列 保存当前窗口最大值的数组位置 保证队列中数组位置的数值按从大到小排序
        LinkedList<Integer> queue = new LinkedList();
        // 结果数组
        int[] result = new int[nums.length-k+1];
        // 遍历nums数组
        for(int i = 0;i < nums.length;i++){
            // 保证从大到小 如果前面数小则需要依次弹出，直至满足要求
            while(!queue.isEmpty() && nums[queue.peekLast()] <= nums[i]){
                queue.pollLast();
            }
            // 添加当前值对应的数组下标
            queue.addLast(i);
            // 判断当前队列中队首的值是否有效
            if(queue.peek() <= i-k){
                queue.poll();   
            } 
            // 当窗口长度为k时 保存当前窗口中最大值
            if(i+1 >= k){
                //设k=3 ,i=2推一下就得出公式
                result[i+1-k] = nums[queue.peek()];
            }
        }
        return result;
    }
}
```



#### [225. 用队列实现栈](https://leetcode-cn.com/problems/implement-stack-using-queues/)

请你仅使用两个队列实现一个后入先出（LIFO）的栈，并支持普通栈的全部四种操作（push、top、pop 和 empty）。

实现 MyStack 类：

void push(int x) 将元素 x 压入栈顶。
int pop() 移除并返回栈顶元素。
int top() 返回栈顶元素。
boolean empty() 如果栈是空的，返回 true ；否则，返回 false 。


注意：

你只能使用队列的基本操作 —— 也就是 push to back、peek/pop from front、size 和 is empty 这些操作。
你所使用的语言也许不支持队列。 你可以使用 list （列表）或者 deque（双端队列）来模拟一个队列 , 只要是标准的队列操作即可。



```java
class MyStack {

    Queue<Integer> queue1; // 和栈中保持一样元素的队列
    Queue<Integer> queue2; // 辅助队列

    /** Initialize your data structure here. */
    public MyStack() {
        queue1 = new LinkedList<>();
        queue2 = new LinkedList<>();
    }
    
    /** Push element x onto stack. */
    public void push(int x) {
        queue2.offer(x); // 先放在辅助队列中
        while (!queue1.isEmpty()){
            queue2.offer(queue1.poll());
        }
        Queue<Integer> queueTemp;
        queueTemp = queue1;
        queue1 = queue2;
        queue2 = queueTemp; // 最后交换queue1和queue2，将元素都放到queue1中
    }
    
    /** Removes the element on top of the stack and returns that element. */
    public int pop() {
        return queue1.poll(); // 因为queue1中的元素和栈中的保持一致，所以这个和下面两个的操作只看queue1即可
    }
    
    /** Get the top element. */
    public int top() {
        return queue1.peek();
    }
    
    /** Returns whether the stack is empty. */
    public boolean empty() {
        return queue1.isEmpty();
    }
}
```



#### [347. 前 K 个高频元素](https://leetcode-cn.com/problems/top-k-frequent-elements/)

给你一个整数数组 `nums` 和一个整数 `k` ，请你返回其中出现频率前 `k` 高的元素。你可以按 **任意顺序** 返回答案。

```java
class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        int[] result = new int[k];
        HashMap<Integer, Integer> map = new HashMap<>();
        for (int num : nums) {
            map.put(num, map.getOrDefault(num, 0) + 1);
        }

        Set<Map.Entry<Integer, Integer>> entries = map.entrySet();
        // 根据map的value值正序排，相当于一个小顶堆
        PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>((o1, o2) -> o1.getValue() - o2.getValue());
        for (Map.Entry<Integer, Integer> entry : entries) {
            queue.offer(entry);
            if (queue.size() > k) {
                //这就是不能用大顶堆的原因
                queue.poll();
            }
        }
        for (int i = k - 1; i >= 0; i--) {
            result[i] = queue.poll().getKey();
        }
        return result;
    }
}
```





## 单调栈



#### [739. 每日温度](https://leetcode-cn.com/problems/daily-temperatures/)

请根据每日 `气温` 列表 `temperatures` ，请计算在每一天需要等几天才会有更高的温度。如果气温在这之后都不会升高，请在该位置用 `0` 来代替

```java
class Solution {
    public int[] dailyTemperatures(int[] temperatures) {

        //单调栈
        Stack<Integer> stack=new Stack<>();
        int[] res=new int[temperatures.length];

        for(int i=0;i<temperatures.length;i++){
            while(!stack.isEmpty() && temperatures[i] > temperatures[stack.peek()]){
                res[stack.peek()]=i-stack.pop();
            }

            stack.push(i);
        }

        return res;
    }
}
```





#### [496. 下一个更大元素 I](https://leetcode-cn.com/problems/next-greater-element-i/)

给你两个 没有重复元素 的数组 nums1 和 nums2 ，其中nums1 是 nums2 的子集。

请你找出 nums1 中每个元素在 nums2 中的下一个比其大的值。

nums1 中数字 x 的下一个更大元素是指 x 在 nums2 中对应位置的右边的第一个比 x 大的元素。如果不存在，对应位置输出 -1 。



```java
class Solution {
    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        int n = nums1.length;
        int m = nums2.length;

        // 自定义哈希表
        int[] map = new int[10001];
        for (int i = 0; i < n; i++) {
            map[nums1[i]] = i + 1;
        }

        int[] ans = new int[n];

        // 自定义栈的写法
        int[] stack = new int[m];
        int size = 0;
        for (int i = m - 1; i >= 0; i--) {
            // 弹出栈顶比当前元素小的数
            while (size > 0 && stack[size - 1] < nums2[i]) {
                size--;
            }

            // map中含有的才计算
            if (map[nums2[i]] != 0) {
                // 构造答案,因为是从后往前的顺序,因此 nums2[i]比它大的第一个一定在栈的顶端,只要size不为0,就说明一定存在
                ans[map[nums2[i]] - 1] = size == 0 ? -1 : stack[size - 1];
            }

            // 当前元素入栈
            stack[size++] = nums2[i];
        }

        return ans;
    }
}

```



#### [503. 下一个更大元素 II](https://leetcode-cn.com/problems/next-greater-element-ii/)

给定一个循环数组（最后一个元素的下一个元素是数组的第一个元素），输出每个元素的下一个更大元素。数字 x 的下一个更大的元素是按数组遍历顺序，这个数字之后的第一个比它更大的数，这意味着你应该循环地搜索它的下一个更大的数。如果不存在，则输出 -1。

```java
//这种方法有一部分是重复的
class Solution {
    public int[] nextGreaterElements(int[] nums) {
        //边界判断
        if(nums == null || nums.length <= 1) {
            return new int[]{-1};
        }
        int size = nums.length;
        int[] result = new int[size];//存放结果
        Arrays.fill(result,-1);//默认全部初始化为-1
        Stack<Integer> st= new Stack<>();//栈中存放的是nums中的元素下标
        for(int i = 0; i < 2*size; i++) {
                            //如果当前值大于 栈顶的元素,说明找到第一个大于当前栈顶元素的值,更新后弹出,这样一直循环
            while(!st.empty() && nums[i % size] > nums[st.peek()]) {
                result[st.peek()] = nums[i % size];//更新result
                st.pop();//弹出栈顶
            }
            st.push(i % size);
        }
        return result;
    }
}

	//这种更巧妙
    public int[] nextGreaterElement(int[] nums) {
        Deque<Integer> deque = new LinkedList<>();
        int[] ans = new int[nums.length];
        for (int i = nums.length - 1; i >= 0; i--) {
            deque.push(nums[i]);
        }
        for (int i = nums.length - 1; i >= 0; i--) {
            while (!deque.isEmpty() && nums[i] >= deque.peek()) {
                deque.pop();
            }
            if (deque.isEmpty()) {
                ans[i] = -1;
                deque.push(nums[i]);
            } else {
                ans[i] = deque.peek();
                deque.push(nums[i]);
            }
        }
        return ans;
    }
```





## 回溯算法

#### [17. 电话号码的字母组合](https://leetcode-cn.com/problems/letter-combinations-of-a-phone-number/)

给定一个仅包含数字 2-9 的字符串，返回所有它能表示的字母组合。答案可以按 任意顺序 返回。

给出数字到字母的映射如下（与电话按键相同）。注意 1 不对应任何字母。



```java
class Solution {

    //设置全局列表存储最后的结果
    List<String> list = new ArrayList<>();

    public List<String> letterCombinations(String digits) {
        if (digits == null || digits.length() == 0) {
            return list;
        }
        //初始对应所有的数字，为了直接对应2-9，新增了两个无效的字符串""
        String[] numString = {"", "", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        //迭代处理
        backTracking(digits, numString, 0);
        return list;

    }

    //每次迭代获取一个字符串，所以会设计大量的字符串拼接，所以这里选择更为高效的 StringBuild
    StringBuilder temp = new StringBuilder();

    //比如digits如果为"23",num 为0，则str表示2对应的 abc
    public void backTracking(String digits, String[] numString, int num) {
        //遍历全部一次记录一次得到的字符串
        if (num == digits.length()) {
            list.add(temp.toString());
            return;
        }
        //str 表示当前num对应的字符串
        String str = numString[digits.charAt(num) - '0'];
        for (int i = 0; i < str.length(); i++) {
            temp.append(str.charAt(i));
            //c
            backTracking(digits, numString, num + 1);
            //剔除末尾的继续尝试
            temp.deleteCharAt(temp.length() - 1);
        }
    }
}
```





#### [40. 组合总和 II](https://leetcode-cn.com/problems/combination-sum-ii/)

给定一个数组 candidates 和一个目标数 target ，找出 candidates 中所有可以使数字和为 target 的组合。

candidates 中的每个数字在每个组合中只能使用一次。

注意：解集不能包含重复的组合。 

```java
class Solution {
    List<List<Integer>> lists = new ArrayList<>();
    Deque<Integer> deque = new LinkedList<>();
    int sum = 0;

    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        //为了将重复的数字都放到一起，所以先进行排序
        Arrays.sort(candidates);
        //加标志数组，用来辅助判断同层节点是否已经遍历
        boolean[] flag = new boolean[candidates.length];
        backTracking(candidates, target, 0, flag);
        return lists;
    }

    public void backTracking(int[] arr, int target, int index, boolean[] flag) {
        if (sum == target) {
            lists.add(new ArrayList(deque));
            return;
        }
        for (int i = index; i < arr.length && arr[i] + sum <= target; i++) {
            //出现重复节点，同层的第一个节点已经被访问过，所以直接跳过
            if (i > 0 && arr[i] == arr[i - 1] && !flag[i - 1]) {
                continue;
            }

            //标志当前节点如果与下一个节点同值的话,是可以使用的,最后为什么要设置成 false呢? 因为防止同一层出现重复
            flag[i] = true;

            sum += arr[i];
            deque.push(arr[i]);
            //每个节点仅能选择一次，所以从下一位开始
            backTracking(arr, target, i + 1, flag);
            int temp = deque.pop();

            //记得设置成false
            flag[i] = false;

            sum -= temp;
        }
    }
}
```



#### [77. 组合](https://leetcode-cn.com/problems/combinations/)

给定两个整数 `n` 和 `k`，返回范围 `[1, n]` 中所有可能的 `k` 个数的组合。

你可以按 **任何顺序** 返回答案。

```java
class Solution {
    List<List<Integer>> result = new ArrayList<>();
    LinkedList<Integer> path = new LinkedList<>();
    public List<List<Integer>> combine(int n, int k) {
        combineHelper(n, k, 1);
        return result;
    }
    
    private void combineHelper(int n, int k, int startIndex){

        if(path.size() == k){
            result.add(new ArrayList<>(path));
            return;
        }
        //k-path.size() 得到 还差多少个数, +1是因为包括左区间, n=5 ,k=5,得包含1才行
        for(int i = startIndex; i <= n - (k-path.size()) + 1 ; i++){
            path.add(i);
            combineHelper(n,k,i+1);
            path.removeLast();
        }
    }
}






class Solution {
    LinkedList<Integer> list;
    List<List<Integer>> res;
    public List<List<Integer>> combine(int n, int k) {

        res = new ArrayList<List<Integer>>();
        list = new LinkedList<Integer>();

        dfs(n,k,1);
        return res;
    }


    private void dfs(int n, int k,int startIdx){

        if(list.size() == k){res.add(new ArrayList(list));return;}
    // n - k + 1 是保证 从 [1,n-k+1]任意一个数开始都可以达到k个数的组合,如果当前已经有 x 个数,那么就可以往后推 x 个位置
        for(int i = startIdx; i <= n - k + 1 + list.size(); i++){

            list.add(i);
            dfs(n,k,i+1);
            list.removeLast();
        }
    }
}
```







#### [79. 单词搜索](https://leetcode-cn.com/problems/word-search/)

给定一个 m x n 二维字符网格 board 和一个字符串单词 word 。如果 word 存在于网格中，返回 true ；否则，返回 false 。

单词必须按照字母顺序，通过相邻的单元格内的字母构成，其中“相邻”单元格是那些水平相邻或垂直相邻的单元格。同一个单元格内的字母不允许被重复使用。

```java
class Solution {
    private boolean[][] used;
    private int row, col;
    private char[][] board;
    private char[] ws;
    private int[][] direction = new int[][] {{-1, 0}, {1, 0}, {0, 1}, {0, -1}};

    public boolean exist(char[][] board, String word) {
        this.row = board.length;
        this.col = board[0].length;
        this.used = new boolean[row][col];
        for (boolean[] u : used) {
            Arrays.fill(u, false);
        }

        this.board = board;
        this.ws = word.toCharArray();

        for (int i = 0; i < row; ++i) {
            for (int j = 0; j < col; ++j) {
                if (board[i][j] == ws[0]) {
                    used[i][j] = true;
                    if (dfs(i, j, 1)) {
                        return true;
                    } else {
                        used[i][j] = false;
                    }
                }
            }
        }
        return false;
    }

    private boolean dfs(int i, int j, int depth) {
        if (depth == ws.length) {
            return true;
        }

        for (int[] d : direction) {
            int newX = i + d[0];
            int newY = j + d[1];
            // 剪枝
            if (! inArea(newX, newY)) {
                continue;
            }
            // 剪枝
            if (used[newX][newY]) {
                continue;
            }
            // 剪枝
            if (board[newX][newY] != ws[depth]) {
                continue;
            }

            used[newX][newY] = true;
            if (dfs(newX, newY, depth + 1)) {
                return true;
            } else {
                used[newX][newY] = false;
            }
        }
        return false;
    }

    private boolean inArea(int x, int y) {
        return x >= 0 && x < this.row && y >= 0 && y < this.col;
    }
}
```



#### [93. 复原 IP 地址](https://leetcode-cn.com/problems/restore-ip-addresses/)

有效 IP 地址 正好由四个整数（每个整数位于 0 到 255 之间组成，且不能含有前导 0），整数之间用 '.' 分隔。

例如："0.1.2.201" 和 "192.168.1.1" 是 有效 IP 地址，但是 "0.011.255.245"、"192.168.1.312" 和 "192.168@1.1" 是 无效 IP 地址。
给定一个只包含数字的字符串 s ，用以表示一个 IP 地址，返回所有可能的有效 IP 地址，这些地址可以通过在 s 中插入 '.' 来形成。你不能重新排序或删除 s 中的任何数字。你可以按 任何 顺序返回答案。

```java
class Solution {
    List<String> result = new ArrayList<>();

    public List<String> restoreIpAddresses(String s) {
        if (s.length() > 12) return result; // 算是剪枝了
        backTrack(s, 0, 0);
        return result;
    }

    // startIndex: 搜索的起始位置， pointNum:添加逗点的数量
    private void backTrack(String s, int startIndex, int pointNum) {
        if (pointNum == 3) {// 逗点数量为3时，分隔结束
            // 判断第四段⼦字符串是否合法，如果合法就放进result中
            if (isValid(s,startIndex,s.length()-1)) {
                result.add(s);
            }
            return;
        }
        for (int i = startIndex; i < s.length(); i++) {
            if (isValid(s, startIndex, i)) {
                s = s.substring(0, i + 1) + "." + s.substring(i + 1);    //在str的后⾯插⼊⼀个逗点
                pointNum++;
                backTrack(s, i + 2, pointNum);// 插⼊逗点之后下⼀个⼦串的起始位置为i+2
                pointNum--;// 回溯
                s = s.substring(0, i + 1) + s.substring(i + 2);// 回溯删掉逗点
            } else {
                break;
            }
        }
    }

    // 判断字符串s在左闭⼜闭区间[start, end]所组成的数字是否合法
    private Boolean isValid(String s, int start, int end) {
        if (start > end) {
            return false;
        }
        if (s.charAt(start) == '0' && start != end) { // 0开头的数字不合法
            return false;
        }
        int num = 0;
        for (int i = start; i <= end; i++) {
            if (s.charAt(i) > '9' || s.charAt(i) < '0') { // 遇到⾮数字字符不合法
                return false;
            }
            num = num * 10 + (s.charAt(i) - '0');
            if (num > 255) { // 如果⼤于255了不合法
                return false;
            }
        }
        return true;
    }
}
```



#### [131. 分割回文串](https://leetcode-cn.com/problems/palindrome-partitioning/)

给你一个字符串 `s`，请你将 `s` 分割成一些子串，使每个子串都是 **回文串** 。返回 `s` 所有可能的分割方案。

**回文串** 是正着读和反着读都一样的字符串。

```java
class Solution {
    List<List<String>> lists = new ArrayList<>();
    Deque<String> deque = new LinkedList<>();

    public List<List<String>> partition(String s) {
        backTracking(s, 0);
        return lists;
    }

    private void backTracking(String s, int startIndex) {
        //如果起始位置大于s的大小，说明找到了一组分割方案
        if (startIndex >= s.length()) {
            lists.add(new ArrayList(deque));
            return;
        }
        for (int i = startIndex; i < s.length(); i++) {
            //如果是回文子串，则记录
            if (isPalindrome(s, startIndex, i)) {
                String str = s.substring(startIndex, i + 1);
                deque.addLast(str);
            } else {
                continue;
            }
            //起始位置后移，保证不重复
            backTracking(s, i + 1);
            deque.removeLast();
        }
    }
    //判断是否是回文串
    private boolean isPalindrome(String s, int startIndex, int end) {
        for (int i = startIndex, j = end; i < j; i++, j--) {
            if (s.charAt(i) != s.charAt(j)) {
                return false;
            }
        }
        return true;
    }
}
```



#### [216. 组合总和 III](https://leetcode-cn.com/problems/combination-sum-iii/)

找出所有相加之和为 ***n*** 的 ***k\*** 个数的组合***。\***组合中只允许含有 1 - 9 的正整数，并且每种组合中不存在重复的数字。

```java
class Solution {
    List<List<Integer>> res=new ArrayList<>();
    LinkedList<Integer> path=new LinkedList<>();
    public List<List<Integer>> combinationSum3(int k, int n) {
        combineHelper(k, n, 1,0);
        return res;
    }


    private void combineHelper(int k, int n, int startIdx,int sum){

        if(path.size() > k || sum > n){return;}
        else if(path.size() == k && sum == n){res.add(new ArrayList<>(path)); return;}

        for(int i=startIdx; i <= 9 - (k - path.size()) + 1; i++){
            path.add(i);
            sum+=i;
            combineHelper(k,n,i+1,sum);
            path.removeLast();
            sum-=i;
        }

        
    }
}
```







## 位运算



#### [剑指 Offer 56 - I. 数组中数字出现的次数](https://leetcode-cn.com/problems/shu-zu-zhong-shu-zi-chu-xian-de-ci-shu-lcof/)

一个整型数组 `nums` 里除两个数字之外，其他数字都出现了两次。请写程序找出这两个只出现一次的数字。要求时间复杂度是O(n)，空间复杂度是O(1)。

 

```java
class Solution {
    public int[] singleNumbers(int[] nums) {
        //因为相同的数字异或为0，任何数字与0异或结果是其本身。
        //所以遍历异或整个数组最后得到的结果就是两个只出现一次的数字异或的结果：即 z = x ^ y
        int z = 0;  
        for(int i : nums) z ^= i;
        //我们根据异或的性质可以知道：z中至少有一位是1，否则x与y就是相等的。
        //我们通过一个辅助变量m来保存z中哪一位为1.（可能有多个位都为1，我们找到最低位的1即可）。
        //举个例子：z = 10 ^ 2 = 1010 ^ 0010 = 1000,第四位为1.
        //我们将m初始化为1，如果（z & m）的结果等于0说明z的最低为是0
        //我们每次将m左移一位然后跟z做与操作，直到结果不为0.
        //此时m应该等于1000，同z一样，第四位为1.
        int m = 1;
        while((z & m) == 0) m <<= 1;
        //我们遍历数组，将每个数跟m进行与操作，结果为0的作为一组，结果不为0的作为一组
        //例如对于数组：[1,2,10,4,1,4,3,3]，我们把每个数字跟1000做与操作，可以分为下面两组：
        //nums1存放结果为0的: [1, 2, 4, 1, 4, 3, 3]
        //nums2存放结果不为0的: [10] (碰巧nums2中只有一个10，如果原数组中的数字再大一些就不会这样了)
        //此时我们发现问题已经退化为数组中有一个数字只出现了一次
        //分别对nums1和nums2遍历异或就能得到我们预期的x和y
        int x = 0, y = 0;
        for(int i : nums) {
            //这里我们是通过if...else将nums分为了两组，一边遍历一遍异或。
            //跟我们创建俩数组nums1和nums2原理是一样的。
            if((i & m) == 0) x ^= i;
            else y ^= i;
        }
        return new int[]{x, y};
    }
}
```



## 其他

### [7. 整数反转](https://leetcode-cn.com/problems/reverse-integer/)

给你一个 32 位的有符号整数 x ，返回将 x 中的数字部分反转后的结果。

如果反转后整数超过 32 位的有符号整数的范围 [−231,  231 − 1] ，就返回 0。

假设环境不允许存储 64 位整数（有符号或无符号）。

```java
class Solution {
    public int reverse(int x) {
        int res = 0;
        while(x!=0) {
            //每次取末尾数字
            int tmp = x%10;
            //判断是否 大于 最大32位整数,这里为什么不全部算完再比较大小呢,因为担心超出int范围,所以提前一位来比较
            if (res>214748364 || (res==214748364 && tmp>7)) {
                return 0;
            }
            //判断是否 小于 最小32位整数
            if (res<-214748364 || (res==-214748364 && tmp<-8)) {
                return 0;
            }
            res = res*10 + tmp;
            x /= 10;
        }
        return res;
    }
}
```



#### [9. 回文数](https://leetcode-cn.com/problems/palindrome-number/)

给你一个整数 x ，如果 x 是一个回文整数，返回 true ；否则，返回 false 。

回文数是指正序（从左向右）和倒序（从右向左）读都是一样的整数。例如，121 是回文，而 123 不是。



```java
class Solution {
    public boolean isPalindrome(int x) {
        int _old=x;
        int _new=0;
        if((x % 10 == 0 && x != 0) || x <0){return false;}

        //循环中止可能有两种：数字长度为偶数：则可能是左右均分但不是回文导致大小不相等，也可能是左右均分且是回文数，所以  _old == _new 成立 ； 数字长度为奇数： 虽然是回文数，但_new得去掉最后一位才能相等,_old == _new/10;
        while(_old > _new){
            
            _new=_new * 10 + _old %10;
            _old=_old/10;
        }

        return _old == _new || _old == _new/10;
        
    }
}
```





#### [13. 罗马数字转整数](https://leetcode-cn.com/problems/roman-to-integer/)



```java
class Solution {
    public int romanToInt(String s) {
        int sum = 0;
        int preNum = getValue(s.charAt(0));
        for(int i = 1;i < s.length(); i ++) {
            int num = getValue(s.charAt(i));
            if(preNum < num) {
                sum -= preNum;
            } else {
                sum += preNum;
            }
            preNum = num;
        }
        sum += preNum;
        return sum;
    }
    
    private int getValue(char ch) {
        switch(ch) {
            case 'I': return 1;
            case 'V': return 5;
            case 'X': return 10;
            case 'L': return 50;
            case 'C': return 100;
            case 'D': return 500;
            case 'M': return 1000;
            default: return 0;
        }
    }
}

作者：donespeak
链接：https://leetcode-cn.com/problems/roman-to-integer/solution/yong-shi-9993nei-cun-9873jian-dan-jie-fa-by-donesp/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```



#### [31. 下一个排列](https://leetcode-cn.com/problems/next-permutation/)

实现获取 下一个排列 的函数，算法需要将给定数字序列重新排列成字典序中下一个更大的排列（即，组合出下一个更大的整数）。

如果不存在下一个更大的排列，则将数字重新排列成最小的排列（即升序排列）。

必须 原地 修改，只允许使用额外常数空间。

```java
class Solution {
    public void nextPermutation(int[] nums) {
        int i = nums.length - 2;
        while (i >= 0 && nums[i] >= nums[i + 1]) {
            i--;
        }
        if (i >= 0) {
            int j = nums.length - 1;
            while (j >= 0 && nums[i] >= nums[j]) {
                j--;
            }
            swap(nums, i, j);
        }
        reverse(nums, i + 1);
    }

    public void swap(int[] nums, int i, int j) {
        int temp = nums[i];
        nums[i] = nums[j];
        nums[j] = temp;
    }

    public void reverse(int[] nums, int start) {
        int left = start, right = nums.length - 1;
        while (left < right) {
            swap(nums, left, right);
            left++;
            right--;
        }
    }
}

// 作者：LeetCode-Solution
// 链接：https://leetcode-cn.com/problems/next-permutation/solution/xia-yi-ge-pai-lie-by-leetcode-solution/
// 来源：力扣（LeetCode）
// 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```





#### [剑指 Offer 13. 机器人的运动范围](https://leetcode-cn.com/problems/ji-qi-ren-de-yun-dong-fan-wei-lcof/)

地上有一个m行n列的方格，从坐标 [0,0] 到坐标 [m-1,n-1] 。一个机器人从坐标 [0, 0] 的格子开始移动，它每次可以向左、右、上、下移动一格（不能移动到方格外），也不能进入行坐标和列坐标的数位之和大于k的格子。例如，当k为18时，机器人能够进入方格 [35, 37] ，因为3+5+3+7=18。但它不能进入方格 [35, 38]，因为3+5+3+8=19。请问该机器人能够到达多少个格子？

```java
class Solution {
    public int movingCount(int m, int n, int k) {
        boolean[][] visited = new boolean[m][n];
        return dfs(0, 0, m, n, k, visited);
    }

    private int dfs(int i, int j, int m, int n, int k, boolean visited[][]) {
        if (i < 0 || i >= m || j < 0 || j >= n || (i/10 + i%10 + j/10 + j%10) > k || visited[i][j]) {
            return 0;
        }
        visited[i][j] = true;
        return dfs(i + 1, j, m, n, k, visited) + dfs(i - 1, j, m, n, k, visited) + 
               dfs(i, j + 1, m, n, k, visited) + dfs(i, j - 1, m, n, k, visited) + 1;
    }
}
```





### [剑指 Offer 29. 顺时针打印矩阵](https://leetcode-cn.com/problems/shun-shi-zhen-da-yin-ju-zhen-lcof/)

输入一个矩阵，按照从外向里以顺时针的顺序依次打印出每一个数字。

```java
class Solution {
    public int[] spiralOrder(int[][] matrix) {
        if(matrix.length == 0) return new int[0];
        int l = 0, r = matrix[0].length - 1, t = 0, b = matrix.length - 1, x = 0;
        int[] res = new int[(r + 1) * (b + 1)];
        while(true) {
            for(int i = l; i <= r; i++) res[x++] = matrix[t][i]; // left to right.
            if(++t > b) break;
            for(int i = t; i <= b; i++) res[x++] = matrix[i][r]; // top to bottom.
            if(l > --r) break;
            for(int i = r; i >= l; i--) res[x++] = matrix[b][i]; // right to left.
            if(t > --b) break;
            for(int i = b; i >= t; i--) res[x++] = matrix[i][l]; // bottom to top.
            if(++l > r) break;
        }
        return res;
    }
}
```



### [66. 加一](https://leetcode-cn.com/problems/plus-one/)

给定一个由 整数 组成的 非空 数组所表示的非负整数，在该数的基础上加一。

最高位数字存放在数组的首位， 数组中每个元素只存储单个数字。

你可以假设除了整数 0 之外，这个整数不会以零开头。

```java
class Solution {
    public int[] plusOne(int[] digits) {
        for (int i = digits.length - 1; i >= 0; i--) {
            digits[i]++;
            digits[i] = digits[i] % 10;
            if (digits[i] != 0) return digits;
        }
        digits = new int[digits.length + 1];
        digits[0] = 1;
        return digits;
    }
}
```



#### [150. 逆波兰表达式求值](https://leetcode-cn.com/problems/evaluate-reverse-polish-notation/)

根据 逆波兰表示法，求表达式的值。

有效的算符包括 +、-、*、/ 。每个运算对象可以是整数，也可以是另一个逆波兰表达式。

 

说明：

整数除法只保留整数部分。
给定逆波兰表达式总是有效的。换句话说，表达式总会得出有效数值且不存在除数为 0 的情况。

```java
class Solution {
    public int evalRPN(String[] tokens) {
        Deque<Integer> stack=new LinkedList<>();

        for(String token : tokens){
            char c=token.charAt(0);

            if(!isOperation(token)){
                stack.push(valueOf(token));
            }else if(c == '+'){
                stack.push(stack.pop() + stack.pop());
            }else if(c == '-'){
                stack.push(-stack.pop() + stack.pop());
            }else if(c == '*'){
                stack.push(stack.pop() * stack.pop());
            }else{
                int num1=stack.pop();
                int num2=stack.pop();
                stack.push(num2/num1);
            }
        }

        return stack.pop();
    }

    private boolean isOperation(String s){
        return s.length() == 1 && (s.charAt(0) < '0' || s.charAt(0) >'9');
    }


    private int valueOf(String s){
        return Integer.valueOf(s);
    }
}
```



### [500. 键盘行](https://leetcode-cn.com/problems/keyboard-row/)

给你一个字符串数组 words ，只返回可以使用在 美式键盘 同一行的字母打印出来的单词。键盘如下图所示。

美式键盘 中：

第一行由字符 "qwertyuiop" 组成。
第二行由字符 "asdfghjkl" 组成。
第三行由字符 "zxcvbnm" 组成。

```java
class Solution {
    static String[] ss = new String[]{"qwertyuiop", "asdfghjkl", "zxcvbnm"};
    static int[] hash = new int[26];
    static {
        for (int i = 0; i < ss.length; i++) {
            for (char c : ss[i].toCharArray()) hash[c - 'a'] = i;
        }
    }
    public String[] findWords(String[] words) {
        List<String> list = new ArrayList<>();
        out:for (String w : words) {
            int t = -1;
            for (char c : w.toCharArray()) {
                c = Character.toLowerCase(c);
                //找到该单词的第一个字母,决定了该单词只能存在的行的位置
                if (t == -1) t = hash[c - 'a'];
                else if (t != hash[c - 'a']) continue out;
            }
            list.add(w);
        }
        return list.toArray(new String[list.size()]);
    }
}

```







### [705. 设计哈希集合](https://leetcode-cn.com/problems/design-hashset/)

不使用任何内建的哈希表库设计一个哈希集合（HashSet）。

实现 MyHashSet 类：

void add(key) 向哈希集合中插入值 key 。
bool contains(key) 返回哈希集合中是否存在这个值 key 。
void remove(key) 将给定值 key 从哈希集合中删除。如果哈希集合中没有这个值，什么也不做。

```java
//解法1
class MyHashSet {
    Node[] nodes=new Node[10009];
  
    public void add(int key) {
        int index=getIndex(key);
        Node node=nodes[index];
        Node pre=node;
        //如果数组上该点已存在
        while(node != null){
            if(node.key == key){return;}
            pre=node;
            node=node.next;
        }

        if(pre == null){
            nodes[index]=new Node(key);
        }else{
            pre.next=new Node(key);
        }
    }
    
    public void remove(int key) {
        int index=getIndex(key);
        Node node=nodes[index];
        Node pre=null;
        while(node != null){
            if(node.key == key){
                if(pre == null){
                    //此处必须 指定 nodes[index]
                    nodes[index]=node.next;
                }else{
                    pre.next=node.next;
                }
                return;
            }
            pre=node;
            node=node.next;
        }
    }

    public boolean contains(int key) {
        int index=getIndex(key);
        Node node=nodes[index];

        while(node != null){
            if(node.key == key){return true;}
            node=node.next;
        }

        return false;
    }


    public int getIndex(int key){
        int hash=Integer.hashCode(key);
        // 因为 nodes 的长度只有 10009，对应的十进制的 10011100011001（总长度为 32 位，其余高位都是 0）
        // 为了让 key 对应的 hash 高位也参与运算，这里对 hashCode 进行右移异或
        // 使得 hashCode 的高位随机性和低位随机性都能体现在低 16 位中
        hash=hash^(hash>>>16);
        return hash&(nodes.length-1);
    }
    static class Node{
        private int key;
        private Node next;
        public Node(int key){
            this.key=key;
        }
    }
}

/**
 * Your MyHashSet object will be instantiated and called as such:
 * MyHashSet obj = new MyHashSet();
 * obj.add(key);
 * obj.remove(key);
 * boolean param_3 = obj.contains(key);
 */
```

```java
//解法2 仿照 bitmap
class MyHashSet {
    int[] bs = new int[40000];
    public void add(int key) {
        int bucketIdx = key / 32;
        int bitIdx = key % 32;
        setVal(bucketIdx, bitIdx, true);
    }
    
    public void remove(int key) {
        int bucketIdx = key / 32;
        int bitIdx = key % 32;
        setVal(bucketIdx, bitIdx, false);
    }
    
    public boolean contains(int key) {
        int bucketIdx = key / 32;
        int bitIdx = key % 32;
        return getVal(bucketIdx, bitIdx);
    }

    void setVal(int bucket, int loc, boolean val) {
        if (val) {
            int u = bs[bucket] | (1 << loc);
            bs[bucket] = u;
        } else {
            int u = bs[bucket] & ~(1 << loc);
            bs[bucket] = u;
        }
    }

    boolean getVal(int bucket, int loc) {
        int u = (bs[bucket] >> loc) & 1;
        return u == 1;
    }
}
```





### [706. 设计哈希映射](https://leetcode-cn.com/problems/design-hashmap/)

不使用任何内建的哈希表库设计一个哈希映射（HashMap）。

实现 MyHashMap 类：

MyHashMap() 用空映射初始化对象
void put(int key, int value) 向 HashMap 插入一个键值对 (key, value) 。如果 key 已经存在于映射中，则更新其对应的值 value 。
int get(int key) 返回特定的 key 所映射的 value ；如果映射中不包含 key 的映射，返回 -1 。
void remove(key) 如果映射中存在 key 的映射，则移除 key 和它所对应的 value 。

```java
class MyHashMap {
    Node[] nodes=new Node[10009];

    static class Node{
        int key;
        int value;
        Node next;
        public Node(int _key,int _value){
            key=_key;
            value=_value;
        }
    }

    
    public void put(int key, int value) {
        int index=getIndex(key);
        Node node=nodes[index];

        Node pre=null;

        while(node != null){
            if(node.key == key){
                node.value=value;
                return;
            }
            pre=node;
            node=node.next;
        }

        Node r=new Node(key,value);

        //这里别写成 if(node == null)因为这样无法区分 node 一开始到底是不是null
        if(pre == null){
            nodes[index]=r;
        }else{
            pre.next=r;
        }
    }
        
    public int get(int key) {
        int index=getIndex(key);

        Node node=nodes[index];

        while(node != null){
            if(node.key == key){
                return node.value;
            }
            node=node.next;
        }
        return -1;
    }

    public void remove(int key) {
        int index=getIndex(key);

        Node node=nodes[index];
        Node pre=null;
        while(node != null){
            if(node.key == key){
                if(pre == null){

                    //这地方注意别写成 node=node.next,注意引用对象
                    nodes[index]=node.next;
                    
                }else{
                    pre.next=node.next;
                }
                return;
            }

            pre=node;
            node=node.next;
        }
    }

    private int getIndex(int key){
        int hash=Integer.hashCode(key);
        hash=hash ^ (hash >>> 16);
        return hash & (nodes.length-1);
    }
}

/**
 * Your MyHashMap object will be instantiated and called as such:
 * MyHashMap obj = new MyHashMap();
 * obj.put(key,value);
 * int param_2 = obj.get(key);
 * obj.remove(key);
 */
```



---







## 动态规划



#### [5. 最长回文子串](https://leetcode-cn.com/problems/longest-palindromic-substring/)

给你一个字符串 `s`，找到 `s` 中最长的回文子串。

```java
public class Solution {

    public String longestPalindrome(String s) {
        int len = s.length();
        if (len < 2) {
            return s;
        }

        int maxLen = 1;
        int begin = 0;
        // dp[i][j] 表示 s[i..j] 是否是回文串
        boolean[][] dp = new boolean[len][len];
        // 初始化：所有长度为 1 的子串都是回文串
        for (int i = 0; i < len; i++) {
            dp[i][i] = true;
        }

        char[] charArray = s.toCharArray();
        // 递推开始
        // 先枚举子串长度
        for (int L = 2; L <= len; L++) {
            // 枚举左边界，左边界的上限设置可以宽松一些
            for (int i = 0; i < len; i++) {
                // 由 L 和 i 可以确定右边界，即 j - i + 1 = L 得
                int j = L + i - 1;
                // 如果右边界越界，就可以退出当前循环
                if (j >= len) {
                    break;
                }

                if (charArray[i] != charArray[j]) {
                    dp[i][j] = false;
                } else {

                    //因为 aba 0~2,只要首位相同就行
                    if (j - i < 3) {
                        dp[i][j] = true;
                    } else {
                        //如果一个字符串的两端字母相同,那么只要(left,right)是true,[left,right]就是回文
                        dp[i][j] = dp[i + 1][j - 1];
                    }
                }

                // 只要 dp[i][L] == true 成立，就表示子串 s[i..L] 是回文，此时记录回文长度和起始位置
                if (dp[i][j] && j - i + 1 > maxLen) {
                    maxLen = j - i + 1;
                    begin = i;
                }
            }
        }
        return s.substring(begin, begin + maxLen);
    }
}
```



#### [343. 整数拆分](https://leetcode-cn.com/problems/integer-break/)

给定一个正整数 *n*，将其拆分为**至少**两个正整数的和，并使这些整数的乘积最大化。 返回你可以获得的最大乘积

```java
class Solution {
    public int integerBreak(int n) {
        //为什么 2~3都要单独列出来呢? 因为>=4的时候切开后的乘积都能大于 当前数, <=3的时候都是小于当前数,所以担心绳子本身的长度<=3,造成结果错误,如果绳子长度大于3,那<=3的部分就可以不切,这样取得的值最大
        if(n==3) return 2;
        if(n==2) return 1; 
        int[] dp = new int[n+1];
		for(int i=1;i<=n;i++){ //base case
            dp[i]=i;
        }
		for(int i=2;i<=n;i++) {//状态
			for(int j=1;j<i;j++) {//选择
				//if(i+j<=n)
				dp[i]=Math.max(dp[i-j]*dp[j], dp[i]);
			}
		}
		return dp[n];
    }
}

// 作者：priate1
// 链接：https://leetcode-cn.com/problems/integer-break/solution/java-dong-tai-gui-hua-xin-shou-you-hao-h-dqll/
// 来源：力扣（LeetCode）
// 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。


class Solution {
    public int integerBreak(int n) {
        if(n <= 3) return n - 1;
        int a = n / 3, b = n % 3;
        if(b == 0) return (int)Math.pow(3, a);
        if(b == 1) return (int)Math.pow(3, a - 1) * 4;
        return (int)Math.pow(3, a) * 2;
    }
}

作者：jyd
链接：https://leetcode-cn.com/problems/integer-break/solution/343-zheng-shu-chai-fen-tan-xin-by-jyd/
来源：力扣（LeetCode）
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
```



#### [746. 使用最小花费爬楼梯](https://leetcode-cn.com/problems/min-cost-climbing-stairs/)

数组的每个下标作为一个阶梯，第 i 个阶梯对应着一个非负数的体力花费值 cost[i]（下标从 0 开始）。

每当你爬上一个阶梯你都要花费对应的体力值，一旦支付了相应的体力值，你就可以选择向上爬一个阶梯或者爬两个阶梯。

请你找出达到楼层顶部的最低花费。在开始时，你可以选择从下标为 0 或 1 的元素作为初始阶梯。

 

```java
//我自己的思路
class Solution {
    public int minCostClimbingStairs(int[] cost) {
        //到达终点所花费的体力值,从最小开始模拟
        int[] record =new int[cost.length+1];

        int a = cost[0], b = cost[1];

        //最后是 record[n]+ cost[n] and record[n-1] + cost[n-1] compare

        for(int i = 2; i <= cost.length; i++){
            record[i] = Math.min(record[i-1] + cost[i-1],record[i-2] + cost[i-2]);
        }

        return record[record.length-1];
    }
}
```



http://8.129.34.193:8080/ProjectEnding/

---



## 2021-10-10

> [467. 环绕字符串中唯一的子字符串](https://leetcode-cn.com/problems/unique-substrings-in-wraparound-string/)

> [剑指 Offer 29. 顺时针打印矩阵](https://leetcode-cn.com/problems/shun-shi-zhen-da-yin-ju-zhen-lcof/)

>[209. 长度最小的子数组](https://leetcode-cn.com/problems/minimum-size-subarray-sum/)

>[34. 在排序数组中查找元素的第一个和最后一个位置](https://leetcode-cn.com/problems/find-first-and-last-position-of-element-in-sorted-array/)



---



## 2021-10-11

[467. 环绕字符串中唯一的子字符串](https://leetcode-cn.com/problems/unique-substrings-in-wraparound-string/)

 [35. 搜索插入位置](https://leetcode-cn.com/problems/search-insert-position/)

 [69. Sqrt(x)](https://leetcode-cn.com/problems/sqrtx/)



---



## 2021-10-12

 [27. 移除元素](https://leetcode-cn.com/problems/remove-element/)



## 2021-10-13

[26. 删除有序数组中的重复项](https://leetcode-cn.com/problems/remove-duplicates-from-sorted-array/)

 [283. 移动零](https://leetcode-cn.com/problems/move-zeroes/)

[844. 比较含退格的字符串](https://leetcode-cn.com/problems/backspace-string-compare/)



---



## 2021-10-14

[26. 删除有序数组中的重复项](https://leetcode-cn.com/problems/remove-duplicates-from-sorted-array/)

[199. 二叉树的右视图](https://leetcode-cn.com/problems/binary-tree-right-side-view/)



---



## 2021-10-16

[116. 填充每个节点的下一个右侧节点指针](https://leetcode-cn.com/problems/populating-next-right-pointers-in-each-node/)

[111. 二叉树的最小深度](https://leetcode-cn.com/problems/minimum-depth-of-binary-tree/)

[226. 翻转二叉树](https://leetcode-cn.com/problems/invert-binary-tree/)

[101. 对称二叉树](https://leetcode-cn.com/problems/symmetric-tree/)



---



## 2021-10-17

[222. 完全二叉树的节点个数](https://leetcode-cn.com/problems/count-complete-tree-nodes/)

[110. 平衡二叉树](https://leetcode-cn.com/problems/balanced-binary-tree/)

[257. 二叉树的所有路径](https://leetcode-cn.com/problems/binary-tree-paths/)



## 2021-10-18

[404. 左叶子之和](https://leetcode-cn.com/problems/sum-of-left-leaves/)

[222. 完全二叉树的节点个数](https://leetcode-cn.com/problems/count-complete-tree-nodes/)

[513. 找树左下角的值](https://leetcode-cn.com/problems/find-bottom-left-tree-value/)



## 2021-10-19

[106. 从中序与后序遍历序列构造二叉树](https://leetcode-cn.com/problems/construct-binary-tree-from-inorder-and-postorder-traversal/)

[654. 最大二叉树](https://leetcode-cn.com/problems/maximum-binary-tree/)

[617. 合并二叉树](https://leetcode-cn.com/problems/merge-two-binary-trees/)



## 2021-10-20

[98. 验证二叉搜索树](https://leetcode-cn.com/problems/validate-binary-search-tree/)





## 2021-10-21

[66. 加一](https://leetcode-cn.com/problems/plus-one/)



## 2021-10-22

[229. 求众数 II](https://leetcode-cn.com/problems/majority-element-ii/)



## 2021-10-24

[66. 加一](https://leetcode-cn.com/problems/plus-one/)

[98. 验证二叉搜索树](https://leetcode-cn.com/problems/validate-binary-search-tree/)

[236. 二叉树的最近公共祖先](https://leetcode-cn.com/problems/lowest-common-ancestor-of-a-binary-tree/)



## 2021-10-27

[235. 二叉搜索树的最近公共祖先](https://leetcode-cn.com/problems/lowest-common-ancestor-of-a-binary-search-tree/)

[783. 二叉搜索树节点最小距离](https://leetcode-cn.com/problems/minimum-distance-between-bst-nodes/)(学习 pre的用法)

[701. 二叉搜索树中的插入操作](https://leetcode-cn.com/problems/insert-into-a-binary-search-tree/)(写得更简洁优美)

[450. 删除二叉搜索树中的节点](https://leetcode-cn.com/problems/delete-node-in-a-bst/)(非常难,尽量会多种写法吧)



## 2021-10-28

[9. 回文数](https://leetcode-cn.com/problems/palindrome-number/)



## 2021-10-31

[501. 二叉搜索树中的众数](https://leetcode-cn.com/problems/find-mode-in-binary-search-tree/)

[669. 修剪二叉搜索树](https://leetcode-cn.com/problems/trim-a-binary-search-tree/)





## 2021-11-1

[500. 键盘行](https://leetcode-cn.com/problems/keyboard-row/)

[671. 二叉树中第二小的节点](https://leetcode-cn.com/problems/second-minimum-node-in-a-binary-tree/)





## 2021-11-2

[455. 分发饼干](https://leetcode-cn.com/problems/assign-cookies/)

[376. 摆动序列](https://leetcode-cn.com/problems/wiggle-subsequence/)

[121. 买卖股票的最佳时机](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock/)





## 2021-11-3

[55. 跳跃游戏](https://leetcode-cn.com/problems/jump-game/)

[45. 跳跃游戏 II](https://leetcode-cn.com/problems/jump-game-ii/)

[376. 摆动序列](https://leetcode-cn.com/problems/wiggle-subsequence/)

[705. 设计哈希集合](https://leetcode-cn.com/problems/design-hashset/)

[500. 键盘行](https://leetcode-cn.com/problems/keyboard-row/)



## 2021-11-4

[706. 设计哈希映射](https://leetcode-cn.com/problems/design-hashmap/)

[367. 有效的完全平方数](https://leetcode-cn.com/problems/valid-perfect-square/)

[1005. K 次取反后最大化的数组和](https://leetcode-cn.com/problems/maximize-sum-of-array-after-k-negations/)

[134. 加油站](https://leetcode-cn.com/problems/gas-station/)

[135. 分发糖果](https://leetcode-cn.com/problems/candy/)

[860. 柠檬水找零](https://leetcode-cn.com/problems/lemonade-change/)

[406. 根据身高重建队列](https://leetcode-cn.com/problems/queue-reconstruction-by-height/)





## 2021-11-5

[452. 用最少数量的箭引爆气球](https://leetcode-cn.com/problems/minimum-number-of-arrows-to-burst-balloons/)

[763. 划分字母区间](https://leetcode-cn.com/problems/partition-labels/)

[56. 合并区间](https://leetcode-cn.com/problems/merge-intervals/)





## 2021-11-6

[738. 单调递增的数字](https://leetcode-cn.com/problems/monotone-increasing-digits/)

[714. 买卖股票的最佳时机含手续费](https://leetcode-cn.com/problems/best-time-to-buy-and-sell-stock-with-transaction-fee/)(第一次交易的时候就提前把服务费算在后一个节点中),来达到后续交易无需服务费,只是我的猜想

[225. 用队列实现栈](https://leetcode-cn.com/problems/implement-stack-using-queues/)

[232. 用栈实现队列](https://leetcode-cn.com/problems/implement-queue-using-stacks/)

[20. 有效的括号](https://leetcode-cn.com/problems/valid-parentheses/)

[1047. 删除字符串中的所有相邻重复项](https://leetcode-cn.com/problems/remove-all-adjacent-duplicates-in-string/)(多种解法)

[150. 逆波兰表达式求值](https://leetcode-cn.com/problems/evaluate-reverse-polish-notation/)

[239. 滑动窗口最大值](https://leetcode-cn.com/problems/sliding-window-maximum/)

[347. 前 K 个高频元素](https://leetcode-cn.com/problems/top-k-frequent-elements/)





## 2021-11-7

[242. 有效的字母异位词](https://leetcode-cn.com/problems/valid-anagram/)(直接比较每个位置上的字母是否一样 或者 选择每个字母个数是否一样)

[349. 两个数组的交集](https://leetcode-cn.com/problems/intersection-of-two-arrays/)

[202. 快乐数](https://leetcode-cn.com/problems/happy-number/)

[598. 范围求和 II](https://leetcode-cn.com/problems/range-addition-ii/)

[454. 四数相加 II](https://leetcode-cn.com/problems/4sum-ii/)

[15. 三数之和](https://leetcode-cn.com/problems/3sum/)

[18. 四数之和](https://leetcode-cn.com/problems/4sum/)





## 2021-11-8

复习前面3天的内容 (5,6,7)

[14. 最长公共前缀](https://leetcode-cn.com/problems/longest-common-prefix/)

[299. 猜数字游戏](https://leetcode-cn.com/problems/bulls-and-cows/)





## 2021-11-9

[151. 翻转字符串里的单词](https://leetcode-cn.com/problems/reverse-words-in-a-string/)(多种题解)

543 124 687 都差不多

[543. 二叉树的直径](https://leetcode-cn.com/problems/diameter-of-binary-tree/)

[124. 二叉树中的最大路径和](https://leetcode-cn.com/problems/binary-tree-maximum-path-sum/)

[687. 最长同值路径](https://leetcode-cn.com/problems/longest-univalue-path/)



## 2021-11-10

[739. 每日温度](https://leetcode-cn.com/problems/daily-temperatures/)

[496. 下一个更大元素 I](https://leetcode-cn.com/problems/next-greater-element-i/)

[503. 下一个更大元素 II](https://leetcode-cn.com/problems/next-greater-element-ii/)

[495. 提莫攻击](https://leetcode-cn.com/problems/teemo-attacking/)



## 2021-11-11

[77. 组合](https://leetcode-cn.com/problems/combinations/)

[17. 电话号码的字母组合](https://leetcode-cn.com/problems/letter-combinations-of-a-phone-number/)



## 2021-11-12

[39. 组合总和](https://leetcode-cn.com/problems/combination-sum/)(注意下模板就行)

[40. 组合总和 II](https://leetcode-cn.com/problems/combination-sum-ii/)(这题先排序,然后同一层树不允许出现相同元素,如 1 1 2,这样必会造成重复,1 2 就可以列出所有可能)

[131. 分割回文串](https://leetcode-cn.com/problems/palindrome-partitioning/)



## 2021-11-13

[93. 复原 IP 地址](https://leetcode-cn.com/problems/restore-ip-addresses/)

[78. 子集](https://leetcode-cn.com/problems/subsets/)(亲麻烦把眼睛擦亮再做题好吗? 把i写成startIdx,卡了10分钟)

[90. 子集 II](https://leetcode-cn.com/problems/subsets-ii/)

[46. 全排列](https://leetcode-cn.com/problems/permutations/)





## 2021-11-14

[47. 全排列 II](https://leetcode-cn.com/problems/permutations-ii/)

[5927. 反转偶数长度组的节点](https://leetcode-cn.com/problems/reverse-nodes-in-even-length-groups/)

[42. 接雨水](https://leetcode-cn.com/problems/trapping-rain-water/)





## 2021-11-15

[496. 下一个更大元素 I](https://leetcode-cn.com/problems/next-greater-element-i/)





## 2021-11-16

[746. 使用最小花费爬楼梯](https://leetcode-cn.com/problems/min-cost-climbing-stairs/)

[62. 不同路径](https://leetcode-cn.com/problems/unique-paths/)







## 2021-11-17

[203. 移除链表元素](https://leetcode-cn.com/problems/remove-linked-list-elements/)

[707. 设计链表](https://leetcode-cn.com/problems/design-linked-list/)

[206. 反转链表](https://leetcode-cn.com/problems/reverse-linked-list/)





## 2021-11-18

思路不够流畅:

[203. 移除链表元素](https://leetcode-cn.com/problems/remove-linked-list-elements/)

[707. 设计链表](https://leetcode-cn.com/problems/design-linked-list/)



不会做:

[19. 删除链表的倒数第 N 个结点](https://leetcode-cn.com/problems/remove-nth-node-from-end-of-list/)

[面试题 02.07. 链表相交](https://leetcode-cn.com/problems/intersection-of-two-linked-lists-lcci/)

[142. 环形链表 II](https://leetcode-cn.com/problems/linked-list-cycle-ii/)



## 2021-11-19

不会做:

[454. 四数相加 II](https://leetcode-cn.com/problems/4sum-ii/)

[15. 三数之和](https://leetcode-cn.com/problems/3sum/)

[18. 四数之和](https://leetcode-cn.com/problems/4sum/)

[343. 整数拆分](https://leetcode-cn.com/problems/integer-break/)



扩展思路:

[剑指 Offer 03. 数组中重复的数字](https://leetcode-cn.com/problems/shu-zu-zhong-zhong-fu-de-shu-zi-lcof/)(原地交换)







## 2021-11-20

不会做:

[594. 最长和谐子序列](https://leetcode-cn.com/problems/longest-harmonious-subsequence/)

[34. 在排序数组中查找元素的第一个和最后一个位置](https://leetcode-cn.com/problems/find-first-and-last-position-of-element-in-sorted-array/)

思路不清晰:

[15. 三数之和](https://leetcode-cn.com/problems/3sum/)





## 2021-11-21

不会做:

[594. 最长和谐子序列](https://leetcode-cn.com/problems/longest-harmonious-subsequence/)

[34. 在排序数组中查找元素的第一个和最后一个位置](https://leetcode-cn.com/problems/find-first-and-last-position-of-element-in-sorted-array/)

[541. 反转字符串 II](https://leetcode-cn.com/problems/reverse-string-ii/)

[剑指 Offer 05. 替换空格](https://leetcode-cn.com/problems/ti-huan-kong-ge-lcof/)

[151. 翻转字符串里的单词](https://leetcode-cn.com/problems/reverse-words-in-a-string/)

[28. 实现 strStr()](https://leetcode-cn.com/problems/implement-strstr/)(简单题藏着KMP偷袭我这老同志)

扩充思路:

[559. N 叉树的最大深度](https://leetcode-cn.com/problems/maximum-depth-of-n-ary-tree/)

[剑指 Offer 58 - II. 左旋转字符串](https://leetcode-cn.com/problems/zuo-xuan-zhuan-zi-fu-chuan-lcof/)(API熟悉多种就行)





## 2021-11-22

思路不清晰:

[594. 最长和谐子序列](https://leetcode-cn.com/problems/longest-harmonious-subsequence/)



不会做:

[151. 翻转字符串里的单词](https://leetcode-cn.com/problems/reverse-words-in-a-string/)





## 2021-11-23

不会做:

[459. 重复的子字符串](https://leetcode-cn.com/problems/repeated-substring-pattern/)(旋转)

[77. 组合](https://leetcode-cn.com/problems/combinations/)

[216. 组合总和 III](https://leetcode-cn.com/problems/combination-sum-iii/)

[17. 电话号码的字母组合](https://leetcode-cn.com/problems/letter-combinations-of-a-phone-number/)

[40. 组合总和 II](https://leetcode-cn.com/problems/combination-sum-ii/)

[131. 分割回文串](https://leetcode-cn.com/problems/palindrome-partitioning/)







## 2021-11-24

不会:

[93. 复原 IP 地址](https://leetcode-cn.com/problems/restore-ip-addresses/)

[131. 分割回文串](https://leetcode-cn.com/problems/palindrome-partitioning/)





思路不清晰:

[77. 组合](https://leetcode-cn.com/problems/combinations/)

[17. 电话号码的字母组合](https://leetcode-cn.com/problems/letter-combinations-of-a-phone-number/)

[232. 用栈实现队列](https://leetcode-cn.com/problems/implement-queue-using-stacks/)(可优化)

[225. 用队列实现栈](https://leetcode-cn.com/problems/implement-stack-using-queues/)(优化)

[20. 有效的括号](https://leetcode-cn.com/problems/valid-parentheses/)





## 2021-11-25

不会做:

[28. 实现 strStr()](https://leetcode-cn.com/problems/implement-strstr/)(KMP , 滑动窗口)

[93. 复原 IP 地址](https://leetcode-cn.com/problems/restore-ip-addresses/)

[剑指 Offer 13. 机器人的运动范围](https://leetcode-cn.com/problems/ji-qi-ren-de-yun-dong-fan-wei-lcof/)

[343. 整数拆分](https://leetcode-cn.com/problems/integer-break/)(掌握动态规划 , 直接用数学结论)

[剑指 Offer 14- I. 剪绳子](https://leetcode-cn.com/problems/jian-sheng-zi-lcof/)(优化)

[剑指 Offer 14- II. 剪绳子 II](https://leetcode-cn.com/problems/jian-sheng-zi-ii-lcof/)(贪心)

[剑指 Offer 15. 二进制中1的个数](https://leetcode-cn.com/problems/er-jin-zhi-zhong-1de-ge-shu-lcof/)(两种做法)





## 2021-11-27

不会做:

[93. 复原 IP 地址](https://leetcode-cn.com/problems/restore-ip-addresses/)

[28. 实现 strStr()](https://leetcode-cn.com/problems/implement-strstr/)(KMP , 滑动窗口)

[剑指 Offer 14- II. 剪绳子 II](https://leetcode-cn.com/problems/jian-sheng-zi-ii-lcof/)(贪心)

[1047. 删除字符串中的所有相邻重复项](https://leetcode-cn.com/problems/remove-all-adjacent-duplicates-in-string/)

[150. 逆波兰表达式求值](https://leetcode-cn.com/problems/evaluate-reverse-polish-notation/)

[239. 滑动窗口最大值](https://leetcode-cn.com/problems/sliding-window-maximum/)



思路不清晰:

[剑指 Offer 13. 机器人的运动范围](https://leetcode-cn.com/problems/ji-qi-ren-de-yun-dong-fan-wei-lcof/)

[343. 整数拆分](https://leetcode-cn.com/problems/integer-break/)(掌握动态规划 , 直接用数学结论)

[剑指 Offer 14- I. 剪绳子](https://leetcode-cn.com/problems/jian-sheng-zi-lcof/)(动态规划)

[20. 有效的括号](https://leetcode-cn.com/problems/valid-parentheses/)(学着怎么优雅点)







## 2021-11-28

不会做:

[93. 复原 IP 地址](https://leetcode-cn.com/problems/restore-ip-addresses/)(已经无语了,今天做明天忘,无限循环)



思路不清晰:

[239. 滑动窗口最大值](https://leetcode-cn.com/problems/sliding-window-maximum/)



## 2021-11-29

[438. 找到字符串中所有字母异位词](https://leetcode-cn.com/problems/find-all-anagrams-in-a-string/)



## 2021-11-30

[5. 最长回文子串](https://leetcode-cn.com/problems/longest-palindromic-substring/)

[438. 找到字符串中所有字母异位词](https://leetcode-cn.com/problems/find-all-anagrams-in-a-string/)

[144. 二叉树的前序遍历](https://leetcode-cn.com/problems/binary-tree-preorder-traversal/)(迭代法做,别递归,还有一个Mirrors方法,理解一下) &&  [114. 二叉树展开为链表](https://leetcode-cn.com/problems/flatten-binary-tree-to-linked-list/)

[31. 下一个排列](https://leetcode-cn.com/problems/next-permutation/)

[49. 字母异位词分组](https://leetcode-cn.com/problems/group-anagrams/)





## 2021-12-1

[剑指 Offer 64. 求1+2+…+n](https://leetcode-cn.com/problems/qiu-12n-lcof/)

[22. 括号生成](https://leetcode-cn.com/problems/generate-parentheses/)

[1446. 连续字符](https://leetcode-cn.com/problems/consecutive-characters/)

[5. 最长回文子串](https://leetcode-cn.com/problems/longest-palindromic-substring/)

[93. 复原 IP 地址](https://leetcode-cn.com/problems/restore-ip-addresses/)





## 2021-12-2

[506. 相对名次](https://leetcode-cn.com/problems/relative-ranks/)(不熟悉)



## 2021-12-3

[剑指 Offer 14- II. 剪绳子 II](https://leetcode-cn.com/problems/jian-sheng-zi-ii-lcof/)

[剑指 Offer 38. 字符串的排列](https://leetcode-cn.com/problems/zi-fu-chuan-de-pai-lie-lcof/)

[1005. K 次取反后最大化的数组和](https://leetcode-cn.com/problems/maximize-sum-of-array-after-k-negations/)



## 2021-12-4

不会做:

[79. 单词搜索](https://leetcode-cn.com/problems/word-search/)

[13. 罗马数字转整数](https://leetcode-cn.com/problems/roman-to-integer/)

[4. 寻找两个正序数组的中位数](https://leetcode-cn.com/problems/median-of-two-sorted-arrays/)

[136. 只出现一次的数字](https://leetcode-cn.com/problems/single-number/)

[剑指 Offer 56 - I. 数组中数字出现的次数](https://leetcode-cn.com/problems/shu-zu-zhong-shu-zi-chu-xian-de-ci-shu-lcof/)



思路需要纠正一点:

[剑指 Offer 38. 字符串的排列](https://leetcode-cn.com/problems/zi-fu-chuan-de-pai-lie-lcof/)

[1005. K 次取反后最大化的数组和](https://leetcode-cn.com/problems/maximize-sum-of-array-after-k-negations/)



## 2021-12-5

[79. 单词搜索](https://leetcode-cn.com/problems/word-search/)

[剑指 Offer 62. 圆圈中最后剩下的数字](https://leetcode-cn.com/problems/yuan-quan-zhong-zui-hou-sheng-xia-de-shu-zi-lcof/)





思路不清晰:

[剑指 Offer 56 - I. 数组中数字出现的次数](https://leetcode-cn.com/problems/shu-zu-zhong-shu-zi-chu-xian-de-ci-shu-lcof/)

[13. 罗马数字转整数](https://leetcode-cn.com/problems/roman-to-integer/)



挑战题:

[4. 寻找两个正序数组的中位数](https://leetcode-cn.com/problems/median-of-two-sorted-arrays/)





## 2021-12-6

[50. Pow(x, n)](https://leetcode-cn.com/problems/powx-n/)

[剑指 Offer 57 - II. 和为s的连续正数序列](https://leetcode-cn.com/problems/he-wei-sde-lian-xu-zheng-shu-xu-lie-lcof/)

[剑指 Offer 45. 把数组排成最小的数](https://leetcode-cn.com/problems/ba-shu-zu-pai-cheng-zui-xiao-de-shu-lcof/)

[372. 超级次方](https://leetcode-cn.com/problems/super-pow/)





思路不清晰:

[剑指 Offer 16. 数值的整数次方](https://leetcode-cn.com/problems/shu-zhi-de-zheng-shu-ci-fang-lcof/)(两种)





## 2021-12-7

[剑指 Offer 59 - I. 滑动窗口的最大值](https://leetcode-cn.com/problems/hua-dong-chuang-kou-de-zui-da-zhi-lcof/)

[450. 删除二叉搜索树中的节点](https://leetcode-cn.com/problems/delete-node-in-a-bst/)



## 2021-12-8

[450. 删除二叉搜索树中的节点](https://leetcode-cn.com/problems/delete-node-in-a-bst/)

[剑指 Offer 59 - II. 队列的最大值](https://leetcode-cn.com/problems/dui-lie-de-zui-da-zhi-lcof/)

[剑指 Offer 45. 把数组排成最小的数](https://leetcode-cn.com/problems/ba-shu-zu-pai-cheng-zui-xiao-de-shu-lcof/)



试一下:

[剑指 Offer 48. 最长不含重复字符的子字符串](https://leetcode-cn.com/problems/zui-chang-bu-han-zhong-fu-zi-fu-de-zi-zi-fu-chuan-lcof/)

[剑指 Offer 39. 数组中出现次数超过一半的数字](https://leetcode-cn.com/problems/shu-zu-zhong-chu-xian-ci-shu-chao-guo-yi-ban-de-shu-zi-lcof/)(摩尔排序)



## 2021-12-9

[剑指 Offer 49. 丑数](https://leetcode-cn.com/problems/chou-shu-lcof/)

[剑指 Offer 44. 数字序列中某一位的数字](https://leetcode-cn.com/problems/shu-zi-xu-lie-zhong-mou-yi-wei-de-shu-zi-lcof/)

[448. 找到所有数组中消失的数字](https://leetcode-cn.com/problems/find-all-numbers-disappeared-in-an-array/)

[剑指 Offer 66. 构建乘积数组](https://leetcode-cn.com/problems/gou-jian-cheng-ji-shu-zu-lcof/)

[剑指 Offer 60. n个骰子的点数](https://leetcode-cn.com/problems/nge-tou-zi-de-dian-shu-lcof/)





## 2021-12-10

[748. 最短补全词](https://leetcode-cn.com/problems/shortest-completing-word/)



## 2021-12-11

[141. 环形链表](https://leetcode-cn.com/problems/linked-list-cycle/)

[一文搞定常见的链表问题 (欢迎交流 - 环形链表 - 力扣（LeetCode） (leetcode-cn.com)](https://leetcode-cn.com/problems/linked-list-cycle/solution/yi-wen-gao-ding-chang-jian-de-lian-biao-wen-ti-h-2/)





## 2021-12-18

[2099. 找到和最大的长度为 K 的子序列](https://leetcode-cn.com/problems/find-subsequence-of-length-k-with-the-largest-sum/)



## 2021-12-19

[2100. 适合打劫银行的日子](https://leetcode-cn.com/problems/find-good-days-to-rob-the-bank/)
