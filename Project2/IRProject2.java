import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.*;

/***********************************************************************************************************************************************************************************************
 * CSE535 Information Retrieval (Fall 2016) Project Two: Boolean Query and
 * Inverted Index Created by : Saurabh Bajoria 
 * UbIT Name: sbajoria 
 * UbNo: 50208005
 **********************************************************************************************************************************************************************************************/
public class IRProject2 {
	public static HashMap<String, LinkedList<Integer>> HM = new HashMap<String, LinkedList<Integer>>();			//hashmap for storing the inverted index

	public static void main(String[] args) {
		try {
			IRProject2 IRP = new IRProject2();				//instance of the class
			FileSystem fs = FileSystems.getDefault();
			Path P = fs.getPath(args[0]);					//get the path of the Lucene index
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2]), "UTF8"));			//BufferedReader instance to read the input file	
			Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]), "UTF8"));				//Writer instance to write the output to a file
			IndexReader IR = DirectoryReader.open(FSDirectory.open(P));						//read the index provided
			Fields F = MultiFields.getFields(IR);
			for (Iterator<String> FE = F.iterator(); FE.hasNext();) {					//for loop to iterate over all the fields present in the index
				String element = FE.next();
				if (element.equals("_version_") || element.equals("id")) {				//condition to ignore terms from the "_version_" and "id" field
				} else {
					TermsEnum termEnum = MultiFields.getTerms(IR, element).iterator();			//iterate over all the terms in the Index for a field
					while ((termEnum.next()) != null) {
						if (HM.get(termEnum.term().utf8ToString()) == null)				//check if the term does not exist in the HashMap
							HM.put(termEnum.term().utf8ToString(), new LinkedList<Integer>());		//insert the term
						PostingsEnum postenum = MultiFields.getTermDocsEnum(IR, element, termEnum.term());		//iterate over all the docIDs corresponding to the term
						while ((postenum.nextDoc() != PostingsEnum.NO_MORE_DOCS)) {
							HM.get(termEnum.term().utf8ToString()).addLast(postenum.docID());			//insert the docID against the term in the HashMap
						}
					}
				}
			}
			String line;
			while ((line = br.readLine()) != null) {			//if the input file has a line left to be read 
				String tokens[] = line.split("\\s+");
				for (int i = 0; i < tokens.length; i++) {		//for loop for the number of terms in a line
					output.write("GetPostings" + "\r\n" + tokens[i] + "\r\n" + "Postings list:");
					LinkedList<Integer> printList = HM.get(tokens[i]);		//retrieve the postings list for the term 
					printList.sort(null);
					Iterator<Integer> printEnum = printList.iterator();		//iterate over all the docIDs
					while (printEnum.hasNext())					//while the postings list has more elements
						output.write(" " + printEnum.next().toString());	//print individual docIds
					output.write("\r\n");
				}
				// IRP.TAAT(tokens, output, line);				//function call for Taat ("And" and "Or" --Scoring method)
				IRP.taatAnd(tokens, output, line);				//function call for TaatAnd
				IRP.taatOr(tokens, output, line);				//function call for TaatOr
				IRP.daat(tokens, output, line);					//function call for Daat ("And" and "Or")

			}
			br.close();
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*This function is to implement the Document-at-a-time query. It takes the query terms as input and outputs the intersection and the union of the lists using DAAT logic*/
	public void daat(String[] tokens, Writer output, String line) {
		try {										//Document-at-a-time OR implementation
			ArrayList<Integer> pointerOr = new ArrayList<Integer>();				//initialize different ArrayList to be used for this method
			ArrayList<Integer> pointerAnd = new ArrayList<Integer>();
			ArrayList<Integer> compareValuesOr = new ArrayList<Integer>();
			ArrayList<Integer> compareValuesAnd = new ArrayList<Integer>();
			List<Integer> postingsList = new LinkedList<Integer>();
			ArrayList<Integer> daatAnd = new ArrayList<Integer>();
			ArrayList<Integer> daatOr = new ArrayList<Integer>();
			ArrayList<Integer> size = new ArrayList<Integer>();
			Boolean loopbreak = true;
			int orCounter = 0, andCounter = 0;
			int tokenCount = 0;
			for (int i = 0; i < tokens.length; i++) {		//for loop for retrieving the postings list for all the query terms
				postingsList = HM.get(tokens[i]);			//retrieving the postings list for the term
				postingsList.sort(null);
				tokenCount = tokenCount + postingsList.size();		//keep a count for all the docIDs of all the query terms
				size.add(postingsList.size());						//maintain an ArrayList for the size of each postings list
				pointerOr.add(0);									//maintain a pointer ArrayList for DaatOr implementation
				pointerAnd.add(0);									//maintain a pointer ArrayList for DaatAnd implementation
				compareValuesOr.add(postingsList.get(0));			//maintain an ArrayList for storing the first element of each postings List--to be used for the DaatOr implementation			
				compareValuesAnd.add(postingsList.get(0));			//maintain an ArrayList for storing the first element of each postings List--to be used for the DaatAnd implementation
			}
			while (loopbreak) {
				int min = Integer.MAX_VALUE;						//temporary minimum
				Integer minIndex = 0;
				for (int i = 0; i < compareValuesOr.size(); i++) {				//Iterate through all the terms, starting with the first element of each list
					if (HM.get(tokens[i]).size() > pointerOr.get(i)) {			
						if (compareValuesOr.get(i) < min) {						//check if the ith term is less than the current minimum
							min = compareValuesOr.get(i);						
							minIndex = i;
							orCounter++;										//comparison count for OR incremented
						} else if (compareValuesOr.get(i) == min) {				//check if the ith term is equal to the current minumum 
							orCounter++;										//comparison count for OR incremented
							if (HM.get(tokens[i]).size() > pointerOr.get(i) + 1) {	//if yes, then increment the pointer	
								pointerOr.set(i, pointerOr.get(i) + 1);
								compareValuesOr.set(i, HM.get(tokens[i]).get(pointerOr.get(i)));
							} else {
								pointerOr.set(i, pointerOr.get(i) + 1);
							}
						} else {
							orCounter++;										//comparison count for OR incremented ..(if the ith term is greater than the minimum)
						}
					}
				}
				daatOr.add(min);						//add the minimum term to the DaatOr list
				if (HM.get(tokens[minIndex]).size() > pointerOr.get(minIndex) + 1) {	//increment the pointer of the minimum term postings list			
					pointerOr.set(minIndex, pointerOr.get(minIndex) + 1);
					compareValuesOr.set(minIndex, HM.get(tokens[minIndex]).get(pointerOr.get(minIndex)));
				} else
					pointerOr.set(minIndex, pointerOr.get(minIndex) + 1);
				Integer counter = 0;
				for (int i = 0; i < compareValuesOr.size(); i++) {			//for loop to keep a count of the number of docIDs processed
					if (pointerOr.get(i) == HM.get(tokens[i]).size()) {
						counter = counter + pointerOr.get(i);
					}
				}
				if (counter >= tokenCount) {			//if all the docIds processed, then break
					loopbreak = false;
					break;
				}
			}
			//Document-at-a-time AND implementation
			int sizeMax = Integer.MIN_VALUE;		//temporary maximum
			boolean loopbreakAnd = true;
			for (int a = 0; a < size.size(); a++) {		//find the largest Postings List
				if (size.get(a) > sizeMax) {
					sizeMax = size.get(a);
				}
			}
			orCounter = orCounter - sizeMax;
			outerloop: while (loopbreakAnd) {
				int matchCountAnd = 0, max = 0;
				for (int a = 0; a < compareValuesAnd.size(); a++) {		//Iterate through the first terms of all the postings list to calculate the max
					andCounter++;										//comparison count for AND incremented every time two terms are compared
					if (compareValuesAnd.get(a) > max) {
						max = compareValuesAnd.get(a);
					}

				}
				andCounter -= 1;
				for (int j = 0; j < pointerAnd.size(); j++) {		//Iterate through all the terms, starting with the first element of each list
					if (compareValuesAnd.get(j) == max) {			//check if the jth term is equal to max
						matchCountAnd += 1;					//keep a count of equal terms
						andCounter += 1;					//comparison count for AND incremented
						if (matchCountAnd == pointerAnd.size()) {			//if the count of equal terms if equal to the number of postings list
							daatAnd.add(compareValuesAnd.get(j));			//add to the DAATAnd List
							for (int k = 0; k < pointerAnd.size(); k++) {	//and increment the pointers for all the terms
								if (HM.get(tokens[k]).size() > pointerAnd.get(k) + 1) {		//check if there exists a next term in the postings list
									pointerAnd.set(k, pointerAnd.get(k) + 1);
									compareValuesAnd.set(k, HM.get(tokens[k]).get(pointerAnd.get(k)));
								} else {					//if next term does not exist then break the outer while loop
									loopbreakAnd = false;
									break outerloop;
								}
							}
						}
					} else if (compareValuesAnd.get(j) < max) {			//check if the jth term is less than the max
						while (max > compareValuesAnd.get(j)) {			//iterate until the jth term is less than the max, increasing the pointer every time
							andCounter += 1;							//comparison count for AND incremented
							if (HM.get(tokens[j]).size() > pointerAnd.get(j) + 1) {		//check if there exists a next term in the postings list
								pointerAnd.set(j, pointerAnd.get(j) + 1);
								compareValuesAnd.set(j, HM.get(tokens[j]).get(pointerAnd.get(j)));
							} else {						//if next term does not exist then break the outer while loop
								loopbreakAnd = false;
								break outerloop;
							}
						}
					}
				}
			}

			daatAnd.sort(null);
			daatOr.sort(null);
			output.write("\n" + "DaatAnd" + "\r\n" + line + "\r\n" + "Results:");			
			if (!daatAnd.isEmpty())											
				for (Integer print : daatAnd) {					//print the results of DaatAnd iterating through the list
					output.write(" " + print);
				}
			else											//print "empty" if the list is empty
				output.write(" empty");
			output.write("\n" + "Number of documents in results: " + daatAnd.size() + "\r\n" + "Number of comparisons: "	//print the number of documents, number of comparisons
					+ andCounter + "\r\n" + "DaatOr" + "\r\n" + line + "\r\n" + "Results:");								
			if (!daatOr.isEmpty())
				for (Integer print : daatOr) {					//print the results of DaatOr iterating through the list
					output.write(" " + print);
				}
			else
				output.write(" empty");						//print "empty" if the list is empty
			output.write("\n" + "Number of documents in results: " + daatOr.size() + "\r\n" + "Number of comparisons: "		//print the number of documents, number of comparisons
					+ orCounter + "\r\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*This function is to implement the Term-at-a-time OR query. It takes the query terms as input and outputs the union of the lists using TAAT OR logic*/
	public void taatOr(String tokens[], Writer output, String line) {
		try {
			boolean flag = true;
			Integer orCount = 0;
			Iterator<Integer> temp = HM.get(tokens[0]).iterator();		//retrieve the postings list for the first query term
			List<Integer> postingListTaatOr1 = new ArrayList<Integer>();
			while (temp.hasNext()) {
				postingListTaatOr1.add(temp.next());					//add the docIDs of the postings list to a new List
			}
			if (tokens.length > 1) {								//check if the input query has more than 1 term
				List<Integer> postingListTaatOr2 = HM.get(tokens[1]);	//retrieve the postings list for the second query term
				List<Integer> intermediateTaatOr = new ArrayList<Integer>();	//intermediate list to hold the result of the two postings list being compared

				int k = 2;
				do {
					int i = 0, j = 0;						//i and j are pointers for the two postings list being compared
					while ((i < postingListTaatOr1.size()) && (j < postingListTaatOr2.size())) {	//check if more docIDs exist in both the postings list 
						if (postingListTaatOr1.get(i) < postingListTaatOr2.get(j)) {				//check if the docID in the first postings list is smaller than the docID in the second postings list
							intermediateTaatOr.add(postingListTaatOr1.get(i));						//if yes then add to the intermediate list
							orCount++;																//comparison count for OR incremented
							i++;																	//increment the pointer referring to the first postings list
						} else if (postingListTaatOr1.get(i) > postingListTaatOr2.get(j)) {			//check if the docID in the second postings list is smaller than the docID in the first postings list
							intermediateTaatOr.add(postingListTaatOr2.get(j));						//if yes then add to the intermediate list
							orCount++;																//comparison count for OR incremented
							j++;																	//increment the pointer referring to the second postings list
						} else {																	//check if the docID in both the postings list is equal
							intermediateTaatOr.add(postingListTaatOr1.get(i));						//if yes then add to the intermediate list
							orCount++;																//comparison count for OR incremented
							i++;																	//increment the pointer referring to the first postings list
							j++;																	//increment the pointer referring to the second postings list
						}
					}
					while (j < postingListTaatOr2.size()) {											//check if more terms exist in the second postings list
						intermediateTaatOr.add(postingListTaatOr2.get(j));							//add to the intermediate list
						j++;																		//increment the pointer
					}
					while (i < postingListTaatOr1.size()) {											//check if more terms exist in the first postings list
						intermediateTaatOr.add(postingListTaatOr1.get(i));							//add to the intermediate list
						i++;																		//increment the pointer
					}

					postingListTaatOr1.clear();
					Iterator<Integer> intermediateEnum = intermediateTaatOr.iterator();				
					while (intermediateEnum.hasNext()) {											//copy the elements of the intermediate list to the first postings list
						postingListTaatOr1.add(intermediateEnum.next());

					}
					postingListTaatOr1.sort(null);
					intermediateTaatOr.clear();
					if (k == tokens.length) {														//break if the query has only two terms
						break;
					}
					postingListTaatOr2 = HM.get(tokens[k]);											//retrieve the postings list for the subsequent query term
					k++;
				} while (k <= tokens.length);														//iterate for all the query terms
			}
			output.write("TaatOr" + "\r\n" + line + "\r\n" + "Results:");							
			Iterator<Integer> print = postingListTaatOr1.iterator();
			while (print.hasNext()) {																//print the results for TaatOR iterating through the list
				flag = false;
				output.write(" " + print.next());
			}
			if (flag == true)
				output.write(" empty");																//print empty if the list is empty
			output.write("\n" + "Number of documents in results: " + postingListTaatOr1.size() + "\r\n"	//print the Number of documents and the number of comparisons
					+ "Number of comparisons: " + orCount);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/*This function is to implement the Term-at-a-time AND query. It takes the query terms as input and outputs the intersection of the lists using TAAT AND logic*/
	public void taatAnd(String tokens[], Writer output, String line) {
		try {
			Integer andCount = 0;
			boolean flag = true;
			Iterator<Integer> temp = HM.get(tokens[0]).iterator();									//retrieve the postings list for the first query term
			List<Integer> postingList1 = new ArrayList<Integer>();
			while (temp.hasNext()) {
				postingList1.add(temp.next());														//add the docIDs of the postings list to a new List
			}
			if (tokens.length > 1) {																//check if the input query has more than 1 term
				List<Integer> postingList2 = HM.get(tokens[1]);										//retrieve the postings list for the second query term
				List<Integer> intermediate = new ArrayList<Integer>();								//intermediate list to hold the result of the two postings list being compared

				int k = 2;
				do {
					int i = 0, j = 0;																//i and j are pointers for the two postings list being compared
					while ((i < postingList1.size()) && (j < postingList2.size())) {				//check if more docIDs exist in both the postings list
						if (postingList1.get(i) < postingList2.get(j)) {							//check if the docID in the first postings list is smaller than the docID in the second postings list
							andCount++;																//comparison count for AND incremented
							i++;																	//increment the pointer referring to the first postings list
						} else if (postingList1.get(i) > postingList2.get(j)) {						//check if the docID in the first postings list is bigger than the docID in the second postings list
							j++;																	//increment the pointer referring to the second postings list
							andCount++;																//comparison count for AND incremented
						} else {																	//check if the docID in the first postings list is equal to the docID in the second postings list
							intermediate.add(postingList1.get(i));									//if yes then add the docID to the intermediate list 
							andCount++;																//comparison count for AND incremented
							i++;																	//increment the pointer referring to the first postings list
							j++;																	//increment the pointer referring to the second postings list
						}
					}

					postingList1.clear();
					Iterator<Integer> intermediateEnum = intermediate.iterator();
					while (intermediateEnum.hasNext()) {											//copy the elements of the intermediate list to the first postings list
						postingList1.add(intermediateEnum.next());
					}
					intermediate.clear();
					if (k == tokens.length) {														//break if the query has only two terms
						break;
					}
					postingList2 = HM.get(tokens[k]);												//retrieve the postings list for the subsequent query term
					k++;
				} while (k <= tokens.length);														//iterate for all the query terms
			}
			output.write("TaatAnd" + "\r\n" + line + "\r\n" + "Results:");
			Iterator<Integer> print = postingList1.iterator();
			while (print.hasNext()) {																//print the results for TaatAnd iterating through the list
				flag = false;
				output.write(" " + print.next());
			}
			System.out.println("\n");
			if (flag == true)
				output.write(" empty");																//print empty if the list is empty

			output.write("\n" + "Number of documents in results: " + postingList1.size() + "\r\n"
					+ "Number of comparisons: " + andCount + "\r\n");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*The following commented code is to implement the Term-at-a-time query using document scoring method.*/
	/*
	 * public void taat(String[] tokens, Writer output, String line) { try {
	 * Map<Integer, Integer> taatMap = new HashMap<Integer, Integer>();
	 * ArrayList<Integer> taatOr = new ArrayList<Integer>(); ArrayList<Integer>
	 * taatAnd = new ArrayList<Integer>(); int orCounter = 0, andCounter = 0;
	 * output.write("TaatAnd" + "\r\n" + line); for (int i = 0; i <
	 * tokens.length; i++) { LinkedList<Integer> list = HM.get(tokens[i]);
	 * Iterator<Integer> postingsListEnum = list.iterator(); while
	 * (postingsListEnum.hasNext()) { int reference = postingsListEnum.next();
	 * if (taatMap.get(reference) == null) { taatMap.put(reference, 1);
	 * taatOr.add(reference); orCounter += 1; andCounter += 1; } else {
	 * andCounter += 1; orCounter += 1; taatMap.put(reference,
	 * (taatMap.get(reference) + 1)); if (taatMap.get(reference) ==
	 * tokens.length) { taatAnd.add(reference); } } } } output.write("\r\n" +
	 * "Results: "); if (taatAnd.size() != 0) { taatAnd.sort(null); for (int i =
	 * 0; i < taatAnd.size(); i++) { output.write(taatAnd.get(i) + " "); } }
	 * else { output.write("empty"); } output.write("\n" +
	 * "Number of documents in results: " + taatAnd.size() + "\r\n" +
	 * "Number of Comparisons: " + andCounter + "\r\n"); output.write("TaatOr "
	 * + "\r\n" + line + "\r\n" + "Results: "); if (taatOr.size() != 0) {
	 * taatOr.sort(null); for (int i = 0; i < taatOr.size(); i++) {
	 * output.write(taatOr.get(i) + " "); } } else { output.write("empty"); }
	 * output.write("\n" + "Number of documents in results: " + taatOr.size() +
	 * "\r\n"); output.write("Number of comparisons: " + orCounter + "\r\n"); }
	 * catch (Exception e) { e.printStackTrace(); } }
	 */

}
