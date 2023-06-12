package tools;

import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/** * 手动实现一个布隆过滤器，我们需要：
 * * 1. 一个大小合适的位数组保存数据
 * * 2. 几个不同的哈希函数
 * * 3. 添加元素到位数组（布隆过滤器）的方法实现
 * * 4. 判断给定元素是否存在于位数组（布隆过滤器）的方法实现
 * * * @author roger * @create 2021-06-28 15:12 */
public class BloomFilter {
    /* * 位数组的大小 * */
    public static final int DEFAULT_SIZE = 1000;
    /* * 通过这个数组可以创建6个不同的哈希函数 * */
    public static final int[] SEEDS = new int[]{
            3, 13, 46, 71, 91, 134};
    /* * 位数组，数组中的元素只能是0或者1 * */
    public static BitSet bits = new BitSet(DEFAULT_SIZE);
    /* * 存放包含hash函数的类的数组 * */
    private static SimpleHash[] func = new SimpleHash[SEEDS.length];
    /* * 初始化多个包含hash函数的类的数组，每个类中的hash函数都不一样 * */
    public static int[] hashcode = new int[SEEDS.length];

    public BloomFilter() {
        // 初始化多个不同的Hash函数
        for (int i = 0; i < SEEDS.length; i++)
            func[i] = new SimpleHash(DEFAULT_SIZE, SEEDS[i]);
    }

    /* * 添加元素到位数组 * */
    public static int[] add(String value) throws FileNotFoundException {

        int length = 2;
        LSHMinHash lsh = new LSHMinHash(6, DEFAULT_SIZE, 20);
        Set<Integer> vector = new HashSet<>(); //用set容器拆分向量

        int n = (value.length() + length - 1) / length; //获取整个字符串可以被切割成字符子串的个数
        int[] split = new int[n];
        for (int i = 0; i < n; i++) {
            if (i < (n - 1))
                split[i] = Integer.valueOf(value.substring(i * length, (i + 1) * length));
            else
                split[i] = Integer.valueOf(value.substring(i * length));

            vector.add(split[i]);

        }
        hashcode = lsh.hash(vector);

        for (int i = 0; i < hashcode.length; i++) {
            bits.set(hashcode[i], true);
        }

        return hashcode;
    }

    /* * 判断指定元素是否存在于位数组 * */
    public boolean contains(Object value) {
        boolean ret = true;
        for (int i = 0; i < hashcode.length; i++)
            ret = ret && bits.get(hashcode[i]);
        return ret;
    }

    /* * 静态内部类，用于hash操作！*/
    public static class SimpleHash {
        private int cap;
        private int seed;

        public SimpleHash(int cap, int seed) {
            this.cap = cap;
            this.seed = seed;
        }

        /* * 计算hash值 * */
        public int hash(Object value) {
            int h;

            return (value == null) ? 0 : Math.abs(seed * (cap - 1) & ((h = value.hashCode()) ^ (h >>> 16)));
        }
    }
}


