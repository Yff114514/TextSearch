package main;
import tools.Bigram;
import tools.BloomFilter;
import tools.RSA;
import tools.TF_IDF;
import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

public class Main
{
    /**
        0.基本全局参数
     **/
    static RSAPublicKey publicKey;                                          //存储RSA公钥
    static RSAPrivateKey privateKey;                                        //存储RSA私钥

    static String[] ArticlePath=new String[100];                            //存储文档的路径
    static String[] Article=new String[10000];                              //存储明文文档
    static String[][] EncodedArticle=new String[100][256];                  //存储加密后的文档内容

    static BloomFilter filter=new BloomFilter();                            //布隆过滤器

    static HashMap<String, int[]> keywords=new HashMap<>();                 //存放关键词向量-文档的倒排索引
    static HashMap<Integer, Set<String>> filterindex=new HashMap<>();       //存放布隆过滤器下标-关键词向量的索引

    static HashMap<String,Integer> StopWords =new HashMap<>();              // 停用词集合
    static Set<String> bloomlist=new HashSet<>();
    static List<Map.Entry<String,Integer>> keywordList=new ArrayList();    //存放关键词频率列表

    static int ArticleNum=100;                                              //文档数量
    static int top_k=200;                                                   //选取词频为前k的单词作为关键词，默认值为200



    /**
        1.密钥生成算法，使用RSA加密体制，KeyGen()方法生成RSA公钥和私钥，赋值给公钥和私钥全局变量publicKey/privateKey
     **/
    static void KeyGen() throws NoSuchAlgorithmException
    {
        HashMap<String, Object> h=RSA.getKeys();       //用一个hashmap容器存储getKeys()生成的密钥对
        publicKey=(RSAPublicKey) h.get("public");     //从容器获取生成的公钥
        privateKey=(RSAPrivateKey) h.get("private");  //从容器获取生成的私钥
    }

    /**
       2.文档加密算法,分为文件读取方法readTxt()、获取文章内容方法getArticle()和文件加密方法EncryptFile()
    **/
    //根据路径读取文件内容
    static String readTxt(String txtPath,int num)
    {
        File file = new File(txtPath);
        if(file.isFile() && file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String text;
                while((text = bufferedReader.readLine()) != null){
                    if(num==1)
                        sb.append(text);

                    if(num==2)
                        StopWords.put(text,0);
                }
                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //读取文档内容
    static void getArticle()
    {
        for(int i=0;i<ArticleNum;i++)
        {
            String x= String.valueOf((i+1));
            //获取文档路径
            ArticlePath[i]="./src/ArticleDemo/text"+x+".txt";
            //读取文档内容
            Article[i]=readTxt(ArticlePath[i],1);
        }
        readTxt("./src/data/StopWords.txt",2);

    }

    static String[][] EncryptFile(int num, String[] file) throws Exception {
        String[][] EncFile=new String[100][100];
        //统计字符串分割次数
        for(int i=0;i<num;i++)
        {
            int k=0;
            for(int j=0;j<file[i].length();j+=100)
            {
                if(j+99<file[i].length())
                    EncFile[i][k]=RSA.encryptByPublicKey(file[i].substring(j,j+99),publicKey);
                else EncFile[i][k]=RSA.encryptByPublicKey(file[i].substring(j),publicKey);
                k++;
            }
        }
        return EncFile;
    }


    /**
       3.安全索引构造算法，分为文章单词提取方法wordSplit()、关键词统计方法wordCount() 和索引构造方法indexBuild()
    **/
    static String[] wordSplit(String text) {
        String[] words=new String[1000];
        StringTokenizer st = new StringTokenizer(text, ", ，.。’\\\"!';”“_123456789-—()?");

        //按单词分割每篇文档并临时存储
        for(int i=0;st.hasMoreElements();i++)
        {
            String s=(String)st.nextElement();
            s=s.toLowerCase();
            words[i]=s;
        }
       return words;
    }

    //建立索引，分别建立关键词-文档倒排索引和布隆过滤器-关键词索引
    static void indexBuild() throws FileNotFoundException {
        String[] buffer=new String[1000];
        String[][] splitwords=new String[100][1000];
        HashMap<String,Integer> wordlist=new HashMap<>();
        new Bigram();

        for(int i=0;i<ArticleNum;i++)
        {
            int j=0;
            splitwords[i]=wordSplit(Article[i]);

            while(splitwords[i][j]!=null)
            {
                if(splitwords[i][j].length()>2&&(!StopWords.containsKey(splitwords[i][j])))
                {
                    buffer[j] = tools.Bigram.Bigram(splitwords[i][j]);
                    TF_IDF.tfCalculate(buffer[j],wordlist);     //统计词频
                }
                j++;
            }

        }
        keywordList=TF_IDF.sort(wordlist);                               //对符合条件的单词按词频从高到低排序

        //按排序的结果选取前k个关键词

        int count=0;
        for (Map.Entry<String, Integer> entry :keywordList)
        {
            String keyword=entry.getKey();
            System.out.println(tools.Bigram.Decode(keyword)); //输出符合条件的关键词，这里输出是方便查看符合条件的前k个关键词

            keywords.put(keyword,new int[100]);   //符合条件的关键词放入容器

            count++;
            if(count==top_k-1) break;
        }


        for(int i=0;i<ArticleNum;i++)
        {

            for(int j=0;splitwords[i][j]!=null;j++)
            {
                if(splitwords[i][j].length()>1&&(keywords.containsKey(tools.Bigram.Bigram(splitwords[i][j]))))
                {

                    int[] indexbuffer;                        //临时存放索引的缓冲区
                    buffer[j] = tools.Bigram.Bigram(splitwords[i][j]);

                    int[] code = new int[6];                                //获取关键词在布隆过滤器的对应哈希下标
                    System.arraycopy(BloomFilter.add(buffer[j]),0,code,0,6);         //加入布隆过滤器

                    /*
                         加入布隆过滤器下标-关键词索引
                     */
                    for(int c=0;c<6;c++)
                    {
                        filterindex.computeIfAbsent(code[c], k -> new HashSet<>());
                        bloomlist=filterindex.get(code[c]);
                        bloomlist.add(buffer[j]);
                        filterindex.put(code[c], bloomlist);
                    }

                    /*
                        在HashMap内定位对应的关键词,并修改倒排索引值
                     */
                    if (keywords.containsKey(buffer[j]))
                    {
                        indexbuffer = keywords.get(buffer[j]);
                        indexbuffer[i] = 1;
                        keywords.put(buffer[j], indexbuffer);
                    }
                }
            }
        }
    }

    /**
      4.陷门生成算法
    **/
    static int[] TrapdoorGen(String keyword) throws FileNotFoundException {
        BloomFilter Wq=new BloomFilter();
        String Biword=Bigram.Bigram(keyword);
        int[] keycode=new int[6];

        System.arraycopy(Wq.add(Biword),0,keycode,0,6);      //获取布隆过滤器的对应下标

        return keycode;
    }

    /**
      5.关键词匹配算法,分为搜索算法Search()和关键词匹配算法Match()
    **/
    static int Match(int[] keycode,String keyword)
    {
        //布隆过滤器内积匹配
        int count=0;
        if(!filter.contains(keyword))
        {
            for (int i=0; i<6; i++)
            {
                //如果输入的词对应的布隆过滤器下标与关键词集的布隆过滤器下标都为1，count加1，count大于4认为可以匹配
                if (filter.bits.get(keycode[i]))
                    count++;
            }

            return count;
        }
        else return 6;
    }

    static int[] Search() throws FileNotFoundException {
        int[] result=new int[100];
        Scanner sc=new Scanner(System.in);
        String str;
        System.out.print("请输入要搜索的关键词(输入'#'退出）:");
        str=sc.nextLine();

        //输入#直接退出程序
        if(str.equals("#"))
            System.exit(1);

        str=str.toLowerCase();          //全部转为小写

        int[] keycode=TrapdoorGen(str); //为关键词构造布隆过滤器并输入，返回布隆过滤器的输出值（对应过滤器的下标）

        /*
        若满足匹配阈值（这里设置为4），则先从布隆过滤器下标-关键词集索引取出对应下标包含的关键词。
        再从HashMap容器（索引）中取出对应的关键词的倒排索引，得到应输出的文档编号，否则返回空值
         */
        if(Match(keycode,str)>=4)
        {
            HashMap<String,Integer> finalword=new HashMap<>();    //存储获取的可能关键词，并按其在布隆过滤器内的词频排序

            /*
                获得输入的关键词对应的布隆过滤器下标keycode后，根据下标取出链接的关键词向量，并统计词频，按照词频高低依次排序。
             */
            for(int i=0;i<6;i++)
            {
                Set<String> wordset=filterindex.get(keycode[i]);
                if(wordset!=null) {
                    for (String s :wordset) {
                        TF_IDF.tfCalculate(s, finalword);
                    }
                }
            }
            keywordList=TF_IDF.sort(finalword);

            //获取对应关键词的文档倒排索引并整合
            System.out.print("匹配到可能的关键词:");
            for (Map.Entry<String, Integer> entry :keywordList)
            {
                String key=entry.getKey();
                if(entry.getValue()>1)
                {
                    System.out.print(Bigram.Decode(key) + ' ');
                    int[] buf = keywords.get(key);
                    for (int i = 0; i < ArticleNum; i++) {
                        if (result[i] == 0 && buf[i] == 1)
                            result[i] = 1;
                    }
                }
            }
            System.out.println();

            //最终将结果整合为数组result
            System.out.print("匹配的目标文档编号为：");
            for(int i=0;i<ArticleNum;i++) {
                if (result[i] == 1)
                    System.out.print((i + 1) + " ");
            }
            System.out.println("\n*****\n如果搜索结果与输入单词不匹配，可能是拼写错误或输入单词不属于关键词。\n*****\n");

        }
        else result[0]=-1;  //标记，表示没有找到匹配的关键词
        return result;
    }

    /**
      6.文档解密算法
    **/
    static void DecryptFile(int[] result,RSAPrivateKey prkey) throws Exception {
        StringBuilder DecResult= new StringBuilder();

        if(result[0]==-1)
        {
            System.out.println("失败，没有找到可能匹配的关键词！");
            return;
        }

        for(int i=0;i<ArticleNum;i++)
        {
            if(result[i]==1)
            {
                int j=0;
                while(EncodedArticle[i][j]!=null)
                {
                    DecResult.append(RSA.decryptByPrivateKey(EncodedArticle[i][j], prkey));
                    j++;
                }
                System.out.println("解密文档：编号"+(i+1));
                System.out.println(DecResult);
                DecResult = new StringBuilder();
            }
        }
    }

    /**
     * 主函数
     */
    public static void main(String[] args) throws Exception
    {
        /**
         * 第一步：生成RSA密钥对
         */
        KeyGen();

        /**
         * 第二步：读取文档内容，
         * 提取关键词，
         * 构造倒排索引
         * 对提取的关键词构造布隆过滤器
         */
        getArticle();
        indexBuild();

        /**
         * 第三步：对文档内容加密，并存储（为简化流程，本程序不使用数据库，仅存储于字符串内）
         */
        EncodedArticle=EncryptFile(ArticleNum , Article);

        /**
         * 第四步：关键词查找与匹配
         * 第五步：根据匹配结果解密文档并返回
         */
        while(true)
            DecryptFile(Search(),privateKey);
    }

}
