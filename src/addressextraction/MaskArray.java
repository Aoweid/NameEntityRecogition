package addressextraction;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MaskArray {
	// private ArrayList<Integer> x0List;
	// private ArrayList<Integer> x1List;
	// private ArrayList<Integer> y0List;
	// private ArrayList<Integer> y1List;
	// private ArrayList<String> content;
	private ArrayList<Map<String, Object>> pages = new ArrayList<Map<String, Object>>();
	// private int length;
	// private int width;
	private String hocrPath;

	public MaskArray(String hocrPath) {
		this.hocrPath = hocrPath;
	}

	@SuppressWarnings("unchecked")
	public void setUp() {
		// x0List = new ArrayList<Integer>();
		// x1List = new ArrayList<Integer>();
		// y0List = new ArrayList<Integer>();
		// y1List = new ArrayList<Integer>();
		// content = new ArrayList<String>();
		File hocr = new File(hocrPath);
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
			int pageNumber = 0;
			NodeList divList = doc.getElementsByTagName("div");
			for (int i = 0; i < divList.getLength(); i++) {
				Node node = divList.item(i);
				Element ele = (Element) node;
				if (ele.getAttribute("class").equals("ocr_page")) {
					pageNumber++;
				}
			}
			for (int k = 1; k < pageNumber + 1; k++) {
				Element pageEle = null;
				for (int i = 0; i < divList.getLength(); i++) {
					Node node = divList.item(i);
					Element ele = (Element) node;
					if (ele.getAttribute("id").equals("page_" + k)) {
						pageEle = ele;
					}
				}
				String[] tempCoordinate = pageEle.getAttribute("title").split("; ")[1].split(" ");
				int width = Integer.parseInt(tempCoordinate[3]);
				int length = Integer.parseInt(tempCoordinate[4]);
				NodeList nList = doc.getElementsByTagName("span");
				ArrayList<String> content = new ArrayList<>();
				ArrayList<Integer> x0List = new ArrayList<>();
				ArrayList<Integer> y0List = new ArrayList<>();
				ArrayList<Integer> x1List = new ArrayList<>();
				ArrayList<Integer> y1List = new ArrayList<>();
				for (int i = 0; i < nList.getLength(); i++) {
					Node node = nList.item(i);
					Element ele = (Element) node;
					if (ele.getAttribute("id").startsWith("word_" + k)) {
						content.add(ele.getTextContent());
						String[] coordinate = ele.getAttribute("title").split(" ");
						int tempX0 = Integer.parseInt(coordinate[1]);
						int tempY0 = Integer.parseInt(coordinate[2]);
						int tempX1 = Integer.parseInt(coordinate[3]);
						int tempY1 = Integer.parseInt(coordinate[4].replace(";", ""));
						x0List.add(tempX0);
						y0List.add(tempY0);
						x1List.add(tempX1);
						y1List.add(tempY1);
					}
				}
				Map<String, Object> page = new HashMap<String, Object>();
				page.put("PageNumber", k);
				page.put("Width", width);
				page.put("Length", length);
				page.put("X0List", x0List);
				page.put("Y0List", y0List);
				page.put("X1List", x1List);
				page.put("Y1List", y1List);
				page.put("Content", content);
				pages.add(page);
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
	}

	public int[][] getMaskArray(ArrayList<Integer> x0List, ArrayList<Integer> y0List, ArrayList<Integer> x1List,
			ArrayList<Integer> y1List, ArrayList<String> content, int width, int length) {
		int[][] maskArray = new int[width + 1][length + 1];
		for (int i = 0; i < width + 1; i++) {
			for (int j = 0; j < length + 1; j++) {
				maskArray[i][j] = 0;
			}
		}
		for (int i = 0; i < x0List.size(); i++) {
			if (!content.get(i).equals(" ") && !content.get(i).equals("")) {
				int x0 = x0List.get(i);
				int y0 = y0List.get(i);
				int x1 = x1List.get(i);
				int y1 = y1List.get(i);
				for (int m = x0; m <= x1; m++) {
					for (int n = y0; n <= y1; n++) {
						maskArray[m][n] = 1;
					}
				}
			}
		}
		return maskArray;
	}

	public ArrayList<Map<String, Object>> getPages() {
		return pages;
	}
	// public ArrayList<Integer> getX0List(){
	// return x0List;
	// }
	// public ArrayList<Integer> getY0List(){
	// return y0List;
	// }
	// public ArrayList<Integer> getX1List(){
	// return x1List;
	// }
	// public ArrayList<Integer> getY1List(){
	// return y1List;
	// }
	// public ArrayList<String> getContent(){
	// return content;
	// }
	// public int getLength(){
	// return length;
	// }
	// public int getWidth(){
	// return width;
	// }
}
