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

public class DefaultClassLoaderJUnitCoreTestInvoker {
    public static void main(String[] args) {
        Result result = run(parse(args));
        System.out
                .println(String.format("Run %s tests in %s ms; ignore %s tests; %s tests failed --- %s",
                        result.getRunCount(),
                        result.getRunTime(),
                        result.getIgnoreCount(),
                        result.getFailureCount(),
                        result.wasSuccessful() ? "PASS" : "FAIL"));
        for (Failure f : result.getFailures()) {
            System.out.println("---");
            System.out.println(f.getDescription());
            System.out.println(f.getTrace());
        }
    }

    private static Map<String, List<String>> parse(String[] args) {
        Map<String, List<String>> methodMap = new HashMap<>();
        for (String method : args[0].split("#")) {
            String[] pair = method.split("::");
            methodMap.putIfAbsent(pair[0], new ArrayList<>());
            methodMap.get(pair[0]).add(pair[1]);
        }
        return methodMap;
    }

    public static Result run(Map<String, List<String>> methodMap) {
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
        Class[] classes = new Class[methodMap.keySet().size()];
        int idx = 0;
        for (String clz : methodMap.keySet()) {
            try {
                Class clazz = Class.forName(clz);
                classes[idx] = clazz;
                idx++;
                for (String met : methodMap.get(clz))
                    ft = merge(ft, Filter.matchMethodDescription(Description.createTestDescription(clazz, met)));
            } catch (ClassNotFoundException e) {
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
