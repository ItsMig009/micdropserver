import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

class Server {


    static boolean status = true;
    static int port = 50005;
    static int sampleRate = 8000; //44100
    static Queue<byte[]> concurrentLinkedQueue = new ConcurrentLinkedQueue<>();
    static AudioInputStream audioInputStream;
    static AudioFormat audioFormat;
    static int sampleSizeInBits =16;
    static int numOfChannels = 1;
    static boolean signed = true;
    static boolean bigEndian = false;

    public static void main(String[] args) throws Exception {

        System.out.println("==========TESTING LOG===========");
        byte[] bytes = new byte[1280];
        DatagramSocket serverSocket = new DatagramSocket(port);
        System.out.println("Created UDP Socket ready to listen on port:"+port);
        // Sampled analog signal linear representation
        audioFormat = new AudioFormat(sampleRate,
                sampleSizeInBits,
                numOfChannels,
                signed,
                bigEndian);
        System.out.println("Audio Format:" + audioFormat.toString());
        System.out.println("Sample frequency:" + sampleRate + " Hz per second");
        System.out.println("Sample size: " + sampleSizeInBits + " bits");

        if(numOfChannels == 1){
            System.out.println("Channel: Mono");
        }else if (numOfChannels == 2){
            System.out.println("Channel: Stereo");
        }else{
            System.out.println("There are more than 2 channels.");
        }
        System.out.println("PCM Signed Audio fun facts:");
        System.out.println("=========");
        System.out.println("PCM (Pulse Code Modulation)");
        System.out.println("One industry standard of sending audio " +
                "from the player or transmitter to the receiver or speaker.");
        System.out.println(" - PCM Signed: Whether the sample is signed or unsigned is needed to understand the range. ");
        System.out.println("     -> Unsigned, the sample range is 0..255 with a centerpoint of 128.");
        System.out.println("     -> Signed, the sample range is -128..127 with a centerpoint of 0.");
        System.out.println("         ---) If a PCM type is signed, the sign encoding is almost always 2's complement.\n " +
                           "         ---) In very rare cases, signed PCM audio is represented as a series of sign/magnitude coded numbers.");

        while (status) {
            System.out.println("listening on port:" + port);
            DatagramPacket incomingPacket = new DatagramPacket(bytes, bytes.length);
            serverSocket.receive(incomingPacket);
            concurrentLinkedQueue.add(incomingPacket.getData());

            if(incomingPacket.getData().length > 0){
                System.out.println("Incoming data...");
            } else{
                System.out.println("No data...");
            }

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(incomingPacket.getData());
            audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, incomingPacket.getLength());

            // A thread solve the problem of chunky audio
            new Thread(() -> toSpeaker(incomingPacket.getData())).start();
        }

    }

//Original implementation

    public static void toSpeaker(byte[] soundbytes) {
        try {
            DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
            SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);

            sourceDataLine.open(audioFormat);

            FloatControl volumeControl = (FloatControl) sourceDataLine.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(5.0f);

            sourceDataLine.start();
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            System.out.println("format? :" + sourceDataLine.getFormat());

            sourceDataLine.write(soundbytes, 0, soundbytes.length);
            System.out.println(soundbytes.toString());
            sourceDataLine.drain();
            sourceDataLine.close();

        } catch (Exception e) {
            System.out.println("Not working in speakers...");
            e.printStackTrace();
        }
    }
}
