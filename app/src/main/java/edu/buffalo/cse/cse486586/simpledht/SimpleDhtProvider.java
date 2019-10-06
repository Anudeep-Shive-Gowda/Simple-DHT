package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class SimpleDhtProvider extends ContentProvider {

    static final int SERVER_PORT = 10000;
    int msg_count=0;
    String  masterNode ="11108";
    String portStr=null;
    String myPort=null;
    String predecessor=null;
    String successor=null;
    String hashedPort=null;
    TreeSet<String> chord=new TreeSet<String>();
    TreeSet<String> chordUnHashed=new TreeSet<String>();
    TreeSet<String> localChordData = new TreeSet<String>();
    TreeSet<String> localChordDataUnHashed = new TreeSet<String>();
    Map<String,String> hashAndPortValue = new HashMap<String,String>();
    Map<String,String> localHashAndPortValue = new HashMap<String,String>();
    List<String> allKeyList = new ArrayList<String>();
    List<String> localKeyList = new ArrayList<String>();
    String relayCursorKey = null;
    String relayCursorVal= null;
    boolean queryFlag = false;
    String relayCursorKeyVal= null;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub


        if(selection.equals("@")){

            for(String localKey: localKeyList){
                localKeyList.remove(localKey);
                getContext().deleteFile(localKey);

            }
            return  0;
        }

        else if(selection.equals("*")){
            if(localChordData.size()==1){
                for(String localKey: localKeyList){
                    localKeyList.remove(localKey);
                    getContext().deleteFile(localKey);

                } return 0;
            }
            else {
                int del = starDelete();
                return  del;
            }



        }



        String deleteHashedQueryKey = null;
        String message =  null;
        int delete=0;
        try {
            deleteHashedQueryKey = genHash(selection);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        localChordData.add(deleteHashedQueryKey);
        Log.e("Chord At delete", localChordData + "");

        String deleteNode = localChordData.higher(deleteHashedQueryKey) == null ? localChordData.first() : localChordData.higher(deleteHashedQueryKey);
        Log.e("deleteNode", deleteNode);
        localChordData.remove(deleteHashedQueryKey);
        if (hashedPort.equals(deleteNode)) {
            localKeyList.remove(selection);
            getContext().deleteFile(selection);
        }
        else{
            try {

                Socket socket = null;
                /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(localHashAndPortValue.get(deleteNode)));
                // Log.e("ClntQueryTask:queryNode",msgs[0]);
                String originalPort = hashedPort;
                String qKey = selection;


                String finalInsertMsg = "delete" + "-" + originalPort + "-" + qKey;

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(finalInsertMsg);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                int keyValue = (Integer) input.readObject();

              // delete = keyValue;


                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


            //Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])
            // Log.e("ReturnedRealy", relayCursorVal);
            // relayCursorKey =relayCursorKeyVal.split("-")[0];

        }

        return 0;
    }

    public int starDelete(){

        List<String> allKeys = new ArrayList<String>();
        // allKeys.addAll(allKeyList);
        String  message=null;
        for (String srvrPort : localChordData) {
            try {

                Socket socket = null;
                /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(localHashAndPortValue.get(srvrPort)));
                // Log.e("ClntQueryTask:queryNode",msgs[0]);


                String finalInsertMsg = "keyList";

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(finalInsertMsg);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                List<String> qkeyLst = (List<String>) input.readObject();

                allKeys.addAll(qkeyLst);
                // Querresult = relayCursorKey + "-"+relayCursorVal;
                Log.e("relayCurser", relayCursorKey + "-" + relayCursorVal);

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

                /*References used*/
                /*https://developer.android.com/reference/android/os/AsyncTask*/
                /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/


                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        for(String allKey : allKeys ){
            String deleteHashedQueryKey = null;

            int delete=0;
            try {
                deleteHashedQueryKey = genHash(allKey);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            localChordData.add(deleteHashedQueryKey);
            Log.e("Chord At delete", localChordData + "");

            String deleteNode = localChordData.higher(deleteHashedQueryKey) == null ? localChordData.first() : localChordData.higher(deleteHashedQueryKey);
            Log.e("deleteNode", deleteNode);
            localChordData.remove(deleteHashedQueryKey);
            if (hashedPort.equals(deleteNode)) {
                localKeyList.remove(allKey);
                getContext().deleteFile(allKey);
            }

            else{
                try {

                    Socket socket = null;
                    /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */

                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(localHashAndPortValue.get(deleteNode)));
                    // Log.e("ClntQueryTask:queryNode",msgs[0]);
                    String originalPort = hashedPort;
                    String qKey = allKey;


                    String finalInsertMsg = "delete" + "-" + originalPort + "-" + qKey;

                    /*
                     * TODO: Fill in your client code that sends out a message.
                     */
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(finalInsertMsg);

                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    int keyValue = (Integer) input.readObject();

                    // delete = keyValue;


                    output.flush();
                    socket.close();

                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                //Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])
                // Log.e("ReturnedRealy", relayCursorVal);
                // relayCursorKey =relayCursorKeyVal.split("-")[0];

            }

        }

        return 0;
    }


    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {




       // new  publishTheChord().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"publishChord",masterNode);

        try {

            Socket socket=null;
            /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */
            // ArrayList<String> chordNodes = (ArrayList<String>)hashAndPortValue.values();

            socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                    Integer.parseInt(masterNode));


            //String clientHash = msgs[0];
            String shareMsg = "shareYourData" + "-" + "publishChord";

            /*
             * TODO: Fill in your client code that sends out a message.
             */
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            output.writeObject(shareMsg);

            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            Object[] inputArr =  (Object[])input.readObject();
            localChordData = (TreeSet<String>) inputArr[0];
            localHashAndPortValue= (HashMap<String,String>) inputArr[1];
            Log.e("Local Chord", localChordData + "");

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

            /*References used*/
            /*https://developer.android.com/reference/android/os/AsyncTask*/
            /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
            /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
            /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/


            output.flush();
            socket.close();

        } catch (UnknownHostException e) {
            Log.e(TAG, "ClientTask UnknownHostException");
        } catch (IOException e) {
            Log.e(TAG, "ClientTask socket IOException");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

      /*  try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        //addPredAndSucc(hashedPort);
        Log.e("SuccAndpred", predecessor+":"+successor);
        Log.e("Add to node Before",hashedPort);
        OutputStream f_0utStrm;
        String fileName=values.getAsString("key");
        String fileValue=values.getAsString("value");
        Log.e("File Val",fileValue);
        Log.e("Add to node After",hashedPort);
        try {
            String hashedfileName =genHash(fileName);
            Log.e("hashedfileName",hashedfileName);
            Log.e("Chord Size Insert",localChordData.size()+"");
            if(localChordData.size()==0){
                localChordData.add(hashedPort);
                localHashAndPortValue.put(hashedPort,myPort);
            }
            localChordData.add(hashedfileName);
            Log.e("Chord After fileNInsert",localChordData+"");
            // Log.e("Chord fileNInsertNNxxt",localChordData.higher(hashedfileName)+"");
            String insertNode= localChordData.higher(hashedfileName)==null?localChordData.first():localChordData.higher(hashedfileName);
            //  Log.e("NNNNN",localChordData.higher(hashedfileName));
            localChordData.remove(hashedfileName);
            Log.e("Add to node",localHashAndPortValue.get(insertNode));
            allKeyList.add(fileName);
            if(hashedPort.equals(insertNode)) {
                localKeyList.add(fileName);
                Log.e("Equal",hashedPort+":"+insertNode);
                f_0utStrm = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                f_0utStrm.write(fileValue.getBytes());
                f_0utStrm.close();
            }
            else  {

                new ClientInsertTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,fileName,fileValue, localHashAndPortValue.get(insertNode), hashedPort);
                // localChordData.remove(hashedfileName);
            }
        } catch (Exception e) {
            Log.e(TAG, "File write failed");
        }


        return uri;
    }
    private class publishTheChord extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                Socket socket=null;
                /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */
                // ArrayList<String> chordNodes = (ArrayList<String>)hashAndPortValue.values();

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(masterNode));


                String clientHash = msgs[0];
                String shareMsg = "shareYourData" + "-" + clientHash;

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(shareMsg);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                Object[] inputArr =  (Object[])input.readObject();
                localChordData = (TreeSet<String>) inputArr[0];
                localHashAndPortValue= (HashMap<String,String>) inputArr[1];
                Log.e("Local Chord", localChordData + "");

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

                /*References used*/
                /*https://developer.android.com/reference/android/os/AsyncTask*/
                /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/


                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    private class ClientInsertTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                Socket socket=null;
                /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(msgs[2]));

                String fileName = msgs[0];
                String fileValue = msgs[1];

                String  finalInsertMsg = "insert"+"-"+fileName + "-" +fileValue;

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(finalInsertMsg);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                String msg = (String)input.readObject();
                Log.e("Local Chord", localChordData + "");

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

                /*References used*/
                /*https://developer.android.com/reference/android/os/AsyncTask*/
                /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/



                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
    @Override
    public boolean onCreate() {

        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
         portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
          myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        // new SimpleDhtActivity().onCreate(new Bundle());


        try {
            hashedPort = genHash(portStr);

            Log.e("hash and its Port",hashedPort +":"+ myPort);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e("InsideOncrate", "Can't create a ServerSocket");
            return  false;
        }

        if(!myPort.equals(masterNode)){
            Log.e("Join request",hashedPort +":"+ myPort);
            new ClientJoinTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,hashedPort, masterNode,myPort);

        }
        else {
            chord.add(hashedPort);

            localChordData.add(hashedPort);
            localHashAndPortValue.put(hashedPort,myPort);
            hashAndPortValue.put(hashedPort,myPort);
            //addPredAndSucc(hashedPort);
        }
        // addPredAndSucc(hashedPort);

        return true;

    }

    private class ClientJoinTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            String  clientHash = msgs[0];
            try {

                Socket socket=null;
                /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */
                Log.e("InsideClient join",hashedPort +":"+ myPort);
                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(msgs[1]));
                //  socket.setSoTimeout(100);

                clientHash = msgs[0];
                String  finalJoinMsg = "join" + "-" +clientHash+"-"+msgs[2];

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                Log.e("InsideClient join",hashedPort +":"+ myPort);
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(finalJoinMsg);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                localChordData = (TreeSet<String>)input.readObject();
                //addPredAndSucc(clientHash);
                Log.e("Local Chord", localChordData + "");

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

                /*References used*/
                /*https://developer.android.com/reference/android/os/AsyncTask*/
                /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/



                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
               /* chord.add(clientHash);
                hashAndPortValue.put(clientHash,myPort);
                localChordData.add(clientHash);
                localHashAndPortValue.put(clientHash,myPort);
                masterNode=myPort;
                Log.e("ChordData", chord+"");
                Log.e("Master", masterNode);*/
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            String message;

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */

            try {

                while (true) {
                    Socket socket = serverSocket.accept();

                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    message = (String) input.readObject();
                    //publishProgress(message);

                    String[] remoteMessage = message.split("-");
                    String ReqMsg = remoteMessage[0];


                    ObjectOutputStream output = null;
                    Log.e("Join req in server", hashedPort + ":" + myPort);
                    Log.e("Chord server before", ":" + chord);
                    if (ReqMsg.equals("join")) {
                        String addClientHash = remoteMessage[1];
                        chord.add(addClientHash);
                        hashAndPortValue.put(addClientHash, remoteMessage[2]);
                        // addPredAndSucc(addClientHash);
                        output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeObject(chord);
                        Log.e("Chord server After", ":" + chord);
                    } else if (ReqMsg.equals("insert")) {
                        localKeyList.add(remoteMessage[1]);
                        OutputStream f_0utStrm = getContext().openFileOutput(remoteMessage[1], Context.MODE_PRIVATE);
                        f_0utStrm.write(remoteMessage[2].getBytes());
                        output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeObject("InsertDone");
                        f_0utStrm.close();
                    } else if (ReqMsg.equals("shareYourData")) {
                        localChordData.addAll(chord);
                        localHashAndPortValue.putAll(hashAndPortValue);
                        output = new ObjectOutputStream(socket.getOutputStream());
                        Object[] objArr = {chord, hashAndPortValue};
                        output.writeObject(objArr);
                    } else if (ReqMsg.equals("query")) {
                        try {
                            Log.e("InsidequeryServer", remoteMessage[2]);

                            /*Reference-https://developer.android.com/reference/android/content/Context#openFileInput(java.lang.String)*/
                            InputStream f_inStrm = getContext().openFileInput(remoteMessage[2]);
                            /*Reference-https://stackoverflow.com/questions/2864117/read-data-from-a-text-file-using-java*/
                            BufferedReader br = new BufferedReader(new InputStreamReader(f_inStrm));
                            message = br.readLine();
                            f_inStrm.close();
                            Log.e("InsidequeryServerWritng", remoteMessage[2] + "-" + message);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                        /*
                         * MatrixCursor takes column names as the constructor arguments*/
                        /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#MatrixCursor(java.lang.String[])*/

                        output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeObject(remoteMessage[2] + "-" + message);
                        queryFlag = true;
                        Log.e("InsidequeryServer", "wrote Cursor");
                    }

                    else if(ReqMsg.equals("keyList")){
                        Log.e("keyListServer", "wrote keyList");
                        output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeObject(localKeyList);
                        queryFlag = true;

                    }
                    else if(ReqMsg.equals("delete")){
                        localKeyList.remove(remoteMessage[2]);
                        getContext().deleteFile(remoteMessage[2]);
                        output = new ObjectOutputStream(socket.getOutputStream());
                        output.writeObject(1);
                    }
                    /*localChordData.addAll(chord);
                    localHashAndPortValue.putAll(hashAndPortValue);*/
                    Log.e("Master Chord", "" + chord);

                   /* DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String) input.readUTF();
                    publishProgress(message);

                    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(message);*/

                    output.flush();
                    socket.close();

                    /*References used*/
                    /*https://developer.android.com/reference/android/os/AsyncTask*/
                    /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                    /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                    /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/


                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            return null;
        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        /*
         * TODO: You need to implement this method. Note that you need to return a Cursor object
         * with the right format. If the formatting is not correct, then it is not going to work.
         *
         * If you use SQLite, whatever is returned from SQLite is a Cursor object. However, you
         * still need to be careful because the formatting might still be incorrect.
         *
         * If you use a file storage option, then it is your job to build a Cursor * object. I
         * recommend building a MatrixCursor described at:
         * http://developer.android.com/reference/android/database/MatrixCursor.html
         */


        InputStream f_inStrm;
        String message="";
        String qPort=myPort;
        String hashedQueryKey=null;


         if(selection.equals("@")){
            MatrixCursor  cursor = new MatrixCursor(new String[] {"key", "value"});
            for(String localKey: localKeyList){
                try {

                    /*Reference-https://developer.android.com/reference/android/content/Context#openFileInput(java.lang.String)*/
                    f_inStrm = getContext().openFileInput(localKey);
                    /*Reference-https://stackoverflow.com/questions/2864117/read-data-from-a-text-file-using-java*/
                    BufferedReader br = new BufferedReader(new InputStreamReader(f_inStrm));
                    message = br.readLine();
                    f_inStrm.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.v("query", selection);
                /*
                 * MatrixCursor takes column names as the constructor arguments*/
                /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#MatrixCursor(java.lang.String[])*/
                //MatrixCursor cursor = new MatrixCursor(new String[] {"key", "value"});
                /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])*/

                cursor.addRow(new String[] {localKey,message});


            }
            return  cursor;
        }
         if(selection.equals("*")){
             if(localChordData.size()==1){
                 MatrixCursor cursor = new MatrixCursor(new String[] {"key", "value"});
                 for(String allKey : allKeyList){
                     try {
                         /*Reference-https://developer.android.com/reference/android/content/Context#openFileInput(java.lang.String)*/
                         f_inStrm = getContext().openFileInput(allKey);
                         /*Reference-https://stackoverflow.com/questions/2864117/read-data-from-a-text-file-using-java*/
                         BufferedReader br = new BufferedReader(new InputStreamReader(f_inStrm));
                         message = br.readLine();
                         f_inStrm.close();

                     } catch (FileNotFoundException e) {
                         e.printStackTrace();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     Log.v("query", selection);
                     /*
                      * MatrixCursor takes column names as the constructor arguments*/
                     /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#MatrixCursor(java.lang.String[])*/

                     /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])*/
                     cursor.addRow(new String[] {allKey,message});



                 }
                 return  cursor;
             }
             else {
                 MatrixCursor cursor = startQuery();
                 return  cursor;
             }
         }


        try {
            hashedQueryKey=genHash(selection);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        localChordData.add(hashedQueryKey);
        Log.e("Chord At query",localChordData+"");
        // Log.e("Chord fileNInsertNNxxt",localChordData.higher(hashedfileName)+"");
        String queryNode= localChordData.higher(hashedQueryKey)==null?localChordData.first():localChordData.higher(hashedQueryKey);
        Log.e("queryNode",queryNode);
        localChordData.remove(hashedQueryKey);
        if(hashedPort.equals(queryNode)) {
            try {
                /*Reference-https://developer.android.com/reference/android/content/Context#openFileInput(java.lang.String)*/
                f_inStrm = getContext().openFileInput(selection);
                /*Reference-https://stackoverflow.com/questions/2864117/read-data-from-a-text-file-using-java*/
                BufferedReader br = new BufferedReader(new InputStreamReader(f_inStrm));
                message = br.readLine();
                f_inStrm.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.v("query", selection);
            /*
             * MatrixCursor takes column names as the constructor arguments*/
            /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#MatrixCursor(java.lang.String[])*/
            MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
            /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])*/

            cursor.addRow(new String[]{selection, message});

            return cursor;
        }



        else {

           // new ClientQueryTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, localHashAndPortValue.get(queryNode), hashedPort,selection);

            try {

                Socket socket=null;
                /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(localHashAndPortValue.get(queryNode)));
               // Log.e("ClntQueryTask:queryNode",msgs[0]);
                String originalPort = hashedPort;
                String qKey=selection;


                String  finalInsertMsg = "query"+"-"+originalPort+"-"+qKey;

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(finalInsertMsg);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                String keyValue = (String)input.readObject();

                relayCursorKey = keyValue.split("-")[0];
                relayCursorVal= keyValue.split("-")[1];
                queryFlag=true;
               // Querresult = relayCursorKey + "-"+relayCursorVal;
                Log.e("relayCurser", relayCursorKey + "-"+relayCursorVal);

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

                /*References used*/
                /*https://developer.android.com/reference/android/os/AsyncTask*/
                /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/



                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


                    MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
                    //Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])
                   // Log.e("ReturnedRealy", relayCursorVal);
                    // relayCursorKey =relayCursorKeyVal.split("-")[0];
                    //relayCursorVal = relayCursorKeyVal.split("-")[1];
                    Log.e("ReturnedRealy", "QueryFlag");
                    cursor.addRow(new String[]{relayCursorKey, relayCursorVal});
                    queryFlag = false;
                    return cursor;

        }
        // =Log.e("ReturnedRealy", relayCursorVal);


    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public  MatrixCursor startQuery(){

        List<String> allKeys = new ArrayList<String>();
        // allKeys.addAll(allKeyList);
        String  message=null;
        for (String srvrPort : localChordData) {
            try {

                Socket socket = null;
                /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */

                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                        Integer.parseInt(localHashAndPortValue.get(srvrPort)));
                // Log.e("ClntQueryTask:queryNode",msgs[0]);


                String finalInsertMsg = "keyList";

                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                output.writeObject(finalInsertMsg);

                ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                List<String> qkeyLst = (List<String>) input.readObject();

                allKeys.addAll(qkeyLst);
                // Querresult = relayCursorKey + "-"+relayCursorVal;
                Log.e("relayCurser", relayCursorKey + "-" + relayCursorVal);

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

                /*References used*/
                /*https://developer.android.com/reference/android/os/AsyncTask*/
                /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/


                output.flush();
                socket.close();

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
        for (String allk : allKeys) {
            String starHashedQueryKey = null;
            try {
                starHashedQueryKey = genHash(allk);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            localChordData.add(starHashedQueryKey);
            Log.e("Chord At query", localChordData + "");
            // Log.e("Chord fileNInsertNNxxt",localChordData.higher(hashedfileName)+"");
            String starQueryNode = localChordData.higher(starHashedQueryKey) == null ? localChordData.first() : localChordData.higher(starHashedQueryKey);
            Log.e("queryNode", starQueryNode);
            localChordData.remove(starHashedQueryKey);

            if (hashedPort.equals(starQueryNode)) {
                try {
                    /*Reference-https://developer.android.com/reference/android/content/Context#openFileInput(java.lang.String)*/
                   InputStream f_inStrm = getContext().openFileInput(allk);
                    /*Reference-https://stackoverflow.com/questions/2864117/read-data-from-a-text-file-using-java*/
                    BufferedReader br = new BufferedReader(new InputStreamReader(f_inStrm));
                    message = br.readLine();
                    f_inStrm.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.v("query", allk);
                /*
                 * MatrixCursor takes column names as the constructor arguments*/
                /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#MatrixCursor(java.lang.String[])*/
                // MatrixCursor cursor = new MatrixCursor(new String[]{"key", "value"});
                /*Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])*/

                 cursor.addRow(new String[]{allk, message});
            } else {
                try {

                    Socket socket = null;
                    /*Creating a new socket connections for each AVDs and multi-casting message to all of them and to itself */

                    socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(localHashAndPortValue.get(starQueryNode)));
                    // Log.e("ClntQueryTask:queryNode",msgs[0]);
                    String originalPort = hashedPort;
                    String qKey = allk;


                    String finalInsertMsg = "query" + "-" + originalPort + "-" + qKey;

                    /*
                     * TODO: Fill in your client code that sends out a message.
                     */
                    ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                    output.writeObject(finalInsertMsg);

                    ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
                    String keyValue = (String) input.readObject();

                    relayCursorKey = keyValue.split("-")[0];
                    relayCursorVal = keyValue.split("-")[1];
                    queryFlag = true;
                    // Querresult = relayCursorKey + "-"+relayCursorVal;
                    Log.e("relayCurser", relayCursorKey + "-" + relayCursorVal);

                   /* DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                    output.writeUTF(msgToSend);

                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    message = (String)input.readUTF();*/

                    /*References used*/
                    /*https://developer.android.com/reference/android/os/AsyncTask*/
                    /*https://developer.android.com/reference/java/io/DataInputStream and https://developer.android.com/reference/java/io/DataOutputStream*/
                    /*https://docs.oracle.com/javase/tutorial/networking/sockets/*/
                    /*https://stackoverflow.com/questions/28187038/tcp-client-server-program-datainputstream-dataoutputstream-issue*/


                    output.flush();
                    socket.close();

                } catch (UnknownHostException e) {
                    Log.e(TAG, "ClientTask UnknownHostException");
                } catch (IOException e) {
                    Log.e(TAG, "ClientTask socket IOException");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                //Reference-https://developer.android.com/reference/android/database/MatrixCursor#addRow(java.lang.Object[])
                // Log.e("ReturnedRealy", relayCursorVal);
                // relayCursorKey =relayCursorKeyVal.split("-")[0];
                //relayCursorVal = relayCursorKeyVal.split("-")[1];
                Log.e("ReturnedRealy", "QueryFlag");
                cursor.addRow(new String[]{relayCursorKey, relayCursorVal});
            }
        }
        return  cursor;
    }
}
