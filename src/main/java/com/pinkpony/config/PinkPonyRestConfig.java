package com.pinkpony.config;

import com.pinkpony.validator.CalendarEventValidator;
import com.pinkpony.validator.RsvpCreateValidator;
import com.pinkpony.validator.RsvpUpdateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class PinkPonyRestConfig extends RepositoryRestConfigurerAdapter {

    @Autowired
    CalendarEventValidator calendarEventValidator;

    @Autowired
    RsvpCreateValidator rsvpCreateValidator;

    @Autowired
    RsvpUpdateValidator rsvpUpdateValidator;

    @Bean
    public RsvpCreateValidator rsvpCreateValidator()
    {
        return new RsvpCreateValidator();
    }

    @Bean
    public RsvpUpdateValidator rsvpUpdateValidator()
    {
        return new RsvpUpdateValidator();
    }

    @Bean
    public CalendarEventValidator calendarEventValidator()
    {
        return new CalendarEventValidator();
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        super.configureValidatingRepositoryEventListener(validatingListener);

        //Register our validator with the REST data event listener
        validatingListener.addValidator("beforeCreate", rsvpCreateValidator);
        validatingListener.addValidator("beforeSave", rsvpUpdateValidator);
        validatingListener.addValidator("beforeCreate", calendarEventValidator);
    }


}
