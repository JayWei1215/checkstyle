package org.gradle.internal;

import org.gradle.api.Action;
import org.gradle.api.internal.exceptions.MarkedVerificationException;
import org.gradle.api.internal.project.antbuilder.AntBuilderDelegate;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.internal.logging.ConsoleRenderer;
import org.gradle.util.GradleVersion;
import org.gradle.util.VersionNumber;
import groovy.xml.XmlParser;
import groovy.util.Node;
import org.gradle.util.internal.GFileUtils;

import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class CheckstyleInvoker implements Action<AntBuilderDelegate> {
    private static final Logger LOGGER = Logging.getLogger(CheckstyleInvoker.class);

    private static final String FAILURE_PROPERTY_NAME = "org.gradle.checkstyle.violations";
    private static final String CONFIG_LOC_PROPERTY = "config_loc";
    private File xmlOuputLocation;

    private final CheckstyleActionParameters parameters;

    public CheckstyleInvoker(CheckstyleActionParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public void execute(AntBuilderDelegate ant) {
        FileCollection source = parameters.getSource().getAsFileTree();
        boolean showViolations = parameters.getShowViolations().get();
        int maxErrors = parameters.getMaxErrors().get();
        int maxWarnings = parameters.getMaxWarnings().get();
        Map<String, Object> configProperties = parameters.getConfigProperties().getOrElse(new HashMap<>());
        boolean ignoreFailures = parameters.getIgnoreFailures().get();
        File config = parameters.getConfig().getAsFile().get();
        File configDir = parameters.getConfigDirectory().getAsFile().getOrNull();
        boolean isXmlRequired = parameters.getIsXmlRequired().get();
        boolean isHtmlRequired = parameters.getIsHtmlRequired().get();
        boolean isSarifRequired = parameters.getIsSarifRequired().get();
        xmlOuputLocation = parameters.getXmlOuputLocation().getAsFile().getOrElse(null);
        Property<String> stylesheetString = parameters.getStylesheetString();
        File htmlOuputLocation = parameters.getHtmlOuputLocation().getAsFile().getOrElse(null);
        File sarifOutputLocation = parameters.getSarifOutputLocation().getAsFile().getOrElse(null);

        VersionNumber currentToolVersion = determineCheckstyleVersion(Thread.currentThread().getContextClassLoader());
        boolean sarifSupported = isSarifSupported(currentToolVersion);

        if (isHtmlReportEnabledOnly(isXmlRequired, isHtmlRequired)) {
            xmlOuputLocation = new File(parameters.getTemporaryDir().getAsFile().get(), xmlOuputLocation.getName());
        }

        if (configDir != null) {
            Object userProvidedConfigLoc = configProperties.get(CONFIG_LOC_PROPERTY);
            if (userProvidedConfigLoc != null) {
                throw new IllegalArgumentException("Cannot add config_loc to checkstyle.configProperties. Please configure the configDirectory on the checkstyle task instead.");
            }
        }

        try {
            ant.invokeMethod("taskdef", new Object[]{"name", "checkstyle", "classname", "com.puppycrawl.tools.checkstyle.CheckStyleTask"});
        } catch (RuntimeException ignore) {
            ant.invokeMethod("taskdef", new Object[]{"name", "checkstyle", "classname", "com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask"});
        }

        try {
            Map<String, Object> checkstyleArgs = new HashMap<>();
            checkstyleArgs.put("config", config);
            checkstyleArgs.put("failOnViolation", false);
            checkstyleArgs.put("maxErrors", maxErrors);
            checkstyleArgs.put("maxWarnings", maxWarnings);
            checkstyleArgs.put("failureProperty", FAILURE_PROPERTY_NAME);

            ant.invokeMethod("checkstyle", new Object[]{checkstyleArgs, (Action<AntBuilderDelegate>) ant1 -> {
                source.addToAntBuilder(ant1, "fileset", FileCollection.AntType.FileSet);

                if (showViolations) {
                    Map<String, Object> formatterArgs = new HashMap<>();
                    formatterArgs.put("type", "plain");
                    formatterArgs.put("useFile", false);
                    ant1.invokeMethod("formatter", new Object[]{formatterArgs});
                }

                if (isXmlRequired || isHtmlRequired) {
                    Map<String, Object> formatterArgs = new HashMap<>();
                    formatterArgs.put("type", "xml");
                    formatterArgs.put("toFile", xmlOuputLocation);
                    ant1.invokeMethod("formatter", new Object[]{formatterArgs});
                }

                if (isSarifRequired) {
                    if (sarifSupported) {
                        Map<String, Object> formatterArgs = new HashMap<>();
                        formatterArgs.put("type", "sarif");
                        formatterArgs.put("toFile", sarifOutputLocation);
                        ant1.invokeMethod("formatter", new Object[]{formatterArgs});
                    } else {
                        assertUnsupportedReportFormatSARIF(currentToolVersion);
                    }
                }

                for (Map.Entry<String, Object> entry : configProperties.entrySet()) {
                    Map<String, Object> propertyArgs = new HashMap<>();
                    propertyArgs.put("key", entry.getKey());
                    propertyArgs.put("value", entry.getValue().toString());
                    ant1.invokeMethod("property", new Object[]{propertyArgs});
                }

                // Use configDir for config_loc
                Map<String, Object> propertyArgs = new HashMap<>();
                propertyArgs.put("key", CONFIG_LOC_PROPERTY);
                propertyArgs.put("value", configDir.toString());
                ant1.invokeMethod("property", new Object[]{propertyArgs});
            }});
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred configuring and executing Checkstyle.", e);
        }

        if (isHtmlRequired) {
            String stylesheet;
            if (stylesheetString.isPresent()) {
                stylesheet = stylesheetString.get();
            } else {
                try (InputStream is = CheckstyleInvoker.class.getClassLoader().getResourceAsStream("checkstyle-noframes-sorted.xsl")) {
                    if (is != null) {
                        stylesheet = new String(is.readAllBytes());
                    } else {
                        throw new RuntimeException("Could not find 'checkstyle-noframes-sorted.xsl' resource.");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error reading 'checkstyle-noframes-sorted.xsl' resource.", e);
                }
            }

            Map<String, Object> xsltArgs = new HashMap<>();
            xsltArgs.put("in", xmlOuputLocation);
            xsltArgs.put("out", htmlOuputLocation);

            ant.invokeMethod("xslt", new Object[]{xsltArgs, (Action<AntBuilderDelegate>) ant1 -> {
                Map<String, String> paramArgs = new HashMap<>();
                paramArgs.put("name", "gradleVersion");
                paramArgs.put("expression", GradleVersion.current().toString());
                ant1.invokeMethod("param", new Object[]{paramArgs});

                Map<String, Object> styleArgs = new HashMap<>();
                styleArgs.put("string", stylesheet);
                ant1.invokeMethod("style", new Object[]{styleArgs});
            }});
        }

        if (isHtmlReportEnabledOnly(isXmlRequired, isHtmlRequired)) {
            GFileUtils.deleteQuietly(xmlOuputLocation);
        }

        Node reportXml = parseCheckstyleXml(isXmlRequired, xmlOuputLocation);

        String message = getMessage(isXmlRequired, xmlOuputLocation, isHtmlRequired, htmlOuputLocation, isSarifRequired, sarifOutputLocation, reportXml);

        Boolean failurePropertyExists = checkFailureProperty(ant);
        if (Boolean.TRUE.equals(failurePropertyExists) && !ignoreFailures) {
            throw new MarkedVerificationException(message);
        } else {
            if (violationsExist(reportXml)) {
                LOGGER.warn(message);
            }
        }
    }

    private static VersionNumber determineCheckstyleVersion(ClassLoader antLoader) {
        Class<?> checkstyleTaskClass;
        try {
            checkstyleTaskClass = antLoader.loadClass("com.puppycrawl.tools.checkstyle.CheckStyleTask");
        } catch (ClassNotFoundException e) {
            try {
                checkstyleTaskClass = antLoader.loadClass("com.puppycrawl.tools.checkstyle.ant.CheckstyleAntTask");
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("Unable to load Checkstyle class", ex);
            }
        }
        return VersionNumber.parse(checkstyleTaskClass.getPackage().getImplementationVersion());
    }

    private static boolean isSarifSupported(VersionNumber versionNumber) {
        return versionNumber.compareTo(VersionNumber.parse("10.3.3")) >= 0;
    }

    private static void assertUnsupportedReportFormatSARIF(VersionNumber version) {
        throw new GradleException("SARIF report format is supported on Checkstyle versions 10.3.3 and newer. Please upgrade from Checkstyle " + version + " or disable the SARIF format.");
    }

    private static Node parseCheckstyleXml(boolean isXmlRequired, File xmlOutputLocation) {
        if (isXmlRequired) {
            try {
                return new XmlParser().parse(xmlOutputLocation);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing Checkstyle XML report", e);
            }
        }
        return null;
    }

    private static String getMessage(boolean isXmlRequired, File xmlOutputLocation, boolean isHtmlRequired, File htmlOutputLocation, boolean isSarifRequired, File sarifOutputLocation, Node reportXml) {
        return "Checkstyle rule violations were found."
                + getReportUrlMessage(isXmlRequired, xmlOutputLocation, isHtmlRequired, htmlOutputLocation, isSarifRequired, sarifOutputLocation)
                + getViolationMessage(reportXml);
    }

    private static String getReportUrlMessage(boolean isXmlRequired, File xmlOutputLocation, boolean isHtmlRequired, File htmlOutputLocation, boolean isSarifRequired, File sarifOutputLocation) {
        File outputLocation;
        if (isHtmlRequired) {
            outputLocation = htmlOutputLocation;
        } else if (isXmlRequired) {
            outputLocation = xmlOutputLocation;
        } else if (isSarifRequired) {
            outputLocation = sarifOutputLocation;
        } else {
            outputLocation = null;
        }
        return outputLocation != null ? " See the report at: " + new ConsoleRenderer().asClickableFileUrl(outputLocation) : "\n";
    }

    private static String getViolationMessage(Node reportXml) {
        if (violationsExist(reportXml)) {
            int errorFileCount = getErrorFileCount(reportXml);
            Object violations = null;
            try {
                violations = reportXml.children()
                        .stream()
                        .flatMap(node -> ((Node) node).children().stream())
                        .collect(Collectors.groupingBy(node -> ((Node) node).attribute("severity").toString(), Collectors.counting()));
            } catch (Exception e) {
                throw new RuntimeException("An unexpected error occurred getting violation message.", e);
            }

            return String.format("Checkstyle files with violations: %d%nCheckstyle violations by severity: %s%n", errorFileCount, violations);
        }
        return "\n";
    }

    private static boolean violationsExist(Node reportXml) {
        return reportXml != null && getErrorFileCount(reportXml) > 0;
    }

    private static int getErrorFileCount(Node reportXml) {
        Object errorFile = null;
        Map<Object, Object> errorFileMap = null;
        try {
            errorFile = reportXml.children()
                    .stream()
                    .collect(Collectors.groupingBy(node -> ((Node) node).attribute("name").toString()));
            errorFileMap = (Map<Object, Object>)errorFile;
        } catch (Exception e) {
            throw new RuntimeException("An unexpected error occurred getting error file count.", e);
        }
        return Objects.isNull(errorFileMap)? 0 :errorFileMap.keySet().size();
    }

    private static boolean isHtmlReportEnabledOnly(boolean isXmlRequired, boolean isHtmlRequired) {
        return !isXmlRequired && isHtmlRequired;
    }

    private static Boolean checkFailureProperty(AntBuilderDelegate ant) {
        final Boolean[] failurePropertyExists = {null};

        ant.invokeMethod("projectproperties", new Object[]{(Action<AntBuilderDelegate>) ant1 -> {
            failurePropertyExists[0] = ant1.getProperty(FAILURE_PROPERTY_NAME) != null;
        }});

        return failurePropertyExists[0];
    }
}
