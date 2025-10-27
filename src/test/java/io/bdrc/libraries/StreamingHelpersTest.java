package io.bdrc.libraries;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import io.bdrc.libraries.formatters.JSONLDFormatter.DocType;

/**
 * Unit tests for StreamingHelpers.getModelStream() methods.
 * Tests the various overloaded versions of getModelStream() with different formats
 * and parameters, including TTL, JSONLD, RDF/XML, N-Triples, TriG formats.
 */
public class StreamingHelpersTest {

    private Model testModel;
    private PrefixMap testPrefixMap;

    @Before
    public void setUp() {
        // Create a simple test model with some triples
        testModel = ModelFactory.createDefaultModel();
        Resource subject = testModel.createResource("http://example.org/subject");
        subject.addProperty(
            testModel.createProperty("http://example.org/predicate"),
            "test value"
        );
        
        // Create a test prefix map
        testPrefixMap = PrefixMapFactory.create();
        testPrefixMap.add("ex", "http://example.org/");
    }

    @Test
    public void testGetModelStreamWithNullFormat() throws IOException {
        // Test with null format, should default to STTL
        StreamingResponseBody stream = StreamingHelpers.getModelStream(testModel, null, testPrefixMap);
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamWithTTLFormat() throws IOException {
        // Test with TTL format
        StreamingResponseBody stream = StreamingHelpers.getModelStream(testModel, "ttl", testPrefixMap);
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamWithJSONLDFormat() throws IOException {
        // Test with JSONLD format
        StreamingResponseBody stream = StreamingHelpers.getModelStream(testModel, "jsonld", testPrefixMap);
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamWithRDFFormat() throws IOException {
        // Test with RDF/XML format
        StreamingResponseBody stream = StreamingHelpers.getModelStream(testModel, "rdf", testPrefixMap);
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
        assertTrue("Output should be RDF/XML format", output.contains("rdf:RDF") || output.contains("<?xml"));
    }

    @Test
    public void testGetModelStreamWithNTFormat() throws IOException {
        // Test with N-Triples format
        StreamingResponseBody stream = StreamingHelpers.getModelStream(testModel, "nt", testPrefixMap);
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamWithResourceAndDocType() throws IOException {
        // Test the overloaded version with resource and DocType
        String resourceUri = "http://example.org/resource1";
        StreamingResponseBody stream = StreamingHelpers.getModelStream(
            testModel, 
            "ttl", 
            resourceUri, 
            DocType.GRAPH, 
            testPrefixMap
        );
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamWithResourceAndTriGFormat() throws IOException {
        // Test the overloaded version with TriG format
        String resourceUri = "http://example.org/resource1";
        StreamingResponseBody stream = StreamingHelpers.getModelStream(
            testModel, 
            "trig", 
            resourceUri, 
            DocType.GRAPH, 
            testPrefixMap
        );
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamWithResourceAndNullFormat() throws IOException {
        // Test the overloaded version with null format (should default to STTL)
        String resourceUri = "http://example.org/resource1";
        StreamingResponseBody stream = StreamingHelpers.getModelStream(
            testModel, 
            null, 
            resourceUri, 
            DocType.GRAPH, 
            testPrefixMap
        );
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamNoStable() throws IOException {
        // Test the getModelStreamNoStable method
        String resourceUri = "http://example.org/resource1";
        StreamingResponseBody stream = StreamingHelpers.getModelStreamNoStable(
            testModel, 
            "ttl", 
            resourceUri, 
            DocType.GRAPH, 
            testPrefixMap
        );
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }

    @Test
    public void testGetModelStreamNoStableWithSTTL() throws IOException {
        // Test that getModelStreamNoStable converts STTL to TTL
        String resourceUri = "http://example.org/resource1";
        StreamingResponseBody stream = StreamingHelpers.getModelStreamNoStable(
            testModel, 
            "STTL", 
            resourceUri, 
            DocType.GRAPH, 
            testPrefixMap
        );
        assertNotNull("StreamingResponseBody should not be null", stream);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        stream.writeTo(outputStream);
        String output = outputStream.toString();
        
        assertNotNull("Output should not be null", output);
        assertTrue("Output should not be empty", output.length() > 0);
    }
}
