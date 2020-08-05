package strings;

import java.util.*;

/**
 * 代码比较工具（基于后缀数组）
 */
public class CodeUtils {

    /**
     * 判断两端代码是否相似
     * @param code1 代码段1
     * @param code2 代码段2
     * @return
     */
    public static float isSimilarCode(String code1, String code2){
        try{
            //token化
            List<Byte> tokens1 = lexer(code1);
            List<Byte> tokens2 = lexer(code2);

            //标记片段的token边界
            List<Fragment> fragments = new ArrayList<>();
            fragments.add(new Fragment(0, tokens1.size() - 1));
            fragments.add(new Fragment(tokens1.size(), tokens1.size() + tokens2.size() - 1));

            //后缀数组检测重叠片段
            List<Byte> tokens = new ArrayList<>();
            tokens.addAll(tokens1);
            tokens.addAll(tokens2);
            SuffixArray suffixArray = new SuffixArray();
            suffixArray.init(tokens);
            List<Integer> result = suffixArray.process();

            //处理检测结果
            List<ClonePair> clonePairs = new ArrayList<>();
            for (int i = 0; i < result.size() / 3; i++) {
                if (result.get(3 * i) == 0) {
                    continue;
                }
                int x1 = result.get(3 * i);
                int x2 = result.get(3 * i + 1);
                int cloneLen = result.get(3 * i + 2);
                int firstFrom = searchFragment(fragments, x1);
                int firstTo = searchFragment(fragments, x1 + cloneLen - 1);
                int secondFrom = searchFragment(fragments, x2);
                int secondTo = searchFragment(fragments, x2 + cloneLen - 1);

                if (firstFrom == secondFrom){
                    continue;
                }
                if (firstFrom != firstTo || secondFrom != secondTo){
                    continue;
                }

                if (cloneLen == 0){
                    continue;
                }
                if (firstFrom == 0){
                    clonePairs.add(new ClonePair(x1, x2 ,cloneLen));
                }else{
                    clonePairs.add(new ClonePair(x2, x1 ,cloneLen));
                }
            }

            Collections.sort(clonePairs, new Comparator<ClonePair>() {
                @Override
                public int compare(ClonePair o1, ClonePair o2) {
                    if (o1.first < o2.first){
                        return -1;
                    }else if (o1.first > o2.first){
                        return 1;
                    }
                    return 0;
                }
            });

            int overlapping = calculateOverlapping(clonePairs);

            return overlapping * 1f / Math.max(tokens1.size(), tokens2.size());
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0f;
    }

    /**
     * 搜索片段的索引
     * @param fragments
     * @param startIndex
     * @return
     */
    private static int searchFragment(List<Fragment> fragments, int startIndex){
        int index = -1;
        for (int i=0; i<fragments.size(); i++) {
            if (startIndex >= fragments.get(i).start && startIndex <= fragments.get(i).end) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 计算克隆片段的重叠长度
     * @param pairs
     * @return
     */
    private static int calculateOverlapping(List<ClonePair> pairs){
        int index = 0;
        int startToken = 0;
        int size = 0;
        int totalSize = 0;

        while (index < pairs.size()){
            int pairIndex = pairs.get(index).first;
            int pairSize = pairs.get(index).size;
            if (index == 0){
                startToken = pairIndex;
                size = pairSize;
                index++;
                continue;
            }
            if (startToken + size >= pairIndex) {
                if (startToken + size >= pairIndex + pairSize){
                }else{
                    size = pairIndex - startToken + pairSize;
                }
                index++;
            }else{
                totalSize += size;
                startToken = pairIndex;
                size = pairSize;
                index++;
            }
        }
        return Math.max(totalSize, size);
    }

    /**
     * 代码token化方法
     * @param stat
     * @return
     */
    public static List<Byte> lexer(String stat){
        int index = 0;
        List<Byte> res = new ArrayList<>();
        String token = "";
        while (index < stat.length()){
            char c = stat.charAt(index);
            if (Character.isSpaceChar(c)){
                index++;
                continue;
            }
            if (Character.isDigit(c)){
                while (Character.isDigit(c)){
                    token += c;
                    if (++index >= stat.length())
                        break;
                    c = stat.charAt(index);
                }
                res.add(str2hash(token));
                token = "";
                continue;
            }
            if (Character.isLetter(c) || c == '_'){
                while (Character.isLetterOrDigit(c) || c == '_'){
                    token += c;
                    if (++index >= stat.length())
                        break;
                    c = stat.charAt(index);
                }
                res.add(str2hash(token));
                token = "";
                continue;
            }
            res.add(str2hash(c+""));
            index++;
        }
        return res;
    }

    /**
     * 哈希函数，将字符串映射到[-128,-3]u[125,127]字节空间
     * @param str
     * @return
     */
    private static byte str2hash(String str) {
        str = str.toLowerCase();
        if (str.length() < 2) {
            int h = str.toCharArray()[str.length() - 1];
            h <<= 1;
            return (byte) (-3 - (h & 0x7f));
        } else {
            int h1 = str.toCharArray()[str.length() - 1];
            int h2 = str.toCharArray()[str.length() - 2];
            h1 <<= 1;
            int h = h1 ^ h2;
            return (byte) (-3 - (h & 0x7f));
        }
    }


    /**
     * 后缀数组工具类
     */
    @SuppressWarnings("Duplicates")
    public static class SuffixArray{
        private List<Byte> tokens;
        private int[] sa;
        private int[] height;

        /**
         * 初始化token列表
         * @param tokens
         */
        public void init(List<Byte> tokens){
            this.tokens = tokens;
            sa = new int[tokens.size()];
            height = new int[tokens.size()];
        }

        /**
         * 构建后缀数组
         */
        private void buildSuffixArray(){
            //初始化sa
            for (int i=0; i<sa.length; i++){
                sa[i] = i;
            }
            for(int i=0; i<tokens.size()-1; i++) {
                for (int j=i+1; j<tokens.size(); j++){
                    List<Byte> suffix1 = tokens.subList(sa[i], tokens.size());
                    List<Byte> suffix2 = tokens.subList(sa[j], tokens.size());
                    int size = Math.min(suffix1.size(), suffix2.size());
                    boolean result = suffix1.size() < suffix2.size();
                    for (int m=0; m<size; m++){
                        if (suffix1.get(m) < suffix2.get(m)){
                            result = true;
                            break;
                        }else if (suffix1.get(m) > suffix2.get(m)){
                            result = false;
                            break;
                        }
                    }
                    if (!result){
                        int tmp = sa[i];
                        sa[i] = sa[j];
                        sa[j] = tmp;
                    }
                }
            }
        }

        /**
         * 计算高度数组
         */
        private void calculateHeight(){
            for (int i=1; i<sa.length; i++){
                List<Byte> pre = tokens.subList(sa[i - 1], tokens.size());
                List<Byte> cur = tokens.subList(sa[i], tokens.size());
                int cnt = 0;
                int size = Math.min(pre.size(), cur.size());
                for (int j=0; j<size; j++){
                    if (!pre.get(j).equals(cur.get(j))){
                        break;
                    }
                    cnt++;
                }
                height[i] = cnt;
            }
        }

        /**
         * 获取检测结果
         * @return
         */
        public List<Integer> process(){
            buildSuffixArray();
            calculateHeight();
            List<Integer> results = new ArrayList<>();
            for (int i=1; i<height.length; i++){
                results.add(sa[i - 1]);
                results.add(sa[i]);
                results.add(height[i]);
            }
            return results;
        }
    }

    /**
     * 克隆片段
     */
    public static class Fragment{
        public int start;
        public int end;

        public Fragment(int start, int end){
            this.start = start;
            this.end = end;
        }
    }

    /**
     * 克隆检测结果
     */
    public static class ClonePair{
        public int first;
        public int second;
        public int size;

        public ClonePair(int first, int second, int size) {
            this.first = first;
            this.second = second;
            this.size = size;
        }
    }
}
