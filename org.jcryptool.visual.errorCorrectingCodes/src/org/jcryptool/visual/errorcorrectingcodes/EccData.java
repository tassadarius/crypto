package org.jcryptool.visual.errorcorrectingcodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.BitSet;
import java.util.List;

public class EccData {

    private String originalString;
    private String correctedString;
    private String binaryAsString;
    private String codeAsString;
    private String codeStringWithErrors;

    private int[][] matrixG;
    private int[][] matrixP;
    private int[][] matrixS;
    
    private List<BitSet> binary;
    private List<BitSet> encoded;
    private List<BitSet> errorCode;

    private PropertyChangeSupport pcs;

    public EccData() {
        originalString = "";
        binaryAsString = "";
        codeAsString = "";
        codeStringWithErrors = "";
        pcs = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        pcs.firePropertyChange(propertyName, oldValue, newValue);
    }

    public String getOriginalString() {
        return originalString;
    }

    public void setOriginalString(String originalString) {
        String oldText = this.originalString;
        this.originalString = originalString;
        pcs.firePropertyChange("originalString", oldText, originalString);
    }

    public String getBinaryAsString() {
        return binaryAsString;
    }

    public void setBinaryAsString(String binaryAsString) {
        String oldText = this.binaryAsString;
        this.binaryAsString = binaryAsString;
        pcs.firePropertyChange("binaryAsString", oldText, binaryAsString);
    }

    public String getCodeAsString() {
        return codeAsString;
    }

    public void setCodeAsString(String codeAsString) {
        String oldText = this.codeAsString;
        this.codeAsString = codeAsString;
        pcs.firePropertyChange("codeAsString", oldText, codeAsString);
    }

    public String getCodeStringWithErrors() {
        return codeStringWithErrors;
    }

    public void setCodeStringWithErrors(String codeStringWithErrors) {
        String oldText = this.codeStringWithErrors;
        this.codeStringWithErrors = codeStringWithErrors;
        pcs.firePropertyChange("codeStringWithErrors", oldText, codeStringWithErrors);
    }

    public List<BitSet> getBinary() {
        return binary;
    }

    public void setBinary(List<BitSet> binary) {
        this.binary = binary;
    }

    public List<BitSet> getEncoded() {
        return encoded;
    }

    public void setEncoded(List<BitSet> binaryCode) {
        this.encoded = binaryCode;
    }

    public List<BitSet> getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(List<BitSet> errorCode) {
        this.errorCode = errorCode;
    }

    public String getCorrectedString() {
        return correctedString;
    }

    public void setCorrectedString(String correctedString) {    
        String oldText = this.correctedString;
        this.correctedString = correctedString;
        pcs.firePropertyChange("correctedString", oldText, correctedString);
    }

    public int[][] getMatrixG() {
        return matrixG;
    }

    public void setMatrixG(int[][] matrixG) {
        this.matrixG = matrixG;
    }

    public int[][] getMatrixP() {
        return matrixP;
    }

    public void setMatrixP(int[][] matrixP) {
        this.matrixP = matrixP;
    }

    public int[][] getMatrixS() {
        return matrixS;
    }

    public void setMatrixS(int[][] matrixS) {
        this.matrixS = matrixS;
    }    
    
    
}
