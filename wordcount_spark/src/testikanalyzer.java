import java.io.StringReader;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;
public class testikanalyzer {
 
		public static void main(String[] args) throws Exception {
			String text = "不同于计算机，人类一睁眼就能迅速看到和看明白一个场景，因为人的大脑皮层至少有一半以上海量神经元参与了视觉任务的完成。";
			Analyzer analyzer = new IKAnalyzer(true);
			StringReader reader = new StringReader(text);
			TokenStream ts = analyzer.tokenStream("", reader); 
			CharTermAttribute term=ts.getAttribute(CharTermAttribute.class);
			while(ts.incrementToken()){ 
				System.out.print(term.toString()+"|"); 
			}
			analyzer.close();
			reader.close(); 
}
 
}