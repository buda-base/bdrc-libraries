package io.bdrc.libraries;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.sparql.core.DatasetGraph;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.bdrc.jena.sttl.STriGWriter;
import io.bdrc.libraries.formatters.JSONLDFormatter;
import io.bdrc.libraries.formatters.JSONLDFormatter.DocType;
import io.bdrc.libraries.formatters.TTLRDFWriter;

public class StreamingHelpers {

    public static final ObjectMapper om = new ObjectMapper();
    public static boolean prettyPrint = false;

    public static StreamingResponseBody getStream(String obj) {
        final StreamingResponseBody stream = new StreamingResponseBody() {
            @Override
            public void writeTo(final OutputStream os) throws IOException {
                os.write(obj.getBytes());
            }
        };
        return stream;
    }

    public static StreamingResponseBody getModelStream(final Model model, final String format, PrefixMap pm) {
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) {
                String JenaFormat = null;
                if (format == null) {
                    JenaFormat = "STTL";
                } else {
                    if (format.equals("jsonld")) {
                        JSONLDFormatter.writeModelAsCompact(model, os, pm);
                        return;
                    }
                    JenaFormat = BudaMediaTypes.getJenaFromExtension(format);
                }
                if (JenaFormat == null || JenaFormat.equals("STTL") || JenaFormat.contentEquals(RDFLanguages.strLangTriG)) {
                    final RDFWriter writer = TTLRDFWriter.getSTTLRDFWriter(model, "");
                    writer.output(os);
                    return;
                }
                model.write(os, JenaFormat);
            }
        };
    }

    public static StreamingResponseBody getModelStream(final Model model, final String format, final String res, DocType docType,
            PrefixMap prefixes) {

        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) {
                if (format.equals("jsonld")) {
                    Object json = JSONLDFormatter.modelToJsonObject(model, res, docType, prefixes);
                    JSONLDFormatter.jsonObjectToOutputStream(json, os);

                } else {
                    String JenaFormat = BudaMediaTypes.getJenaFromExtension(format);
                    if (JenaFormat == null || JenaFormat.equals("STTL")) {
                        final RDFWriter writer = TTLRDFWriter.getSTTLRDFWriter(model, "");
                        writer.output(os);
                        return;
                    }
                    if (JenaFormat.contentEquals(RDFLanguages.strLangTriG)) {
                        DatasetGraph dsg = DatasetFactory.create().asDatasetGraph();
                        dsg.addGraph(ResourceFactory.createResource(res).asNode(), model.getGraph());
                        new STriGWriter().write(os, dsg, prefixes, "", GlobalHelpers.createWriterContext());
                        return;
                    }
                    model.write(os, JenaFormat);
                }
            }
        };
    }

    public static StreamingResponseBody getJsonObjectStream(Object obj) {
        return new StreamingResponseBody() {
            @Override
            public void writeTo(OutputStream os) throws IOException {
                if (prettyPrint)
                    om.writerWithDefaultPrettyPrinter().writeValue(os, obj);
                else
                    om.writeValue(os, obj);
            }
        };
    }

}
