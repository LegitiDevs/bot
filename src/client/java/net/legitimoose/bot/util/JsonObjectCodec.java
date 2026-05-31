package net.legitimoose.bot.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.DocumentCodec;
import org.bson.codecs.EncoderContext;

public class JsonObjectCodec implements Codec<JsonObject> {
    private static final DocumentCodec DOCUMENT_CODEC = new DocumentCodec();

    @Override
    public void encode(BsonWriter writer,
                       JsonObject value,
                       EncoderContext encoderContext) {
        Document document = Document.parse(value.toString());

        DOCUMENT_CODEC.encode(writer, document, encoderContext);
    }

    @Override
    public JsonObject decode(BsonReader reader,
                             DecoderContext decoderContext) {
        Document doc = DOCUMENT_CODEC.decode(reader, decoderContext);
        return JsonParser.parseString(doc.toJson()).getAsJsonObject();
    }

    @Override
    public Class<JsonObject> getEncoderClass() {
        return JsonObject.class;
    }
}