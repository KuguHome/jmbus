/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.transportlayer.TransportLayer;

class WMBusConnectionCUL extends AbstractWMBusConnection {
	
	private BufferedReader br;
	private BufferedWriter bw;

    private class MessageReceiverImpl extends MessageReceiver {

        private static final byte CONTROL_BYTE = 0x44;
    	
        private final TransportLayer transportLayer;

        private final byte[] discardBuffer = new byte[BUFFER_LENGTH];
        private int bufferPointer = 0;
        


        public MessageReceiverImpl(TransportLayer transportLayer, WMBusListener listener) {
            super(listener);
            this.transportLayer = transportLayer;
        }

        @Override
        public void run() {
            try {

                while (!isClosed()) {
                    byte[] messageData = initMessageData();
                    int len = messageData.length - 2;
                    handleData(messageData, len);
                }
            } catch (final IOException e) {
                if (!isClosed()) {
                    super.notifyStoppedListening(e);
                }

            } finally {
                close();
                super.shutdown();
            }

        }
        
        private void handleData(byte[] messageData, int len) throws IOException {
            try {
                int numReadBytes = getInputStream().read(messageData, 2, len);

                if (len == numReadBytes) {
                    notifyListener(messageData);
                }
                else {
                    discard(messageData, 0, numReadBytes + 2);
                }
            } catch (InterruptedIOException e) {
                discard(messageData, 0, 2);
            }
        }
        
        private byte[] initMessageData() throws IOException {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e1) {	}

            byte b0, b1;
            while (true) {
//                this.transportLayer.setTimeout(MESSAGE_FRAGEMENT_TIMEOUT);
                do {
                    System.out.println(23);
                    String line = br.readLine();
                    System.out.println(line);
                    b0 = (byte) line.charAt(0);
//                	b0 = getInputStream().readByte();
                    System.out.println(24);

                } while (b0 == -1);

                try {
                	// we have to wait for a short moment as otherwise
                	// the next byte will be -1 immediately as well
					Thread.sleep(50);
				} catch (InterruptedException e) {}
                b1 = getInputStream().readByte();
                if(b1 == -1) {
                	// we ran into a timeout
                	continue;
                }

                if (b1 == CONTROL_BYTE) {
                    break;
                }

                discardBuffer[bufferPointer++] = b0;
                discardBuffer[bufferPointer++] = b1;

                if (bufferPointer - 2 >= discardBuffer.length) {
                    discard(discardBuffer, 0, bufferPointer);
                    bufferPointer = 0;
                }

            }

            int messageLength = b0 & 0xff;

            final byte[] messageData = new byte[messageLength + 1];
            messageData[0] = b0;
            messageData[1] = b1;

            return messageData;
        }

        private void notifyListener(final byte[] messageBytes) {
            messageBytes[0] = (byte) (messageBytes[0] - 1);
            int rssi = messageBytes[messageBytes.length - 1] & 0xff;

            final int signalStrengthInDBm = (rssi * -1) / 2;
            try {
                super.notifyNewMessage(WMBusMessage.decode(messageBytes, signalStrengthInDBm, keyMap));
            } catch (DecodingException e) {
                super.notifyDiscarded(messageBytes);
            }
        }

        private void discard(byte[] buffer, int offset, int length) {
            final byte[] discardedBytes = Arrays.copyOfRange(buffer, offset, offset + length);

            super.notifyDiscarded(discardedBytes);
        }

    }



    public WMBusConnectionCUL(WMBusMode mode, WMBusListener listener, TransportLayer tl) {
        super(mode, listener, tl);
    }

    @Override
    protected MessageReceiver newMessageReceiver(TransportLayer transportLayer, WMBusListener listener) {
        return new MessageReceiverImpl(transportLayer, listener);
    }

    /**
     * @param mode
     *            - the wMBus mode to be used for transmission
     * @throws IOException
     */
    @Override
    protected void initializeWirelessTransceiver(WMBusMode mode) throws IOException {

        DataOutputStream os = getOutputStream();
        
        br = new BufferedReader(new InputStreamReader(getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(os));

//        bw.flush();
        bw.write("brt\r\n");
        bw.flush();
    	System.out.println("00"); 
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e1) {	}
        String line = br.readLine();
//    	System.out.println("01");
    	System.out.println(line); 
        if (!line.equals("TMODE")) {
        	System.out.println("CLOSING");
            close();
        }


    }

}
