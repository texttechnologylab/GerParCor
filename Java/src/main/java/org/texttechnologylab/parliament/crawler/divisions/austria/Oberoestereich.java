package org.texttechnologylab.parliament.crawler.divisions.austria;

import com.google.common.html.HtmlEscapers;
import com.google.common.io.Files;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.texttechnologylab.utilities.helper.FileUtils;

import javax.net.ssl.*;
import javax.print.Doc;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

public class Oberoestereich {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();

        String sDownladBasePath = "https://www.land-oberoesterreich.gv.at";

        Document pDocument = Jsoup.connect("https://www.land-oberoesterreich.gv.at/ltgspsuche.htm").sslSocketFactory(socketFactory()).get();

        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        Elements pElements = pDocument.select("ul.liste-extra li a");

        pElements.stream().forEach(el1->{


            String sTitle = el1.text();
            sTitle = sTitle.replace("Sitzungen der ", "");
            System.out.println(sTitle);
            new File(sOutpath+sTitle).mkdir();

            try {
                if (!el1.attr("href").contains("alex.onb.")) {
                    Document subPage = Jsoup.connect(
                            el1.attr("href").startsWith("https://") ? el1.attr("href") : sDownladBasePath + el1.attr("href")).sslSocketFactory(socketFactory()).ignoreContentType(true).get();

                    Elements pSubElements = subPage.select("li.link-extern");
                    if (pSubElements.size() == 0) {
                        pSubElements = subPage.select("ul.liste-extra li");
                    }

                    String finalSTitle = sTitle;
                    pSubElements.stream().forEach(el2 -> {
                        System.out.println(el2.select("a").get(0).text());

                        try {
                            Document intDocument = Jsoup.connect(el2.select("a").get(0).attr("href")).get();

                            Elements pLinks = intDocument.select("div.beilagenElement ul li a");

                            pLinks.stream().forEach(el->{
                                if(el.text().contains("Wortprotokoll")){
                                    String sURL = "https://www2.land-oberoesterreich.gv.at/internetltgbeilagen/"+el.attr("href");

                                    sURL = sURL.substring(0, sURL.indexOf("#page="));
                                    try {
                                        Document pPDF = Jsoup.connect(sURL).ignoreContentType(true).get();
                                        //FileUtils.writeContent(pPDF.body().text(), new File("/tmp"))
                                        System.out.println(pPDF);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }

                                    File dFile = new File(sOutpath + finalSTitle + "/" + el2.select("a").get(0).text() + ".pdf");
                                    if (!dFile.exists()) {
                                        try {
                                            FileUtils.downloadFile(dFile, sURL);
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                }
                            });

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        if(el2.select("a").attr("href").endsWith(".pdf")) {




                        }
                        else{

                            try {

                                Document intDocument = Jsoup.connect(el2.select("a").attr("href")).get();
                                String sDownloadLink = "https://www2.land-oberoesterreich.gv.at/internetltgbeilagen/"+intDocument.select("div.beilagenElement li a").get(0).attr("href");
                                System.out.println(StringEscapeUtils.escapeHtml(sDownloadLink));



//                                FileUtils.downloadFile(dFile, "https://www2.land-oberoesterreich.gv.at/internetltgbeilagen/"+intDocument.select("div.beilagenElement li a").get(0).attr("href"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    });

                }

                } catch(IOException e){
                    throw new RuntimeException(e);
                }




        });



    }

    static private SSLSocketFactory socketFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory result = sslContext.getSocketFactory();

            return result;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Failed to create a SSL socket factory", e);
        }
    }


    @Test
    public void zwentySevenTh() throws IOException {

        Document pDocument = Jsoup.connect("https://www.land-oberoesterreich.gv.at/92799.htm").sslSocketFactory(socketFactory()).get();

        String sBaseURL = "https://www.land-oberoesterreich.gv.at";
        String sOutput = "/storage/projects/abrami/GerParCor/pdf/Austria/Oberoestereich/XXVII._Gesetzgebungsperiode/";

        Elements pElements = pDocument.select("ul.liste-extra li a");
        pElements.stream().forEach(el->{
            String sLink = el.attr("href");
            System.out.println(sLink.substring(sLink.indexOf("sitzung_")));
            String subLink = sLink.substring(sLink.indexOf("sitzung_")+8);
            subLink = subLink.replace("_am_", "__");
            String sID = subLink.split("__")[0];
            String sDatum = subLink.split("__")[1];
            sDatum.replaceAll("_", " ");

            String[] dSplit = sDatum.split("_");
            String sYear = dSplit[dSplit.length-1];
            sYear = sYear.replace(".pdf", "");
            new File(sOutput+""+sYear).mkdir();

            File dFile = new File(sOutput+""+sYear+"/"+sID+"__"+sDatum);

            if(!dFile.exists()){
                try {
                    FileUtils.downloadFile(dFile, sBaseURL+sLink);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

//            System.out.println(el.attr("href"));
        });

    }

    @Test
    public void oldImages(){

        String sURL = "https://alex.onb.ac.at/olr_00ID.htm";
        String sBaseURI = "https://alex.onb.ac.at";
        String sBaseTmp = "/storage/projects/abrami/GerParCor/pdf/Austria/Oberoestereich/";

        for(int iRunCount = 23; iRunCount<24; iRunCount++) {
            try {
                Document pDocument = Jsoup.connect(sURL.replace("ID", iRunCount+"")).get();

                String sTitle = pDocument.select("#content h2").text();
                sTitle = sTitle.replaceAll("/", "_");

                Elements pElements = pDocument.select("table tr");
                String finalSTitle = sTitle;
                pElements.subList(1, pElements.size()).stream().forEach(el -> {

                    String sID = el.select("td").get(0).text();
                    String sDatum = el.select("td").get(1).text();
                    String sLink = el.select("a").attr("href");

                    System.out.println(sID);
                    System.out.println(sDatum);
                    System.out.println(sLink);


                    int iMax = 0;
                    try {
                        Document pImages = Jsoup.connect(sBaseURI + sLink).get();
                        Elements elImages =  pImages.select("div.prevws a");

                        elImages.stream().forEach(image->{
                            try {
                                System.out.println(sBaseURI+image.attr("href"));
                                Document pImage = Jsoup.connect(sBaseURI + image.attr("href")).followRedirects(true).ignoreHttpErrors(true).ignoreContentType(true).get();
                                System.out.println(pImage.select("#content img").get(0).attr("src"));
                                String sDownloadLink = pImage.select("#content img").get(0).attr("src");
                                sDownloadLink = sDownloadLink.replaceAll("tif", "jpg");
                                sDownloadLink = sDownloadLink.substring(0, sDownloadLink.lastIndexOf("||"));
                                sDownloadLink = sDownloadLink+"||100|";
                                System.out.println(sDownloadLink);

                                String sTempDir = sBaseTmp + finalSTitle;
                                new File(sTempDir).mkdir();
                                sTempDir = sTempDir + "/" + sID + "_" + sDatum.replaceAll("/", "__");

                                new File(sTempDir).mkdir();

//                                if (iCount % 10 == 0) {

//                                }

                                String[] sSplit = sLink.substring(sLink.indexOf("&")).split("&");
                                int prID = Integer.valueOf(sSplit[2].split("=")[1]);
                                String prDatum = sSplit[1].split("=")[1];
                                String sName = sDownloadLink.split("\\|")[3];
                                try {
                                    File dFile = new File(sTempDir + "/" + sName + ".jpg");
                                    if (!dFile.exists()) {
                                        try {
                                            Thread.sleep(1000l);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }
                                        System.out.println(sDownloadLink);
                                        FileUtils.downloadFile(dFile, "https://alex.onb.ac.at/cgi-content/"+sDownloadLink);
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });



                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }



//                    String sDownloadPagesURL = "https://alex.onb.ac.at/cgi-content/alex-show?call=olr|DATUM|PR|COUNT||jpg||100|";
//
//                    String[] sSplit = sLink.substring(sLink.indexOf("&")).split("&");
//                    int prID = Integer.valueOf(sSplit[2].split("=")[1]);
//                    String prDatum = sSplit[1].split("=")[1];
//
//
//
//                    int iCount = 1;
//
//                    while (iCount <= iMax) {
//
//                        String sDownload = sDownloadPagesURL.replace("DATUM", prDatum);
//                        sDownload = sDownload.replace("PR", fillZero(prID, 4));
//                        sDownload = sDownload.replace("COUNT", fillZero(iCount, 8));
//
//
//
//                        iCount++;
//                    }

                });


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static String fillZero(int iValue, int iMax){

        String rString = ""+iValue;

            while(rString.length()<iMax){
                rString = "0"+rString;
            }


        return rString;

    }

    public static File convertToPDF(String sPath) throws IOException, InterruptedException {
// convert
        ProcessBuilder pb = new ProcessBuilder("/usr/bin/convert", "$(ls -1v "+sPath+"/*.jpg)", "-quality 100", sPath+"/out.pdf");
        pb.directory(new File(sPath+"/"));

        Process p = null;
        try {
            p = pb.start();

            try {
                // Create a new reader from the InputStream
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                // Take in the input
                String input;
                while((input = br.readLine()) != null){
                    // Print the input
                    System.out.println(input);
                }
                while((input = br2.readLine()) != null){
                    // Print the input
                    System.err.println(input);
                }
            } catch(IOException io) {
                io.printStackTrace();
            }

            p.waitFor();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }

        return new File(sPath+"/out.pdf");
    }

    @Test
    public void rename(){

        String sBaseTmp = "/storage/projects/abrami/GerParCor/pdf/Austria/Oberoestereich/";

        Set<File> dirs = new HashSet<>(0);

        File bFile = new File(sBaseTmp);
        for (File file : bFile.listFiles()) {
            if(file.isDirectory()){
                dirs.addAll(getDirs(file));
            }
        }

        for (File d : dirs) {
            try {
                File nFile = new File(d.getParent()+"/"+d.getName().replaceAll(" ", "_"));
                if(!nFile.exists()) {
                    Files.move(d, nFile);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }
    public static Set<File> getDirs(File pFile){
        Set<File> rSet = new HashSet<>(0);
        if(pFile.isDirectory()){
            rSet.add(pFile);
        }
        for (File file : pFile.listFiles()) {
            if(file.isDirectory()){
                rSet.add(file);
                rSet.addAll(getDirs(file));
            }
        }

        return rSet;

    }

}
