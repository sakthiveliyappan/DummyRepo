package MainExtractor;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class PDFTest {

	public static void main(String[] args) {
		PDDocument pd;
		Set<String> selectedMultilines = new LinkedHashSet<String>();
		Set<String> finalines = new LinkedHashSet<String>();
		Set<String> manuallines = new LinkedHashSet<String>();

		List<String> dupLines = new ArrayList<String>();
		List<String> pages = new ArrayList<String>();
		List<String> finalSelectedLines = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		int valueIndex = 0;
		List<String> confiStrings;
		String pageHeaderStr = null;
		String pageHeaderStrFinal = null;
		StringBuilder builder = new StringBuilder();
		BufferedWriter wr;
		try {

			File input = new File(
					"C:\\Users\\Hp\\Desktop\\TriplePulse\\150406634_AMARNATHAN_M_18May2015_104753_WL.pdf");
			// extract
			File output = new File("SampleText.txt"); // The text file where you are going to store the extracted data
			pd = PDDocument.load(input);
			System.out.println(pd.getNumberOfPages());
			System.out.println(pd.isEncrypted());
			PDFTextStripper stripper = new PDFLayoutTextStripper();
			for (int p = 1; p <= pd.getNumberOfPages(); ++p) {
				stripper.setStartPage(p);
				stripper.setEndPage(p);
				wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
				stripper.writeText(pd, wr);
				wr.close();
				BufferedReader b = new BufferedReader(new FileReader(output));
				String currentLine = null;
				while ((currentLine = b.readLine()) != null) {
					if (currentLine.trim().length() > 0) {
						builder.append(currentLine + "#");
					}
				}
				builder.append("!!#"); // append to end of the page. This is used to split pages by "!!# ".
			}
			//System.out.println("All Pages String :  " + builder.toString()); // entire page in one single string builder
			// spliting pages seperately by "!!# ".
			String[] pagesArr = builder.toString().split("!!#");
			for (int i = 0; i < pagesArr.length; i++) {
				if (pagesArr[i].trim().length() > 0) {
					pages.add(pagesArr[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// remove header part
		for (int i = 0; i < pages.size(); i++) {
			List<String> pageSelectedLines = new ArrayList<String>();
			String currentPage = pages.get(i).trim();
			//System.out.println("______________________________________");
			//System.out.println("currentPageStr : " + currentPage);
			//System.out.println("______________________________________");

			// dump page line into list
			String[] fls = currentPage.split("#");
			for (int j = 0; j < fls.length; j++) {
				pageSelectedLines.add(fls[j]);
			}

			// find test header
			for (int p = 0; p < pageSelectedLines.size(); p++) {
				String currentline = pageSelectedLines.get(p);
				String toLowerCaseStr = currentline.toLowerCase();
				String removeWhiteSpace = toLowerCaseStr.replaceAll(" ", "");
				if ((removeWhiteSpace.contains("units") && removeWhiteSpace.startsWith("test"))
						|| (removeWhiteSpace.contains("value") && removeWhiteSpace.startsWith("test"))) {
					pageHeaderStr = currentline;
					pageHeaderStrFinal = currentline;
					break;
				}
			}
			// split by test header
			if (pageHeaderStr != null) {
				String withouthead = currentPage.split(pageHeaderStr)[1].trim();
				String[] fls1 = withouthead.split("#");
				for (int j = 0; j < fls1.length; j++) {
					finalSelectedLines.add(fls1[j]);
				}
				pageHeaderStr = null;
			} else {
				//System.out.println("----------- page header not found-------------");
			}

		}
		// remove empty lines
		for (int i = 0; i < finalSelectedLines.size(); i++) {
			if (finalSelectedLines.get(i).length() > 0) {
				values.add(finalSelectedLines.get(i));
			}
		}
		// to find test headers like testName , Units , reference
		String valueStr = null;
		String unitStr = null;
		String refStr = null;

		String[] head = pageHeaderStrFinal.trim().split("\\s+\\s+\\s+\\s+");
		for (int i = 0; i < head.length; i++) {
			//System.out.println("i" + i + head[i]);
			if (head[i].toLowerCase().contains("ob")) {
				valueStr = head[i];
			} else if (head[i].toLowerCase().contains("value") || head[i].toLowerCase().contains("result")) {
				if (valueStr == null) {
					valueStr = head[i];
				} else {
					if (head[i].trim().toLowerCase().contains("reference")) {
						refStr = head[i];
					}
				}
			} else if (head[i].toLowerCase().contains("unit")) {
				unitStr = head[i];
			} else if (head[i].trim().toLowerCase().contains("reference")
					|| head[i].toLowerCase().contains("bio.ref")) {
				if (refStr == null) {
					refStr = head[i];
				}
			}
		}
		System.out.println("valueStr:" + valueStr + ":" + "unitStr:" + ":" + unitStr + ":" + "refStr:" + refStr + ":");
		
		// final input
		for (int i = 0; i < values.size(); i++) {
			 //System.out.println("INPUT: " + ":" + i + ":" + values.get(i));
		}
		

		// push the user to enter the test values if the test is not appear in final output
		for(String text: values) {
			Pattern letter = Pattern.compile("[a-zA-z]");
			Pattern ref_special = Pattern.compile("[<:>-]");

			if (valueStr != null && unitStr != null && refStr != null) {
				String testName;
				String testValue;
				String referenceValue;
				try {
					valueIndex = pageHeaderStrFinal.indexOf(valueStr);
					int unitIndex = pageHeaderStrFinal.indexOf(unitStr);
					int refIndex = pageHeaderStrFinal.indexOf(refStr);
					testName = text.substring(0, valueIndex - 1);
					testValue = text.substring(valueIndex - 1, unitIndex);
					referenceValue = text.substring(refIndex, text.length());		
					if ( ( letter.matcher(testName).find() 
							&& ref_special.matcher(referenceValue).find() )  && testValue.trim().length() == 0  ) {
						manuallines.add(text);
						System.out.println("NEED TO ENTER MANUALY  :"+ text);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (unitStr == null && (valueStr != null && refStr != null)) {
				try {
					valueIndex = pageHeaderStrFinal.indexOf(valueStr);
					int refIndex = pageHeaderStrFinal.indexOf(refStr);
					String testName = text.substring(0, valueIndex - 1);
					String testValue = text.substring(valueIndex - 1, refIndex);
					String referenceValue = text.substring(refIndex, text.length());
					if ( ( letter.matcher(testName).find() 
							&& ref_special.matcher(referenceValue).find() )  && testValue.trim().length() == 0  ) {
						System.out.println("NEED TO ENTER MANUALY  :"+ text);
						manuallines.add(text);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		// adding units to list
		confiStrings = new ArrayList<String>();
		confiStrings.add("%");
		confiStrings.add("cells/cu.m");
		confiStrings.add("mill/cu.mm");
		confiStrings.add("gm/dL");
		confiStrings.add("fL");
		confiStrings.add("fl");
		confiStrings.add("pg");
		confiStrings.add("Thousand/cu.mm");
		confiStrings.add("mm/hr");
		confiStrings.add("mg/dL");
		confiStrings.add("mg/dl");
		confiStrings.add("mmol/L");
		confiStrings.add("U/L");
		confiStrings.add("g/dL");
		confiStrings.add("µmol/L");
		confiStrings.add("RU/ml");
		confiStrings.add("AU/mL");
		confiStrings.add("mg/L");
		confiStrings.add("ng/ml");
		confiStrings.add("pg/ml");
		confiStrings.add("Ratio");
		confiStrings.add("µg/dl");
		confiStrings.add("mg/L");
		confiStrings.add("µU/ml");
		confiStrings.add("µmol/L");
		confiStrings.add("µg/dL");
		confiStrings.add("ng/dL");
		confiStrings.add("U/l");
		confiStrings.add("µIU/ml");
		confiStrings.add("mmol/l");
		confiStrings.add("mL/min/1.73m2");
		confiStrings.add(" X 10³  / µL ");
		confiStrings.add("X 10³  / µL");
		confiStrings.add("X  10^6/µL");
		confiStrings.add("µg/l");
		confiStrings.add("thou/cmm");
		confiStrings.add("cum");
		confiStrings.add("mm");
		confiStrings.add("mEq/l");
		confiStrings.add("millions/cumm");
		confiStrings.add("U/L");

		valueIndex = pageHeaderStrFinal.indexOf(valueStr);

		// extraction part - core part
		for (int i = 0; i < values.size(); i++) {
			int p = i;
			StringBuilder matchedTest = new StringBuilder();
			String currentLine = values.get(i);
			Pattern letter = Pattern.compile("[a-zA-z]");
			Pattern digit = Pattern.compile("[0-9]");
			Pattern special = Pattern.compile("[<>-]");
			Pattern ref_special = Pattern.compile("[<:>-]");
			Matcher hasLetter = letter.matcher(currentLine);
			Matcher hasDigit = digit.matcher(currentLine);
			Matcher hasSpecial = special.matcher(currentLine);

			// System.out.println("dub : " + currentLine + ": " +
			// +duplicateElementSizeInList(dupLines, currentLine));
			if (duplicateElementSizeInList(dupLines, currentLine) == 0) {

				if ((hasDigit.find() && hasLetter.find() && isContainsUnit(currentLine, confiStrings)
						&& hasSpecial.find()) || (hasDigit.find() && hasLetter.find())) {

					// System.out.println("passed single line :>>>>> : " + currentLine);

					if (i < values.size() - 2) {
						p = p + 1;
						String sL = values.get(p);
						// System.out.println("sl : " + sL);

						String sL1sub = sL.substring(valueIndex - 4, valueIndex);
						String sL2sub = sL.substring(valueIndex, valueIndex + 10);
						Pattern patternCenter = Pattern.compile("^\\s+$");

						if (patternCenter.matcher(sL1sub).find() && patternCenter.matcher(sL2sub).find()
								&& ref_special.matcher(sL).find()) {

							p = p + 1;
							String tL = values.get(p);
							// System.out.println("tl : " + tL);
							String tL1sub = tL.substring(valueIndex - 10, valueIndex);
							String tL2sub = tL.substring(valueIndex, valueIndex + 8);
							// System.out.println(":" + tL1sub + ":" + tL2sub);
							if (patternCenter.matcher(tL1sub).find() && patternCenter.matcher(tL2sub).find()
									&& ref_special.matcher(tL).find()) {

								p = p + 1;
								String fL = values.get(p);
								// System.out.println("fL : " + fL);

								// System.out.println("flLength" + valueIndex);
								String fL1sub = fL.substring(valueIndex - 4, valueIndex);
								String fL2sub = fL.substring(valueIndex, valueIndex + 10);

								if (patternCenter.matcher(fL1sub).find() && patternCenter.matcher(fL2sub).find()
										&& ref_special.matcher(fL).find()) {
									// System.out.println("inFL,...");
									matchedTest.append(currentLine);
									matchedTest.append("#" + sL.trim());
									matchedTest.append("#" + tL.trim());
									matchedTest.append("#" + fL.trim());
									dupLines.add(currentLine);
									dupLines.add(sL);
									dupLines.add(tL);
									dupLines.add(fL);
									selectedMultilines.add(matchedTest.toString());

								} else {
									matchedTest.append(currentLine);
									matchedTest.append("#" + sL.trim());
									matchedTest.append("#" + tL.trim());
									dupLines.add(currentLine);
									dupLines.add(sL);
									dupLines.add(tL);
									selectedMultilines.add(matchedTest.toString());
								}

							} else {
								matchedTest.append(currentLine);
								matchedTest.append("#"+sL.trim());
								dupLines.add(currentLine);
								dupLines.add(sL);
								selectedMultilines.add(matchedTest.toString());
							}

						} else {
							// second ref checking
							if (i < values.size() - 2) {

								p = p + 1;
								String trL = values.get(p);
								// System.out.println("trl : " + trL);
								String tfL1sub = trL.substring(valueIndex - 4, valueIndex);
								String tfL2sub = trL.substring(valueIndex, valueIndex + 10);
								// System.out.println(":" + tfL1sub + ":" + tfL2sub);

								if (patternCenter.matcher(tfL1sub).find() && patternCenter.matcher(tfL2sub).find()
										&& ref_special.matcher(trL).find()) {

									p = p + 1;
									String frL = values.get(p);
									// System.out.println("frL : " + frL);
									String frL1sub = frL.substring(valueIndex - 4, valueIndex);
									String frL2sub = frL.substring(valueIndex, valueIndex + 10);

									if (patternCenter.matcher(frL1sub).find() && patternCenter.matcher(frL2sub).find()
											&& ref_special.matcher(frL).find()) {

										p = p + 1; // five
										String fiverL = values.get(p);
										// System.out.println("fiverL" + fiverL);
										String firL1sub = fiverL.substring(valueIndex - 4, valueIndex);
										String firL2sub = fiverL.substring(valueIndex, valueIndex + 10);

										if (patternCenter.matcher(firL1sub).find()
												&& patternCenter.matcher(firL2sub).find()
												&& ref_special.matcher(fiverL).find()) {

											p = p + 1; // six
											String sixrL = values.get(p);
											// System.out.println("sixrL" + sixrL);
											String sixrL1sub = sixrL.substring(valueIndex - 4, valueIndex);
											String sixL2sub = sixrL.substring(valueIndex, valueIndex + 10);

											if (patternCenter.matcher(sixrL1sub).find()
													&& patternCenter.matcher(sixL2sub).find()
													&& ref_special.matcher(fiverL).find()) {

												matchedTest.append(currentLine);
												matchedTest.append("#"+trL.trim());
												matchedTest.append("#"+frL.trim());
												matchedTest.append("#"+fiverL.trim());
												matchedTest.append("#"+sixrL.trim());
												dupLines.add(sixrL);
												dupLines.add(frL);
												dupLines.add(trL);
												dupLines.add(fiverL);
												dupLines.add(currentLine);
												selectedMultilines.add(matchedTest.toString());

											} else {
												matchedTest.append(currentLine);
												matchedTest.append("#"+trL.trim());
												matchedTest.append("#"+frL.trim());
												matchedTest.append("#"+fiverL.trim());
												dupLines.add(frL);
												dupLines.add(trL);
												dupLines.add(fiverL);
												dupLines.add(currentLine);
												selectedMultilines.add(matchedTest.toString());
											}

										} else {

											matchedTest.append(currentLine);
											dupLines.add(frL.trim());
											dupLines.add(trL.trim());
											matchedTest.append("#"+trL.trim());
											matchedTest.append("#"+frL.trim());
											dupLines.add(currentLine);
											selectedMultilines.add(matchedTest.toString());
										}

									} else {
										matchedTest.append(currentLine);
										matchedTest.append("#"+trL.trim());
										dupLines.add(trL);
										dupLines.add(currentLine);
										selectedMultilines.add(matchedTest.toString());
									}

								} else {
									matchedTest.append(currentLine);
									dupLines.add(currentLine);
									selectedMultilines.add(matchedTest.toString());
								}
							}
						}

					} else {

						matchedTest.append(currentLine);
						dupLines.add(currentLine);
						selectedMultilines.add(matchedTest.toString());

					}

				}

			}

		}

		System.out.println(
				"------------------------------------------------------------------------------------------------------------------------------------------------");

		//System.out.println("<<<<<<<<<<< BEFORE REMOVING DUBLICATE: LIST SIZE ::" + selectedMultilines.size());
		// Removing duplicate list items
		for (String text : selectedMultilines) {
			Pattern letter = Pattern.compile("[a-zA-z]");
			Pattern digit = Pattern.compile("[0-9]");
			Pattern ref_special = Pattern.compile("[<:>-]");

			if (valueStr != null && unitStr != null && refStr != null) {

				String testName;
				String testValue;
				String referenceValue;
				try {
					valueIndex = pageHeaderStrFinal.indexOf(valueStr);
					int unitIndex = pageHeaderStrFinal.indexOf(unitStr);
					int refIndex = pageHeaderStrFinal.indexOf(refStr);
					testName = text.substring(0, valueIndex - 1);
					testValue = text.substring(valueIndex - 1, unitIndex);
					referenceValue = text.substring(refIndex, text.length());
					if (letter.matcher(testName).find() && digit.matcher(testValue).find()
							&& ref_special.matcher(referenceValue).find()) {
						finalines.add(text);
					} else if (letter.matcher(testName).find() && digit.matcher(testValue).find()) {
						finalines.add(text);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (unitStr == null && (valueStr != null && refStr != null)) {
				try {
					valueIndex = pageHeaderStrFinal.indexOf(valueStr);
					int refIndex = pageHeaderStrFinal.indexOf(refStr);
					String testName = text.substring(0, valueIndex - 1);
					String testValue = text.substring(valueIndex - 1, refIndex);
					String referenceValue = text.substring(refIndex, text.length());
					if (letter.matcher(testName).find() && digit.matcher(testValue).find()
							&& ref_special.matcher(referenceValue).find()) {
						finalines.add(text);
					} else if (letter.matcher(testName).find() && digit.matcher(testValue).find()) {
						finalines.add(text);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		//System.out.println("OUT PUT LIST SIZE: " + finalines.size());

		for (String line : finalines) {
			System.out.println( line + ":end");
		}

		
		
		// converting to json part
		JSONArray array = new JSONArray();

		for (String text : finalines) {

			if (valueStr != null && unitStr != null && refStr != null) {

				String testName;
				String testValue;
				String unitValue;
				String referenceValue;
				try {
					valueIndex = pageHeaderStrFinal.indexOf(valueStr);
					int unitIndex = pageHeaderStrFinal.indexOf(unitStr);
					int refIndex = pageHeaderStrFinal.indexOf(refStr);

					testName = text.substring(0, valueIndex - 1);
					testValue = text.substring(valueIndex - 1, unitIndex);
					unitValue = text.substring(unitIndex, refIndex);
					referenceValue = text.substring(refIndex, text.length());

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("parameterName", testName.trim());
					jsonObject.put("result", testValue.trim());
					jsonObject.put("referenceRange", referenceValue.trim());
					jsonObject.put("unit", unitValue.trim());
					array.add(jsonObject);

//					System.out.println("-----------------------------------");
//					System.out.println("testName:" + testName.trim());
//					System.out.println("testValue:" + testValue.trim());
//					System.out.println("unitValue:" + unitValue.trim());
//					System.out.println("referenceValue:" + referenceValue.trim());
//					System.out.println("-----------------------------------");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (unitStr == null && (valueStr != null && refStr != null)) {
				// three coloum without unit
				try {
					valueIndex = pageHeaderStrFinal.indexOf(valueStr);
					int refIndex = pageHeaderStrFinal.indexOf(refStr);
					String testName = text.substring(0, valueIndex - 1);
					String testValue = text.substring(valueIndex - 1, refIndex);
					String referenceValue = text.substring(refIndex, text.length());

					JSONObject jsonObject = new JSONObject();
					jsonObject.put("parameterName", testName.trim());
					jsonObject.put("result", testValue.trim());
					jsonObject.put("referenceRange", referenceValue.trim());
					array.add(jsonObject);

//					System.out.println("-----------------------------------");
//					System.out.println("testName:" + testName.trim());
//					System.out.println("testValue:" + testValue.trim());
//					System.out.println("referenceValue:" + referenceValue.trim());
//					System.out.println("-----------------------------------");
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
		}

		System.out.println(array.toString());


	}

	public static boolean containsIgnoreCase(String str, String subString) {
		return str.toLowerCase().contains(subString.toLowerCase());
	}

	public static boolean isContainsUnit(String sL, List<String> unitStrings) {
		boolean proceed = false;
		for (int m = 0; m < unitStrings.size(); m++) {
			String cl = sL.trim();
			Pattern pattern = Pattern.compile(unitStrings.get(m).trim());
			Matcher matcher = pattern.matcher(cl);
			if (matcher.find()) {
				// System.out.println(" Pattern matched " + unitStrings.get(m) + " : " + cl);
				proceed = true;
				break;
			}
			if (proceed)
				break;
		}
		return proceed;
	}

	public static int duplicateElementSizeInList(List<String> eventList, String text) {
		int dupSize = Collections.frequency(eventList, text);
		return dupSize;
	}

	private static boolean isLengthGreaterThanZero(String string) {
		return string != null && string.length() > 0;
	}
	
	
	
	
	
	
	
	
	
	
}
