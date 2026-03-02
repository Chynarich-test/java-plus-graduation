package ru.yandex.practicum.avro.serializer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GeneralAvroSerializer implements Serializer<SpecificRecordBase> {
    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) return null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] schemaNameBytes = data.getSchema().getFullName()
                    .getBytes(StandardCharsets.UTF_8);
            out.write(ByteBuffer.allocate(2)
                    .putShort((short) schemaNameBytes.length)
                    .array());
            out.write(schemaNameBytes);
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);
            DatumWriter<SpecificRecordBase> writer =
                    new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();

            return out.toByteArray();
        } catch (IOException ex) {
            throw new SerializationException("Ошибка сериализации данных для топика [" + topic + "]", ex);
        }
    }
}