package org.example;

public class Register {

    private int value;
    private boolean modified;

    public Register() {
        this.value = 0;
        this.modified = false;
    }

    public Register(int initialValue) {
        this.value = initialValue;
        this.modified = true;
    }

    public int read() {
        return this.value;
    }

    public int getValue() {
        return this.value;
    }

    public void write(int newValue) {
        this.value = newValue;
        this.modified = true;
    }

    public void clear() {
        this.value = 0;
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public void resetModifiedFlag() {
        this.modified = false;
    }

    @Override
    public String toString() {
        return String.format("0x%08X (%d)", value, value);
    }

    public String toHexString() {
        return String.format("0x%08X", value);
    }

    public String toDecimalString() {
        return String.valueOf(value);
    }

    public String toBinaryString() {
        return String.format("0b%32s", Integer.toBinaryString(value)).replace(
            ' ',
            '0'
        );
    }
}
