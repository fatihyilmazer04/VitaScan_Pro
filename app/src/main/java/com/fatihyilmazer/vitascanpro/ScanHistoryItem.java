package com.fatihyilmazer.vitascanpro;

public class ScanHistoryItem {
    String barcode;
    String date;

    public ScanHistoryItem(String barcode, String date) {
        this.barcode = barcode;
        this.date = date;
    }
}