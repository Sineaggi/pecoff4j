package com.kichik.pecoff4j;

public class ElementTypes {
    public static final int ELEMENT_TYPE_END = 0x00;
    public static final int ELEMENT_TYPE_VOID = 0x01;
    public static final int ELEMENT_TYPE_BOOLEAN = 0x02;
    public static final int ELEMENT_TYPE_CHAR = 0x03;
    public static final int ELEMENT_TYPE_I1 = 0x04;
    public static final int ELEMENT_TYPE_U1 = 0x05;
    public static final int ELEMENT_TYPE_I2 = 0x06;
    public static final int ELEMENT_TYPE_U2 = 0x07;
    public static final int ELEMENT_TYPE_I4 = 0x08;
    public static final int ELEMENT_TYPE_U4 = 0x09;
    public static final int ELEMENT_TYPE_I8 = 0x0a;
    public static final int ELEMENT_TYPE_U8 = 0x0b;
    public static final int ELEMENT_TYPE_R4 = 0x0c;
    public static final int ELEMENT_TYPE_R8 = 0x0d;
    public static final int ELEMENT_TYPE_STRING = 0x0e;
    public static final int ELEMENT_TYPE_PTR = 0x0f;
    public static final int ELEMENT_TYPE_BYREF = 0x10;
    public static final int ELEMENT_TYPE_VALUETYPE = 0x11;
    public static final int ELEMENT_TYPE_CLASS = 0x12;
    public static final int ELEMENT_TYPE_VAR = 0x13;
    public static final int ELEMENT_TYPE_ARRAY = 0x14;
    public static final int ELEMENT_TYPE_GENERICINST = 0x15;
    public static final int ELEMENT_TYPE_TYPEDBYREF = 0x16;
    public static final int ELEMENT_TYPE_I = 0x18;
    public static final int ELEMENT_TYPE_U = 0x19;
    public static final int ELEMENT_TYPE_FNPTR = 0x1b;
    public static final int ELEMENT_TYPE_OBJECT = 0x1c;
    public static final int ELEMENT_TYPE_SZARRAY = 0x1d;
    public static final int ELEMENT_TYPE_MVAR = 0x1e;
    public static final int ELEMENT_TYPE_CMOD_REQD = 0x1f;
    public static final int ELEMENT_TYPE_CMOD_OPT = 0x20;
    public static final int ELEMENT_TYPE_INTERNAL = 0x21;
    public static final int ELEMENT_TYPE_MODIFIER = 0x40;
    public static final int ELEMENT_TYPE_SENTINEL = 0x41;
    public static final int ELEMENT_TYPE_PINNED = 0x45;

    public static String resolve(int i) {
        switch (i) {
            case ELEMENT_TYPE_END: return "END";
            case ELEMENT_TYPE_VOID: return "VOID";
            case ELEMENT_TYPE_BOOLEAN: return "BOOLEAN";
            case ELEMENT_TYPE_CHAR: return "CHAR";
            case ELEMENT_TYPE_I1: return "I1";
            case ELEMENT_TYPE_U1: return "U1";
            case ELEMENT_TYPE_I2: return "I2";
            case ELEMENT_TYPE_U2: return "U2";
            case ELEMENT_TYPE_I4: return "I4";
            case ELEMENT_TYPE_U4: return "U4";
            case ELEMENT_TYPE_I8: return "I8";
            case ELEMENT_TYPE_U8: return "U8";
            case ELEMENT_TYPE_R4: return "R4";
            case ELEMENT_TYPE_R8: return "R8";
            case ELEMENT_TYPE_STRING: return "STRING";
            case ELEMENT_TYPE_CLASS: return "CLASS";
            default:
                throw new IllegalStateException("Unexpected value: " + i);
        }
    }
}
