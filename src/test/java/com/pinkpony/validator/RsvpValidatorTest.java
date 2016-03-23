package com.pinkpony.validator;

import com.pinkpony.PinkPonyApplication;
import com.pinkpony.model.Event;
import com.pinkpony.model.Rsvp;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.Assert.*;



@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = PinkPonyApplication.class)
public class RsvpValidatorTest {
    RsvpValidator validator;
    Event event;
    Rsvp rsvp;

    @Autowired
    MessageSource messageSource;

    @Before
    public void setUp() {
        validator = new RsvpValidator();

        rsvp = new Rsvp();
        rsvp.setName("Hermione");
        rsvp.setResponse("yes");
    }

    @Test
    public void testValidateBlanks() throws Exception {
        rsvp.setName("");
        rsvp.setResponse("");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(rsvp, "Rsvp");

        validator.validate(rsvp, errors);

        assertTrue(errors.getErrorCount() > 0);
        assertThat(errors.getFieldError("name").getCodes(), hasItemInArray("rsvp.name.field.empty"));
        assertThat(errors.getFieldError("response").getCodes(), hasItemInArray("rsvp.response.field.empty"));
    }

    @Test
    public void testValidateResponse() throws Exception {
        rsvp.setResponse("invalid");

        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(rsvp, "Rsvp");
        validator.validate(rsvp, errors);

        assertTrue(errors.getErrorCount() > 0);
        assertNull(errors.getFieldError("name"));
        assertThat(errors.getFieldError("response").getCodes(), hasItemInArray("rsvp.response.field.invalidValue"));

        rsvp.setResponse("yes");
        assertValid(rsvp, validator);

        rsvp.setResponse("no");
        assertValid(rsvp, validator);
    }

    private void assertValid(Object object, Validator validator) {
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(object, "_");
        validator.validate(object, errors);
        assertTrue(errors.getErrorCount() == 0);
    }
}