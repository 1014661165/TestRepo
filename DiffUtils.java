package strings;

import difflib.Delta;
import difflib.DiffRow;
import difflib.DiffRowGenerator;
import difflib.Patch;
import java.util.ArrayList;
import java.util.List;

/**
 * Diff工具类
 */
public class DiffUtils {

    /**
     * 计算original到revised的变化
     * @param original 原始文本
     * @param revised 修改文本
     * @return
     */
    public static List<String> getPatch(List<String> original, List<String> revised){
        Patch<String> patch = difflib.DiffUtils.diff(original, revised);
        List<String> res = new ArrayList<>();
        for (Delta<String> delta :  patch.getDeltas()) {
            int s1 = delta.getOriginal().getPosition();
            int e1 = s1 + delta.getOriginal().getLines().size();
            int s2 = delta.getRevised().getPosition();
            int e2 = s2 + delta.getRevised().getLines().size();
            res.add(String.format("%d,%dc%d,%d", s1, e1, s2, e2));
        }
        return res;
    }
}
