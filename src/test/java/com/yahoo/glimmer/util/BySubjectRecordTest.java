package com.yahoo.glimmer.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Before;
import org.junit.Test;


public class BySubjectRecordTest {
    private static final int ID_1 = 33;
    private static final String SUBJECT_1 = "http://subject/";
    private static final String RELATION_1_1 = "<http://predicate1> \"literal\" .";
    private static final String RELATION_1_2 = "<http://predicate2> <http://resource> .";
    private static final String SUBJECT_DOC_1 = "" + ID_1 + '\t' + SUBJECT_1 + '\t' + RELATION_1_1 + "  " + RELATION_1_2;
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(4096);
    private Writer writer;
    private BySubjectRecord record;
    
    @Before
    public void before() {
	byteArrayOutputStream.reset();
	writer = new OutputStreamWriter(byteArrayOutputStream);
	record = new BySubjectRecord();
    }
    
    @Test
    public void writeToTest() throws IOException {
	record.writeTo(writer);
	writer.flush();
	byteArrayOutputStream.flush();
	assertEquals("0\t\t\n", byteArrayOutputStream.toString("UTF-8"));
	
	byteArrayOutputStream.reset();
	
	record.setId(ID_1);
	record.setSubject(SUBJECT_1);
	record.addRelation(RELATION_1_1);
	record.addRelation(RELATION_1_2);
	
	record.writeTo(writer);
	writer.flush();
	byteArrayOutputStream.flush();
	assertEquals(SUBJECT_DOC_1 + '\n', byteArrayOutputStream.toString("UTF-8"));
    }
    
    @Test
    public void parseTest() throws IOException {
	byte[] bytes = SUBJECT_DOC_1.getBytes("UTF-8");
	assertTrue(record.parse(bytes, 0, bytes.length));
	
	assertEquals(ID_1, record.getId());
	assertEquals(SUBJECT_1, record.getSubject());
	assertTrue(record.hasRelations());
	assertEquals(2, record.getRelationsCount());
	Iterator<String> relations = record.getRelations().iterator();
	assertEquals(RELATION_1_1, relations.next());
	assertEquals(RELATION_1_2, relations.next());
	assertFalse(relations.hasNext());
    }
    
    @Test
    public void parseFromBufferTest() throws IOException {
	byte[] bytes = SUBJECT_DOC_1.getBytes("UTF-8");
	byte[] buffer = new byte[4096];
	
	System.arraycopy(bytes, 0, buffer, 20, bytes.length);
	buffer[20 + bytes.length] = '\n';
	
	assertTrue(record.parse(buffer, 20, bytes.length + 1));
	assertEquals(ID_1, record.getId());
	assertEquals(SUBJECT_1, record.getSubject());
	assertTrue(record.hasRelations());
	assertEquals(2, record.getRelationsCount());
	Iterator<String> relations = record.getRelations().iterator();
	assertEquals(RELATION_1_1, relations.next());
	assertEquals(RELATION_1_2, relations.next());
	assertFalse(relations.hasNext());
    }
    
    @Test
    public void empty1ParseTest() throws IOException {
	byte[] bytes = "".getBytes("UTF-8");
	assertFalse(record.parse(bytes, 0, bytes.length));
	
	assertEquals(0, record.getId());
	assertNull(record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	Iterator<String> relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
    }
    
    @Test
    public void empty2ParseTest() throws IOException {
	byte[] bytes = "\t\t\n".getBytes("UTF-8");
	assertFalse(record.parse(bytes, 0, bytes.length));
	
	assertEquals(0, record.getId());
	assertNull(record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	Iterator<String> relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
	
	bytes = "\t\t".getBytes("UTF-8");
	assertFalse(record.parse(bytes, 0, bytes.length));
	
	assertEquals(0, record.getId());
	assertNull(record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
    }
    
    @Test
    public void badParseTest() throws IOException {
	byte[] bytes = "4\t\t\n".getBytes("UTF-8");
	assertFalse(record.parse(bytes, 0, bytes.length));
	
	assertEquals(4, record.getId());
	assertNull(record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	Iterator<String> relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
	
	bytes = "4\t\t".getBytes("UTF-8");
	assertFalse(record.parse(bytes, 0, bytes.length));
	
	assertEquals(4, record.getId());
	assertNull(record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
	
	bytes = "4\t".getBytes("UTF-8");
	assertFalse(record.parse(bytes, 0, bytes.length));
	
	assertEquals(4, record.getId());
	assertNull(record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
    }
    
    @Test
    public void noRelationsTest() throws IOException {
	byte[] bytes = "6\thttp://sbj/\t\n".getBytes("UTF-8");
	assertTrue(record.parse(bytes, 0, bytes.length));
	
	assertEquals(6, record.getId());
	assertEquals("http://sbj/", record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	Iterator<String> relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
    }
    
    @Test
    public void spacesTest() throws IOException {
	byte[] bytes = " 6 \t http://sbj/ \t  \n".getBytes("UTF-8");
	assertTrue(record.parse(bytes, 0, bytes.length));
	
	assertEquals(6, record.getId());
	assertEquals("http://sbj/", record.getSubject());
	assertFalse(record.hasRelations());
	assertEquals(0, record.getRelationsCount());
	Iterator<String> relations = record.getRelations().iterator();
	assertFalse(relations.hasNext());
    }
    
    @Test
    public void relationsReaderTest() throws IOException {
	Reader relationsReader = record.getRelationsReader();
	
	char[] buffer = new char[4096];
	
	int charsRead = relationsReader.read(buffer);
	assertEquals(-1, charsRead);
	assertTrue(Arrays.equals(new char[4096], buffer));
	
	record.setId(55);
	record.setSubject(SUBJECT_1);
	record.addRelation(RELATION_1_1);
	record.addRelation(RELATION_1_2);
	
	relationsReader = record.getRelationsReader();
	charsRead = relationsReader.read(buffer);
	assertEquals(72, charsRead);
	assertEquals(RELATION_1_1 + "  " + RELATION_1_2, new String(buffer, 0, charsRead));
    }
}