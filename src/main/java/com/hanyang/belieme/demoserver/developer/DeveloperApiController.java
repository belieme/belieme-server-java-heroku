package com.hanyang.belieme.demoserver.developer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.event.*;

@RestController
@RequestMapping(path="/developer")//git ignore
public class DeveloperApiController {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @GetMapping("/")
    public String getInfo() {
        String result = "timeStamp : " + System.currentTimeMillis()/1000 + "\n" +
                "Date : " + (new Date(System.currentTimeMillis())) + "\n";
        return result;
    }
}
