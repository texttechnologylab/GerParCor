package org.texttechnologylab.parliament.duui;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.util.CasCopier;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategy;
import org.texttechnologylab.DockerUnifiedUIMAInterface.segmentation.DUUISegmentationStrategyByDelemiter;
import org.texttechnologylab.annotation.AnnotationComment;

import java.util.HashSet;
import java.util.Set;

public class DUUISegmentationStrategyByDelemiterModified extends DUUISegmentationStrategy {

    private int iLength = 500000;
    private String sDelemiter;

    private Set<String> currentOffset = new HashSet<>();

    private JCas emptyCas = null;

    private int iOverlap = 0;

    private boolean bDebug = false;

    public DUUISegmentationStrategyByDelemiterModified() {
        super();
    }

    public DUUISegmentationStrategyByDelemiterModified withLength(int iLength){
        this.iLength=iLength;
        return this;
    }

    public DUUISegmentationStrategyByDelemiterModified withDebug() {
        this.bDebug = true;
        return this;
    }

    public boolean hasDebug() {
        return this.bDebug;
    }

    public DUUISegmentationStrategyByDelemiterModified withOverlap(int iOverlap) {
        this.iOverlap = iOverlap;
        return this;
    }

    public int getSegments(){
        return this.currentOffset.size();
    }

    public DUUISegmentationStrategyByDelemiterModified withDelemiter(String sValue){
        this.sDelemiter=sValue;
        return this;
    }


    @Override
    public JCas getNextSegment() {
        emptyCas.reset();

        if(currentOffset.size()==0){
            return null;
        }

        String sOffset = currentOffset.stream().findFirst().get();
        String[] sSplit = sOffset.split("-");
        int iStart = Integer.valueOf(sSplit[0]);
        int iEnde = Integer.valueOf(sSplit[1]);

        emptyCas.setDocumentText(jCasInput.getDocumentText().substring(iStart, iEnde));
        emptyCas.setDocumentLanguage(jCasInput.getDocumentLanguage());


        AnnotationComment da = new AnnotationComment(emptyCas);
        da.setKey("offset");
        da.setValue(""+iStart);
        da.addToIndexes();

        currentOffset.remove(sOffset);

        return emptyCas;
    }

    @Override
    protected void initialize() throws UIMAException {
        this.emptyCas = JCasFactory.createJCas();

        if (iOverlap > iLength) {
            System.err.println("Overlap: " + iOverlap + " is > Segmenting-Length: " + iLength);
        }

        String sText = this.jCasInput.getDocumentText();

        int tLength = sText.length();

        int iCount = 0;

        while((iCount+iLength)<tLength){

            String sSubText = "";
            if(tLength>(int) (iCount+this.iLength)) {
                sSubText = sText.substring(iCount, (int) (iCount + this.iLength));
            }
            else{
                sSubText = sText;
            }
            int iLastPoint = sSubText.lastIndexOf(this.sDelemiter);
//            System.out.println(iLastPoint);
            sSubText = sSubText.substring(0, iLastPoint>0 ? (iLastPoint+1) : sSubText.length());
//            System.out.println(sSubText);
            currentOffset.add(iCount+"-"+(sSubText.length()+iCount));

//            System.out.println(iCount+"\t"+sSubText.length());
            iCount = iCount+sSubText.length();

            if (iOverlap > 0) {
                if (iCount + iOverlap < tLength) {
                    int iStartOverlap = iCount - iOverlap;
                    int iEndOverlap = iCount + iOverlap;

                    String overlabSubString = sText.substring(iStartOverlap, iEndOverlap);
                    int iOStart = overlabSubString.indexOf(".") + 1;
                    int iOEnd = overlabSubString.lastIndexOf(".") + 1;
                    overlabSubString = overlabSubString.substring(iOStart, iOEnd);
                    currentOffset.add((iStartOverlap + iOStart) + "-" + (iStartOverlap + iOEnd));
//                    System.out.println(overlabSubString);
                }
            }


        }
        if(iCount<tLength){
            currentOffset.add(iCount+"-"+tLength);
        }

//        currentOffset.stream().forEach(co->{
//            System.out.println(co);
//        });
    }

    @Override
    public void merge(JCas jCasSegment) {
        int iOffset;
        AnnotationComment offset = JCasUtil.select(jCasSegment, AnnotationComment.class).stream().filter(ac->{
            return ac.getKey().equalsIgnoreCase("offset");
        }).findFirst().get();

        if(offset!=null){
            iOffset = Integer.valueOf(offset.getValue());
        } else {
            iOffset = 0;
        }

        Set<AnnotationComment> removeSet = new HashSet<>();
        JCasUtil.select(jCasSegment, AnnotationComment.class).stream().filter(f->{
            return f.getKey().equalsIgnoreCase("offset");
        }).forEach(f->{
            removeSet.add(f);
        });

        for (AnnotationComment annotationComment : removeSet) {
            annotationComment.removeFromIndexes();
        }


        if(iOffset>0) {
//            System.out.println("Offset: "+iOffset);
            JCasUtil.select(jCasSegment, Annotation.class).stream().forEach(a -> {
                a.setBegin(a.getBegin() + iOffset);
                a.setEnd(a.getEnd() + iOffset);
            });
        }
        CasCopier.copyCas(jCasSegment.getCas(), jCasInput.getCas(), false);
    }

    @Override
    public void finalize(JCas jCas) {
//        System.out.println("Finish");
    }
}
