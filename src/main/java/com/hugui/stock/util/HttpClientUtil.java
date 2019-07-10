package com.hugui.stock.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Copyright © 2019 Obexx. All rights reserved.
 * 
 * @Title: HttpClientUtil.java
 * @Prject: obexx-im
 * @Package: com.obexx.im.util
 * @Description: HTTP工具类
 * @author: HuGui
 * @date: 2019年2月23日 下午3:29:32
 * @version: V1.0
 */

public class HttpClientUtil {

	private static Logger logger = LoggerFactory.getLogger("HttpClientUtil");

	private static final int TIMEOUT = 10 * 1000;

	private static volatile CloseableHttpClient httpClient = null;

	private static final Object syncLock = new Object();

	private static void config(HttpRequestBase httpRequestBase) {
		// 配置请求的超时设置
		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT)
				.setConnectTimeout(TIMEOUT).setSocketTimeout(TIMEOUT).build();
		httpRequestBase.setConfig(requestConfig);
	}

	/**
	 * 获取HttpClient对象
	 */
	public static CloseableHttpClient getHttpClient(String url) {
		String hostname = url.split("/")[2];
		int port = 80;
		if (hostname.contains(":")) {
			String[] arr = hostname.split(":");
			hostname = arr[0];
			port = Integer.parseInt(arr[1]);
		}
		if (httpClient == null) {
			synchronized (syncLock) {
				if (httpClient == null) {
					httpClient = createHttpClient(200, 40, 100, hostname, port);
				}
			}
		}
		return httpClient;
	}

	/**
	 * 创建HttpClient对象
	 */
	public static CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute, int maxRoute, String hostname,
			int port) {
		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
		LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", plainsf).register("https", sslsf).build();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		// 将最大连接数增加
		cm.setMaxTotal(maxTotal);
		// 将每个路由基础的连接增加
		cm.setDefaultMaxPerRoute(maxPerRoute);
		HttpHost httpHost = new HttpHost(hostname, port);
		// 将目标主机的最大连接数增加
		cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

		// 请求重试处理
		HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
			public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
				// 如果已经重试了5次，就放弃
				if (executionCount >= 5) {
					return false;
				}
				// 如果服务器丢掉了连接，那么就重试
				if (exception instanceof NoHttpResponseException) {
					return true;
				}
				// 不要重试SSL握手异常
				if (exception instanceof SSLHandshakeException) {
					return false;
				}
				// 超时
				if (exception instanceof InterruptedIOException) {
					return false;
				}
				// 目标服务器不可达
				if (exception instanceof UnknownHostException) {
					return false;
				}
				// 连接被拒绝
				if (exception instanceof ConnectTimeoutException) {
					return false;
				}
				// SSL握手异常
				if (exception instanceof SSLException) {
					return false;
				}

				HttpClientContext clientContext = HttpClientContext.adapt(context);
				HttpRequest request = clientContext.getRequest();
				// 如果请求是幂等的，就再次尝试
				return !(request instanceof HttpEntityEnclosingRequest);
			}
		};

		return HttpClients.custom().setConnectionManager(cm).setRetryHandler(httpRequestRetryHandler).build();
	}

	private static void setPostParams(HttpPost httpost, Map<String, Object> params, Map<String, Object> headers) {
		List<NameValuePair> nvps = new ArrayList<>();
		Set<String> keySet = params.keySet();
		for (String key : keySet) {
			nvps.add(new BasicNameValuePair(key, params.get(key).toString()));
		}
		try {
			httpost.setHeader("Content-Type", "application/json");
			httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));

			if (headers != null) {
				for (Iterator<String> iterator = headers.keySet().iterator(); iterator.hasNext();) {
					String key = iterator.next();
					httpost.setHeader(key, String.valueOf(headers.get(key)));
				}
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String post(String url, Map<String, Object> params) throws IOException {
		return post(url, params, null);
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String post(String url, Map<String, Object> params, Map<String, Object> headers) throws IOException {
		HttpPost httppost = new HttpPost(url);
		config(httppost);
		setPostParams(httppost, params, headers);
		CloseableHttpResponse response = null;
		try {
			response = getHttpClient(url).execute(httppost, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw e;
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * GET请求URL获取内容
	 * 
	 * @param url
	 * @return
	 */
	public static String get(String url) {
		HttpGet httpget = new HttpGet(url);
		config(httpget);
		CloseableHttpResponse response = null;
		try {
			response = getHttpClient(url).execute(httpget, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, "utf-8");
			EntityUtils.consume(entity);
			return result;
		} catch (IOException e) {
			logger.error(e.getMessage());
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return null;
	}
}
