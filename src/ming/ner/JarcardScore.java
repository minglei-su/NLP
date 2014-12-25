package ming.ner;

import edu.fudan.ml.types.*;
import edu.fudan.nlp.cn.tag.CWSTagger;
import edu.fudan.nlp.cn.tag.NERTagger;
import ming.ner.score.AddressLevelRule;
import ming.ner.score.SimilarRule;

import java.io.*;
import java.util.*;

/**
 * Created by mingleis on 2014/12/21.
 */
public class JarcardScore {

    private SimilarRule similarRule;
    private AddressLevelRule addressLevelRule;

    private static String  SPILIT=",";
    private static String  SPILITKEY = ":";

    JarcardScore() {
        super();
        similarRule = new SimilarRule();
        addressLevelRule = new AddressLevelRule();
    }

    private float compareWord(String word1, String word2) {
        float score = 0;
        boolean isSimilar = false , isAddressLevel = false;
        if (word1.equalsIgnoreCase(word2)) {
            score = 1;
        } else {
            if ( (score = similarRule.compareWords(word1, word2)) == 0.0f) {
                score = addressLevelRule.compareWords(word1, word2);
            }
        }
        return score;
    }

    /**
     * 暂未完成
     * @param str1
     * @param str2
     * @param step
     * @return
     */
    private float getScoreByNgram(String str1, String str2, int step) {
        Set<String> strset1 = new HashSet<String>();
        Set<String> strset2 = new HashSet<String>();
        boolean isSimilar = false, isAddressLevel = false;
        for (int i=0; i <= str1.length()-step; i ++ ) {
            strset1.add(str1.substring(i,i + step));
        }
        for (int i=0; i <= str2.length()-step; i ++ ) {
            strset2.add(str2.substring(i,i + step));
        }
        float equalsNum = 0;
        Iterator<String> iterator = strset1.iterator();
        while (iterator.hasNext()) {
            String temp1 = iterator.next();
            if (strset2.contains(temp1)) {
                equalsNum++;
            }
        }
        float mergeNum = (strset1.size() + strset2.size() - equalsNum);
        float similarity = equalsNum / mergeNum;
        return similarity;
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
        map.put("行政区划", "山东");
        map.put("字号", "易贸创想");
        map.put("行业", "软件技术");
        map.put("组织形式", "有限公司");
        map2.put("行政区划", "青岛");
        map2.put("字号", "易贸创想");
        map2.put("行业", "信息技术");
        map2.put("组织形式", "集团公司");
        float[] weight = new float[]{0.5f,1.0f,0.4f,0.0f};
        float score = (this.compareWord(map.get("行政区划"), map2.get("行政区划")) * weight[0]
                + this.compareWord(map.get("字号"), map2.get("字号")) * weight[1]
                + this.compareWord(map.get("行业"), map2.get("行业")) * weight[2]
                )
                / (weight[0] + weight[1] + weight[2]);
        return score;
    }

    public void ComputeByNER (String s , String s2) {
        try {
            NERTagger nerTagger = new NERTagger("./models/cwsdbmodel.m", "./models/model.m", "./models/cities_alias.properties");
            Map<String ,String > map = nerTagger.tag(s);
            Map<String , String > map2 = nerTagger.tag(s2);
            String srcAddress = null,  srcGroup = new String();
            String desAddress = null,  desGroup = null;
            List<String> srcOther = new ArrayList<String>();
            List<String> desOther = new ArrayList<String>();
            System.out.println(map);
            System.out.println(map2);
            for (String key : map.keySet()) {
                if (key.equalsIgnoreCase("行政区划")) {
                    srcAddress = map.get(key);
                } else if (key.equalsIgnoreCase("组织实行")){
                    srcGroup = map.get(key);
                } else {
                    srcOther.add(map.get(key));
                }
            }
            for (String key : map2.keySet()) {
                if (key.equalsIgnoreCase("行政区划")) {
                    desAddress = map.get(key);
                } else if (key.equalsIgnoreCase("组织实行")){
                    desGroup = map.get(key);
                } else {
                    desOther.add(map.get(key));
                }
            }
            if (srcAddress != null && desAddress != null) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {

        JarcardScore score = new JarcardScore();

        System.out.println(score.getSimilarity("", "", 2));
    }
}
