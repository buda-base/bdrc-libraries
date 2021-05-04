package io.bdrc.libraries;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.VCARD4;
import org.apache.jena.vocabulary.XSD;

public class Models {

	public static String STATUS_PROP = "http://purl.bdrc.io/ontology/admin/status";
	public static String STATUS_WITHDRAWN = "http://purl.bdrc.io/admindata/StatusWithdrawn";
	public static String GIT_REVISION = "http://purl.bdrc.io/ontology/admin/gitRevision";

	public static final String BDO = "http://purl.bdrc.io/ontology/core/";
	public static final String ADM = "http://purl.bdrc.io/ontology/admin/";
	public static final String BDA = "http://purl.bdrc.io/admindata/";
	public static final String BDG = "http://purl.bdrc.io/graph/";
	public static final String BDR = "http://purl.bdrc.io/resource/";
	public static final String BDU = "http://purl.bdrc.io/resource-nc/user/";
	public static final String AUT = "http://purl.bdrc.io/ontology/ext/auth/";
	public static final String ADR = "http://purl.bdrc.io/resource-nc/auth/";
	public static final String BF = "http://id.loc.gov/ontologies/bibframe/";
	public static final String VCARD = VCARD4.getURI();

	public static final String USER = "MigrationApp";

	public static final String EWTS_TAG = "bo-x-ewts";
	public static final boolean lowerCaseLangTags = true;

	public static final Map<String, String> typeToRepo = new HashMap<>();

	public static MessageDigest md5;
	private static final int hashNbChars = 2;
	private static final int nbShaChars = 16;

	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		fillTypeToRepo();
	}

	private static void fillTypeToRepo() {
		typeToRepo.put("Corporation", BDA + "GR0001");

		typeToRepo.put("Etext", BDA + "GR0002");
		typeToRepo.put("EtextNonPaginated", BDA + "GR0002");
		typeToRepo.put("EtextPaginated", BDA + "GR0002");

		typeToRepo.put("Item", BDA + "GR0003");
		typeToRepo.put("ItemEtext", BDA + "GR0003");
		typeToRepo.put("ItemEtextNonPaginated", BDA + "GR0003");
		typeToRepo.put("ItemEtextPaginated", BDA + "GR0003");
		typeToRepo.put("ItemImageAsset", BDA + "GR0003");
		typeToRepo.put("ItemPhysicalAsset", BDA + "GR0003");

		typeToRepo.put("Lineage", BDA + "GR0004");
		typeToRepo.put("Place", BDA + "GR0005");
		typeToRepo.put("Person", BDA + "GR0006");
		typeToRepo.put("Product", BDA + "GR0011");
		typeToRepo.put("Topic", BDA + "GR0007");

		typeToRepo.put("Work", BDA + "GR0008");
		typeToRepo.put("AbstractWork", BDA + "GR0008");
		typeToRepo.put("PublishedWork", BDA + "GR0008");
		typeToRepo.put("UnicodeWork", BDA + "GR0008");
		typeToRepo.put("VirtualWork", BDA + "GR0008");
		typeToRepo.put("UnspecifiedWorkClass", BDA + "GR0008");
		typeToRepo.put("SerialWork", BDA + "GR0008");
		typeToRepo.put("SerialMember", BDA + "GR0008");

		typeToRepo.put("Instance", BDA + "GR0012");
		typeToRepo.put("PhysicalInstance", BDA + "GR0012");
		typeToRepo.put("SerialInstance", BDA + "GR0012");
		typeToRepo.put("VirtualInstance", BDA + "GR0012");
		typeToRepo.put("SingletonInstance", BDA + "GR0012");
		typeToRepo.put("BundleInstance", BDA + "GR0012");

		typeToRepo.put("ImageInstance", BDA + "GR0014");

		typeToRepo.put("EtextInstance", BDA + "GR0013");

		typeToRepo.put("EtextContent", BDA + "GR0009");
		typeToRepo.put("Role", BDA + "GR0010");
		typeToRepo.put("Product", BDA + "GR0011");
		typeToRepo.put("Collection", BDA + "GR0011");
		typeToRepo.put("Subscriber", BDA + "GR0012");
	}

	public static String getMd5(String resId) {
		try {
			String message = resId;
			final byte[] bytesOfMessage = message.getBytes("UTF-8");
			final byte[] hashBytes = md5.digest(bytesOfMessage);
			BigInteger bigInt = new BigInteger(1, hashBytes);
			return String.format("%032x", bigInt).substring(0, hashNbChars);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Resource createRoot(Model m, String uri, String typeUri) {
		Resource rez = m.createResource(uri);
		if (typeUri != null)
			rez.addProperty(RDF.type, m.createResource(typeUri));
		rez.addLiteral(m.createProperty(BDO + "isRoot"), true);
		return rez;
	}

	/**
	 * Returns an AdminData resource for the resource r. Be aware that if r IS an
	 * <code>adm:</code> resource this method will return the same resource with an
	 * additional stmt declaring this resource to be adm:adminAbout itself, and an
	 * additional <code>rdf:type</code> stmt, making the resource of two types.
	 * 
	 * @param rez resource for which the corresponding AdminData resource is
	 *            requested.
	 * @return the corresponding AdminData resource
	 */
	public static Resource getAdminData(Resource rez) {
		Model m = rez.getModel();
		String firstLetter = "";
		if (rez.getURI().startsWith(BDA)) {
		    firstLetter = "A";
		}
		Resource admR = m.createResource(BDA + firstLetter + rez.getLocalName());
		if (!m.contains(admR, RDF.type, m.createResource(ADM + "AdminData"))) {
			admR.addProperty(RDF.type, m.createResource(ADM + "AdminData"));
			m.add(admR, m.createProperty(ADM + "adminAbout"), rez);
		}

		return admR;
	}

	public static Resource getRepoFor(String typeName) {
		String repoUri = typeToRepo.get(typeName);
		if (repoUri != null) {
			return ResourceFactory.createResource(repoUri);
		} else {
			return null;
		}
	}

	private static Resource getRezRepo(Resource rez) {
		Resource adClass = ResourceFactory.createResource(ADM + "AdminData");
		String typeName = null;

		if (!rez.hasProperty(RDF.type, adClass)) {
			Statement typeStmt = rez.getProperty(RDF.type);
			typeName = typeStmt != null ? typeStmt.getObject().asResource().getLocalName() : null;
		} else {
			// multiple typed admin resource - e.g., adm:Product
			StmtIterator iter = rez.listProperties(RDF.type);
			while (iter.hasNext()) {
				Statement stmt = iter.next();
				Resource typeR = stmt.getObject().asResource();
				if (typeR.equals(adClass)) {
					continue;
				} else {
					typeName = typeR.getLocalName();
				}
			}
		}

		return getRepoFor(typeName);
	}

	/**
	 * Returns a root AdminData resource for the root resource rez. If rez IS a BDA:
	 * resource this method will return the same resource with an additional stmt
	 * declaring this resource to be adm:adminAbout itself, and adm:isRoot true.
	 * 
	 * @param rez resource for which the corresponding AdminData resource is
	 *            requested.
	 * @return the root AdminData resource
	 */
	public static Resource createAdminRoot(Resource rez) {
		Model m = rez.getModel();
		String rid = rez.getLocalName();
		Resource admR = getAdminData(rez);
		Resource repoR = getRezRepo(rez);

		if (repoR != null) {
			// add GitInfo
			admR.addProperty(m.createProperty(ADM + "gitRepo"), repoR);
			admR.addProperty(m.createProperty(ADM + "gitPath"), getMd5(rid) + "/" + rid + ".trig");

			// add link to graphId. Only resorces w/ a git repo have a graphId
			admR.addProperty(m.createProperty(ADM + "graphId"), m.createResource(BDG + rid));
		} else {
			// probably called from TaxonomyMigration or ImagegroupMigration -
			// nothing to do since they aren't stored in their own repo
		}

		// MAY BE REMOVED - not needed since presence of ?s adm:gitInfo ?o indicates
		// that ?s is a root
		// or adm:graphId is also a good indicator of a root AdminData.
		// AdminData
		admR.addLiteral(m.createProperty(BDO + "isRoot"), true);

		return admR;
	}

	public static Resource getAdminRoot(Resource rez) {
		return getAdminRoot(rez, false);
	}

	public static Resource getAdminRoot(Resource rez, boolean create) {
		Resource admR = getAdminRoot(rez.getModel());

		if (admR == null && create) {
			admR = createAdminRoot(rez);
		}

		return admR;
	}

	public static Resource getAdminRoot(Model m) {
		ResIterator resIt = m.listResourcesWithProperty(RDF.type, m.createResource(ADM + "AdminData"));
		while (resIt.hasNext()) {
			Resource admR = resIt.next();
			Statement stmt = m.getProperty(admR, m.createProperty(ADM + "gitInfo"));
			RDFNode node = stmt != null ? stmt.getObject() : null;
			if (node != null) {
				return admR;
			}
			// TO BE REMOVED ??
			if (m.containsLiteral(admR, m.createProperty(BDO + "isRoot"), true)) {
				return admR;
			}
		}

		return null;
	}

	private static Map<String, FacetType> strToFacetType = new HashMap<>();

	public enum FacetType {

		CORP_MEMBER("corporationMember", "CM", BDO + "CorporationMember"), CREATOR("creator", "CR", BDO + "AgentAsCreator"),
		ETEXT_CHUNK("etextChunk", "EC", BDO + "EtextChunk"), ETEXT_LINE("etextLine", "EL", BDO + "EtextLine"),
		ETEXT_PAGE("etextPage", "EP", BDO + "EtextPage"), ETEXT_REF("etextRef", "ER", BDO + "EtextRef"), EVENT("event", "EV", BDO + "Event"),
		HOLDER("lineageHolder", "LH", BDO + "LineageHolder"), LINEAGE_HOLDER("lineageHolder", "LH", BDO + "LineageHolder"),
		LOG_ENTRY("logEntry", "LG", ADM + "LogEntry"), NAME("name", "NM", BDO + "PersonName"), NOTE("note", "NT", BDO + "Note"),
		PRODUCT_ORG("productOrg", "PG", ADM + "ProductOrg"), TITLE("title", "TT", BDO + "WorkTitle"),
		VCARD_ADDR("vcardAddr", "VA", VCARD + "Address"), VOLUME("volume", "VL", BDO + "Volume"), CONTENT_LOC("contentLoc", "CL", BDO + "ContentLocation"),
		MICROFILM("microfilmItem", "MF", BDO + "ItemMicrofilmAsset"), CATALOG("catalogLoc", "CT", BDO + "CatalogLocation"),
		DATE_INDICATION("dateIndication", "DT", BDO + "DateIndication"), IDENTIFIER("identifier", "ID", BDO+"Identifier"),
		SUBSCRIBER_ORG("subscriberHasOrg", "SO", AUT+"SubscriberOrg");

		private String label;
		private String prefix;
		private String nodeTypeUri;

		private FacetType(String label, String prefix) {
			this(label, prefix, null);
		}

		private FacetType(String label, String prefix, String nodeTypeUri) {
			this.label = label;
			this.prefix = prefix;
			this.nodeTypeUri = nodeTypeUri;
			strToFacetType.put(prefix, this);
		}

		public static FacetType getType(String prefix) {
			return strToFacetType.get(prefix);
		}

		public String getPrefix() {
			return prefix;
		}

		public Resource getNodeType() {
			return ResourceFactory.createResource(nodeTypeUri);
		}

		@Override
		public String toString() {
			return label;
		}
	}

	/**
	 * retrieves the adm:facetIndex for admin data resource, rootAdmRez, for the
	 * given facet type, increment the index and store it back into the underlying
	 * model.
	 * 
	 * @param rootAdmRez admin data resource containing the adm:facetIndex to be
	 *                   used
	 * @return
	 */
	private static int getFacetIndex(Resource rootAdmRez) {
		Model m = rootAdmRez.getModel();
		Property inxP = m.createProperty(ADM + "facetIndex");
		Statement stmt = m.getProperty(rootAdmRez, inxP);

		int inx = 1;
		if (stmt != null) {
			inx = stmt.getInt();
			m.remove(stmt);
		}

		m.addLiteral(rootAdmRez, inxP, m.createTypedLiteral(inx + 1, XSDDatatype.XSDinteger));
		return inx;
	}

	// returns hex string in uppercase
	private static String bytesToHex(byte[] hash) {
		return DatatypeConverter.printHexBinary(hash);
	}

	public static String mintId(Resource rootAdmRez, String seed, String prefix) {
		try {
			String data = seed + getFacetIndex(rootAdmRez);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(data.getBytes("UTF-8"));
			return prefix + bytesToHex(hash).substring(0, nbShaChars);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns prefix + 8 character unique id based on a hash of the concatenation
	 * of the last three string arguments. This is itended to be used to generate
	 * ids for named nodes like Events, AgentAsCreator, WorkTitle and PersonName.
	 * 
	 * @param facet      enum type to provide a prefix will that distinguish what
	 *                   the id will identify
	 * @param rez        subject resource that refers to an Event, AgentAsCreator
	 *                   etc
	 * @param user       some String identifying the user or tool that is creating
	 *                   the id, make unique to the user
	 * @param rootAdmRez adm:AdminData resource for the top-level subject, relevant
	 *                   particularly for :Works which may have many sub-parts
	 * @return id = facet prefix + 8 character hash substring
	 */
	private static String generateId(FacetType facet, Resource rez, String user, Resource rootAdmRez) {
		return mintId(rootAdmRez, rez.getLocalName() + user, facet.getPrefix());
	}

	/**
	 * Creates a facet node in the BDR namespace w/ the default facet nodeType from
	 * the facet enum
	 * 
	 * @param facet the type of facet node to create
	 * @param rez   the resource the node is associated w/ such as via bdo:creator
	 * @return the newly minted facet node resource of rdf:type default nodeType for
	 *         the facet
	 */
	public static Resource getFacetNode(FacetType facet, Resource rez) {
		Resource nodeType = facet.getNodeType();
		return getFacetNode(facet, BDR, rez, nodeType);
	}

	/**
	 * Creates a facet node in the BDR namespace w/ the supplied facet nodeType.
	 * This method takes a nodeType resource for use with PersonName, WorkTitle and
	 * Events since these have many specialized sub-types.
	 * 
	 * @param facet    the type of facet node to create
	 * @param rez      the resource the node is associated w/ such as via
	 *                 bdo:creator
	 * @param nodeType the class for the type of node
	 * @return the newly minted facet node resource of rdf:type nodeType
	 */
	public static Resource getFacetNode(FacetType facet, Resource rez, Resource nodeType) {
		return getFacetNode(facet, BDR, rez, nodeType);
	}

	public static Resource getFacetNode(FacetType facet, String nsUri, Resource rez) {
		Resource nodeType = facet.getNodeType();
		return getFacetNode(facet, nsUri, rez, nodeType);
	}

	public static Resource getFacetNode(FacetType facet, String nsUri, Resource rez, Resource nodeType) {
		Model m = rez.getModel();
		Resource rootAdm = getAdminRoot(rez);
		String id = generateId(facet, rez, USER, rootAdm);
		Resource facetNode = m.createResource(nsUri + id);
		facetNode.addProperty(RDF.type, nodeType);
		return facetNode;
	}

	public static Resource getEvent(Resource rez, String eventType, String eventProp) {
		Model m = rez.getModel();
		Property prop = m.createProperty(BDO, eventProp);
		StmtIterator it = rez.listProperties(prop);
		while (it.hasNext()) {
			Statement s = it.next();
			Resource event = s.getObject().asResource();
			Resource eventTypeR = event.getPropertyResourceValue(RDF.type);
			if (eventTypeR != null && eventTypeR.getLocalName().equals(eventType)) {
				return event;
			}
		}

		Resource event = getFacetNode(FacetType.EVENT, rez, m.createProperty(BDO + eventType));
		m.add(rez, prop, event);
		return event;
	}

	public static void setPrefixes(Model m) {
		setPrefixes(m, false);
	}

	public static void setPrefixes(Model m, String type) {
		setPrefixes(m, type.equals("place"));
	}

	public static void setPrefixes(Model m, boolean addVcard) {
		m.setNsPrefix("", BDO);
		m.setNsPrefix("adm", ADM);
		m.setNsPrefix("bdr", BDR);
		m.setNsPrefix("bda", BDA);
		m.setNsPrefix("bdg", BDG);
		m.setNsPrefix("bdu", BDU);
		m.setNsPrefix("owl", OWL.getURI());
		m.setNsPrefix("bf", BF);
		m.setNsPrefix("rdf", RDF.getURI());
		m.setNsPrefix("rdfs", RDFS.getURI());
		m.setNsPrefix("skos", SKOS.getURI());
		m.setNsPrefix("xsd", XSD.getURI());
		m.setNsPrefix("rkts", "http://purl.rkts.eu/resource/");
		if (addVcard)
			m.setNsPrefix("vcard", VCARD4.getURI());
	}

	public static void addReleased(Model m, Resource r) {
		addStatus(m, r, "released");
	}

	public static void addStatus(Model m, Resource r, String status) {
		if (status == null || status.isEmpty())
			return;
		String statusName = "Status" + status.substring(0, 1).toUpperCase() + status.substring(1);
		r.addProperty(m.getProperty(ADM + "status"), m.getResource(BDA + statusName));
	}
}
