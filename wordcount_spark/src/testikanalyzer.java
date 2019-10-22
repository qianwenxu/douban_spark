import java.io.StringReader;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.wltea.analyzer.lucene.IKAnalyzer;
public class testikanalyzer {
 
		public static void main(String[] args) throws Exception {
			String text = "��ͬ�ڼ����������һ���۾���Ѹ�ٿ����Ϳ�����һ����������Ϊ�˵Ĵ���Ƥ��������һ�����Ϻ�����Ԫ�������Ӿ��������ɡ�";
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