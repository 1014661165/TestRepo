package cn.lylf.util;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 */
public class FileUtils {

    /**
     * 列出目录dir下所有后缀在extensions内的文件路径
     * @param dir
     * @param extensions
     * @return
     */
    public static List<String> listFiles(String dir, String[] extensions){
        List<String> files = new ArrayList<>();
        try {
            File root = new File(dir);
            File[] items = root.listFiles();
            if (items == null || items.length == 0){
                return files;
            }
            for (File f: items){
                if (f.isDirectory()){
                    files.addAll(listFiles(f.getAbsolutePath(), extensions));
                }else{
                    String filename = f.getName();
                    if (!filename.contains(".")){
                        continue;
                    }
                    String ext = filename.substring(filename.lastIndexOf(".") + 1);
                    for (String extension: extensions){
                        if (extension.toLowerCase().equals(ext.toLowerCase())){
                            files.add(f.getAbsolutePath());
                            break;
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return files;
    }
}
