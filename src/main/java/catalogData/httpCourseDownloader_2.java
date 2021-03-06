package catalogData;

import models.Course;
import models.Section;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Scanner;


//import java.io.*;


public class httpCourseDownloader_2 {
	//private static String[] DEPTS = {"A+HTG", "ACC", "AEROS", "AFRIK", "AM+ST", "ANES", "ANTHR", "ARAB", "ARTHC", "ASIAN", "ASL", "BIO", "BULGN", "BUS+M", "C+S", "CANT", "CE+EN", "CH+EN", "CHEM", "CHIN", "CL+CV", "CLSCS", "CM", "CMLIT", "CMPST", "COMD", "COMMS", "CPSE", "CSANM", "CZECH", "DANCE", "DIGHT", "DUTCH", "EC+EN", "ECE", "ECON", "EDLF", "EIME", "EL+ED", "ELANG", "EMBA", "ENG+T", "ENGL", "ESL", "EUROP", "EXSC", "FIN", "FINN", "FLANG", "FNART", "FPM", "FREN", "GEOG", "GEOL", "GERM", "GREEK", "HCOLL", "HEB", "HIST", "HLTH", "HONRS", "IAS", "ICLND", "IHUM", "INDES", "IP&T", "IR", "IS", "IT", "ITAL", "JAPAN", "KOREA", "LATIN", "LAW", "LFSCI", "LING", "LINGC", "LT+AM", "M+B+A", "M+COM", "MATH", "ME+EN", "MESA", "MFG", "MFHD", "MFT", "MIL+S", "MMBIO", "MTHED", "MUSIC", "NDFS", "NE+LG", "NES", "NEURO", "NORWE", "NURS", "ORG+B", "P+MGT", "P+POL", "PDBIO", "PETE", "PHIL", "PHSCS", "PHY+S", "PL+SC", "POLSH", "PORT", "PSYCH", "PWS", "RECM", "REL+A", "REL+C", "REL+E", "ROM", "RUSS", "SC+ED", "SCAND", "SFL", "SLAT", "SOC", "SOC+W", "SPAN", "STAC", "STAT", "STDEV", "SWED", "T+ED", "TECH", "TEE", "TELL", "TMA", "UNIV", "VA", "VAANM", "VADES", "VAEDU", "VAGD", "VAILL", "VAPHO", "VASTU", "WELSH", "WRTG", "WS"};



    public static String executePost(String targetURL, String urlParameters)
	  {
	    URL url;
	    HttpURLConnection connection = null;  
	    try {
	      //Create connection
	      url = new URL(targetURL);
	      connection = (HttpURLConnection)url.openConnection();
	      connection.setRequestMethod("POST");
	      connection.setRequestProperty("Content-Type", 
	           "application/x-www-form-urlencoded");
				
	      connection.setRequestProperty("Content-Length", "" + 
	               Integer.toString(urlParameters.getBytes().length));
	      connection.setRequestProperty("Content-Language", "en-US");  
				
	      connection.setUseCaches (false);
	      connection.setDoInput(true);
	      connection.setDoOutput(true);

	      //Send request
	      DataOutputStream wr = new DataOutputStream (
	                  connection.getOutputStream ());
	      wr.writeBytes (urlParameters);
	      wr.flush ();
	      wr.close ();

	      //Get Response	
	      InputStream is = connection.getInputStream();
	      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	      String line;
	      StringBuffer response = new StringBuffer(); 
	      
	      boolean firstLine = true;
	      while((line = rd.readLine()) != null) {
	       
	    	if (firstLine)
	    		firstLine = false;
	    	else
	    		response.append('\r');
	    	response.append(line);
	      }
	      
	      rd.close();
	      return response.toString();

	    } catch (Exception e) {

	      e.printStackTrace();
          System.out.println("Continuing :)");
	      return "";

	    } finally {

	      if(connection != null) {
	        connection.disconnect(); 
	      }
	    }
	  }
	 
	 public static void main(String[] args) throws IOException {
         String semesterCode = "20145"; // TODO - Find each semester code


         byte[] data = downloadCourses(semesterCode);

         //TODO: send baos to parser - add all to database

         //write baos to file
         String outputFileName = "test1.txt";
         createCourseDataFile(outputFileName, data);
	}

    public static void createCourseDataFile(String outputFileName, byte[] data) throws FileNotFoundException, UnsupportedEncodingException {

        PrintWriter writer = new PrintWriter(outputFileName, "UTF-8");
        String contents = new String(data);
        writer.print(contents);
        writer.flush();
        writer.close();
    }

    private static byte[] downloadCourses(String semesterCode) throws IOException {
        OutputStream os = new ByteArrayOutputStream();

        String creditType = "A"; //Figure out what this means, also "S"
        
        for(String dept : DepartmentDownloader.getHTMLdeptCodes(semesterCode)){
           System.out.println(dept);
           
           String targetURL = "http://saasta.byu.edu/noauth/classSchedule/ajax/searchXML.php";
           String urlParams = "SEMESTER=" + semesterCode + "&CREDIT_TYPE=" + creditType + "&DEPT="+ dept +"&INST=&DESCRIPTION=&DAYFILTER=&BEGINTIME=&ENDTIME=&SECTION_TYPE=&CREDITS=&CREDITCOMP=&CATFILTER=&BLDG=";
           String out = executePost(targetURL, urlParams);

            os.write(out.getBytes(Charset.forName("UTF-8")));
        }

        os.flush();
        byte[] result = removeHtml(((ByteArrayOutputStream)os).toByteArray());

        os.close();
        return result;
    }


    public static byte[] removeHtml(byte[] data) throws IOException {
        InputStream is = new ByteArrayInputStream(data);
        OutputStream os = new ByteArrayOutputStream();

        String htmlString = new Scanner(is).useDelimiter("\\Z").next();
        htmlString=htmlString.replaceAll("<br>","\n");
        htmlString=htmlString.replaceAll("</li>","\n");
        htmlString=htmlString.replaceAll("#R#", "R#");
        htmlString=htmlString.replaceAll("#A#", "A#");
        htmlString=htmlString.replaceAll("#M#", "M#");
        htmlString=htmlString.replaceAll("#B#", "B#");
        htmlString=htmlString.replaceAll("#D#", "D#");
        String noHTMLString = htmlString.replaceAll("\\<.*?\\>", "");
        noHTMLString += System.getProperty("line.separator");

        //System.out.println(noHTMLString);
        os.write(noHTMLString.getBytes());
        os.flush();
        byte[] result = ((ByteArrayOutputStream) os).toByteArray();
        os.close();
        is.close();
        //CompareFile();
        return result;
    }


    private static void CompareFile() throws Exception {

        File f1 = new File("D:\\CS340\\CS340\\Test\\src\\TestCatalog.txt");
        File f2 = new File("D:\\CS340\\CS340\\Test\\src\\catalog.txt");

        FileReader fR1 = new FileReader(f1);
        FileReader fR2 = new FileReader(f2);

        BufferedReader reader1 = new BufferedReader(fR1);
        BufferedReader reader2 = new BufferedReader(fR2);

        String line1 = null;
        String line2 = null;
        int count =0;

        while (((line1 = reader1.readLine()) != null) &&((line2 = reader2.readLine()) != null)) {
            if (!line1.equalsIgnoreCase(line2)) {
                System.out.println("The files are DIFFERENT on line "+ count);
            } else {
                //   System.out.println("The files are identical on line "+ count);
            }
            count++;

        }
        reader1.close();
        reader2.close();
    }

    public static void getDataForCourse(PrintWriter writer, Section s, Course c, String semesterCode) {
		String targetURL;
		String urlParams;
		//Individual Course HTTP requests here
		//Need to parse this Data
		//Outcomes, Catalog, Syllabus
		String courseID = c.getCourseID(); //"02859";
		String titleCode = c.getNewTitleCode(); //"006";
		//Catalog, Syllabus
		String section = s.getSectionID(); //"052";
		String yearTerm = semesterCode; //Spring = "20135"
		String creditType= "S";
		//Only Syllabus
		String department = c.getDepartment();//"A+HTG";
		String CAT = "100";

        //Outcomes
        String outcomes = getCourseOutcomes(courseID, titleCode);

        urlParams ="CUR_ID="+ courseID + "&TITLE_CODE=" + titleCode;
		writer.println("<br>OUTCOMES= " + urlParams + "<br>");
		writer.println(outcomes);
        System.out.println("OUTCOMES:\n" + outcomes);
		
		//CatalogInfo
		targetURL = "http://saasta.byu.edu/noauth/classSchedule/ajax/getCatalogInfo.php";
		urlParams = "CUR_ID=" + courseID +"&TITLE_CODE=" + titleCode + "&SECTION_NUM=" + section + "&YEAR_TERM=" + yearTerm + "&CREDIT_TYPE=" + creditType;
        System.out.println(targetURL +"?"+ urlParams);
        String catalogInfo = executePost(targetURL, urlParams);
		
		writer.println("<br>CAT_INFO= " + urlParams + "<br>");
		writer.println(catalogInfo);
        System.out.println("CATALOG_INFO:\n" + catalogInfo);
		
		//Syllabus
		targetURL = "http://saasta.byu.edu/noauth/classSchedule/ajax/getSyllabus.php";
		urlParams = "CUR_ID=" + courseID + "&TITLE_CODE=" + titleCode + "&YEAR_TERM=" + yearTerm + "&SECTION=" + section + "&DEPT=" + department + "&CAT=" + CAT;
        System.out.println(targetURL +"?"+ urlParams);
        String syllabus = executePost(targetURL, urlParams);
		
		writer.println("<br>SYLLABUS= " + urlParams + "<br>");
		writer.println(syllabus);
        System.out.println("SYLLABUS:\n" + syllabus);
	}

    public static String getCourseOutcomes(String courseID, String titleCode){
        //Outcomes
        String targetURL = "http://saasta.byu.edu/noauth/classSchedule/ajax/getOutcomes.php";
        String urlParams ="CUR_ID="+ courseID + "&TITLE_CODE=" + titleCode;
        String outcomes = executePost(targetURL, urlParams);


        System.out.println("\n\nURL:\n"+ targetURL +"?"+ urlParams + "\n" + outcomes);
        return outcomes;
    }
}
