import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.apache.bcel.generic.IF_ACMPEQ;
import org.apache.commons.io.FileUtils;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class AutoPay {
    public WebDriver webDriver;
    public tools.Log logger;
    public static String baseFilePath="C:/Users/lichuang/Desktop/ota-pay";
    public static int defaultSleep=5;
//    public static String responseUrl="http://nirvana.test.ganhuoche.com/api/client/receive/pay";
//    public static String payUrl="http://nirvana.test.ganhuoche.com/api/client/get/to/pay/html";
//    public static String getOrderUrl="http://nirvana.test.ganhuoche.com/api/client/get/to/pay";

    public static String responseUrl="http://nirvana.ganhuoche.com/api/client/receive/pay";
    public static String payUrl="http://nirvana.ganhuoche.com/api/client/get/to/pay/html";
    public static String getOrderUrl="http://nirvana.ganhuoche.com/api/client/get/to/pay";
    public static void main(String[] args) {
        new AutoPay().start();
    }

    //获取待支付订单  - 去支付 - 提交支付结果 - 休眠 - 重复
    private void start()
    {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).toString();
        String path=baseFilePath+"/logs/"+date+"/ota_pay.log";
        logger = tools.Log.getLogger(path);
        logger.info("java crontab start");
        openBrowser();
        //打开初始化页面
        openPage("http://www.baidu.com");

        while (true)
            checkOrder();
    }

    private void checkOrder()
    {
        //获取订单
        JSONObject payOrderInfo = getWaitPayOrder();

        int sleep = defaultSleep;

        if (payOrderInfo != null) {
            logger.info("获取订单");
            logger.info(payOrderInfo.toJSONString());
            //验证订单是否可以去支付
            String status = payOrderInfo.getString("status");
            String dataString = payOrderInfo.getString("data");
            JSONObject data = JSONObject.parseObject(dataString);

            if (status != null && status.equals("200")) {
                // 走去只支付流程
                Object otaPay = data.getString("otaPay");
                if (otaPay != null) {
                    logger.info("银联支付");
                    yinLian(data);
                } else {
                    logger.info("支付宝支付");
                }
            }
            sleep = data.getIntValue("sleep");
            if (sleep == 0) {
                sleep = 5;
            }
        }
        logger.info("休眠"+sleep+"秒");
        sleep(sleep*1000);
    }


    //打开浏览器
    private WebDriver openBrowser()
    {
        try {
            if (webDriver==null) {
                System.getProperties().setProperty("webdriver.chrome.driver", "drivers/chromedriver_windows.exe");
                webDriver = new ChromeDriver();
                webDriver.manage().window().maximize();
            }
            return webDriver;
        }catch (WebDriverException e){
            dump(e.toString());
            throw new WebDriverException("浏览器打开异常");
            //日志 记录异常 相应后台
        }
    }

    //打开页面
    private void openPage(String url)
    {
        webDriver.get(url);
        DD.INSTANCE.DD_key(313,1);
        DD.INSTANCE.DD_key(313,2);
    }

    //银联支付
    private void yinLian(JSONObject data)
    {

        String orderId = data.getString("orderid");
        String otaPayStr = data.getString("otaPay");
        JSONObject otaPay = JSONObject.parseObject(otaPayStr);
        JsonObject responseData = new JsonObject();
        String getewayid = data.getString("getewayid");
        responseData.addProperty("getewayid",getewayid);
        responseData.addProperty("orderid",orderId);
        responseData.addProperty("pay_order_id",otaPay.getString("payOrderId"));
        responseData.addProperty("ota_pay_id",otaPay.getString("payOrderId"));
        responseData.addProperty("access_12306_time",0.2);
        responseData.addProperty("proxyIP","");
        responseData.addProperty("balance",0);
        responseData.addProperty("bankorderid","");
        responseData.addProperty("aliuser",otaPay.getString("bankCardNo"));

        //最初始句柄
        String currentWindow = webDriver.getWindowHandle();
        try{
            logger.info("====================银联支付 START==========================");
            logger.info("orderId:"+orderId);
            //新建页面
            openNewPage();
            sleep(100);
            //句柄切换到新标签 并打开页面
            switchHandle();
            //打开链接
            openPage(payUrl+"?getewayid="+getewayid);
            sleep(2000);
            String title = webDriver.getTitle();

            screenshot();

            //判断是否在支付页面
            if (!title.equals("银联在线支付-银行卡综合性网上交易转接清算平台！")) {
                logger.info("支付失败，TITLE 不一致，"+title);
                throw new Exception("支付失败");
                //响应支付失败，表单错误
            }

            boolean isCheck = isJudgingElement(By.id("cardNumber"));
            if (!isCheck) {
                logger.info("支付失败，未找到输入卡号的输入框");
                throw new Exception("支付失败，FORM 表单提交失败");
            }
            logger.info("输入卡号："+otaPay.getString("bankCardNo"));
            webDriver.findElement(By.id("cardNumber")).click();
            DD.INSTANCE.DD_str(otaPay.getString("bankCardNo"));
            screenshot();
            //等待页面跳转
            sleep(1000);
            logger.info("点击下一步");
            webDriver.findElement(By.id("btnNext")).click();
            //判断是否需要添加支付密码
            boolean isOcxPassword=isJudgingElement(By.id("_ocx_password"));
            if (isOcxPassword) {
                logger.info("输入支付密码");
                sleep(1000);
                webDriver.findElement(By.id("_ocx_password")).click();
                DD.INSTANCE.DD_str("1234561");
            }
            sleep(1000);
            logger.info("发送短信验证码");
            webDriver.findElement(By.id("btnGetCode")).click();
            sleep(2000);
            webDriver.findElement(By.id("smsCode")).click();
            logger.info("输入短信验证码："+otaPay.getString("verifyCode"));
            DD.INSTANCE.DD_str(otaPay.getString("verifyCode"));
            sleep(1000);
            //下一步确认付款
            logger.info("点击确认付款");
            webDriver.findElement(By.id("btnCardPay")).click();
            sleep(5000);
            //跳转完成后获取支付情况
            JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
            String get_sub_word_js = "return $('.sub_word').text();";
            String get_sub_word = (String) jsExecutor.executeScript(get_sub_word_js);
            logger.info(get_sub_word);

            screenshot();
            if (get_sub_word.indexOf("失败") != -1) {
                throw new Exception(get_sub_word);
            }

            String get_main_word_js = "return $('.main_word').text()";
            String get_main_word = (String) jsExecutor.executeScript(get_main_word_js);

            logger.info(get_main_word);
            if (get_main_word.indexOf("已成功支付") == -1) {
                logger.info("支付成功");
                responseData.addProperty("info","支付成功");
                responseData.addProperty("status",200);
                responseMsg(responseData.toString(),0);
            }
        }catch (Exception e) {
            logger.error("支付失败"+e);
            //通知 后台支付失败
            String message = e.getMessage();
            responseData.addProperty("info",message);
            responseData.addProperty("status",101);
            responseMsg(responseData.toString(),0);
        }finally {
            logger.info("关闭当前标签");
            closeAndSwitchHandle(currentWindow);
            logger.info("====================银联支付 END==========================");
        }
    }

    //关闭不等于handle的窗口
    private void closeAndSwitchHandle(String handle)
    {
        for (String handles:webDriver.getWindowHandles()) {
            if (handles.equals(handle)) {
                continue;
            }
            webDriver.close();
        }
        webDriver.switchTo().window(handle);
    }


    //响应当前订单处理情况
    private void responseMsg(String data,int i)
    {
        logger.info("Response:"+data);
        if (i<=2){
            Request request = new Request();
            String result = request.doPost(responseUrl, data);
            logger.info("Result:"+result);
            if (!result.equals("SUCCESS")) {
                i=i+1;
                responseMsg(data,i);
            }
        }
    }

    // 支付宝支付
    private void aliPay()
    {
        System.getProperties().setProperty("webdriver.chrome.driver", "drivers/chromedriver_windows.exe");
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        WebDriver webDriver = new ChromeDriver(options);
        webDriver.get("https://authsu18.alipay.com/login/option.htm");
        sleep(4000);
        String accout = "huoche665@quyuanyou.com";
        String loginPassword="youayou@123";
        webDriver.findElement(By.id("logonId")).click();
        DD.INSTANCE.DD_str(accout);
        sleep(1000);
        webDriver.findElement(By.id("password_rsainput")).click();
        DD.INSTANCE.DD_str(loginPassword);
        sleep(1000);
    }

    private void switchHandle()
    {
        //获取当前页面句柄
        String handle = webDriver.getWindowHandle();
        //获取所有句柄，循环判断是否等于当前句柄
        for (String handles:webDriver.getWindowHandles()) {
            if (handles.equals(handle))
                continue;
            webDriver.switchTo().window(handles);
        }
    }

    //打开新的标签页面
    private void openNewPage(){
        DD.INSTANCE.DD_key(600,1);
        DD.INSTANCE.DD_key(305,1);
        DD.INSTANCE.DD_key(600,2);
        DD.INSTANCE.DD_key(305,2);
    }

    //DD 输入类  虚拟
    public interface DD extends Library
    {
        DD INSTANCE = (DD) Native.loadLibrary("DD94687.64", DD.class);
        public int DD_mov(int x, int y);
        public int DD_movR(int dx, int dy);
        public int DD_btn(int btn);
        public int DD_whl(int whl);
        public int DD_key(int ddcode, int flag);
        public int DD_str(String s);
    }

    private void sleep(int number)
    {
        try {
            Thread.sleep(number);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getWaitPayOrder()
    {
        JSONObject payOrderInfo = new JSONObject();
        try{
            //请求后台 获取 订单
            Request request = new Request();
            String urlNameString = getOrderUrl;
            String result = request.sendGet(urlNameString,new JSONObject(),new JSONArray());
//            System.out.print(result);
//        String result = "{\"status\":200,\"info\":\"\\u6210\\u529f\",\"data\":{\"getewayid\":862,\"content\":\"\",\"orderid\":\"PDD1911271504328534\",\"ip\":\"122.247.91.122:9001\",\"source\":\"lvmama\",\"canGetTime\":\"2019-09-18 18:52:21\",\"host\":\"118.190.138.73\",\"aliuser\":\"newpay094@ganhuoche.com\",\"alipwd\":\"ghc&ghc...2012\",\"tab\":\"1\",\"status\":\"1\",\"maxprice\":2147483647,\"payType\":0,\"username\":\"123123213\",\"passwd\":\"123123123\",\"ticketno\":\"EH57606882\",\"traincode\":\"k10086\",\"fromstation\":\"\\u5317\\u4eac\",\"tostation\":\"\\u4e0a\\u6d77\",\"otaPay\":{\"bankCardNo\":\"2892000004003932548\",\"payOrderId\":\"PDD1911271504328534111\",\"verifyCode\":\"447111\",\"activeTime\":\"2019-09-18 19:22:54\",\"orderId\":\"PDD1911271504328534\"},\"sleep\":10}}";
            payOrderInfo = JSONObject.parseObject(result);
        }catch (Exception e) {
            logger.info("获取订单解析失败");
            logger.error("获取订单失败"+e);
        }finally {
            return payOrderInfo;
        }

    }

    //验证元素是否存在
    private boolean isJudgingElement(By by)
    {
        try {
            webDriver.findElement(by);
            return  true;
        }catch (Exception e) {
            return false;
        }
    }

    private void dump(String content)
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
        System.out.print(content);
        System.out.print("\r\n");
    }

    private void screenshot()
    {
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");//设置日期格式
        //执行屏幕截图操作
        File srcFile = ((TakesScreenshot)webDriver).getScreenshotAs(OutputType.FILE);
        System.out.print(baseFilePath+df.format(new Date())+".jpg");
        try {
            FileUtils.copyFile(srcFile, new File(baseFilePath+"/img/"+df.format(new Date())+".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("截图ERROR"+e);
        }
    }

}
