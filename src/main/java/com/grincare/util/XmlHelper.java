package com.grincare.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class XmlHelper {

    /**
     * Baca XML dari file, atau buat Document baru dengan rootElement jika file belum ada.
     * Whitespace-only text node dibersihkan agar indentasi tidak ganda saat ditulis ulang.
     */
    public static Document bacaXml(String filePath, String rootElementName) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            Document doc = buatDokumenBaru();
            doc.appendChild(doc.createElement(rootElementName));
            return doc;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        removeWhitespaceNodes(doc);
        return doc;
    }

    /** Overload backward-compat: return null jika file tidak ada. */
    public static Document bacaXml(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(file);
        removeWhitespaceNodes(doc);
        return doc;
    }

    /**
     * Simpan Document ke file dengan indentasi 2-spasi yang bersih.
     * Memanggil normalize() + removeWhitespaceNodes() sebelum transform
     * untuk menghindari baris kosong berlebihan.
     */
    public static void simpanXml(Document document, String filePath) throws Exception {
        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }
        document.normalize();
        removeWhitespaceNodes(document);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(new DOMSource(document), new StreamResult(file));
    }

    public static Document buatDokumenBaru() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    /**
     * Hapus semua whitespace-only text node secara rekursif dari subtree node ini.
     * Iterasi mundur agar pergeseran indeks NodeList live tidak menyebabkan skip.
     */
    private static void removeWhitespaceNodes(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE
                    && child.getTextContent().trim().isEmpty()) {
                node.removeChild(child);
            } else {
                removeWhitespaceNodes(child);
            }
        }
    }
}
