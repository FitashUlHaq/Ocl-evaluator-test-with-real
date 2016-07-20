package test.example.eocl;
import static org.junit.Assert.*;

import java.io.File;

import org.eclipse.uml2.uml.InstanceSpecification;
import org.junit.Test;

import example.eocl.helper.EOCLHelper;

public class TestEclipseOCL {

	private File f=new File("model/Blank Package.uml");
	private String uri = f.toURI().toString();
	
	
	/**
	 * The function tests for evaluation of Real data type for UML
	 */
	@Test
	public void testRealDataType() {		
		//problem due to LiteralReal EOCLHelper line # 204
		EOCLHelper eHelper=new EOCLHelper();
		String expr = "Apple.allInstances()->select(f : Apple | f.colour>2.1)->isEmpty()";
		InstanceSpecification result = (InstanceSpecification) eHelper.process(uri, expr);
	
		assertNotEquals(result.getName(),"invalid");
	}
	
	/**
	 * The function tests for evaluation of operations for UML
	 */
	@Test
	public void testClassOperation() {
		//problem in handling uml class operation in OCL constraint
		EOCLHelper eHelper=new EOCLHelper();
		String expr = "Apple.allInstances()->select(f : Apple | f.getColour(1)>=10)->isEmpty()";
		Object result = eHelper.process(uri, expr);
		assertNotNull(result);
	}
	
}
