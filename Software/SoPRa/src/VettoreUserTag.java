import java.io.IOException;
import java.util.HashMap;

import javax.xml.xpath.XPathExpressionException;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;


/**
 * Vettore che rappresenta i tag di un utente su un documento
 */
@SuppressWarnings("serial")
public class VettoreUserTag extends Vettore {

	public VettoreUserTag(IndexReader tagir, IndexReader userir, HashMap<String, HashMap<String, String>> userdoc, int u, int doc) throws IOException, XPathExpressionException {
		this(new VettoreTag(tagir, doc), tagir, userir, userdoc, u, doc);	
	}

	/**
	 * Partendo dal vettore di tutti i tag su un documento mantiene solo quelli dell'utente
	 * 
	 */
	public VettoreUserTag(VettoreTag vettoreTag, IndexReader tagir, IndexReader userir, HashMap<String, HashMap<String, String>> userdoc, int u_, int doc_) throws XPathExpressionException, NumberFormatException, CorruptIndexException, IOException {
		super();
		int u = Integer.parseInt(userir.document(u_).get("userID"));
		int doc = Integer.parseInt(tagir.document(doc_).get("docID"));
		String t = getUserDocTag(userdoc, u, doc);
		String[] tags = t.split(" ");
		float norm2 = 0f;
		
		for (int i=0; i<tags.length; i++){
			TermScore ts = vettoreTag.getByTerm(tags[i]);
			
			if(ts != null){
				super.add(ts);
				norm2 += ts.score * ts.score;
			}
		}
		
		super.setNorm((float) Math.sqrt(norm2));
	}

	/**
	 * Ritorna una Stringa contente i tag dell'utente su quel documento 
	 */
	private String getUserDocTag(HashMap<String, HashMap<String, String>> userdoc, int u, int doc) throws XPathExpressionException {
		HashMap<String, String> users = userdoc.get(new Integer(doc).toString());
		String res = users.get(new Integer(u).toString());
		
		return res;
	}

}
