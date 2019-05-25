import info.monitorenter.cpdetector.io.*;

import java.io.*;
import java.nio.charset.Charset;

public class CpdetectorUtils {

    //获取文本编码
    private static final String FILE_ENCODE_TYPE = "file";
    //获取文件流编码
    private static final String IO_ENCODE_TYPE = "io";

    /**
     * 获取探测到的文件对象
     */
    private CodepageDetectorProxy getDetector() {
        /*
         * detector是探测器，它把探测任务交给具体的探测实现类的实例完成。
         * cpDetector内置了一些常用的探测实现类，这些探测实现类的实例可以通过add方法 加进来，如ParsingDetector、
         * JChardetFacade、ASCIIDetector、UnicodeDetector。
         * detector按照“谁最先返回非空的探测结果，就以该结果为准”的原则返回探测到的
         * 字符集编码。使用需要用到三个第三方JAR包：antlr.jar、chardet.jar和cpdetector.jar
         * cpDetector是基于统计学原理的，不保证完全正确。
         */
        CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();

        /*
         * ParsingDetector可用于检查HTML、XML等文件或字符流的编码,构造方法中的参数用于
         * 指示是否显示探测过程的详细信息，为false不显示。
         */
        detector.add(new ParsingDetector(false));
        /*
         * JChardetFacade封装了由Mozilla组织提供的JChardet，它可以完成大多数文件的编码
         * 测定。所以，一般有了这个探测器就可满足大多数项目的要求，如果你还不放心，可以
         * 再多加几个探测器，比如下面的ASCIIDetector、UnicodeDetector等。
         */
        detector.add(JChardetFacade.getInstance());// 用到antlr.jar、chardet.jar
        // ASCIIDetector用于ASCII编码测定
        detector.add(ASCIIDetector.getInstance());
        // UnicodeDetector用于Unicode家族编码的测定
        detector.add(UnicodeDetector.getInstance());

        return detector;
    }

    /**
     * 根据"encodeType"获取文本编码或文件流编码
     */
    public String getFileOrIOEncode(String filepath, String encodeType) {
        CodepageDetectorProxy detector = getDetector();
        Charset charset = null;
        File file = new File(filepath);
        try {
            switch (encodeType) {
                case FILE_ENCODE_TYPE:
                    charset = detector.detectCodepage(file.toURI().toURL());
                    break;
                case IO_ENCODE_TYPE:
                    charset = detector.detectCodepage(new BufferedInputStream(new FileInputStream(file)), 128);//128表示读取128字节来判断文件流的编码,读得越多越精确,但是速度慢
                    break;
                default:
                    charset = Charset.defaultCharset();
                    break;
            }

        } catch (IOException e) {
            //这里获取编码失败,使用系统默认的编码
            charset = Charset.defaultCharset();
            System.out.println(e.getMessage());
        }
        return charset.name();
    }

    public static void main(String[] args) {
        CpdetectorUtils cpdetectorUtils = new CpdetectorUtils();
        File file = new File("D:\\workspace\\FindCharsetDemo\\src\\main\\java\\");
        printFile(cpdetectorUtils,file);


    }


    private static void getCharSet(CpdetectorUtils cpdetectorUtils,String filepath){
        System.out.println("文件编码: " + cpdetectorUtils.getFileOrIOEncode(filepath, FILE_ENCODE_TYPE));
        System.out.println("文件流编码: " + cpdetectorUtils.getFileOrIOEncode(filepath, IO_ENCODE_TYPE));
    }


    public static void printFile(CpdetectorUtils cpdetectorUtils,File file) {
        if (file.isFile()) {
            System.out.println("您给定的是一个文件"); // 判断给定目录是否是一个合法的目录，如果不是，输出提示
        } else {
            File[] fileLists = file.listFiles(); // 如果是目录，获取该目录下的内容集合

            for (int i = 0; i < fileLists.length; i++) { // 循环遍历这个集合内容
                File fileList = fileLists[i];
                System.out.println(fileLists[i].getName());    //输出元素名称
                if (fileLists[i].isDirectory()) {    //判断元素是不是一个目录
                    printFile(cpdetectorUtils,fileLists[i]);    //如果是目录，继续调用本方法来输出其子目录
                } else {
                    getCharSet(cpdetectorUtils,fileList.getPath());
                }
            }

        }
    }
}
