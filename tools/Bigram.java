package tools;

import java.util.HashMap;

/**
 * Bigram向量转换方法，将单词转换为基于字母表的字符串向量（String BigramVector)
 */
public class Bigram
{

    public static HashMap<String,String> Alphabet=new HashMap<>();
    public Bigram()
    {
        /**
         * 构造字母对照表
         */
        char init1='a';

        for(int i=0;i<26;i++)
        {
            char init2='a';
            for(int j=0;j<26;j++)
            {
                Alphabet.put(String.valueOf(init1)+String.valueOf(init2),String.valueOf(i)+'@'+String.valueOf(j));
                init2+=1;
            }
            init1+=1;
        }
    }
    public static String Bigram(String word)
    {

        char[] letter=word.toCharArray();
        String BigramVector=new String();
        String[] index=new String[2];
        String[] buffer=new String[50];           //单词拆分存储缓冲区


        /**
         * 根据字母表生成单词向量
         */
        if(word.length()>1)             //单字母的单词剔除
        {
            //System.out.print(word + '#');
            for (int i=0; i<word.length()-1; i++)
            {
                if(Character.isLowerCase(letter[i])&&Character.isLowerCase(letter[i+1]))
                {
                    buffer[i] = String.valueOf(letter[i]) + String.valueOf(letter[i+1]);
                    //System.out.print(buffer[i] + ' ');
                    index = Alphabet.get(buffer[i]).split("@");
                    for (int j = 0; j < 2; j++) {
                        if (index[j].length() == 1)
                            index[j] = "0" + index[j];
                    }
                    BigramVector += index[0] + index[1];  //输出bigram转码后的向量
                }
            }
            //System.out.println(BigramVector);
        }
        return BigramVector;
    }

    public static String Decode(String vector)
    {
        String decode = new String();
        int n = (vector.length() +1) /2;  //获取整个字符串可以被切割成字符子串的个数
        int [] split = new int[n];
        for (int i = 0; i < n; i++)
        {
            if (i < (n - 1)) {
                split[i] = Integer.valueOf(vector.substring(i * 2, (i + 1) * 2));
            } else {
                split[i] = Integer.valueOf(vector.substring(i * 2));
            }

        }
        for(int i=0;i<n;i+=2)
        {
            char c1= (char) (split[i]+'a');
            char c2= (char) (split[i+1]+'a');

           if(i==0)
           {
               decode+=c1;
               decode+=c2;
           }

           else
               decode+=c2;
        }

        return decode;
    }


    /**
     * 功能测试用，可忽略
     * @param args
     */
    public static void main(String args[])
    {
        new Bigram();
        Decode("15040414141515111104");
    }
}
