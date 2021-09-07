package com.kichik.pecoff4j;

import com.kichik.pecoff4j.io.PEParser;
import com.kichik.pecoff4j.util.IO;
import com.kichik.pecoff4j.util.Reflection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestParseWINMD {
    private static final Logger logger = LoggerFactory.getLogger(TestParseWINMD.class);

    public static void main(String[] args) throws Exception {
        File[] files = findPEs();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
            PE pe = PEParser.parse(files[i]);
            System.out.println(Reflection.toString(pe));
        }
    }

    public static File[] findPEs() {
        FilenameFilter ff = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".winmd"))
                        && name.indexOf("dllcache") == -1;
            }
        };
        File[] files = IO.findFiles(new File("F:/Program Files/"), ff);
        // File[] files = IO.findFiles(new File("C:/Program Files/"), ff);
        // File[] files = IO.findFiles(new File("C:/windows/system32"), ff);

        return files;
    }

    @Test
    public void test() throws IOException {
        //System.out.println("hi");
        //PE pe = PEParser.parse(Paths.get("/Users/cwalker/git/windows-java/src/main/resources/Windows.Win32.winmd"));
        PE pe = PEParser.parse(Paths.get("/Users/cwalker/git/pecoff4j/Windows.Win32.winmd"));

        Metadata md = CLRParser.parseCLRMetadata(pe);
        //System.out.println(md);
        //assertEquals(md.getTypeDefTableRows().size(), 23);
        assertEquals(md.getModuleTableRows().size(), 1);
        assertEquals(md.getTypeRefTableRows().size(), 12619);
        assertEquals(md.getTypeDefTableRows().size(), 31463);
        assertEquals(md.getFieldTableRows().size(), 183219);
        assertEquals(md.getMethodDefTableRows().size(), 62106);
        assertEquals(md.getParamTableRows().size(), 191694);
        assertEquals(md.getInterfaceImplTableRows().size(), 6985);
        assertEquals(md.getMemberRefTableRows().size(), 22);
        assertEquals(md.getConstantTableRows().size(), 109316);
        assertEquals(md.getCustomAttributeTableRows().size(), 66888);
        assertEquals(md.getClassLayoutTableRows().size(), 966);
        assertEquals(md.getFieldLayoutTableRows().size(), 3618);
        assertEquals(md.getModuleRefTableRows().size(), 307);
        assertEquals(md.getImplMapTableRows().size(), 15859);
        assertEquals(md.getAssemblyTableRows().size(), 1);
        assertEquals(md.getAssemblyRefTableRows().size(), 4);
        assertEquals(md.getNestedClassTableRows().size(), 1650);

        dumpAll(md);

    }

    private static void dumpAll(Metadata metadata) {

        List<String> cfgIgnoredNamespaces = new ArrayList<>();
        List<String> namespaces = getNamespaces(metadata, cfgIgnoredNamespaces);

        Path path = Paths.get("example.java");


        try (OutputStream f = Files.newOutputStream(path)) {
            for (String namespace : namespaces) {
                System.out.println(namespace);
                dumpEnums(f, metadata, namespace);
                dumpApisConstants(f, metadata, namespace);
                //dumpDelegates(f, metadata, namespace);
                //dumpStructs(f, metadata, namespace);
                //dumpApis(f, metadata, namespace);
                //dumpInterfaces(f, metadata, namespace);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<String> getNamespaces(Metadata metadata, List<String> ignoredNamespaces) {
        //Set<String> ii = new HashSet<>(ignoredNamespaces);
        //for (TypeDefTableRow typeDefTableRow : metadata.getTypeDefTableRows()) {
        //    if (!ii.contains(typeDefTableRow.namespace())) {
        //    }
        //    typeDefTableRow.namespace();
        //}
        Set<String> defSet = metadata.getTypeDefTableRows().stream().map(TypeDefTableRow::namespace).filter(i -> !ignoredNamespaces.contains(i)).collect(Collectors.toSet());
        Set<String> refSet = metadata.getTypeRefTableRows().stream().map(TypeRefTableRow::namespace).filter(i -> !ignoredNamespaces.contains(i)).collect(Collectors.toSet());
        Set<String> all = new HashSet<>();
        all.addAll(defSet);
        all.addAll(refSet);
        return all.stream().filter(i -> i.length() > 0).sorted().collect(Collectors.toList());
    }

    private static void dumpEnums(OutputStream os, Metadata metadata, String namespace) {
        List<TypeDefTableRow> a = getEnums(metadata, namespace);
        if (a.size() != 0) {
            System.out.println("KOLA " + a);
        }
        List<TypeRefTableRow> ab = getpEnums(metadata, namespace);
        if (ab.size() != 0) {
            System.out.println("KOLA " + ab);
        }
    }

    private static void dumpApisConstants(OutputStream os, Metadata metadata, String namespace) {
        //List<TypeDefTableRow> a = getEnums(metadata, namespace);
        //if (a.size() != 0) {
         //   System.out.println("KOLA " + a);
        //}
    }

    public static List<TypeDefTableRow> getEnums(Metadata db, String namespace) {
        return getTypeDefs(db, namespace).stream().filter(TypeDefTableRow::isEnum).collect(Collectors.toList());
    }

    public static List<TypeRefTableRow> getpEnums(Metadata db, String namespace) {
        return getTypeRefs(db, namespace).stream().filter(TypeRefTableRow::isEnum).collect(Collectors.toList());
    }

    public static List<TypeDefTableRow> getTypeDefs(Metadata db, String namespace) {
        //return db.typeDefTable.items.filter!(a => a.namespace == namespace);

        return db.getTypeDefTableRows().stream().filter(i -> i.namespace().equals(namespace)).collect(Collectors.toList());
    }

    public static List<TypeRefTableRow> getTypeRefs(Metadata db, String namespace) {
        //return db.typeDefTable.items.filter!(a => a.namespace == namespace);

        return db.getTypeRefTableRows().stream().filter(i -> i.namespace().equals(namespace)).collect(Collectors.toList());
    }

}
