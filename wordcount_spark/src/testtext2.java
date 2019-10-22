import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.api.java.function.*;

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class testtext2 {
	static int endtmp=0;
	static int wordLength = 0;
	//ͣ�ôʴʱ�  
    public static final String stopWordTable = "D:\\stopword.txt"; 
    //�������ͣ�ôʵļ���  
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
		File writename1 = new File(filename); // ���·�������û����Ҫ����һ���µ�output��txt�ļ�  
		writename1.createNewFile();// �������ļ�  
		BufferedWriter out1 = new BufferedWriter(new FileWriter(writename1));  
		int count = 0;
		String str = "";
		String str1="";
		List<Tuple2<String, Integer>> words;
		
		words = wordCount.collect();
		
		for (Tuple2<String, Integer> word : words) {
			if (str.equals("")) {
				str = word._1 + ":" + word._2;
				out1.write(str+"\r\n"); // \r\n��Ϊ����  
		    	out1.flush(); // �ѻ���������ѹ���ļ�  
			} else {
				str1 = word._1 + ":" + word._2;
				out1.write(str1+"\r\n"); // \r\n��Ϊ����  
		    	out1.flush(); // �ѻ���������ѹ���ļ�  
		    	str += "  " + str1;
			}
			count++;
			if (count % 10 == 0) {
				//addTextLine(str, count);
				str = "";
			}
		}
		out1.close(); // ���ǵùر��ļ�	
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
		//InputStream is = new ByteArrayInputStream(line.getBytes("GBK"));
		//String str = getStringFromInputStream(is);
		String str=line;
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
    	//����ͣ�ô��ļ�  
        BufferedReader StopWordFileBr = new BufferedReader(new InputStreamReader(new FileInputStream(new File(stopWordTable))));   
        //���绯ͣ�ôʼ�  
        String stopWord = null;  
        for(; (stopWord = StopWordFileBr.readLine()) != null;){  
            stopWordSet.add(stopWord);  
        }
        
        SparkConf conf =
                new SparkConf().setMaster("local").setAppName("WordCount_Java");
        JavaStreamingContext jsc = new JavaStreamingContext(conf, Durations.minutes(1));//ʱ������ó�һ�㣬Ҫ��û���ϴ���Ϳ�ʼ�ˣ�����ʶ�𲻳���
        //JavaRDD<String> file = jsc.textFile("D:\\1.txt");
        //JavaDStream<String> file = jsc.textFileStream("hdfs://localhost:9000/sparktest");
        JavaDStream<String> file = jsc.textFileStream("D:\\1");
        //������ק��ֻ���½��ļ�����
        JavaDStream<String> words = file.flatMap(new FlatMapFunction<String, String>() {
            public Iterator<String> call(String line) throws Exception {
            	//return Arrays.asList(line.split("\\W+")).iterator();
    			//return strlist.iterator();
    			return getSplitWords(line).iterator();
            }
        });
        JavaPairDStream<String, Integer> wordMap = words.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String word) throws Exception {
                return new Tuple2<String, Integer>(word, 1);
            }
        });
        JavaPairDStream<String, Integer> reduceWord = wordMap.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer v1, Integer v2) throws Exception {
                return v1 + v2;
            }
        });
        JavaPairDStream<Integer, String> pairRDD = reduceWord.mapToPair(new PairFunction<Tuple2<String, Integer>,
                Integer, String>() {
            public Tuple2<Integer, String> call
                    (Tuple2<String, Integer> t2) throws Exception {
                return new Tuple2<Integer, String>(t2._2, t2._1);
            }
        });
        //���밴��Ƶ������
        //�Ȱ�key��value������Ȼ��sortByKey������ٽ�����ȥ
        //����
        JavaPairDStream<Integer, String> sortPairRDD = pairRDD.transformToPair(pairRdd -> pairRdd.sortByKey(false));
        //JavaPairRDD<Integer, String> sortPairRDD = pairRDD.sortByKey(false);
        //�ٴν���key��value
        JavaPairDStream<String, Integer> resultRdd = sortPairRDD.mapToPair(new PairFunction<Tuple2<Integer, String>, String, Integer>() {
            public Tuple2<String, Integer> call
                    (Tuple2<Integer, String> t2) throws Exception {
                return new Tuple2<String, Integer>(t2._2, t2._1);
            }
        });
        /*resultRdd.foreach(new VoidFunction<Tuple2<String, Integer>>() {
            public void call
                    (Tuple2<String, Integer> t2) throws Exception {
                System.out.println("���ֵĵ�����     : " + t2._1);
                System.out.println("���ʳ��ֵĴ����� : " + t2._2);
            }
        });*/
        resultRdd.foreachRDD(new VoidFunction<JavaPairRDD<String, Integer>>() {
            public void call(JavaPairRDD<String, Integer> stringIntegerJavaPairRDD) throws Exception {
                stringIntegerJavaPairRDD.foreach(new VoidFunction<Tuple2<String, Integer>>() {
                    @Override
                    public void call(Tuple2<String, Integer> t2) throws Exception {
                        //ConnectionPool�Ķ�����ConnectionPoolTitle��һ��
                    	System.out.println("���ֵĵ�����     : " + t2._1);
                        System.out.println("���ʳ��ֵĴ����� : " + t2._2);
                    }
                });
              //���������ļ���
                System.out.println("ǰ��ʮƵ����");
                showRDDWordCount(stringIntegerJavaPairRDD,20);
                System.out.println("���浽�ļ���");
                writetofile(stringIntegerJavaPairRDD,"D:\\1_result\\1_result"+endtmp+".txt");
                endtmp++;
            }
        });
        
        jsc.start();
        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        jsc.stop();
    }
}