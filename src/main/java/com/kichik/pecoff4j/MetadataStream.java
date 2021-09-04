package com.kichik.pecoff4j;

public class MetadataStream {
    private int reserved1;// { get; set; }
    private int majorVersion;// { get; set; }
    private int minorVersion;// { get; set; }
    private int offsetSizeFlags;// { get; set; } // indicates offset sizes to be used within the other streams.
    private int reserved2;// { get; set; } // Always set to 0x01 [01]
    private long tablesFlags;// { get; set; } // indicated which tables are present. 8 bytes.
    private long sortedTablesFlags;// { get; set; } // indicated which tables are sorted. 8 bytes.
    private int[] tableSizes;// { get; set; } // Size of each table. Array count will be same as # of '1's in TableFlags.

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        this.reserved1 = reserved1;
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

    public int getOffsetSizeFlags() {
        return offsetSizeFlags;
    }

    public void setOffsetSizeFlags(int offsetSizeFlags) {
        this.offsetSizeFlags = offsetSizeFlags;
    }

    public int getReserved2() {
        return reserved2;
    }

    public void setReserved2(int reserved2) {
        this.reserved2 = reserved2;
    }

    public long getTablesFlags() {
        return tablesFlags;
    }

    public void setTablesFlags(long tablesFlags) {
        this.tablesFlags = tablesFlags;
    }

    public long getSortedTablesFlags() {
        return sortedTablesFlags;
    }

    public void setSortedTablesFlags(long sortedTablesFlags) {
        this.sortedTablesFlags = sortedTablesFlags;
    }

    public int[] getTableSizes() {
        return tableSizes;
    }

    public void setTableSizes(int[] tableSizes) {
        this.tableSizes = tableSizes;
    }

    public boolean hasTable(long tableFlag) {
        return (tableFlag & tablesFlags) == tableFlag;
    }

    public boolean hasSortedTable(long tableFlag) {
        return (tableFlag & sortedTablesFlags) == tableFlag;
    }
}
