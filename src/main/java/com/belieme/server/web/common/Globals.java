package com.belieme.server.web.common;

import java.net.*;

import com.belieme.server.web.exception.*;

public class Globals {
    public static final String serverUrl = "https://belieme.herokuapp.com";
    public static final String developerApiToken = "c305ee87-a4c7-4b5a-8d71-7e23b6064613";
    
    public static URI getLocation(String path) throws InternalServerErrorException {
        try {
            return new URI(Globals.serverUrl + path);
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
    }
}