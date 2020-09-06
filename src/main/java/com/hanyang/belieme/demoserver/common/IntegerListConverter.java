package com.hanyang.belieme.demoserver.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class IntegerListConverter implements AttributeConverter<List<Integer>, String> {
    private static final String SPLIT_CHAR = ";";

    @Override
    public String convertToDatabaseColumn(List<Integer> integerList) {
        List<String> stringList = new ArrayList<>();
        for(int i = 0; i < integerList.size(); i++) {
            stringList.add(integerList.get(i).toString());
        }
        return String.join(SPLIT_CHAR, stringList);
    }

    @Override
    public List<Integer> convertToEntityAttribute(String string) {
        List<String> stringList = Arrays.asList(string.split(SPLIT_CHAR));
        List<Integer> integerList = new ArrayList<>();
        
        for(int i = 0; i < stringList.size(); i++) {
            integerList.add(Integer.parseInt(stringList.get(i)));
        }
        return integerList;
    }
}