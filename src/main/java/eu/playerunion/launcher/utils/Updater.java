package eu.playerunion.launcher.utils;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Updater {
	
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private File updateInfo = new File(FileUtil.getWorkingDirectory(), "updateInfo.json");
	private String repository = "https://files.playerunion.eu/";
	
	public void checkForRelease() {
		if(!this.checkMcFolder() || !this.updateInfo.exists())
			this.doSetup();
		
		System.out.println("[ DEBUG ] Launcher ellenőrzése...");
		
		
		this.checkLauncher();
	}
	
	private boolean checkMcFolder() {
		return FileUtil.getWorkingDirectory().exists();
	}
	
	private void checkLauncher() {
		System.out.println("[ DEBUG ] Legfrissebb build keresése...");
		
		HashMap<String, String> request = new HashMap<>();
		
		request.put("checksum", "launcher");
		
		String apiResponse = HttpUtil.executePost(this.repository, this.gson.toJson(request));
		JsonObject resp = this.gson.fromJson(apiResponse, JsonObject.class);
		
		UpdateInfo info = new UpdateInfo(resp.get("artifactId").getAsString(), resp.get("releaseDate").getAsString(), resp.get("checksum").getAsString());
		
		System.out.println("[ DEBUG ] Talált build: " + resp.get("releaseDate").getAsString());
		
		try {
			FileWriter fw = new FileWriter(this.updateInfo);
			
			fw.write(this.gson.toJson(info));
			
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		String downloadedFile = null;
		
		try {
			File file = new File(FileUtil.getWorkingDirectory().toString() + File.separator + resp.get("artifactId").getAsString());
			
			if(!file.exists()) {
				this.doSetup();
				
				return;
			}
			
			String f = file.toURL().toString();
			
			downloadedFile = FileUtil.generateChecksum(f);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		if(!info.getChecksum().equals(downloadedFile)) {
			System.out.println("[ DEBUG ] A fájl frissült, vagy módosították, ezért a launcher újra letölti azt.");
			
			this.doSetup();
			
			return;
		}
		
		System.out.println("[ DEBUG ] A fájl egyezik, módosítás nem történt.");
		
		// LAUNCH
	}
	
	private void doSetup() {
		FileUtil.getWorkingDirectory().mkdir();
		
		System.out.println("[ DEBUG ] Legfrissebb build keresése...");
		
		HashMap<String, String> request = new HashMap<>();
		
		request.put("checksum", "launcher");
		
		String apiResponse = HttpUtil.executePost(this.repository, this.gson.toJson(request));
		JsonObject resp = this.gson.fromJson(apiResponse, JsonObject.class);
		
		System.out.println("[ DEBUG ] Checksum: " + resp.get("checksum").getAsString());
		System.out.println("[ DEBUG ] Talált build: " + resp.get("releaseDate").getAsString());
		
		try {
			FileUtil.donwloadLauncher(this.repository, FileUtil.getWorkingDirectory().toString() + File.separator + resp.get("artifactId").getAsString());
			
			UpdateInfo info = new UpdateInfo(resp.get("artifactId").getAsString(), resp.get("releaseDate").getAsString(), resp.get("checksum").getAsString());
			FileWriter fw = new FileWriter(this.updateInfo);
			
			fw.write(this.gson.toJson(info));
			
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
