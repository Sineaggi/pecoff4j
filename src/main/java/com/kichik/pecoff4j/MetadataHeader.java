package com.kichik.pecoff4j;

public class MetadataHeader {
    private int signature;
    private int majorVersion; // short
    private int minorVersion; // short
    private int reserved1; // int, 0
    private int versionStringLength; // string length
    private String versionString; // todo:
    private int flags;
    private int numberOfStreams;

    public int getSignature() {
        return signature;
    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        this.reserved1 = reserved1;
    }

    public int getVersionStringLength() {
        return versionStringLength;
    }

    public void setVersionStringLength(int versionStringLength) {
        this.versionStringLength = versionStringLength;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getNumberOfStreams() {
        return numberOfStreams;
    }

    public void setNumberOfStreams(int numberOfStreams) {
        this.numberOfStreams = numberOfStreams;
    }
}


