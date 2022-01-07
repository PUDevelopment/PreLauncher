package eu.playerunion.launcher;

import javax.swing.JOptionPane;

import eu.playerunion.launcher.utils.Updater;

public class PreLauncher {
	
	public static void main(String[] args) {
		System.out.println("[ DEBUG ] PreLauncher inicializálása...\n");
		
		String javaVendor = System.getProperty("sun.arch.data.model");
    	
    	if(javaVendor.equals("32")) {
    		System.out.println("[ DEBUG ] Nem megfelelő (32 bites) Java detektálva, a PreLauncher bezárja önmagát...");
    		
    		JOptionPane.showMessageDialog(null, "A kliens futtatásához 64 bites Java szükséges!\nTöltsd le a Java 8-as (64 bit) verzióját a https://www.java.com oldalról!", "Java verzió nem megfelelő", JOptionPane.WARNING_MESSAGE);
    		
    		System.exit(0);
    	}
		
		new Updater().checkForRelease();
	}
	
}