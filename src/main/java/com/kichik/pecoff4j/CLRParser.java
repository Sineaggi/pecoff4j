package com.kichik.pecoff4j;

import com.kichik.pecoff4j.io.DataReader;
import com.kichik.pecoff4j.io.IDataReader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CLRParser {
    public static Metadata parseCLRMetadata(PE pe) throws IOException {
        CLRRuntimeHeader clr = pe.getImageData().getClrRuntimeHeader();
        int address = clr.getMetaDataDirectoryAddress();
        int size = clr.getMetaDataDirectorySize();
        int offset = pe.getSectionTable().getRVAConverter().convertVirtualAddressToRawDataPointer(address);
        SectionHeader sh = pe.getSectionTable().findHeader(".text");
        SectionData sb = pe.getSectionTable().findSection(".text");
        DataReader dr = new DataReader(sb.getData(), offset - sh.getPointerToRawData(), size);
        MetadataHeader mdh = new MetadataHeader();
        mdh.setSignature(dr.readDoubleWord());
        mdh.setMajorVersion(dr.readWord());
        mdh.setMinorVersion(dr.readWord());
        mdh.setReserved1(dr.readDoubleWord());
        mdh.setVersionStringLength(dr.readDoubleWord());
        mdh.setVersionString(dr.readUtf(mdh.getVersionStringLength())); // or is this unicode?
        mdh.setFlags(dr.readWord());
        mdh.setNumberOfStreams(dr.readWord());
        List<StreamHeader> streamHeaders = new ArrayList<StreamHeader>();
        for (int i = 0; i < mdh.getNumberOfStreams(); i++) {
            StreamHeader streamHeader = new StreamHeader();
            streamHeader.setOffset(dr.readDoubleWord());
            streamHeader.setSize(dr.readDoubleWord());
            int currentPosition = dr.getPosition();
            streamHeader.setName(dr.readUtf()); // or is this unicode?

            int strLength = dr.getPosition() - currentPosition;
            if (strLength % 4 > 0) {
                dr.skipBytes(4 - (strLength % 4));
            }
            streamHeaders.add(streamHeader);
        }

        //Map<String, byte[]> moba = new HashMap<>();
        Metadata md = null;
        StringsStream stringsStream = null;
        for (StreamHeader st : streamHeaders) {
            //dr.jumpTo(st.getOffset());
            //st.getOffset();
            switch (st.getName()) {
                case "#~":
                case "#-": {
                    byte[] header = parseHeader(st, dr);
                    DataReader dr2 = new DataReader(header);
                    MetadataStream mds = mds(dr2);
                    md = md(dr2, mds);
                    break;
                }
                case "#Strings": {
                    byte[] strings = parseStrings(st, dr);
                    stringsStream = new StringsStream(strings);
                    break;
                }
                case "#US": {
                    byte[] us = parseUS(st, dr);
                    // moba.put(st.getName(), us);
                    break;
                }
                case "#Blob": {
                    byte[] blob = parseUnknown(st, dr);
                    // moba.put(st.getName(), us);
                    break;
                }
                case "#GUID": {
                    byte[] guid = parseGuid(st, dr);
                    //moba.put(st.getName(), guid);

                    //int pm3 = indexOf(guid, "PeekMessageA".getBytes(StandardCharsets.UTF_8));
                    //if (pm3 != -1) {
                    //    System.out.println(pm3);
                    //}
                    break;
                }
                default: {
                    throw new RuntimeException("Unknown metadata stream " + st.getName());
                }
            }
            // finish reading any missed input, also will throw if we've previously read too far.
            dr.jumpTo(st.getOffset() + st.getSize());
        }
        if (size != dr.getPosition()) {
            throw new RuntimeException("Failed to read all bytes, size of clr " + size + " != bytes read " + dr.getPosition());
        }

        md.stringsStream = stringsStream;

        //StreamHeader stringsStreamHeader = Objects.requireNonNull(
        //        findStringsStream(streamHeaders), "#Strings header must exist");
        //System.out.println(stringsStreamHeader.getOffset());

        //dr = new DataReader(sb.getData(), offset - sh.getPointerToRawData(), size);
        //dr.jumpTo(stringsStreamHeader.getOffset());
        //byte[] strings = new byte[stringsStreamHeader.getSize()];

        //int pm = indexOf(strings, "PeekMessageA".getBytes(StandardCharsets.UTF_8));
        //System.out.println(pm);

        //int pm2 = indexOf(allb, "PeekMessageA".getBytes(StandardCharsets.UTF_8));
        //System.out.println(pm2);

        //dr.read(strings);
        //StringsStream stringsStream = new StringsStream(strings);
        //dr = new DataReader(sb.getData(), offset - sh.getPointerToRawData(), size);
        // todo: read until stringsStreamHeader.getSize();
        //Map<Integer, String> offsetToStrings = new HashMap<>();
        //List<String> strings = new ArrayList<>();
        //List<StringOffset> strings2 = new ArrayList<>();
        //for (int i = dr.getPosition(); i < stringsStreamHeader.getOffset() + stringsStreamHeader.getSize(); i = dr.getPosition()) {
        //    //int stringOffset = (stringsStreamHeader.getOffset() - dr.getPosition());
        //    int stringOffset = dr.getPosition() - stringsStreamHeader.getOffset();
        //    String hex = String.format("%02X", stringOffset);
        //    String str = dr.readUtf();
        //    strings.add(str);
        //    strings2.add(new StringOffset(stringOffset, str));
        //    offsetToStrings.put(stringOffset, str);
        //}
        //int HRESULT_offset = stringsStream.get();
        //int win32found = 0;
        //for (Map.Entry<Integer, String> integerStringEntry : offsetToStrings.entrySet()) {
        //    if (integerStringEntry.getValue().equals("HRESULT")) {
        //        HRESULT_offset = integerStringEntry.getKey();
        //    }
        //    if (integerStringEntry.getValue().equals("Windows.Win32.Foundation")) {
        //        win32found = integerStringEntry.getKey();
        //    }
        //}
        //System.out.println(strings);
        //StringOffset last1 = strings2.get(strings2.size() - 1);
        //StringOffset last2 = strings2.get(strings2.size() - 2);
        //StringOffset last3 = strings2.get(strings2.size() - 3);
        //StringOffset last4 = strings2.get(strings2.size() - 4);
        //StringOffset last5 = strings2.get(strings2.size() - 5);
        //StringOffset last6 = strings2.get(strings2.size() - 6);
        //System.out.println(last1);

        // todo: maybe don't read like this
        //dr = new DataReader(sb.getData(), offset - sh.getPointerToRawData(), size);

        //StreamHeader metadataStreamHeader = Objects.requireNonNull(findMetadataStream(streamHeaders), "#~ or #- header must exist");
        //System.out.println(metadataStreamHeader.getOffset());
        //dr.jumpTo(metadataStreamHeader.getOffset()); // should be noop?
        //dr = new DataReader(pe.getSectionTable().getSection(0).getData());
        //dr.jumpTo(pe.getSectionTable().getRVAConverter().convertVirtualAddressToRawDataPointer(metadataStreammm.getOffset()) - dataoffset);
        //byte[] mdby = new byte[metadataStreamHeader.getSize()];
        //dr.read(mdby);
        //dr = new DataReader(mdby);


        //MetadataStream mds = mds(dr);
        //Metadata md = md(dr, mds);
        Objects.requireNonNull(md);

        return md;
    }

    static MetadataStream mds(DataReader dr) throws IOException {

        MetadataStream metadataStream = new MetadataStream();
        metadataStream.setReserved1(dr.readDoubleWord());
        metadataStream.setMajorVersion(dr.readByte());
        metadataStream.setMinorVersion(dr.readByte());
        metadataStream.setOffsetSizeFlags(dr.readByte());
        metadataStream.setReserved2(dr.readByte());
        metadataStream.setTablesFlags(dr.readLong());
        metadataStream.setSortedTablesFlags(dr.readLong());
        // todo: continue here https://codingwithspike.wordpress.com/2012/09/01/building-a-net-disassembler-part-4-reading-the-metadata-tables-in-the-stream/

        return metadataStream;
    }

    static MD getMDfromIndex(int index) {
        switch (index)
        {
            case 0x00: return MD.module;
            case 0x01: return MD.typeRef;
            case 0x02: return MD.typeDef;
            case 0x04: return MD.field;
            case 0x06: return MD.methodDef;
            case 0x08: return MD.param;
            case 0x09: return MD.interfaceImpl;
            case 0x0a: return MD.memberRef;
            case 0x0b: return MD.constant;
            case 0x0c: return MD.customAttribute;
            case 0x0d: return MD.fieldMarshal;
            case 0x0e: return MD.declSecurity;
            case 0x0f: return MD.classLayout;
            case 0x10: return MD.fieldLayout;
            case 0x11: return MD.standAloneSig;
            case 0x12: return MD.eventMap;
            case 0x14: return MD.event;
            case 0x15: return MD.propertyMap;
            case 0x17: return MD.property;
            case 0x18: return MD.methodSemantics;
            case 0x19: return MD.methodImpl;
            case 0x1a: return MD.moduleRef;
            case 0x1b: return MD.typeSpec;
            case 0x1c: return MD.implMap;
            case 0x1d: return MD.fieldRVA;
            case 0x20: return MD.assembly;
            case 0x21: return MD.assemblyProcessor;
            case 0x22: return MD.assemblyOS;
            case 0x23: return MD.assemblyRef;
            case 0x24: return MD.assemblyRefProcessor;
            case 0x25: return MD.assemblyRefOS;
            case 0x26: return MD.file;
            case 0x27: return MD.exportedType;
            case 0x28: return MD.manifestResource;
            case 0x29: return MD.nestedClass;
            case 0x2a: return MD.genericParam;
            case 0x2b: return MD.methodSpec;
            case 0x2c: return MD.genericParamConstraint;
            default: return MD.unknown;
        }
    }

    static Metadata md(DataReader dr, MetadataStream metadataStream) throws IOException {

        long tablesFlags = metadataStream.getTablesFlags();
        int n = Long.bitCount(tablesFlags);
        int[] tableSizes = new int[n];
        for (int i = 0; i < n; i++) {
            tableSizes[i] = dr.readDoubleWord();
        }
        metadataStream.setTableSizes(tableSizes);

        Map<MD, Integer> rowCounts = new HashMap<>();

        int tableIndex = 0;
        for (int i = 0; i < 64; i++) {
            if ((tablesFlags & (1L << i)) != 0)
            {
                rowCounts.put(getMDfromIndex(i), tableSizes[tableIndex++]);
            }
        }

        int bits = metadataStream.getOffsetSizeFlags();
        int stringIndexSize = (bits & 0x01) == 0x01 ? 4 : 2;
        int guidIndexSize = (bits & 0x02) == 0x02 ? 4 : 2;
        int blobIndexSize = (bits & 0x04) == 0x04 ? 4 : 2;

        int typeDefOrRefIndexSize
                = compositeIndexSize(rowCounts.get(MD.typeDef), rowCounts.get(MD.typeRef), rowCounts.get(MD.typeSpec));
        int hasConstantIndexSize
                = compositeIndexSize(rowCounts.get(MD.field), rowCounts.get(MD.param), rowCounts.get(MD.property));
        int hasCustomAttributeIndexSize
                = compositeIndexSize(rowCounts.get(MD.methodDef), rowCounts.get(MD.field), rowCounts.get(MD.typeRef),
                rowCounts.get(MD.typeDef), rowCounts.get(MD.param), rowCounts.get(MD.interfaceImpl),
                rowCounts.get(MD.memberRef), rowCounts.get(MD.module), rowCounts.get(MD.property),
                rowCounts.get(MD.event), rowCounts.get(MD.standAloneSig), rowCounts.get(MD.moduleRef),
                rowCounts.get(MD.typeSpec), rowCounts.get(MD.assembly), rowCounts.get(MD.assemblyRef),
                rowCounts.get(MD.file), rowCounts.get(MD.exportedType), rowCounts.get(MD.manifestResource),
                rowCounts.get(MD.genericParam), rowCounts.get(MD.genericParamConstraint),
                rowCounts.get(MD.methodSpec));
        int hasFieldMarshalIndexSize
                = compositeIndexSize(rowCounts.get(MD.field), rowCounts.get(MD.param));
        int hasDeclSecurityIndexSize
                = compositeIndexSize(rowCounts.get(MD.typeDef), rowCounts.get(MD.methodDef), rowCounts.get(MD.assembly));
        int memberRefParentIndexSize
                = compositeIndexSize(rowCounts.get(MD.typeDef), rowCounts.get(MD.typeRef), rowCounts.get(MD.moduleRef),
                rowCounts.get(MD.methodDef), rowCounts.get(MD.typeSpec));
        int hasSemanticsIndexSize
                = compositeIndexSize(rowCounts.get(MD.event), rowCounts.get(MD.property));
        int methodDefOrRefIndexSize
                = compositeIndexSize(rowCounts.get(MD.methodDef), rowCounts.get(MD.memberRef));
        int memberForwardedIndexSize
                = compositeIndexSize(rowCounts.get(MD.field), rowCounts.get(MD.methodDef));
        int implementationIndexSize
                = compositeIndexSize(rowCounts.get(MD.file), rowCounts.get(MD.assemblyRef), rowCounts.get(MD.exportedType));
        int customAttributeTypeIndexSize
                = compositeIndexSize(rowCounts.get(MD.methodDef), rowCounts.get(MD.memberRef), 0, 0, 0);
        int resolutionScopeIndexSize
                = compositeIndexSize(rowCounts.get(MD.module), rowCounts.get(MD.moduleRef),
                rowCounts.get(MD.assemblyRef),  rowCounts.get(MD.typeRef));
        int typeOrMethodDefIndexSize
                = compositeIndexSize(rowCounts.get(MD.typeDef), rowCounts.get(MD.methodDef));

        DataReaderWrapper drw = new DataReaderWrapper(dr);

        //for (ModuleTableRow moduleTableRow : md.getModuleTableRows()) {
        //    System.out.println("tater");
        //    System.out.println(stringsStream.get(moduleTableRow.getName()));
        //}


        int tableSizesIndex = 0;
        //IntMap intMap = new IntMap();
        Metadata md = new Metadata();
        if (metadataStream.hasTable(MetadataTableFlags.Module)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<ModuleTableRow> moduleTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                ModuleTableRow mtr = new ModuleTableRow();
                mtr.setGeneration(dr.readWord());
                mtr.setName(drw.read(stringIndexSize));
                mtr.setMvid(drw.read(guidIndexSize));
                mtr.setEncId(drw.read(guidIndexSize));
                mtr.setEncBaseId(drw.read(guidIndexSize));
                moduleTableRows.add(mtr);
            }
            md.setModuleTableRows(moduleTableRows);
            //logger.info("Module ({})", numModules);
        }
        if (metadataStream.hasTable(MetadataTableFlags.TypeRef)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<TypeRefTableRow> typeRefTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                TypeRefTableRow trtr = new TypeRefTableRow(md);
                trtr.setResolutionScope(drw.read(resolutionScopeIndexSize));
                trtr.setTypeName(drw.read(stringIndexSize));
                trtr.setTypeNamespace(drw.read(stringIndexSize));
                typeRefTableRows.add(trtr);
                //String log = ("TypeRef " + trtr.getResolutionScope() + ", " + stringsStream.get(trtr.getTypeName()) + ", " + stringsStream.get(trtr.getTypeNamespace()));
            }
            md.setTypeRefTableRows(typeRefTableRows);
            //logger.info("TypeRef ({})", numModules);
        }
        if (metadataStream.hasTable(MetadataTableFlags.TypeDef)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<TypeDefTableRow> typeDefTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                TypeDefTableRow tdtr = new TypeDefTableRow(md);
                tdtr.setFlags(dr.readDoubleWord());
                tdtr.setTypeName(drw.read(stringIndexSize));
                tdtr.setTypeNamespace(drw.read(stringIndexSize));
                tdtr.setExtendsType(drw.read(typeDefOrRefIndexSize));
                tdtr.setFieldList(drw.read(indexSize(rowCounts.get(MD.field))));
                tdtr.setMethodList(drw.read(indexSize(rowCounts.get(MD.methodDef))));
                //String log = ("TypeDef " + String.format("%08X", tdtr.getFlags()) + " | " + stringsStream.get(tdtr.getTypeName()) + " | " + stringsStream.get(tdtr.getTypeNamespace()) + " | " + tdtr.getExtendsType());
                // TypeDefOrRef
                typeDefTableRows.add(tdtr);
            }
            md.setTypeDefTableRows(typeDefTableRows);
            //logger.info("TypeDef ({})", numModules);
        }


        //List<TypeDefTableRow> kk = md.getTypeDefTableRows().subList(md.getTypeDefTableRows().size() - 5, md.getTypeDefTableRows().size());
        //for (TypeDefTableRow tdtr : kk) {
        //    String log = ("TypeDef " + String.format("%08X", tdtr.getFlags()) + " | " + stringsStream.get(tdtr.getTypeName()) + " | " + stringsStream.get(tdtr.getTypeNamespace()) + " | " + tdtr.getExtendsType());
        //    System.out.println(log);
        //}


        if (metadataStream.hasTable(MetadataTableFlags.Reserved1)) {
            throw new UnsupportedOperationException("Reserved1");
        }
        if (metadataStream.hasTable(MetadataTableFlags.Field)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<FieldTableRow> fieldTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                FieldTableRow ftr = new FieldTableRow();
                ftr.setFlags(dr.readWord());
                ftr.setName(drw.read(stringIndexSize));
                ftr.setSignature(drw.read(blobIndexSize));
                //String log = ("Field " + String.format("%04X", ftr.getFlags()) + " | " + stringsStream.get(ftr.getName()));
                //System.out.println(log);
                // TypeDefOrRef
                fieldTableRows.add(ftr);
            }
            md.setFieldTableRows(fieldTableRows);
            //logger.info("Field ({})", numModules);
        }
        if (metadataStream.hasTable(MetadataTableFlags.Reserved2)) {
            throw new UnsupportedOperationException("Reserved2");
        }
        if (metadataStream.hasTable(MetadataTableFlags.MethodDef)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<MethodDefTableRow> methodDefTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                MethodDefTableRow mdtr = new MethodDefTableRow();
                mdtr.setRva(dr.readDoubleWord());
                mdtr.setImplFlags(dr.readWord());
                mdtr.setFlags(dr.readWord());
                mdtr.setName(drw.read(stringIndexSize));
                mdtr.setSignature(drw.read(blobIndexSize));
                mdtr.setParamList(dr.readDoubleWord());
                //String log = ("Field " + String.format("%04X", ftr.getFlags()) + " | " + stringsStream.get(ftr.getName()));
                //System.out.println(log);
                // TypeDefOrRef
                methodDefTableRows.add(mdtr);
            }
            md.setMethodDefTableRows(methodDefTableRows);
            //logger.info("MethodDef ({})", numModules);
//
            //List<MethodDefTableRow> end = methodDefTableRows.subList(numModules - 5, numModules);
            //for (MethodDefTableRow tdtr : end) {
            //    String log = ("MethodDef " + String.format("0x%08X", tdtr.getRva()) + " | " + String.format("0x%04X", tdtr.getImplFlags()) + " | " + String.format("0x%04X", tdtr.getFlags()) + " | " + stringsStream.get(tdtr.getName()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.Reserved3)) {
            throw new UnsupportedOperationException("Reserved3");
        }
        if (metadataStream.hasTable(MetadataTableFlags.Param)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<ParamTableRow> paramTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                ParamTableRow ptr = new ParamTableRow();
                ptr.setFlags(dr.readWord());
                ptr.setSequence(dr.readWord());
                ptr.setName(drw.read(stringIndexSize));
                paramTableRows.add(ptr);
            }
            md.setParamTableRows(paramTableRows);
            //logger.info("Param ({})", numModules);
//
            //List<ParamTableRow> end = paramTableRows.subList(numModules - 10, numModules);
            //for (ParamTableRow ptr : end) {
            //    String log = ("Param " + String.format("0x%04X", ptr.getFlags()) + " | " + String.format("0x%04X", ptr.getSequence()) + " | " + stringsStream.get(ptr.getName()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.InterfaceImpl)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<InterfaceImplTableRow> interfaceImplTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                InterfaceImplTableRow iitr = new InterfaceImplTableRow();
                iitr.setClassType(drw.read(indexSize(rowCounts.get(MD.typeDef))));
                iitr.setInterfaceType(drw.read(typeDefOrRefIndexSize));
                interfaceImplTableRows.add(iitr);
            }
            md.setInterfaceImplTableRows(interfaceImplTableRows);
            //logger.info("InterfaceImpl ({})", numModules);
//
            //List<InterfaceImplTableRow> end = interfaceImplTableRows.subList(numModules - 10, numModules);
            //for (InterfaceImplTableRow iitr : end) {
            //    String log = ("InterfaceImpl " + String.format("0x%08X", iitr.getClassType()) + " | " + String.format("0x%04X", iitr.getInterfaceType()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.MemberRef)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<MemberRefTableRow> memberRefTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                MemberRefTableRow mrtr = new MemberRefTableRow();
                mrtr.setClassType(drw.read(memberRefParentIndexSize));
                mrtr.setName(drw.read(stringIndexSize));
                mrtr.setSignature(drw.read(blobIndexSize));
                memberRefTableRows.add(mrtr);
            }
            md.setMemberRefTableRows(memberRefTableRows);
            //logger.info("MemberRef ({})", numModules);
//
            ////List<MemberRefTableRow> end = memberRefTableRows.subList(numModules - 10, numModules);
            //for (MemberRefTableRow mrtr : memberRefTableRows) {
            //    String log = ("MemberRef " + String.format("0x%08X", mrtr.getClassType()) + " | " + stringsStream.get(mrtr.getName()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.Constant)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<ConstantTableRow> constantTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                ConstantTableRow ctr = new ConstantTableRow();
                ctr.setType(dr.readByte());
                ctr.setPadding(dr.readByte());
                ctr.setParent(drw.read(hasConstantIndexSize));
                ctr.setValue(drw.read(blobIndexSize));
                constantTableRows.add(ctr);
            }
            md.setConstantTableRows(constantTableRows);
            //logger.info("Constant ({})", numModules);
//
            //List<ConstantTableRow> end = constantTableRows.subList(numModules - 10, numModules);
            //for (ConstantTableRow mrtr : end) {
            //    String log = ("Constant " + ElementTypes.resolve(mrtr.getType()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.CustomAttribute)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<CustomAttributeTableRow> customAttributeTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                CustomAttributeTableRow catr = new CustomAttributeTableRow();
                catr.setParent(drw.read(hasConstantIndexSize));
                catr.setType(drw.read(hasCustomAttributeIndexSize));
                catr.setValue(drw.read(blobIndexSize));
                customAttributeTableRows.add(catr);
            }
            md.setCustomAttributeTableRows(customAttributeTableRows);
            //logger.info("CustomAttribute ({})", numModules);
//
            //List<CustomAttributeTableRow> end = customAttributeTableRows.subList(numModules - 10, numModules);
            //for (CustomAttributeTableRow catr : end) {
            //    String log = ("CustomAttribute " + String.format("0x%08X", catr.getValue()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.FieldMarshal)) {
            throw new UnsupportedOperationException("FieldMarshal");
        }
        if (metadataStream.hasTable(MetadataTableFlags.DeclSecurity)) {
            throw new UnsupportedOperationException("DeclSecurity");
        }
        if (metadataStream.hasTable(MetadataTableFlags.ClassLayout)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<ClassLayoutTableRow> classLayoutTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                ClassLayoutTableRow cltr = new ClassLayoutTableRow();
                cltr.setPackingSize(dr.readWord());
                cltr.setClassSize(dr.readDoubleWord());
                cltr.setParent(drw.read(indexSize(rowCounts.get(MD.typeDef))));
                classLayoutTableRows.add(cltr);
            }
            md.setClassLayoutTableRows(classLayoutTableRows);
            //logger.info("ClassLayout ({})", numModules);
//
            //List<ClassLayoutTableRow> end = classLayoutTableRows.subList(numModules - 10, numModules);
            //for (ClassLayoutTableRow cltr : end) {
            //    String log = ("ClassLayout " + String.format("0x%04X", cltr.getPackingSize()) + " | " + String.format("0x%08X", cltr.getClassSize()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.FieldLayout)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<FieldLayoutTableRow> fieldLayoutTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                FieldLayoutTableRow fltr = new FieldLayoutTableRow();
                fltr.setOffset(dr.readDoubleWord());
                fltr.setField(drw.read(indexSize(rowCounts.get(MD.field))));
                fieldLayoutTableRows.add(fltr);
            }
            md.setFieldLayoutTableRows(fieldLayoutTableRows);
            //logger.info("FieldLayout ({})", numModules);
//
            //List<FieldLayoutTableRow> end = fieldLayoutTableRows.subList(numModules - 10, numModules);
            //for (FieldLayoutTableRow fltr : end) {
            //    String log = ("FieldLayout " + String.format("0x%08X", fltr.getOffset()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.StandAloneSig)) {
            throw new UnsupportedOperationException("StandAloneSig");
        }
        if (metadataStream.hasTable(MetadataTableFlags.EventMap)) {
            throw new UnsupportedOperationException("EventMap");
        }
        if (metadataStream.hasTable(MetadataTableFlags.Reserved4)) {
            throw new UnsupportedOperationException("Reserved4");
        }
        if (metadataStream.hasTable(MetadataTableFlags.Event)) {
            throw new UnsupportedOperationException("Event");
        }
        if (metadataStream.hasTable(MetadataTableFlags.PropertyMap)) {
            throw new UnsupportedOperationException("PropertyMap");
        }
        if (metadataStream.hasTable(MetadataTableFlags.Reserved5)) {
            throw new UnsupportedOperationException("Reserved5");
        }
        if (metadataStream.hasTable(MetadataTableFlags.Property)) {
            throw new UnsupportedOperationException("Property");
        }
        if (metadataStream.hasTable(MetadataTableFlags.MethodSemantics)) {
            throw new UnsupportedOperationException("MethodSemantics");
        }
        if (metadataStream.hasTable(MetadataTableFlags.MethodImpl)) {
            throw new UnsupportedOperationException("MethodImpl");
        }
        if (metadataStream.hasTable(MetadataTableFlags.ModuleRef)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<ModuleRefTableRow> moduleRefTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                ModuleRefTableRow mrtr = new ModuleRefTableRow();
                mrtr.setName(drw.read(stringIndexSize));
                moduleRefTableRows.add(mrtr);
            }
            md.setModuleRefTableRows(moduleRefTableRows);
            //logger.info("ModuleRef ({})", numModules);
//
            //List<ModuleRefTableRow> end = moduleRefTableRows.subList(numModules - 10, numModules);
            //for (ModuleRefTableRow mrtr : end) {
            //    String log = ("ModuleRef " + stringsStream.get(mrtr.getName()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.TypeSpec)) {
            throw new UnsupportedOperationException("TypeSpec");
        }
        if (metadataStream.hasTable(MetadataTableFlags.ImplMap)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<ImplMapTableRow> implMapTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                ImplMapTableRow imtr = new ImplMapTableRow();
                imtr.setMappingFlags(dr.readWord());
                imtr.setMemberForwarded(drw.read(memberForwardedIndexSize));
                imtr.setImportName(drw.read(stringIndexSize));
                imtr.setImportScope(indexSize(rowCounts.get(MD.moduleRef)));
                implMapTableRows.add(imtr);
            }
            md.setImplMapTableRows(implMapTableRows);
            //logger.info("ImplMap ({})", numModules);

            //List<ImplMapTableRow> end = implMapTableRows.subList(numModules - 10, numModules);
            //for (ImplMapTableRow imtr : end) {
            //    String log = ("ImplMap " + stringsStream.get(imtr.getImportName()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.FieldRVA)) {
            throw new UnsupportedOperationException("FieldRVA");
        }
        if (metadataStream.hasTable(MetadataTableFlags.Reserved6)) {
            throw new UnsupportedOperationException();
        }
        if (metadataStream.hasTable(MetadataTableFlags.Reserved7)) {
            throw new UnsupportedOperationException();
        }
        if (metadataStream.hasTable(MetadataTableFlags.Assembly)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<AssemblyTableRow> assemblyTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                AssemblyTableRow atr = new AssemblyTableRow();
                atr.setHashAlgId(dr.readDoubleWord());
                atr.setMajorVersion(dr.readWord());
                atr.setMinorVersion(dr.readWord());
                atr.setBuildNumber(dr.readWord());
                atr.setRevisionNumber(dr.readWord());
                atr.setFlags(dr.readDoubleWord());
                atr.setPublicKey(drw.read(blobIndexSize));
                atr.setName(drw.read(stringIndexSize));
                atr.setCulture(drw.read(stringIndexSize));
                assemblyTableRows.add(atr);
            }
            md.setAssemblyTableRows(assemblyTableRows);
            //logger.info("Assembly ({})", numModules);

            //List<AssemblyTableRow> end = assemblyTableRows.subList(numModules - 1, numModules);
            //for (AssemblyTableRow atr : end) {
            //    String log = ("Assembly " + stringsStream.get(atr.getName()) + " | " + stringsStream.get(atr.getCulture()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.AssemblyProcessor)) {
            throw new UnsupportedOperationException("AssemblyProcessor");
        }
        if (metadataStream.hasTable(MetadataTableFlags.AssemblyOS)) {
            throw new UnsupportedOperationException("AssemblyOS");
        }
        if (metadataStream.hasTable(MetadataTableFlags.AssemblyRef)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<AssemblyRefTableRow> assemblyRefTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                AssemblyRefTableRow artr = new AssemblyRefTableRow();
                artr.setMajorVersion(dr.readWord());
                artr.setMinorVersion(dr.readWord());
                artr.setBuildNumber(dr.readWord());
                artr.setRevisionNumber(dr.readWord());
                artr.setFlags(dr.readDoubleWord());
                artr.setPublicKeyOrToken(drw.read(blobIndexSize));
                artr.setName(drw.read(stringIndexSize));
                artr.setCulture(drw.read(stringIndexSize));
                artr.setHashValue(drw.read(blobIndexSize));
                assemblyRefTableRows.add(artr);
            }
            md.setAssemblyRefTableRows(assemblyRefTableRows);
            //logger.info("AssemblyRef ({})", numModules);

            //System.out.println("AssemblyRef (" + numModules + "): MajorVersion - 2b | MinorVersion - 2b");
            //List<AssemblyRefTableRow> end = assemblyRefTableRows.subList(numModules - 4, numModules);
            //for (AssemblyRefTableRow artr : end) {
            //    String log = ("AssemblyRef "
            //            + String.format("0x%04X", artr.getMajorVersion()) + " | "
            //            + String.format("0x%04X", artr.getMinorVersion()) + " | "
            //            + String.format("0x%04X", artr.getBuildNumber()) + " | "
            //            + String.format("0x%04X", artr.getRevisionNumber()) + " | "
            //            + String.format("0x%08X", artr.getFlags()) + " | "
            //            + stringsStream.get(artr.getName()) + " | "
            //            + stringsStream.get(artr.getCulture())) + " | "
            //            + ((artr.getHashValue() == 0) ? "null" : String.format("0x%04X", artr.getHashValue()));
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.AssemblyRefProcessor)) {
            throw new UnsupportedOperationException("AssemblyRefProcessor");
        }
        if (metadataStream.hasTable(MetadataTableFlags.AssemblyRefOS)) {
            throw new UnsupportedOperationException("AssemblyRefOS");
        }
        if (metadataStream.hasTable(MetadataTableFlags.File)) {
            throw new UnsupportedOperationException("File");
        }
        if (metadataStream.hasTable(MetadataTableFlags.ExportedType)) {
            throw new UnsupportedOperationException("ExportedType");
        }
        if (metadataStream.hasTable(MetadataTableFlags.ManifestResource)) {
            throw new UnsupportedOperationException("ManifestResource");
        }
        if (metadataStream.hasTable(MetadataTableFlags.NestedClass)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<NestedClassTableRow> nestedClassTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                NestedClassTableRow nctr = new NestedClassTableRow();
                nctr.setNestedClass(drw.read(indexSize(rowCounts.get(MD.typeDef))));
                nctr.setEnclosingClass(drw.read(indexSize(rowCounts.get(MD.typeDef))));
                nestedClassTableRows.add(nctr);
            }
            md.setNestedClassTableRows(nestedClassTableRows);
            //logger.info("NestedClass ({})", numModules);

            //List<NestedClassTableRow> end = nestedClassTableRows.subList(numModules - 14, numModules);
            //for (NestedClassTableRow nctr : end) {
            //    String log = ("NestedClass " + nctr.getNestedClass() + " | " + nctr.getEnclosingClass());
            //    System.out.println(log);
            //}
        }
        if (metadataStream.hasTable(MetadataTableFlags.GenericParam)) {
            throw new UnsupportedOperationException("GenericParam");
        }
        if (metadataStream.hasTable(MetadataTableFlags.MethodSpec)) {
            throw new UnsupportedOperationException("MethodSpec");
        }
        if (metadataStream.hasTable(MetadataTableFlags.GenericParamConstraint)) {
            throw new UnsupportedOperationException("GenericParamConstraint");
        }
        if (tableSizesIndex != n) {
            throw new IllegalStateException("Read " + tableSizesIndex + " tables but there were " + n + " tables to read.");
        }

        // now there's two more words left over? both 0

        System.out.println(md);

        //DataReader dr = new DataReader(clr);
        //int headerSize = dr.readDoubleWord();
        //int majorRuntimeVersion = dr.readWord();
        //int minorRuntimeVersion = dr.readWord();
        //int metaDataDirectoryAddress = dr.readDoubleWord();
        //int metaDataDirectorySize = dr.readDoubleWord();
        //int flags = dr.readDoubleWord();
        //int entryPointToken = dr.readDoubleWord();
        //int resourcesDirectoryAddress = dr.readDoubleWord();
        //int resourcesDirectorySize = dr.readDoubleWord();
        //int strongNameSignatureAddress = dr.readDoubleWord();
        //int strongNameSignatureSize = dr.readDoubleWord();
        //int codeManagerTableAddress = dr.readDoubleWord();
        //int codeManagerTableSize = dr.readDoubleWord();
        //int vTableFixupsAddress = dr.readDoubleWord();
        //int vTableFixupsSize = dr.readDoubleWord();
        //int exportAddressTableJumpsAddress = dr.readDoubleWord();
        //int exportAddressTableJumpsSize = dr.readDoubleWord();
        //int managedNativeHeaderAddress = dr.readDoubleWord();
        //int managedNativeHeaderSize = dr.readDoubleWord();

        //public CLRHeader ReadCLRHeader(BinaryReader assemblyReader, PEHeader peHeader)
        //{
        //	var clrDirectoryHeader = peHeader.Directories[(int) DataDirectoryName.CLRHeader];
        //	var clrDirectoryData = ReadVirtualDirectory(assemblyReader, clrDirectoryHeader, peHeader.Sections);
        //	using (var reader = new BinaryReader(new MemoryStream(clrDirectoryData)))
        //	{
        //		return new CLRHeader
        //		{
        //			HeaderSize = reader.ReadUInt32(),
        //					MajorRuntimeVersion = reader.ReadUInt16(),
        //					MinorRuntimeVersion = reader.ReadUInt16(),
        //					MetaDataDirectoryAddress = reader.ReadUInt32(),
        //					MetaDataDirectorySize = reader.ReadUInt32(),
        //					Flags = reader.ReadUInt32(),
        //					EntryPointToken = reader.ReadUInt32(),
        //					ResourcesDirectoryAddress = reader.ReadUInt32(),
        //					ResourcesDirectorySize = reader.ReadUInt32(),
        //					StrongNameSignatureAddress = reader.ReadUInt32(),
        //					StrongNameSignatureSize = reader.ReadUInt32(),
        //					CodeManagerTableAddress = reader.ReadUInt32(),
        //					CodeManagerTableSize = reader.ReadUInt32(),
        //					VTableFixupsAddress = reader.ReadUInt32(),
        //					VTableFixupsSize = reader.ReadUInt32(),
        //					ExportAddressTableJumpsAddress = reader.ReadUInt32(),
        //					ExportAddressTableJumpsSize = reader.ReadUInt32(),
        //					ManagedNativeHeaderAddress = reader.ReadUInt32(),
        //					ManagedNativeHeaderSize = reader.ReadUInt32()
        //		};
        //	}
        //}
        /*
        int ador = pe.getSectionTable().getRVAConverter().convertVirtualAddressToRawDataPointer(address);
         */

        //System.out.println("hi");
        //var pe = PEParser.parse(Path.of(""));
        return md;
    }

    private static int bitsNeeded(int rc)
    {
        if (rc == 0)
            return 0;
        int r = 1;
        --rc;
        while ((rc >>= 1) != 0)
            ++r;
        return r;
    }

    private static int bitsNeeded(List<Integer> rowCounts)
    {
        int max = 0;
        for (Integer rc : rowCounts) {
            if (rc == null)
                continue;
            max = Math.max(max, bitsNeeded(rc));
        }
        return max;
    }

    private static int compositeIndexSize(Integer... rowCounts)
    {
        return (bitsNeeded(Arrays.asList(rowCounts)) + bitsNeeded(rowCounts.length) <= 16) ? 2 : 4;
    }

    private static int indexSize(int rowCount)
    {
        return rowCount <= 0xffff ? 2 : 4;
    }

    static byte[] parseHeader(StreamHeader sh, DataReader dr) throws IOException {
        return parseUnknown(sh, dr);
    }

    static byte[] parseStrings(StreamHeader sh, DataReader dr) throws IOException {
        return parseUnknown(sh, dr);
    }

    static byte[] parseUS(StreamHeader sh, DataReader dr) throws IOException {
        return parseUnknown(sh, dr);
    }

    static byte[] parseGuid(StreamHeader sh, DataReader dr) throws IOException {
        return parseUnknown(sh, dr);
    }

    static byte[] parseUnknown(StreamHeader sh, DataReader dr) throws IOException {
        //dr.jumpTo(sh.getOffset() - 512);
        jumpToNoJump(dr, sh.getOffset());
        byte[] b = new byte[sh.getSize()];
        dr.read(b);
        return b;
    }

    static void jumpToNoJump(DataReader dr, int location) throws IOException {
        int currentPosition = dr.getPosition();
        dr.jumpTo(location);
        if (currentPosition != dr.getPosition()) {
            throw new RuntimeException("Jump was off by " + (dr.getPosition() - currentPosition));
        }
    }

    static class DataReaderWrapper {
        private final IDataReader dr;
        public DataReaderWrapper(IDataReader dataReader) {
            this.dr = dataReader;
        }
        public int read(int bytes) {
            try {
                if (bytes == 1) {
                    return dr.readByte();
                } else if (bytes == 2) {
                    return dr.readWord();
                } else if (bytes == 4) {
                    return dr.readDoubleWord();
                } else {
                    throw new RuntimeException("Cannot read " + bytes + " bytes.");
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }
}
