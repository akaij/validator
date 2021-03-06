import static org.junit.Assert.*;

import org.junit.*;
import org.junit.runner.RunWith;
import java.io.*;

import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.validator.api.ValidatorUtils;
import org.biopax.validator.api.Validator;
import org.biopax.validator.api.beans.Validation;
import org.biopax.validator.impl.IdentifierImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This Is an Integration Test.
 *
 * Moreover, this here uses the 'production' application context,
 * i.e., enabling all the AOP aspects and Load-Time Weaving (LTW)! 
 * So, LTW is the one that sometimes makes things complicated 
 * (I believe, - due to Spring's bug...)
 *
 * NOTE: 
 *  Surprisingly, but the (sad) fact here is that, 
 *  when using @DirtiesContext annotation with a test method,
 *  it reloads the context, but, in fact, not everything.
 *  E.g., ControlAspect references not the same 'validator' bean
 *  instance as the autowired one here... 
 *  
 *  So, we do not want to use '@DirtiesContext' in this test class... 
 *
 * TODO Report the issue to Springsource
 *
 * @author rodche
 */
//@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
		"/META-INF/spring/appContext-loadTimeWeaving.xml",
		"/META-INF/spring/appContext-validator.xml"})
public class AOPAspectJLTWIntegrationTest {
    @Autowired
    Validator validator;
    
    @Autowired
    ValidatorUtils utils;
    
    @Autowired
    ApplicationContext context;

    BioPAXFactory factory3 = BioPAXLevel.L3.getDefaultFactory();
    
    @Test
    public void testValidator() throws IOException {
    	Resource resource = context
    		.getResource("classpath:biopax3-short-metabolic-pathway.owl");
    	Validation result = new Validation(new IdentifierImpl());
    	result.setDescription(resource.getDescription());
    	validator.importModel(result, resource.getInputStream());
        validator.validate(result); // check all rules
        validator.getResults().clear(); // clean after itself
        
        assertFalse(result.getError().isEmpty());
    }
     
    
    @Test
    public void testUnknownProperty() throws IOException {
    	Validation validation = new Validation(new IdentifierImpl());
    	validator.importModel(validation, getClass()
    		.getResourceAsStream("testSyntaxErrors.xml")); 
    	validator.getResults().clear(); // clean after itself
    	assertEquals(1, validation.countErrors(null, null, "unknown.property", null, false, false));
    	assertEquals(0, validation.countErrors(null, null, "syntax.error", null, false, false));  
    }
    
    
//    @Ignore("worked with paxtools 4.2.1; but paxtools 4.3.0-SNAPSHOT dependency breaks it; fixed 19-Mar-2014")
    @Test
    public void testSyntaxErrors() throws IOException {
    	Validation validation = new Validation(new IdentifierImpl());
    	validator.importModel(validation, getClass()
    		.getResourceAsStream("testBiochemPathwayStepOneConversionRule.owl")); //misplaced.step.conversion
    	validator.getResults().clear(); // clean after itself
    	assertFalse(validation.getError().isEmpty());
    	assertEquals(1, validation.countErrors(null, null, "syntax.error", null, false, false));   	
    }

    
    @Test
    public void testClonedUtilityClass() throws IOException {
    	Validation validation = new Validation(new IdentifierImpl());
    	validator.importModel(validation, getClass().getResourceAsStream("testEvidenceEquivalence.xml")); 
    	validator.validate(validation);
    	validator.getResults().clear(); // clean after itself
    	System.out.println(validation.getError().toString());
    	//there are several errors related to CV use,
    	assertFalse(validation.getError().isEmpty());
    	//but - no cloned.utility.class error case
    	assertEquals(0, validation.countErrors(null, null, "cloned.utility.class", null, false, false));
    }
    
    @Test
    public void testMemberPhysicalEntityRange() throws IOException {
    	Validation validation = new Validation(new IdentifierImpl());
    	validator.importModel(validation, getClass()
    			.getResourceAsStream("testMemberPhysicalEntityRange.xml")); 
    	validator.validate(validation);
    	validator.getResults().clear(); // clean after itself
    	assertEquals(1, validation.countErrors(null, null, "syntax.error", null, false, false));
    }
}
