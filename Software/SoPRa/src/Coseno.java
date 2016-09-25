

public class Coseno {
	
	/**
	 * Calcola il coseno tra due vettori
	 */
	public static float coseno(Vettore a, Vettore b){
		Vettore corto = null;
		Vettore lungo = null;
		if (a.size() < b.size()){
			corto = a;
			lungo = b;
		}
		else {
			corto = b;
			lungo = a;
		}
		
		if (corto.size() == 0)
			return 0;
		
		lungo.sort();
		
		float sopra, sotto1, sotto2;
		sopra = 0;
		sotto1 = a.getNorm();
		sotto2 = b.getNorm();
		
		//la sommatoria avviene solo su i termini presenti in entrambi i vettori
		for (int i=0; i<corto.size(); i++){
			String term = corto.getTerm(i);
			float sa = corto.getScore(i);
			float sb = 0f;
			TermScore t = lungo.getByTerm(term);
			
			if(t != null)
				sb = t.score;
			
			sopra += sa * sb;
		}
		
		return (float) (sopra / (sotto1 * sotto2));
	}

}
