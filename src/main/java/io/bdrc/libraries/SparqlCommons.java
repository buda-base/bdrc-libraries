package io.bdrc.libraries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.reasoner.Reasoner;

public class SparqlCommons {

    // validate the list of result vars of a select query against a desired set
    public static boolean validateResultVars(Query q, ArrayList<String> expectedVars) {
        Collections.sort(expectedVars);
        List<String> vars = q.getResultVars();
        Collections.sort(vars);
        return expectedVars.equals(vars);
    }

    // validate the list of result vars of a select query against a desired set
    public static boolean validateResultVars(String query, ArrayList<String> expectedVars) {
        Query q = QueryFactory.create(Prefixes.getPrefixesString() + query);
        return validateResultVars(q, expectedVars);
    }

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

    public static HashMap<String, ArrayList<String>> getGraphsByGitRepos(ArrayList<String> graphUris, String fusekiUrl) {
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        String query = "select ?g ?rep\n" + "where{" + "	?ad adm:graphId ?g ." + "    ?ad adm:gitRepo ?rep " + " values ?g { ";
        for (String uri : graphUris) {
            query = query + " <" + uri + "> ";
        }
        query = query + " } }";
        System.out.println(query);
        final Query q = QueryFactory.create(Prefixes.getPrefixesString() + query);

        final QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiUrl, q);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            String rep = qs.get("?rep").asResource().getURI();
            ArrayList<String> uris = map.get(rep);
            if (uris == null) {
                uris = new ArrayList<>();
            }
            uris.add(qs.get("?g").asResource().getURI());
            map.put(rep, uris);
        }
        return map;
    }

    public static HashMap<String, ArrayList<String>> getGraphsByGitRepos(String query, String fusekiUrl) {
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        final Query q = QueryFactory.create(Prefixes.getPrefixesString() + query);
        ArrayList<String> vars = new ArrayList<>(Arrays.asList("g", "rep"));
        if (!validateResultVars(q, vars)) {
            return map;
        }
        final QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiUrl, q);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            String rep = qs.get("?rep").asResource().getURI();
            ArrayList<String> uris = map.get(rep);
            if (uris == null) {
                uris = new ArrayList<>();
            }
            uris.add(qs.get("?g").asResource().getURI());
            map.put(rep, uris);
        }
        return map;
    }

    public static HashMap<String, ArrayList<String>> getGraphsByGitReposHavingProp(ArrayList<String> graphUris, String propUri, String fusekiUrl) {
        HashMap<String, ArrayList<String>> map = new HashMap<>();
        String query = "select distinct ?g ?rep " + "where { " + "  graph ?g {?s <" + propUri + "> ?o .} " + "  ?ad adm:graphId ?g . "
                + "  ?ad adm:gitRepo ?rep " + "}";
        System.out.println(query);
        final Query q = QueryFactory.create(Prefixes.getPrefixesString() + query);

        final QueryExecution qe = QueryExecutionFactory.sparqlService(fusekiUrl, q);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            String rep = qs.get("?rep").asResource().getURI();
            ArrayList<String> uris = map.get(rep);
            if (uris == null) {
                uris = new ArrayList<>();
            }
            uris.add(qs.get("?g").asResource().getURI());
            map.put(rep, uris);
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

    public static Model setStatusWithDrawn(Model m, String adminGraphUri, String statusUri) {
        StmtIterator stmt = m.listStatements();
        Statement stt = ResourceFactory.createStatement(ResourceFactory.createResource(adminGraphUri),
                ResourceFactory.createProperty(Models.STATUS_PROP), ResourceFactory.createResource(Models.STATUS_WITHDRAWN));
        ArrayList<Statement> to_remove = new ArrayList<>();
        ArrayList<Statement> to_add = new ArrayList<>();
        while (stmt.hasNext()) {
            Statement st = stmt.next();
            if (st.getPredicate().equals(ResourceFactory.createProperty(Models.STATUS_PROP))) {
                to_remove.add(st);
                to_add.add(stt);
            }
        }
        for (Statement str : to_remove) {
            m.remove(str);
        }
        for (Statement sta : to_add) {
            m.add(sta);
        }
        m.add(stt);
        return m;
    }

    public static Model setPropValue(Model m, String adminGraphUri, Property p, Resource value) {
        StmtIterator stmt = m.listStatements();
        Statement stt = ResourceFactory.createStatement(ResourceFactory.createResource(adminGraphUri), p, value);
        ArrayList<Statement> to_remove = new ArrayList<>();
        ArrayList<Statement> to_add = new ArrayList<>();
        while (stmt.hasNext()) {
            Statement st = stmt.next();
            if (st.getPredicate().equals(p)) {
                to_remove.add(st);
                to_add.add(stt);
            }
        }
        for (Statement str : to_remove) {
            m.remove(str);
        }
        for (Statement sta : to_add) {
            m.add(sta);
        }
        m.add(stt);
        return m;
    }

    public static Model setLiteralPropValue(Model m, String adminGraphUri, Property p, String value, String lang) {
        StmtIterator stmt = m.listStatements();
        Literal l = null;
        if (lang != null) {
            l = ResourceFactory.createLangLiteral(value, lang);
        } else {
            l = ResourceFactory.createPlainLiteral(value);
        }
        Statement stt = ResourceFactory.createStatement(ResourceFactory.createResource(adminGraphUri), p, l);
        ArrayList<Statement> to_remove = new ArrayList<>();
        ArrayList<Statement> to_add = new ArrayList<>();
        while (stmt.hasNext()) {
            Statement st = stmt.next();
            if (st.getPredicate().equals(p)) {
                to_remove.add(st);
                to_add.add(stt);
            }
        }
        for (Statement str : to_remove) {
            m.remove(str);
        }
        for (Statement sta : to_add) {
            m.add(sta);
        }
        m.add(stt);
        return m;
    }

    public static Model setGitRevision(Model m, String adminGraphUri, String revNumber) {
        StmtIterator stmt = m.listStatements();
        Statement stt = ResourceFactory.createStatement(ResourceFactory.createResource(adminGraphUri),
                ResourceFactory.createProperty(Models.GIT_REVISION), ResourceFactory.createPlainLiteral(revNumber));
        ArrayList<Statement> to_remove = new ArrayList<>();
        ArrayList<Statement> to_add = new ArrayList<>();
        while (stmt.hasNext()) {
            Statement st = stmt.next();
            if (st.getPredicate().equals(ResourceFactory.createProperty(Models.GIT_REVISION))) {
                to_remove.add(st);
                to_add.add(stt);
            }
        }
        for (Statement str : to_remove) {
            m.remove(str);
        }
        for (Statement sta : to_add) {
            m.add(sta);
        }
        m.add(stt);
        return m;
    }

    public static Model addResourceValueForPropInGraph(Model m, Property p, Resource ro) {
        ResIterator it = m.listResourcesWithProperty(p);
        ArrayList<Statement> to_add = new ArrayList<>();
        while (it.hasNext()) {
            Resource rs = it.next();
            Statement stt = ResourceFactory.createStatement(rs, p, ro);
            to_add.add(stt);
        }
        for (Statement st : to_add) {
            m.add(st);
        }
        return m;
    }

    public static Model addLiteralValueForPropInGraph(Model m, Property p, String value, String lang) {
        Literal l = null;
        if (lang != null) {
            l = ResourceFactory.createLangLiteral(value, lang);
        } else {
            l = ResourceFactory.createPlainLiteral(value);
        }
        ArrayList<Statement> to_add = new ArrayList<>();
        ResIterator it = m.listResourcesWithProperty(p);
        while (it.hasNext()) {
            Resource rs = it.next();
            Statement stt = ResourceFactory.createStatement(rs, p, l);
            to_add.add(stt);
        }
        return m;
    }

    public static Model renamePropInGraph(Model m, Property oldProp, Property newProp) {
        StmtIterator stmt = m.listStatements();
        ArrayList<Statement> to_remove = new ArrayList<>();
        ArrayList<Statement> to_add = new ArrayList<>();
        while (stmt.hasNext()) {
            Statement st = stmt.next();
            if (st.getPredicate().equals(oldProp)) {
                to_remove.add(st);
                Statement stt = ResourceFactory.createStatement(st.getSubject(), newProp, st.getObject());
                to_add.add(stt);
            }
        }
        for (Statement str : to_remove) {
            m.remove(str);
        }
        for (Statement sta : to_add) {
            m.add(sta);
        }
        return m;
    }

    public static void main(String[] args) throws IOException {
        /*
         * System.out.println(getGraphsForResourceByGitRepos(
         * "http://purl.bdrc.io/resource/P1487",
         * "http://buda1.bdrc.io:13180/fuseki/testrw/query")); Dataset ds =
         * DatasetFactory.create(); RDFDataMgr.read(ds, new
         * StringReader(GitHelpers.getGitHeadFileContent(
         * "/etc/buda/share/gitData/persons", "c7/P1585.trig")), "", Lang.TRIG);
         * Reasoner reasoner = BDRCReasoner.getReasoner(); Model to_update =
         * ModelFactory.createModelForGraph(ds.asDatasetGraph().getUnionGraph()); Model
         * m = replaceRefInModel(to_update, "http://purl.bdrc.io/graph/P1585",
         * "http://purl.bdrc.io/resource/P1584", "http://purl.bdrc.io/resource/PPP1584",
         * reasoner); m.write(System.out, "TURTLE");
         */

        ArrayList<String> uris = new ArrayList<>();
        uris.add("http://purl.bdrc.io/graph/P1583");
        uris.add("http://purl.bdrc.io/graph/P1585");
        uris.add("http://purl.bdrc.io/graph/W22703");
        uris.add("http://purl.bdrc.io/graph/T2423");
        System.out.println(getGraphsByGitRepos(uris, "http://buda1.bdrc.io:13180/fuseki/testrw/query"));

        String q = "select ?g ?rep\n"
                + "where{	?ad adm:graphId ?g .    ?ad adm:gitRepo ?rep  values ?g {  <http://purl.bdrc.io/graph/P1583>  <http://purl.bdrc.io/graph/P1585>  <http://purl.bdrc.io/graph/W22703>  <http://purl.bdrc.io/graph/T2423>  } }\n";
        getGraphsByGitRepos(q, "http://buda1.bdrc.io:13180/fuseki/testrw/query");
    }

}
