package assets.vehicles;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VehicleLoader {

    private static String getTagValue(String tagName, Element element) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList != null && nodeList.item(0) != null) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }

    public static List<Vehicle> loadVehicles(String xmlPath) {
        List<Vehicle> vehicleList = new ArrayList<>();

        try {
            File inputFile = new File(xmlPath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("vehicle");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String name = getTagValue("name", eElement);
                    int width = Integer.parseInt(getTagValue("width", eElement).isEmpty() ? "300" : getTagValue("width", eElement));
                    int height = Integer.parseInt(getTagValue("height", eElement).isEmpty() ? "150" : getTagValue("height", eElement));
                    int wheelSize = Integer.parseInt(getTagValue("wheelSize", eElement).isEmpty() ? "67" : getTagValue("wheelSize", eElement));

                    // Leitura segura dos atributos de offset das rodas (frontWheel e rearWheel)
                    // Leitura segura dos atributos de offset das rodas
                    int frontWheelX = 65, frontWheelY = 10;
                    NodeList fwList = eElement.getElementsByTagName("frontWheel");
                    if (fwList != null && fwList.item(0) != null) {
                        Element fwElem = (Element) fwList.item(0);
                        frontWheelX = Integer.parseInt(fwElem.getAttribute("offsetX"));
                        frontWheelY = Integer.parseInt(fwElem.getAttribute("offsetY"));
                    }

                    int rearWheelX = 235, rearWheelY = 10;
                    NodeList rwList = eElement.getElementsByTagName("rearWheel");
                    if (rwList != null && rwList.item(0) != null) {
                        Element rwElem = (Element) rwList.item(0);
                        rearWheelX = Integer.parseInt(rwElem.getAttribute("offsetX"));
                        rearWheelY = Integer.parseInt(rwElem.getAttribute("offsetY"));
                    }

                    double mass = Double.parseDouble(getTagValue("mass", eElement).isEmpty() ? "800.0" : getTagValue("mass", eElement));
                    double baseTorque = Double.parseDouble(getTagValue("baseTorque", eElement).isEmpty() ? "150.0" : getTagValue("baseTorque", eElement));
                    double maxRpm = Double.parseDouble(getTagValue("maxRpm", eElement).isEmpty() ? "5000.0" : getTagValue("maxRpm", eElement));
                    double speedMax = Double.parseDouble(getTagValue("speedMax", eElement).isEmpty() ? "110" : getTagValue("speedMax", eElement));

                    String gearsText = getTagValue("gearRatios", eElement);
                    String[] gearsStr = gearsText.isEmpty() ? new String[]{"1.0"} : gearsText.split(",");
                    double[] gearRatios = new double[gearsStr.length];
                    for (int i = 0; i < gearsStr.length; i++) {
                        gearRatios[i] = Double.parseDouble(gearsStr[i].trim());
                    }

                    Element sprites = (Element) eElement.getElementsByTagName("sprites").item(0);
                    String bodyPath = sprites != null ? sprites.getAttribute("body") : "";
                    String wheelPath = sprites != null ? sprites.getAttribute("wheel") : "";

                    Element sounds = (Element) eElement.getElementsByTagName("sounds").item(0);
                    String startSound = sounds != null ? sounds.getAttribute("start") : "";
                    String idleSound = sounds != null ? sounds.getAttribute("idle") : "";
                    String runSound = sounds != null ? sounds.getAttribute("run") : "";
                    String stopSound = sounds != null ? sounds.getAttribute("stop") : "";
                    String gearSound = sounds != null ? sounds.getAttribute("gear") : "";

                    Vehicle v = new Vehicle(name, width, height, wheelSize,
                            frontWheelX, frontWheelY,
                            rearWheelX, rearWheelY,
                            mass, baseTorque, maxRpm, speedMax, gearRatios, bodyPath, wheelPath);
                    v.setAudioPaths(startSound, stopSound, idleSound, runSound, gearSound);

                    vehicleList.add(v);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar veículos do XML: " + e.getMessage());
            e.printStackTrace();
        }
        return vehicleList;
    }
}