package com.grincare.repository;

import com.grincare.model.RekapHarian;
import com.grincare.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RekapRepository {
    private static final String FILE_PATH    = "data/rekap_harian.xml";
    private static final String ROOT_ELEMENT = "rekapHarian";
    private static final String TAG_REKAP    = "rekap";

    private Document muatDokumen() throws Exception {
        return XmlHelper.bacaXml(FILE_PATH, ROOT_ELEMENT);
    }

    private String teks(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return (nl.getLength() > 0) ? nl.item(0).getTextContent() : "";
    }

    private int angka(Element el, String tag) {
        try {
            return Integer.parseInt(teks(el, tag));
        } catch (Exception e) {
            return 0;
        }
    }

    private RekapHarian elemenKeRekap(Element el) {
        return new RekapHarian(
            teks(el, "tanggal"),
            angka(el, "jumlahPemeriksaanUmum"),
            angka(el, "jumlahScaling"),
            angka(el, "jumlahKontrol"),
            angka(el, "jumlahKonsultasiEstetika"),
            angka(el, "jumlahSelesai"),
            angka(el, "jumlahMenunggu")
        );
    }

    private Element buatElemenRekap(Document doc, RekapHarian r) {
        Element el = doc.createElement(TAG_REKAP);
        String[] tags   = {"tanggal", "jumlahPemeriksaanUmum", "jumlahScaling",
                           "jumlahKontrol", "jumlahKonsultasiEstetika",
                           "jumlahSelesai", "jumlahMenunggu"};
        String[] values = {
            r.getTanggal(),
            String.valueOf(r.getJumlahPemeriksaanUmum()),
            String.valueOf(r.getJumlahScaling()),
            String.valueOf(r.getJumlahKontrol()),
            String.valueOf(r.getJumlahKonsultasiEstetika()),
            String.valueOf(r.getJumlahSelesai()),
            String.valueOf(r.getJumlahMenunggu())
        };
        for (int i = 0; i < tags.length; i++) {
            Element child = doc.createElement(tags[i]);
            child.setTextContent(values[i] != null ? values[i] : "");
            el.appendChild(child);
        }
        return el;
    }

    public RekapHarian getRekapHariIni() {
        String hari = LocalDate.now().toString();
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName(TAG_REKAP);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (hari.equals(teks(el, "tanggal"))) {
                    return elemenKeRekap(el);
                }
            }
        } catch (Exception e) {
            System.err.println("[RekapRepository] Gagal baca rekap harian: " + e.getMessage());
        }
        return new RekapHarian(hari, 0, 0, 0, 0, 0, 0);
    }

    public void simpanRekap(RekapHarian rekap) {
        try {
            Document doc = muatDokumen();
            Element root = doc.getDocumentElement();
            NodeList nodes = root.getElementsByTagName(TAG_REKAP);
            Element existing = null;
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (rekap.getTanggal().equals(teks(el, "tanggal"))) {
                    existing = el;
                    break;
                }
            }
            Element baru = buatElemenRekap(doc, rekap);
            if (existing != null) {
                root.replaceChild(baru, existing);
            } else {
                root.appendChild(baru);
            }
            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            System.err.println("[RekapRepository] Gagal simpan rekap harian: " + e.getMessage());
        }
    }

    // Ambil semua rekap (untuk modul Statistik)
    public List<RekapHarian> getSemuaRekap() {
        List<RekapHarian> list = new ArrayList<>();
        try {
            Document doc = muatDokumen();
            NodeList nodes = doc.getDocumentElement().getElementsByTagName(TAG_REKAP);
            for (int i = 0; i < nodes.getLength(); i++) {
                list.add(elemenKeRekap((Element) nodes.item(i)));
            }
        } catch (Exception e) {
            System.err.println("[RekapRepository] Gagal baca semua rekap: " + e.getMessage());
        }
        return list;
    }
}
