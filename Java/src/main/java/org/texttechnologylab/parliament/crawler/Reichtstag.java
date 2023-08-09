package org.texttechnologylab.parliament.crawler;

import com.google.api.client.util.ArrayMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for Parsing Reichstags-Minutes
 * @author Giuseppe Abrami
 * @date 2021-12-01
 */
public class Reichtstag {

    public static String BASEURL = "https://www.reichstagsprotokolle.de/";
    public static String OUTFILE = "/tmp/parlament/";
    public static String OCRAPI = "https://api.digitale-sammlungen.de/ocr/{object_id}/{page_num}";

    public static void main(String[] args) {


        try {

            StringBuilder errorBuilder1 = new StringBuilder();
            StringBuilder errorBuilder2 = new StringBuilder();

            new File(OUTFILE).mkdir();
            Document pDocument = Jsoup.connect(String.valueOf(new URL(BASEURL + "index.html"))).get();

            Elements innernav = pDocument.select("div .innernav2");

            innernav.forEach(element -> {


                if (element.text().contains("Protokolle/Anlagen")) {

                    Elements topElements = element.select(".navi");

                    List<Element> ttList = topElements.stream().collect(Collectors.toList());

                    ttList.remove(ttList.get(0));

                    ttList.forEach(elements -> {

                        String sURL = elements.attr("href");
                        System.out.println(sURL);
                        try {
                            Document pZeitraum = Jsoup.connect(String.valueOf(new URL(BASEURL + sURL))).get();

                            Elements pLi = pZeitraum.select("div.innernav2 li a");

                            List<Element> tList = pLi.stream().collect(Collectors.toList());

                            tList.forEach(link -> {

                                new File(OUTFILE + "/" + link.text()).mkdir();

                                try {
                                    try {
                                        Thread.sleep(1500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    Document pUnter = Jsoup.connect(String.valueOf(new URL(BASEURL + link.attr("href")))).get();

                                    Elements pEintraege = pUnter.select("ul.left2 li a");

                                    List<Element> eList = pEintraege.stream().collect(Collectors.toList());


                                    eList.forEach(eintrag -> {
                                        try {
                                            String sEintrag = eintrag.text();
                                            sEintrag = sEintrag.replace("/", "_");

                                            new File(OUTFILE + "/" + link.text() + "/" + sEintrag).mkdir();

                                            Map<String, String> sMap = getContent(new URL(BASEURL + eintrag.attr("href")));

                                            Map<String, Map<Integer, Integer>> rangeMap = new HashMap<>(0);

                                            List<Integer> iMap = new ArrayList<>(0);

                                            sMap.remove("");

                                            try {
                                                iMap = sMap.values().stream().map(v -> {
                                                    String s1 = v.substring(v.lastIndexOf("_") + 1);
                                                    String s2 = s1.substring(0, s1.lastIndexOf("."));
                                                    return Integer.valueOf(s2);
                                                }).collect(Collectors.toList());
                                            } catch (Exception e) {
                                                System.out.println(OUTFILE + "/" + link.text() + "/" + sEintrag);

                                            }

                                            List<String> keyList = sMap.keySet().stream().collect(Collectors.toList());
                                            long waitTime = 1500l;

                                            for (int a = 0; a < keyList.size(); a++) {

                                                if (a < keyList.size() - 1) {

                                                    int iStart = iMap.get(a);
                                                    int iEnd = iMap.get(a + 1);

                                                    for (int download = iStart; download < iEnd; download++) {

                                                        String sValue = sMap.get(keyList.get(a));

                                                        String sFileName = keyList.get(a);

                                                        String apiValueKey = sValue.substring(sValue.indexOf("bsb"), sValue.lastIndexOf("_"));

                                                        String getURI = OCRAPI.replace("{object_id}", apiValueKey);
                                                        getURI = getURI.replace("{page_num}", String.valueOf(download));
                                                        File targetFile = null;
                                                        try {
                                                            targetFile = new File(OUTFILE + "/" + link.text() + "/" + sEintrag + "/" + sFileName + "_" + download + ".xml");
                                                            if (!targetFile.exists()) {
                                                                Thread.sleep(waitTime);
                                                                FileUtils.downloadFile(targetFile, getURI);
                                                            }
                                                        } catch (InterruptedException e) {
                                                            e.printStackTrace();
                                                        } catch (IOException fne) {

                                                            if (fne.getMessage().contains("429 for URL")) {
                                                                errorBuilder2.append(targetFile.getAbsolutePath() + "\t" +fne.getMessage());

                                                                waitTime = waitTime + 1000;
                                                                try {
                                                                    Thread.sleep(10000);
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }

                                                            }
                                                            else{
                                                                errorBuilder1.append(sEintrag + "/" + sFileName + "\t" + fne.getClass().getSimpleName() + "\t" + fne.getMessage());
                                                            }

                                                            System.out.println(sEintrag + "/" + sFileName + "\t" + fne.getClass().getSimpleName() + "\t" + fne.getMessage());
                                                        }


                                                    }

                                                } else {

                                                    int iStart = iMap.get(a);
                                                    boolean downloadLeft = true;
                                                    while (downloadLeft) {

                                                        String sValue = sMap.get(keyList.get(a));

                                                        String sFileName = keyList.get(a);

                                                        String apiValueKey = sValue.substring(sValue.indexOf("bsb"), sValue.lastIndexOf("_"));

                                                        String getURI = OCRAPI.replace("{object_id}", apiValueKey);
                                                        getURI = getURI.replace("{page_num}", String.valueOf(iStart++));

                                                        File targetFile = new File(OUTFILE + "/" + link.text() + "/" + sEintrag + "/" + sFileName + "_" + iStart + ".xml");

                                                        try {
                                                            if (!targetFile.exists()) {
                                                                Thread.sleep(1500);
                                                                FileUtils.downloadFile(targetFile, getURI);
                                                            }
                                                        } catch (Exception e) {
                                                            System.out.println("Finish: " + sEintrag + "/" + sFileName + "\t" + e.getMessage());
                                                            downloadLeft = false;
                                                        }

                                                    }
                                                }


                                            }

                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }
                                    });


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }


                            });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    });


                }


            });

            FileUtils.writeContent(errorBuilder1.toString(), new File("/tmp/error1.txt"));
            FileUtils.writeContent(errorBuilder2.toString(), new File("/tmp/error2.txt"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    public static Map<String, String> getContent(URL pURL) {

        Map<String, String> rSet = new ArrayMap<>();

        try {
            Document pDocument = Jsoup.connect(String.valueOf(pURL)).get();

            Elements content = pDocument.select("div.content12");

            Elements links = content.select("a.navi");

            links.forEach(l -> {

                String label = l.text();
                int iCount = 0;
                while (rSet.containsKey(label)) {
                    iCount++;

                    if (label.contains("__")) {
                        label = label.substring(0, label.lastIndexOf("__"));
                    }

                    label = label + "__" + iCount;

                }

                rSet.put(label, l.attr("href"));

            });

        } catch (IOException e) {
            e.printStackTrace();
        }


        return rSet;

    }

}
