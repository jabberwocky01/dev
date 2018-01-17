package com.nils.electricitywatercuts.com.nils.electricitywatercuts.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nil on 15.01.2018.
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "metin",
        "saat1",
        "saat2",
        "nedeni"
})

public class EuropeElectricityData {

    @JsonProperty("metin")
    private String location;
    @JsonProperty("saat1")
    private String startHour;
    @JsonProperty("saat2")
    private String endHour;
    @JsonProperty("nedeni")
    private String reason;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("metin")
    public String getLocation() {
        return location;
    }

    @JsonProperty("metin")
    public void setLocation(String location) {
        this.location = location;
    }

    @JsonProperty("saat1")
    public String getStartHour() {
        return startHour;
    }

    @JsonProperty("saat1")
    public void setStartHour(String startHour) {
        this.startHour = startHour;
    }

    @JsonProperty("saat2")
    public String getEndHour() {
        return endHour;
    }

    @JsonProperty("saat2")
    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    @JsonProperty("nedeni")
    public String getReason() {
        return reason;
    }

    @JsonProperty("nedeni")
    public void setReason(String reason) {
        this.reason = reason;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
