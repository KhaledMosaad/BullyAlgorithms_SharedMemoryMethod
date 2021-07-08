package com.company;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.LongBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Node {
    private static String filePath;
    private static Long myId;
    private static ArrayList<Long> processesIds;
    private static LongBuffer longBuffer;
    private static CharBuffer charBuffer;


    public static void main(String[] args) throws IOException, InterruptedException {
        filePath =args[0];
        myId=ProcessHandle.current().pid();
        TimeUnit.SECONDS.sleep(1);
        readProcessesId();
        System.out.println(processesIds);
        beginElection();
        readMessage();
    }

    private static void beginElection(){
        int flag=0;
        for(int i=0;i<processesIds.size();i++)
        {
            if(processesIds.get(i)>myId) {
                flag++;
            }
        }
        if(flag==0)
        {
            for(int i=0;i<processesIds.size();i++)
            {
                if(!processesIds.get(i).equals(myId))
                {
                    writeMessage("I'm Coordinator",processesIds.get(i).toString());
                }
            }
        }
    }
    private static void writeMessage(String message,String toProcess) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
             FileChannel fileChannel = randomAccessFile.getChannel()
        ) {
            FileLock lock;
            while((lock=fileChannel.tryLock(0,Long.MAX_VALUE,true))==null){
            }
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096);
            charBuffer = buffer.asCharBuffer();
            charBuffer.put(toProcess);
            charBuffer.put('\0');
            charBuffer.put(message);
            charBuffer.put('\0');
            charBuffer.put(myId.toString());
            charBuffer.put('\0');
            //System.out.println(myId + " Sending " + message + " To " + toProcess);
            lock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void readMessage() throws IOException {
        StringBuilder message = new StringBuilder("");
        StringBuilder myProcessId = new StringBuilder("");
        StringBuilder processId = new StringBuilder("");
        char c;
        boolean notFree=true;
        while (notFree) {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
                     FileChannel fileChannel = randomAccessFile.getChannel();
                ) {
                    FileLock lock;
                    while((lock=fileChannel.tryLock(0,Long.MAX_VALUE,true))==null){
                        // wait until unlock
                    }
                    ByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, 4096);
                    charBuffer = buffer.asCharBuffer();
                    while ((c = charBuffer.get()) != '\0')
                    {
                        myProcessId.append(c);
                        //System.out.print(c);
                    }
                    while ((c = charBuffer.get()) != '\0')
                    {
                        message.append(c);
                        //System.out.print(c);
                    }
                    while ((c = charBuffer.get()) != '\0')
                    {
                        processId.append(c);
                        //System.out.print(c);
                    }
                    //System.out.println("id = " + myProcessId +" received "+ message +" Send from " + processId);
                    if (myProcessId.toString().equals(myId.toString())) {
                        System.out.println("Process(" + myProcessId + ") receive: "
                                + message + " From " + processId + " at " + new Timestamp(System.currentTimeMillis()));
                        notFree=false;
                        lock.close();
                        switch (message.toString()) {
                            case "Election":
                                writeMessage("OK", processId.toString());
                                break;
                            case "OK":
                                break;
                            case "I'm Coordinator":
                            case "Yes":
                                writeMessage("Coordinator Alive", processId.toString());
                                break;
                            case "Coordinator Alive":
                                writeMessage("Yes", processId.toString());
                                break;
                        }
                    }
                    else
                    {
                        lock.close();
                    }
            }
        }
    }

    private static void readProcessesId() throws IOException {
        boolean notFree=true;
        while(notFree) {
            try(RandomAccessFile randomAccessFile=new RandomAccessFile(filePath,"rw");
                    FileChannel fileChannel = randomAccessFile.getChannel();
                ){
                FileLock lock=fileChannel.tryLock(0,Long.MAX_VALUE,true);
                MappedByteBuffer buffer=fileChannel.map(FileChannel.MapMode.READ_ONLY,0,4096);
                charBuffer =buffer.asCharBuffer();
                longBuffer =buffer.asLongBuffer();
                processesIds=new ArrayList<>();
                long id;
                while((id= longBuffer.get())!=0)
                {
                    processesIds.add(id);
                }
                notFree=false;
                lock.close();
            }
        }

    }
}
