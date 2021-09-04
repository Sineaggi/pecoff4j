package com.kichik.pecoff4j;

public class MethodDefTableRow {
    private int rva;
    private int implFlags;
    private int flags;
    private int name;
    private int signature;
    private int paramList;

    public int getRva() {
        return rva;
    }

    public void setRva(int rva) {
        this.rva = rva;
    }

    public int getImplFlags() {
        return implFlags;
    }

    public void setImplFlags(int implFlags) {
        this.implFlags = implFlags;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getSignature() {
        return signature;
    }

    public void setSignature(int signature) {
        this.signature = signature;
    }

    public int getParamList() {
        return paramList;
    }

    public void setParamList(int paramList) {
        this.paramList = paramList;
    }
}
