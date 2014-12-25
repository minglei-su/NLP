package ming.ner;

import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.corpus.StopWords;
import gnu.trove.set.hash.THashSet;

import java.util.List;

/**
 * Created by zxzx on 2014-11-21.
 */
public class TestStop {

    public static void main(String args[]) {
        try {
            CWSTagger tag2 = new CWSTagger("./models/cwsmodel.m");
            THashSet<String> set = new THashSet<String>();
            set.add("./models/dict.txt");
            set.add("./models/cities_alias.properties");
            tag2.setDictionary(set);

            //tag2.setDictionary(new Dictionary("./models/cities_alias.properties"));
            System.out.println("\n使用词典的分词：");
            String src="(只贷不收)西吉县华绒有限责任公司";
            StopWords stopWords = new StopWords("./models/stopwords/StopWords.txt");
            System.out.println(tag2.tag(src));
            List<String> list = stopWords.phraseDel(tag2.tag(src));
            for (int i = 0; i <list.size() ; i++) {
                System.out.println(list.get(i));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }
}
