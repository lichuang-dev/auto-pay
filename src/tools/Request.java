
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class Request {
	public Request() {}

	public String sendGet(String urlNameString, JSONObject proxy, JSONArray header1) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String result = "";
		//设置代理
		HttpHost httpHost;
		String hostName;
		String port;
		HttpGet httpGet;
		if(!proxy.isEmpty()) {
			hostName=proxy.getString("host");
			port = proxy.getString("port");
			httpHost = new HttpHost(hostName, Integer.parseInt(port),"http");
			RequestConfig config = RequestConfig.custom().setConnectTimeout(10000).setConnectionRequestTimeout(10000).setSocketTimeout(10000).setProxy(httpHost).build();
			httpGet = new HttpGet(urlNameString);
			httpGet.setConfig(config);
		} else {
			httpGet = new HttpGet(urlNameString);
		}

		if(header1.size()==0) {
			httpGet.setHeader("Accept","*/*");
			httpGet.setHeader("Accept-Encoding","gzip, deflate, br");
			httpGet.setHeader("Accept-Language","zh-CN,zh;q=0.9,en;q=0.8");
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");
		} else {
			for (int i = 0; i < header1.size(); i++) {
				JSONObject header = header1.getJSONObject(i);
				httpGet.setHeader(header.getString("key"),header.getString("value"));
			}
		}


		CloseableHttpResponse response = null;
		try {
			response = httpClient.execute(httpGet);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public String  doPost(String url,String params) {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost(url);// 创建httpPost
		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-Type", "application/json");
		String charSet = "UTF-8";
		StringEntity entity = new StringEntity(params, charSet);
		httpPost.setEntity(entity);
		CloseableHttpResponse response = null;

		try {
			try {
				response = httpclient.execute(httpPost);
			} catch (IOException e) {
				e.printStackTrace();
			}
			StatusLine status = response.getStatusLine();
			int state = status.getStatusCode();
			if (state == HttpStatus.SC_OK) {
				HttpEntity responseEntity = response.getEntity();
				String jsonString = null;
				try {
					jsonString = EntityUtils.toString(responseEntity);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return jsonString;
			}
			else{
				System.out.println("请求返回:"+state+"("+url+")");
			}
		}finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return "";
	}

}
