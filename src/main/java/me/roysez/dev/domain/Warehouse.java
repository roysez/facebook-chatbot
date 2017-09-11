package me.roysez.dev.domain;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class Warehouse {

    @JsonProperty("SiteKey")
    private String siteKey;
    @JsonProperty("Description")
    private String description;
    @JsonProperty("DescriptionRu")
    private String descriptionRu;
    @JsonProperty("ShortAddress")
    private String shortAddress;
    @JsonProperty("ShortAddressRu")
    private String shortAddressRu;
    @JsonProperty("Phone")
    private String phone;
    @JsonProperty("TypeOfWarehouse")
    private String typeOfWarehouse;
    @JsonProperty("Ref")
    private String ref;
    @JsonProperty("Number")
    private String number;
    @JsonProperty("CityRef")
    private String cityRef;
    @JsonProperty("CityDescription")
    private String cityDescription;
    @JsonProperty("CityDescriptionRu")
    private String cityDescriptionRu;
    @JsonProperty("Longitude")
    private String longitude;
    @JsonProperty("Latitude")
    private String latitude;
    @JsonProperty("Schedule")
    private Schedule schedule;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    })
    public class Schedule {

        @JsonProperty("Monday")
        private String monday;
        @JsonProperty("Tuesday")
        private String tuesday;
        @JsonProperty("Wednesday")
        private String wednesday;
        @JsonProperty("Thursday")
        private String thursday;
        @JsonProperty("Friday")
        private String friday;
        @JsonProperty("Saturday")
        private String saturday;
        @JsonProperty("Sunday")
        private String sunday;

    }

    @Data
    @AllArgsConstructor
    public static class WarehouseTracking {
        private String CityName;
        private String Language;
    }
}
