package io.bdrc.libraries;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class SparqlCommons {

	public static HashMap<String, ArrayList<String>> getGraphsForResourceByGitRepos(String resUri, String fusekiUrl) {
		HashMap<String, ArrayList<String>> map = new HashMap<>();
		String query = "SELECT DISTINCT ?g ?rep\n" + "WHERE{\n" + "GRAPH ?g {\n" + "{\n" + "?s ?p <" + resUri + "> .\n" + "} \n" + "    union {\n"
				+ "<" + resUri + "> ?pp ?oo .\n" + "}\n" + "?ad adm:graphId ?g .\n" + "?ad adm:gitRepo ?rep\n" + "}\n" + "}ORDER BY ?rep";
		final Query q = QueryFactory.create(Prefixes.getPrefixesString() + query);
		System.out.println(q.toString());
		final QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiUrl, q);
		ResultSet rs = qe.execSelect();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();

			String rep = qs.get("?rep").asResource().getURI();
			ArrayList<String> list = map.get(rep);
			if (list == null) {
				list = new ArrayList<>();
			}
			list.add(qs.get("?g").asResource().getURI());
			map.put(rep, list);
		}
		return map;
	}

	public static Model replaceRefInModel(Model mod, String graphUri, String toReplaceUri, String newUri, Reasoner bdrcReasoner) {
		String query = "with <" + graphUri + "> \n" + "DELETE { <" + toReplaceUri + "> ?p ?s.}\n" + "INSERT { <" + newUri + "> ?p ?s .}\n" + "WHERE\n"
				+ "{ <" + toReplaceUri + "> ?p ?s .};\n" + "with <" + graphUri + "> \n" + "DELETE { ?s1 ?p1 <" + toReplaceUri + ">}\n"
				+ "INSERT { ?s1 ?p1 <" + newUri + ">}\n" + "WHERE\n" + "  { ?s1 ?p1 <" + toReplaceUri + ">}";
		query = Prefixes.getPrefixesString() + " " + query;
		Dataset ds = DatasetFactory.create();
		ds.addNamedModel(graphUri, mod);
		RDFConnection conn = RDFConnectionFactory.connect(ds);
		conn.update(query);
		conn.commit();
		conn.close();
		Model m = ds.getNamedModel(graphUri);
		if (bdrcReasoner != null) {
			m = ModelFactory.createInfModel(bdrcReasoner, m);
		}
		return m;
	}

	public static Model setAdminStatus(Model m, String adminGraphUri, String statusUri) {
		StmtIterator stmt = m.listStatements();
		Statement stt = ResourceFactory.createStatement(ResourceFactory.createResource(adminGraphUri),
				ResourceFactory.createProperty(Models.STATUS_PROP), ResourceFactory.createResource(Models.STATUS_WITHDRAWN));
		while (stmt.hasNext()) {
			Statement st = stmt.next();
			if (st.getPredicate().equals(ResourceFactory.createProperty(Models.STATUS_PROP))) {
				m.remove(st);
				m.add(stt);
				return m;
			}
		}
		return m;
	}

	public static Model setGitRevision(Model m, String adminGraphUri, String revNumber) {
		StmtIterator stmt = m.listStatements();
		Statement stt = ResourceFactory.createStatement(ResourceFactory.createResource(adminGraphUri),
				ResourceFactory.createProperty(Models.GIT_REVISION), ResourceFactory.createPlainLiteral(revNumber));
		while (stmt.hasNext()) {
			Statement st = stmt.next();
			if (st.getPredicate().equals(ResourceFactory.createProperty(Models.GIT_REVISION))) {
				m.remove(st);
				m.add(stt);
				return m;
			}
		}
		return m;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(getGraphsForResourceByGitRepos("http://purl.bdrc.io/resource/P1487", "http://buda1.bdrc.io:13180/fuseki/testrw/query"));
		Dataset ds = DatasetFactory.create();
		RDFDataMgr.read(ds, new StringReader(GitHelpers.getGitHeadFileContent("/etc/buda/share/gitData/persons", "c7/P1585.trig")), "", Lang.TRIG);
		Reasoner reasoner = BDRCReasoner.getReasoner();
		Model to_update = ModelFactory.createModelForGraph(ds.asDatasetGraph().getUnionGraph());
		Model m = replaceRefInModel(to_update, "http://purl.bdrc.io/graph/P1585", "http://purl.bdrc.io/resource/P1584",
				"http://purl.bdrc.io/resource/PPP1584", reasoner);
		m.write(System.out, "TURTLE");
	}

}
