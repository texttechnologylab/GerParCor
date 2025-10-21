package org.texttechnologylab.parliament.duui;

import org.junit.jupiter.api.Test;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIUIMADriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUIAsynchronousProcessor;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUICollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;

import java.util.HashSet;
import java.util.Set;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class DUUIPipeline {


    @Test
    public void annotate() throws Exception {

        int iWorkers = 1;

        org.texttechnologylab.parliament.duui.XmiReader multiReader = new org.texttechnologylab.parliament.duui.XmiReader("D:\\UniCode\\Java\\GerParCorGitFork\\Java\\downloads\\bundestagNeu\\20", "xmi");
        Set<DUUICollectionReader> readers = new HashSet<>();
        readers.add(multiReader);

        DUUIAsynchronousProcessor processor = new DUUIAsynchronousProcessor(readers);

        DUUILuaContext ctx = new DUUILuaContext().withJsonLibrary();

        DUUIComposer composer = new DUUIComposer()
                .withSkipVerification(true)
                .withLuaContext(ctx)
                .withWorkers(iWorkers);

        DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver(60);
        DUUIUIMADriver uimaDriver = new DUUIUIMADriver();

        composer.addDriver(uimaDriver, remoteDriver);

        /*composer.add(new DUUIRemoteDriver.Component("http://localhost:9714/")  // WhisperX
                .withScale(iWorkers)
                .withTargetView("transcription_view")
                .withParameter("language", "de")
                .build().withTimeout(60));*/

        /*composer.add(new DUUIRemoteDriver.Component("http://localhost:9713/")  // SpaCy
                .withScale(iWorkers)
                .withView("transcription_view")
                .build().withTimeout(60));*/

        /*composer.add(new DUUIRemoteDriver.Component("http://localhost:9715/")  // Jina
                .withScale(iWorkers)
                .withView("transcription_view")
                .withParameter("type", "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence")
                .build().withTimeout(60));*/

        composer.add(new DUUIRemoteDriver.Component("http://localhost:9713/")  // SpaCy
                .withScale(iWorkers)
                .build().withTimeout(60));

        composer.add(new DUUIRemoteDriver.Component("http://localhost:9715/")  // Jina
                .withScale(iWorkers)
                .withParameter("type", "org.texttechnologylab.annotation.parliamentary.SpeechText")
                .build().withTimeout(60));

        /*composer.add(new DUUIUIMADriver.Component(createEngineDescription(XmiWriter.class,
                XmiWriter.PARAM_TARGET_LOCATION, "C:/test/temp",
                XmiWriter.PARAM_PRETTY_PRINT, true,
                XmiWriter.PARAM_OVERWRITE, true,
                XmiWriter.PARAM_VERSION, "1.1",
                XmiWriter.PARAM_COMPRESSION, "GZIP"))
                .build().withTimeout(60));*/

        composer.run(processor, "test");
    }

}
