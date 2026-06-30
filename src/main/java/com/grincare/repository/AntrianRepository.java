package com.grincare.repository;

import com.grincare.model.Antrian;
import com.grincare.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AntrianRepository {

    private static final String FILE_PATH   = "data/antrian.xml";
    private static final String ROOT_ELEMENT = "antrian";

    private Document muatDokumen() throws Exception {
        return XmlHelper.bacaXml(FILE_PATH, ROOT_ELEMENT);
    }

    private Antrian elemenKeAntrian(Element el) {
        return new Antrian(
            teks(el, "ticketId"),
            teks(el, "noAntrian"),
            teks(el, "nama"),
            teks(el, "noWhatsApp"),
            teks(el, "kategoriLayanan"),
            teks(el, "status"),
            teks(el, "waktuDibuat"),
            teks(el, "statusKirimWA")
        );
    }

    private String teks(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return (nl.getLength() > 0) ? nl.item(0).getTextContent() : "";
    }

    private void isiElemen(Document doc, Element tiket, Antrian a) {
        String[] tags   = {"ticketId","noAntrian","nama","noWhatsApp","kategoriLayanan","status","waktuDibuat","statusKirimWA"};
        String[] values = {a.getTicketId(), a.getNoAntrian(), a.getNama(), a.getNoWhatsApp(),
                           a.getKategoriLayanan(), a.getStatus(), a.getWaktuDibuat(), a.getStatusKirimWA()};
        for (int i = 0; i < tags.length; i++) {
            Element el = doc.createElement(tags[i]);
            el.setTextContent(values[i] != null ? values[i] : "");
            tiket.appendChild(el);
        }
    }

    public List<Antrian> getSemuaAntrian() {
        List<Antrian> list = new ArrayList<>();
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName("tiket");
            for (int i = 0; i < nodes.getLength(); i++) {
                list.add(elemenKeAntrian((Element) nodes.item(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Antrian> getAntrianAktif() {
        return getSemuaAntrian().stream()
                .filter(a -> "MENUNGGU".equals(a.getStatus()) || "DIPANGGIL".equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    public Antrian getAntrianById(String ticketId) {
        return getSemuaAntrian().stream()
                .filter(a -> ticketId.equals(a.getTicketId()))
                .findFirst().orElse(null);
    }

    public void tambahAntrian(Antrian antrian) {
        try {
            Document doc = muatDokumen();
            Element tiket = doc.createElement("tiket");
            isiElemen(doc, tiket, antrian);
            doc.getDocumentElement().appendChild(tiket);
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateStatus(String ticketId, String statusBaru) {
        updateFieldXml(ticketId, "status", statusBaru);
    }

    public void updateStatusKirimWA(String ticketId, String statusKirimWA) {
        updateFieldXml(ticketId, "statusKirimWA", statusKirimWA);
    }

    private void updateFieldXml(String ticketId, String fieldName, String nilaiBar) {
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName("tiket");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (ticketId.equals(teks(el, "ticketId"))) {
                    el.getElementsByTagName(fieldName).item(0).setTextContent(nilaiBar);
                    break;
                }
            }
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hapusAntrian(String ticketId) {
        try {
            Document doc = muatDokumen();
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("tiket");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (ticketId.equals(teks(el, "ticketId"))) {
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
