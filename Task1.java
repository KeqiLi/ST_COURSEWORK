import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import st.EntryMap;
import st.TemplateEngine;

public class Task1 {

    private EntryMap map, map2, map3;

    private TemplateEngine engine;

    @Before
    public void setUp() throws Exception {
        map = new EntryMap();
        map2 = new EntryMap();
        map3 = new EntryMap();
        engine = new TemplateEngine();
    }

    @Test
    public void Test1() {
        map.store("name", "Adam", false);
        map.store("surname", "Dykes", false);
        String result = engine.evaluate("Hello ${name} ${surname}", map,"delete-unmatched");
        assertEquals("Hello Adam Dykes", result);
    }
 
    
    @Test
    public void Test2() {
        map.store("name", "Adam", false);
        map.store("surname", "Dykes", false);
        map.store("age", "29", false);
        String result = engine.evaluate("Hello ${name}, is your age ${age ${symbol}}", map,"delete-unmatched");
        assertEquals("Hello Adam, is your age 29", result);
    }
    
    
    /*
     * EntryMap Spec1: Template cannot be null. If null, runtime exception.
     */
    @Test (expected = RuntimeException.class) 
    public void MAP_SPEC1_TemplateNullMap(){
        map.store(null, "Adam", false);
    }
    
    /*
     * EntryMap Spec2: Replace value cannot be null. If null, runtime exception.
     */
    @Test (expected = RuntimeException.class) 
    public void MAP_SPEC2_ReplaceNull(){
        map.store("day", null, false);
    }
    
    /*
     * EntryMap Spec3: When case_sensitive is null, result should be the same as it were false.
     * The templates/targets are given lower case in the EntryMap object, where the template
     * 
     * This test case should have passed but it fails due to NullPointerException. However, in
     * the spec3, it specifies that map.store() should be able to take null for the variable
     * case_sensitive.
     */
    @Test
    public void MAP_SPEC3_CaseSensitive() {

        map.store("name", "Adam", null);
        map2.store("name", "Adam", false);
        map3.store("name", "Adam", false);
        
        String result2 = engine.evaluate("${NAME}, hello!", map2, "delete-unmatched");
        String result3 = engine.evaluate("${NAME}, hello!", map3, "delete-unmatched");
        
        assertEquals("case-sensitive both false", result2, result3);
        
        String result1 = engine.evaluate("${NAME}, hello!", map, "delete-unmatched");
        assertEquals("case-sensitive false and null", result1, result2);
    }
    
    /*
     * EntryMap Spec4: The engine matches templates with the first matching entry from the map.
     * 
     */
    
    @Test
    public void MAP_SPEC4_EntryOrder(){
    	map.store("name", "Adam", false);
    	map.store("name", "Ben", false);
    	
    	String result = engine.evaluate("${name}, hello?", map, "keep-unmatched");
    	
    	map2.store("name", "Ben", false);
    	map2.store("name", "Adam", false);
    	
    	String result2 = engine.evaluate("${name}, hello?", map2, "keep-unmatched");
    	
    	assertEquals(result, "Adam, hello?");
    	assertEquals(result2, "Ben, hello?");
    	
    }
    
    /*
     * EntryMap Spec5: Entries that already exist cannot be stored again.
     * 
     * Map2 should store the entry with "true" first, then entry with "false"
     */
    @Test
    public void MAP_SPEC5_DuplicateEntries(){
    	map.store("name", "Adam", true);
    	String result = engine.evaluate("${Name}, hello?", map, "keep-unmatched");


    	map2.store("name", "Adam", true);
    	map2.store("name", "Adam", false);
    	
    	String result2 = engine.evaluate("${Name}, hello?", map2, "keep-unmatched");
    	
    	assertEquals(result, "${Name}, hello?");
    	assertEquals(result2, "Adam, hello?");

    	
    }
    
    /*
     * TemplateEngine Spec1: Template string can be null or empty. If null or empty, unchanged
     * template string will be returned.
     */
    @Test
    public void ENG_SPEC1_TemplateStringNullEngine() {
        map.store("name", "Adam", null);
        String result = engine.evaluate(null, map, "keep-unmatched");
        String result2 = engine.evaluate("", map, "keep-unmatched");
        
        assertEquals(result, null);
        assertEquals(result2, "");
    }
    
    /*
     * TemplateEngine Spec2: EntryMap object can be null or empty. If null or empty, unchanged
     * template string will be returned.
     */
    @Test
    public void ENG_SPEC2_MapNullEngine() {
        String result = engine.evaluate("${name}! Hello!", null, "keep-unmatched");
        String result2 = engine.evaluate("${name}! Hello!", map, "keep-unmatched");
        
        assertEquals(result, "${name}! Hello!");
        assertEquals(result2, "${name}! Hello!");
    }
    
    /*
     * TemplateEngine Spec3: matching-mode cannot be null. If null or other values,
     * default to "delete-unmatched". 
     * This test case should have passed with matching mode parameter being null.
     */
    @Test
    public void ENG_SPEC3_MatchingModeNull(){
        map.store("name", "Adam", false);
        String result = engine.evaluate("${name}! Hello!", map, "delete-unmatched");
        String result2 = engine.evaluate("${name}! Hello!", map, null);
        String result3 = engine.evaluate("${name}! Hello!", map, "othervalue");
        
        assertEquals(result, result2);
        assertEquals(result, result3);
    }
    
    /*
     * TemplateEngine Spec4: template boundaries are omitted.
     */
    @Test
    public void ENG_SPEC4_TemplateBoundaries(){
        map.store("name", "Adam", false);
        String result = engine.evaluate("${name}! Hello!", map, "keep-unmatched");
        String result2 = engine.evaluate("name! Hello!", map, "keep-unmatched");
        
        assertEquals(result, "Adam! Hello!");
        assertNotEquals(result2, "Adam! Hello!");
    }
    
    /*
     * TemplateEngine Spec5: non-visible character in templates should not affect the result
     */
    @Test
    public void ENG_SPEC5_TemplateNonVisibleChar(){
        map.store("middle name", "Adam", false);
        String result = engine.evaluate("${middle name}! Hello!", map, "keep-unmatched");
        String result2 = engine.evaluate("${middlename}! Hello!", map, "keep-unmatched");
        String result3 = engine.evaluate("${middle   name}! Hello!", map, "keep-unmatched");
        
        assertEquals(result, "Adam! Hello!");
        assertEquals(result2, "Adam! Hello!");
        assertEquals(result3, "Adam! Hello!");
    }
    
    /*
     * TemplateEngine Spec6: In a template string every "${" and "}" 
     * occurrence acts as a boundary of at MOST one template.
     * 
     */
    @Test
    public void ENG_SPEC6_BoundariesMatching(){
    	map.store("name", "Adam", false);
    	String result = engine.evaluate("}${name}, hello!${", map, "keep-unmatched");
    	
    	assertEquals(result, "}Adam, hello!${");
    }
    
    
    
    /*
     * TemplateEngine Spec7: Templates are stored in order of length, thus "competition" will be 
     * evaluated before "we should try or best for winning the this won't be shown cup."
     * 
     */
    @Test
    public void ENG_SPEC7_PatternsOrdering(){
        map.store("name", "Adam", false);
        map.store("competition", "this won't be shown", false);
        map.store("we should try or best for winning the this won't be shown cup.", "this should be shown", false);

        String result = engine.evaluate("I heard that }: ${name} said: ${we should try or best for winning the ${competition} cup.}", map, "keep-unmatched");
        
        assertEquals(result, "I heard that }: Adam said: this should be shown");
    }
    
    /*
     * TemplateEngine Spec8: Templates are matched one at a time exhaustively, according to the order.
     * 
     * If delete-unmatched templates, the ${age ${symbol}} can be substituted, as ${symbol} will be deleted;
     * If keep-unmatched templates, the ${age ${symbol}} cannot be substituted, 
     * as ${age ${symbol}} does not match with ${age}
     */
    @Test
    public void ENG_SPEC8_ExhaustReplacement(){
    	map.store("name", "Adam", false);
    	map.store("surname", "Dykes", false);
    	map.store("age", "29", false);
    	
    	String result = engine.evaluate("Hello ${name}, is your age ${age ${symbol}}", map, "delete-unmatched");
    	assertEquals(result, "Hello Adam, is your age 29");
    	
    	String result2 = engine.evaluate("Hello ${name}, is your age ${age ${symbol}}", map, "keep-unmatched");
    	assertEquals(result2, "Hello Adam, is your age ${age ${symbol}}");
    }
    
}