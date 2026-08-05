package assets.vehicles;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VehicleLoader {

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

                    String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                    int width = Integer.parseInt(eElement.getElementsByTagName("width").item(0).getTextContent());
                    int height = Integer.parseInt(eElement.getElementsByTagName("height").item(0).getTextContent());
                    int wheelSize = Integer.parseInt(eElement.getElementsByTagName("wheelSize").item(0).getTextContent());
                    double mass = Double.parseDouble(eElement.getElementsByTagName("mass").item(0).getTextContent());
                    double baseTorque = Double.parseDouble(eElement.getElementsByTagName("baseTorque").item(0).getTextContent());
                    double maxRpm = Double.parseDouble(eElement.getElementsByTagName("maxRpm").item(0).getTextContent());
                    double speedMax = Double.parseDouble(eElement.getElementsByTagName("speedMax").item(0).getTextContent());

                    String[] gearsStr = eElement.getElementsByTagName("gearRatios").item(0).getTextContent().split(",");
                    double[] gearRatios = new double[gearsStr.length];
                    for (int i = 0; i < gearsStr.length; i++) {
                        gearRatios[i] = Double.parseDouble(gearsStr[i].trim());
                    }

                    Element sprites = (Element) eElement.getElementsByTagName("sprites").item(0);
                    String bodyPath = sprites.getAttribute("body");
                    String wheelPath = sprites.getAttribute("wheel");

                    Element sounds = (Element) eElement.getElementsByTagName("sounds").item(0);
                    String startSound = sounds.getAttribute("start");
                    String idleSound = sounds.getAttribute("idle");
                    String runSound = sounds.getAttribute("run");
                    String stopSound = sounds.getAttribute("stop");
                    String gearSound = sounds.getAttribute("gear");

                    Vehicle v = new Vehicle(name, width, height, wheelSize, mass, baseTorque, maxRpm, speedMax, gearRatios, bodyPath, wheelPath);
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