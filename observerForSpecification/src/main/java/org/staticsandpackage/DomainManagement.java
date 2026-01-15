package org.staticsandpackage;

import java.util.HashSet;
import java.util.Set;

/**
 * It makes an assumption that each module has a root package, and the root package is the shortest package name
 */
public class DomainManagement {

    /**
     * This is a set of class names (.), parts involving inner classes are excluded($)
     */
    public static Set<String> domains = new HashSet<String>();

    public static String projectDomain;

    public static Set<String> packages = new HashSet<String>();

    public static String getProjectDomain(Set<String> target) {
        int shortest = 1000;
        String shortString = "";
        for (String domain : target) {
            int size = domain.split("\\.").length;
            if (size < shortest) {
                shortest = size;
                shortString = domain;
            }
        }
        int lastIndex = shortString.lastIndexOf(".");
        projectDomain = shortString.substring(0, lastIndex);
        return projectDomain;
    }

    public static String getPackageDomain(String testClass) {
        if (testClass.lastIndexOf(".") == -1) {
            return projectDomain;
        }
        String domain = testClass.substring(0, testClass.lastIndexOf("."));
        if (packages.contains(domain)) {
            return domain;
        } else {
            return getPackageDomain(domain);
        }
    }

    public static String getClassDomain(String testClass) {
        String possibleClassName = "";
        if (testClass.endsWith("Test")) {
            possibleClassName = testClass.substring(0, testClass.length() - 4);
        } else {
            String onlyClassName = testClass.substring(testClass.lastIndexOf(".") + 1);
            if (onlyClassName.startsWith("Test")) {
                possibleClassName = testClass.substring(0, testClass.lastIndexOf(".") + 1) + onlyClassName.substring(4);
            }
        }
        if (domains.contains(possibleClassName)) {
            return possibleClassName;
        } else {
            return getPackageDomain(testClass);
        }
    }

}
