package com.mycompany.savasgemisi.common;

/**
 * İstemci ve sunucu arasındaki mesajlaşmayı yöneten sınıf.
 */
public class Message {
    
    /**
     * Verilen mesaj tipi ve veri ile formatlanmış bir mesaj oluşturur.
     * 
     * @param type Mesaj tipi
     * @param data Mesaj verisi
     * @return Formatlanmış mesaj (type#data)
     */
    public static String generateMessage(MessageType type, String data) {
        return type.toString() + "#" + data;
    }
    
    /**
     * Gelen mesajı ayrıştırır ve ParsedMessage nesnesine dönüştürür.
     * 
     * @param msg Ayrıştırılacak mesaj
     * @return Ayrıştırılmış mesaj nesnesi
     */
    public static ParsedMessage parseMessage(String msg) {
        String[] parts = msg.split("#", 2);
        if (parts.length != 2) {
            return new ParsedMessage(MessageType.ERROR, "Geçersiz mesaj formatı");
        }
        
        try {
            MessageType type = MessageType.valueOf(parts[0]);
            return new ParsedMessage(type, parts[1]);
        } catch (IllegalArgumentException e) {
            return new ParsedMessage(MessageType.ERROR, "Bilinmeyen mesaj tipi: " + parts[0]);
        }
    }
    
    /**
     * Ayrıştırılmış mesajı temsil eden iç sınıf
     */
    public static class ParsedMessage {
        private MessageType type;
        private String data;
        
        public ParsedMessage(MessageType type, String data) {
            this.type = type;
            this.data = data;
        }
        
        public MessageType getType() {
            return type;
        }
        
        public String getData() {
            return data;
        }
    }
} 