package eu.playerunion.launcher;

import javax.swing.JOptionPane;

import eu.playerunion.launcher.utils.Updater;

public class PreLauncher {
	
	public static void main(String[] args) {
		System.out.println("[ DEBUG ] PreLauncher inicializálása...\n");
		System.out.println("[ DEBUG ] Java vendor ellenőrzése...");
		
		String javaVendor = System.getProperty("sun.arch.data.model");
    	
    	if(javaVendor.equals("32")) {
    		System.out.println("[ DEBUG ] Nem megfelelő (32 bites) Java detektálva, a PreLauncher bezárja önmagát...");
    		
    		JOptionPane.showConfirmDialog(null, "Nem megfelelő Java kiadást (32 bit) találtunk a gépeden!\n"
    				+ "A kliens futtatásához 64 bitesre lesz szükséged a nagy memória igény miatt!\n"
    				+ "Kérlek, töltsd le a Java 8-as verzió 64 bites kiadását!\n"
    				+ "Letöltési oldal: https://java.com", "Nem megfelelő Java verzió", JOptionPane.CLOSED_OPTION);
    		
    		System.exit(0);
    	}
		
		new Updater().checkForRelease();
	}
	
}