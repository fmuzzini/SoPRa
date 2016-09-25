import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.SAXException;


public class Test {
	
	private static final String user_dir = "../index/user";
	private static final String doc_dir = "../index/doc";
	private final static String hashfile = "../hash.data";
	
	private static final int n_test = 2000;

	/**
	 * Classe che genera casualmente query da un utente e ne ricava i risultati pertinenti.
	 * Alla fine calcola il Mean Average Precision e il Mean Reciprocal Rank
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, ParseException, ClassNotFoundException {
		float ep;
		try{
			ep = Float.parseFloat(args[0]);
		} catch (Exception e) {
			ep = 0.5f;
		}
		
		// reader utenti
		Directory index_user = new SimpleFSDirectory(new File(user_dir));
		IndexReader reader_user = IndexReader.open(index_user);
		
		//reader documenti
		Directory index_doc = new SimpleFSDirectory(new File(doc_dir));
		IndexReader reader_doc = IndexReader.open(index_doc);
		
		//Mappa tra documento-user-tag
		FileInputStream in = new FileInputStream(hashfile);
        ObjectInputStream s = new ObjectInputStream(in);
        @SuppressWarnings("unchecked")
		HashMap<String, HashMap<String, String>> userdoc = (HashMap<String, HashMap<String, String>>) s.readObject();
        s.close();
		
		float map_lucene = 0f;
		float map_sora = 0f;
		float map_sopra = 0f;
		float map_ext = 0f;
		float mrr_lucene = 0f;
		float mrr_sora = 0f;
		float mrr_sopra = 0f;
		float mrr_ext = 0f;
		for (int i=0; i<n_test; i++){
			
			int user = getRandomUser(userdoc);
			int userindex = getUser(new Integer(user).toString(), reader_user);
			String term = getRandomTerm(userindex, reader_user);
			HashMap<Integer, Integer> ra = getRelevant(user, term, userdoc);
			
			
			LuceneRisultati res = new LuceneRisultati(term, reader_doc);
		    SoraRisultati sora = new SoraRisultati(res);
		    SopraRisultati sopra = new SopraRisultati(sora, userindex, reader_user, ep);
		    ExtendedRisultati ext = new ExtendedRisultati(res, userdoc, userindex, reader_user, reader_doc, ep);
		    		    
		    ScoreDoc[] hits_lucene = res.getScore();
		    ScoreDoc[] hits_sora = sora.getScore();
		    ScoreDoc[] hits_sopra = sopra.getScore();
		    ScoreDoc[] hits_ext = ext.getScore();
		    
		    Arrays.sort(hits_lucene, new ScoreDocComparator());
		    Arrays.sort(hits_sora, new ScoreDocComparator());
		    Arrays.sort(hits_sopra, new ScoreDocComparator());
		    Arrays.sort(hits_ext, new ScoreDocComparator());

		    int[] hits_lucene_id = getId(hits_lucene, reader_doc);	    
		    int[] hits_sora_id = getId(hits_sora, reader_doc);	
		    int[] hits_sopra_id = getId(hits_sopra, reader_doc);
		    int[] hits_ext_id = getId(hits_ext, reader_doc);	
		    
		    
		    map_lucene += getAverangePrecision(hits_lucene_id, ra);
		    map_sora += getAverangePrecision(hits_sora_id, ra);
		    map_sopra += getAverangePrecision(hits_sopra_id, ra);
		    map_ext += getAverangePrecision(hits_ext_id, ra);
		    
		    mrr_lucene += 1/getMaxRank(hits_lucene_id, ra);
		    mrr_sora += 1/getMaxRank(hits_sora_id, ra);
		    mrr_sopra += 1/getMaxRank(hits_sopra_id, ra);
		    mrr_ext += 1/getMaxRank(hits_ext_id, ra);
		    
		    System.out.println("Fatti " + (i+1) + " Di " + n_test);
		}
		
		System.out.println("ep = " + ep);
		System.out.println("map_lucene: " + map_lucene/n_test);
		System.out.println("map_sora: " + map_sora/n_test);
		System.out.println("map_sopra: " + map_sopra/n_test);
		System.out.println("map_ext: " + map_ext/n_test);
		System.out.println("mrr_lucene: " + mrr_lucene/n_test);
		System.out.println("mrr_sora: " + mrr_sora/n_test);
		System.out.println("mrr_sopra: " + mrr_sopra/n_test);
		System.out.println("mrr_ext: " + mrr_ext/n_test);
		

	}

	/**
	 * Ritorna una Mappa con i risultati rilevanti
	 */
	private static HashMap<Integer, Integer> getRelevant(int user_, String term, HashMap<String, HashMap<String, String>> userdoc)  {		
		String user = new Integer(user_).toString();
		HashMap<Integer, Integer> res = new HashMap<Integer, Integer>();
		for(Entry<String, HashMap<String, String>> e : userdoc.entrySet()){
			HashMap<String, String> v = e.getValue();
			String t = v.get(user);
			if(t != null && t.contains(term)){
				res.put(Integer.parseInt(e.getKey()), null);	
			}
		}		
		return res;
	}

	
	
	/**
	 * Ritorna un termine utilizzato dall'utente
	 */
	private static String getRandomTerm(int id, IndexReader reader) throws ParseException, IOException {
	    String tag = reader.document(id).get("tag");
	    String term[] = tag.split(" ");
	    int n = (new Random()).nextInt(term.length-1);
	    
	    return term[n];
	}

	/**
	 * Ritorna un utente casuale
	 */
	private static int getRandomUser(HashMap<String, HashMap<String, String>> userdoc) throws XPathExpressionException {
		Object[] keydoc = userdoc.keySet().toArray();
		int kd = new Random().nextInt(keydoc.length);
		HashMap<String, String> users = userdoc.get(keydoc[kd]);
		
		Object[] userkey = users.keySet().toArray();
		int ku = new Random().nextInt(userkey.length);
		
		return Integer.parseInt((String) userkey[ku]);
	}

	/**
	 * Ritorna il max rank raggiunto da un risultato rilevante
	 */
	private static int getMaxRank(int[] hits,
			HashMap<Integer, Integer> ra) {
		for(int i=0; i<hits.length; i++){
			if(ra.containsKey(hits[i])){
				return i+1;
			}
		}
		
		return hits.length + 1;
	}

	/**
	 * Ritorna l'id dei documenti
	 */
	private static int[] getId(ScoreDoc[] hits, IndexReader reader) throws NumberFormatException, CorruptIndexException, IOException {
		int[] res = new int[hits.length];
		for(int i=0; i<hits.length; i++){
			res[i] = Integer.parseInt(reader.document(hits[i].doc).get("docID"));
		}
		
		return res;
	}

	/**
	 * Ritorna l'Averange Precision
	 */
	private static float getAverangePrecision(int[] hits,
			HashMap<Integer, Integer> ra) {
		int n = ra.size();
		float p[] = new float[n+1];
		float ip[] = new float[11];
		
		//calcolo p nei vari livelli
		int find = 0;
		p[0] = 0;
		for(int i=0; i<hits.length; i++){
			if(ra.containsKey(hits[i])){
				find++;
				p[find] = ((float)find)/(i+1);
			}
		}
		
		//Inserisco i valori di p nei livelli standardizzati
		for(int i=0; i<n; i++){
			int r = i/n;
			ip[r] = p[i];
		}
		//Calcolo la precision interpolata per i livelli standard
		for (int i=1; i<11; i++){
			ip[i] = ip[i] > ip[i-1] ? ip[i] : ip[i-1];
		}
		
		//Calcola e ritorna la media
		float sum = 0f;
		for (int i=0; i<11; i++){
			sum += ip[i];
		}
		
		return sum/11;
	}
	
	/**
	 * Ritorna l'indice lucene dell'user
	 */
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
