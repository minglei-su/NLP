package ming.ner;


import java.io.*;
import java.util.ArrayList;

import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;

/**
 * 分词使用示例
 * @author xpqiu
 *
 */
public class DictCWSTagger {
	/**
	 * 主程序
	 * @param args 
	 * @throws IOException 
	 * @throws  
	 */
	public static void main(String[] args) throws Exception {
/*		CWSTagger tag = new CWSTagger("./models/cwsmodel.m");
		System.out.println("不使用词典的分词：");
		String str = " 媒体计算研究所成立了, 高级数据挖掘(data mining)很难。 ";
		String s = tag.tag(str);
		System.out.println(s);
		
		//设置英文预处理
		tag.setEnFilter(true);
		s = tag.tag(str);
		System.out.println(s);
		tag.setEnFilter(false);
		//注：词典里只能有中文字符，英文与数字不支持
		System.out.println("\n设置临时词典：");
		ArrayList<String> al = new ArrayList<String>();
		al.add("数据挖掘");
//		al.add("媒体计算研究所");
		tag.setDictionary(new Dictionary(Dictionary.format(al), false));
		s = tag.tag(str);
		System.out.println(s);
*/
		CWSTagger tag2 = new CWSTagger("./models/cwsdbmodel.m", new Dictionary("./models/cities_alias.properties"));
		System.out.println("\n使用词典的分词：");
        BufferedReader input = new BufferedReader(new FileReader("./models/dbwrong.txt"));
        String s = new String();
        String s2 = new String();
        String src = new String();
        long start = System.currentTimeMillis();
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
                "./models/cwsdbresult.txt"), "utf8");
        for (int i = 0; i < 100000; i++) {
            if ((src = input.readLine()) != null) {
                s = tag2.tag(src);
                w.write(s+"\r\n");
            }
        }
        w.flush();
        input.close();
        w.close();
        long over = System.currentTimeMillis();
        System.out.println("time :" + String.valueOf(over-start));
		
		/**
		CWSTagger tag2 = new CWSTagger("./models/cwsmodel.m", new Dictionary("./models/cities_alias.properties"));
		System.out.println("\n使用词典的分词：");
		String s2 = tag2.tagFile("./models/cwstestsource.txt");
		OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(
				"./models/cwstestresult.txt"), "utf8");
		w.write(s2);
		w.close();
		*/
		
/*		
		//使用不严格的词典
		CWSTagger tag3 = new CWSTagger("./models/seg.m", new Dictionary("./models/dict_ambiguity.txt",true));
		//尽量满足词典，比如词典中有“成立”“成立了”和“了”, 会使用Viterbi决定更合理的输出
		System.out.println("\n使用不严格的词典的分词：");
		String str3 = "媒体计算研究所成立了, 高级数据挖掘很难";
		String s3 = tag3.tag(str3);
		System.out.println(s3);
		str3 = "我送给力学系的同学一个玩具 (送给给力力学力学系都在词典中)";
		s3 = tag3.tag(str3);
		System.out.println(s3);
		
		System.out.println("\n处理文件：");
		String s4 = tag.tagFile("./example-data/data-tag.txt");
		System.out.println(s4);
		*/
//		tag3.tagFile("./example-data/data-tag.txt","./example-data/data-tag1.txt");
		
	}
	

}
