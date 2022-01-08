package eu.playerunion.launcher.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Updater {
	
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private File updateInfo = new File(FileUtil.getWorkingDirectory(), "updateInfo.json");
	private String repository = "https://jenkins.playerunion.eu/job/ferrum/lastStableBuild/";
	
	private int attempts = 0;
	
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
		System.out.println("[ DEBUG ] Legfrissebb stabil build keresése...");
		
		String apiResponse = HttpUtil.executeGet(this.repository + "api/json", null);
		JsonObject resp = this.gson.fromJson(apiResponse, JsonObject.class);
		String artifactId = resp.get("artifacts").getAsJsonArray().get(0).getAsJsonObject().get("fileName").getAsString();
		String artifactPath = resp.get("artifacts").getAsJsonArray().get(0).getAsJsonObject().get("relativePath").getAsString();
		String buildNumber = resp.get("id").getAsString();
		String checksum = FileUtil.generateChecksum(this.repository + "artifact/" + artifactPath);
		Timestamp stamp = new Timestamp(Long.parseLong(resp.get("timestamp").getAsString()));
		Date date = new Date(stamp.getTime());
		
		System.out.println("[ DEBUG ] Talált utolsó sikeres build: #" + buildNumber + " (" + date.toLocaleString() + ")");
		System.out.println("[ DEBUG ] Checksum: " + checksum);
		
		UpdateInfo info = new UpdateInfo(artifactId, date.toLocaleString(), checksum);
		
		try {
			FileWriter fw = new FileWriter(this.updateInfo);
			
			fw.write(this.gson.toJson(info));
			
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		try {
			File file = new File(FileUtil.getWorkingDirectory().toString() + File.separator + artifactId);
			String downloadedFile = DigestUtils.md5Hex(new FileInputStream(file));
			
			if(!info.getChecksum().equals(downloadedFile) || !file.exists()) {
				System.out.println("[ DEBUG ] A fájl frissült, vagy módosították, ezért a launcher újra letölti azt.");
				
				this.donwloadLauncher(this.repository + "/artifact/" + artifactPath, FileUtil.getWorkingDirectory().toString() + File.separator + artifactId);
				
				return;
			}
			
			System.out.println("[ DEBUG ] A fájl egyezik, módosítás nem történt.");
			
			// Launcher elindítása
		} catch (Exception e) {
			e.printStackTrace();
			
			this.attempts++;
			
			if(this.attempts == 5) {
				JOptionPane.showConfirmDialog(null, "Nem sikerült a launcher telepítése és ellenőrzése!\n"
						+ "Kérlek, próbáld meg később, vagy vedd fel velünk a kapcsolatot!\n"
						+ "Discord: https://dc.playerunion.eu\n"
						+ "Email: admin@playerunion.eu", "Sikertelen telepítés", JOptionPane.CLOSED_OPTION);
				
				System.gc();
				System.exit(0);
			}
			
			this.doSetup(); // Próbálja meg telepíteni a biztonság kedvéért. Legfeljebb 5x próbálkozik, majd kibreakel.
			
			this.checkForRelease();
		}
	}
	
	private void doSetup() {
		FileUtil.getWorkingDirectory().mkdir();
		
		System.out.println("[ DEBUG ] Legfrissebb stabil build keresése...");
		
		String apiResponse = HttpUtil.executeGet(this.repository + "api/json", null);
		JsonObject resp = this.gson.fromJson(apiResponse, JsonObject.class);
		String artifactId = resp.get("artifacts").getAsJsonArray().get(0).getAsJsonObject().get("fileName").getAsString();
		String artifactPath = resp.get("artifacts").getAsJsonArray().get(0).getAsJsonObject().get("relativePath").getAsString();
		String buildNumber = resp.get("id").getAsString();
		String checksum = FileUtil.generateChecksum(this.repository + "artifact/" + artifactPath);
		Timestamp stamp = new Timestamp(Long.parseLong(resp.get("timestamp").getAsString()));
		Date date = new Date(stamp.getTime());
		
		System.out.println("[ DEBUG ] Talált utolsó sikeres build: #" + buildNumber + " (" + date.toLocaleString() + ")");
		System.out.println("[ DEBUG ] Checksum: " + checksum);
		
		try {
			this.donwloadLauncher(this.repository + "/artifact/" + artifactPath, FileUtil.getWorkingDirectory().toString() + File.separator + artifactId);
			
			UpdateInfo info = new UpdateInfo(artifactId, date.toLocaleString(), checksum);
			FileWriter fw = new FileWriter(this.updateInfo);
			
			fw.write(this.gson.toJson(info));
			
			fw.close();
		} catch(Exception e) {
			e.printStackTrace();
			
			JOptionPane.showConfirmDialog(null, "Nem sikerült a launcher telepítése és ellenőrzése!\n"
					+ "Kérlek, próbáld meg később, vagy vedd fel velünk a kapcsolatot!\n"
					+ "Discord: https://dc.playerunion.eu\n"
					+ "Email: admin@playerunion.eu", "Sikertelen telepítés", JOptionPane.CLOSED_OPTION);
			
			System.gc();
			System.exit(0);
		}
	}
	
	public void donwloadLauncher(String url, String path) throws Exception {
		System.out.println("[ DEBUG ] A rendszer megkísérli a kliens letöltését...");
		
		OkHttpClient httpClient = new OkHttpClient();
		Request request = new Request.Builder()
				.url(url)
				.get()
				.build();
		
		try(Response response = httpClient.newCall(request).execute()) {
			FileOutputStream fos = new FileOutputStream(path);
			byte[] bytes = response.body().bytes();
			String originalFile = DigestUtils.md5Hex(bytes);
			
			fos.write(bytes);
			
			File file = new File(path);
			String downloadedFile = DigestUtils.md5Hex(new FileInputStream(file));
			
			System.out.println("[ DEBUG ] Fájl sikeresen letöltve!");
			System.out.println("[ DEBUG ] Fájl ellenőrzése...");
			System.out.println("[ DEBUG ] Checksum: " + originalFile + " => " + downloadedFile);
			
			if(!originalFile.equals(downloadedFile)) {
				System.out.println("[ DEBUG ] A fájl sérült, vagy módosult! Újrapróbálkozás...");
				
				donwloadLauncher(url, path); // Launcher letöltésének megkísérlése újból
				
				return;
			}
			
			System.out.println("[ DEBUG ] A fájl ellenőrzése sikeres volt, a fájl rendben van!");
		} catch(Exception e) {
			e.printStackTrace();
			
			JOptionPane.showConfirmDialog(null, "Nem sikerült a launcher telepítése és ellenőrzése!\n"
					+ "Kérlek, próbáld meg később, vagy vedd fel velünk a kapcsolatot!\n"
					+ "Discord: https://dc.playerunion.eu\n"
					+ "Email: admin@playerunion.eu", "Sikertelen telepítés", JOptionPane.CLOSED_OPTION);
			
			System.gc();
			System.exit(0);
		}
	}

}
