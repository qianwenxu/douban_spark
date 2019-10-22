import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import scala.Tuple2;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class testtxt1 {
	static int wordLength = 0;
	//停用词词表  
    public static final String stopWordTable = "doc\\stopword.txt"; 
    //用来存放停用词的集合  
    static Set<String> stopWordSet = new HashSet<String>(); 
	
	private static void showRDDWordCount(JavaPairRDD<String, Integer> wordCount,
			int countLimit) {
		int count = 0;
		String str = "";
		
		List<Tuple2<String, Integer>> words;
		
		words = wordCount.collect();
		
		for (Tuple2<String, Integer> word : words) {
			if (countLimit == 0 || count < countLimit) {
				if (str.equals("")) {
					str = word._1 + ":" + word._2;
				} else {
					str += "  " + word._1 + ":" + word._2;
				}
				count++;
				if (count % 10 == 0) {
					//addTextLine(str, count);
					System.out.println("str:"+str+"count:"+count);
					str = "";
				}
			}
		}

		if ("".equals(str) == false) {
			//addTextLine(str, count);
			System.out.println("str:"+str+"count:"+count);
		}
	}
	
	
	private static void writetofile(JavaPairRDD<String, Integer> wordCount,String filename) throws IOException {
		File writename1 = new File(filename); // 相对路径，如果没有则要建立一个新的output。txt文件  
		writename1.createNewFile();// 创建新文件  
		BufferedWriter out1 = new BufferedWriter(new FileWriter(writename1));  
		int count = 0;
		String str = "";
		String str1="";
		List<Tuple2<String, Integer>> words;
		
		words = wordCount.collect();
		
		for (Tuple2<String, Integer> word : words) {
			if (str.equals("")) {
				str = word._1 + ":" + word._2;
				out1.write(str+"\r\n"); // \r\n即为换行  
		    	out1.flush(); // 把缓存区内容压入文件  
			} else {
				str1 = word._1 + ":" + word._2;
				out1.write(str1+"\r\n"); // \r\n即为换行  
		    	out1.flush(); // 把缓存区内容压入文件  
		    	str += "  " + str1;
			}
			count++;
			if (count % 10 == 0) {
				//addTextLine(str, count);
				str = "";
			}
		}
		out1.close(); // 最后记得关闭文件	
	}
	
	 private static String getStringFromInputStream(InputStream is) {
	        BufferedReader br = null;
	        StringBuilder sb = new StringBuilder();
	        String line;
	        try {
	            br = new BufferedReader(new InputStreamReader(is));
	            while ((line = br.readLine()) != null) {
	                sb.append(line);
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (br != null) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	        return sb.toString();
	    }
	private static List<String> getSplitWords(String line) throws IOException{
		List<String> words=new ArrayList<String>();
		InputStream is = new ByteArrayInputStream(line.getBytes("GBK"));
		String str = getStringFromInputStream(is);
		Analyzer analyzer = new IKAnalyzer(true);
		StringReader reader = new StringReader(str);
		TokenStream ts = analyzer.tokenStream("", reader); 
		CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);
		while(ts.incrementToken()){ 
			//System.out.print(term.toString()+"|"); 
			if(stopWordSet.contains(term.toString())) {  
                continue;  
            }
			words.add(term.toString());
		}
		analyzer.close();
		reader.close(); 
		
		return words;
	}
	
    public static void main(String[]args) throws Exception{
    	//读入停用词文件  
        BufferedReader StopWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(stopWordTable))));   
        //初如化停用词集  
        String stopWord = null;  
        for(; (stopWord = StopWordFileBr.readLine()) != null;){  
            stopWordSet.add(stopWord);  
        }
        
        SparkConf conf =
                new SparkConf().setMaster("local").setAppName("WordCount_Java");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        //JavaRDD<String> file = jsc.textFile("D:\\1.txt");
        JavaRDD<String> file = jsc.textFile("doc\\1.txt");
        JavaRDD<String> words = file.flatMap(new FlatMapFunction<String, String>() {
            public Iterator<String> call(String line) throws Exception {
            	//return Arrays.asList(line.split("\\W+")).iterator();
    			//return strlist.iterator();
    			return getSplitWords(line).iterator();
            }
        });
        JavaPairRDD<String, Integer> wordMap = words.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });
        JavaPairRDD<String, Integer> reduceWord = wordMap.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        JavaPairRDD<Integer, String> pairRDD = reduceWord.mapToPair(new PairFunction<Tuple2<String, Integer>,
                Integer, String>() {
            public Tuple2<Integer, String> call
                    (Tuple2<String, Integer> t2) throws Exception {
                return new Tuple2<Integer, String>(t2._2, t2._1);
            }
        });
        //加入按词频排序功能
        //先把key和value交换，然后按sortByKey，最后再交换回去
        //降序
        JavaPairRDD<Integer, String> sortPairRDD = pairRDD.sortByKey(false);
        //再次交换key和value
        JavaPairRDD<String, Integer> resultRdd = sortPairRDD.mapToPair(new PairFunction<Tuple2<Integer, String>, String, Integer>() {
            public Tuple2<String, Integer> call
                    (Tuple2<Integer, String> t2) throws Exception {
                return new Tuple2<String, Integer>(t2._2, t2._1);
            }
        });
        resultRdd.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            public void call
                    (Tuple2<String, Integer> t2) throws Exception {
                System.out.println("出现的单词是     : " + t2._1);
                System.out.println("单词出现的次数是 : " + t2._2);
            }
        });
        //保存结果到文件中
        System.out.println("前二十频繁数");
        showRDDWordCount(resultRdd,20);
        System.out.println("保存到文件中");
        writetofile(resultRdd,"doc\\1_result.txt");
        jsc.close();
    }
}