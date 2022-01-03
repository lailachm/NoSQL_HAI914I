package qengine.program;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.algebra.Projection;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.algebra.helpers.AbstractQueryModelVisitor;
import org.eclipse.rdf4j.query.algebra.helpers.StatementPatternCollector;
import org.eclipse.rdf4j.query.parser.ParsedQuery;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLParser;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;

/**
 * Programme simple lisant un fichier de requÃªte et un fichier de donnÃ©es.
 * 
 * <p>
 * Les entrÃ©es sont donnÃ©es ici de maniÃ¨re statique, Ã  vous de programmer
 * les entrÃ©es par passage d'arguments en ligne de commande comme demandÃ© dans
 * l'Ã©noncÃ©.
 * </p>
 * 
 * <p>
 * Le prÃ©sent programme se contente de vous montrer la voie pour lire les
 * triples et requÃªtes depuis les fichiers ; ce sera Ã  vous
 * d'adapter/rÃ©Ã©crire le code pour finalement utiliser les requÃªtes et
 * interroger les donnÃ©es. On ne s'attend pas forcÃ©mment Ã  ce que vous
 * gardiez la mÃªme structure de code, vous pouvez tout rÃ©Ã©crire.
 * </p>
 * 
 * @author Olivier Rodriguez <olivier.rodriguez1@umontpellier.fr>
 */
final class Main {
	static final String baseURI = null;

	/**
	 * Votre rÃ©pertoire de travail oÃ¹ vont se trouver les fichiers Ã  lire
	 */
	static final String workingDir = "data/";

	/**
	 * Fichier contenant les requÃªtes sparql
	 */
	static final String queryFile = workingDir + "sample_query.queryset";

	/**
	 * Fichier contenant des donnÃ©es rdf
	 */
	static final String dataFile = workingDir + "sample_data.nt";

	static HashMap<Integer, String> dico = new HashMap<Integer, String>();
	static HashMap<HashMap<Integer, Integer>, Integer> indexSPO = new HashMap<HashMap<Integer, Integer>, Integer>();
	// ========================================================================

	/**
	 * MÃ©thode utilisÃ©e ici lors du parsing de requÃªte sparql pour agir sur
	 * l'objet obtenu.
	 */
	public static void processAQuery(ParsedQuery query) {
		List<StatementPattern> patterns = StatementPatternCollector.process(query.getTupleExpr());

		/*
		 * Nous appelons dans un premier temps la fonction parseData qui permet de
		 * récupérer le dico et l'index sur lesquels nous allons évaluer les requêtes
		 */
		try {
			parseData();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * Ces deux hashmap vont contenir pour chaque statement de la requête,
		 * l'objet/le prédicat seulement s'ils apparaissent dans le dico. Nous aurons
		 * donc en clé le numéro de statement et en valeur l'objet/le prédicat.
		 */
		HashMap<Integer, Integer> listOfObjectsInQueryAndInDico = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> listOfPredicatesInQueryAndInDico = new HashMap<Integer, Integer>();

		/*
		 * Pour chaque élément du dictionnaire, nous allons y comparer les objets et
		 * prédicats. Si l'objet/le prédicat comparé est dans le dico, on l'ajoute à la
		 * liste correspondante.
		 */
		for (Integer element : dico.keySet()) { // pour chaque élément du dictionnaire
			int keyDico = element;
			String valueDico = dico.get(element);

			int numOfStatement = 0;
			for (StatementPattern pattern : patterns) { // pour chaque statement de la requête
				String predicateInQuery = pattern.getPredicateVar().getValue().toString();
				String objectInQuery = pattern.getObjectVar().getValue().toString();
				if (predicateInQuery.equals(valueDico)) {// si le prédicat apparaît dans le dico
					listOfPredicatesInQueryAndInDico.put(numOfStatement, keyDico);// on ajoute à la liste correspondante
																					// la clé du dico qui correspond à
																					// la valeur du prédicat
				}
				if (objectInQuery.equals(valueDico)) {
					listOfObjectsInQueryAndInDico.put(numOfStatement, keyDico);
				}
				numOfStatement++;

			}
		}

		//System.out.println("Les objets qui apparaissent dans la requête et le dico : " + listOfObjectsInQueryAndInDico);
		//System.out.println("Les prédicats qui apparaissent dans la requête et le dico : " + listOfPredicatesInQueryAndInDico);

		/*
		 * keyForLists est le numéro de statement dans lequel on se trouve. Cette
		 * variable permettre également d'arrêter le while une fois tous les statement
		 * évalués.
		 */
		int keyForLists = 0;
		/*
		 * Dans cette partie, nous comparons les objets et prédicats des statements qui
		 * apparaissent bien dans le dictionnaire, à l'index SPO. Ainsi, pour chaque
		 * statement, si l'objet O et le prédicat P sont un index de l'indexSPO, on
		 * ajoute le sujet S à une liste subjectsThatAnswerToStatementInQuery
		 */
		ArrayList<Integer> subjectsThatAnswerToStatementInQuery = new ArrayList<Integer>();
		while (keyForLists < patterns.size()) {
			int predicateInStatement = 9999999; // le prédicat garde cette valeur s'il n'apparaît pas dans le dico
			if (listOfPredicatesInQueryAndInDico.get(keyForLists) != null) {// au statement n°keyForLists, si le
																			// prédicat existe, il instancie la variable
																			// qui sera comparé à l'index
				predicateInStatement = listOfPredicatesInQueryAndInDico.get(keyForLists);
			}
			int objectInStatement = 9999999;
			if (listOfObjectsInQueryAndInDico.get(keyForLists) != null) {
				objectInStatement = listOfObjectsInQueryAndInDico.get(keyForLists);
			}

			for (HashMap<Integer, Integer> bigKeyIndex : indexSPO.keySet()) {// indexSPO est de la forme
																				// {subject=predicate}=object.
				int object = indexSPO.get(bigKeyIndex);// dans ce contexte, bigKeyIndex(clé) est {subject=predicate}, et
														// sa valeur est object
				for (Integer smallKeyIndex : bigKeyIndex.keySet()) {
					int predicate = bigKeyIndex.get(smallKeyIndex);
					int subject = smallKeyIndex;

					/*
					 * Pour chaque index, si l'objet et le prédicat de l'index match l'objet et le
					 * prédicat du statement, on ajoute le sujet à la liste
					 * subjectsThatAnswerToStatementInQuery
					 */
					if (objectInStatement != 9999999 && predicateInStatement != 9999999) {
						if (objectInStatement == object && predicateInStatement == predicate) {
							subjectsThatAnswerToStatementInQuery.add(subject);
						}
					}

				}
			}
			keyForLists++;
		}
		//System.out.println("Les sujets qui répondent aux statements de la requête : " + subjectsThatAnswerToStatementInQuery);

		/*
		 * La liste subjectsThatAnswerToStatementInQuery qui contient les sujets qui
		 * répondent aux différents statements de la requête doit être révisée afin
		 * d'obtenir une liste qui ne contient que les sujets répondant à la requête
		 * entière, càd à tous les statements.
		 */
		LinkedHashSet<Integer> subjectsThatAnswerToQuery = new LinkedHashSet<Integer>();
		for (int subject : subjectsThatAnswerToStatementInQuery) {
			int occurences = Collections.frequency(subjectsThatAnswerToStatementInQuery, subject);
			if (occurences == patterns.size()) { // si le sujet apparaît dans la liste autant de fois qu'il y a de
													// statements, c'est qu'il répond à la requête
				subjectsThatAnswerToQuery.add(subject);
			}
		}

		//System.out.println("Les sujets qui répondent à tous les statements : " + subjectsThatAnswerToQuery);

		for (int s : subjectsThatAnswerToQuery) {
			for (int key : dico.keySet()) {
				if (s == key) {
					//System.out.println("Subject answering the query (match in dico) : " + dico.get(key));
				}
			}
		}

//		System.out.println("first pattern : " + patterns.get(0));
//
//		System.out.println("object of the first pattern : " + patterns.get(0).getObjectVar().getValue());
//		System.out.println("predicate of the first pattern : " + patterns.get(0).getPredicateVar().getValue());
//
//		System.out.println("variables to project : ");

		// Utilisation d'une classe anonyme
//		query.getTupleExpr().visit(new AbstractQueryModelVisitor<RuntimeException>() {
//
//			public void meet(Projection projection) {
//				System.out.println(projection.getProjectionElemList().getElements());
//			}
//		});
	}

	/**
	 * EntrÃ©e du programme
	 */
	public static void main(String[] args) throws Exception {
		String pipee = " | ";
		String ligneCSV = dataFile + pipee + queryFile + pipee;
		
		int nbTripletsRDF = 0;
		try {
		      // create a new file object
		      File file = new File(dataFile);

		      // create an object of Scanner
		      // associated with the file
		      Scanner sc = new Scanner(file);

		      // read each line and
		      // count number of lines
		      while(sc.hasNextLine()) {
		        sc.nextLine();
		        nbTripletsRDF++;
		      }
		      //System.out.println("Total Number of Lines: " + count);

		      // close scanner
		      sc.close();
		    } catch (Exception e) {
		      e.getStackTrace();
		    }
		
		ligneCSV+=nbTripletsRDF + pipee;
		
		long time = System.currentTimeMillis();
		ligneCSV+=parseData();
		ligneCSV+=parseQueries();
		long endTime = System.currentTimeMillis() - time;
		//System.out.println("endtime : " + endTime + " ms\n");
		
		ligneCSV+=endTime;
		
		System.out.println("nom fichier données | nom fichier requetes | nombre de triplets | temps création dico | temps création index | nombre index | temps lecture données | nombre requetes | temps lecture requetes | temps total");
		System.out.println(ligneCSV);
	}

	// ========================================================================

	/**
	 * Traite chaque requÃªte lue dans {@link #queryFile} avec
	 * {@link #processAQuery(ParsedQuery)}.
	 */
	private static String parseQueries() throws FileNotFoundException, IOException {
		/**
		 * Try-with-resources
		 * 
		 * @see <a href=
		 *      "https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html">Try-with-resources</a>
		 */
		/*
		 * On utilise un stream pour lire les lignes une par une, sans avoir Ã  toutes
		 * les stocker entiÃ¨rement dans une collection.
		 */
		try (Stream<String> lineStream = Files.lines(Paths.get(queryFile))) {
			SPARQLParser sparqlParser = new SPARQLParser();
			Iterator<String> lineIterator = lineStream.iterator();
			StringBuilder queryString = new StringBuilder();
			int numQuery = 0;

			long tpsDebutLectureRequetes = System.currentTimeMillis();
			while (lineIterator.hasNext())
			/*
			 * On stocke plusieurs lignes jusqu'Ã  ce que l'une d'entre elles se termine par
			 * un '}' On considÃ¨re alors que c'est la fin d'une requÃªte
			 */
			{
				String line = lineIterator.next();
				queryString.append(line);

				/*
				 * CONSIGNE AFFICHAGE : • nom du fichier de données | nom du dossier des
				 * requêtes | nombre de triplets RDF | nombre de requêtes | temps de lecture des
				 * données (ms) | temps de lecture des requêtes (ms) | temps création dico (ms)
				 * | nombre d’index |temps de création des index (ms) | temps total d’évaluation
				 * du workload (ms)| temps total (du début à la fin du programme) (ms)
				 */

				if (line.trim().endsWith("}")) {
					ParsedQuery query = sparqlParser.parseQuery(queryString.toString(), baseURI);

					//System.out.println("******QUERY N°" + numQuery + "******");

					processAQuery(query); // Traitement de la requÃªte, Ã  adapter/rÃ©Ã©crire pour votre programme

					//System.out.println("******END QUERY N°" + numQuery + "******\n");
					numQuery++;

					queryString.setLength(0); // Reset le buffer de la requÃªte en chaine vide
				}

			}
			//System.out.println("numQuery : " + numQuery);
			long tpsLectureRequetes = System.currentTimeMillis() - tpsDebutLectureRequetes;
			//System.out.println("tpsLectureRequetes : " + tpsLectureRequetes + "ms");
			
			String sousLigneCSV = numQuery + " | " + tpsLectureRequetes + " | ";
			return sousLigneCSV;
		}
	}

	/**
	 * Traite chaque triple lu dans {@link #dataFile} avec {@link MainRDFHandler}.
	 */
	private static String parseData() throws FileNotFoundException, IOException {

		String pie = " | ";
		String sousLigneCSVData = "";
		long tpsDebutLectureDonnees = System.currentTimeMillis();
		try (Reader dataReader = new FileReader(dataFile)) {
			// On va parser des donnÃ©es au format ntriples
			RDFParser rdfParser = Rio.createParser(RDFFormat.NTRIPLES);

			// On utilise notre implÃ©mentation de handler
			MainRDFHandler rdfHandler = new MainRDFHandler();
			rdfParser.setRDFHandler(rdfHandler);

			// Parsing et traitement de chaque triple par le handler
			rdfParser.parse(dataReader, baseURI);

			long tpsDebutCreationDico = System.currentTimeMillis();
			dico = rdfHandler.getDico();
			long tpsCreationDico = System.currentTimeMillis() - tpsDebutCreationDico;
			//System.out.println("tpsCreationDico : " + tpsCreationDico + "ms");
//			rdfHandler.printDico();
//			rdfHandler.printIndexOPS();
//			rdfHandler.printIndexOSP();
//			rdfHandler.printIndexPOS();
//			rdfHandler.printIndexPSO();
//			rdfHandler.printIndexSOP();
//			rdfHandler.printIndexSPO();
			long tpsDebutCreationIndexSPO = System.currentTimeMillis();
			indexSPO = rdfHandler.getIndexSPO();
			long tpsCreationIndexSPO = System.currentTimeMillis() - tpsDebutCreationIndexSPO;
			//System.out.println("tpsCreationIndexSPO : " + tpsCreationIndexSPO + "ms");

			//System.out.println("nb index : " + indexSPO.size());
			sousLigneCSVData = tpsCreationDico + pie + tpsCreationIndexSPO + pie + indexSPO.size() + pie;
		}
		long tpsLectureDonnees = System.currentTimeMillis() - tpsDebutLectureDonnees;
		//System.out.println("tpsLectureDonnees : " + tpsLectureDonnees + "ms");
		
		sousLigneCSVData += tpsLectureDonnees + pie;
		return sousLigneCSVData;
//		ArrayList<String> list = new ArrayList<String>();
//		HashMap<String, ArrayList<String>> query = new HashMap <String, ArrayList<String>>();
//		try {
//			BufferedReader br = new BufferedReader(new FileReader(queryFile));
//			String line, select = "";
//			ArrayList<String> where = new ArrayList<String>();
//			while ((line = br.readLine()) != null) {
//				if (line.startsWith("SELECT")) {
//					if (!where.isEmpty()) {
//						query.put(select, where);
//					}
//					select = line.substring(line.indexOf("SELECT")+6, line.indexOf("WHERE")).replaceAll(" ", "");;
//					where = new ArrayList<String>();
//				} else {
//					String clause = line.replace("}", "").replace("\t", " ").trim();
//					if (line.startsWith(" ")) {
//						clause = clause.substring(1);
//					}
//					if (clause != null && clause.length() > 3) {
//						where.add(clause);
//					}
//				}
//			}
//			br.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		System.out.println("query : " + query);
	}
}
