import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;

public class LoadingScript {

	public static final String SCHEMAPATH = "/home/ubuntu/solr-6.2.0/IRF16P3/solr/";
	public static final String RESTARTCOMMAND = "/home/ubuntu/solr-6.2.0/bin/solr restart -s /home/ubuntu/solr-6.2.0/IRF16P3/solr/";
	public static final String POSTCOMMAND = "/home/ubuntu/solr-6.2.0/bin/post -c ";
	public static final String LOADFILENAME = " train.json";
	public static final String OUTPUTFOLDERPATH = "/home/ubuntu/IRF16P3_Output/";
	public static final String[] CORENAMES = {"BM25","DFR"};
	//public static final String[] CORENAMES = {"DFR"};
	public static final String[] BASICMODELS = {"Be","G","P","D","I(n)","I(ne)","I(F)"};
	public static final String[] BVALUES = {"L","B","none"};
	public static final String[] NORMALISATIONS = {"H1","H2","H3","Z","none"};
	//public static final String[] BASICMODELS = {"Be","G"};
	//public static final String[] BVALUES = {"L","B"};
	//public static final String[] NORMALISATIONS = {"H1","H2"};
	
	public static final DecimalFormat twoDForm = new DecimalFormat("#.##");
	
	public static void main(String[] args) {
		try {
			System.out.println("In Main");
			for(String core : CORENAMES){
				String coreSchemaPath = SCHEMAPATH + core + "/conf/schema.xml";

				String fileContent = readFile(coreSchemaPath, StandardCharsets.UTF_8);
				System.out.println("----------");
				String schemaNewContent = "";
				float k1 = (float) 1.2;
				float b = (float) 0.5;
				int startIndex = 0;
				if(core == "BM25"){
					while(k1 < 2.01){
						while(b < 0.81){
							System.out.println(" ### k1 " + k1 + "  b " + b);
							while(fileContent.contains("<similarity")){
								schemaNewContent += fileContent.substring(startIndex, fileContent.indexOf("<similarity"));
								schemaNewContent += "<similarity class=\"solr.BM25SimilarityFactory\">\n" + 
													"<float name=\"k1\">" + k1 + "</float>\n" +
													"<float name=\"b\">" + b + "</float>\n" +
													"</similarity>";
								startIndex = fileContent.indexOf("</similarity>") + 13;
								fileContent = fileContent.substring(startIndex,fileContent.length());
								
							}
							//if(!schemaNewContent.contains("</schema>")){
								schemaNewContent += fileContent;
							//}
							//System.out.println("###################### \n " + fileContent + " \n #############################");
							writeToFile(coreSchemaPath,schemaNewContent,false);
							Process restart = Runtime.getRuntime().exec(new String[]{"/home/ubuntu/solr-6.2.0/bin/solr","restart","-s","/home/ubuntu/solr-6.2.0/IRF16P3/solr/"});
							restart.waitFor();
							Process post = Runtime.getRuntime().exec(new String[]{"/home/ubuntu/solr-6.2.0/bin/post","-c",core,"/home/ubuntu/train.json"});
							post.waitFor();
							Process runPython = Runtime.getRuntime().exec(new String[]{"python3.5", "/home/ubuntu/json_to_trec.py", OUTPUTFOLDERPATH + core + "_" + twoDForm.format(k1) + "_" + twoDForm.format(b),core});
							runPython.waitFor();
							Process treEval = Runtime.getRuntime().exec(new String[]{"/home/ubuntu/shellScript.sh",core + "_" + twoDForm.format(k1) + "_" + twoDForm.format(b)});
							treEval.waitFor();
							writeToFile("/home/ubuntu/FinalOutput.txt",core + " " + "k1 " + twoDForm.format(k1) + " b " + twoDForm.format(b) + " Map Value ---> " + getMapValue(readFile("/home/ubuntu/IRF16P3_Map_Output/"+core + "_" + twoDForm.format(k1) + "_" + twoDForm.format(b) +"_output.txt", StandardCharsets.UTF_8)),true);
							b += 0.05;
							fileContent = readFile(coreSchemaPath, StandardCharsets.UTF_8);
							startIndex = 0;
							schemaNewContent = "";
							//System.out.println("###################### \n " + fileContent + " \n #############################");
						}
						k1 += 0.05;
						b = (float) 0.5;
						System.out.println("----k1-----" + k1);
					}
				}else if(core == "DFR"){
					System.out.println("BASICMODELS.length " + BASICMODELS.length + " BVALUES.length " + BVALUES.length + " NORMALISATIONS.length " + NORMALISATIONS.length);
					for(int i=0; i <BASICMODELS.length;i++ ){
						for(int j=0; j <BVALUES.length;j++ ){
							for(int k=0; k <NORMALISATIONS.length;k++ ){
								System.out.println("i " + i + " j " + j + " k " + k);
								while(fileContent.contains("<similarity")){
									schemaNewContent += fileContent.substring(startIndex, fileContent.indexOf("<similarity"));
									schemaNewContent += "<similarity class=\"solr.DFRSimilarityFactory\">\n" + 
														"<str name=\"basicModel\">" + BASICMODELS[i] + "</str>\n" +
														"<str name=\"afterEffect\">" + BVALUES[j] + "</str>\n" +
														"<str name=\"normalization\">" + NORMALISATIONS[k] + "</str>\n" +
														"</similarity>";
									startIndex = fileContent.indexOf("</similarity>") + 13;
									fileContent = fileContent.substring(startIndex,fileContent.length());
									//System.out.println("###################### \n " + fileContent + " \n #############################");
								}
								//if(!schemaNewContent.contains("</schema>")){
									schemaNewContent += fileContent;
								//}
								System.out.println("###################### \n " + schemaNewContent + " \n #############################");
								writeToFile(coreSchemaPath,schemaNewContent,false);
								Process restart = Runtime.getRuntime().exec(new String[]{"/home/ubuntu/solr-6.2.0/bin/solr","restart","-s","/home/ubuntu/solr-6.2.0/IRF16P3/solr/"});
								restart.waitFor();
								Process post = Runtime.getRuntime().exec(new String[]{"/home/ubuntu/solr-6.2.0/bin/post","-c",core,"/home/ubuntu/train.json"});
								post.waitFor();
								Process runPython = Runtime.getRuntime().exec(new String[]{"python3.5", "/home/ubuntu/json_to_trec.py", OUTPUTFOLDERPATH+ core + "_" + BASICMODELS[i] + "_" + BVALUES[j] + "_" + NORMALISATIONS[k],core});
								runPython.waitFor();
								Process treEval = Runtime.getRuntime().exec(new String[]{"/home/ubuntu/shellScript.sh",core + "_" + BASICMODELS[i] + "_" + BVALUES[j] + "_" + NORMALISATIONS[k]});
								treEval.waitFor();
								writeToFile("/home/ubuntu/FinalOutput.txt",core + " " + "basicModel " + BASICMODELS[i] + " afterEffect " + BVALUES[j] + " NORMALISATION " + NORMALISATIONS[k] + " Map Value ---> " + getMapValue(readFile("/home/ubuntu/IRF16P3_Map_Output/"+core + "_" + BASICMODELS[i] + "_" + BVALUES[j] + "_" + NORMALISATIONS[k] +"_output.txt", StandardCharsets.UTF_8)) + "\n",true);
								fileContent = readFile(coreSchemaPath, StandardCharsets.UTF_8);
								startIndex = 0;
								schemaNewContent = "";
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static String readFile(String path, Charset encoding) throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded, encoding);
	}
	
	static String getMapValue(String fileContent){
		fileContent = fileContent.replaceAll("\t", "").replaceAll(" ", "");
		return fileContent.substring(fileContent.indexOf("mapall") + 6,fileContent.indexOf("\n",fileContent.indexOf("mapall")));
	}
	
	static void writeToFile(String path, String content, Boolean append){
		File file = new File(path);
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter(file,append);
			fileWriter.write(content);
			fileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
