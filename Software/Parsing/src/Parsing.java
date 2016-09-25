import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.jsoup.Jsoup;

/**
 * Classe per il parsing del dataset.
 * Inserisce in lucene i documenti e i tag
 * e crea le strutture dati necessarie.
 *
 */
public class Parsing {
	
	private final static String tagFile = "tags.dat";
	private final static String bookmarkFile = "user_taggedbookmarks-timestamps.dat";
	private final static String urlFile = "bookmarks.dat";
	private final static String root = "../hetrec";
	
	private final static String indexUserFile = "../index/user";
	private final static String indexDocFile = "../index/doc";
	private final static String hashfile = "../hash.data";


	public static void main(String[] args) {
		
		//tag
		InputStream fis = null;;
		try {
			fis = new FileInputStream(root + "/" + tagFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    InputStreamReader isr = new InputStreamReader(fis);
	    BufferedReader tagBuf = new BufferedReader(isr);
	    
	    //mapping tra tag id e tag testuale
	    HashMap<Integer, String> tagMap = new HashMap<Integer, String>();
	    
	    try {
			tagBuf.readLine();
			
			String line = null;
			while((line = tagBuf.readLine()) != null){
				String[] tok = line.split("\t");
				tagMap.put(Integer.parseInt(tok[0]), tok[1]);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	    //doc
	    fis = null;;
		try {
			fis = new FileInputStream(root + "/" + urlFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    isr = new InputStreamReader(fis);
	    BufferedReader docBuf = new BufferedReader(isr);
	    
	    //mapping tra doc id e url
	    HashMap<Integer, String> docMap = new HashMap<Integer, String>();
	    
	    try {
			docBuf.readLine();
			
			String line = null;
			while((line = docBuf.readLine()) != null){
				String[] tok = line.split("\t");
				docMap.put(Integer.parseInt(tok[0]), tok[3]);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	  //bookmark
	    fis = null;;
		try {
			fis = new FileInputStream(root + "/" + bookmarkFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    isr = new InputStreamReader(fis);
	    BufferedReader bookmarkBuf = new BufferedReader(isr);
	    
	    UserDocWriter udw = new UserDocWriter(hashfile);
	    
	    /* mapping tra doc id e url
		* - userTagMap: indice che contiene tutti i tag usati dall’i-esimo utente
		* - docTagMap: indice che contiene tutti i tag usati per l’i-esimo documento
		*/
	    HashMap<Integer, String> userTagMap = new HashMap<Integer, String>();
	    HashMap<Integer, String> docTagMap = new HashMap<Integer, String>();
	    
	    int old_doc = 0;
	    int old_user = 0;
	    String tot_tag = "";
	    
	    try {
			bookmarkBuf.readLine();
			
			String line = null;
			while((line = bookmarkBuf.readLine()) != null){
				String[] tok = line.split("\t");
				int user = Integer.parseInt(tok[0]);
				int doc = Integer.parseInt(tok[1]);
				int tag = Integer.parseInt(tok[2]);


				/*
				 * Controllo che sia presente una entry corrispondente (rispettivamente) a
				 * user (ID) nella userTagMap e a doc (ID) nella docTagMap.
				 * Nel caso sia già presente la coppia chiave-valore, si concatena ai valori delle due HashMap
				 * il tag (stringa ottenuta dal suo id) parsato dal file dei bookmarks. 
				 * Qualora non sia presente si creerà una nuova entry per entrambe le HashMap
				 * usando il metodo put(key,value).
				 * 
				 */
				
				String tagstr = tagMap.get(tag);
				
				String utag = userTagMap.get(user);
				String dtag = docTagMap.get(doc);
				
				String usertags = tagstr;
				String doctags = tagstr;
				
				if (utag != null)
					usertags = utag.concat(" " + tagstr);
				
				userTagMap.put(user, usertags);
				
				if (dtag != null)
					doctags = dtag.concat(" " + tagstr);
				
				docTagMap.put(doc, doctags);
				

				/*
				 * Si concatenano tutti i tag usati dallo stesso utente per lo stesso
				 * documento, dopodiché si “esporta” in una Map
				 *
				 *
				 */

				if (doc == old_doc && user == old_user)
					tot_tag = tot_tag + " " + tagstr;
				else{
					if (old_user != 0 && old_doc != 0){
						udw.add(old_user, old_doc, tot_tag);
					}
					
					tot_tag = tagstr;
					old_doc = doc;
					old_user = user;
				}
				
			}

			udw.close();
			
			
			/*
			 * Aggiunta all’indice userTagMap di tutti gli utenti che hanno inserito almeno un tag.
			 * (Questo perché nel dataset sono presenti anche utenti che non hanno usato tag.)
			 */

			Directory index = new SimpleFSDirectory(new File(indexUserFile));
			Analyzer analyzer = new EnglishAnalyzer(Version.LUCENE_36);
			IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer );
			IndexWriter uiw = new IndexWriter(index, config);
			
			for(Map.Entry<Integer, String> entry : userTagMap.entrySet()){
				luceneWriteUser(uiw, entry.getKey(), entry.getValue());
			}
			
			uiw.close();
			index.close();
			

			/*
			 * Aggiunta all’indice docTagMap di tutti i documenti che hanno almeno un tag:
			 * come key si usa il docID mentre come valore corrispondente si ha una stringa con la
			 * concatenazione dei tag usati per quel documento.
			 */

			Directory indexDoc = new SimpleFSDirectory(new File(indexDocFile));
			IndexWriterConfig configDoc = new IndexWriterConfig(Version.LUCENE_36, analyzer );
			IndexWriter diw = new IndexWriter(indexDoc, configDoc);
			
			int size = docMap.size();
			int fatti = 0;
			for(Map.Entry<Integer, String> entry : docMap.entrySet()){
				luceneWriteDoc(diw, docTagMap, entry.getKey(), entry.getValue());
				
				fatti++;
				if (fatti % 100 == 0){
					System.out.println("Fatti " + fatti +" di " + size);
				}
			}
			
			diw.close();
			indexDoc.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    

	}

	private static void luceneWriteDoc(IndexWriter diw, Map<Integer, String> docTagMap, Integer id,
			String url) throws CorruptIndexException, IOException {
		String tag = docTagMap.get(id);
		tag = tag != null ? tag : "";
		
		Document doc = new Document();
		doc.add(new Field("docID", id.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
		doc.add(new Field("URL", url, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
		doc.add(new Field("text", getDocFromURL(url), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
		doc.add(new Field("tag", tag, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
		diw.addDocument(doc);
	}


	/*
	* Download delle pagine dagli url corrispondenti ai docID.
	* A seconda del formato (PDF, HTML, msword) del documento scaricato
	* avviene un corrispondente parsing.
	*/

	private static String getDocFromURL(String value) {
		try {
			URL url = new URL(value);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			int code = con.getResponseCode();
			if (code != 200)
				return "";
			
			String type = con.getHeaderFields().get("Content-Type").get(0);
			return parseDoc(type, con.getInputStream());
		} catch (Exception e) {
			System.out.println("URL: " + value + " Skipped: " + e.getMessage());
			return "";
		}
		
		
	}

	private static String parseDoc(String type, InputStream responseMessage) {
		if (type.equals("application/pdf"))
			return parsePDF(responseMessage);
		
		if (type.equals("application/msword"))
			return parseDOC(responseMessage);
		
		return parseHTML(responseMessage);
	}

	private static String parseHTML(InputStream responseMessage) {
		String str = "";
		String s = null;
		InputStreamReader i = new InputStreamReader(responseMessage);
		BufferedReader buf = new BufferedReader(i);
		try {
			while((s = buf.readLine()) != null){
				str = str + s + "\n";
			}
		} catch (IOException e) {
			return str;
		}
		
		return Jsoup.parse(str).text();
	}

	private static String parseDOC(InputStream responseMessage) {
		try {
			WordExtractor ex = new WordExtractor(responseMessage);
			String str = ex.getText();
			ex.close();
			return str;
		} catch (IOException e) {
			return "";
		}
		
		
	}

	private static String parsePDF(InputStream pdf) {
		try {
			PDDocument doc = PDDocument.load(pdf);
			PDFTextStripper st = new PDFTextStripper();
			String str = st.getText(doc);
			doc.close();
			return str;
		} catch (Exception e) {
			return "";
		}
	}

	private static void luceneWriteUser(IndexWriter uiw, Integer key,
			String value) throws CorruptIndexException, IOException {
		Document doc = new Document();
		doc.add(new Field("userID", key.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO));
		doc.add(new Field("tag", value, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.YES));
		uiw.addDocument(doc);
		
	}

}
