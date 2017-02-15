package namedentityextraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
//import org.dom4j.Node;
//import org.dom4j.io.SAXReader;
//import org.dom4j.xpath.DefaultXPath;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//get hocr and txt file
		String inputTiffName = "3070694_10_single.tif";
		String folderPath = "/Users/daw/Documents/heavywater/ScoreFunction";
		String inputTiff = folderPath + "/inputTiff/" + inputTiffName;
		String outputHocrName = inputTiffName.replace(".tif", "");
		String hocrFile = folderPath + "/outputHocr/" + outputHocrName;
		String outputTxtName = inputTiffName.replace(".tif", "");
		String txtFile = folderPath + "/outputHocr/" + outputTxtName;
		String command1 = "/usr/local/Cellar/tesseract/3.04.01_1/bin/tesseract " + inputTiff + " " + hocrFile+" hocr";
		String command2 = "/usr/local/Cellar/tesseract/3.04.01_1/bin/tesseract " + inputTiff + " " + txtFile;
//		System.out.println(command1);
//		System.out.println(command2);
		Process p;
		try {
			p = Runtime.getRuntime().exec(command1);
			p = Runtime.getRuntime().exec(command2);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//get txt output content
		String txtOutput = "";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(txtFile+".txt"));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			txtOutput = sb.toString();
			br.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
//		System.out.println(txtOutput);
		//using Standford NER to get supposed names
		ArrayList<String> names = new ArrayList<String>();
//		ArrayList<String> address = new ArrayList<String>();
		AbstractSequenceClassifier<CoreLabel> classifier;
		String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
		try {
			classifier = CRFClassifier.getClassifier(serializedClassifier);
			String[] example = txtOutput.split("\n");
			for (String str : example) {
				str = str.replaceAll("& ", "&amp; ");
				str = str.replaceAll( "&([^;]+(?!(?:\\w|;)))", "&amp;$1" );
				str = str.replaceAll("<", ". &lt;");
				str = str.replaceAll("\"", "&quot;");
				str = str.replaceAll("'","&apos;");
				str = str.replaceAll(">", ". &gt;");
				String xml = "<root>" + classifier.classifyToString(str, "xml", true) + "</root>";
				Document doc = DocumentHelper.parseText(xml);
				Element rootElt = doc.getRootElement();
				@SuppressWarnings("rawtypes")
				Iterator iter = rootElt.elementIterator("wi");
				while (iter.hasNext()) {
					Element recordEle = (Element) iter.next();
					if (recordEle.attributeValue("entity").equals("PERSON")) {
						names.add(recordEle.getText());
					}
				}
			}
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(names.toString());
		//get termnology dictionary words
		DictionaryReader dictionary = new DictionaryReader(
				"/Users/daw/Documents/heavywater/ScoreFunction/terminology_dictionary.txt");
		ArrayList<String> dicWords = new ArrayList<String>();
		dicWords = dictionary.getWords();
		
		//parse hocr file
		ArrayList<String> content = new ArrayList<>();
		ArrayList<Double> x0List = new ArrayList<>();
		ArrayList<Double> y0List = new ArrayList<>();
		ArrayList<Double> x1List = new ArrayList<>();
		ArrayList<Double> y1List = new ArrayList<>();
		ArrayList<Double> centerXList = new ArrayList<>();
		ArrayList<Double> centerYList = new ArrayList<>();
		File hocr = new File(hocrFile+".hocr");
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			dbFactory.setNamespaceAware(false);
			dbFactory.setValidating(false);
			dbFactory.setFeature("http://xml.org/sax/features/namespaces", false);
			dbFactory.setFeature("http://xml.org/sax/features/validation", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(hocr);
			doc.getDocumentElement().normalize();
			NodeList nList = doc.getElementsByTagName("span");
			for(int i = 0;i < nList.getLength();i++){
				org.w3c.dom.Node node = nList.item(i);
				org.w3c.dom.Element ele = (org.w3c.dom.Element) node;
				if(ele.getAttribute("class").equals("ocrx_word")){
					content.add(ele.getTextContent());
					String[] coordinate = ele.getAttribute("title").split(" ");
					double tempX0 = Double.parseDouble(coordinate[1]);
					double tempY0 = Double.parseDouble(coordinate[2]);
					double tempX1 = Double.parseDouble(coordinate[3]);
					double tempY1 = Double.parseDouble(coordinate[4].replace(";", ""));
					x0List.add(tempX0);
					y0List.add(tempY0);
					x1List.add(tempX1);
					y1List.add(tempY1);
					centerXList.add((tempX0+tempX1)/2);
					centerYList.add((tempY0+tempY1)/2);
				}
			}
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println(content);
		for(int i = 0;i<content.size();i++){
//			Pattern pattern1 = Pattern.compile("\\W");
//			Matcher matcher1 = pattern1.matcher(content.get(i));
//			boolean match = matcher1.find();
			if(!content.get(i).matches("[a-zA-Z]+")){
				content.remove(i);
				x0List.remove(i);
				x1List.remove(i);
				y0List.remove(i);
				y1List.remove(i);
				centerXList.remove(i);
				centerYList.remove(i);
			}
		}
		for(int i = 0;i<content.size();i++){
			Pattern pattern2 = Pattern.compile("\\d");
			Matcher matcher2 = pattern2.matcher(content.get(i));
			boolean match = matcher2.find();
			if(match){
				content.remove(i);
				x0List.remove(i);
				x1List.remove(i);
				y0List.remove(i);
				y1List.remove(i);
				centerXList.remove(i);
				centerYList.remove(i);
			}
		}
		//pre score
		System.out.println(content.toString());
		List<Map<String,String>> result = new ArrayList<Map<String, String>>(); 
		for(int i=0;i<content.size();i++){
			String word = content.get(i);
			Map<String, String> scoreMap = new HashMap<String, String>();
			double score = 0;
			//Stanford NER scoring
			for(String name:names){
				if(name.equals(word)){
					score += 1;
					break;
				}
			}
			//terminology dictionary scoring
			for(String dicWord:dicWords){
				if(dicWord.equals(word.toLowerCase().replaceAll("\\W", ""))){
					score = score - 1;
					break;
				}
			}
			//classifier scoring
			double tempx0 = x0List.get(i);
			double tempy0 = y0List.get(i);
			double tempx1 = x1List.get(i);
			double tempy1 = y0List.get(i);
			//top-left
			if(tempx0>=130&&tempy0>=130&&tempx1<=830&&tempy1<=750){
				score += 0.309;
			}
			//top-right
			if(tempx0>=1540&&tempy0>=550&&tempx1<=2080&&tempy1<=780){
				score += 0.072;
			}
			//middle-middle
			if(tempx0>=830&&tempy0>=1200&&tempx1<=1730&&tempy1<=1420){
				score += 0.371;
			}
			//middle-left
			if(tempx0>=100&&tempy0>=1200&&tempx1<=820&&tempy1<=1500){
				score += 0.103;
			}
			//middle-right
			if(tempx0>=1560&&tempy0>=1220&&tempx1<=2100&&tempy1<=1400){
				score += 0.072;
			}
			//bottom-left
			if(tempx0>=320&&tempy0>=2750&&tempx1<=900&&tempy1<=2900){
				score += 0.052;
			}
			//key words scoring
			String[] keyWords1 = {"insured", "name", "named","holder","borrower","issued","agent"};
			String[] keyWords2 = {"ave","road","st","avenue","rd","bank","inc","llc","insurance"};
			if(i>=10&i<=content.size()-10){
				for(int j=i-10;j<i+10;j++){
					double centerDistance = Math.sqrt(Math.pow(centerXList.get(i)-centerXList.get(j),2)
							+Math.pow(centerYList.get(i)-centerYList.get(j),2));
					double xDistance = Math.abs(centerXList.get(i)-centerXList.get(j));
					double yDistance = Math.abs(centerYList.get(i)-centerYList.get(j));
					if(xDistance<350&&yDistance<12){
						for(int k=0;k<keyWords2.length;k++){
							if(keyWords2[k].equals(content.get(j).toLowerCase().replaceAll("[\\pP‘’“”]", ""))){
								score = score - 0.6;
								break;
							}
						}
					}
					if(centerDistance<=350){
						for(int k=0;k<keyWords1.length;k++){
							if(keyWords1[k].equals(content.get(j).toLowerCase().replaceAll("[\\pP‘’“”]", ""))){
								score += 0.6;
								break;
							}
						}
					}
				}
			}
			if(i<10){
				for(int j=0;j<i+10;j++){
					if(j == content.size()){
						break;
					}
					double centerDistance = Math.sqrt(Math.pow(centerXList.get(i)-centerXList.get(j),2)
							+Math.pow(centerYList.get(i)-centerYList.get(j),2));
					double xDistance = Math.abs(centerXList.get(i)-centerXList.get(j));
					double yDistance = Math.abs(centerYList.get(i)-centerYList.get(j));
					if(xDistance<350&&yDistance<12){
						for(int k=0;k<keyWords2.length;k++){
							if(keyWords2[k].equals(content.get(j).toLowerCase().replaceAll("[\\pP‘’“”]", ""))){
								score = score - 0.6;
								break;
							}
						}
					}
					if(centerDistance<=350){
						for(int k=0;k<keyWords1.length;k++){
							if(keyWords1[k].equals(content.get(j).toLowerCase().replaceAll("[\\pP‘’“”]", ""))){
								score += 0.6;
								break;
							}
						}
					}
				}
			}
			if(i>content.size()-10){
				for(int j=i-10;j<content.size();j++){
					if(j>=0){
						double centerDistance = Math.sqrt(Math.pow(centerXList.get(i)-centerXList.get(j),2)
								+Math.pow(centerYList.get(i)-centerYList.get(j),2));
						double xDistance = Math.abs(centerXList.get(i)-centerXList.get(j));
						double yDistance = Math.abs(centerYList.get(i)-centerYList.get(j));
						if(xDistance<350&&yDistance<12){
							for(int k=0;k<keyWords2.length;k++){
								if(keyWords2[k].equals(content.get(j).toLowerCase().replaceAll("[\\pP‘’“”]", ""))){
									score = score - 0.6;
									break;
								}
							}
						}
						if(centerDistance<=350){
							for(int k=0;k<keyWords1.length;k++){
								if(keyWords1[k].equals(content.get(j).toLowerCase().replaceAll("[\\pP‘’“”]", ""))){
									score += 0.6;
									break;
								}
							}
						}

					}
				}
			}
			scoreMap.put("Word", word);
			scoreMap.put("Score", score+"");
			scoreMap.put("Index",i+"");
			result.add(scoreMap);
		}
		System.out.println("------------------------------------------------");
		for(Map<String, String> map:result){
			System.out.println(map.get("Word")+": "+map.get("Score")+" "+map.get("Index"));
		}
		//sort result list by the score value	
		Collections.sort(result, new Comparator<Map<String, String>>() {
			public int compare(Map<String, String> map1, Map<String, String> map2) {

				if (Double.parseDouble(map1.get("Score")) == Double.parseDouble(map2.get("Score"))) {
					return 0;
				} else if (Double.parseDouble(map1.get("Score")) < Double.parseDouble(map2.get("Score"))) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		//spatial logical relationship scoring
		List<Map<String,String>> highScored = new ArrayList<Map<String, String>>(); 
		for(int i = 0;i<result.size();i++){
			double score = Double.parseDouble(result.get(i).get("Score"));
			if(score>1){
				Map<String, String> map = new HashMap<String, String>();
				map =result.get(i);
				map.put("IndexInResult", i+"");
				highScored.add(map);
			}
			else{
				break;
			}
		}
		for(int i = 0; i<highScored.size();i++){
			int index_i = Integer.parseInt(highScored.get(i).get("Index"));
			double score = Double.parseDouble(highScored.get(i).get("Score"));
			for(int j=0;j<highScored.size();j++){
				if(!(i==j)){
					int index_j = Integer.parseInt(highScored.get(j).get("Index"));
					double xDistance = Math.abs(centerXList.get(index_i)-centerXList.get(index_j));
					double yDistance = Math.abs(centerYList.get(index_i)-centerYList.get(index_j));
					if(xDistance<350&&yDistance<12){
						score += 1;
					}
				}
			}
			
			Map<String, String> scoreMap = new HashMap<String, String>();
			scoreMap.put("Word", highScored.get(i).get("Word"));
			scoreMap.put("Score", score+"");
			scoreMap.put("Index",highScored.get(i).get("Index"));
			int indexInResult = Integer.parseInt(highScored.get(i).get("IndexInResult"));
			result.set(indexInResult, scoreMap);
		}
		// sort result list by the score value
		Collections.sort(result, new Comparator<Map<String, String>>() {
			public int compare(Map<String, String> map1, Map<String, String> map2) {

				if (Double.parseDouble(map1.get("Score")) == Double.parseDouble(map2.get("Score"))) {
					return 0;
				} else if (Double.parseDouble(map1.get("Score")) < Double.parseDouble(map2.get("Score"))) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		System.out.println("------------------------------------------------");
		for(Map<String, String> map:result){
			System.out.println(map.get("Word")+": "+map.get("Score"));
		}
//		 get the output in name sequence
		List<Map<String,String>> output = new ArrayList<Map<String, String>>(); 
		for(int i = 0;i<result.size();i++){
			double score = Double.parseDouble(result.get(i).get("Score"));
			if(score>2){
				Map<String, String> map = new HashMap<String, String>();
				map =result.get(i);
				output.add(map);
			}
			else{
				break;
			}
		}
		Collections.sort(output, new Comparator<Map<String, String>>() {
			public int compare(Map<String, String> map1, Map<String, String> map2) {
				
				if (Double.parseDouble(map1.get("Index")) == Double.parseDouble(map2.get("Index"))) {
					return 0;
				} else if (Double.parseDouble(map1.get("Index")) > Double.parseDouble(map2.get("Index"))) {
					return 1;
				} else {
					return -1;
				}
			}
		});
		System.out.println("------------------------------------------------");
//		for(Map<String, String> map:output){
//			System.out.println(map.get("Word")+": "+map.get("Score"));
//		}
		ArrayList<String> combinedNames = new ArrayList<String>();
		ArrayList<Integer> indexs = new ArrayList<Integer>();
		indexs.add(0);
		for(int i=1;i<output.size();i++){
			int index_1 = Integer.parseInt(output.get(i).get("Index"));
			int index_2 = Integer.parseInt(output.get(i-1).get("Index"));
			if(index_1 - index_2 > 1){
				indexs.add(i);
			}
		}
		indexs.add(output.size());
		for(int i=1;i<indexs.size();i++){
			String combinedName="";
			double x0 = 0;
			double y0 = 0;
			double x1 = 0;
			double y1 = 0;
			for(int j=indexs.get(i-1);j<indexs.get(i);j++){
				int index = Integer.parseInt(output.get(j).get("Index")); 
				double tempx0 = x0List.get(index);
				double tempy0 = y0List.get(index);
				double tempx1 = x1List.get(index);
				double tempy1 = y1List.get(index);
				if(j == indexs.get(i-1)){
					x0 = tempx0;
					y0 = tempy0;
					x1 = tempx1;
					y1 = tempy1;
				}
				combinedName = combinedName + output.get(j).get("Word")+" ";
				if(tempx0<x0){
					x0 = tempx0;
				}
				if(tempy0<y0){
					y0 = tempy0;
				}
				if(tempx1>x1){
					x1 = tempx1;
				}
				if(tempy1>y1){
					y1 = tempy1;
				}	
			}
			combinedName = combinedName + (int)x0 + " "+ (int)y0 + " "+ (int)x1 + " "+ (int)y1 + " ";
			combinedName = combinedName.substring(0, combinedName.length()-1);
			combinedNames.add(combinedName);
		}
		System.out.println(combinedNames.toString());
	}
}
