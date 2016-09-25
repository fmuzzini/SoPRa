import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.w3c.dom.DOMException;

/**
 * Calcola gli score secondo l'agoritmo SoPRa Extendend
 */
public class ExtendedRisultati {
	
	private final float beta = 0.5f;
	
	private ScoreDoc[] res;
	private IndexReader userir;
	
	public ExtendedRisultati(LuceneRisultati res, HashMap<String, HashMap<String, String>> userdoc, int u, IndexReader userir, IndexReader tagir) throws IOException, XPathExpressionException, DOMException, ParseException{
		this(res,userdoc,u,userir,tagir,0.5f);
	}

	public ExtendedRisultati(LuceneRisultati res, HashMap<String, HashMap<String, String>> userdoc, int u, IndexReader userir, IndexReader tagir, float ep) throws IOException, XPathExpressionException, DOMException, ParseException{
		ScoreDoc[] luc = res.getScore();
		this.res = new ScoreDoc[luc.length];
		this.userir = userir;
		VettoreTag pu = new VettoreTag(userir, u);
		VettoreQuery q = new VettoreQuery(res.getQuery());
		
		for(int i=0; i<luc.length; i++){
			float sum1 = 0f;
			float sum2 = 0f;
			String docID = tagir.document(luc[i].doc).get("docID");
			int[] puk_ = get_user_had_tagged(docID, userdoc);


			for (int k=0; k<puk_.length; k++){
				
				VettoreTag puk = new VettoreTag(userir, puk_[k]);
				VettoreUserTag tukd = new VettoreUserTag(tagir, userir, userdoc, puk_[k], luc[i].doc);
				
				
				float cos_user = Coseno.coseno(puk, pu);
				sum1 += cos_user + Coseno.coseno(pu, tukd);
				sum2 += cos_user + Coseno.coseno(q, tukd);

			} 
			
			
			float score = ep * sum1 + (1-ep) * (beta * sum2 + (1-beta) * luc[i].score);
			
			this.res[i] = new ScoreDoc(luc[i].doc, score);
		}
	}

	/**
	 * Ritorna gli utenti che hanno taggato quel documento
	 */
	private int[] get_user_had_tagged(String doc, HashMap<String, HashMap<String, String>> userdoc) throws IOException {
		HashMap<String, String> users = userdoc.get(doc);
		String res[] = new String[users.size()]; 
		int j = 0;
		for(Entry<String, String> e : users.entrySet()){
			res[j++] = e.getKey();
		}
		
		int[] r = new int[res.length];
		for (int i=0; i<res.length; i++){
			r[i] = SoPRa.getUser(res[i], userir);
		}
		
		return r;
	}

	public ScoreDoc[] getScore() {
		return res;
	}
	
}
