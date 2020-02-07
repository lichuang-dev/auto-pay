package tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Function {

    public static Random random;

    public static String nowDate(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String nowDate =  df.format(new Date());
        return  nowDate;
    }

    public static void getRandomObj(){
        if(random == null) random = new Random();
    }

    // 把ResultSet集合转换成JsonArray数组
    public static JSONArray formatRsToJsonArray(ResultSet rs){
        ResultSetMetaData md= null;
        JSONArray array=new JSONArray();
        try {
            md = rs.getMetaData();
            int num=md.getColumnCount();
            while(rs.next()){
                JSONObject mapOfColValues=new JSONObject();
                for(int i=1;i<=num;i++){
                    mapOfColValues.put(md.getColumnName(i), rs.getObject(i));
                }
                array.add(mapOfColValues);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return array;
    }

    public static int getRandom(int startInt, int endStart) {
        //创建Random类对象
        getRandomObj();
        //产生随机数
        int number = random.nextInt(endStart - startInt + 1) + startInt;
        return  number;
    }

    public static  String EncoderByMd5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            return "";
        }
    }




}
