package org.texttechnologylab.parliament.helper;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.io.xmi.XmiWriter;
import org.json.JSONObject;
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.annotation.DocumentModification;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Merger {


    static String globalOutput = "/storage/projects/abrami/GerParCor/xmi/";

    public static void main(String[] args){

        try {
            oberoestereich_AT("/storage/projects/abrami/GerParCor/txt/Oberoestereich/", "Austria/Oberoestereich/");
//            vorarlbergAT_new("/storage/projects/abrami/GerParCor/txt/Austria/Vorarlberg_test4/", "Austria/Vorarlberg/");
//            vorarlbergAT("/storage/projects/abrami/GerParCor/txt/Austria/Vorarlberg/", "Austria/Vorarlberg/");
//            bundesratAT("/storage/projects/abrami/GerParCor/txt/Austria/Bundesrat/", "Austria/Bundesrat/");
//            nationalratAT("/storage/projects/abrami/GerParCor/txt/Austria/Nationalrat/", "Austria/Nationalrat/");
//            kaerntenAT("/storage/projects/abrami/GerParCor/txt/Austria/Kaernten/", "Austria/Kaernten/");
//            niederAT("/storage/projects/abrami/GerParCor/txt/Austria/Niederoestereich/", "Austria/Niederoestereich/");
//            salzburgAT("/storage/projects/abrami/GerParCor/txt/Austria/Salzburg/", "Austria/Salzburg/");
//            steiermarkAT("/storage/projects/abrami/GerParCor/txt/Austria/Steiermark/", "Austria/Steiermark/");
//            wienAT("/storage/projects/abrami/GerParCor/txt/Austria/Wien/", "Austria/Wien/");
//            tirolAT("/storage/projects/abrami/GerParCor/txt/Austria/Tirol/", "Austria/Tirol/");
//            baWueDE("/storage/projects/abrami/GerParCor/txt/Germany/BadenWuertemmberg/","Germany/BadenWuertemmberg/");
//            brandenburg_DE("/storage/projects/abrami/GerParCor/txt/Germany/Brandenburg/", "Germany/Brandenburg/");
//            berlin_DE("/storage/projects/abrami/GerParCor/txt/Germany/Berlin/", "/storage/projects/abrami/GerParCor/dates/Berlin/", "Germany/Berlin/");
//            bayern_DE("/storage/projects/abrami/GerParCor/txt/Germany/Bayern/","Germany/Bayern/");
//            bundesrat_DE("/storage/projects/abrami/GerParCor/txt/Germany/Bundesrat/","Germany/Bundesrat/");
//            bremen_DE("/storage/projects/abrami/GerParCor/txt/Germany/Bremen/","Germany/Bremen/");
//            hamburgDE("/storage/projects/abrami/GerParCor/txt/Germany/Hamburg/","Germany/Hamburg/");
//            hessen_DE("/storage/projects/abrami/GerParCor/txt/Germany/Hessen/","Germany/Hessen/");
//            meckPomDE("/storage/projects/abrami/GerParCor/txt/Germany/MeckPom/","Germany/MeckPom/");
//            niedersachsen_DE("/storage/projects/abrami/GerParCor/txt/Germany/Niedersachsen/","Germany/Niedersachsen/");
//            nrw_DE("/storage/projects/abrami/GerParCor/txt/Germany/NordrheinWestfahlen/","Germany/NordrheinWestfahlen/");
//            rlp_DE("/storage/projects/abrami/GerParCor/txt/Germany/RheinlandPfalz/","Germany/RheinlandPfalz/");
//            saarlandDE("/storage/projects/abrami/GerParCor/txt/Germany/Saarland/","Germany/Saarland/");
//            sachsen_anhalt_DE("/storage/projects/abrami/GerParCor/txt/Germany/SachsenAnhalt/","Germany/SachsenAnhalt/");
//            schleswigholsteinDE("/storage/projects/abrami/GerParCor/txt/Germany/SchleswigHolstein/","Germany/SchleswigHolstein/");
//            sachsen_DE("/storage/projects/abrami/GerParCor/txt/Germany/Sachsen/","Germany/Sachsen/");
//            thueringenDE("/storage/projects/abrami/GerParCor/txt/Germany/Thueringen/","Germany/Thueringen/");
//            baWueDE_older("/storage/projects/abrami/GerParCor/txt/Germany/BadenWuertemmberg/","Germany/BadenWuertemmberg/");
//            lichtenstein("/storage/projects/abrami/GerParCor/txt/Liechtenstein/","Liechtenstein/");


        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (UIMAException e) {
            throw new RuntimeException(e);
        }

    }

    public static AnalysisEngine nlp(String sOutput) throws ResourceInitializationException {

        AnalysisEngine pAE = null;
        AggregateBuilder pipeline = new AggregateBuilder();
        pipeline.add(createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, sOutput,
                XmiWriter.PARAM_PRETTY_PRINT, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1",
                XmiWriter.PARAM_COMPRESSION, "GZIP"
        ));

        // create an AnalysisEngine for running the Pipeline.
        pAE = pipeline.createAggregate();
        return pAE;
    }


    public static void bundesratAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sSplit[] = sFileName.split("_");
            String sID = sSplit[0];
            String sDate = sSplit[1];

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle("Bundesrat Plenarprotokoll "+sID+" vom "+sDate);
                dmd.setDocumentId(sID+"_"+sDate);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sDate);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID+". Sitzung");
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }


    public static void nationalratAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sSplit[] = sFileName.split("_");
            String sID = sSplit[0];
            String sDate = sSplit[1];

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle("Nationalrat Plenarprotokoll "+sID+" vom "+sDate);
                dmd.setDocumentId(sID+"_"+sDate);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sDate);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID+". Sitzung");
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void kaerntenAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName.replace("Jänner", "Januar");
            sChangeFileName = sChangeFileName.replace("Jaenner", "Januar");
            sChangeFileName = sChangeFileName.replace("Maerz", "März");
            sChangeFileName = sChangeFileName.replace("Apirl", "April");
            sChangeFileName = sChangeFileName.replace("Julii", "Juli");
            sChangeFileName = sChangeFileName.replace("Feber", "Februar");
            sChangeFileName = sChangeFileName.replace("28.Mai 2009", "28. Mai 2009");
            sChangeFileName = sChangeFileName.replace("16., 17. und 18. Dezember 2015", "16. Dezember 2015");
            sChangeFileName = sChangeFileName.replace("26.November 2020 Budget 2021", "26. November 2020 Budget 2021");
            sChangeFileName = sChangeFileName.replace("vom 1. Dezember", "am 1. Dezember");
            sChangeFileName = sChangeFileName.replace("1. Dezember bis 3. Dezember 1999", "1. Dezember 1999");

            sChangeFileName = sChangeFileName.replace("29. November bis 2. Dezember 1994", "29. November 1994");
            sChangeFileName = sChangeFileName.replace("20., 21. und 22. Dezember 2016", "29. Dezember 2016");
            sChangeFileName = sChangeFileName.replace("12., 13. und 14. Dezember 2018", "12. Dezember 2018");
            sChangeFileName = sChangeFileName.replace("18., 19 und 20. Dezember 2014", "18. Dezember 2014");
            sChangeFileName = sChangeFileName.replace("28. November bis 29. November 1995", "28. November 1995");
            sChangeFileName = sChangeFileName.replace("11., 12. und 13. Dezember 2013", "11. Dezember 2013");
            sChangeFileName = sChangeFileName.replace("22.,23. u. 24. Juli 2010", "22. Juli 2010");
            sChangeFileName = sChangeFileName.replace("vom 12. bis 14. Dezember 2007", "am 12. Dezember 2007");
            sChangeFileName = sChangeFileName.replace("bis", "und");


            String sSplit[] = sChangeFileName.split(" ");
            String sID = sSplit[0];
            String sName = sSplit[1];

            String sSplit2[] = sChangeFileName.split("am ");
            String sDate = "";
            if(sSplit2.length>1){
                sDate = sSplit2[1];
            }
            else{
                sDate = sSplit2[0];
                sDate = sDate.substring(sDate.indexOf("LTG-Sitzung ")+12);
                String sDateSplitNew[] = sDate.split(" ");
                if(sDateSplitNew.length>3){
                    sDate = sDate.substring(0, sDate.indexOf(" "+sDateSplitNew[3]));
                }

                sDate = sDate.replace("Julii", "Juli");

            }


            if(sDate.contains("und")){
                String sDatumSplit[] = sDate.split(" und ");
                System.out.println(sDate);
                sDate = sDatumSplit[0];
                if(sDate.split(" ").length<3){
                    String sOld = sDate;
                    sDate = sOld + sDatumSplit[1].substring(sDate.length());
                    System.out.println(sDate);
                }
            }

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                sDate = sDate.replace("Julii", "Juli");
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sFileName);
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void wienAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName.replace("Jänner", "Januar");
            sChangeFileName = sChangeFileName.replace("Jaenner", "Januar");
            sChangeFileName = sChangeFileName.replace("Maerz", "März");
            sChangeFileName = sChangeFileName.replace("Apirl", "April");
            sChangeFileName = sChangeFileName.replace("Julii", "Juli");
            sChangeFileName = sChangeFileName.replace("Feber", "Februar");
            sChangeFileName = sChangeFileName.replace("28.Mai 2009", "28. Mai 2009");
            sChangeFileName = sChangeFileName.replace("16., 17. und 18. Dezember 2015", "16. Dezember 2015");
            sChangeFileName = sChangeFileName.replace("26.November 2020 Budget 2021", "26. November 2020 Budget 2021");
            sChangeFileName = sChangeFileName.replace("vom 1. Dezember", "am 1. Dezember");
            sChangeFileName = sChangeFileName.replace("1. Dezember bis 3. Dezember 1999", "1. Dezember 1999");

            sChangeFileName = sChangeFileName.replace("29. November bis 2. Dezember 1994", "29. November 1994");
            sChangeFileName = sChangeFileName.replace("20., 21. und 22. Dezember 2016", "29. Dezember 2016");
            sChangeFileName = sChangeFileName.replace("12., 13. und 14. Dezember 2018", "12. Dezember 2018");
            sChangeFileName = sChangeFileName.replace("18., 19 und 20. Dezember 2014", "18. Dezember 2014");
            sChangeFileName = sChangeFileName.replace("28. November bis 29. November 1995", "28. November 1995");
            sChangeFileName = sChangeFileName.replace("11., 12. und 13. Dezember 2013", "11. Dezember 2013");
            sChangeFileName = sChangeFileName.replace("22.,23. u. 24. Juli 2010", "22. Juli 2010");
            sChangeFileName = sChangeFileName.replace("vom 12. bis 14. Dezember 2007", "am 12. Dezember 2007");
            sChangeFileName = sChangeFileName.replace("am 1. Dezember 1957", "vom 1. Dezember 1957");
            sChangeFileName = sChangeFileName.replace("am 28. März 2018_Sitzungsbericht", "vom 28. März 2018");
            sChangeFileName = sChangeFileName.replace("5. bis 7. Juli 2023_Kurzprotokoll - Anlagen", "5. Juli 2023");
            sChangeFileName = sChangeFileName.replace("Kurzprotokoll - Anlagen", "");
            sChangeFileName = sChangeFileName.replace("_Kurzprotokoll - Anlagen", "");
            sChangeFileName = sChangeFileName.replace("6. bis 8. Juli 2022", "6. Juli 2022");
            sChangeFileName = sChangeFileName.replace("14. bis 16. Oktober 2020", "14. Oktober 2020");
            sChangeFileName = sChangeFileName.replace("am 27. Mai 1957", "vom 27. Mai 1957");
            sChangeFileName = sChangeFileName.replace("am 27. Mai 1957", "vom 27. Mai 1957");
            sChangeFileName = sChangeFileName.replace("am 1. Dezember 1921", "vom 1. Dezember 1921");
            sChangeFileName = sChangeFileName.replace("18. bis 20. Mai 2022", "18. Mai 2022");
            sChangeFileName = sChangeFileName.replace("21. November 1963", "vom 21. November 1963");
            sChangeFileName = sChangeFileName.replace("am 5. Mai 1999", "vom 5. Mai 1999");
            sChangeFileName = sChangeFileName.replace("von 13. Dezember 1985", "vom 13. Dezember 1985");
            sChangeFileName = sChangeFileName.replace("16._17. November 2022", "16. November 2022");
            sChangeFileName = sChangeFileName.replace("15._16. Dezember 2021", "15. Dezember 2021");
            sChangeFileName = sChangeFileName.replace("am 11. Oktober 2007", "vom 11. Oktober 2007");
            sChangeFileName = sChangeFileName.replace("29.10.2009", "29. Oktober 2009");
            sChangeFileName = sChangeFileName.replace("9. und 10. Februar 2022", "9. Februar 2022");
            sChangeFileName = sChangeFileName.replace("am 9. Dezember 1998", "vom 9. Dezember 1998");
            sChangeFileName = sChangeFileName.replace("24.März 1988", "24. März 1988");
            sChangeFileName = sChangeFileName.replace("am 10. Oktober 2007", "vom 10. Oktober 2007");
            sChangeFileName = sChangeFileName.replace("16. bis 18. Dezember 2020", "16. Dezember 2020");
            sChangeFileName = sChangeFileName.replace("15. Dezember 1976", "vom 15. Dezember 1976");
            sChangeFileName = sChangeFileName.replace("von 11. Dezember 1985", "vom 11. Dezember 1985");
            sChangeFileName = sChangeFileName.replace("am 4. Oktober 2001", "vom 4. Oktober 2001");
            sChangeFileName = sChangeFileName.replace("am 3. Oktober 2001", "vom 3. Oktober 2001");
            sChangeFileName = sChangeFileName.replace("14. Dezember 1976", "vom 14. Dezember 1976");
            sChangeFileName = sChangeFileName.replace("13. Dezember 1976", "vom 13. Dezember 1976");
            sChangeFileName = sChangeFileName.replace("30.11.2011", "30. November 2011");
            sChangeFileName = sChangeFileName.replace("von", "vom");
            sChangeFileName = sChangeFileName.replace("am", "vom");
            sChangeFileName = sChangeFileName.replace("Okober", "Oktober");

            sChangeFileName = sChangeFileName.replace("am 28. März 2018_Kurzprotokoll", "vom 28. März 2018");
            sChangeFileName = sChangeFileName.contains("1. Dezember 1960") ? "vom 1. Dezember 1960" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("14. September 2023") ? "vom 14. September 2023" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("am 24. Juni 2022") ? "vom 24. Juni 2022" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("13. Juli 1963") ? "vom 13. Juli 1963" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("21._22. Oktober 2021") ? "vom 21. Oktober 2021" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("16. bis 18. November 2022") ? "vom 16. November 2022" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("10. bis 12. Mai 2023") ? "vom 10. Mai 2023" : sChangeFileName;

//            if(sChangeFileName.contains("1. Dezember 1921")){
//                System.out.println("stop");
//            }

            String sSplit[] = sChangeFileName.split(" ");
            String sID = sSplit[0];
            String sName = sSplit[1];

            String sSplit2[] = sChangeFileName.split("vom ");
            String sDate = "";
            if(sSplit2.length>1){
                sDate = sSplit2[sSplit2.length-1];
            }
            else{
                sDate = sSplit2[0];
                sDate = sDate.substring(sDate.indexOf("Kurzprotokoll vom ")+20);
                String sDateSplitNew[] = sDate.split(" ");
                if(sDateSplitNew.length>3){
                    sDate = sDate.substring(0, sDate.indexOf(" "+sDateSplitNew[3]));
                }
            }


            if(sDate.contains("und")){
                String sDatumSplit[] = sDate.split(" und ");
                System.out.println(sDate);
                sDate = sDatumSplit[0];
                if(sDate.split(" ").length<3){
                    String sOld = sDate;
                    sDate = sOld + sDatumSplit[1].substring(sDate.length());
                    System.out.println(sDate);
                }
            }

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                sDate = sDate.replace("Julii", "Juli");
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sFileName);
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void vorarlbergAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        fSet = fSet.stream().filter(f->f.getName().contains("#1")).collect(Collectors.toSet());

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfBackup = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            System.out.println(sFileName);
            sFileName = sFileName.replace(".txt", "");
            if(sFileName.contains(" - ")) {
                sFileName = sFileName.substring(0, sFileName.indexOf(" - "));
            }
            else{
                sFileName = sFileName.substring(0, sFileName.indexOf("#"));
            }
            String sChangeFileName = sFileName.replace("Jänner", "Januar");
            sChangeFileName = sChangeFileName.replace("Jaenner", "Januar");
            sChangeFileName = sChangeFileName.replace("Maerz", "März");
            sChangeFileName = sChangeFileName.replace("Apirl", "April");
            sChangeFileName = sChangeFileName.replace("Julii", "Juli");
            sChangeFileName = sChangeFileName.replace("Feber", "Februar");
            sChangeFileName = sChangeFileName.replace("Okober", "Oktober");
            sChangeFileName = sChangeFileName.replace("von", "vom");
            sChangeFileName = sChangeFileName.replace("am", "vom");

//            sChangeFileName = sChangeFileName.replace("am 28. März 2018_Kurzprotokoll", "vom 28. März 2018");
//            sChangeFileName = sChangeFileName.contains("1. Dezember 1960") ? "vom 1. Dezember 1960" : sChangeFileName;
//            sChangeFileName = sChangeFileName.contains("14. September 2023") ? "vom 14. September 2023" : sChangeFileName;
//            sChangeFileName = sChangeFileName.contains("am 24. Juni 2022") ? "vom 24. Juni 2022" : sChangeFileName;
//            sChangeFileName = sChangeFileName.contains("13. Juli 1963") ? "vom 13. Juli 1963" : sChangeFileName;
//            sChangeFileName = sChangeFileName.contains("21._22. Oktober 2021") ? "vom 21. Oktober 2021" : sChangeFileName;
//            sChangeFileName = sChangeFileName.contains("16. bis 18. November 2022") ? "vom 16. November 2022" : sChangeFileName;
//            sChangeFileName = sChangeFileName.contains("10. bis 12. Mai 2023") ? "vom 10. Mai 2023" : sChangeFileName;

//            if(sChangeFileName.contains("1. Dezember 1921")){
//                System.out.println("stop");
//            }

            String sSplit[] = sChangeFileName.split(" ");
            String sID = sSplit[0];
            String sName = sSplit[1];

            String sSplit2[] = sChangeFileName.split("vom ");
            String sDate = "";
            if(sSplit2.length>1){
                sDate = sSplit2[sSplit2.length-1];
            }
            else{
                sDate = sSplit2[0];
                sDate = sDate.substring(sDate.indexOf("Kurzprotokoll vom ")+20);
                String sDateSplitNew[] = sDate.split(" ");
                if(sDateSplitNew.length>3){
                    sDate = sDate.substring(0, sDate.indexOf(" "+sDateSplitNew[3]));
                }
            }


            if(sDate.contains("und")){
                String sDatumSplit[] = sDate.split(" und ");
                System.out.println(sDate);
                sDate = sDatumSplit[0];
                if(sDate.split(" ").length<3){
                    String sOld = sDate;
                    sDate = sOld + sDatumSplit[1].substring(sDate.length());
                    System.out.println(sDate);
                }
            }

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                sDate = sDate.replace("Julii", "Juli");
                Date pDate = null;

                try {
                    pDate = sdf.parse(sDate);
                } catch (ParseException pe){

                    pDate = sdfBackup.parse(sDate);

                }


                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sFileName);
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void vorarlbergAT_new(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

//        fSet = fSet.stream().filter(f->f.getName().contains("#1")).collect(Collectors.toSet());

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        SimpleDateFormat sdfShort = new SimpleDateFormat("d.M.yyyy", Locale.GERMAN);
        SimpleDateFormat sdfOCR = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            System.out.println(sFileName);
            sFileName = sFileName.replace(".txt", "");

            sFileName = sFileName.contains("09##13. und 14.12.2006##Komplette_Sitzung") ? "09##13.12.2006##Komplette_Sitzung" : sFileName;

            String sSplit[] = sFileName.split("##");
            String sID = sSplit[0];

            try {
                int iCheck = Integer.valueOf(sID);
            }
            catch (Exception e){
                sID = "00";
            }

            String sDatum = sSplit[1];

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.substring(0, subPath.indexOf("/"));

            Date pDate = null;

            try {
                pDate = sdf.parse(sDatum);
            } catch (ParseException pe){
                System.out.println(pe.getMessage());
            }
            if(pDate==null){
                String sDescription = sSplit[2];
                try {
                    if (sDescription.contains("6. Sitzung des XXI. Vorarlberger")) {
                        pDate = sdf.parse("12.07.1972");
                    }
                    if (sDescription.contains("am 2. und 3. Juli 1980")) {
                        pDate = sdf.parse("02.07.1980");
                    }

                    if (sDescription.contains("OCR")) {
                        String[] dSplit = sDescription.split(" ");
                        pDate = sdfOCR.parse(dSplit[1]);
                    }


                    if(sDescription.split(" ").length==2){
                        String tDateSplit = sDescription.split(" ")[0];
                        try {
                            pDate = sdfOCR.parse(tDateSplit);

                        }
                        catch (Exception e){

                        }
                    }

                    if(pDate==null) {
                        String sNewDate = sDescription.contains("14. und 15. Oktober 1970") ? "14.10.1970" : sDescription;
                        sNewDate = sDescription.contains("16. und 17. Dezember 1970") ? "16.12.1970" : sNewDate;
                        sNewDate = sDescription.contains("15. und 16. Dezember 1971") ? "15.12.1971" : sNewDate;
                        sNewDate = sDescription.contains("7. und 8. Juli 1982") ? "07.07.1982" : sNewDate;
                        sNewDate = sDescription.contains("12., 13. und 14. Dezember 1972") ? "12.12.1972" : sNewDate;
                        sNewDate = sDescription.contains("17. und 18 Juli 1974") ? "17.07.1974" : sNewDate;
                        sNewDate = sDescription.contains("28. Februar und 1. März 1973") ? "28.02.1973" : sNewDate;
                        sNewDate = sDescription.contains("17. und 18. Oktober 1972") ? "17.10.1972" : sNewDate;
                        sNewDate = sDescription.contains("8. und 9. Juli 1981") ? "08.07.1981" : sNewDate;
                        sNewDate = sDescription.contains("fromDocFile-4B30B6CA45AAC7166525714600350EBF") ? "10.12.2003" : sNewDate;
                        sNewDate = sDescription.contains("7. und 9. Dezember 1983") ? "07.12.1983" : sNewDate;
                        sNewDate = sDescription.contains("11. und 12. Juli 1984") ? "11.07.1984" : sNewDate;
                        sNewDate = sDescription.contains("19690716") ? "16.07.1969" : sNewDate;
                        sNewDate = sDescription.contains("19641221") ? "21.12.1964" : sNewDate;
                        sNewDate = sDescription.contains("19661214") ? "14.12.1966" : sNewDate;
                        sNewDate = sDescription.contains("19681211") ? "11.12.1968" : sNewDate;
                        sNewDate = sDescription.contains("19690129") ? "29.01.1969" : sNewDate;
                        sNewDate = sDescription.contains("19651209") ? "09.12.1965" : sNewDate;
                        sNewDate = sDescription.contains("19661123") ? "23.11.1966" : sNewDate;
                        sNewDate = sDescription.contains("19. und 20 Juni 1974") ? "19.06.1974" : sNewDate;
                        sNewDate = sDescription.contains("18. und 19.12.2019") ? "18.12.2019" : sNewDate;
                        sNewDate = sDescription.contains("2., 3. und 4. Dezember 1981") ? "02.12.1981" : sNewDate;
                        sNewDate = sDescription.contains("3., 4. und 5. Dezember 1980") ? "03.12.1988" : sNewDate;
                        sNewDate = sDescription.contains("17. und 18. November 1971") ? "17.11.1971" : sNewDate;
                        sNewDate = sDescription.contains("12. und 13. Dezember 1979") ? "12.12.1979" : sNewDate;
                        sNewDate = sDescription.contains("6. und 7. Juli 1983") ? "06.07.1983" : sNewDate;
                        sNewDate = sDescription.contains("12. und 13. Dezember 1973") ? "12.12.1973" : sNewDate;
                        sNewDate = sDescription.contains("17. und 18. Dezember 1969") ? "17.12.1969" : sNewDate;
                        sNewDate = sDescription.contains("17. und 18. Oktober 1973") ? "17.10.1973" : sNewDate;
                        sNewDate = sDescription.contains("17. und 18. Oktober 1973") ? "17.10.1973" : sNewDate;
                        sNewDate = sDescription.contains("9. und 10. Dezember 1982") ? "09.12.1982" : sNewDate;
                        sNewDate = sDescription.contains("29. und 30 Mai 1974") ? "19.05.1974" : sNewDate;
                        sNewDate = sDescription.contains("0325_22_Landesregierung_06_Sitzung_2020_DRUCK") ? "08.07.2020" : sNewDate;

                        if (pDate == null) {
                            pDate = sdf.parse(sNewDate);
                        }
                    }

                }
                catch (ParseException pe1){
                    System.out.println(pe1.getMessage());
                }

                if(pDate==null){
                    String[] sLines = sContent.split("\n");

                    String sNewDatum= "";
                    for (String sLine : sLines) {
                        if(sLine.contains("LT-Sitzung")){
                            System.out.println(sLine);
                            sNewDatum = sLine.split("vom ")[1];
                        }

                        try {
                            pDate = sdfShort.parse(sNewDatum);
                            if(pDate!=null){
                                break;
                            }
                        } catch (ParseException e) {
                        }

                    }

                }

            }

            DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
            dmd.setDocumentTitle(sID+"_"+sdf.format(pDate));
            dmd.setDocumentId(sID+"_"+sdf.format(pDate));
            dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+"/"+sdfYear.format(pDate)+"/"+sID+"_"+sdf.format(pDate));
            dmd.setDocumentBaseUri(globalOutput);

            DocumentModification dm1 = new DocumentModification(emptyCas);
            dm1.setUser("bagci");
            FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
            dm1.setTimestamp(ft.toMillis());
            dm1.setComment("Download");
            dm1.addToIndexes();

            DocumentModification dm2 = new DocumentModification(emptyCas);
            dm2.setUser("abrami");
            dm2.setTimestamp(ft.toMillis());
            dm2.setComment("Conversion");
            dm2.addToIndexes();

            DocumentAnnotation da = new DocumentAnnotation(emptyCas);
            da.setTimestamp(pDate.getTime());
            da.setDateDay(pDate.getDay());
            da.setDateMonth(pDate.getMonth());
            da.setDateYear(pDate.getYear());
            da.setSubtitle(Integer.valueOf(sID)+". Sitzung vom "+sdf.format(pDate));
            da.addToIndexes();

            SimplePipeline.runPipeline(emptyCas, nlp);


        }


    }    public static void


    tirolAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName.replace("Jänner", "Januar");
            sChangeFileName = sChangeFileName.replace("Jaenner", "Januar");
            sChangeFileName = sChangeFileName.replace("Maerz", "März");
            sChangeFileName = sChangeFileName.replace("Apirl", "April");
            sChangeFileName = sChangeFileName.replace("Julii", "Juli");
            sChangeFileName = sChangeFileName.replace("Feber", "Februar");
            sChangeFileName = sChangeFileName.replace("28.Mai 2009", "28. Mai 2009");
            sChangeFileName = sChangeFileName.replace("16., 17. und 18. Dezember 2015", "16. Dezember 2015");
            sChangeFileName = sChangeFileName.replace("26.November 2020 Budget 2021", "26. November 2020 Budget 2021");
            sChangeFileName = sChangeFileName.replace("vom 1. Dezember", "am 1. Dezember");
            sChangeFileName = sChangeFileName.replace("1. Dezember bis 3. Dezember 1999", "1. Dezember 1999");

            sChangeFileName = sChangeFileName.replace("29. November bis 2. Dezember 1994", "29. November 1994");
            sChangeFileName = sChangeFileName.replace("20., 21. und 22. Dezember 2016", "29. Dezember 2016");
            sChangeFileName = sChangeFileName.replace("12., 13. und 14. Dezember 2018", "12. Dezember 2018");
            sChangeFileName = sChangeFileName.replace("18., 19 und 20. Dezember 2014", "18. Dezember 2014");
            sChangeFileName = sChangeFileName.replace("28. November bis 29. November 1995", "28. November 1995");
            sChangeFileName = sChangeFileName.replace("11., 12. und 13. Dezember 2013", "11. Dezember 2013");
            sChangeFileName = sChangeFileName.replace("22.,23. u. 24. Juli 2010", "22. Juli 2010");
            sChangeFileName = sChangeFileName.replace("vom 12. bis 14. Dezember 2007", "am 12. Dezember 2007");
            sChangeFileName = sChangeFileName.replace("am 1. Dezember 1957", "vom 1. Dezember 1957");
            sChangeFileName = sChangeFileName.replace("am 28. März 2018_Sitzungsbericht", "vom 28. März 2018");
            sChangeFileName = sChangeFileName.replace("5. bis 7. Juli 2023_Kurzprotokoll - Anlagen", "5. Juli 2023");
            sChangeFileName = sChangeFileName.replace("Kurzprotokoll - Anlagen", "");
            sChangeFileName = sChangeFileName.replace("_Kurzprotokoll - Anlagen", "");
            sChangeFileName = sChangeFileName.replace("6. bis 8. Juli 2022", "6. Juli 2022");
            sChangeFileName = sChangeFileName.replace("14. bis 16. Oktober 2020", "14. Oktober 2020");
            sChangeFileName = sChangeFileName.replace("am 27. Mai 1957", "vom 27. Mai 1957");
            sChangeFileName = sChangeFileName.replace("am 27. Mai 1957", "vom 27. Mai 1957");
            sChangeFileName = sChangeFileName.replace("am 1. Dezember 1921", "vom 1. Dezember 1921");
            sChangeFileName = sChangeFileName.replace("18. bis 20. Mai 2022", "18. Mai 2022");
            sChangeFileName = sChangeFileName.replace("21. November 1963", "vom 21. November 1963");
            sChangeFileName = sChangeFileName.replace("am 5. Mai 1999", "vom 5. Mai 1999");
            sChangeFileName = sChangeFileName.replace("von 13. Dezember 1985", "vom 13. Dezember 1985");
            sChangeFileName = sChangeFileName.replace("16._17. November 2022", "16. November 2022");
            sChangeFileName = sChangeFileName.replace("15._16. Dezember 2021", "15. Dezember 2021");
            sChangeFileName = sChangeFileName.replace("am 11. Oktober 2007", "vom 11. Oktober 2007");
            sChangeFileName = sChangeFileName.replace("29.10.2009", "29. Oktober 2009");
            sChangeFileName = sChangeFileName.replace("9. und 10. Februar 2022", "9. Februar 2022");
            sChangeFileName = sChangeFileName.replace("am 9. Dezember 1998", "vom 9. Dezember 1998");
            sChangeFileName = sChangeFileName.replace("24.März 1988", "24. März 1988");
            sChangeFileName = sChangeFileName.replace("am 10. Oktober 2007", "vom 10. Oktober 2007");
            sChangeFileName = sChangeFileName.replace("16. bis 18. Dezember 2020", "16. Dezember 2020");
            sChangeFileName = sChangeFileName.replace("15. Dezember 1976", "vom 15. Dezember 1976");
            sChangeFileName = sChangeFileName.replace("von 11. Dezember 1985", "vom 11. Dezember 1985");
            sChangeFileName = sChangeFileName.replace("am 4. Oktober 2001", "vom 4. Oktober 2001");
            sChangeFileName = sChangeFileName.replace("am 3. Oktober 2001", "vom 3. Oktober 2001");
            sChangeFileName = sChangeFileName.replace("14. Dezember 1976", "vom 14. Dezember 1976");
            sChangeFileName = sChangeFileName.replace("13. Dezember 1976", "vom 13. Dezember 1976");
            sChangeFileName = sChangeFileName.replace("30.11.2011", "30. November 2011");
            sChangeFileName = sChangeFileName.replace("von", "vom");
            sChangeFileName = sChangeFileName.replace("am", "vom");
            sChangeFileName = sChangeFileName.replace("Okober", "Oktober");

            sChangeFileName = sChangeFileName.replace("am 28. März 2018_Kurzprotokoll", "vom 28. März 2018");
            sChangeFileName = sChangeFileName.contains("1. Dezember 1960") ? "vom 1. Dezember 1960" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("14. September 2023") ? "vom 14. September 2023" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("am 24. Juni 2022") ? "vom 24. Juni 2022" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("13. Juli 1963") ? "vom 13. Juli 1963" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("21._22. Oktober 2021") ? "vom 21. Oktober 2021" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("16. bis 18. November 2022") ? "vom 16. November 2022" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("10. bis 12. Mai 2023") ? "vom 10. Mai 2023" : sChangeFileName;

//            if(sChangeFileName.contains("1. Dezember 1921")){
//                System.out.println("stop");
//            }

            String sSplit[] = sChangeFileName.split(" ");
            String sID = sSplit[0];
            String sName = sSplit[1];

            String sSplit2[] = sChangeFileName.split("vom ");
            String sDate = "";
            if(sSplit2.length>1){
                sDate = sSplit2[sSplit2.length-1];
            }
            else{
                sDate = sSplit2[0];
                sDate = sDate.substring(sDate.indexOf("Kurzprotokoll vom ")+20);
                String sDateSplitNew[] = sDate.split(" ");
                if(sDateSplitNew.length>3){
                    sDate = sDate.substring(0, sDate.indexOf(" "+sDateSplitNew[3]));
                }
            }


            if(sDate.contains("und")){
                String sDatumSplit[] = sDate.split(" und ");
                System.out.println(sDate);
                sDate = sDatumSplit[0];
                if(sDate.split(" ").length<3){
                    String sOld = sDate;
                    sDate = sOld + sDatumSplit[1].substring(sDate.length());
                    System.out.println(sDate);
                }
            }

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                sDate = sDate.replace("Julii", "Juli");
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sFileName);
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void niederAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName.replace("Jänner", "Januar");
            sChangeFileName = sChangeFileName.replace("Jaenner", "Januar");
            sChangeFileName = sChangeFileName.replace("Maerz", "März");
            sChangeFileName = sChangeFileName.replace("Apirl", "April");
            sChangeFileName = sChangeFileName.replace("Julii", "Juli");
            sChangeFileName = sChangeFileName.replace("Feber", "Februar");

            String sSplit[] = sChangeFileName.split(" ");
            String sID = sSplit[0];
            String sName = sSplit[1];

            String sSplit2[] = sChangeFileName.split("am ");
            String sDate = "";
            if(sSplit2.length>1){
                sDate = sSplit2[1];
            }
            else{
                sDate = sSplit2[0];
            }


//            if(sDate.contains("und")){
//                String sDatumSplit[] = sDate.split(" und ");
//                System.out.println(sDate);
//                sDate = sDatumSplit[0];
//                if(sDate.split(" ").length<3){
//                    String sOld = sDate;
//                    sDate = sOld + sDatumSplit[1].substring(sDate.length());
//                    System.out.println(sDate);
//                }
//            }

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                sDate = sDate.replace("Julii", "Juli");
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sFileName);
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void salzburgAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName.replace("Jänner", "Januar");
            sChangeFileName = sChangeFileName.replace("Jaenner", "Januar");
            sChangeFileName = sChangeFileName.replace("Maerz", "März");
            sChangeFileName = sChangeFileName.replace("Apirl", "April");
            sChangeFileName = sChangeFileName.replace("Julii", "Juli");
            sChangeFileName = sChangeFileName.replace("Feber", "Februar");

            String sSplit[] = sChangeFileName.split(" ");
            String sID = sSplit[0];
//            String sName = sSplit[1];

            String sSplit2[] = sChangeFileName.split("am ");
            String sDate = "";
            if(sSplit2.length>1){
                sDate = sSplit2[1];
            }
            else{
                sDate = sSplit2[0];
            }


//            if(sDate.contains("und")){
//                String sDatumSplit[] = sDate.split(" und ");
//                System.out.println(sDate);
//                sDate = sDatumSplit[0];
//                if(sDate.split(" ").length<3){
//                    String sOld = sDate;
//                    sDate = sOld + sDatumSplit[1].substring(sDate.length());
//                    System.out.println(sDate);
//                }
//            }

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void steiermarkAT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName.replace("Jänner", "Januar");
            sChangeFileName = sChangeFileName.replace("Jaenner", "Januar");
            sChangeFileName = sChangeFileName.replace("Maerz", "März");
            sChangeFileName = sChangeFileName.replace("Apirl", "April");
            sChangeFileName = sChangeFileName.replace("Julii", "Juli");
            sChangeFileName = sChangeFileName.replace("Feber", "Februar");
            sChangeFileName = sChangeFileName.replace("28.10 1869", "28.10.1869");

            String sSplit[] = sChangeFileName.split(" ");
            String sID = sSplit[0];
//            String sName = sSplit[1];

            String sSplit2[] = sChangeFileName.split("vom ");
            String sDate = "";
            if(sSplit2.length>1){
                sDate = sSplit2[1];
            }
            else{
                sDate = sSplit2[0];
            }


//            if(sDate.contains("und")){
//                String sDatumSplit[] = sDate.split(" und ");
//                System.out.println(sDate);
//                sDate = sDatumSplit[0];
//                if(sDate.split(" ").length<3){
//                    String sOld = sDate;
//                    sDate = sOld + sDatumSplit[1].substring(sDate.length());
//                    System.out.println(sDate);
//                }
//            }

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("bagci");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void oberoestereich_AT(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
        SimpleDateFormat sdfBackup = new SimpleDateFormat("dd MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sPath = "";

            if(file.getName().equalsIgnoreCase("out.txt")){
                sPath = file.getAbsolutePath().replace("/"+file.getName(), "");
                sPath = sPath.substring(sPath.lastIndexOf("/")+1);
            }
            else{
                sPath = file.getName();
            }
            System.out.println(file.getAbsolutePath());

            sPath = sPath.replace("Jänner", "Januar");
            sPath = sPath.replace("Jaenner", "Januar");
            sPath = sPath.replace("jaenner", "Januar");
            sPath = sPath.replace("Maerz", "März");
            sPath = sPath.replace("Apirl", "April");
            sPath = sPath.replace("Julii", "Juli");
            sPath = sPath.replace("maerz", "März");
            sPath = sPath.replace("Feber", "Februar");
            sPath = sPath.replace("okt", "Oktober");
            sPath = sPath.replace("Oktoberober", "Oktober");
            sPath = sPath.replace("08_nov_2007", "08_November_2007");
            sPath = sPath.replace("06_nov_2007", "08_November_2008");
            sPath = sPath.replace("_nov_", "_November_");
            sPath = sPath.replace("_feb_", "_Februar_");
            sPath = sPath.replace(" des Oberösterreichischen Landtags am ", "__");
            sPath = sPath.replace(" Sitzung des Oö. Landtags am ", "__");
            sPath = sPath.replace("16_17_und_18_dezember_2003", "16 Dezember 2003");
            sPath = sPath.replace("19._Sitzung_9.__10._Dezember_1987", "19._Sitzung_9._Dezember_1987");
            sPath = sPath.replace("33._Sitzung_12.__13._Dezember_1983", "33._Sitzung_12._Dezember_1983");
            sPath = sPath.replace("18._Sitzung_18.__19._Dezember_1975", "18._Sitzung_18._Dezember_1975");
            sPath = sPath.replace("46._Sitzung_12.__13.__14._Dezember_1990", "46._Sitzung_12._Dezember_1990");
            sPath = sPath.replace("20._Sitzung_11.__12._Dezember_1969", "20._Sitzung_11._Dezember_1969");
            sPath = sPath.replace("13_am_14_15_und_16_dezember_2004", "13._Sitzung_16._dezember_2004");
            sPath = sPath.replace("28._Sitzung_6.__7._Dezember_1989", "28._Sitzung_6._Dezember_1989");
            sPath = sPath.replace("3., 4. und 5. Dezember 2019", "3. Dezember 2019");
            sPath = sPath.replace("_am_5_6_und_7_dezember_2005", "5._Dezember_2005");
            sPath = sPath.replace("255._Dezember_2005", "25._Sitzung_5._Dezember_2005");
            sPath = sPath.replace("12._Sitzung_10.__11._Dezember_1962", "12._Sitzung_10._Dezember_1962");
            sPath = sPath.replace("3._Sitzung_18.__19._Dezember_1985", "3._Sitzung_18._Dezember_1985");
            sPath = sPath.replace("01_am_23_und_27_Oktober_2003", "01._Sitzung_23._Oktober_2003");
            sPath = sPath.replace("33._Sitzung_9.__10._Dezember_1971", "33._Sitzung_9._Dezember_1971");
            sPath = sPath.replace("21._Sitzung_18.__19._Dezember_1957", "21._Sitzung_18._Dezember_1957");
            sPath = sPath.replace("03_am_15_16_und_17_dezember_2009", "03._Sitzung_15_dezember_2009");
            sPath = sPath.replace("15., 16. und 17. Dezember 2015", "15. Dezember 2015");
            sPath = sPath.replace("14., 15. und 16. Dezember 2021", "14. Dezember 2021");
            sPath = sPath.replace("3._Sitzung_14.__15._Dezember_1961", "3._Sitzung_14._Dezember_1961");
            sPath = sPath.replace("43._Sitzung_12.__13._Dezember_1972", "43._Sitzung_12._Dezember_1972");
            sPath = sPath.replace("3._Sitzung_20.__21._Dezember_1979", "3._Sitzung_20._Dezember_1979");
            sPath = sPath.replace("4., 5. und 6. Dezember 2018", "4. Dezember 2018");
            sPath = sPath.replace("14_15_und_16_dezember_2010", "12._Sitzung_14._dezember_2010");
            sPath = sPath.replace("42._Sitzung_10.__11._Dezember_1984", "42._Sitzung_10._Dezember_1984");
            sPath = sPath.replace("26._Sitzung_11.__12._Dezember_1964", "26._Sitzung_11._Dezember_1964");
            sPath = sPath.replace("35._Sitzung_9.__10._Dezember_1965", "35._Sitzung_9._Dezember_1965");
            sPath = sPath.replace("11._Sitzung_10.__11._Dezember_1986", "11._Sitzung_10._Dezember_1986");
            sPath = sPath.replace("27._Sitzung_2.__3._Dezember_1970", "27._Sitzung_2._Dezember_1970");
            sPath = sPath.replace("13., 14. und 15. Dezember 2016", "13._Dezember_2016");
            sPath = sPath.replace("02_03_04_dezember_2014", "02._dezember_2014");
            sPath = sPath.replace("03_04_und_05_dezember_2013", "03._dezember_2013");
            sPath = sPath.replace("5_6_und_7_dezember_2006", "5._dezember_2006");
            sPath = sPath.replace("42._Sitzung_13.__14._Dezember_1978", "42._Sitzung_13._Dezember_1978");
            sPath = sPath.replace("3._Sitzung_20.__21._Dezember_1967", "3._Sitzung_20._Dezember_1967");
            sPath = sPath.replace("4_5_und_6_dezember_2007", "4._dezember_2007");
            sPath = sPath.replace("2_3_und_4_dezember_2008", "2._dezember_2008");
            sPath = sPath.replace("05_06_und_07_dezember_2011", "5._dezember_2011");
            sPath = sPath.replace("04_05_und_06_dezember_2012", "4._dezember_2012");
            sPath = sPath.replace("5., 6. und 7. Dezember 2017", "5._dezember_2017");
            sPath = sPath.replace("36._Sitzung_13.__14._Dezember_1989", "36._Sitzung_13._Dezember_1989");
            sPath = sPath.replace("10._Sitzung_17.__18._Dezember_1980", "10._Sitzung_17._Dezember_1980");
            sPath = sPath.replace("20._Sitzung_11.__12._Dezember_1963", "20._Sitzung_11._Dezember_1963");
            sPath = sPath.replace("38._Sitzung_17.__18._Dezember_1959", "38._Sitzung_17._Dezember_1959");
            sPath = sPath.replace("43._Sitzung_13.__14._Dezember_1966", "43._Sitzung_13._Dezember_1966");
            sPath = sPath.replace("33._Sitzung_5.__6._Dezember_1977", "33._Sitzung_5._Dezember_1977");
            sPath = sPath.replace("25._Sitzung_9.__10._Dezember_1976", "25._Sitzung_9._Dezember_1976");
            sPath = sPath.replace("11._Sitzung_5.__6._Dezember_1974", "11._Sitzung_5._Dezember_1974");
            sPath = sPath.replace("26._Sitzung_15.__16._Dezember_1982", "26._Sitzung_15._Dezember_1982");
            sPath = sPath.replace("3._Sitzung_18.__19._Dezember_1973", "3._Sitzung_18._Dezember_1973");
            sPath = sPath.replace("17._Sitzung_10.__11._Dezember_1981", "17._Sitzung_10._Dezember_1981");
            sPath = sPath.replace("46._Sitzung_13.__14._Dezember_1960", "46._Sitzung_13._Dezember_1960");
            sPath = sPath.replace("11._Sitzung_12.__13._Dezember_1968", "11._Sitzung_12._Dezember_1968");
            sPath = sPath.replace("30._Sitzung_18.__19._Dezember_1958", "30._Sitzung_18._Dezember_1958");
            sPath = sPath.replace("5., 6. und 7. Dezember 2022", "5._Dezember_2022");

            String[] split = null;

            boolean ltg = false;

            if(sPath.contains("_Sitzung_")) {
                sPath = sPath.replace("_Sitzung_", "__");
            }
            else if(sPath.contains("landtagssitzung")){
                sPath = sPath.replace("_landtagssitzung_", "__");
                ltg = true;
            }
            System.out.println("\t"+sPath);

            sPath = sPath.replace("am_", "");

            split = sPath.split("__");

            String sID = "";
            String sDatum = "";

            if(ltg){
                sID =split[1].split("_")[0];
                sDatum = split[1].substring(split[1].indexOf("_")+1);
                sDatum = sDatum.replace("_", " ");
                sDatum = sDatum.replace(".txt", "");
            }
            else{
                sID =split[0];
                sDatum = split[1].replace("_", " ");
            }




            String subPath = file.getAbsolutePath().split("/")[7].replace("\\.", "");


            try {
                Date pDate = null;
                try {
                    pDate = sdf.parse(sDatum);
                }
                catch (Exception e){
                    pDate = sdfBackup.parse(sDatum);
                }

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sID+" "+sdfExport.format(pDate));
                dmd.setDocumentId(sdf.format(pDate));
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+"/"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("bagci");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void baWueDE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        fSet = fSet.stream().filter(f->!f.getAbsolutePath().contains("older") && !f.getAbsolutePath().contains("/17/")).collect(Collectors.toSet());

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String sSplit[] = sChangeFileName.split("_");
            String sPer = sSplit[0];
            String sID = sSplit[1];
            String sDate = sSplit[2];


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(Integer.valueOf(sID)+". Sitzung");
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void baWueDE_older(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        fSet = fSet.stream().filter(f->f.getAbsolutePath().contains("older")).collect(Collectors.toSet());

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String sYear = "";

            if(sChangeFileName.startsWith("Serie")){
                sYear = sChangeFileName.split(", ")[1];
                if(sYear.contains("-")){
                    sYear = sYear.split("-")[0];
                }
            }
            else{
                sYear = sChangeFileName.split(", ")[0];
                if(sYear.contains("-")){
                    sYear = sYear.split("-")[0];
                }
            }

            if(sYear.contains(" ")){
                sYear = sYear.split(" ")[0];
            }


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            subPath = subPath.replace("/ ", "/");
            try {
//                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setDateYear(Integer.valueOf(sYear));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }
    public static void lichtenstein(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            subPath = subPath.replace("/ ", "/");
            try {
                Date pDate = sdf.parse(sChangeFileName);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sFileName);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setDateYear(pDate.getYear());
                da.setDateMonth(pDate.getMonth());
                da.setDateDay(pDate.getDay());
                da.setTimestamp(pDate.getTime());
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    public static void baWueDE_New(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        fSet = fSet.stream().filter(f->f.getAbsolutePath().contains("/17/")).collect(Collectors.toSet());

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String sSplit[] = sChangeFileName.split(" ");
            String sPer = sSplit[0];
            String sID = sSplit[1];
            String sDate = sSplit[2];


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sDate);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                if(sSplit.length>3) {
                    da.setSubtitle(sID + " " + sSplit[3]);
                }
                else{
                    da.setSubtitle(sID);
                }
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void hamburgDE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String sSplit[] = sChangeFileName.split("_");
            String sID = sSplit[0];
            String sDate = sSplit[1];


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sDate);
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                if(sSplit.length>3) {
                    da.setSubtitle(sID + " " + sSplit[3]);
                }
                else{
                    da.setSubtitle(sID);
                }
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void bayern_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String sID = sChangeFileName.substring(0, sChangeFileName.indexOf("PL"));
            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + " " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void bundesrat_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String sID = sChangeFileName.substring(sChangeFileName.indexOf(" "), sChangeFileName.indexOf(","));
            String sDate = sChangeFileName.substring(sChangeFileName.indexOf(", ")+2);


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdfExport.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + " " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void meckPomDE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String[] fileSplit = sChangeFileName.split("_");

            String sID = fileSplit[0]+" "+fileSplit[1];
            String sDate = fileSplit[2];


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdfExport.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + " " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void saarlandDE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String[] fileSplit = sChangeFileName.split("_");

            String sID = fileSplit[1];
            String sDate = fileSplit[2];


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + ". Sitzung " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void schleswigholsteinDE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String[] fileSplit = sChangeFileName.split(", ");

            String sID = fileSplit[0];
            String sDate = fileSplit[1];


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdf.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID +", "+sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void thueringenDE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String[] fileSplit = sChangeFileName.split("_");

            String sID = fileSplit[0];
            String sDate = fileSplit[1];


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdfExport.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID +", "+sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void berlin_DE(String sInput, String sDates, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");
        Set<File> fDatesSet = FileUtils.getFiles(sDates, ".json");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyy", Locale.GERMAN);
        SimpleDateFormat sdfExport = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            File pFile = fDatesSet.stream().filter(f->{
                String sFileInputName = file.getName().replace(".txt", "");
                String sFileName = f.getName().replace(".json", "");
                return sFileInputName.equalsIgnoreCase(sFileName);
            }).findFirst().get();

            JSONObject pObject = new JSONObject(FileUtils.getContentFromFile(pFile));

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            String[] sSplit = sChangeFileName.split("_");
            String sID = sSplit[1];
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdfExport.parse(pObject.getInt("day")+"."+pObject.getInt("month")+"."+pObject.getInt("year"));

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + " " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void brandenburg_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";

            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.startsWith("Potsdam")){
                    System.out.println(sSplitContentLine);
                    sDate = sSplitContentLine.split(", ")[2];
                }
                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;


            String sID = sChangeFileName;
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdfExport.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + " " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void niedersachsen_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfBackup = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";

            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.startsWith("Hannover")){
                    System.out.println(sSplitContentLine);
                    sDate = sSplitContentLine.split("den ")[1];
                }
                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;


            String sID = Integer.valueOf(sChangeFileName)+"";
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = null;

                try {
                    pDate = sdfExport.parse(sDate);
                }
                catch (Exception e){
                    pDate = sdfBackup.parse(sDate);
                }
                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID+". Sitzung " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void nrw_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfBackup = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";

            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.startsWith("Düsseldorf")){
                    System.out.println(sSplitContentLine);
                    sDate = sSplitContentLine.split(", ")[2];
                }
                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;


            String sID = Integer.valueOf(sChangeFileName)+"";
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {

                Date pDate = null;

                try {
                    pDate = sdfExport.parse(sDate);
                }
                catch (Exception e){
                    pDate = sdfBackup.parse(sDate);
                }

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID+". Sitzung " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void rlp_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfBackup = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";

            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.contains("Plenarsitzung am")){
                    System.out.println(sSplitContentLine);
                    sDate = sSplitContentLine.split(", dem")[1];
                }
                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;


            String sID = Integer.valueOf(sChangeFileName)+"";
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {

                Date pDate = null;

                try {
                    pDate = sdfExport.parse(sDate);
                }
                catch (Exception e){
                    pDate = sdfBackup.parse(sDate);
                }

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID+". Sitzung " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void sachsen_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfBackup = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";

            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.contains("Plenarsaal")){
                    System.out.println(sSplitContentLine);
                    sDate = sSplitContentLine.split(", ")[1];
                }
                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            if(sChangeFileName.contains("_")){
                sChangeFileName = sChangeFileName.split("_")[1];
            }
            String sID = Integer.valueOf(sChangeFileName)+"";
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {

                Date pDate = null;

                try {
                    pDate = sdfExport.parse(sDate);
                }
                catch (Exception e){
                    pDate = sdfBackup.parse(sDate);
                }

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID+". Sitzung " + sdfBackup.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }
    public static void sachsen_anhalt_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);
        SimpleDateFormat sdfBackup = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";

            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.contains("Sitzung, ")){
                    System.out.println(sSplitContentLine);
                    sDate = sSplitContentLine.split(", ")[2];
                }
                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;

            if(sChangeFileName.contains("_")){
                sChangeFileName = sChangeFileName.split("_")[1];
            }
            String sID = Integer.valueOf(sChangeFileName)+"";
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {

                Date pDate = null;

                if(sDate.equalsIgnoreCase("27.042023")){
                    sDate = "27.04.2023";
                }

                try {
                    pDate = sdfExport.parse(sDate);
                }
                catch (Exception e){

                    pDate = sdfBackup.parse(sDate);
                }

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID+". Sitzung " + sdfBackup.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void hessen_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";

            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.startsWith("Wiesbaden")){
                    System.out.println(sSplitContentLine);
                    if(sSplitContentLine.contains("den ")) {
                        sDate = sSplitContentLine.split("den ")[1];
                    }
                    else{
                        String[] sSplit = sSplitContentLine.split(", ");
                        sDate = sSplit[sSplit.length-1];
                    }
                }
                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;


            String sID = sChangeFileName;
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdfExport.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + " " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

        }


    }

    public static void bremen_DE(String sInput, String sOutput) throws IOException, UIMAException {

        Set<File> fSet = FileUtils.getFiles(sInput, ".txt");

        JCas emptyCas = JCasFactory.createJCas();
        AnalysisEngine nlp = nlp(globalOutput);

        SimpleDateFormat sdfExport = new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMAN);

        for (File file : fSet) {

            emptyCas.reset();

            String sContent = FileUtils.getContentFromFile(file);
            emptyCas.setDocumentText(sContent);
            emptyCas.setDocumentLanguage("de");

            String[] sSplitContentLines = sContent.split("\n");

            String sDate = "";


            for (String sSplitContentLine : sSplitContentLines) {
                if(sSplitContentLine.startsWith("am ") || sSplitContentLine.contains(" am ")){
                    sDate = sSplitContentLine.substring(sSplitContentLine.indexOf("dem ")+4);
                    if(sDate.contains("und")){
                        sDate = sDate.substring(0, sDate.indexOf(", "));
                    }
                }

                if(sDate.length()>0){
                    break;
                }
            }

            String sFileName = file.getName();
            sFileName = sFileName.replace(".txt", "");
            String sChangeFileName = sFileName;


            String sID = Integer.valueOf(sChangeFileName.split("L")[1])+"";
//            String sDate = sChangeFileName.substring(sChangeFileName.indexOf("PL")+2, sChangeFileName.indexOf("gesend"));


            System.out.println(file.getAbsolutePath());
            String subPath = file.getAbsolutePath().replace(sInput, "");
            subPath = subPath.replace(file.getName(), "");
            try {
                Date pDate = sdfExport.parse(sDate);

                DocumentMetaData dmd = DocumentMetaData.create(emptyCas);
                dmd.setDocumentTitle(sFileName);
                dmd.setDocumentId(sFileName);
                dmd.setDocumentUri(globalOutput+""+sOutput+""+subPath+""+sID+"_"+sdfExport.format(pDate));
                dmd.setDocumentBaseUri(globalOutput);

                DocumentModification dm1 = new DocumentModification(emptyCas);
                dm1.setUser("abrami");
                FileTime ft = (FileTime) Files.getAttribute(file.toPath(), "creationTime");
                dm1.setTimestamp(ft.toMillis());
                dm1.setComment("Download");
                dm1.addToIndexes();

                DocumentModification dm2 = new DocumentModification(emptyCas);
                dm2.setUser("abrami");
                dm2.setTimestamp(ft.toMillis());
                dm2.setComment("Conversion");
                dm2.addToIndexes();

                DocumentAnnotation da = new DocumentAnnotation(emptyCas);
                da.setTimestamp(pDate.getTime());
                da.setDateDay(pDate.getDay());
                da.setDateMonth(pDate.getMonth());
                da.setDateYear(pDate.getYear());
                da.setSubtitle(sID + " " + sdfExport.format(pDate));
                da.addToIndexes();

                SimplePipeline.runPipeline(emptyCas, nlp);


            } catch (ParseException e) {
                System.out.println(file.getName());
                throw new RuntimeException(e);
            }

        }


    }

}
