package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.CharBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class Node {
    private static File file;
    private static RandomAccessFile randomAccessFile;
    private static FileChannel fileChannel;
    private static Long myId;
    private static ArrayList<Long> processesIds;
    private static LongBuffer longBuffer;
    private static CharBuffer charBuffer;


    public static void main(String[] args) throws IOException {
        file=new File(args[0]);
        myId=ProcessHandle.current().pid();
        getFileReadWriteReady();
        beginElection();
    }

    private static void beginElection() {
        for(int i=0;i<processesIds.size();i++)
        {
            if(processesIds.get(i)!=myId) {
                writeMessage("Election", processesIds.get(i));
            }
        }
    }

    private static void writeMessage(String message,Long toProcess) {
        longBuffer.put(myId);
        charBuffer.put(message+'\0');
        //TODO  : what it will read long and then message or
    }


    private static void getFileReadWriteReady() throws IOException {
        randomAccessFile=new RandomAccessFile(file.getPath(),"rw");
        fileChannel=randomAccessFile.getChannel();
        MappedByteBuffer buffer=fileChannel.map(FileChannel.MapMode.READ_WRITE,0,4096);
        longBuffer=buffer.asLongBuffer();
        charBuffer=buffer.asCharBuffer();
        Long id;
        while((id=longBuffer.get())!=0)
        {
            processesIds.add(id);
        }
    }
}
