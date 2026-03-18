package com.yourname.wanderlust.data.model;

import java.util.List;

public class RouteRequest {

    public boolean isFlightMode;

    // טיסה
    public String destination;
    public String dateFrom;
    public String dateTo;
    public String pax;
    public String budget;
    public List<String> luggage;

    // מקומי
    public String area;
    public String difficulty;
    public List<String> accommodation;

    // קונסטרקטור טיסה
    public RouteRequest(String destination, String dateFrom, String dateTo,
                        String pax, String budget, List<String> luggage) {
        this.isFlightMode = true;
        this.destination  = destination;
        this.dateFrom     = dateFrom;
        this.dateTo       = dateTo;
        this.pax          = pax;
        this.budget       = budget;
        this.luggage      = luggage;
    }

    // קונסטרקטור מקומי
    public RouteRequest(String area, String pax, String budget,
                        String difficulty, String dateFrom, String dateTo,
                        List<String> accommodation) {
        this.isFlightMode  = false;
        this.area          = area;
        this.pax           = pax;
        this.budget        = budget;
        this.difficulty    = difficulty;
        this.dateFrom      = dateFrom;
        this.dateTo        = dateTo;
        this.accommodation = accommodation;
    }

    public String toPrompt() {
        if (isFlightMode) {
            return "אתה מומחה טיולים. המלץ על 3 מסלולים לטיסה.\n\n" +
                    "יעד: " + destination + "\n" +
                    "תאריכים: " + dateFrom + " עד " + dateTo + "\n" +
                    "נוסעים: " + pax + "\n" +
                    "תקציב לאדם: " + budget + " ₪\n" +
                    "כבודה: " + String.join(", ", luggage) + "\n\n" +
                    "כלול המלצות על:\n" +
                    "- חברות תעופה מומלצות ומחירים משוערים\n" +
                    "- שכונות מומלצות ללינה\n" +
                    "- עלויות לינה משוערות ללילה\n\n" +
                    buildJsonFormat();
        } else {
            return "אתה מומחה טיולים בישראל. המלץ על 3 מסלולים.\n\n" +
                    "אזור: " + area + "\n" +
                    "תאריכים: " + dateFrom + " עד " + dateTo + "\n" +
                    "אנשים: " + pax + "\n" +
                    "תקציב לאדם: " + budget + " ₪\n" +
                    "רמת קושי: " + difficulty + "\n" +
                    "סוג לינה: " + String.join(", ", accommodation) + "\n\n" +
                    "כלול המלצות על:\n" +
                    "- מקומות לינה ספציפיים (קמפינגים, צימרים, מלונות)\n" +
                    "- מחירי לינה משוערים\n" +
                    "- עלויות כוללות משוערות\n\n" +
                    buildJsonFormat();
        }
    }

    private String buildJsonFormat() {
        return "החזר JSON בלבד:\n" +
                "{\n" +
                "  \"routes\": [\n" +
                "    {\n" +
                "      \"title\": \"שם המסלול\",\n" +
                "      \"badge\": \"מומלץ AI\",\n" +
                "      \"description\": \"תיאור\",\n" +
                "      \"highlights\": [\"נקודה 1\", \"נקודה 2\"],\n" +
                "      \"duration\": \"משך\",\n" +
                "      \"difficulty\": \"קל/בינוני/קשה\",\n" +
                "      \"rating\": \"4.5\",\n" +
                "      \"estimatedCost\": \"עלות כוללת משוערת לאדם\",\n" +
                "      \"accommodation\": \"מקום לינה מומלץ + מחיר\",\n" +
                "      \"flightInfo\": \"חברת תעופה + מחיר משוער (רק לטיסות)\",\n" +
                "      \"source\": \"מקור\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"summary\": \"סיכום קצר\",\n" +
                "  \"flightTip\": \"טיפ טיסות (רק לטיסות)\",\n" +
                "  \"hotelTip\": \"טיפ לינה\"\n" +
                "}";
    }
}