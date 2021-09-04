package com.kichik.pecoff4j;

public class ImplMapTableRow {
    private int mappingFlags;
    private int memberForwarded;
    private int importName;
    private int importScope;

    public int getMappingFlags() {
        return mappingFlags;
    }

    public void setMappingFlags(int mappingFlags) {
        this.mappingFlags = mappingFlags;
    }

    public int getMemberForwarded() {
        return memberForwarded;
    }

    public void setMemberForwarded(int memberForwarded) {
        this.memberForwarded = memberForwarded;
    }

    public int getImportName() {
        return importName;
    }

    public void setImportName(int importName) {
        this.importName = importName;
    }

    public int getImportScope() {
        return importScope;
    }

    public void setImportScope(int importScope) {
        this.importScope = importScope;
    }
}
