import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.texttechnologylab.parliament.duui.MongoDBImporter;

import java.io.File;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class Importer {

    private static int iWorkers = 2;


    @Test
    public void importGermany() throws Exception {

//        importSubDir("/storage/xmi/GerParCorDownload/Germany/National/Bundestag", "xmi.gz", "Germany", "Bundestag", "National", "false", "");

//        importSubDir("/storage/xmi/GerParCorDownload/Germany/National/Bundesrat/xmi", "xmi.gz", "Germany", "Bundesrat", "National", "false", "");
//
        importSubDir("/storage/xmi/GerParCorDownload/Germany/Historical/National/Weimar_Republic", ".xmi.gz", "Germany", "Reichstag", "National", "true", "Weimar_Republic");
//        importSubDir("/storage/xmi/GerParCorDownload/Germany/Historical/National/ThirdReich", "xmi.gz", "Germany", "Reichstag", "National", "true", "Third_Reich");
//
//        importSubDir("/storage/xmi/GerParCorDownload/Germany/Historical/National/Reichstag", "xmi.gz", "Germany", "Reichstag", "National", "true", "German Empire");
//
//        importSubDir("/storage/xmi/GerParCorDownload/Germany/Historical/National/Reichstag_Empire", "xmi.gz", "Germany", "Reichstag", "National", "true", "North German Union / Zollparlamente");

//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Alter Landtag Württemberg (1797-1799)", "xmi.gz", "Germany", "Alter Landtag Württemberg", "Regional", "true", "", "1797-1799");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Landtag Baden-Württemberg (1953-1996)", "xmi.gz", "Germany", "Landtag Baden-Württemberg", "Regional", "true", "", "1953-1996");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Landtag Württemberg", "xmi.gz", "Germany", "Landtag Württemberg", "Regional", "true", "", "");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Landtag Württemberg-Baden (1946-1952)", "xmi.gz", "Germany", "Landtag Württemberg-Baden", "Regional", "true", "", "1946-1952");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Landtag Württemberg-Hohenzollern (1946-1952)", "xmi.gz", "Germany", "Landtag Württemberg-Hohenzollern", "Regional", "true", "", "1946-1952");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Ständeversammlung Württemberg (1815-1819)", "xmi.gz", "Germany", "Ständeversammlung Württemberg", "Regional", "true", "", "1815-1819");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Verfassungsgebende Landesversammlung Baden-Württemberg", "xmi.gz", "Germany", "Verfassungsgebende Landesversammlung Baden-Württemberg", "Regional", "true", "", "");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Verfassungsgebende Landesversammlungen Württemberg (1849-1850, 1919-1920)", "xmi.gz", "Germany", "Verfassungsgebende Landesversammlungen Württemberg", "Regional", "true", "", "1849-1850, 1919-1920");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Verfassungsgebende Landesversammlung Württemberg-Baden", "xmi.gz", "Germany", "Verfassungsgebende Landesversammlung Württemberg-Baden", "Regional", "true", "", "");
//        importMethod("/storage/xmi/GerParCorDownload/Germany/Historical/Regional/Verfassungsgebende Landesversammlung Württemberg-Hohenzollern", "xmi.gz", "Germany", "Verfassungsgebende Landesversammlung Württemberg-Hohenzollern", "Regional", "true", "", "");
//
//        for (String s : "BadenWuertemberg;Bayern;Berlin;Brandenburg;Bremen;Hamburg;Hessen;MeckPom;Niedersachsen;NordrheinWestfalen;RheinlandPfalz;Saarland;Sachsen;SachsenAnhalt;SchleswigHolstein;Thueringen".split(";")) {
//            importSubDir("/storage/xmi/GerParCorDownload/Germany/Regional/"+s, "xmi.gz", "Germany", s, "Regional", "false", "");
//        }


    }

    @Test
    public void importAustria() throws Exception {

//        importSubDir("/storage/xmi/GerParCorDownload/Germany/National/Bundestag", "xmi.gz", "Germany", "Bundestag", "National", "false", "");

        importSubDir("/storage/xmi/GerParCorDownload/Austria/National/Bundesrat", "xmi.gz", "Austria", "Bundesrat", "National", "false", "");

        importSubDir("/storage/xmi/GerParCorDownload/Austria/National/Nationalrat/xmi", "xmi.gz", "Austria", "Nationalrat", "National", "false", "");
//


        for (String s : "Kaernten;Niederoestereich;Salzburg;Steiermark;Tirol;Vorarlberg;Wien".split(";")) {
            importSubDir("/storage/xmi/GerParCorDownload/Austria/Regional/"+s, "xmi.gz", "Austria", s, "Regional", "false", "");
        }


    }


    @Test
    public void importLiechtenstein() throws Exception {
        importMethod("/storage/xmi/GerParCorDownload/Liechtenstein/xmi", "xmi.gz", "Liechtenstein", "Landtag", "National", "false", "", "");

    }

    @Test
    public void importSchweiz() throws Exception {

//        importSubDir("/storage/xmi/GerParCorDownload/Germany/National/Bundestag", "xmi.gz", "Germany", "Bundestag", "National", "false", "");

//        importMethod("/storage/xmi/GerParCorDownload/Schweiz/xmi", "xmi.gz", "Schweiz", "Nationalrat", "National", "false", "", "");

    }

    public static void importSubDir(String sInput, String sEnding, String sCountry, String sParliament, String sDevision, String sHistorical, String sComment) throws Exception {
        File[] dirs = new File(sInput).listFiles(File::isDirectory);

        for (File dir : dirs) {

            importMethod(sInput+"/"+dir.getName(), sEnding, sCountry, sParliament, sDevision, sHistorical, dir.getName(), sComment);
        }
    }

    public static void importMethod(String sInput, String sEnding, String sCountry, String sParliament, String sDevision, String sHistorical, String sSubpath, String sComment) throws Exception {


        AsyncCollectionReader importer = new AsyncCollectionReader(sInput, sEnding, 1, 0, "", false, "all", 0);
//        this(folder, ending, debugCount, iRandom, bSort, savePath, bAddMetadata, language, 0);


        DUUILuaContext ctx = new DUUILuaContext().withJsonLibrary();

        DUUIComposer composer = new DUUIComposer()
                //       .withStorageBackend(new DUUIArangoDBStorageBackend("password",8888))
                .withWorkers(iWorkers)
                .withLuaContext(ctx).withSkipVerification(true);

        // Instantiate drivers with options
        DUUIUIMADriver uima_driver = new DUUIUIMADriver();

        // A driver must be added before components can be added for it in the composer.
        composer.addDriver(uima_driver);

        composer.add(new DUUIUIMADriver.Component(createEngineDescription(MongoDBImporter.class,
                MongoDBImporter.PARAM_DBConnection, "/home/staff_homes/abrami/Projects/GitHub/GerParCor/Java/src/main/resources/new_rw",
                MongoDBImporter.PARAM_Country, sCountry,
                MongoDBImporter.PARAM_Parliament, sParliament,
                MongoDBImporter.PARAM_Devision, sDevision,
                MongoDBImporter.PARAM_Historical, sHistorical,
                MongoDBImporter.PARAM_Subpath, sSubpath,
                MongoDBImporter.PARAM_Comment, sComment
        )).build());

        composer.run(importer, "import");

    }

}
