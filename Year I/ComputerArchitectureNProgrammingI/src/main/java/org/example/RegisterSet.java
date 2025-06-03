package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class RegisterSet {
    private final Map<String, Register> registers;
    private final int maxSize;

    public RegisterSet(int size) {
        if (size <= 0 || size > 32) {
            throw new IllegalArgumentException("Register set size must be between 1 and 32");
        }
        
        this.maxSize = size;
        this.registers = new HashMap<>();

        for (int i = 0; i < size; i++) {
            String regName = "R" + i;
            registers.put(regName, new Register());
        }
    }

    public void write(String regName, int value) {
        checkExists(regName);
        registers.get(regName).write(value);
    }

    public int read(String regName) {
        checkExists(regName);
        return registers.get(regName).read();
    }

    public Register get(String regName) {
        checkExists(regName);
        return registers.get(regName);
    }

    public void clear(String regName) {
        checkExists(regName);
        registers.get(regName).clear();
    }

    public void dump() {
        System.out.println("=== Register Dump ===");
        for (Map.Entry<String, Register> entry : registers.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    public String getDumpString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Register Dump ===\n");
        
        for (int i = 0; i < maxSize; i++) {
            String regName = "R" + i;
            Register reg = registers.get(regName);
            sb.append(String.format("%-4s = %s%n", regName, reg.toString()));
        }
        
        return sb.toString();
    }

    public Map<String, Register> getAllRegisters() {
        return Collections.unmodifiableMap(registers);
    }

    public int size() {
        return registers.size();
    }


    public boolean hasRegister(String regName) {
        return registers.containsKey(regName);
    }


    public void clearAll() {
        for (Register register : registers.values()) {
            register.clear();
        }
    }


    public void add(String reg1, String reg2, String dest) {
        checkExists(reg1);
        checkExists(reg2);
        checkExists(dest);
        
        int result = registers.get(reg1).getValue() + registers.get(reg2).getValue();
        registers.get(dest).write(result);
    }

    public void subtract(String reg1, String reg2, String dest) {
        checkExists(reg1);
        checkExists(reg2);
        checkExists(dest);
        
        int result = registers.get(reg1).getValue() - registers.get(reg2).getValue();
        registers.get(dest).write(result);
    }

    public void multiply(String reg1, String reg2, String dest) {
        checkExists(reg1);
        checkExists(reg2);
        checkExists(dest);
        
        long result = (long) registers.get(reg1).getValue() * registers.get(reg2).getValue();
        if (result > Integer.MAX_VALUE || result < Integer.MIN_VALUE) {
            throw new ArithmeticException("Arithmetic overflow");
        }
        registers.get(dest).write((int) result);
    }

    public void divide(String reg1, String reg2, String dest) {
        checkExists(reg1);
        checkExists(reg2);
        checkExists(dest);
        
        int divisor = registers.get(reg2).getValue();
        if (divisor == 0) {
            throw new ArithmeticException("Division by zero");
        }
        
        int result = registers.get(reg1).getValue() / divisor;
        registers.get(dest).write(result);
    }


    public List<String> getModifiedRegisters() {
        List<String> modified = new ArrayList<>();
        for (Map.Entry<String, Register> entry : registers.entrySet()) {
            if (entry.getValue().isModified()) {
                modified.add(entry.getKey());
            }
        }
        return modified;
    }


    public void resetModificationFlags() {
        for (Register register : registers.values()) {
            register.resetModifiedFlag();
        }
    }


    public void copy(String source, String dest) {
        checkExists(source);
        checkExists(dest);
        
        int value = registers.get(source).getValue();
        registers.get(dest).write(value);
    }

    public void loadImmediate(String regName, int value) {
        checkExists(regName);
        registers.get(regName).write(value);
    }

    private void checkExists(String regName) {
        if (!registers.containsKey(regName)) {
            throw new IllegalArgumentException("Register not found: " + regName);
        }
    }
}