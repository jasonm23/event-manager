package com.pinkpony.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pinkpony.validator.CalendarEventValidator;
import com.pinkpony.validator.RsvpValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.MediaType;

@Configuration
public class AppConfig {

    public static final MediaType MARVIN_JSON_MEDIATYPE = new MediaType("application", "x-marvin-bot+json");

    @Bean
    public RsvpValidator rsvpValidator(){
        return new RsvpValidator();
    }

    @Bean
    public CalendarEventValidator calendarEventValidator(){
        return new CalendarEventValidator();
    }

    @Bean
    SpelAwareProxyProjectionFactory spelAwareProxyProjectionFactory(){
        return new SpelAwareProxyProjectionFactory();
    }

}
