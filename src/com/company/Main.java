package com.company;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;


public class Main {
    private static ArrayList<Long> processesIds;
    private static File file;
    public static void main(String[] args) throws IOException {

        processesIds=new ArrayList<>();
        for(int i=0;i<2;i++) {
            file=new File(String.valueOf(ProcessHandle.current().pid()));
            processesIds.add(newNode(file.getPath()));
        }
        shareProcessesIds();
    }
    public static void shareProcessesIds() throws IOException {
        RandomAccessFile randomAccessFile=new RandomAccessFile(file.getPath(),"rw");
        FileChannel fileChannel= randomAccessFile.getChannel();
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
        LongBuffer longBuffer = buffer.asLongBuffer();
        for (Long processesId : processesIds) {
            longBuffer.put(processesId);
        }
    }
    public static Long newNode(String args) throws IOException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome +
                File.separator + "bin" +
                File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = "com.company.Node";

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        command.add(className);
        if(args != null)
        {
            command.add(args);
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.inheritIO().start();
        return process.pid();
    }
}
