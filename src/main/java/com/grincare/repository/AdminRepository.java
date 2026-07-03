package com.grincare.repository;

import com.grincare.model.Admin;
import com.grincare.util.XmlHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.security.MessageDigest;

public class AdminRepository {

    private static final String FILE_PATH    = "data/admin.xml";
    private static final String ROOT_ELEMENT = "admins";

    public Admin cariAdmin(String username) {
        try {
            Document doc = XmlHelper.bacaXml(FILE_PATH, ROOT_ELEMENT);
            NodeList nodes = doc.getDocumentElement().getElementsByTagName("admin");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String u = teks(el, "username");
                if (username.equals(u)) {
                    return new Admin(u, teks(el, "passwordHash"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean sudahAdaAdmin() {
        try {
            Document doc = XmlHelper.bacaXml(FILE_PATH, ROOT_ELEMENT);
            return doc.getDocumentElement().getElementsByTagName("admin").getLength() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void buatAdminDefault() {
        try {
            Document doc = XmlHelper.bacaXml(FILE_PATH, ROOT_ELEMENT);
            Element root = doc.getDocumentElement();

            Element admin = doc.createElement("admin");
            Element usernameEl = doc.createElement("username");
            usernameEl.setTextContent("admin");
            Element hashEl = doc.createElement("passwordHash");
            hashEl.setTextContent(hashPassword("admin123"));
            admin.appendChild(usernameEl);
            admin.appendChild(hashEl);
            root.appendChild(admin);

            XmlHelper.simpanXml(doc, FILE_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String teks(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        return (nl.getLength() > 0 && nl.item(0) != null) ? nl.item(0).getTextContent() : "";
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Gagal hash password", e);
        }
    }
}
