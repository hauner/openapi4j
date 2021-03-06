package org.openapi4j.operation.validator.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import org.openapi4j.core.util.IOUtil;
import org.openapi4j.core.util.TreeUtil;
import org.openapi4j.parser.model.v3.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Convert supported media types to abstract tree nodes.
 */
public final class ContentConverter {
  private ContentConverter() {
  }

  public static JsonNode convert(final Schema schema,
                                 final String rawContentType,
                                 final InputStream is,
                                 final String str) throws IOException {

    String contentType = ContentType.getTypeOnly(rawContentType);

    if (ContentType.isJson(contentType)) {
      return is != null ? jsonToNode(is) : jsonToNode(str);
    } else if (ContentType.isXml(contentType)) {
      return is != null ? xmlToNode(schema, is) : xmlToNode(schema, str);
    } else if (ContentType.isFormUrlEncoded(contentType)) {
      return is != null ? formUrlEncodedToNode(schema, rawContentType, is) : formUrlEncodedToNode(schema, rawContentType, str);
    } else if (ContentType.isMultipartFormData(contentType)) {
      return is != null ? multipartToNode(schema, rawContentType, is) : multipartToNode(schema, rawContentType, str);
    } else { // UNKNOWN
      return is != null ? textToNode(is) : textToNode(str);
    }
  }

  private static JsonNode formUrlEncodedToNode(final Schema schema, final String rawContentType, final InputStream content) throws IOException {
    String encoding = ContentType.getCharSet(rawContentType);
    return FormUrlConverter.instance().formUrlEncodedToNode(schema, content, encoding);
  }

  private static JsonNode formUrlEncodedToNode(final Schema schema, final String rawContentType, final String content) {
    String encoding = ContentType.getCharSet(rawContentType);
    return FormUrlConverter.instance().formUrlEncodedToNode(schema, content, encoding);
  }

  private static JsonNode multipartToNode(final Schema schema, final String rawContentType, InputStream content) throws IOException {
    String encoding = ContentType.getCharSet(rawContentType);
    return MultipartConverter.instance().multipartToNode(schema, content, rawContentType, encoding);
  }

  private static JsonNode multipartToNode(final Schema schema, final String rawContentType, final String content) throws IOException {
    String encoding = ContentType.getCharSet(rawContentType);
    return MultipartConverter.instance().multipartToNode(schema, content, rawContentType, encoding);
  }

  private static JsonNode jsonToNode(InputStream content) throws IOException {
    return TreeUtil.json.readTree(content);
  }

  private static JsonNode jsonToNode(String content) throws IOException {
    return TreeUtil.json.readTree(content);
  }

  private static JsonNode xmlToNode(final Schema schema, InputStream content) throws IOException {
    return XmlConverter.instance().xmlToNode(schema, IOUtil.toString(content, StandardCharsets.UTF_8.name()));
  }

  private static JsonNode xmlToNode(final Schema schema, String content) {
    return XmlConverter.instance().xmlToNode(schema, content);
  }

  private static JsonNode textToNode(InputStream content) throws IOException {
    return JsonNodeFactory.instance.textNode(IOUtil.toString(content, StandardCharsets.UTF_8.name()));
  }

  private static JsonNode textToNode(String content) {
    return JsonNodeFactory.instance.textNode(content);
  }

  public static JsonNode mapToNode(final Schema schema, final Map<String, Object> content) {
    return TypeConverter.instance().convertObject(schema, content);
  }
}
