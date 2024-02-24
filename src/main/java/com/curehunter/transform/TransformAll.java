package com.curehunter.transform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.curehunter.utils.FileIterator;
import com.curehunter.utils.LocalCacheEntityResolver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "transform")
public class TransformAll implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help and exit")
    boolean help;

    @Option(names = { "-i", "--in" }, description = "directory to search for files, default: ./data")
    private Path inputDir = Paths.get(System.getProperty("user.dir")+"/data");

    @Option(names = { "-f", "--filter" }, description = "filter files with wildcard pattern, default: *.xml.gz", defaultValue = "*.xml.gz")
    private String filterPattern;

    @Option(names = { "-o", "--out" }, description = "output files to directory, default: ./output")
    private Path outputDir = Paths.get(System.getProperty("user.dir")+"/output");

    @Option(names = { "-x", "--xsl" }, description = "XSL file to use for transformation, default: ./xsl/medlineCitationTSV.xsl")
    private Path xslFile = Paths.get(System.getProperty("user.dir")+"/xsl/medlineCitationTSV.xsl");

    @Option(names = { "-d", "--dtd" }, description = "local copies of DTDs referenced in XML to avoid remote lookup, default: ./dtd")
    private Path dtdDir = Paths.get(System.getProperty("user.dir")+"/dtd");

    @Option(names = { "-e", "--outExt" }, description = "extension to append to output files, default: .out", defaultValue = ".out")
    private String outputExtension;

    @Option(names = { "-w", "--workers" }, description = "number of worker threads, files to process simultaneously, default: 4", defaultValue = "4")
    private int workerThreads;

    @Override
    public Integer call() {
        long startTime = System.currentTimeMillis();
        FileIterator workerData = new FileIterator(inputDir.toFile());
        WildcardFileFilter filter = new WildcardFileFilter(filterPattern);
        try {
            System.out.println("in=" + inputDir.toFile().getCanonicalPath() + " out="
                    + outputDir.toFile().getCanonicalPath()
                    + " xsl=" + xslFile.toFile().getCanonicalPath()
                    + " dtd=" + dtdDir.toFile().getCanonicalPath()
                    + " workers=" + workerThreads
                    + " filter=" + filterPattern
                    + " outputExtension=" + outputExtension);
            ExecutorService exec = Executors.newFixedThreadPool(workerThreads);
            for (int n = 0; n < workerThreads; n++) {
                exec.execute(new Worker(workerData, xslFile.toFile(), dtdDir.toFile(), outputDir.toFile(), 
                        outputExtension, filter));
            }
            exec.shutdown();
            exec.awaitTermination(1, TimeUnit.DAYS);
            System.out.println("Processing complete in " + (System.currentTimeMillis() - startTime) + "ms");
            return 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Processing complete in " + (System.currentTimeMillis() - startTime) + "ms");
        return 1;
    }

    static class Worker implements Runnable {
        FileIterator workerData;
        File xslt;
        File dtdDir;
        File outputDir;
        String outputExtension;
        WildcardFileFilter filter;

        public Worker(FileIterator workerData, File xslt, File dtdDir, File outputDir, String outputExtension,
                WildcardFileFilter filter) {
            this.workerData = workerData;
            this.xslt = xslt;
            this.dtdDir = dtdDir;
            this.outputDir = outputDir;
            this.outputExtension = outputExtension;
            this.filter = filter;
        }

        public void run() {
            SAXParserFactory parserFactory = SAXParserFactory.newInstance();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            System.out.println("transformerFactory=" + transformerFactory + " parserFactory=" + parserFactory);
            try {
                SAXParser saxParser = parserFactory.newSAXParser();
                XMLReader xmlReader = saxParser.getXMLReader();
                xmlReader.setEntityResolver(new LocalCacheEntityResolver(dtdDir));
                Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslt));
                File f = null;
                while ((f = workerData.next()) != null) {
                    if (filter.accept(f)) {
                        System.out.println("start transforming file=" + f.getCanonicalPath());
                        try {
                            SAXSource saxSource = new SAXSource(xmlReader, new InputSource(
                            f.getCanonicalPath().toLowerCase().endsWith(".gz")
                                            ? new GZIPInputStream(new FileInputStream(f))
                                            : new FileInputStream(f)));
                            transformer.transform(saxSource,
                                    new StreamResult(new File(outputDir, f.getName() + outputExtension)));
                            System.out.println("done transforming file=" + f.getCanonicalPath());
                        } catch (Throwable e) {
                            System.err.printf("error transforming file=" + f.getCanonicalPath(), e);
                            e.printStackTrace();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
