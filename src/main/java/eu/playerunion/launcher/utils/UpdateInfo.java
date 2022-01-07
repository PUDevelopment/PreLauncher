package eu.playerunion.launcher.utils;

public class UpdateInfo {
	
	private String artifactId;
	private String releaseDate;
	private String checksum;
	
	public UpdateInfo(String artifactId, String releaseDate, String checksum) {
		this.artifactId = artifactId;
		this.releaseDate = releaseDate;
		this.checksum = checksum;
	}
	
	public String getArtifactId() {
		return this.artifactId;
	}
	
	public String getReleaseDate() {
		return this.releaseDate;
	}
	
	public String getChecksum() {
		return this.checksum;
	}

}
