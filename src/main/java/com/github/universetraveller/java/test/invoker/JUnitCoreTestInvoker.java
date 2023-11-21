package com.github.universetraveller.java.test.invoker;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.Failure;


/**
 * ParseResult
 */
class ParseResult {
    private URLClassLoader loader;
    private Map<String, List<String>> methodMap;
    public ParseResult(URLClassLoader loader, Map<String, List<String>> methodMap) {
        this.loader = loader;
        this.methodMap = methodMap;
    }
    public URLClassLoader getLoader() {
        return loader;
    }
    public Map<String, List<String>> getMethodMap() {
        return methodMap;
    }
}
public class JUnitCoreTestInvoker {
    public static void main(String[] args) {
        Result result = run(parse(args));
        System.out.println(String.format("Run %s tests in %s ms; ignore %s tests; skip %s tests; %s tests failed --- %s",
         result.getRunCount(),
          result.getRunTime(),
          result.getIgnoreCount(),
          result.getAssumptionFailureCount(),
          result.getFailureCount(),
          result.wasSuccessful() ? "PASS" : "FAIL"));
        for(Failure f : result.getFailures()){
            System.out.println("---");
            System.out.println(f.getDescription());
            System.out.println(f.getTrace());
        }
    }

    private static ParseResult parse(String[] args) {
        Map<String, List<String>> methodMap = new HashMap<>();
        for(String method : args[0].split("#")){
            String[] pair = method.split("::");
            methodMap.putIfAbsent(pair[0], new ArrayList<>());
            methodMap.get(pair[0]).add(pair[1]);
        }
        String[] classPathElements = args[1].split(":");
        List<URL> urls = new ArrayList<>();
        for(String classPathElement : classPathElements){
            if(classPathElement.isEmpty())
                continue;
            try{
                urls.add(new File(classPathElement).toURI().toURL());
            }catch(MalformedURLException e){
                e.printStackTrace();
                continue;
            }
        }
        /*
        * return new ParseResult(new URLClassLoader(urls.toArray(new URL[0]), null), methodMap);
        * will cause error initializationError(org.junit.runner.manipulation.Filter); The root cause is computeTestMethods() of runner
        * which finds test methods from testClass via annotation @Test. However, when we use URLClassLoader to load debuggee project,
        * @Test keeps different between URLClassLoader and AppClassLoader which is used to load JUnitCore (different ids)
        * thus runner cannot find any available methods from project. Then the cause of it is covered by filter that makes me hard to localize it
        * Do not specify null leads @Test being directly loaded via AppClassLoader so that it does not encounter this problem
        */
        return new ParseResult(new URLClassLoader(urls.toArray(new URL[0])), methodMap);
    }

    public static Result run(ParseResult result) {
        Filter ft = new Filter() {
            @Override
            public boolean shouldRun(Description description) {
                return false;
            }

            @Override
            public String describe() {
                return "none";
            }

            @Override
            public void apply(Object child) throws NoTestsRemainException {
                // do nothing
            }

            @Override
            public Filter intersect(Filter second) {
                return second;
            }
        };
        Map<String, List<String>> methodMap = result.getMethodMap();
        URLClassLoader loader = result.getLoader();
        Class[] classes = new Class[methodMap.keySet().size()];
        int idx = 0;
        for(String clz : methodMap.keySet()){
            try{
                Class clazz = loader.loadClass(clz);
                classes[idx] = clazz;
                idx++;
                for(String met : methodMap.get(clz))
                    ft = merge(ft, Filter.matchMethodDescription(Description.createTestDescription(clazz, met)));
            }catch(ClassNotFoundException e){
                e.printStackTrace();
                continue;
            }
        }
        return new JUnitCore().run(Request.classes(classes).filterWith(ft));
    }

    public static Filter merge(final Filter first, final Filter second) {
        if (second == first) {
            return first;
        }
        return new Filter() {
            @Override
            public boolean shouldRun(Description description) {
                return first.shouldRun(description)
                        || second.shouldRun(description);
            }

            @Override
            public String describe() {
                return first.describe() + " or " + second.describe();
            }
        };
    }
}
