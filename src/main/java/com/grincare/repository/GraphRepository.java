package com.grincare.repository;

import com.grincare.model.GraphEdge;
import com.grincare.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GraphRepository {

    private static final String FILE_PATH = "data/keyword_graph.xml";
    private static final String ROOT_ELEMENT = "keywordGraph";

    private Document muatDokumen() throws Exception {
        Document doc = XmlHelper.bacaXml(FILE_PATH, ROOT_ELEMENT);
        if (doc.getDocumentElement().getElementsByTagName("edge").getLength() == 0) {
            isiDataDefault(doc);
            XmlHelper.simpanXml(doc, FILE_PATH);
        }
        return doc;
    }

    private void isiDataDefault(Document doc) {
        Element root = doc.getDocumentElement();
        buatElemen(doc, root, UUID.randomUUID().toString(), "sakit", "Pemeriksaan Umum", 5);
        buatElemen(doc, root, UUID.randomUUID().toString(), "lubang", "Pemeriksaan Umum", 7);
        buatElemen(doc, root, UUID.randomUUID().toString(), "bengkak", "Pemeriksaan Umum", 7);
        buatElemen(doc, root, UUID.randomUUID().toString(), "sensitif", "Pemeriksaan Umum", 5);
        buatElemen(doc, root, UUID.randomUUID().toString(), "nyeri", "Pemeriksaan Umum", 6);
        buatElemen(doc, root, UUID.randomUUID().toString(), "ngilu", "Pemeriksaan Umum", 5);
        buatElemen(doc, root, UUID.randomUUID().toString(), "karang", "Scaling", 10);
        buatElemen(doc, root, UUID.randomUUID().toString(), "tartar", "Scaling", 8);
        buatElemen(doc, root, UUID.randomUUID().toString(), "bau", "Scaling", 5);
        buatElemen(doc, root, UUID.randomUUID().toString(), "behel", "Kontrol", 10);
        buatElemen(doc, root, UUID.randomUUID().toString(), "kawat", "Kontrol", 8);
        buatElemen(doc, root, UUID.randomUUID().toString(), "whitening", "Konsultasi Estetika", 10);
        buatElemen(doc, root, UUID.randomUUID().toString(), "veneer", "Konsultasi Estetika", 10);
        buatElemen(doc, root, UUID.randomUUID().toString(), "putih", "Konsultasi Estetika", 6);
    }

    private String teks(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return (nl.getLength() > 0 && nl.item(0) != null) ? nl.item(0).getTextContent() : "";
    }

    private void buatElemen(Document doc, Element root, String id, String source, String target, int weight) {
        Element el = doc.createElement("edge");
        
        Element idEl = doc.createElement("id");
        idEl.setTextContent(id);
        
        Element srcEl = doc.createElement("source");
        srcEl.setTextContent(source);
        
        Element tgtEl = doc.createElement("target");
        tgtEl.setTextContent(target);
        
        Element wEl = doc.createElement("weight");
        wEl.setTextContent(String.valueOf(weight));
        
        el.appendChild(idEl);
        el.appendChild(srcEl);
        el.appendChild(tgtEl);
        el.appendChild(wEl);
        
        root.appendChild(el);
    }

    public List<GraphEdge> getSemuaEdge() {
        List<GraphEdge> list = new ArrayList<>();
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName("edge");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String id = teks(el, "id");
                String src = teks(el, "source");
                String tgt = teks(el, "target");
                int weight = 1;
                try {
                    weight = Integer.parseInt(teks(el, "weight"));
                } catch (NumberFormatException ignored) {}
                
                list.add(new GraphEdge(id, src, tgt, weight));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void tambahEdge(GraphEdge edge) {
        try {
            Document doc = muatDokumen();
            buatElemen(doc, doc.getDocumentElement(),
                    edge.getId(), edge.getSource(), edge.getTarget(), edge.getWeight());
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateEdge(String id, GraphEdge edgeBaru) {
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName("edge");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (id.equals(teks(el, "id"))) {
                    NodeList srcNodes = el.getElementsByTagName("source");
                    NodeList tgtNodes = el.getElementsByTagName("target");
                    NodeList wNodes = el.getElementsByTagName("weight");
                    
                    if (srcNodes.getLength() > 0) srcNodes.item(0).setTextContent(edgeBaru.getSource());
                    if (tgtNodes.getLength() > 0) tgtNodes.item(0).setTextContent(edgeBaru.getTarget());
                    if (wNodes.getLength() > 0) wNodes.item(0).setTextContent(String.valueOf(edgeBaru.getWeight()));
                    break;
                }
            }
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hapusEdge(String id) {
        try {
            Document doc = muatDokumen();
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("edge");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (id.equals(teks(el, "id"))) {
                    root.removeChild(el);
                    break;
                }
            }
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
