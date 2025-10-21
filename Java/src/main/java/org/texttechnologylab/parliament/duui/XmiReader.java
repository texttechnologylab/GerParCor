package org.texttechnologylab.parliament.duui;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.javaync.io.AsyncFiles;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.AsyncCollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.io.DUUICollectionReader;
import org.texttechnologylab.DockerUnifiedUIMAInterface.monitoring.AdvancedProgressMeter;
import org.texttechnologylab.utilities.helper.StringUtils;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class XmiReader implements DUUICollectionReader {

    private String _path;
    private ConcurrentLinkedQueue<String> _filePaths;
    private ConcurrentLinkedQueue<String> _filePathsBackup;
    private ConcurrentLinkedQueue<ByteReadFuture> _loadedFiles;

    private String _viewName;

    private int _initialSize;
    private AtomicInteger _docNumber;
    private long _maxMemory;
    private AtomicLong _currentMemorySize;

    private boolean _addMetadata = true;

    private String _targetPath = null;

    private String _language = null;

    private AdvancedProgressMeter progress = null;

    private int debugCount = 25;

    private String targetLocation = null;

    public XmiReader(String folder, String ending) {
        this(folder, ending, "_InitialView", 25, getRandomFromMode(null, -1), getSortFromMode(null), "", true, null, 0, "", null);
    }

    public XmiReader(String folder, String ending, String viewName) {
        this(folder, ending, viewName, 25, getRandomFromMode(null, -1), getSortFromMode(null), "", true, null, 0, "", null);
    }

    public XmiReader(String folder, String ending, String viewName, int debugCount, int iRandom, boolean bSort, String savePath, boolean bAddMetadata, String language, int skipSmallerFiles, String targetLocation, String targetEnding) {
        this.targetLocation = targetLocation;
        _addMetadata = bAddMetadata;
        _language = language;
        _filePaths = new ConcurrentLinkedQueue<>();
        _loadedFiles = new ConcurrentLinkedQueue<>();
        _filePathsBackup = new ConcurrentLinkedQueue<>();
        _viewName = viewName;

        if (new File(savePath).exists() && savePath.length() > 0) {
            File sPath = new File(savePath);

            String sContent = null;
            try {
                sContent = StringUtils.getContent(sPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] sSplit = sContent.split("\n");

            for (String s : sSplit) {
                _filePaths.add(s);
            }

        } else {
            File fl = new File(folder);
            if (!fl.isDirectory()) {
                throw new RuntimeException("The folder is not a directory!");
            }


            _path = folder;
            addFilesToConcurrentList(fl, ending, _filePaths);

            if (skipSmallerFiles > 0) {
                _filePaths = skipBySize(_filePaths, skipSmallerFiles);
            }
        }


        if (skipSmallerFiles > 0) {
            _filePaths = skipBySize(_filePaths, skipSmallerFiles);
        }

        if (bSort) {
            _filePaths = sortBySize(_filePaths);
        }

        if (bSort && iRandom > 0) {
            System.out.println("Sorting and Random Selection is active, using the " + (iRandom > 0 ? "largest " : "smallest ") + Math.abs(iRandom) + " documents.");
//            _filePaths = takeFirstOrLast(_filePaths, iRandom);
        } else if (iRandom > 0) {
            _filePaths = random(_filePaths, iRandom);
        }

        if (savePath.length() > 0) {
            File nFile = new File(savePath);

            if (!nFile.exists()) {
                StringBuilder sb = new StringBuilder();
                _filePaths.forEach(f -> {
                    if (sb.length() > 0) {
                        sb.append("\n");
                    }
                    sb.append(f);
                });
                try {
                    StringUtils.writeContent(sb.toString(), nFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // remove files that are already in the target location
        // NOTE we do this after saving the file list, as we do not want to change anything but only avoid processing files multiple times
        if (this.targetLocation != null) {
            // _filePaths = removeIfInTarget(_filePaths, this.targetLocation, targetEnding, this._path, ending);
        }

        _filePathsBackup.addAll(_filePaths);

        this.debugCount = debugCount;

        System.out.printf("Found %d files matching the pattern! \t Using Random: %d\n", _filePaths.size(), iRandom);
        _initialSize = _filePaths.size();
        _docNumber = new AtomicInteger(0);
        _currentMemorySize = new AtomicLong(0);
        // 500 MB
        _maxMemory = 500 * 1024 * 1024;

        progress = new AdvancedProgressMeter(_initialSize);
    }

    private static int getRandomFromMode(AsyncCollectionReader.DUUI_ASYNC_COLLECTION_READER_SAMPLE_MODE sampleMode, int sampleSize) {
        if (sampleMode == AsyncCollectionReader.DUUI_ASYNC_COLLECTION_READER_SAMPLE_MODE.SMALLEST) {
            return sampleSize * -1;
        }
        return sampleSize;
    }

    private static boolean getSortFromMode(AsyncCollectionReader.DUUI_ASYNC_COLLECTION_READER_SAMPLE_MODE mode) {
        if (mode == AsyncCollectionReader.DUUI_ASYNC_COLLECTION_READER_SAMPLE_MODE.RANDOM) {
            return false;
        }
        return true;
    }

    public static void addFilesToConcurrentList(File folder, String ending, ConcurrentLinkedQueue<String> paths) {
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                if (listOfFiles[i].getName().endsWith(ending)) {
                    paths.add(listOfFiles[i].getPath().toString());
                }
            } else if (listOfFiles[i].isDirectory()) {
                addFilesToConcurrentList(listOfFiles[i], ending, paths);
            }
        }

    }

    public static ConcurrentLinkedQueue<String> sortBySize(ConcurrentLinkedQueue<String> paths) {

        ConcurrentLinkedQueue<String> rQueue = new ConcurrentLinkedQueue<String>();

        rQueue.addAll(paths.stream().sorted((s1, s2) -> {
            Long firstLength = new File(s1).length();
            Long secondLength = new File(s2).length();

            return firstLength.compareTo(secondLength) * -1;
        }).collect(Collectors.toList()));

        return rQueue;

    }

    /**
     * Skips files smaller than skipSmallerFiles
     *
     * @param paths            paths to files
     * @param skipSmallerFiles skip files smaller than this value in bytes
     * @return filtered paths to files
     */
    public static ConcurrentLinkedQueue<String> skipBySize(ConcurrentLinkedQueue<String> paths, int skipSmallerFiles) {
        ConcurrentLinkedQueue<String> rQueue = new ConcurrentLinkedQueue<>();

        System.out.println("Skip files smaller than " + skipSmallerFiles + " bytes");
        System.out.println("  Number of files before skipping: " + paths.size());

        rQueue.addAll(paths
                .stream()
                .filter(s -> new File(s).length() >= skipSmallerFiles)
                .collect(Collectors.toList())
        );

        System.out.println("  Number of files after skipping: " + rQueue.size());

        return rQueue;
    }

    public static ConcurrentLinkedQueue<String> random(ConcurrentLinkedQueue<String> paths, int iRandom) {

        ConcurrentLinkedQueue<String> rQueue = new ConcurrentLinkedQueue<String>();

        Random nRandom = new Random(iRandom);

        ArrayList<String> sList = new ArrayList<>();
        sList.addAll(paths);

        Collections.shuffle(sList, nRandom);

        if (iRandom > sList.size()) {
            rQueue.addAll(sList.subList(0, sList.size()));
        } else {
            rQueue.addAll(sList.subList(0, iRandom));
        }


        return rQueue;

    }


    public static String getSize(String sPath) {
        return FileUtils.byteCountToDisplaySize(new File(sPath).length());
    }

    @Override
    public AdvancedProgressMeter getProgress() {
        return this.progress;
    }

    @Override
    public void getNextCas(JCas empty) {
        ByteReadFuture future = _loadedFiles.poll();

        byte[] bFile = null;
        String result = null;
        if (future == null) {
            result = _filePaths.poll();
            if (result == null) return;
        } else {
            result = future.getPath();
            bFile = future.getBytes();
            long factor = 1;
            if (result.endsWith(".gz") || result.endsWith(".xz")) {
                factor = 10;
            }
            _currentMemorySize.getAndAdd(-factor * (long) bFile.length);
        }
        int val = _docNumber.addAndGet(1);

        progress.setDone(val);
        progress.setLeft(_initialSize - val);

        if (_initialSize - progress.getCount() > debugCount) {
            if (val % debugCount == 0 || val == 0) {
                System.out.printf("%s: \t %s \t %s\n", progress, getSize(result), result);
            }
        } else {
            System.out.printf("%s: \t %s \t %s\n", progress, getSize(result), result);
        }

        if (bFile == null) {
            try {
                bFile = Files.readAllBytes(Path.of(result));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            /*
            if (result.endsWith(".xz")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                decodedFile = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.XZ, new ByteArrayInputStream(file));
            } else if (result.endsWith(".gz")) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                decodedFile = new CompressorStreamFactory().createCompressorInputStream(CompressorStreamFactory.GZIP, new ByteArrayInputStream(file));
            } else {
                decodedFile = new ByteArrayInputStream(file);
            }
            */

            empty.reset();

            var parts = result.split("\\.");
            String fileExtension = parts[parts.length - 1];

            File fFile = new File(result);
            String mimeType = Files.probeContentType(fFile.toPath());


            if(mimeType == null){
                if(fileExtension.equals("xmi")){
                    mimeType = "application/xmi";
                }
            }

            System.out.println(mimeType);

            InputStream decodedFile = new FileInputStream(fFile.getPath());
            XmiCasDeserializer.deserialize(decodedFile, empty.getCas(), true);

        } catch (Exception e) {
            e.printStackTrace();
        }


        if (_addMetadata) {
            if (JCasUtil.select(empty, DocumentMetaData.class).size() == 0) {
                DocumentMetaData dmd = DocumentMetaData.create(empty);
                File pFile = new File(result);
                dmd.setDocumentId(pFile.getName());
                dmd.setDocumentTitle(pFile.getName());
                dmd.setDocumentUri(pFile.getAbsolutePath());
                dmd.addToIndexes();
            }
        }

        if (_language != null && !_language.isEmpty()) {
            empty.setDocumentLanguage(_language);
        }

    }

    public void reset() {
        _filePaths = _filePathsBackup;
        _docNumber.set(0);
        progress = new AdvancedProgressMeter(_initialSize);
    }

    @Override
    public boolean hasNext() {
        return _filePaths.size() > 0;
    }

    @Override
    public long getSize() {
        return _filePaths.size();
    }

    public CompletableFuture<Integer> getAsyncNextByteArray() throws IOException, CompressorException, SAXException {
        String result = _filePaths.poll();
        if (result == null) return CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> val = AsyncFiles
                .readAllBytes(Paths.get(result), 1024 * 1024 * 5)
                .thenApply(bytes -> {
                    _loadedFiles.add(new ByteReadFuture(result, bytes));

                    //Calculate estimated unpacked size by using a compression ratio of 0.1
                    long factor = 1;
                    if (result.endsWith(".gz") || result.endsWith(".xz")) {
                        factor = 10;
                    }
                    _currentMemorySize.getAndAdd(factor * (long) bytes.length);
                    return 0;
                });
        return val;
    }

    @Override
    public long getDone() {
        return _docNumber.get();
    }

    public String formatSize(long lSize) {

        int u = 0;
        for (; lSize > 1024 * 1024; lSize >>= 10) {
            u++;
        }
        if (lSize > 1024)
            u++;
        return String.format("%.1f %cB", lSize / 1024f, " kMGTPE".charAt(u));

    }

    public enum DUUI_ASYNC_COLLECTION_READER_SAMPLE_MODE {
        RANDOM,
        SMALLEST,
        LARGEST
    }

    private String readFile(File file) throws FileNotFoundException {
        String result = "";
        Scanner myReader = new Scanner(file);
        while (myReader.hasNextLine()) {
            if(result == ""){
                result = myReader.nextLine();
            }else{
                result += "\n" + myReader.nextLine();
            }
        }

        return result;
    }

    class ByteReadFuture {
        private String _path;
        private byte[] _bytes;

        public ByteReadFuture(String path, byte[] bytes) {
            _path = path;
            _bytes = bytes;
        }

        public String getPath() {
            return _path;
        }

        public byte[] getBytes() {
            return _bytes;
        }
    }
}