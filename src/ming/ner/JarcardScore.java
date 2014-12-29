package ming.ner;

import edu.fudan.ml.types.*;
import edu.fudan.ml.types.Dictionary;
import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.NERTagger;
import edu.fudan.nlp.cn.tag.POSTagger;
import ming.ner.score.AddressLevelRule;
import ming.ner.score.NgramRule;
import ming.ner.score.SimilarRule;

import java.io.*;
import java.util.*;

/**
 * Created by mingleis on 2014/12/21.
 */
public class JarcardScore {

    private SimilarRule similarRule;
    private AddressLevelRule addressLevelRule;
    private NgramRule ngramRule;

    private static String  SPILIT=",";
    private static String  SPILITKEY = ":";

    private static String FIELD_QH = "行政区划";
    private static String FIELD_ZH = "字号";
    private static String FIELD_HY = "行业";
    private static String FIELD_ZZ = "组织形式";

    public JarcardScore() {
        super();
        similarRule = new SimilarRule();
        addressLevelRule = new AddressLevelRule();
        ngramRule = new NgramRule();
    }

    /**
     * 规则的调度
     * @param word1
     * @param word2
     * @return
     */
    private float compareWord(String word1, String word2) {
        float score = 0;
        boolean isSimilar = false , isAddressLevel = false;
        if (word1 != null && word2 != null) {
            if (word1.equalsIgnoreCase(word2)) {
                score = 1;
            } else {
                if ((score = similarRule.compareWords(word1, word2)) == 0.0f) {
                    if ((score = addressLevelRule.compareWords(word1, word2)) == 0.0f) {
                        score = ngramRule.compareWords(word1, word2);
                    }
                }
            }
        } else {
            score = 0.0f;
        }
        System.out.println(score);
        return score;
    }

    public void generateManiData(Map<String, String> map, Map<String, String> map2) {
        map.put("行政区划", "山东");
        map.put("字号", "易贸创想");
        map.put("行业", "软件技术");
        map.put("组织形式", "有限公司");
        map2.put("行政区划", "青岛");
        map2.put("字号", "易贸创想");
        map2.put("行业", "信息技术");
        map2.put("组织形式", "集团公司");
    }
    /**
     * 根据各个字段不同权重获得分值,目前使用模拟数据
     * @param str1
     * @param str2
     * @param step
     * @return
     */
    public float getSimilarity(String str1, String str2, int step) {
        //模拟NER
        Map<String,String> map = new HashMap<String, String>();
        Map<String, String> map2 = new HashMap<String, String>();
//        this.generateManiData(map, map2);
        this.ComputeByNER(str1, str2, map, map2);
        float[] weight = new float[]{0.5f,1.0f,0.4f,0.0f};
        if (map.get("字号") == null && map2.get("字号") == null) {
            weight[1] = 0f;
            weight[2] = 1.0f;
        }
        float score = (this.compareWord(map.get("行政区划"), map2.get("行政区划")) * weight[0]
                + this.compareWord(map.get("字号"), map2.get("字号")) * weight[1]
                + this.compareWord(map.get("行业"), map2.get("行业")) * weight[2]
                )
                / (weight[0] + weight[1] + weight[2]);
        return score;
    }
    public boolean convertArray2Map(String[][] array, Map<String, String> map1) {
        for (int i = 0; i < array[0].length; i ++) {
            if (map1.get(array[1][i]) != null) {
                map1.put(array[1][i],map1.get(array[1][i]).concat(array[0][i]));
            } else {
                map1.put(array[1][i], array[0][i]);
            }
        }
        return true;
    }
    //desprated
    public boolean converValue2Key(Map<String, String > map, Map<String, String> map1 ) {
        for (Map.Entry<String, String > entry : map.entrySet()) {
            String key = entry.getValue();
            if (map1.get(key) != null) {
                StringBuilder sb = new StringBuilder(map1.get(key));
                sb.append(entry.getKey());
                map1.put(entry.getValue(), sb.toString());
            } else {
                map1.put(entry.getValue(), entry.getKey());
            }
        }
        return true;
    }
    public void ComputeByNER (String s , String s2, Map<String ,String> map, Map<String,String> map2) {
        try {
            POSTagger posTagger = new POSTagger("./models/cwsdbmodel.m", "./models/model.m", new Dictionary("./models/cities_alias.properties"));
            String[][] array0 = posTagger.tag2Array(s);
            String[][] array1 = posTagger.tag2Array(s2);
            this.convertArray2Map(array0, map);
            this.convertArray2Map(array1, map2);
            System.out.println(map);
            System.out.println(map2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {

        JarcardScore score = new JarcardScore();

        System.out.println(score.getSimilarity("山东网易软件技术有限公司", "青岛网易信息技术有限公司", 2));
    }
}
