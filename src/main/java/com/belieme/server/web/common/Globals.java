package com.belieme.server.web.common;

import java.net.*;

import com.belieme.server.web.exception.*;

public class Globals {
    public static final String serverUrl = "https://belieme.herokuapp.com";
    
    public static URI getLocation(String path) throws HttpException {
        try {
            return new URI(Globals.serverUrl + path);
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
    }
}