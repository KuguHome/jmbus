package org.openmuc.jmbus;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.openmuc.jmbus.DataRecord.DataValueType;

public class VariableDataStructureTest {

    @Test
    public void test1() throws Exception {
        SecondaryAddress linkLayerSecondaryAddress = SecondaryAddress
                .newFromWMBusLlHeader(DatatypeConverter.parseHexBinary("2423759468372507"), 0);

        final byte[] key = "HalloWorldTestPW".getBytes();
        Map<SecondaryAddress, byte[]> keyMap = new HashMap<>();
        keyMap.put(linkLayerSecondaryAddress, key);

        byte[] encrypt = DatatypeConverter.parseHexBinary(
                "7ACB5030055E861434F34A14AE2B9973AEE9811E32578336455E9AC7E7EF960B2253CA7F2BB6632C35E3DD95D66FE96C699A298A53");

        VariableDataStructure vds = new VariableDataStructure(encrypt, 0, encrypt.length, linkLayerSecondaryAddress,
                keyMap);
        vds.decode();

        System.out.println(vds);

        assertEquals(203, vds.getAccessNumber());
        assertEquals(80, vds.getStatus());
        assertEquals(EncryptionMode.AES_CBC_IV, vds.getEncryptionMode());
        assertEquals(3, vds.getNumberOfEncryptedBlocks());
        List<DataRecord> dataRecords = vds.getDataRecords();

        DataRecord dr = dataRecords.get(0);
        assertEquals(12, dr.getDataLength());
        assertEquals(DataValueType.BCD, dr.getDataValueType());
        assertArrayEquals(new byte[] { 12 }, dr.getDib());

    }

    @Test
    public void testEFE() throws Exception {
        SecondaryAddress linkLayerSecondaryAddress = SecondaryAddress
                .newFromWMBusLlHeader(DatatypeConverter.parseHexBinary("C514764008640004"), 0);

        final byte[] key = "51728910E66D83F851728910E66D83F8".getBytes();
        Map<SecondaryAddress, byte[]> keyMap = new HashMap<>();
        keyMap.put(linkLayerSecondaryAddress, key);

        byte[] encrypt = DatatypeConverter
                .parseHexBinary("7AAC203025CAC22DAFA08BB3BC431CD58D856E147C329E61EE3A26490F27DCCEFE8C121574787D56");

        VariableDataStructure vds = new VariableDataStructure(encrypt, 0, encrypt.length, linkLayerSecondaryAddress,
                keyMap);
        vds.decode();

        System.out.println(vds);

        assertEquals(203, vds.getAccessNumber());
        assertEquals(80, vds.getStatus());
        assertEquals(EncryptionMode.AES_CBC_IV, vds.getEncryptionMode());
        assertEquals(3, vds.getNumberOfEncryptedBlocks());
        List<DataRecord> dataRecords = vds.getDataRecords();

        DataRecord dr = dataRecords.get(0);
        assertEquals(12, dr.getDataLength());
        assertEquals(DataValueType.BCD, dr.getDataValueType());
        assertArrayEquals(new byte[] { 12 }, dr.getDib());

    }
}
