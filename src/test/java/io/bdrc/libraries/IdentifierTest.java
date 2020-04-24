package io.bdrc.libraries;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IdentifierTest {

    @Test
    public void testTransition() throws IdentifierException {
        Identifier id = new Identifier("wi:bdr:W123::bdr:I234", Identifier.COLLECTION_ID);
        assertTrue(id.instanceId.equals("bdr:MW123"));
        assertTrue(id.imageInstanceId.equals("bdr:W234"));
        id = new Identifier("wi:bdr:W123::bdr:I234", Identifier.MANIFEST_ID);
        assertTrue(id.instanceId.equals("bdr:MW123"));
        assertTrue(id.imageInstanceId.equals("bdr:W234"));
        id = new Identifier("v:bdr:V543_991_I234_001", Identifier.MANIFEST_ID);
        assertTrue(id.imageGroupId.equals("bdr:I234_001"));
        id = new Identifier("wv:bdr:W123::bdr:V543_991_I234_001", Identifier.MANIFEST_ID);
        assertTrue(id.instanceId.equals("bdr:MW123"));
        assertTrue(id.imageGroupId.equals("bdr:I234_001"));
        id = new Identifier("wi:bdr:W123", Identifier.MANIFEST_ID);
        assertTrue(id.instanceId.equals("bdr:MW123"));
        assertTrue(id.imageInstanceId == null);
    }

    
}
