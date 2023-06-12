package tools;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class TF_IDF
{
    /**
     * 计算每个文档的tf值
     * @param word
     * @return Map<String,Float> key是单词 value是tf值
     */
    public static HashMap<String,Integer> tfCalculate(String word,HashMap<String, Integer> dict)
    {
        /**
         * 统计每个单词的数量，并存放到map中去
         */
            if(dict.containsKey(word))
                dict.put(word, dict.get(word)+1);
            else
                dict.put(word, 1);

        return dict;
    }

    //对统计完成的单词频率从大到小进行排序
    public static List sort(HashMap map)
    {
        //通过ArrayList构造函数将map.entrySet()转换成list
        List<Map.Entry<String,Integer>> list= new ArrayList<>(map.entrySet());
        //通过比较器进行比较排序
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare(Map.Entry<String, Integer> entry1, Map.Entry<String, Integer> entry2)
            {
                //返回值为1，代表前面>后面，-1相反，0表示相等
                return entry2.getValue().compareTo(entry1.getValue());
            }
        });
        return list;
    }

    /**
     *
     * @param D 总文档数
     * @param doc_words 每个文档对应的分词
     * @param tf 计算好的tf,用这个作为基础计算tfidf
     * @return 每个文档中的单词的tfidf的值
     * @throws IOException
     * @throws FileNotFoundException
     */



}
