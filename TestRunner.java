package com.yuurei.testframework;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

public class TestRunner {

    private static String runTest(Object instance, Method method){
        String report = "";
        ExpectedException expectedException = method.getAnnotation(ExpectedException.class);
        if (method == null){
            return report;
        }
        try {
            method.invoke(instance);
            if (expectedException != null) {
                report += "expected " + expectedException.value().getName() + " hasn't been thrown, ";
            }
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException().getClass() == AssertionError.class) {
                report += "assertEquals failed, ";
            }
            else if (expectedException == null || expectedException.value() != e.getTargetException().getClass()) {
                report += "unexpected exception " + e.getTargetException().getClass().getName() + ", ";
            }
        }
        catch (IllegalAccessException e) {
            report += "Test method is not accessible";
        }

        return report;
    }

    private static ArrayList<Method> getAnnotatedMethods(Class classObj, Class<? extends Annotation> annotation){
        Method[] allMethods = classObj.getMethods();
        ArrayList<Method> annotated = new ArrayList<>();
        for (Method method: allMethods) {
            if (method.getAnnotation(annotation) != null){
                annotated.add(method);
            }
        }

        return annotated;
    }

    private static Method getUniqueAnnotated(Class classObj, Class<? extends Annotation> annotation)
            throws AnnotationIsNotUniqueException {
        ArrayList<Method> annotatedMethods = getAnnotatedMethods(classObj, annotation);

        if(annotatedMethods.size() > 1){
            throw new AnnotationIsNotUniqueException();
        }
        else if (annotatedMethods.size() == 0){
            return null;
        }
        else{
            return annotatedMethods.get(0);
        }
    }

    private static void messageError(String message){
        System.err.println(message);
        System.exit(0);
    }

    private static void messagePassed(String methodName){
        System.out.println("Test has been passed for " + methodName + " method.");
    }

    private static void messageFailed(String methodName, String report){
        System.out.println("Test has been failed for " + methodName + " method. Report:  " + report + ".");
    }

    public static void main(String[] args) {
        if (args.length != 1){
            messageError("Pass only one argument, please.");
        }

        Class classTest;
        try {
            classTest = Class.forName(args[0]);
        }
        catch (Throwable e) {
            messageError("Class not found.");
            return;
        }

        ArrayList<Method> annotatedTest = getAnnotatedMethods(classTest, Test.class);

        Method annotatedBefore;
        try {
            annotatedBefore = getUniqueAnnotated(classTest, Before.class);
        }
        catch (AnnotationIsNotUniqueException e) {
            messageError("You can have only one method annotated with @Before.");
            return;
        }

        Method annotatedAfter;
        try {
            annotatedAfter = getUniqueAnnotated(classTest, After.class);
        }
        catch (AnnotationIsNotUniqueException e) {
            messageError("You can have only one method annotated with @After.");
            return;
        }

        Object classTestInstance;
        try{
            classTestInstance = classTest.newInstance();
        }
        catch (Throwable e){
            messageError("Can't instantiate a new class instance.");
            return;
        }

        Iterator<Method> iterator = annotatedTest.iterator();
        while (iterator.hasNext()){
            String report = runTest(classTestInstance, annotatedBefore);
            if (report.isEmpty()){
                messagePassed("@Before annotated " + annotatedBefore.getName());
            }
            else{
                messageFailed("@Before annotated" + annotatedBefore.getName(), report);
            }

            Method testMethod = iterator.next();
            report = runTest(classTestInstance, testMethod);
            if (report.isEmpty()){
                messagePassed(testMethod.getName());
            }
            else {
                messageFailed(testMethod.getName(), report);
            }

            report = runTest(classTestInstance, annotatedAfter);
            if (report.isEmpty()) {
                messagePassed("@After annotated " + annotatedAfter.getName());
            }
            else {
                messageFailed("@After annotated " + annotatedAfter.getName(), report);
            }
        }

        return;
    }
}
