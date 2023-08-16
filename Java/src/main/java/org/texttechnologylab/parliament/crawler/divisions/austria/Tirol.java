package org.texttechnologylab.parliament.crawler.divisions.austria;

import com.goebl.david.Webb;
import it.unimi.dsi.fastutil.Hash;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.tools.ant.filters.StringInputStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.texttechnologylab.utilities.helper.FileUtils;
import sun.misc.BASE64Decoder;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Tirol {

    public static void main(String[] args) throws IOException {

        String sOutpath = args[0];
        new File(sOutpath).mkdir();
        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory());

        String sDownladBasePath = "https://service.salzburg.gv.at";

        Document pDocument = Jsoup.connect("https://portal.tirol.gv.at/LteWeb/public/sitzung/landtag/landtagsSitzungList.xhtml?cid=4").sslSocketFactory(socketFactory()).get();

        pDocument.select("#c_listContent_j_id_4g_menu select option").forEach(option->{
            if(option.text().length()>6){
                int iValue = Integer.parseInt(option.attr("value"));

                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                try {
                    Map<String, String> cookies = new HashMap<>(0);
                    cookies.put("LTESESSIONID", "000098bcnC700_XjmAb6ftpKZ1w:-1");
                    cookies.put("NSC_mc_xbtm", "7ce2a3d983fcd9bc2fc944174dd9aa298e5fb2e9567bc3a7c73bf36991557efbffdc4b4f");
                    cookies.put("PORTALSESSIONID", "0000iEEEEFb99zYItkUnmceS8dz:1c17i9gi6");

                    Map<String, String> params = new HashMap<>();
                    params.put("listContent:j_id_4i:menu_input", String.valueOf(iValue));
                    params.put("token", "0891608186");
                    params.put("javax.faces.source", "listContent:j_id_4v_9");
                    params.put("javax.faces.partial.execute", "@all");
                    params.put("javax.faces.partial.render", "listContent:fid+listContent:resultForm+actionbar");
                    params.put("listContent:j_id_4v_9", "listContent:j_id_4v_9");
                    params.put("javax.faces.ViewState", "IjE91vsAOdMhnRJmNWPYWMLubKkuFt7XLq1xo0CdDef0kawzS+T08+AivhoAsmS3iASyIg==");
                    Document subDocument = Jsoup.connect("https://portal.tirol.gv.at/LteWeb/public/sitzung/sitzungsbericht/sitzungsberichtList.xhtml?cid=4").sslSocketFactory(socketFactory()).cookies(cookies).data(params).followRedirects(true).ignoreContentType(true).post();

//                    System.out.println(subDocument);

                    subDocument.select("tbody.ui-datatable-data tr").stream().forEach(tr->{
//                        System.out.println(tr.text());
                        Element a = tr.select("a").get(0);
//                        System.out.println("stop");

                        try {
                            Map<String, String> paramsDownload = new HashMap<>();
                            paramsDownload.put("listContent:resultForm_SUBMIT", "1");
                            paramsDownload.put("javax.faces.ViewState", params.get("javax.faces.ViewState"));
                            paramsDownload.put(a.id(), a.id());

                            Document dJsoup = Jsoup.connect("https://portal.tirol.gv.at/LteWeb/public/sitzung/sitzungsbericht/sitzungsberichtList.xhtml?cid=4").cookies(cookies).data(paramsDownload).ignoreContentType(true).ignoreHttpErrors(true).followRedirects(true).userAgent("Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/116.0").header("Content-Disposition",
                                    "attachment; filename=\"Sitzungsbericht_vom_22._und_23._M_C3_A4rz_2023.pdf\"").post();



//                            System.out.println(dJsoup);
//                            FileUtils.writeContent(, new File("/tmp/test.pdf"));

//                            FileUtils.writeContent(dJsoup.toString(), new File("/tmp/test.pdf"));



                            byte[] decoder = Base64.getMimeDecoder().decode(dJsoup.body().text());

                            File file = new File("/tmp/test.pdf");
                            FileOutputStream fop = new FileOutputStream(file);

                            fop.write(decoder);
                            fop.flush();
                            fop.close();

//                            byte[] entityBytes = dJsoup.text().getBytes(Charset.forName("UTF-8"));
//                            Files.write(Paths.get("/tmp/test.pdf"), entityBytes);


                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                System.out.println(option.text());
            }

        });



//            Elements pElements = pDocument.select("table#mySearchTable tbody tr");

//            pElements.stream().forEach(el1 -> {
//
//                String sTitle = el1.select("div.col-md").text();
//                sTitle = sTitle.replace("Meldung vom ", "");
//                System.out.println(sTitle);
//
//                String finalSTitle1 = sTitle;
//                el1.select("a").stream().forEach(a->{
//                    if(a.attr("href").endsWith(".pdf")){
//                        File dFile = new File(sOutpath + finalIPeriode + "/" + finalSTitle1 + ".pdf");
//
//                        if(!dFile.exists()){
//                            try {
//                                FileUtils.downloadFile(dFile, "https://"+a.attr("href").replace("http://", ""));
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//
//                        }
//
//                    }
//                });
//
//            });

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

}
