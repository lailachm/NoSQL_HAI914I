package qengine.program;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;

/**
 * Le RDFHandler intervient lors du parsing de donnÃ©es et permet d'appliquer un
 * traitement pour chaque Ã©lÃ©ment lu par le parseur.
 * 
 * <p>
 * Ce qui servira surtout dans le programme est la mÃ©thode
 * {@link #handleStatement(Statement)} qui va permettre de traiter chaque triple
 * lu.
 * </p>
 * <p>
 * Ã€ adapter/rÃ©Ã©crire selon vos traitements.
 * </p>
 */
public final class RDFHandler extends AbstractRDFHandler {

	HashMap<Integer, String> dico = new HashMap<>();
	int key = 0;

	HashMap<HashMap<Integer, Integer>, Integer> indexSPO = new HashMap<HashMap<Integer, Integer>, Integer>();
	HashMap<HashMap<Integer, Integer>, Integer> indexSOP = new HashMap<HashMap<Integer, Integer>, Integer>();
	HashMap<HashMap<Integer, Integer>, Integer> indexPSO = new HashMap<HashMap<Integer, Integer>, Integer>();
	HashMap<HashMap<Integer, Integer>, Integer> indexOPS = new HashMap<HashMap<Integer, Integer>, Integer>();
	HashMap<HashMap<Integer, Integer>, Integer> indexPOS = new HashMap<HashMap<Integer, Integer>, Integer>();
	HashMap<HashMap<Integer, Integer>, Integer> indexOSP = new HashMap<HashMap<Integer, Integer>, Integer>();

	@Override
	public void handleStatement(Statement st) {
		// System.out.println("\n" + st.getSubject() + "\t " + st.getPredicate() + "\t "
		// + st.getObject());
		String subject = st.getSubject().toString();
		int s;
		/*
		 * Si le dictionnaire contient déjà la valeur qu'on essaye d'intégrer, on n'a
		 * pas besoin de créer une nouvelle clé, on utilise celle qui existe déjà. C'est
		 * cette clé qu'on va utiliser pour remplir les indexs.
		 */
		if (dico.containsValue(subject)) {
			int cle = getKeyByValue(dico, subject);
			s = cle;
			// dico.put(cle, subject);
		} else {
			s = key;
			dico.put(key, subject);
			key++;
		}

		String predicate = st.getPredicate().toString();
		int p;
		if (dico.containsValue(predicate)) {
			int cle = getKeyByValue(dico, predicate);
			p = cle;
		} else {
			p = key;
			dico.put(key, predicate);
			key++;
		}

		String object = st.getObject().toString();
		int o;
		if (dico.containsValue(object)) {
			int cle = getKeyByValue(dico, object);
			o = cle;
		} else {
			o = key;
			dico.put(key, object);
			key++;
		}

		indexSPO.put(new HashMap() {
			{
				put(s, p);
			}
		}, o);
		indexSOP.put(new HashMap() {
			{
				put(s, o);
			}
		}, p);
		indexPSO.put(new HashMap() {
			{
				put(p, s);
			}
		}, o);
		indexOPS.put(new HashMap() {
			{
				put(o, p);
			}
		}, s);
		indexPOS.put(new HashMap() {
			{
				put(p, o);
			}
		}, s);
		indexOSP.put(new HashMap() {
			{
				put(o, s);
			}
		}, p);
		
		int oo;
		int pp;
		int ss;
		for ( HashMap<Integer, Integer> key : indexOSP.keySet() ) {
			for (Integer supKey : key.keySet()) {
				
			}
		}
	};
	
	public HashMap<Integer, String> getDico() {
		return dico;
	}

	public HashMap<HashMap<Integer, Integer>, Integer> getIndexSPO() {
		return indexSPO;
	}

	public void printDico() {
		System.out.println("-----------------------------");
		System.out.println("Le dictionnaire est le suivant : " + dico);
		System.out.println("-----------------------------\n");
	}

	public void printIndexSPO() {
		System.out.println("-----------------------------");
		System.out.println("L'index SPO : " + indexSPO);
		System.out.println("-----------------------------\n");
	}

	public void printIndexSOP() {
		System.out.println("-----------------------------");
		System.out.println("L'index SOP : " + indexSOP);
		System.out.println("-----------------------------\n");
	}

	public void printIndexPSO() {
		System.out.println("-----------------------------");
		System.out.println("L'index PSO : " + indexPSO);
		System.out.println("-----------------------------\n");
	}

	public void printIndexOPS() {
		System.out.println("-----------------------------");
		System.out.println("L'index OPS : " + indexOPS);
		System.out.println("-----------------------------\n");
	}

	public void printIndexPOS() {
		System.out.println("-----------------------------");
		System.out.println("L'index POS : " + indexPOS);
		System.out.println("-----------------------------\n");
	}

	public void printIndexOSP() {
		System.out.println("-----------------------------");
		System.out.println("L'index OSP : " + indexOSP);
		System.out.println("-----------------------------\n");
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
}