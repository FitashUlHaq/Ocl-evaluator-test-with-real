package example.eocl.helper;
import java.util.Collection;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.ocl.Environment;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.ParserException;
import org.eclipse.ocl.expressions.OCLExpression;
import org.eclipse.ocl.helper.OCLHelper;
import org.eclipse.ocl.uml.UMLEnvironmentFactory;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.InstanceSpecification;
import org.eclipse.uml2.uml.InstanceValue;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralNull;
import org.eclipse.uml2.uml.LiteralReal;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Slot;
import org.eclipse.uml2.uml.StructuralFeature;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValueSpecification;

public class EOCLHelper {
	protected OCL ocl;

	protected OCLHelper helper;

	/**
	 * This method creates an instance of the class supplied
	 * @param pkg - the package containing class
	 * @param classifier - the class whose instance would be created
	 * @return
	 */
	protected InstanceSpecification instantiate(Package pkg, Classifier classifier) {
		InstanceSpecification result = (InstanceSpecification) pkg.createPackagedElement(
				null, UMLPackage.eINSTANCE.getInstanceSpecification());

		if (classifier != null) {
			result.getClassifiers().add(classifier);
		}

		return result;
	}
	/**
	 * This method creates an instance of the slot according to the property supplied and fills it with the value supplied
	 * @param instance - Instance of the desired class 
	 * @param property - attribute of the instance
	 * @param value - value to for the attribute
	 * @return
	 */
	protected Slot setValue(
			InstanceSpecification instance,
			Property property,
			Object value) {

		Slot result = null;

		for (Slot slot : instance.getSlots()) {
			if (slot.getDefiningFeature() == property) {
				result = slot;
				slot.getValues().clear();
				break;
			}
		}

		if (result == null) {
			result = instance.createSlot();
			result.setDefiningFeature(property);
		}

		if (value instanceof Collection<?>) {
			for (Object e : (Collection<?>) value) {
				addValue(result, e);
			}
		} else {
			addValue(result, value);
		}

		return result;
	}
	
	protected Slot setValue(
			InstanceSpecification instance,
			Operation operation,
			Object value) {

		Slot result = null;

		for (Slot slot : instance.getSlots()) {
			if (slot.getDefiningFeature() == operation) {
				result = slot;
				slot.getValues().clear();
				break;
			}
		}

		if (result == null) {
			result = instance.createSlot();
			result.setDefiningFeature((StructuralFeature) operation);
		}

		if (value instanceof Collection<?>) {
			for (Object e : (Collection<?>) value) {
				addValue(result, e);
			}
		} else {
			addValue(result, value);
		}

		return result;
	}
	/**
	 * This method fills the supplied slot with the supplied value and returns an instance of ValueSpecification
	 * @param slot - Refers to the attribute of Class
	 * @param value - Value of the attribute
	 * @return
	 */
	protected ValueSpecification addValue(Slot slot, Object value) {
		ValueSpecification result;

		if (value instanceof InstanceSpecification) {
			InstanceValue valueSpec = (InstanceValue) slot.createValue(
					null, null, UMLPackage.eINSTANCE.getInstanceValue());
			valueSpec.setInstance((InstanceSpecification) value);
			result = valueSpec;
		} else if (value instanceof String) {
			LiteralString valueSpec = (LiteralString) slot.createValue(
					null, null, UMLPackage.eINSTANCE.getLiteralString());
			valueSpec.setValue((String) value);
			result = valueSpec;
		} 
		else if (value instanceof Integer) {
			LiteralInteger valueSpec = (LiteralInteger) slot.createValue(
					null, null, UMLPackage.eINSTANCE.getLiteralInteger());
			valueSpec.setValue(((Integer) value).intValue());
			result = valueSpec;
		} else if (value instanceof Boolean) {
			LiteralBoolean valueSpec = (LiteralBoolean) slot.createValue(
					null, null, UMLPackage.eINSTANCE.getLiteralBoolean());
			valueSpec.setValue(((Boolean) value).booleanValue());
			result = valueSpec;
		} else if (value == null) {
			LiteralNull valueSpec = (LiteralNull) slot.createValue(
					null, null, UMLPackage.eINSTANCE.getLiteralNull());
			result = valueSpec;
		} 

		else if (value instanceof Double) {
			 
			/*
			 * FIXME
			 * Use of LiteralReal results in OCLInvalid 
			 */
			LiteralReal valueSpec=UMLFactory.eINSTANCE.createLiteralReal();
			slot.getValues().add(valueSpec);
			valueSpec.setValue((double) value);
			result = valueSpec;
		} 
		else {
			throw new IllegalArgumentException("Unrecognized slot value: " + value);
		}

		return result;
	}
	/**
	 * This method returns the result after evaluating the constraint on Class's instance
	 * @param aHelper - Helper instance
	 * @param context - Class whose instance is to be validated
	 * @param expression - OCL Constraint
	 * @return
	 * @throws ParserException
	 */
	protected Object evaluate(OCLHelper aHelper,
			Object context,
			String expression) throws ParserException {

		OCLExpression query = aHelper.createQuery(expression);
		ocl.setEvaluationTracingEnabled(true);


		return ocl.evaluate(context, query);
	}
	/**
	 * This method loads the Class Diagram, retrieves the desired elements , 
	 * creates an instance of the desired class and 
	 * evaluates the constraint for the Class's instance
	 * @param ur - Path of Class Diagram's uml file
	 */
	public Object process(String ur, String expr)
	{
		Package SourcePackage;
		Package nestedPackage;

		Property fruit_color;

		// Creating instanc of UMLEnvironment factory
		Environment.Registry.INSTANCE.registerEnvironment(
				new UMLEnvironmentFactory().createEnvironment());
		// Creating an instance of ResourceSet
		ResourceSet resourceSet = new ResourceSetImpl();
		org.eclipse.ocl.uml.OCL.initialize(resourceSet);
		// Creating an instance of OCL
		ocl = 	org.eclipse.ocl.uml.OCL.newInstance(resourceSet);

		// Creating a Resource instance
		Resource resource = null;
		URI modelUri = URI.createURI(ur);
		try {	
			// Initializing the Resource instance with supplied Class Diagram
			resource = resourceSet.getResource(modelUri,true);//, true);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		// Retrieving the source package of Class Diagram
		SourcePackage = (Package) resource.getContents().get(0);
		// Retrieving the nested package in Class Diagram 
		nestedPackage = SourcePackage.getNestedPackages().get(0);
		// Retrieving the desired class from nested package 
		Class appleClass= (Class) nestedPackage.getOwnedType("Apple");
		// Retrieving the desired attribute from selected class
		fruit_color=appleClass.getOwnedAttribute("colour", null);

		//get class operation
		Operation fruitOperation = appleClass.getOwnedOperations().get(0);

		// Creating a Helper instance
		helper =ocl.createOCLHelper();
		// Setting the context of helper instance 
		helper.setContext(appleClass);

		// creating a temporary Resource and adding a package in it
		Resource res = new ResourceImpl();
		Package pkg = UMLPackage.eINSTANCE.getUMLFactory().createPackage();
		pkg.setName("instances");
		res.getContents().add(pkg);	

		// Creating an instance of the selected class upon which the constraint would be validated
		InstanceSpecification aFruit = instantiate(pkg, appleClass);
		
		// Setting an real value of the instance "color" attribute 
		setValue(aFruit, fruit_color, 10.1);
		Object result = null;
		try {
			// evaluating the constraint on the class's instance and printing the result
			result = evaluate(helper, aFruit, expr);
			System.out.println("Result : "+ result);
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return result;
	}

}
