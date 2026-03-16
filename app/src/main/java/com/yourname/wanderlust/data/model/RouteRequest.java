package com.yourname.wanderlust.data.model;

import java.util.List;

public class RouteRequest {
    public String destination;
    public String duration;
    public String age;
    public String pax;
    public String dateFrom;
    public String dateTo;
    public String budget;
    public String origin;
    public List<String> tripTypes;
    public List<String> prefs;

    public RouteRequest(String destination, String duration, String age,
                        String pax, String dateFrom, String dateTo,
                        String budget, String origin,
                        List<String> tripTypes, List<String> prefs) {
        this.destination = destination;
        this.duration    = duration;
        this.age         = age;
        this.pax         = pax;
        this.dateFrom    = dateFrom;
        this.dateTo      = dateTo;
        this.budget      = budget;
        this.origin      = origin;
        this.tripTypes   = tripTypes;
        this.prefs       = prefs;
    }

    public String toPrompt() {
        return "אתה מומחה טיולים. בהתבסס על המידע הבא, המלץ על 3 מסלולי טיול מפורטים.\n\n" +
                "יעד: " + destination + "\n" +
                "נוסעים מ: " + origin + "\n" +
                "תאריכים: " + dateFrom + " עד " + dateTo + "\n" +
                "משך: " + duration + "\n" +
                "גיל: " + age + "\n" +
                "מספר משתתפים: " + pax + "\n" +
                "תקציב לאדם: " + budget + " ₪\n" +
                "סוגי טיול: " + String.join(", ", tripTypes) + "\n" +
                "העדפות: " + String.join(", ", prefs) + "\n\n" +
                "החזר JSON בלבד:\n" +
                "{\n" +
                "  \"routes\": [\n" +
                "    {\n" +
                "      \"title\": \"שם המסלול\",\n" +
                "      \"badge\": \"מומלץ AI\",\n" +
                "      \"description\": \"תיאור קצר\",\n" +
                "      \"highlights\": [\"נקודה 1\", \"נקודה 2\", \"נקודה 3\"],\n" +
                "      \"duration\": \"משך מומלץ\",\n" +
                "      \"difficulty\": \"קל / בינוני / קשה\",\n" +
                "      \"rating\": \"4.5\",\n" +
                "      \"estimatedCost\": \"עלות משוערת לאדם בשקלים\",\n" +
                "      \"source\": \"מקור המידע\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"summary\": \"סיכום קצר\",\n" +
                "  \"flightTip\": \"טיפ על טיסות מ" + origin + " ל" + destination + "\",\n" +
                "  \"hotelTip\": \"טיפ על מלונות מומלצים\"\n" +
                "}";
    }
}