package converter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        try {
            String s = new String(Files.readAllBytes(Paths.get("test.txt")));
            if (s.charAt(0) == '<') {
                xmlToJson(s);
            } else if (s.charAt(0) == '{') {
                jsonToXml(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void jsonToXml(String text) {
        StringBuilder sb = new StringBuilder();
        String value = "";
        text = text.replaceAll("\\s+", " ");

        Matcher matcher = Pattern.compile("(?<=\")[0-9a-z]+", Pattern.CASE_INSENSITIVE).matcher(text);
        matcher.find();
        String tagName = matcher.group();

        // open tag and insert tag name
        sb.append("<").append(tagName);

        if (!text.contains("#")) { //it does not have attributes

            value = text.replaceAll("\\{\"\\w*\\W.*?\"", "").replaceAll("\"}", "");
            if (value.isEmpty() || "null".equals(value)) {
                sb.append("/>");
            } else {
                sb.append(">").append(value).append("</").append(tagName).append(">");
            }
        } else {  //it has attributes
            value = text.replaceAll("\\{.*?\"\\w*\\W.*?#\\w*\"\\W*", "").replaceAll("\"*}*", "").trim();

            String attribute = "";
            String attributeValue = "";
            matcher = Pattern.compile(":\\s*\\{.*\"#", Pattern.CASE_INSENSITIVE).matcher(text);
            String tagWithAttributes = "";
            if (matcher.find()) {
                tagWithAttributes = matcher.group();
            }
            matcher = Pattern.compile("([0-9a-z]+|\".*?\")", Pattern.CASE_INSENSITIVE).matcher(tagWithAttributes);
            while (matcher.find()) {
                attribute = matcher.group().replaceAll("@", "").replaceAll("\"", "");
                matcher.find();
                attributeValue = matcher.group();
                if (attributeValue.matches("\\d*")) {
                    attributeValue = "\"" + attributeValue + "\"";
                }
                sb.append(" ").append(attribute).append(" = ").append(attributeValue);
            }
            if (value.isEmpty() || "null".equals(value)) {
                sb.append("/>");
            } else {
                sb.append(">").append(value).append("</").append(tagName).append(">");
            }

        }
        System.out.println(sb.toString());

    }

    public static void xmlToJson(String s) {
        StringBuilder sb = new StringBuilder();

        Matcher tagMatcher = Pattern.compile("(?<=<).*?(?=>)").matcher(s); //matches text between tags ( anything between < and >  )
        Matcher attributeMatcher = Pattern.compile("\\b\\w+\\s=\\s.*?\\b\"").matcher(s); //matches attributes in form of attribute = "value"
        Matcher contentMatcher = Pattern.compile("(?<=>).*(?=<)").matcher(s); //gets value between tags
        Matcher matchTagName = Pattern.compile("\\b\\w+\\b").matcher(s);

        matchTagName.find();
        String tagName = matchTagName.group();

        tagMatcher.find();
        String tag = tagMatcher.group().trim();

        String content = "";
        if (contentMatcher.find()) {
            content = contentMatcher.group();
        }

        sb.append("{ \"").append(tagName).append("\" : ");

        //if it contains space after trimming, it should contain attributes
        if (tag.contains(" ")) {
            sb.append(" { ");
            //extract attributes
            while (attributeMatcher.find()) {
                String[] attr = attributeMatcher.group().split("=");
                //append "@attribute1" : "attribute1_value",
                sb.append("\"@").append(attr[0].trim()).append("\" : ").append(attr[1].trim()).append(", ");
            }
            // append #tagname : value
            sb.append("\"#").append(tagName).append("\" : ");
            if (content.isEmpty()) {
                sb.append("null } ");
            } else {
                sb.append("\"").append(content).append("\" } ");
            }
        } else {
            // it does not have attributes, append content
            sb.append("\"").append(content).append("\" ");
        }

        sb.append("}");
        System.out.println(sb.toString());
    }

}
