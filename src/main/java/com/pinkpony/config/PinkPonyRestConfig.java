package com.pinkpony.config;

import com.pinkpony.validator.EventValidator;
import com.pinkpony.validator.RsvpValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

@Configuration
public class PinkPonyRestConfig extends RepositoryRestConfigurerAdapter {

    @Autowired
    EventValidator eventValidator;

    @Autowired
    RsvpValidator rsvpValidator;

    @Bean
    public RsvpValidator rsvpValidator()
    {
        return new RsvpValidator();
    }

    @Bean
    public EventValidator eventValidator()
    {
        return new EventValidator();
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        super.configureValidatingRepositoryEventListener(validatingListener);

        //Register our validator with the REST data event listener
        validatingListener.addValidator("beforeCreate", rsvpValidator);
        validatingListener.addValidator("beforeCreate", eventValidator);
    }


}
