package addressextraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Target {
	private File file;
	private Map<String, ArrayList<String>> map;

	public Target(String fileName) {
		this.file = new File(fileName);
		map = new HashMap<String, ArrayList<String>>();
		read();
	}

	public void read() {
		Scanner sc;
		try {
			sc = new Scanner(file);
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.split(": ")[0].contains("ADDRESS") || line.split(": ")[0].contains("ZIP") || line.split(": ")[0].contains("CITY")
						|| line.split(": ")[0].contains("STATE")) {
					ArrayList<String> targetWords = new ArrayList<String>();
					String context = line.split("_")[0];
					if (map.keySet().contains(context)) {
						targetWords = map.get(context);
					}
					if(line.split(": ").length>1){
						String[] address = line.split(": ")[1].split(" ");
						for (String word : address) {
							targetWords.add(word);
						}
						map.put(context, targetWords);
					}
				}
			}
			sc.close();
			// sc = new Scanner(file);
			// while(sc.hasNextLine()){
			// String line = sc.nextLine();
			// if(line.contains("ZIP")
			// ||line.contains("CITY")
			// ||line.contains("STATE")){
			// String[] address = line.split(": ")[1].split(" ");
			// String key = line.split("_")[0];
			// ArrayList<String> targetWords = map.get(key);
			// for(String word:address){
			// targetWords.add(word);
			// }
			// map.put(key, targetWords);
			// }
			// }
			// sc.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Map<String, ArrayList<String>> getTargetMap() {
		return map;
	}
}
