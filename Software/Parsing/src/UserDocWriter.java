import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.tartarus.snowball.ext.PorterStemmer;

/**
 * Crea e salva la mappatura tra utente-docuemnto-tag:
 * ogni documento é associato (se esistono) a coppie utente (che ha taggato il documento)-tag usati
 * (stringa che contiene i tag in concatenazione).
 * 
 */
public class UserDocWriter {

	private ObjectOutputStream out;
	private HashMap<String, HashMap<String, String>> map;

	public UserDocWriter(String file){
		try {
			FileOutputStream os = new FileOutputStream(file);
			out = new ObjectOutputStream(os);
			map = new HashMap<String, HashMap<String, String>>();
			
			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void add(int user_, int doc_, String tag_){
		PorterStemmer stem = new PorterStemmer();
		stem.setCurrent(tag_);
		String tag = stem.getCurrent();
		String user = new Integer(user_).toString();
		String doc = new Integer(doc_).toString();
		
		HashMap<String, String> usertag = map.get(doc);
		if (usertag == null)
			usertag = new HashMap<String, String>();
		
		usertag.put(user, tag);
		map.put(doc, usertag);
		
	}
	
	public void close(){
		try {
			out.writeObject(map);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
