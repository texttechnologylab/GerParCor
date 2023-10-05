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
import org.texttechnologylab.annotation.DocumentAnnotation;
import org.texttechnologylab.annotation.DocumentModification;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Merger {


    static String globalOutput = "/storage/projects/abrami/GerParCor/xmi/";

    public static void main(String[] args){

        try {
//            bundesratAT("/storage/projects/abrami/GerParCor/txt/Austria/Bundesrat/", "Austria/Bundesrat/");
//            nationalratAT("/storage/projects/abrami/GerParCor/txt/Austria/Nationalrat/", "Austria/Nationalrat/");
//            kaerntenAT("/storage/projects/abrami/GerParCor/txt/Austria/Kaernten/", "Austria/Kaernten/");
//            niederAT("/storage/projects/abrami/GerParCor/txt/Austria/Niederoestereich/", "Austria/Niederoestereich/");
//            salzburgAT("/storage/projects/abrami/GerParCor/txt/Austria/Salzburg/", "Austria/Salzburg/");
//            steiermarkAT("/storage/projects/abrami/GerParCor/txt/Austria/Steiermark/", "Austria/Steiermark/");
            tirolAT("/storage/projects/abrami/GerParCor/txt/Austria/Tirol/", "Austria/Tirol/");
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
    public static void tirolAT(String sInput, String sOutput) throws IOException, UIMAException {

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
            sChangeFileName = sChangeFileName.replace("am 10. Dezember 1998", "vom 10. Dezember 1998");
            sChangeFileName = sChangeFileName.contains("1. Dezember 1960") ? "vom 1. Dezember 1960" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("14. September 2023") ? "vom 14. September 2023" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("am 24. Juni 2022") ? "vom 24. Juni 2022" : sChangeFileName;
            sChangeFileName = sChangeFileName.contains("13. Juli 1963") ? "vom 13. Juli 1963" : sChangeFileName;

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

}
