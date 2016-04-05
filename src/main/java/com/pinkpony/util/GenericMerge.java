package com.pinkpony.util;

import org.springframework.data.repository.CrudRepository;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;

public class GenericMerge<T> {

    private final CrudRepository<T, Long> repository;

    public GenericMerge(CrudRepository repo){
        this.repository = repo;
    }

    public Optional<T> mergeObject(Long id, Map<String, String> mergeMap){
        T originalCalendarEvent = repository.findOne(id);

        if(originalCalendarEvent == null) {return Optional.empty();}

        for(String key: mergeMap.keySet()){
            try {
                //generate setter method from key name
                String methodName = "set" + StringUtils.capitalize(key);

                String value = mergeMap.get(key);

                Method method = originalCalendarEvent.getClass().getMethod(methodName, String.class);
                //apply the setter method with the value of *this* key

                method.invoke(originalCalendarEvent, value);
                //originalCalendarEvent.applyMethod("setterMethod", value);
            }catch(NoSuchMethodException nsme){
                nsme.printStackTrace();
                return Optional.empty();
            }catch(IllegalArgumentException iae){
                iae.printStackTrace();
                return Optional.empty();
            }catch(InvocationTargetException ite){
                ite.printStackTrace();
                return Optional.empty();
            }catch(IllegalAccessException iae){
                iae.printStackTrace();
                return Optional.empty();
            }
        }
        return Optional.of(originalCalendarEvent);
    }
}

