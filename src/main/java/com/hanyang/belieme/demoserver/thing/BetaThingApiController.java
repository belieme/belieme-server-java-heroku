package com.hanyang.belieme.demoserver.thing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.item.*;

import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.event.*;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.WrongInDataBaseException;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;

@RestController
@RequestMapping(path="/beta/universities/{univCode}/departments/{departmentCode}/things")
public class BetaThingApiController {
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ThingRepository thingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("")
    public ResponseWrapper<List<Thing>> getAllThings(@PathVariable String univCode, @PathVariable String departmentCode) {
        try {
            int id = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
            Iterable<ThingDB> allThingDBList = thingRepository.findByDepartmentId(id);
            ArrayList<Thing> responseBody = new ArrayList<>();
            for (Iterator<ThingDB> it = allThingDBList.iterator(); it.hasNext(); ) {
                Thing tmp;
                try {
                    tmp = it.next().toThing(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository); 
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                
                responseBody.add(tmp);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
    }

    @GetMapping("/{id}")
    public ResponseWrapper<ThingWithItems> getThingById(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id) {
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            if(departmentId != targetOptional.get().getDepartmentId()) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); // TODO Exception 바꿀까?
            }
            ThingWithItems responseBody;
            try {
                responseBody = targetOptional.get().toThingWithItems(universityRepository, departmentRepository, majorRepository, userRepository, itemRepository, eventRepository);
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    }

    @PostMapping("")
    public ResponseWrapper<List<Thing>> createNewThing(@PathVariable String univCode, @PathVariable String departmentCode, @RequestBody Thing requestBody) {
        if(requestBody.getName() == null || requestBody.getEmoji() == null || requestBody.getDescription() == null) { //getAmount는 체크 안하는 이유가 amout를 입력 안하면 0으로 자동저장 되어서 item이 0개인 thing이 생성된다.
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        ThingDB requestBodyDB = requestBody.toThingDB();
        requestBodyDB.setDepartmentId(departmentId);
        
        ThingDB savedThing = thingRepository.save(requestBodyDB); 
        for(int i = 0; i < requestBody.getAmount(); i++) { // requestBody에 amout값이 주어졌을때 작동 됨
            ItemDB newItem = new ItemDB(savedThing.getId(), i + 1);
            itemRepository.save(newItem);
        }
        
        Iterable<ThingDB> allThingDBList = thingRepository.findByDepartmentId(departmentId);
        Iterator<ThingDB> iterator = allThingDBList.iterator();

        ArrayList<Thing> responseBody = new ArrayList<>();
        while(iterator.hasNext()) {
            Thing tmp;
            try {
                tmp = iterator.next().toThing(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository); 
            } catch(NotFoundException e) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
            }
            responseBody.add(tmp);
        }
        return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
    }

    @PatchMapping("/{id}")
    public ResponseWrapper<List<Thing>> updateNameAndEmojiOfThing(@PathVariable String univCode, @PathVariable String departmentCode, @PathVariable int id, @RequestBody Thing requestBody){
        if(requestBody.getName() == null && requestBody.getEmoji() == null && requestBody.getDescription() == null) {
            return new ResponseWrapper<>(ResponseHeader.LACK_OF_REQUEST_BODY_EXCEPTION, null);
        }
        
        int departmentId;
        try {
            departmentId = Department.findIdByUniversityCodeAndDepartmentCode(universityRepository, departmentRepository, univCode, departmentCode);
        } catch(NotFoundException e) {
            return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
        } catch(WrongInDataBaseException e) {
            return new ResponseWrapper<>(ResponseHeader.WRONG_IN_DATABASE_EXCEPTION, null);
        }
        
        Optional<ThingDB> targetOptional = thingRepository.findById(id);
        if(targetOptional.isPresent()) {
            ThingDB target = targetOptional.get();
            
            if(target.getDepartmentId() != departmentId) {
                return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null); // TODO Exception 바꿀까?
            }
            
            ThingDB requestBodyDB = requestBody.toThingDB();
            if(requestBodyDB.getName() != null) {
                target.setName(requestBodyDB.getName());    
            }
            if(requestBodyDB.getEmojiByte() != 0) {
                target.setEmojiByte(requestBodyDB.getEmojiByte());    
            }
            if(requestBodyDB.getDescription() != null) {
                target.setDescription(requestBodyDB.getDescription());
            }
            thingRepository.save(target);

            Iterable<ThingDB> allThingsListDB = thingRepository.findByDepartmentId(departmentId);
            Iterator<ThingDB> iterator = allThingsListDB.iterator();

            ArrayList<Thing> responseBody = new ArrayList<>();
            while(iterator.hasNext()) {
                Thing tmp;
                try {
                    tmp = iterator.next().toThing(universityRepository, departmentRepository, majorRepository, userRepository, thingRepository, itemRepository, eventRepository); 
                } catch(NotFoundException e) {
                    return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
                }
                responseBody.add(tmp);
            }
            return new ResponseWrapper<>(ResponseHeader.OK, responseBody);
        }
        return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION,null);
    }

    // deactivate Thing을 만들긴 해야할 거 같지만 생각좀 해봐야 할 듯
    // @PutMapping("/deactivate/{id}/")
    // public ResponseWrapper<Void> deactivateThing(@PathVariable int id) {
    //     if(itemTypeRepository.findById(id).isPresent()) {
    //         List<Item> itemList = itemRepository.findByTypeId(id);
    //         for (int i = 0; i < itemList.size(); i++) {
    //             itemList.get(i).deactivate();
    //             itemRepository.save(itemList.get(i));
    //         }
    //         return new ResponseWrapper<>(ResponseHeader.OK, null);
    //     }
    //     else {
    //         return new ResponseWrapper<>(ResponseHeader.NOT_FOUND_EXCEPTION, null);
    //     }
    // }
}