package eu.playerunion.launcher.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FileUtil {
	
	private static File workDir;
	private static Gson gson = new GsonBuilder().create();
	
	public static String generateChecksum(String url) {
		try {
			return new String(DigestUtils.md5Hex(new URL(url).openStream()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static void donwloadLauncher(String url, String path) throws Exception {
		System.out.println("[ DEBUG ] A rendszer megkísérli a kliens letöltését...");
		
		HashMap<String, String> request = new HashMap<>();
		
		request.put("artifact", "launcher");
		
		String apiResponse = HttpUtil.executePost(url, gson.toJson(request));
		
		FileOutputStream fos = new FileOutputStream(path);
		byte[] bytes = apiResponse.getBytes();
		String originalFile = DigestUtils.md5Hex(bytes);
		
		fos.write(bytes);
		
		String downloadedFile = DigestUtils.md5Hex(new File(path).toURL().openStream());
		
		System.out.println("[ DEBUG ] Fájl sikeresen letöltve!");
		System.out.println("[ DEBUG ] Fájl ellenőrzése...");
		System.out.println("[ DEBUG ] Checksum: " + originalFile + " => " + downloadedFile);
		
		if(!originalFile.equals(downloadedFile)) {
			System.out.println("[ DEBUG ] A fájl sérült, vagy módosult! Újrapróbálkozás...");
			
			donwloadLauncher(url, path); // Launcher letöltésének megkísérlése újból
			
			return;
		}
		
		System.out.println("[ DEBUG ] A fájl ellenőrzése sikeres volt, a fájl rendben van!");
	}
	
	public static OS getPlatform() {
		String osName = System.getProperty("os.name").toLowerCase();
		
		if(osName.contains("windows"))
			return OS.Windows;
		
		if(osName.contains("linux"))
			return OS.Linux;
		
		if(osName.contains("mac"))
			return OS.MacOS;
		
		return osName.contains("unix") ? OS.Linux : OS.Unknown;
	}
	
	public static File getWorkingDirectory() {
	    if (workDir == null)
	    	workDir = getWorkingDirectory("playerunion");

	    return workDir;
	}
	
	public static File getWorkingDirectory(String appName) {
		String userHome = System.getProperty("user.home", ".");
		File workingDirectory = null;
		
		switch(getPlatform()) {
		case Linux:
			workingDirectory = new File(userHome, '.' + appName + '/');
			
	        break;
	    
		case Windows:
			String appData = System.getenv("APPDATA");
			
			if(appData != null)
				workingDirectory = new File(appData, "." + appName + '/');
			else
				workingDirectory = new File(userHome, '.' + appName + '/');
			
			break;
			
		case MacOS:
			workingDirectory = new File(userHome, "Library/Application Support/" + appName);
			
			break;
		}
		
		if (!workingDirectory.exists() && !workingDirectory.mkdirs()) 
	         throw new RuntimeException("[ HIBA ] Nem sikerült létrehozni a játék gyökérkönyvtárát: " + workingDirectory);
		
		System.out.println("[ DEBUG ] A kliens mappája: " + workingDirectory);
		
		return workingDirectory;
	}

}
