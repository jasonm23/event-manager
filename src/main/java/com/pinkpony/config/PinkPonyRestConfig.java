package com.pinkpony.config;

import com.pinkpony.validator.RsvpValidator;
import com.pinkpony.validator.EventValidator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
public class PinkPonyRestConfig extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener validatingListener) {
        super.configureValidatingRepositoryEventListener(validatingListener);


        //Register our validator with the REST data event listener
        validatingListener.addValidator("beforeCreate", new RsvpValidator());
    }
        validatingListener.addValidator("beforeCreate", new EventValidator());
    }


}
