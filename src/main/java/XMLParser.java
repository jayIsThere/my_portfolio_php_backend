import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class XMLParser {
  private static final ArrayList<Map<String, String>> xmlData = new ArrayList<>();
  private static final String OUTPUT_FILE = "C:\\xampp1\\htdocs\\ins_project\\outputfile.xml";

  public static void main(String[] args) {
    String directoryPath = "C:\\XAMPP1\\htdocs\\ins_project"; //not "XAMPP", but "XAMPP1"!
    File directory = new File(directoryPath);

    File[] xmlFiles = directory.listFiles((dir, name) -> name.toLowerCase().startsWith("feedback_") && name.toLowerCase().endsWith(".xml"));

    if (!directory.isDirectory()) {
      System.out.println("The folder does not exist");
      System.exit(1);
    }

    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    factory.setNamespaceAware(true);

    // Kontrolle ob XML-Dateien existieren, File Array erstellen
    if (xmlFiles == null || xmlFiles.length == 0) {
      System.out.println("No XML Data.");
      System.exit(0);
    }

    // Über die Dateien iterieren und diese parsen
    for (File xmlFile : xmlFiles) {
      try {
        SAXParser saxParser = factory.newSAXParser();
        System.out.println("Current Data: " + xmlFile.getName());

        // Parse die Datei mit dem benutzerdefinierten Handler
        XMLHandler handler = new XMLHandler();
        saxParser.parse(xmlFile, handler);

        // Daten in die Liste hinzufügen
        xmlData.add(handler.getData());

        System.out.println("The Data is successfully parsed: " + xmlFile.getName());
      } catch (Exception e) {
        System.out.println("Error in the data: " + xmlFile.getName());
        System.out.println("Error message: " + e.getMessage());
        System.exit(1);
      }
    }

    // Proceed to calculate statistics and write XML file if no errors occurred
    repeaters();
    writeToXMLFile();
  }

  private static void repeaters() {
    int contentCount = 0;
    int repeatVisit = 0;

    for (Map<String, String> data : xmlData) {
      if (data == null) {
        continue;
      }

      contentCount++;
      if ("ja".equalsIgnoreCase(data.get("erneuter_besuch"))) {
        repeatVisit++;
      }
    }

    double repeatVisitPercentage = contentCount > 0 ? (double) repeatVisit / contentCount * 100 : 0;

    System.out.printf("Percentage of people who want to revisit: %.2f%%%n", repeatVisitPercentage);
    System.out.println("Total feedback count: " + contentCount);
  }

  private static boolean writeToFile = true;  // 이 변수로 파일을 작성할지 여부를 제어합니다.

  private static void writeToXMLFile() {
    if (!writeToFile) {  // 유효하지 않은 값이 발견되면 파일 작성 중단
      System.err.println("Skipping file write due to invalid data.");
      return;
    }

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.newDocument();

      Element root = doc.createElement("feedbackdatenbank");
      doc.appendChild(root);

      for (Map<String, String> data : xmlData) {
        if (data == null) {
          continue;
        }


        // valid data에 대한 XML 생성 로직 계속...
        Element feedback = doc.createElement("feedback");
        root.appendChild(feedback);

        Element visitor = doc.createElement("besucher");
        visitor.setAttribute("anrede", data.get("anrede"));

        try {
          String vorname = data.getOrDefault("vorname", "");
          String nachname = data.getOrDefault("nachname", "");
          if (!validateName(vorname)) {
            System.err.println("Invalid first name: " + vorname);
            writeToFile = false;  // 유효하지 않은 데이터가 있을 경우, 파일 작성 중단
            return;  // 즉시 파일 작성 중단
          } else {
            visitor.setAttribute("vorname", vorname);
          }
          if (!validateName(nachname)) {
            System.err.println("Invalid last name: " + nachname);
            writeToFile = false;  // 유효하지 않은 데이터가 있을 경우, 파일 작성 중단
            return;  // 즉시 파일 작성 중단
          } else {
            visitor.setAttribute("nachname", nachname);
          }

        } catch (IllegalArgumentException e) {
          System.err.println("Invalid content rating found, skipping record: " + e.getMessage());
          writeToFile = false;  // 유효하지 않은 데이터가 있을 경우, 파일 작성 중단
          return;  // 즉시 파일 작성 중단
        }


        Element age = doc.createElement("alter");
        age.setTextContent(data.getOrDefault("alter", ""));
        visitor.appendChild(age);

        Element contact = doc.createElement("kontakt");
        optional(doc, contact, "emailadresse", data.get("emailadresse"));
        optional(doc, contact, "telefonnummer", data.get("telefonnummer"));
        visitor.appendChild(contact);

        feedback.appendChild(visitor);

        Element rating = doc.createElement("bewertung");
        rating.setAttribute("erneuter_besuch", mapBooleanToString(data.get("erneuter_besuch")));

//        try {
          String contentRating = data.get("note_inhalt");
//          if (contentRating != null && !validateNoteInhalt(contentRating)) {
//            System.err.println("Invalid content rating, skipping this record: " + contentRating);
//            writeToFile = false;  // 유효하지 않은 데이터가 있을 경우, 파일 작성 중단
//            return;  // 즉시 파일 작성 중단
//          } else {
            rating.setAttribute("note_inhalt", contentRating);
//          }
//        } catch (IllegalArgumentException e) {
//          System.err.println("Invalid content rating found, skipping record: " + e.getMessage());
//          writeToFile = false;  // 유효하지 않은 데이터가 있을 경우, 파일 작성 중단
//          return;  // 즉시 파일 작성 중단
//        }

        rating.setAttribute("note_aussehen", data.get("note_aussehen"));
        optional(doc, rating, "vorschlag", data.get("vorschlag"));
        feedback.appendChild(rating);

        Element info = doc.createElement("info");
        Element date = doc.createElement("datum");
        date.setTextContent(data.get("datum"));
        info.appendChild(date);
        Element time = doc.createElement("uhrzeit");
        time.setTextContent(data.get("uhrzeit"));
        info.appendChild(time);
        feedback.appendChild(info);
      }

      Element developer = doc.createElement("entwickler_parser");
      developer.setTextContent("This parser is developed by Jaehan Kim!");
      root.appendChild(developer);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "feedbackdatenbank.dtd");

      DOMSource source = new DOMSource(doc);
      StreamResult result = new StreamResult(new File(OUTPUT_FILE));
      transformer.transform(source, result);

      validateXMLFile(OUTPUT_FILE);

      System.out.println("XML data has been successfully written to " + OUTPUT_FILE);

    } catch (Exception e) {
      System.out.println("Error writing to XML file: " + e.getMessage());
    }
  }

  private static void validateXMLFile(String filePath) {
    try {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setValidating(true);
      factory.setNamespaceAware(true);

      SAXParser saxParser = factory.newSAXParser();

      // Error-Handler
      DefaultHandler handler = new DefaultHandler() {
        @Override
        public void error(SAXParseException e) throws SAXException {
          System.out.println("Failed to validate: " + e.getMessage());
          // Optional: Log the error without stopping the validation
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
          System.out.println("Fatal error: " + e.getMessage());
          // Optional: Log the fatal error without stopping the validation
        }

        @Override
        public void warning(SAXParseException e) {
          System.out.println("Warning: " + e.getMessage());
        }
      };

      // XML-Datei validieren
      saxParser.parse(new File(filePath), handler);
      System.out.println("The current XML data is successfully validated");
    } catch (Exception e) {
      System.out.println("The current XML data has an error: " + e.getMessage());
    }
  }

  private static boolean validateName(String value) {
    return value != null && !value.trim().isEmpty();
  }

//  private static boolean validateNoteInhalt(String value) {
//    String[] validValues = {"sehr_gut", "gut", "befriedigend", "ausreichend", "mangelhaft", "ungenuegend"};
//    for (String validValue : validValues) {
//      if (validValue.equals(value)) {
//        return true;
//      }
//    }
//    throw new IllegalArgumentException("Invalid note_inhalt value: " + value);
//  }


  private static void optional(Document doc, Element parent, String elementName, String value) {
    if (value != null && !value.isEmpty()) {
      Element element = doc.createElement(elementName);
      element.setTextContent(value);
      parent.appendChild(element);
    }
  }


  private static String mapBooleanToString(String value) {
    return "ja".equalsIgnoreCase(value) ? "ja" : "nein";
  }

  static class XMLHandler extends DefaultHandler {
    private Map<String, String> data;
    private String currentElement;
    private final StringBuilder currentValue = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      currentElement = qName;
      currentValue.setLength(0);

      if ("feedback".equals(qName)) {
        data = new HashMap<>();
      }

      if ("besucher".equals(qName) || "bewertung".equals(qName) || "kontakt".equals(qName)) {
        for (int i = 0; i < attributes.getLength(); i++) {
          data.put(attributes.getQName(i), attributes.getValue(i));
        }
      }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
      if (currentElement != null) {
        currentValue.append(ch, start, length);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if (currentElement != null && data != null) {
        String value = currentValue.toString().trim();
        if (!value.isEmpty()) {
          data.put(currentElement, value);
        }
      }

      if ("feedback".equals(qName) && data != null) {
        xmlData.add(data);
        data = null;
      }

      currentElement = null;
      currentValue.setLength(0);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
      throw new SAXException("Failed to validate:  " + e.getMessage());
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
      throw new SAXException("Fatal error: " + e.getMessage());
    }

    @Override
    public void warning(SAXParseException e) {
      System.out.println("Waring " + e.getMessage());
    }

    public Map<String, String> getData() {
      return data;
    }
  }
}