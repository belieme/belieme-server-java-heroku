package com.hanyang.belieme.demoserver.event;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.hanyang.belieme.demoserver.thing.*;
import com.hanyang.belieme.demoserver.university.University;
import com.hanyang.belieme.demoserver.university.UniversityRepository;
import com.hanyang.belieme.demoserver.user.User;
import com.hanyang.belieme.demoserver.user.UserDB;
import com.hanyang.belieme.demoserver.user.UserRepository;
import com.hanyang.belieme.demoserver.user.permission.PermissionRepository;
import com.hanyang.belieme.demoserver.item.*;
import com.hanyang.belieme.demoserver.common.*;
import com.hanyang.belieme.demoserver.department.DepartmentDB;
import com.hanyang.belieme.demoserver.department.DepartmentRepository;
import com.hanyang.belieme.demoserver.department.Department;
import com.hanyang.belieme.demoserver.department.major.MajorRepository;
import com.hanyang.belieme.demoserver.exception.BadRequestException;
import com.hanyang.belieme.demoserver.exception.ForbiddenException;
import com.hanyang.belieme.demoserver.exception.HttpException;
import com.hanyang.belieme.demoserver.exception.InternalServerErrorException;
import com.hanyang.belieme.demoserver.exception.MethodNotAllowedException;
import com.hanyang.belieme.demoserver.exception.NotFoundException;
import com.hanyang.belieme.demoserver.exception.UnauthorizedException;

@RestController
@RequestMapping(path="/univs/{univCode}/depts/{deptCode}/events")
public class EventApiController {
    @Autowired
    private UniversityRepository universityRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private MajorRepository majorRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ThingRepository thingRepository;

    @GetMapping("")
    public ResponseEntity<ListResponse> getItems(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestParam(value = "studentId", required = false) String studentId) throws HttpException { //TODO value 바꾸기
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && user.getStudentId().equals(studentId)))) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        
        Iterable<EventDB> allEventList = eventRepository.findAll();
        Iterator<EventDB> iterator = allEventList.iterator();
        
        List<Event> output = new ArrayList<>();
        while(iterator.hasNext()) {
            EventDB eventDB = iterator.next();
            Event tmp = eventDB.toEvent(userRepository, thingRepository, itemRepository, eventRepository);    
          
            if(tmp.getThing().deptIdGetter() == dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                if(studentId == null || (tmp.getUser() != null && studentId.equals(tmp.getUser().getStudentId()))) {
                    output.add(tmp);    
                }
            }
        }
        return ResponseEntity.ok().body(new ListResponse(univ, dept, output));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response> getItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        
        Optional<EventDB> eventOptional = eventRepository.findById(id);
        Event output;
        if(eventOptional.isPresent()) {
            output = eventOptional.get().toEvent(userRepository, thingRepository, itemRepository, eventRepository);    
            
            if(output.getThing().deptIdGetter() == dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                if(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && output.getUser() != null && output.getUser().getId() == user.getId())) {
                    return ResponseEntity.ok().body(new Response(univ, dept, output));       
                }
                else {
                    throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
                }
            }
        }
        throw new NotFoundException("기록 id가 " + id + "인 기록을 찾을 수 없습니다.");
    }

    
    //TODO 로그인 없이 관리자가 예약 신청 해주는 경우 추가하기
    @PostMapping("/reserve")
    public ResponseEntity<Response> createRequestEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody EventRequestBody requestBody) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);
        if(!user.hasUserPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        if(requestBody.getThingId() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : thingId(int), itemNum(int)(optional)");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        
        List<EventDB> eventListByUserId = eventRepository.findByUserId(user.getId());
        int currentEventCount = 0;
        for(int i = 0; i < eventListByUserId.size(); i++) {
            Event tmp;
            tmp = eventListByUserId.get(i).toEvent(userRepository, thingRepository, itemRepository, eventRepository);  
            
            if(tmp.getStatus().equals("RESERVED") || tmp.getStatus().equals("USING") || tmp.getStatus().equals("DELAYED") || tmp.getStatus().equals("LOST")) {
                currentEventCount++;
                if(tmp.getThing().getId() == requestBody.getThingId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                    throw new MethodNotAllowedException("빌리려고 하는 물품종류에 대한 열린 기록이 있습니다."); //TODO 이 exception은 없애던가 메세지를 바꾸기
                }
            }
        }
        if(currentEventCount >= 3) {
            throw new MethodNotAllowedException("사용자가 이미 3개의 물품을 사용 또는 예약하였습니다.");
        }
        
        Item reservedItem = null;
        if(requestBody.getItemNum() == null) {
            List<ItemDB> itemListByThingId = itemRepository.findByThingId(requestBody.getThingId());
            for(int i = 0; i < itemListByThingId.size(); i++) {
                reservedItem = itemListByThingId.get(i).toItem(userRepository, eventRepository);
                if (reservedItem.getStatus().equals("USABLE")) {
                    break;
                }
                reservedItem = null;
            }
            if(reservedItem == null) {
                throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
            }
        } else {
            reservedItem = ItemDB.findByThingIdAndNum(itemRepository, requestBody.getThingId(), requestBody.getItemNum()).toItem(userRepository, eventRepository);
            
            if(!reservedItem.getStatus().equals("USABLE")) {
                throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
            }
        }

        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(reservedItem.getId());
        newEventDB.setUserId(user.getId());
        newEventDB.setApproveManagerId(0);
        newEventDB.setReturnManagerId(0);
        newEventDB.setLostManagerId(0);
        newEventDB.setReserveTimeStampNow();
        newEventDB.setApproveTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampZero();
            
        Event output;
        output = eventRepository.save(newEventDB).toEvent(userRepository, thingRepository, itemRepository, eventRepository);  
        
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(reservedItem.getId());
        updatedItemDB.setNum(reservedItem.getNum());
        updatedItemDB.setThingId(reservedItem.thingIdGetter());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/events/" + output.getId());    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(new Response(univ, dept, output));
    }
    
    @PostMapping("/lost")
    public ResponseEntity<Response> createLostEvent(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @RequestBody EventRequestBody requestBody) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        if(requestBody.getThingId() == null || requestBody.getItemNum() == null) {
            throw new BadRequestException("Request body에 정보가 부족합니다.\n필요한 정보 : thingId(int), itemNum(int)");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);

        Item lostItem = ItemDB.findByThingIdAndNum(itemRepository, requestBody.getThingId(), requestBody.getItemNum()).toItem(userRepository, eventRepository);
            
        if(!lostItem.getStatus().equals("USABLE")) {
            throw new MethodNotAllowedException("이 item은 사용할 수 없습니다.");
        }
        
        EventDB newEventDB = new EventDB();
        
        newEventDB.setItemId(lostItem.getId());
        newEventDB.setUserId(0);
        newEventDB.setApproveManagerId(0);
        newEventDB.setReturnManagerId(0);
        newEventDB.setLostManagerId(user.getId());
        newEventDB.setReserveTimeStampZero();
        newEventDB.setApproveTimeStampZero();
        newEventDB.setReturnTimeStampZero();
        newEventDB.setCancelTimeStampZero();
        newEventDB.setLostTimeStampNow();
        
        Event output = eventRepository.save(newEventDB).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            
        ItemDB updatedItemDB = new ItemDB();
        updatedItemDB.setId(lostItem.getId());
        updatedItemDB.setNum(lostItem.getNum());
        updatedItemDB.setThingId(lostItem.thingIdGetter());
        updatedItemDB.setLastEventId(output.getId());;
        
        itemRepository.save(updatedItemDB);
        
        URI location;
        try {
            location = new URI(Globals.serverUrl + "/univs/" + univCode + "/depts/" + deptCode + "/events/" + output.getId());    
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw new InternalServerErrorException("안알랴줌");
        }
        
        return ResponseEntity.created(location).body(new Response(univ, dept, output));
    }

    @PatchMapping("/{id}/cancel") //TODO 논의점 cancel manager를 만들어야 하는가?
    public ResponseEntity<Response> cancelItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            if(!(user.hasStaffPermission(deptCode) || (user.hasUserPermission(deptCode) && eventToUpdate.getUserId() == user.getId()))) {
                throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
            }
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
               throw new NotFoundException(id + "를 기록 id로 갖는 기록이 이 학과의 물건이 아닙니다."); 
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setCancelTimeStampNow();
                
                Event output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                return ResponseEntity.ok().body(new Response(univ, dept, output));
            }
            else {
                throw new MethodNotAllowedException("'예약됨'기록이 아닌 기록을 취소할 수 없습니다.");
            }
        }
        else {
            throw new NotFoundException("기록 id가 " + id + "인 기록을 찾을 수 없습니다.");
        }
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<Response> responseItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);    
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                throw new NotFoundException(id + "를 기록 id로 갖는 기록이 이 학과의 물건이 아닙니다."); 
            }
            
            if(eventToUpdate.getStatus().equals("RESERVED")) {
                eventToUpdate.setApproveTimeStampNow();
                eventToUpdate.setApproveManagerId(user.getId());
                
                Event output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                return ResponseEntity.ok().body(new Response(univ, dept, output));
            }
            else {
                throw new MethodNotAllowedException("'예약됨'기록이 아닌 기록을 승인할 수 없습니다.");
            }
        }
        else {
            throw new NotFoundException("기록 id가 " + id + "인 기록을 찾을 수 없습니다.");
        }
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<Response> returnItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);  
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);
            
            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                throw new NotFoundException(id + "를 기록 id로 갖는 기록이 이 학과의 물건이 아닙니다."); 
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(user.getId());
                
                Event output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                return ResponseEntity.ok().body(new Response(univ, dept, output));
            }
            else {
                throw new MethodNotAllowedException("'사용중' 또는 '연체됨' 기록이 아닌 기록을 반납 처리할 수 없습니다.");
            }
        }
        else {
            throw new NotFoundException("기록 id가 " + id + "인 기록을 찾을 수 없습니다.");
        }
    }
    
    @PatchMapping("{id}/lost")
    public ResponseEntity<Response> lostItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);  
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);

            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                throw new NotFoundException(id + "를 기록 id로 갖는 기록이 이 학과의 물건이 아닙니다.");
            }
            
            if(eventToUpdate.getStatus().equals("USING") || eventToUpdate.getStatus().equals("DELAYED")) {
                eventToUpdate.setLostTimeStampNow();
                eventToUpdate.setLostManagerId(user.getId());
                
                Event output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                return ResponseEntity.ok().body(new Response(univ, dept, output));
            }
            else {
                throw new MethodNotAllowedException("'사용중' 또는 '연체됨' 기록이 아닌 기록을 분실 처리할 수 없습니다.");
            }
        }
        else {
            throw new NotFoundException("기록 id가 " + id + "인 기록을 찾을 수 없습니다.");
        }
    }
    
    @PatchMapping("/{id}/found")
    public ResponseEntity<Response> foundItem(@RequestHeader("user-token") String userToken, @PathVariable String univCode, @PathVariable String deptCode, @PathVariable int id) throws HttpException {
        if(userToken == null) {
            throw new UnauthorizedException("인증이 진행되지 않았습니다. user-token을 header로 전달해 주시길 바랍니다.");
        }
        
        University univ = University.findByUnivCode(universityRepository, univCode);
        Department dept = DepartmentDB.findByUnivCodeAndDeptCode(universityRepository, departmentRepository, univCode, deptCode).toDepartment(majorRepository);
        User user = UserDB.findByToken(userRepository, userToken).toUser(departmentRepository, majorRepository, permissionRepository);  
        
        if(!user.hasStaffPermission(deptCode)) {
            throw new ForbiddenException("주어진 user-token에 해당하는 user에는 api에 대한 권한이 없습니다.");
        }
        
        Optional<EventDB> eventBeforeUpdateOptional = eventRepository.findById(id);
        if(eventBeforeUpdateOptional.isPresent()) {
            EventDB eventToUpdate = eventBeforeUpdateOptional.get();
            
            Event eventBeforeUpdate = eventToUpdate.toEvent(userRepository, thingRepository, itemRepository, eventRepository);

            if(eventBeforeUpdate.getThing().deptIdGetter() != dept.getId()) { //TODO null pointer exception 발생 할 수도 있지 않을까?
                throw new NotFoundException(id + "를 기록 id로 갖는 기록이 이 학과의 물건이 아닙니다."); 
            }
            
            if(eventToUpdate.getStatus().equals("LOST")) {
                eventToUpdate.setReturnTimeStampNow();
                eventToUpdate.setReturnManagerId(user.getId());
                
                Event output = eventRepository.save(eventToUpdate).toEvent(userRepository, thingRepository, itemRepository, eventRepository);
                return ResponseEntity.ok().body(new Response(univ, dept, output));
            }
            else {
                throw new MethodNotAllowedException("'분실됨' 기록이 아닌 기록을 찾음 처리할 수 없습니다."); // TODO message가 구리네...
            }
        }
        else {
            throw new NotFoundException("기록 id가 " + id + "인 기록을 찾을 수 없습니다.");
        }
    }
    
    public class Response {
        University university;
        Department department;
        Event event;

        public Response(University university, Department department, Event event) {
            this.university = new University(university);
            this.department = new Department(department);
            this.event = new Event(event);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }

        public Department getDepartment() {
            if(department == null) {
                return null;
            }
            return new Department(department);
        }
        
        public Event getEvent() {
            if(event == null) {
                return null;
            }
            return new Event(event);
        }
    }
    
    public class ListResponse {
        University university;
        Department department;
        List<Event> events;

        public ListResponse(University university, Department department, List<Event> events) {
            this.university = university;
            this.department = department;
            this.events = new ArrayList<>(events);
        }

        public University getUniversity() {
            if(university == null) {
                return null;
            }
            return new University(university);
        }
        
        public Department getDepartment() {
            if(department == null) {
                return null;
            }
            return new Department(department);
        }

        public List<Event> getEvents() {
            return new ArrayList<>(events);
        }
    }
}