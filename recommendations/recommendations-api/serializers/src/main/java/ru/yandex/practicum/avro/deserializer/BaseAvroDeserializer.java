package ru.yandex.practicum.avro.deserializer;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {
    private final DecoderFactory decoderFactory;
    private final DatumReader<T> reader;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        reader = new SpecificDatumReader<>(schema);
    }


    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(data);

            short schemaNameLength = buffer.getShort();

            buffer.position(buffer.position() + schemaNameLength);

            BinaryDecoder decoder = decoderFactory.binaryDecoder(
                    data, buffer.position(), buffer.remaining(), null);
            return this.reader.read(null, decoder);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации данных из топика " + topic, e);
        }
    }
}
