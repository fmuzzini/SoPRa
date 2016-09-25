import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;


/**
 * Classe main
 */
public class SoPRa {

	private static final String user_dir = "../index/user";
	private static final String doc_dir = "../index/doc";
	private final static String hashfile = "../hash.data";
	

	/**
	 * @param args
	 * @throws IOException 
	 * @throws LockObtainFailedException 
	 * @throws CorruptIndexException 
	 * @throws ParseException 
	 * @throws DOMException 
	 * @throws XPathExpressionException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws CorruptIndexException, LockObtainFailedException, IOException, ParseException, XPathExpressionException, DOMException, ParserConfigurationException, SAXException, ClassNotFoundException {
		if (args.length < 2){
			System.out.println("Usare primo argomento l'id dell'utente seguito dalla query");
			System.exit(0);
		}
		
		// reader utenti
		Directory index_user = new SimpleFSDirectory(new File(user_dir));
		IndexReader reader_user = IndexReader.open(index_user);
		
		//reader documenti
		Directory index_doc = new SimpleFSDirectory(new File(doc_dir));
		IndexReader reader_doc = IndexReader.open(index_doc);
		
		
		FileInputStream in = new FileInputStream(hashfile);
        ObjectInputStream s = new ObjectInputStream(in);
        @SuppressWarnings("unchecked")
		HashMap<String, HashMap<String, String>> userdoc = (HashMap<String, HashMap<String, String>>) s.readObject();
        s.close();
		
		//get user
		int user = getUser(args[0], reader_user);
		if (user == -1){
			System.out.println("Utente inesistente");
			System.exit(0);
		}

	    // 2. query
	    String querystr = "";
	    for (int i=1; i<args.length; i++)
	    	querystr += args[i];
	    
	    LuceneRisultati res = new LuceneRisultati(querystr, reader_doc);
	    SoraRisultati sora = new SoraRisultati(res);
	    SopraRisultati sopra = new SopraRisultati(sora, user, reader_user);
	    ExtendedRisultati ext = new ExtendedRisultati(res, userdoc, user, reader_user, reader_doc);
	    
	    ScoreDoc[] hits_lucene = res.getScore();
	    ScoreDoc[] hits_sora = sora.getScore();
	    ScoreDoc[] hits_sopra = sopra.getScore();
	    ScoreDoc[] hits_ext = ext.getScore();
	    
	 // 4. display results
	    System.out.println("Risultati Lucene");
	    displayRisultati(hits_lucene, reader_doc);
	    
	    System.out.println("Risultati SoRa");
	    displayRisultati(hits_sora, reader_doc);
	    
	    System.out.println("Risultati SoPRa Basic");
	    displayRisultati(hits_sopra, reader_doc);
	    
	    System.out.println("Risultati SoPRa Extended");
	    displayRisultati(hits_ext, reader_doc);
	    


	}
	
	
	private static void displayRisultati(ScoreDoc[] hits, IndexReader reader) throws CorruptIndexException, IOException{
		Arrays.sort(hits, new ScoreDocComparator());
		int show = 10;
		int len = hits.length < show ? hits.length : show;
		for(int i=0;i<len;++i) {
		      int docId = hits[i].doc;
		      Document d = reader.document(docId);
		      System.out.println((i + 1) + ". " + "\t" + d.get("docID") + "\t" + hits[i].score);
		    }
	}
	
	
	public static int getUser(String id, IndexReader reader) throws IOException, ParseException {
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
		Query q = new QueryParser(Version.LUCENE_36, "userID", analyzer).parse(id);
		
		IndexSearcher searcher = new IndexSearcher(reader);
	    TopScoreDocCollector collector = TopScoreDocCollector.create(1, true);
	    searcher.search(q, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	    searcher.close();
	    
	    if (hits.length == 0)
	    	return -1;
	    
		return hits[0].doc;
	}
	
	


}
