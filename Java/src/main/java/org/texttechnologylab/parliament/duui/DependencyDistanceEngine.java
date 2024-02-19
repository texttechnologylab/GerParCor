package org.texttechnologylab.parliament.duui;

import com.google.gson.Gson;
import de.tudarmstadt.ukp.dkpro.core.api.io.JCasFileWriter_ImplBase;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.PUNCT;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.ROOT;
import io.azam.ulidj.ULID;
import org.apache.commons.codec.binary.Hex;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASRuntimeException;
import org.apache.uima.cas.Feature;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.AnnotationBase;
import org.apache.uima.jcas.tcas.Annotation;
import org.texttechnologylab.annotation.DocumentAnnotation;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DependencyDistanceEngine extends JCasFileWriter_ImplBase {

    public static final String PARAM_FAIL_ON_ERROR = "pFailOnError";

    @ConfigurationParameter(name = PARAM_FAIL_ON_ERROR, mandatory = false, defaultValue = "false")
    protected Boolean pFailOnError;

    public static final String PARAM_ULID_SUFFIX = "pUlidSuffix";

    @ConfigurationParameter(name = PARAM_ULID_SUFFIX, mandatory = false, defaultValue = "false")
    protected Boolean pUlidSuffix;

    public static final String PARAM_FIX_DATE_YEAR = "pFixDateYear";

    @ConfigurationParameter(name = PARAM_FIX_DATE_YEAR, mandatory = false, defaultValue = "true")
    protected Boolean pFixDateYear;

    public static final String PARAM_FIX_DATE_YEAR_VALID_FROM = "pFixDateYearValidFrom";

    @ConfigurationParameter(name = PARAM_FIX_DATE_YEAR_VALID_FROM, mandatory = false, defaultValue = "1700")
    protected int pFixDateYearValidFrom;

    public static final String PARAM_FIX_DATE_YEAR_VALID_TO = "pFixDateYearValidTo";

    @ConfigurationParameter(name = PARAM_FIX_DATE_YEAR_VALID_TO, mandatory = false, defaultValue = "2024")
    protected int pFixDateYearValidTo;

    protected final Pattern[] allPatterns = new Pattern[] {
            Pattern.compile("(?!vom |am )(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})"),
            Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4})"),
            Pattern.compile("(?!vom |am )(\\d{1,2})\\.?\\s*(\\p{L}+)\\s*(\\d{4})"),
            Pattern.compile("(\\d{1,2})\\.?\\s*(\\p{L}+)\\s*(\\d{4})")
    };

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        try {
            final DocumentDataPoint documentDataPoint = DocumentDataPoint.fromJCas(jCas);

            String dateYear = getDateYear(documentDataPoint);

            String metaHash = documentDataPoint.getMetaHash();

            String outputFile = String.join("/", dateYear, metaHash);
            if (pUlidSuffix) {
                outputFile = String.join("-", outputFile, ULID.random());
            }

            try {
                // Try to get the output stream _before_ processing the document
                // as we will get an IOException if the target file already exists
                NamedOutputStream outputStream = getOutputStream(outputFile, ".json");

                try {
                    processDocument(jCas, documentDataPoint);
                } catch (Exception e) {
                    // Unexpected: processDocument() is pretty safe, so something bad happened
                    throw new AnalysisEngineProcessException("Error while processing the document. This should not happen!", null, e);
                }

                try {
                    save(documentDataPoint, outputStream);
                } catch (IOException e) {
                    // Unexpected: We could not write to the output stream?
                    throw new AnalysisEngineProcessException("Could not save document data point to output stream.", null, e);
                }
            } catch (IOException e) {
                // Expected: getOutputStream() failed, most likely because the target file
                // already exists
                getLogger().error(e.getMessage());
                if (pFailOnError) throw new AnalysisEngineProcessException(e);
            }
        } catch (AnalysisEngineProcessException e) {
            // Something unexpected happened or an execption was passed on because
            // pFailOnError is true
            getLogger().error(e.getMessage());
            e.printStackTrace();
            if (pFailOnError) {
                throw e;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void processDocument(JCas jCas, final DocumentDataPoint documentDataPoint) {
        final ArrayList<Sentence> sentences = new ArrayList<>(new ArrayList<>(JCasUtil.select(jCas, Sentence.class)));
        final HashMap<Sentence, Collection<Token>> tokenMap = new HashMap<>(JCasUtil.indexCovered(jCas, Sentence.class, Token.class));
        final HashMap<Sentence, Collection<Dependency>> dependencyMap = new HashMap<>(
                JCasUtil.indexCovered(jCas, Sentence.class, Dependency.class)
        );

        for (Sentence sentence : sentences) {
            if (!sentenceIsValid(sentence, tokenMap)) continue;

            final Collection<Dependency> dependencies = dependencyMap.get(sentence);
            if (dependencies == null || dependencies.size() < 2) {
                getLogger().debug(String.format("Skipping due to dependencies: %s", dependencies));
                continue;
            }

            documentDataPoint.add(processDependencies(new ArrayList<>(dependencyMap.get(sentence))));
        }
    }

    private boolean sentenceIsValid(Sentence sentence, HashMap<Sentence, Collection<Token>> tokenMap) {
        if (!tokenMap.containsKey(sentence)) {
            getLogger().debug(String.format("Sentence not in tokenMap: '%s'", sentence.toString()));
            return false;
        }

        final Collection<Token> tokens = tokenMap.get(sentence);
        if (tokens == null) {
            getLogger().debug(String.format("Tokens are null for sentence: '%s'", sentence.toString()));
            return false;
        }

        if (tokens.size() < 3) {
            getLogger().debug(String.format("Sentence too short: '%s'", sentence.toString()));
            return false;
        }
        return true;
    }

    private SentenceDataPoint processDependencies(final ArrayList<Dependency> dependencies) {
        dependencies.sort(Comparator.comparingInt(o -> o.getDependent().getBegin()));
        ArrayList<Token> tokens = dependencies
                .stream()
                .flatMap(d -> Stream.of(d.getGovernor(), d.getDependent()))
                .distinct()
                .sorted(Comparator.comparingInt(Annotation::getBegin))
                .collect(Collectors.toCollection(ArrayList::new));

        int rootDistance = -1;
        int numberOfSyntacticLinks = 0;
        SentenceDataPoint sentenceDataPoint = createSentenceDataPoint();
        for (Dependency dependency : dependencies) {
            numberOfSyntacticLinks++;
            String dependencyType = dependency.getDependencyType();
            if (dependency instanceof PUNCT || dependencyType.equalsIgnoreCase("PUNCT")) {
                continue;
            }
            Token governor = dependency.getGovernor();
            Token dependent = dependency.getDependent();
            if (dependency instanceof ROOT || governor == dependent || dependencyType.equalsIgnoreCase("ROOT")) {
                rootDistance = numberOfSyntacticLinks;
                continue;
            }

            int dist = Math.abs(tokens.indexOf(governor) - tokens.indexOf(dependent));

            sentenceDataPoint.add(dist);
        }
        sentenceDataPoint.rootDistance = rootDistance;
        sentenceDataPoint.numberOfSyntacticLinks = numberOfSyntacticLinks;
        return sentenceDataPoint;
    }

    protected SentenceDataPoint createSentenceDataPoint() {
        return new SentenceDataPoint();
    }

    protected static void save(DocumentDataPoint dataPoints, OutputStream outputStream) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            String json = new Gson().toJson(dataPoints);
            writer.write(json);
        }
    }

    protected static boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    protected Matcher anyMatch(String... strings) {
        for (Pattern pattern : allPatterns) {
            for (String s : strings) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    return matcher;
                }
            }
        }
        return null;
    }

    protected String getDateYear(DocumentDataPoint documentDataPoint) throws AnalysisEngineProcessException {
        String dateYear = documentDataPoint.getDocumentAnnotation().getOrDefault("dateYear", "0000");
        if (pFixDateYear) {
            try {
                dateYear = fixDateYear(documentDataPoint.getDocumentAnnotation(), documentDataPoint.getDocumentMetaData());
                documentDataPoint.getDocumentAnnotation().put("dateYear", dateYear);
                return dateYear;
            } catch (NumberFormatException e) {
                getLogger().error(String.format("Could not parse dateYear '%s': %s", dateYear, e.getMessage()));
                if (pFailOnError) throw new AnalysisEngineProcessException(e);
            }
        }
        return dateYear;
    }

    protected String fixDateYear(Map<String, String> documentAnnotation, Map<String, String> documentMetaData) {
        String dateDay = documentAnnotation.get("dateDay");
        String dateMonth = documentAnnotation.get("dateMonth");
        String dateYear = documentAnnotation.get("dateYear");

        String documentTitle = documentMetaData.get("documentTitle");
        String documentId = documentMetaData.get("documentId");
        String subtitle = documentAnnotation.getOrDefault("subtitle", "");
        try {
            if (!checkDateYear(dateYear)) {
                Matcher mtch = anyMatch(documentTitle, documentId, subtitle);
                if (Objects.nonNull(mtch)) {
                    dateDay = mtch.group(1);
                    dateMonth = mtch.group(2);
                    dateYear = mtch.group(3);
                } else {
                    return dateYear;
                }

                if (!checkDateYear(dateYear)) {
                    throw new Exception(String.format("Year %s is not valid from match: %s", dateYear, mtch));
                }
            }

            String timestamp = "";
            if (isNumeric(dateMonth)) {
                SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss", Locale.GERMANY);
                Date date = df.parse(String.format("%s.%s.%s 00:00:00", dateDay, dateMonth, dateYear));

                timestamp = String.valueOf(date.getTime());
            } else {
                SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss", Locale.GERMAN);
                Date date = df.parse(String.format("%s %s %s 00:00:0", dateDay, dateMonth, dateYear));

                Calendar calendar = df.getCalendar();
                calendar.setTime(date);
                dateMonth = String.valueOf(calendar.get(Calendar.MONTH));

                timestamp = String.valueOf(date.getTime());
            }

            documentAnnotation.put("dateDay", dateDay);
            documentAnnotation.put("dateMonth", dateMonth);
            documentAnnotation.put("dateYear", dateYear);
            documentAnnotation.put("timestamp", timestamp);

            return dateYear;
        } catch (Exception e) {
            return dateYear;
        }
    }

    protected boolean checkDateYear(String dateYear) {
        return (
                isNumeric(dateYear) &&
                        (this.pFixDateYearValidFrom <= Integer.parseInt(dateYear)) &&
                        (Integer.parseInt(dateYear) <= this.pFixDateYearValidTo)
        );
    }
}

class DocumentDataPoint {

    protected final TreeMap<String, String> documentAnnotation;
    protected final TreeMap<String, String> documentMetaData;
    protected final ArrayList<SentenceDataPoint> sentences;

    public DocumentDataPoint(DocumentAnnotation documentAnnotation, DocumentMetaData documentMetaData) {
        this.documentAnnotation = featureMap(documentAnnotation);
        this.documentMetaData = featureMap(documentMetaData);
        this.sentences = new ArrayList<>();
    }

    public static TreeMap<String, String> featureMap(AnnotationBase annotation) {
        TreeMap<String, String> map = new TreeMap<>();
        for (Feature feature : annotation.getType().getFeatures()) {
            try {
                String featureValueAsString = annotation.getFeatureValueAsString(feature);
                if (Objects.nonNull(featureValueAsString)) map.put(feature.getShortName(), featureValueAsString);
            } catch (CASRuntimeException ignored) {}
        }
        return map;
    }

    public static DocumentDataPoint fromJCas(JCas jCas) {
        DocumentAnnotation documentAnnotation = JCasUtil.selectSingle(jCas, DocumentAnnotation.class);
        DocumentMetaData documentMetaData = DocumentMetaData.get(jCas);
        return new DocumentDataPoint(documentAnnotation, documentMetaData);
    }

    public void add(SentenceDataPoint sentenceDataPoint) {
        this.sentences.add(sentenceDataPoint);
    }

    public String getMetaHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            this.documentMetaData.forEach((k, v) -> digest.update(String.join(":", k, v).getBytes(StandardCharsets.UTF_8)));
            this.documentAnnotation.forEach((k, v) -> digest.update(String.join(":", k, v).getBytes(StandardCharsets.UTF_8)));
            String metaHash = Hex.encodeHexString(digest.digest());
            return metaHash;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getDocumentAnnotation() {
        return this.documentAnnotation;
    }

    public Map<String, String> getDocumentMetaData() {
        return this.documentMetaData;
    }

    public List<SentenceDataPoint> getSentences() {
        return this.sentences;
    }
}


class EdgeDataPoint extends SentenceDataPoint {

    protected final ArrayList<Integer> dependencyDistances;

    public EdgeDataPoint() {
        this.dependencyDistances = new ArrayList<>();
    }

    public void add(int distance) {
        this.dependencyDistances.add(distance);
    }

    public int getDependencyDistanceSum() {
        return this.dependencyDistances.stream().reduce(0, (a, b) -> a + b);
    }

    public int getSentenceLength() {
        return this.dependencyDistances.size();
    }

    public final List<Integer> getDependencyDistances() {
        return this.dependencyDistances;
    }
}

class SentenceDataPoint {

    public int rootDistance = -1;
    public int numberOfSyntacticLinks = 0;
    private int dependencyDistanceSum = 0;
    private int sentenceLength = 0;

    public void add(int distance) {
        this.dependencyDistanceSum += distance;
        this.sentenceLength++;
    }

    public double mdd() {
        return ((double) this.getDependencyDistanceSum() / (double) this.getSentenceLength());
    }

    public int getRootDistance() {
        return rootDistance;
    }

    public int getNumberOfSyntacticLinks() {
        return numberOfSyntacticLinks;
    }

    public int getDependencyDistanceSum() {
        return dependencyDistanceSum;
    }

    public int getSentenceLength() {
        return sentenceLength;
    }
}
