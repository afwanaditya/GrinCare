package com.grincare.repository;

import com.grincare.model.KategoriLayanan;
import com.grincare.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KategoriRepository {

    private static final String FILE_PATH = "data/kategori_layanan.xml";
    private static final String ROOT_ELEMENT = "kategoriLayanan";

    private Document muatDokumen() throws Exception {
        Document doc = XmlHelper.bacaXml(FILE_PATH, ROOT_ELEMENT);
        if (doc.getDocumentElement().getElementsByTagName("kategori").getLength() == 0) {
            isiDataDefault(doc);
            XmlHelper.simpanXml(doc, FILE_PATH);
        }
        return doc;
    }

    private void isiDataDefault(Document doc) {
        Element root = doc.getDocumentElement();
        buatElemen(doc, root, UUID.randomUUID().toString(),
                "Pemeriksaan Umum",
                "Sakit gigi, berlubang, gusi bengkak, sensitif, keluhan belum jelas");
        buatElemen(doc, root, UUID.randomUUID().toString(),
                "Scaling",
                "Pembersihan karang gigi");
        buatElemen(doc, root, UUID.randomUUID().toString(),
                "Kontrol",
                "Kontrol behel, pasca tindakan, follow up");
        buatElemen(doc, root, UUID.randomUUID().toString(),
                "Konsultasi Estetika",
                "Whitening, veneer, konsultasi estetika gigi");
    }

    private void buatElemen(Document doc, Element root, String id, String nama, String deskripsi) {
        Element el = doc.createElement("kategori");
        Element idEl = doc.createElement("id");
        idEl.setTextContent(id);
        Element namaEl = doc.createElement("nama");
        namaEl.setTextContent(nama);
        Element deskripsiEl = doc.createElement("deskripsi");
        deskripsiEl.setTextContent(deskripsi);
        el.appendChild(idEl);
        el.appendChild(namaEl);
        el.appendChild(deskripsiEl);
        root.appendChild(el);
    }

    public List<KategoriLayanan> getSemuaKategori() {
        List<KategoriLayanan> list = new ArrayList<>();
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName("kategori");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String id = el.getElementsByTagName("id").item(0).getTextContent();
                String nama = el.getElementsByTagName("nama").item(0).getTextContent();
                String deskripsi = el.getElementsByTagName("deskripsi").item(0).getTextContent();
                list.add(new KategoriLayanan(id, nama, deskripsi));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void tambahKategori(KategoriLayanan kategori) {
        try {
            Document doc = muatDokumen();
            buatElemen(doc, doc.getDocumentElement(),
                    kategori.getId(), kategori.getNama(), kategori.getDeskripsi());
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateKategori(String id, KategoriLayanan kategoriBaru) {
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName("kategori");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (el.getElementsByTagName("id").item(0).getTextContent().equals(id)) {
                    el.getElementsByTagName("nama").item(0).setTextContent(kategoriBaru.getNama());
                    el.getElementsByTagName("deskripsi").item(0).setTextContent(kategoriBaru.getDeskripsi());
                    break;
                }
            }
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hapusKategori(String id) {
        try {
            Document doc = muatDokumen();
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName("kategori");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (el.getElementsByTagName("id").item(0).getTextContent().equals(id)) {
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
