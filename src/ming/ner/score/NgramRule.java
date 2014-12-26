package ming.ner.score;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by mingleis on 2014/12/26.
 */
public class NgramRule implements Rule {

    private int step;

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public NgramRule() {
        super();
        this.step = 2;
    }

    @Override
    public float compareWords(String str1, String str2) {
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
        return similarity;    }
}
