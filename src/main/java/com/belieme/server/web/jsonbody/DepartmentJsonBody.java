package com.belieme.server.web.jsonbody;

import java.util.ArrayList;
import java.util.List;

public class DepartmentJsonBody {
    public String code;

    public String name;
    
    public List<String> majorCodes;
    
    public boolean available;
    
    public DepartmentJsonBody() {
        majorCodes = new ArrayList<>();
    }
}
