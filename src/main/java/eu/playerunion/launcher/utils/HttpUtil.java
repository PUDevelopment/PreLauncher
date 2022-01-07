package eu.playerunion.launcher.utils;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
	
	public static @NonNull String executePost(String url, String post) {
		OkHttpClient httpClient = new OkHttpClient();
		RequestBody body = RequestBody.create(MediaType.get("application/json; charset=utf-8"), post);
		
		Request request = new Request.Builder()
				.url(url)
				.post(body)
				.build();
		
		try (Response response = httpClient.newCall(request).execute()) {
			String resp = response.body().string();
			
			return resp;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static @NonNull String executeGet(String url, String get) {
		OkHttpClient httpClient = new OkHttpClient();
		Request request = new Request.Builder()
				.url(url)
				.get()
				.build();
		
		try (Response response = httpClient.newCall(request).execute()) {
			String resp = response.body().string();
			
			return resp;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}

}
