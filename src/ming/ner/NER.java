/**
 * 
 */
package ming.ner;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.NERTagger;
import edu.fudan.nlp.cn.tag.POSTagger;

/**
 * @info author:suml date:2014-8-15
 *
 */
public class NER {

	public static void main(String args[]) {
		try {

            NERTagger nerTagger = new NERTagger("./models/cwsdbmodel.m", "./models/model.m", "./models/cities_alias.properties");
            String s = "山东网易信息技术有限公司";
            System.out.println(nerTagger.tag(s));
//			String s = "中国石油天然气股份有限公司（简称“中国石油”）是中国油气行业占主导地位的最大的油气生产和销售商，是中国销售收入最多的公司之一，也是世界最大的石油公司之一。截至2013年初，总资产达3478亿美元."+
//			"中国人民银行是中华人民共和国的中央银行，中华人民共和国国务院     组成部门之一。"+
//			"1995年搜狐创始人张朝阳从美国麻省理工学院毕业回到中国，利用风险投资创建了爱特信信息技术有限公司，1998年正式推出搜狐网。";
/*			String s = "常州汉东机械制造有限公司";
			String s2 = "北京科大有限公司";
			String s25 = "北京科技大学";
			String s3 = "西吉县华绒有限责任公司";
			HashMap<String,String> map = new HashMap<String,String>();
			map = tag.tag(s);
			System.out.println(map);
			map = tag.tag(s2);
			System.out.println(map);
			map = tag.tag(s25);
			System.out.println(map);
			map =  tag.tag(s3);
			System.out.println(map);
*//*
            BufferedReader input = new BufferedReader(new FileReader("./models/cwsdbsource.txt"));
            Map<String, String> s = new HashMap<String, String>();
            String s2 = new String();
            String src = new String();
            long start = System.currentTimeMillis();
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
                    "./models/cwsdbresult.txt"), "utf8");
            for (int i = 0; i < 100000; i++) {
                if ((src = input.readLine()) != null) {
                    s = nerTagger.tag(src);
                    w.write(s+"\r\n");
                }
            }
            w.flush();
            input.close();
            w.close();
            long over = System.currentTimeMillis();
            System.out.println("time :" + String.valueOf(over-start));*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
