package com.kichik.pecoff4j;

import com.kichik.pecoff4j.io.DataReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

        StreamHeader stringsStreamHeader = Objects.requireNonNull(
                findStringsStream(streamHeaders), "#Strings header must exist");
        //System.out.println(stringsStreamHeader.getOffset());

        dr = new DataReader(sb.getData(), offset - sh.getPointerToRawData(), size);
        dr.jumpTo(stringsStreamHeader.getOffset());
        byte[] strings = new byte[stringsStreamHeader.getSize()];
        dr.read(strings);
        StringsStream stringsStream = new StringsStream(strings);
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
        dr = new DataReader(sb.getData(), offset - sh.getPointerToRawData(), size);

        StreamHeader metadataStreamHeader = Objects.requireNonNull(findMetadataStream(streamHeaders), "#~ or #- header must exist");
        //System.out.println(metadataStreamHeader.getOffset());
        dr.jumpTo(metadataStreamHeader.getOffset()); // should be noop?
        //dr = new DataReader(pe.getSectionTable().getSection(0).getData());
        //dr.jumpTo(pe.getSectionTable().getRVAConverter().convertVirtualAddressToRawDataPointer(metadataStreammm.getOffset()) - dataoffset);
        byte[] mdby = new byte[metadataStreamHeader.getSize()];
        dr.read(mdby);
        dr = new DataReader(mdby);
        MetadataStream metadataStream = new MetadataStream();
        metadataStream.setReserved1(dr.readDoubleWord());
        metadataStream.setMajorVersion(dr.readByte());
        metadataStream.setMinorVersion(dr.readByte());
        metadataStream.setOffsetSizeFlags(dr.readByte());
        metadataStream.setReserved2(dr.readByte());
        metadataStream.setTablesFlags(dr.readLong());
        metadataStream.setSortedTablesFlags(dr.readLong());
        // todo: continue here https://codingwithspike.wordpress.com/2012/09/01/building-a-net-disassembler-part-4-reading-the-metadata-tables-in-the-stream/

        int n = Long.bitCount(metadataStream.getTablesFlags());
        int[] tableSizes = new int[n];
        for (int i = 0; i < n; i++) {
            tableSizes[i] = dr.readDoubleWord();
        }
        metadataStream.setTableSizes(tableSizes);
        int tableSizesIndex = 0;
        //IntMap intMap = new IntMap();
        Metadata md = new Metadata();
        if (metadataStream.hasTable(MetadataTableFlags.Module)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<ModuleTableRow> moduleTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                ModuleTableRow mtr = new ModuleTableRow();
                mtr.setGeneration(dr.readWord());
                mtr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                mtr.setMvid(readGuidStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                mtr.setEncId(readGuidStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                mtr.setEncBaseId(readGuidStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                moduleTableRows.add(mtr);
            }
            md.setModuleTableRows(moduleTableRows);
            //logger.info("Module ({})", numModules);
        }

        //for (ModuleTableRow moduleTableRow : md.getModuleTableRows()) {
        //    System.out.println("tater");
        //    System.out.println(stringsStream.get(moduleTableRow.getName()));
        //}

        if (metadataStream.hasTable(MetadataTableFlags.TypeRef)) {
            int numModules = tableSizes[tableSizesIndex++];
            List<TypeRefTableRow> typeRefTableRows = new ArrayList<>();
            for (int i = 0; i < numModules; i++) {
                TypeRefTableRow trtr = new TypeRefTableRow();
                trtr.setResolutionScope(dr.readWord()); // todo: 2 or 4 bytes depending on some rule i can't follow, ????
                trtr.setTypeName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                trtr.setTypeNamespace(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                TypeDefTableRow tdtr = new TypeDefTableRow();
                tdtr.setFlags(dr.readDoubleWord());
                tdtr.setTypeName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                tdtr.setTypeNamespace(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                tdtr.setExtendsType(dr.readWord()); // todo: same as above, 2 vs 4 bytes? unsure, check if coded
                tdtr.setFieldList(dr.readDoubleWord()); // todo: think this is correct, check if is simple
                tdtr.setMethodList(dr.readDoubleWord()); // todo: think this is correct, check if is simple
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
                ftr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                ftr.setSignature(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                mdtr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                mdtr.setSignature(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                ptr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                iitr.setClassType(readSimpleIndex(md.getTypeDefTableRows(), dr));
                iitr.setInterfaceType(dr.readDoubleWord()); // todo: codedindex
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
                mrtr.setClassType(dr.readDoubleWord()); // todo: codedindex
                mrtr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                mrtr.setSignature(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                ctr.setParent(dr.readDoubleWord()); // todo: codedindex
                ctr.setValue(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                catr.setParent(dr.readDoubleWord()); // todo: codedindex
                catr.setType(dr.readDoubleWord()); // todo: codedindex
                catr.setValue(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                cltr.setParent(readSimpleIndex(md.getTypeDefTableRows(), dr));
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
                fltr.setField(readSimpleIndex(md.getFieldTableRows(), dr));
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
                mrtr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                imtr.setMemberForwarded(dr.readDoubleWord()); // complex index
                imtr.setImportName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                imtr.setImportScope(readSimpleIndex(md.getModuleRefTableRows(), dr));
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
                atr.setPublicKey(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                atr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                atr.setCulture(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                artr.setPublicKeyOrToken(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                artr.setName(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                artr.setCulture(readStringStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
                artr.setHashValue(readBlobStreamIndex(metadataStream.getOffsetSizeFlags(), dr));
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
                nctr.setNestedClass(readSimpleIndex(md.getTypeDefTableRows(), dr));
                nctr.setEnclosingClass(readSimpleIndex(md.getTypeDefTableRows(), dr));
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
        int ador = pe.getSectionTable().getRVAConverter().convertVirtualAddressToRawDataPointer(address);

        //System.out.println("hi");
        //var pe = PEParser.parse(Path.of(""));
        return md;
    }

    private static StreamHeader findMetadataStream(List<StreamHeader> streamHeaders) {
        for (StreamHeader streamHeader : streamHeaders) {
            String name = streamHeader.getName();
            if (name.equals("#~") || name.equals("#-")) {
                return streamHeader;
            }
        }
        return null;
    }

    private static StreamHeader findStringsStream(List<StreamHeader> streamHeaders) {
        for (StreamHeader streamHeader : streamHeaders) {
            String name = streamHeader.getName();
            if (name.equals("#Strings")) {
                return streamHeader;
            }
        }
        return null;
    }

    private static int readStringStreamIndex(int offsetSizeFlags, DataReader dr) throws IOException {
        return readStreamIndex(StreamOffsetSizeFlags.String, offsetSizeFlags, dr);
    }

    private static int readGuidStreamIndex(int offsetSizeFlags, DataReader dr) throws IOException {
        return readStreamIndex(StreamOffsetSizeFlags.GUID, offsetSizeFlags, dr);
    }

    private static int readBlobStreamIndex(int offsetSizeFlags, DataReader dr) throws IOException {
        return readStreamIndex(StreamOffsetSizeFlags.Blob, offsetSizeFlags, dr);
    }

    private static int readStreamIndex(int streamFlag, int offsetSizeFlags, DataReader dr) throws IOException {
        if ((streamFlag & offsetSizeFlags) == streamFlag) {
            return dr.readDoubleWord();
        } else {
            return dr.readWord();
        }
    }

    private static <T> int readSimpleIndex(List<T> l, DataReader dr) throws IOException {
        if (l.size() >= 65536) { // 2^16
            return dr.readDoubleWord();
        } else {
            return dr.readWord();
        }
    }

    private static <T> int readCodedIndex(List<T> l, DataReader dr) throws IOException {
        if (l.size() >= 65536) { // 2^16
            return dr.readDoubleWord();
        } else {
            return dr.readWord();
        }
    }
}
