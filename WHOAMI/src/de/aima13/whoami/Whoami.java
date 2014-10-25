package de.aima13.whoami;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by D060469 on 16.10.14.
 */
public class Whoami implements  Runnable{
	private static final int ANALYZE_TIME = 60; // Analysezeit in Sekunden
	public static final int PERCENT_FOR_FILE_SEARCHER = 75; // Wie viel Prozent für den
	// FileSearcher?
	private static long startTime;

	public static void main(String[] args) {
		startTime = System.currentTimeMillis();
			// Gui starten und AGB zur Bestätigung anzeigen
		GuiManager.startGui();
		if (!GuiManager.confirmAgb()) {
			// Beenden des Programms, falls der User die AGB ablehnt
			System.exit(0);
		}
	}

	/**
	 * Information über die bisherige und restliche Laufzeit des Programms
	 * @return Ganzzahliger Prozentwert zwischen 0 und 100 (100: Zeit ist um)
	 */
	public static int getTimeProgress() {
		float elapsedTime = (float) ((System.currentTimeMillis() - startTime) / 1000);
		int timeProgress = (int) (elapsedTime / ANALYZE_TIME * 100);

		if (timeProgress < 100) {
			return timeProgress;
		} else {
			return 100;
		}
	}

	@Override
	public void run() {
		System.out.println("\n\nIch runne mal die Module");
		List<Analyzable> moduleList = new ArrayList<>();                // Liste der Module
		List<Representable> representableList = new ArrayList<>();      // Liste der Representables

		GuiManager.updateProgress("Lade und initialisiere Module...");
		/**
		 * @todo Errorhandling
		 */
		try {
			moduleList = ModuleManager.getModuleList();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		GuiManager.updateProgress("Scanne Dateisystem...");
		FileSearcher.startSearch(moduleList);

		// Instanz der Singletonklasse GlobalData holen
		GlobalData globalData = GlobalData.getInstance();

		GuiManager.updateProgress("Analysiere gefundene Dateien...");
		SlaveDriver.startModules(moduleList);

		// Stelle persönliche Daten an den Anfang der Liste
		representableList = new ArrayList<>();
		representableList.add(globalData);
		representableList.addAll(moduleList);

		// Starte Speichervorgang

		// CSV
		CsvCreator.saveCsv(representableList);

		// PDF
		ReportCreator reportCreator = new ReportCreator(representableList);
		try {
			reportCreator.savePdf();
		} catch (Exception e) {
			/**
			 * @todo Errorhandling Report-Creator
			 */
			e.printStackTrace();
		}
		GuiManager.updateProgress("Bin fertig :)");

		GuiManager.closeAnalysisProgess();
		// Anzeigen des Berichtes
		GuiManager.showReport(reportCreator.getHtml());
	}
}
