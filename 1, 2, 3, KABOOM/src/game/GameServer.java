package game;
import java.io.*;
import java.net.*;

public class GameServer {

    private ServerSocket ss;
    private int numPlayers;
    private int maxPlayers;

    private Socket p1Socket;
    private Socket p2Socket;
    private ReadFromClient p1ReadRunnable;
    private ReadFromClient p2ReadRunnable;
    private WriteToClient p1WriteRunnable;
    private WriteToClient p2WriteRunnable;

    private int p1x, p1y, p2x, p2y;

    public GameServer(){
        System.out.println("===== GAME SERVER =====");
        numPlayers = 0;
        maxPlayers = 2;

        p1x = 100;
        p1y = 250;
        p2x = 200;
        p2y = 500;

        try{
            ss = new ServerSocket(12345); // connect
        }catch(IOException ex){
            System.out.println("IOException from GameServer constructor");
        }
    }

    public void acceptConnections(){
        try{
            System.out.println("Waiting for connections....");

            while(numPlayers < maxPlayers){
                Socket s = ss.accept();
                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());

                numPlayers++;
                out.writeInt(numPlayers);
                System.out.println("Player #" + numPlayers + " has connected.");

                ReadFromClient rfc = new ReadFromClient(numPlayers, in);
                WriteToClient wtc = new WriteToClient(numPlayers, out);

                if(numPlayers==1){
                    p1Socket = s;
                    p1ReadRunnable = rfc;
                    p1WriteRunnable = wtc;
                }else if(numPlayers==2){
                    p2Socket = s;
                    p2ReadRunnable = rfc;
                    p2WriteRunnable = wtc;

                    // since this is the last player for now
                    p1WriteRunnable.sendStartMsg();
                    p2WriteRunnable.sendStartMsg();
                }
            }


            System.out.println("No longer accepting responses");

        }catch(IOException ex){
            System.out.println("IOException from acceptConnections()");
        }
    }

    private class WriteToClient implements Runnable{
        private int playerID;
        private DataOutputStream dataOut;

        public WriteToClient(int pid, DataOutputStream out){
            playerID = pid;
            dataOut = out;
            System.out.println("WTC"+playerID+" Runnable created");
        }

        public void run(){
            try{
                while(true){
                    if(playerID == 1){
                        dataOut.writeInt(p2x);
                        dataOut.writeInt(p2y);
                        dataOut.flush();
                    }
                    if(playerID == 2){
                        dataOut.writeInt(p1x);
                        dataOut.writeInt(p1y);
                        dataOut.flush();
                    }

                    try{
                        Thread.sleep(25);
                    }catch(InterruptedException ex){
                        System.out.println("InterruptedException from WTC run()");
                    }
                }
            }catch(IOException ex){
                System.out.println("IOException from WTC run()");
            }
        }

        public void sendStartMsg(){
            try{
                dataOut.writeUTF("We now havw 2 players. GO!");
            }catch(IOException ex){
                System.out.println("IOException from sendStartMsg()");
            }
        }
    }

    private class ReadFromClient implements Runnable{
        private int playerID;
        private DataInputStream dataIn;

        public ReadFromClient(int pid, DataInputStream in){
            playerID = pid;
            dataIn = in;
            System.out.println("RFC"+playerID+" Runnable created");
        }

        public void run(){
            try{
                while(true){
                    if(playerID == 1){
                        p1x = dataIn.readInt();
                        p1y = dataIn.readInt();
                    }
                    if(playerID == 2){
                        p2x = dataIn.readInt();
                        p2y = dataIn.readInt();
                    }
                }
            }catch(IOException ex){
                System.out.println("IOException from RFC run()");
            }
        }
    }

    public static void main(String[] args) {
        //system.out.println("Hello, World!");
        GameServer gs = new GameServer();
        gs.acceptConnections();
    }

}
